package ghiyas.alimaa.fa.domain.strategy

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import ghiyas.alimaa.fa.domain.models.*
import ghiyas.alimaa.fa.data.CustomProfileRepository

enum class DistributionMode { MODE_A_NO_BREAKDOWN, MODE_COMPREHENSIVE, MODE_B_SIMPLE, MODE_C_GHIYAS, MODE_DEFAULT_MAKER, MODE_CUSTOM_BUILDER }

data class Shareholder(val name: String, val ghiyas: Double)

data class PersonNode(
    val id: String, val name: String = "", val isFemale: Boolean = false,
    val isSubDivided: Boolean = false, val subCountInput: String = "",
    val isSubBoyGirlSplit: Boolean = false, val isDetailedFurther: Boolean = false,
    val subNodes: List<PersonNode> = emptyList()
) { val weight: Double get() = if (isFemale) 0.5 else 1.0 }

data class ModeBState(val countInput: String = "", val isBoyGirlSplit: Boolean = false, val isDetailed: Boolean = false, val children: List<PersonNode> = emptyList())
data class ComprehensiveState(val rootMode: ComprehensiveMode = ComprehensiveMode.PERSON, val countLimitInput: String = "", val nodes: List<ShareholderNode> = emptyList())

data class DistributionInput(
    val poolAmount: WalnutUnit, val mode: DistributionMode, val groupName: String = "", 
    val comprehensiveState: ComprehensiveState = ComprehensiveState(), val modeBState: ModeBState = ModeBState(), 
    val shareholders: List<Shareholder> = emptyList(), val defaultStrategyTitle: String = "",
    val customProfileId: String = "", val defaultLabel: String = "سهم یکجا", val calculateZivar: Boolean = true,
    val isNimehkari: Boolean = false, val nimehkariPool: WalnutUnit = WalnutUnit.ZERO, val targetGroup: String = "کل عبدالرحیمی‌ها",
    val transferDadallah: Boolean = false, val dynamicBooleans: Map<String, Boolean> = emptyMap(),
    val dynamicAdvancedTransfers: Map<String, RuntimeTransferAction> = emptyMap()
)

object DistributionEngine {
    private val decimalMode = DecimalMode(decimalPrecision = 15, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    private fun String.toEnglishDecimals(): String = this.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9').replace('٫', '.').replace(',', '.')
    
    private fun safeParseBigDecimal(value: String, default: String = "0"): BigDecimal {
        val clean = value.toEnglishDecimals().trim()
        if (clean.isEmpty()) return BigDecimal.parseString(default)
        return try { BigDecimal.parseString(clean) } catch (e: Exception) { BigDecimal.parseString(default) }
    }

    private fun formatAmt(amount: BigDecimal): String {
        val s = amount.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).toPlainString()
        return if (s.contains(".")) s.dropLastWhile { it == '0' }.dropLastWhile { it == '.' } else s
    }

    private fun evaluateSimpleFormula(formula: String, totalPool: BigDecimal, currentShare: BigDecimal): BigDecimal {
        try {
            val expr = formula.toEnglishDecimals().replace("[کل]", totalPool.toPlainString()).replace("[باقیمانده]", currentShare.toPlainString()).replace(" ", "")
            var currentTotal = BigDecimal.ZERO
            var currentOp = '+'
            var buffer = ""
            fun flushBuffer() {
                if (buffer.isNotEmpty()) {
                    val num = safeParseBigDecimal(buffer)
                    currentTotal = when (currentOp) { '+' -> currentTotal.add(num); '-' -> currentTotal.subtract(num); '*' -> currentTotal.multiply(num); '/' -> if (num.compareTo(BigDecimal.ZERO) != 0) currentTotal.divide(num, decimalMode) else currentTotal; else -> currentTotal }
                    buffer = ""
                }
            }
            for (char in expr) {
                if (char == '+' || char == '-' || char == '*' || char == '/') {
                    if (buffer.isEmpty() && char == '-') { buffer += "-"; continue }
                    flushBuffer()
                    currentOp = char
                } else if (char != '(' && char != ')') { buffer += char }
            }
            flushBuffer()
            return currentTotal
        } catch (e: Exception) { return BigDecimal.ZERO }
    }

    private fun processModeBTree(pool: WalnutUnit, parentName: String, countInput: String, isBoyGirlSplit: Boolean, isDetailed: Boolean, children: List<PersonNode>): List<ResultItem> {
        val count = countInput.toEnglishDecimals().toDoubleOrNull() ?: 1.0
        val validCount = if (count > 0) count else 1.0
        val baseShare = pool / validCount
        if (!isDetailed || children.isEmpty()) {
            val suffix = if (parentName.isNotEmpty()) " [$parentName]" else ""
            return if ((validCount % 1.0 != 0.0) || isBoyGirlSplit) listOf(ResultItem("سهم هر پسر$suffix", baseShare), ResultItem("سهم هر دختر$suffix", baseShare / 2.0)) else listOf(ResultItem("سهم هر فرد$suffix", baseShare))
        } else {
            val results = mutableListOf<ResultItem>()
            children.forEach { child ->
                val childPool = baseShare * child.weight
                val label = if (child.name.isNotBlank()) child.name else "ناشناس"
                val fullLabel = if (parentName.isNotBlank()) "$label (زیرمجموعه $parentName)" else label
                if (!child.isSubDivided) results.add(ResultItem("${if (child.isFemale) "سهم دختر" else "سهم پسر"} $fullLabel", childPool))
                else results.addAll(processModeBTree(childPool, fullLabel, child.subCountInput, child.isSubBoyGirlSplit, child.isDetailedFurther, child.subNodes))
            }
            return results
        }
    }

    data class TransferRecord(val counterpartName: String, val amount: BigDecimal)

    private fun processComprehensiveTree(pool: BigDecimal, nodes: List<ShareholderNode>, rootMode: ComprehensiveMode, parentName: String = ""): List<ResultItem> {
        val activeNodes = nodes.filter { !it.isExcluded }
        if (activeNodes.isEmpty()) return emptyList()
        var totalWeight = BigDecimal.ZERO
        val nodeWeights = mutableMapOf<String, BigDecimal>()
        for (node in activeNodes) {
            val weight = when (rootMode) { ComprehensiveMode.PERSON -> { val base = safeParseBigDecimal(node.rawValue, "1"); if (node.isFemale) base.multiply(safeParseBigDecimal("0.5")) else base }; ComprehensiveMode.SHARE_QYAS, ComprehensiveMode.PERCENTAGE -> safeParseBigDecimal(node.rawValue, "0") }
            nodeWeights[node.id] = weight
            totalWeight = totalWeight.add(weight)
        }
        val denominator = if (rootMode == ComprehensiveMode.PERCENTAGE) safeParseBigDecimal("100") else totalWeight
        
        val rawShares = mutableMapOf<String, BigDecimal>()
        val finalShares = mutableMapOf<String, BigDecimal>()
        
        if (denominator.compareTo(BigDecimal.ZERO) > 0) {
            for (node in activeNodes) {
                val weight = nodeWeights[node.id] ?: BigDecimal.ZERO
                val share = try { pool.multiply(weight).divide(denominator, decimalMode) } catch (e: Exception) { val pDouble = pool.doubleValue(false); val wDouble = weight.doubleValue(false); val dDouble = denominator.doubleValue(false); BigDecimal.fromDouble((pDouble * wDouble) / dDouble) }
                rawShares[node.id] = share
                finalShares[node.id] = share
            }
        }

        val compTransfersInc = mutableMapOf<String, MutableList<TransferRecord>>()
        val compTransfersOut = mutableMapOf<String, MutableList<TransferRecord>>()
        
        for (node in activeNodes) {
            val targetId = node.transferredToId
            if (targetId.isNotEmpty() && targetId != node.id && rawShares.containsKey(targetId)) {
                val amount = finalShares[node.id] ?: BigDecimal.ZERO
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    finalShares[node.id] = BigDecimal.ZERO 
                    finalShares[targetId] = (finalShares[targetId] ?: BigDecimal.ZERO).add(amount) 
                    
                    val sourceName = activeNodes.find { it.id == node.id }?.name ?: "نامشخص"
                    val targetName = activeNodes.find { it.id == targetId }?.name ?: "نامشخص"
                    
                    compTransfersOut.getOrPut(node.id) { mutableListOf() }.add(TransferRecord(targetName, amount))
                    compTransfersInc.getOrPut(targetId) { mutableListOf() }.add(TransferRecord(sourceName, amount))
                }
            }
        }
        
        val results = mutableListOf<ResultItem>()
        for (node in activeNodes) {
            val baseShare = rawShares[node.id] ?: BigDecimal.ZERO
            val finalShare = finalShares[node.id] ?: BigDecimal.ZERO
            val incTransfers = compTransfersInc[node.id] ?: emptyList()
            val outTransfers = compTransfersOut[node.id] ?: emptyList()
            
            val nodeName = node.name.ifEmpty { "ناشناس" }
            val suffix = if (parentName.isNotEmpty()) " (از $parentName)" else ""
            val fullName = "$nodeName$suffix"

            if (node.hasSubDistribution && finalShare.compareTo(BigDecimal.ZERO) > 0 && node.transferredToId.isEmpty()) { 
                results.addAll(processComprehensiveTree(finalShare, node.children, node.subDistributionMode, nodeName)) 
            } else {
                if (incTransfers.isNotEmpty()) {
                    val baseFmt = formatAmt(baseShare)
                    val trLines = incTransfers.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                    val label = "جمع سهم $fullName\n• سهم خالص: $baseFmt\n$trLines"
                    results.add(ResultItem(label, WalnutUnit(finalShare.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))))
                } else {
                    val outNote = if (outTransfers.isNotEmpty()) " [-انتقال به ${outTransfers.joinToString(" و ") { it.counterpartName }}]" else ""
                    results.add(ResultItem("سهم $fullName$outNote", WalnutUnit(finalShare.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))))
                }
            }
        }
        return results
    }

    class TrackedShare(
        val id: String, val name: String, val fullName: String, val weight: BigDecimal, 
        val isDisplayOnly: Boolean, val predefinedTransfer: RuntimeTransferAction?, 
        val splitCount: Double? = null, val isBoyGirlSplit: Boolean = false, val isParentNode: Boolean = false,
        val lineage: List<String> = emptyList()
    )

    private fun processCustomProfile(pool: BigDecimal, profile: CustomProfile, dynamicBooleans: Map<String, Boolean>, dynamicAdvancedTransfers: Map<String, RuntimeTransferAction>): List<ResultItem> {
        val orderedShares = mutableListOf<TrackedShare>()
        
        fun processNodeChildren(
            nodeId: String, nodeName: String, parentFullName: String, parentLineage: List<String>, nodeTotalWeight: BigDecimal, isDisplayOnly: Boolean, predefinedTransfer: RuntimeTransferAction?,
            isSubDivided: Boolean, subDistributionType: DistributionType?, subCountInput: String, isSubBoyGirlSplit: Boolean,
            subHeadcounts: List<BuilderPersonNode>, subShareholders: List<BuilderShareholder>
        ) {
            val fullName = if (parentFullName.isEmpty()) nodeName else "$nodeName (از $parentFullName)"
            val currentLineage = parentLineage + nodeId

            if (isSubDivided) {
                orderedShares.add(TrackedShare(nodeId, nodeName, fullName, nodeTotalWeight, isDisplayOnly, predefinedTransfer, isParentNode = true, lineage = currentLineage))

                val type = subDistributionType ?: DistributionType.HEADCOUNT_BASED
                var childrenSum = BigDecimal.ZERO
                val childrenWeights = mutableListOf<BigDecimal>()
                
                if (type == DistributionType.HEADCOUNT_BASED) {
                    val activeChildren = subHeadcounts.filter { !it.hasToggle || dynamicBooleans[it.id] != false }
                    if (activeChildren.isEmpty()) {
                        val countStr = subCountInput.toEnglishDecimals()
                        val count = safeParseBigDecimal(countStr, "1").doubleValue(false)
                        val validCount = if (count > 0) count else 1.0
                        
                        val leafId = nodeId + "_leaf"
                        if (isSubBoyGirlSplit || count % 1.0 != 0.0 || count > 1.0) {
                            orderedShares.add(TrackedShare(leafId, "زیرمجموعه خرد", fullName, nodeTotalWeight, isDisplayOnly, null, splitCount = validCount, isBoyGirlSplit = isSubBoyGirlSplit || count % 1.0 != 0.0, lineage = currentLineage + leafId))
                        } else {
                            orderedShares.add(TrackedShare(leafId, "زیرمجموعه خرد", fullName, nodeTotalWeight, isDisplayOnly, null, splitCount = validCount, isBoyGirlSplit = false, lineage = currentLineage + leafId))
                        }
                        return
                    }
                    for (child in activeChildren) {
                        val cWeight = safeParseBigDecimal(child.weightInput, "1")
                        val cGender = if (child.isFemale) safeParseBigDecimal("0.5") else safeParseBigDecimal("1")
                        val cw = cWeight.multiply(cGender)
                        childrenWeights.add(cw)
                        childrenSum = childrenSum.add(cw)
                    }
                    if (childrenSum > BigDecimal.ZERO) {
                        activeChildren.forEachIndexed { index, child ->
                            val childFraction = childrenWeights[index].divide(childrenSum, decimalMode)
                            val childAssignedWeight = nodeTotalWeight.multiply(childFraction)
                            val childName = if (child.name.isNotBlank()) child.name else "ناشناس"
                            processNodeChildren(
                                child.id, childName, fullName, currentLineage, childAssignedWeight, child.isDisplayOnly, child.predefinedTransfer,
                                child.isSubDivided, child.subDistributionType, child.subCountInput, child.isSubBoyGirlSplit, child.subNodes, child.subShareholders
                            )
                        }
                    } else {
                        orderedShares.add(TrackedShare(nodeId, nodeName, fullName, nodeTotalWeight, isDisplayOnly, predefinedTransfer, lineage = currentLineage))
                    }
                } else {
                    val activeChildren = subShareholders.filter { !it.hasToggle || dynamicBooleans[it.id] != false }
                    if (activeChildren.isEmpty()) {
                        orderedShares.add(TrackedShare(nodeId, nodeName, fullName, nodeTotalWeight, isDisplayOnly, predefinedTransfer, lineage = currentLineage))
                        return
                    }
                    val isPercentage = type == DistributionType.PERCENTAGE
                    for (child in activeChildren) {
                        val cw = safeParseBigDecimal(child.shareInput, if (isPercentage) "0" else "1")
                        childrenWeights.add(cw)
                        childrenSum = childrenSum.add(cw)
                    }
                    if (childrenSum > BigDecimal.ZERO) {
                        activeChildren.forEachIndexed { index, child ->
                            val childFraction = childrenWeights[index].divide(childrenSum, decimalMode)
                            val childAssignedWeight = nodeTotalWeight.multiply(childFraction)
                            val childName = if (child.name.isNotBlank()) child.name else "ناشناس"
                            processNodeChildren(
                                child.id, childName, fullName, currentLineage, childAssignedWeight, child.isDisplayOnly, child.predefinedTransfer,
                                child.isSubDivided, child.subDistributionType, child.subCountInput, child.isSubBoyGirlSplit, child.subHeadcounts, child.subNodes
                            )
                        }
                    } else {
                        orderedShares.add(TrackedShare(nodeId, nodeName, fullName, nodeTotalWeight, isDisplayOnly, predefinedTransfer, lineage = currentLineage))
                    }
                }
            } else {
                orderedShares.add(TrackedShare(nodeId, nodeName, fullName, nodeTotalWeight, isDisplayOnly, predefinedTransfer, lineage = currentLineage))
            }
        }

        fun traverseBlocks(blocks: List<CustomBlock>) {
            for (block in blocks) {
                when (block) {
                    is MemberBlock -> {
                        when (block.distributionType) {
                            DistributionType.HEADCOUNT_BASED -> {
                                for (node in block.headcountNodes) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val bw = safeParseBigDecimal(node.weightInput, "1")
                                    val gf = if (node.isFemale) safeParseBigDecimal("0.5") else safeParseBigDecimal("1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, "", emptyList(), bw.multiply(gf), node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subCountInput, node.isSubBoyGirlSplit, node.subNodes, node.subShareholders)
                                }
                            }
                            DistributionType.GHIYAS_BASED, DistributionType.PERCENTAGE, DistributionType.CUSTOM_UNIT -> {
                                val isPct = block.distributionType != DistributionType.GHIYAS_BASED
                                val list = if (isPct) block.percentageShareholders else block.ghiyasShareholders
                                for (node in list) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val tw = safeParseBigDecimal(node.shareInput, if(isPct) "0" else "1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, "", emptyList(), tw, node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subCountInput, node.isSubBoyGirlSplit, node.subHeadcounts, node.subNodes)
                                }
                            }
                        }
                        traverseBlocks(block.childBlocks)
                    }
                    is PartnerBlock -> {
                        when (block.distributionType) {
                            DistributionType.HEADCOUNT_BASED -> {
                                for (node in block.headcountNodes) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val bw = safeParseBigDecimal(node.weightInput, "1")
                                    val gf = if (node.isFemale) safeParseBigDecimal("0.5") else safeParseBigDecimal("1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, "", emptyList(), bw.multiply(gf), node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subCountInput, node.isSubBoyGirlSplit, node.subNodes, node.subShareholders)
                                }
                            }
                            DistributionType.GHIYAS_BASED, DistributionType.PERCENTAGE, DistributionType.CUSTOM_UNIT -> {
                                val isPct = block.distributionType != DistributionType.GHIYAS_BASED
                                val list = if (isPct) block.percentageShareholders else block.ghiyasShareholders
                                for (node in list) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val tw = safeParseBigDecimal(node.shareInput, if(isPct) "0" else "1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, "", emptyList(), tw, node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subCountInput, node.isSubBoyGirlSplit, node.subHeadcounts, node.subNodes)
                                }
                            }
                        }
                        traverseBlocks(block.siblingBlocks)
                    }
                    is StageBlock -> traverseBlocks(block.childBlocks)
                    is ConditionGate -> if (dynamicBooleans[block.block_id] == true) traverseBlocks(block.childBlocks)
                    is BaseInputBlock -> traverseBlocks(block.childBlocks)
                    else -> {} 
                }
            }
        }

        traverseBlocks(profile.rootBlocks)

        if (orderedShares.isEmpty()) return listOf(ResultItem("سهم ${profile.name} (بدون شریک فعال)", WalnutUnit(pool.doubleValue(false))))

        var totalWeight = BigDecimal.ZERO
        for (item in orderedShares) {
            if (!item.isParentNode && !item.isDisplayOnly) {
                totalWeight = totalWeight.add(item.weight)
            }
        }

        val baseSharesMap = mutableMapOf<String, BigDecimal>()
        val finalSharesMap = mutableMapOf<String, BigDecimal>()
        
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            for (item in orderedShares) {
                if (!item.isParentNode) {
                    val share = pool.multiply(item.weight).divide(totalWeight, decimalMode)
                    baseSharesMap[item.id] = share
                    finalSharesMap[item.id] = share
                }
            }
        } else {
            for (item in orderedShares) {
                if (!item.isParentNode) {
                    baseSharesMap[item.id] = BigDecimal.ZERO
                    finalSharesMap[item.id] = BigDecimal.ZERO
                }
            }
        }

        val transfersIncoming = mutableMapOf<String, MutableList<TransferRecord>>()
        val transfersOutgoing = mutableMapOf<String, MutableList<TransferRecord>>()
        
        val allActions = mutableMapOf<String, RuntimeTransferAction>()
        for (item in orderedShares) {
            val realId = item.lineage.lastOrNull() ?: item.id
            if (item.predefinedTransfer != null && !allActions.containsKey(realId)) {
                allActions[realId] = item.predefinedTransfer
            }
        }
        for ((k, v) in dynamicAdvancedTransfers) {
            allActions[k] = v
        }

        fun getLeavesOf(nodeId: String): List<TrackedShare> {
            return orderedShares.filter { !it.isParentNode && nodeId in it.lineage }
        }

        for ((sourceId, action) in allActions) {
            if (action.targets.isEmpty()) continue
            
            val sourceNode = orderedShares.find { it.id == sourceId } ?: continue
            val sourceLeaves = getLeavesOf(sourceId)
            if (sourceLeaves.isEmpty()) continue
            
            var sourceBalance = BigDecimal.ZERO
            sourceLeaves.forEach { sourceBalance = sourceBalance.add(finalSharesMap[it.id] ?: BigDecimal.ZERO) }
            
            if (sourceBalance <= BigDecimal.ZERO) continue
            
            var transferAmount = BigDecimal.ZERO
            when (action.sourceAmountType) {
                TransferAmountType.FULL -> transferAmount = sourceBalance
                TransferAmountType.FIXED -> { val parsed = safeParseBigDecimal(action.sourceAmountValue); transferAmount = if (parsed > sourceBalance) sourceBalance else parsed }
                TransferAmountType.PERCENTAGE -> { val pct = safeParseBigDecimal(action.sourceAmountValue).divide(safeParseBigDecimal("100"), decimalMode); transferAmount = sourceBalance.multiply(pct) }
                TransferAmountType.FORMULA -> { val calculated = evaluateSimpleFormula(action.sourceAmountValue, pool, sourceBalance); transferAmount = if (calculated > sourceBalance) sourceBalance else if (calculated < BigDecimal.ZERO) BigDecimal.ZERO else calculated }
            }

            if (transferAmount.compareTo(BigDecimal.ZERO) > 0) {
                var sourceWeights = BigDecimal.ZERO
                sourceLeaves.forEach { sourceWeights = sourceWeights.add(it.weight) }
                
                var targetsTotalRuleWeight = BigDecimal.ZERO
                val targetGroupWeights = mutableMapOf<String, BigDecimal>()
                
                val resolvedTargets = mutableListOf<Pair<AdvancedTransferTarget, Pair<TrackedShare, List<TrackedShare>>>>()
                for (t in action.targets) {
                    val targetNode = orderedShares.find { it.id == t.targetId }
                    val targetLeaves = getLeavesOf(t.targetId)
                    if (targetNode != null && targetLeaves.isNotEmpty()) {
                        resolvedTargets.add(t to (targetNode to targetLeaves))
                        val ruleWeight = when (action.distributionRule) {
                            TransferDistributionRule.EQUAL -> BigDecimal.ONE
                            TransferDistributionRule.BOY_GIRL -> if (t.isFemale) safeParseBigDecimal("0.5") else BigDecimal.ONE
                            TransferDistributionRule.BY_ORIGINAL_SHARE -> {
                                var sumW = BigDecimal.ZERO
                                targetLeaves.forEach { sumW = sumW.add(it.weight) }
                                sumW
                            }
                            TransferDistributionRule.CUSTOM_PERCENTAGE -> safeParseBigDecimal(t.customPercentage, "0")
                        }
                        targetGroupWeights[t.targetId] = ruleWeight
                        targetsTotalRuleWeight = targetsTotalRuleWeight.add(ruleWeight)
                    }
                }

                if (targetsTotalRuleWeight.compareTo(BigDecimal.ZERO) > 0) {
                    val unitShare = transferAmount.divide(targetsTotalRuleWeight, decimalMode)
                    val targetNames = resolvedTargets.map { it.second.first.name }
                    
                    if (sourceWeights > BigDecimal.ZERO) {
                        for (leaf in sourceLeaves) {
                            val deduction = transferAmount.multiply(leaf.weight).divide(sourceWeights, decimalMode)
                            finalSharesMap[leaf.id] = (finalSharesMap[leaf.id] ?: BigDecimal.ZERO).subtract(deduction)
                        }
                    }
                    transfersOutgoing.getOrPut(sourceNode.id) { mutableListOf() }.add(TransferRecord(targetNames.joinToString(" و "), transferAmount))
                    
                    for ((t, targetPair) in resolvedTargets) {
                        val targetNode = targetPair.first
                        val targetLeaves = targetPair.second
                        val groupShare = unitShare.multiply(targetGroupWeights[t.targetId] ?: BigDecimal.ZERO)
                        
                        var leavesSumW = BigDecimal.ZERO
                        targetLeaves.forEach { leavesSumW = leavesSumW.add(it.weight) }
                        if (leavesSumW > BigDecimal.ZERO) {
                            for (leaf in targetLeaves) {
                                val leafShare = groupShare.multiply(leaf.weight).divide(leavesSumW, decimalMode)
                                finalSharesMap[leaf.id] = (finalSharesMap[leaf.id] ?: BigDecimal.ZERO).add(leafShare)
                            }
                        }
                        // کلید حل باگ نمایش انتقال: تگ انتقال را فقط به خود شخص/والدِ هدف می‌چسبانیم تا در زیرمجموعه‌های او چاپ نشود
                        transfersIncoming.getOrPut(targetNode.id) { mutableListOf() }.add(TransferRecord(sourceNode.name, groupShare))
                    }
                }
            }
        }

        for (item in orderedShares) {
            if (item.isParentNode) {
                val pLeaves = getLeavesOf(item.id)
                var pBase = BigDecimal.ZERO
                var pFinal = BigDecimal.ZERO
                for (leaf in pLeaves) {
                    pBase = pBase.add(baseSharesMap[leaf.id] ?: BigDecimal.ZERO)
                    pFinal = pFinal.add(finalSharesMap[leaf.id] ?: BigDecimal.ZERO)
                }
                baseSharesMap[item.id] = pBase
                finalSharesMap[item.id] = pFinal
            }
        }

        val results = mutableListOf<ResultItem>()

        for (item in orderedShares) {
            val base = baseSharesMap[item.id] ?: BigDecimal.ZERO
            val final = finalSharesMap[item.id] ?: BigDecimal.ZERO
            val incTransfers = transfersIncoming[item.id] ?: emptyList()
            val outTransfers = transfersOutgoing[item.id] ?: emptyList()
            
            val isDisplayLabel = if (item.isDisplayOnly) " (محاسبه نمایشی)" else ""
            val outNote = if (outTransfers.isNotEmpty()) " [-انتقال به ${outTransfers.joinToString(" و ") { it.counterpartName }}]" else ""

            if (item.isParentNode) {
                val printName = item.fullName
                if (incTransfers.isNotEmpty()) {
                    val baseFmt = formatAmt(base)
                    val trLines = incTransfers.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                    val label = "جمع سهم کل $printName$isDisplayLabel\n• سهم خالص: $baseFmt\n$trLines"
                    results.add(ResultItem(label, WalnutUnit(final.doubleValue(false))))
                } else {
                    results.add(ResultItem("سهم کل $printName$isDisplayLabel$outNote", WalnutUnit(base.doubleValue(false))))
                }
            } else {
                if (item.splitCount != null) {
                    val split = BigDecimal.fromDouble(item.splitCount)
                    val indBase = base.divide(split, decimalMode)
                    val indFinal = final.divide(split, decimalMode)
                    val mappedIncTransfers = incTransfers.map { it.copy(amount = it.amount.divide(split, decimalMode)) }
                    
                    if (item.isBoyGirlSplit) {
                        val half = safeParseBigDecimal("0.5")
                        val boyBase = indBase
                        val boyFinal = indFinal
                        val boyInc = mappedIncTransfers
                        val boyLabel = "(از ${item.fullName})"
                        
                        if (boyInc.isNotEmpty()) {
                            val baseFmt = formatAmt(boyBase)
                            val trLines = boyInc.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                            val label = "جمع سهم هر پسر $boyLabel$isDisplayLabel\n• سهم خالص: $baseFmt\n$trLines"
                            results.add(ResultItem(label, WalnutUnit(boyFinal.doubleValue(false))))
                        } else {
                            results.add(ResultItem("سهم هر پسر $boyLabel$isDisplayLabel$outNote", WalnutUnit(boyFinal.doubleValue(false))))
                        }
                        
                        val girlBase = indBase.multiply(half)
                        val girlFinal = indFinal.multiply(half)
                        val girlInc = mappedIncTransfers.map { it.copy(amount = it.amount.multiply(half)) }
                        
                        if (girlInc.isNotEmpty()) {
                            val baseFmt = formatAmt(girlBase)
                            val trLines = girlInc.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                            val label = "جمع سهم هر دختر $boyLabel$isDisplayLabel\n• سهم خالص: $baseFmt\n$trLines"
                            results.add(ResultItem(label, WalnutUnit(girlFinal.doubleValue(false))))
                        } else {
                            results.add(ResultItem("سهم هر دختر $boyLabel$isDisplayLabel$outNote", WalnutUnit(girlFinal.doubleValue(false))))
                        }
                        
                    } else {
                        val label = "(از ${item.fullName})"
                        if (mappedIncTransfers.isNotEmpty()) {
                            val baseFmt = formatAmt(indBase)
                            val trLines = mappedIncTransfers.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                            val fullLabel = "جمع سهم هر فرد $label$isDisplayLabel\n• سهم خالص: $baseFmt\n$trLines"
                            results.add(ResultItem(fullLabel, WalnutUnit(indFinal.doubleValue(false))))
                        } else {
                            results.add(ResultItem("سهم هر فرد $label$isDisplayLabel$outNote", WalnutUnit(indFinal.doubleValue(false))))
                        }
                    }
                } else {
                    if (incTransfers.isNotEmpty()) {
                        val baseFmt = formatAmt(base)
                        val trLines = incTransfers.joinToString("\n") { "• انتقالی از ${it.counterpartName}: ${formatAmt(it.amount)}" }
                        val label = "جمع سهم ${item.fullName}$isDisplayLabel\n• سهم خالص: $baseFmt\n$trLines"
                        results.add(ResultItem(label, WalnutUnit(final.doubleValue(false))))
                    } else {
                        results.add(ResultItem("سهم ${item.fullName}$isDisplayLabel$outNote", WalnutUnit(final.doubleValue(false))))
                    }
                }
            }
        }
        return results
    }

    fun calculate(input: DistributionInput): List<ResultItem> {
        return when (input.mode) {
            DistributionMode.MODE_A_NO_BREAKDOWN -> listOf(ResultItem(if (input.groupName.isNotBlank()) input.groupName else input.defaultLabel, input.poolAmount))
            DistributionMode.MODE_COMPREHENSIVE -> processComprehensiveTree(BigDecimal.fromDouble(input.poolAmount.value), input.comprehensiveState.nodes, input.comprehensiveState.rootMode, "")
            DistributionMode.MODE_B_SIMPLE -> processModeBTree(input.poolAmount, "", input.modeBState.countInput, input.modeBState.isBoyGirlSplit, input.modeBState.isDetailed, input.modeBState.children)
            DistributionMode.MODE_C_GHIYAS -> {
                val totalGhiyas = input.shareholders.sumOf { it.ghiyas }
                val valuePerGhiyas = if (totalGhiyas > 0) input.poolAmount / totalGhiyas else WalnutUnit.ZERO
                input.shareholders.map { ResultItem("سهم ${it.name}", valuePerGhiyas * it.ghiyas) }
            }
            DistributionMode.MODE_DEFAULT_MAKER -> DefaultCalculationsRegistry.strategies.find { it.title == input.defaultStrategyTitle }?.calculate(input) ?: emptyList()
            DistributionMode.MODE_CUSTOM_BUILDER -> {
                val customProfile = try { CustomProfileRepository.getAllProfiles().find { it.id == input.customProfileId } } catch (e: Exception) { null }
                if (customProfile != null) { processCustomProfile(BigDecimal.fromDouble(input.poolAmount.value), customProfile, input.dynamicBooleans, input.dynamicAdvancedTransfers) } 
                else { listOf(ResultItem("سهم الگو (پیدا نشد)", input.poolAmount)) }
            }
        }
    }
}
