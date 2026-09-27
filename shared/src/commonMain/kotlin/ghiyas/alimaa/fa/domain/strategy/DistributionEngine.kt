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

    // (توابع ModeB و Comprehensive بدون تغییر در اینجا می‌مانند تا حجم پیام مدیریت شود)
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
        if (denominator.compareTo(BigDecimal.ZERO) > 0) {
            for (node in activeNodes) {
                val weight = nodeWeights[node.id] ?: BigDecimal.ZERO
                val share = try { pool.multiply(weight).divide(denominator, decimalMode) } catch (e: Exception) { val pDouble = pool.doubleValue(false); val wDouble = weight.doubleValue(false); val dDouble = denominator.doubleValue(false); BigDecimal.fromDouble((pDouble * wDouble) / dDouble) }
                rawShares[node.id] = share
            }
        }
        val finalShares = rawShares.toMutableMap()
        val transferNotes = mutableMapOf<String, String>()
        for (node in activeNodes) {
            val targetId = node.transferredToId
            if (targetId.isNotEmpty() && targetId != node.id && rawShares.containsKey(targetId)) {
                val amount = finalShares[node.id] ?: BigDecimal.ZERO
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    finalShares[node.id] = BigDecimal.ZERO 
                    finalShares[targetId] = (finalShares[targetId] ?: BigDecimal.ZERO).add(amount) 
                    transferNotes[node.id] = " (سهم منتقل شد)"
                    transferNotes[targetId] = (transferNotes[targetId] ?: "") + " [انتقالی]"
                }
            }
        }
        val results = mutableListOf<ResultItem>()
        for (node in activeNodes) {
            val share = finalShares[node.id] ?: BigDecimal.ZERO
            val nodeName = node.name.ifEmpty { "ناشناس" }
            val fullLabel = "سهم ${if (parentName.isNotEmpty()) "$nodeName (از $parentName)" else nodeName}${transferNotes[node.id] ?: ""}"
            if (node.hasSubDistribution && share.compareTo(BigDecimal.ZERO) > 0 && node.transferredToId.isEmpty()) { results.addAll(processComprehensiveTree(share, node.children, node.subDistributionMode, nodeName)) } 
            else { results.add(ResultItem(fullLabel, WalnutUnit(share.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false)))) }
        }
        return results
    }

    class TrackedShare(val id: String, val name: String, val weight: BigDecimal, val isDisplayOnly: Boolean, val predefinedTransfer: RuntimeTransferAction?)

    private fun processCustomProfile(pool: BigDecimal, profile: CustomProfile, dynamicBooleans: Map<String, Boolean>, dynamicAdvancedTransfers: Map<String, RuntimeTransferAction>): List<ResultItem> {
        val rawShares = mutableListOf<TrackedShare>()

        // متد ایزوله و بازگشتی برای پردازش هر گره درختی بدون تداخل با سایر شاخه‌ها
        fun processNodeChildren(
            nodeId: String, nodeName: String, nodeTotalWeight: BigDecimal, isDisplayOnly: Boolean, predefinedTransfer: RuntimeTransferAction?,
            isSubDivided: Boolean, subDistributionType: DistributionType?,
            subHeadcounts: List<BuilderPersonNode>, subShareholders: List<BuilderShareholder>
        ) {
            if (isSubDivided) {
                val type = subDistributionType ?: DistributionType.HEADCOUNT_BASED
                var childrenSum = BigDecimal.ZERO
                val childrenWeights = mutableListOf<BigDecimal>()
                
                if (type == DistributionType.HEADCOUNT_BASED) {
                    val activeChildren = subHeadcounts.filter { !it.hasToggle || dynamicBooleans[it.id] != false }
                    if (activeChildren.isEmpty()) {
                        rawShares.add(TrackedShare(nodeId, nodeName, nodeTotalWeight, isDisplayOnly, predefinedTransfer))
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
                            val fullName = "$childName (از $nodeName)" // برچسب برای گروه‌بندی کادرهای رنگی در UI
                            
                            processNodeChildren(
                                child.id, fullName, childAssignedWeight, child.isDisplayOnly, child.predefinedTransfer,
                                child.isSubDivided, child.subDistributionType, child.subNodes, child.subShareholders
                            )
                        }
                    } else {
                        rawShares.add(TrackedShare(nodeId, nodeName, nodeTotalWeight, isDisplayOnly, predefinedTransfer))
                    }
                } else {
                    val activeChildren = subShareholders.filter { !it.hasToggle || dynamicBooleans[it.id] != false }
                    if (activeChildren.isEmpty()) {
                        rawShares.add(TrackedShare(nodeId, nodeName, nodeTotalWeight, isDisplayOnly, predefinedTransfer))
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
                            val fullName = "$childName (از $nodeName)"
                            
                            processNodeChildren(
                                child.id, fullName, childAssignedWeight, child.isDisplayOnly, child.predefinedTransfer,
                                child.isSubDivided, child.subDistributionType, child.subHeadcounts, child.subNodes
                            )
                        }
                    } else {
                        rawShares.add(TrackedShare(nodeId, nodeName, nodeTotalWeight, isDisplayOnly, predefinedTransfer))
                    }
                }
            } else {
                rawShares.add(TrackedShare(nodeId, nodeName, nodeTotalWeight, isDisplayOnly, predefinedTransfer))
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
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, bw.multiply(gf), node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subNodes, node.subShareholders)
                                }
                            }
                            DistributionType.GHIYAS_BASED, DistributionType.PERCENTAGE, DistributionType.CUSTOM_UNIT -> {
                                val isPct = block.distributionType != DistributionType.GHIYAS_BASED
                                val list = if (isPct) block.percentageShareholders else block.ghiyasShareholders
                                for (node in list) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val tw = safeParseBigDecimal(node.shareInput, if(isPct) "0" else "1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, tw, node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subHeadcounts, node.subNodes)
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
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, bw.multiply(gf), node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subNodes, node.subShareholders)
                                }
                            }
                            DistributionType.GHIYAS_BASED, DistributionType.PERCENTAGE, DistributionType.CUSTOM_UNIT -> {
                                val isPct = block.distributionType != DistributionType.GHIYAS_BASED
                                val list = if (isPct) block.percentageShareholders else block.ghiyasShareholders
                                for (node in list) {
                                    if (node.hasToggle && dynamicBooleans[node.id] == false) continue
                                    val tw = safeParseBigDecimal(node.shareInput, if(isPct) "0" else "1")
                                    processNodeChildren(node.id, node.name.ifEmpty {"ناشناس"}, tw, node.isDisplayOnly, node.predefinedTransfer, node.isSubDivided, node.subDistributionType, node.subHeadcounts, node.subNodes)
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

        if (rawShares.isEmpty()) return listOf(ResultItem("سهم ${profile.name} (بدون شریک فعال)", WalnutUnit(pool.doubleValue(false))))

        // حذف نمایشی‌ها از مخرج کسر تا جمع کل به هم نریزد
        var totalWeight = BigDecimal.ZERO
        for (item in rawShares) {
            if (!item.isDisplayOnly) {
                totalWeight = totalWeight.add(item.weight)
            }
        }

        val sharesMap = mutableMapOf<String, BigDecimal>()
        
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            for (item in rawShares) {
                val share = try {
                    pool.multiply(item.weight).divide(totalWeight, decimalMode)
                } catch (e: Exception) {
                    val p = pool.doubleValue(false)
                    val w = item.weight.doubleValue(false)
                    val t = totalWeight.doubleValue(false)
                    BigDecimal.fromDouble((p * w) / t)
                }
                sharesMap[item.id] = share
            }
        } else {
            for (item in rawShares) sharesMap[item.id] = BigDecimal.ZERO
        }

        val transferNotes = mutableMapOf<String, String>()
        for (item in rawShares) {
            val action = dynamicAdvancedTransfers[item.id] ?: item.predefinedTransfer
            if (action != null && action.targets.isNotEmpty()) {
                val currentBalance = sharesMap[item.id] ?: BigDecimal.ZERO
                if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
                    
                    var transferAmount = BigDecimal.ZERO
                    when (action.sourceAmountType) {
                        TransferAmountType.FULL -> transferAmount = currentBalance
                        TransferAmountType.FIXED -> {
                            val parsed = safeParseBigDecimal(action.sourceAmountValue)
                            transferAmount = if (parsed > currentBalance) currentBalance else parsed
                        }
                        TransferAmountType.PERCENTAGE -> {
                            val pct = safeParseBigDecimal(action.sourceAmountValue).divide(safeParseBigDecimal("100"), decimalMode)
                            transferAmount = currentBalance.multiply(pct)
                        }
                        TransferAmountType.FORMULA -> {
                            val calculated = evaluateSimpleFormula(action.sourceAmountValue, pool, currentBalance)
                            transferAmount = if (calculated > currentBalance) currentBalance else if (calculated < BigDecimal.ZERO) BigDecimal.ZERO else calculated
                        }
                    }

                    if (transferAmount.compareTo(BigDecimal.ZERO) > 0) {
                        sharesMap[item.id] = currentBalance.subtract(transferAmount)
                        var targetsTotalWeight = BigDecimal.ZERO
                        action.targets.forEach { t ->
                            val tWeight = when (action.distributionRule) {
                                TransferDistributionRule.EQUAL -> BigDecimal.ONE
                                TransferDistributionRule.BOY_GIRL -> if (t.isFemale) safeParseBigDecimal("0.5") else BigDecimal.ONE
                                TransferDistributionRule.BY_ORIGINAL_SHARE -> rawShares.find { r -> r.id == t.targetId }?.weight ?: BigDecimal.ZERO
                                TransferDistributionRule.CUSTOM_PERCENTAGE -> safeParseBigDecimal(t.customPercentage, "0")
                            }
                            targetsTotalWeight = targetsTotalWeight.add(tWeight)
                        }

                        if (targetsTotalWeight.compareTo(BigDecimal.ZERO) > 0) {
                            val unitShare = transferAmount.divide(targetsTotalWeight, decimalMode)
                            action.targets.forEach { t ->
                                val tWeight = when (action.distributionRule) {
                                    TransferDistributionRule.EQUAL -> BigDecimal.ONE
                                    TransferDistributionRule.BOY_GIRL -> if (t.isFemale) safeParseBigDecimal("0.5") else BigDecimal.ONE
                                    TransferDistributionRule.BY_ORIGINAL_SHARE -> rawShares.find { r -> r.id == t.targetId }?.weight ?: BigDecimal.ZERO
                                    TransferDistributionRule.CUSTOM_PERCENTAGE -> safeParseBigDecimal(t.customPercentage, "0")
                                }
                                val tAmount = unitShare.multiply(tWeight)
                                sharesMap[t.targetId] = (sharesMap[t.targetId] ?: BigDecimal.ZERO).add(tAmount)
                                
                                val amountTypeStr = when(action.sourceAmountType) { TransferAmountType.FULL -> "کامل"; TransferAmountType.PERCENTAGE -> "درصدی"; TransferAmountType.FIXED -> "ثابت"; TransferAmountType.FORMULA -> "فرمولی" }
                                transferNotes[t.targetId] = (transferNotes[t.targetId] ?: "") + " [+انتقالی $amountTypeStr از ${item.name.substringBefore(" (از")}]"
                            }
                            transferNotes[item.id] = (transferNotes[item.id] ?: "") + " [-انتقال داده شده]"
                        }
                    }
                }
            }
        }

        val results = mutableListOf<ResultItem>()
        for (item in rawShares) {
            val finalShare = sharesMap[item.id] ?: BigDecimal.ZERO
            val isDisplayLabel = if (item.isDisplayOnly) " (محاسبه نمایشی)" else ""
            val fullLabel = "سهم ${item.name}$isDisplayLabel${transferNotes[item.id] ?: ""}"
            results.add(ResultItem(fullLabel, WalnutUnit(finalShare.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))))
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
