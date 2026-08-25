package ir.ghiyas.alimaa.domain.strategy

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.models.ShareholderNode
import ir.ghiyas.alimaa.domain.models.ComprehensiveMode

enum class DistributionMode {
    MODE_A_NO_BREAKDOWN, 
    MODE_COMPREHENSIVE, 
    MODE_B_SIMPLE, // حفظ برای سازگاری عقبگرد (Legacy)
    MODE_C_GHIYAS, // حفظ برای سازگاری عقبگرد (Legacy)
    MODE_DEFAULT_MAKER, 
    MODE_CUSTOM_BUILDER
}

// === مدل‌های قدیمی برای جلوگیری از کرش در ExpenseStage و تاریخچه ===
data class Shareholder(val name: String, val ghiyas: Double)

data class PersonNode(
    val id: String, val name: String = "", val isFemale: Boolean = false,
    val isSubDivided: Boolean = false, val subCountInput: String = "",
    val isSubBoyGirlSplit: Boolean = false, val isDetailedFurther: Boolean = false,
    val subNodes: List<PersonNode> = emptyList()
) { val weight: Double get() = if (isFemale) 0.5 else 1.0 }

data class ModeBState(
    val countInput: String = "", val isBoyGirlSplit: Boolean = false,
    val isDetailed: Boolean = false, val children: List<PersonNode> = emptyList()
)
// =================================================================

data class ComprehensiveState(
    val rootMode: ComprehensiveMode = ComprehensiveMode.PERSON,
    val countLimitInput: String = "", 
    val nodes: List<ShareholderNode> = emptyList()
)

data class DistributionInput(
    val poolAmount: WalnutUnit,
    val mode: DistributionMode,
    val groupName: String = "", 
    val comprehensiveState: ComprehensiveState = ComprehensiveState(),
    val modeBState: ModeBState = ModeBState(), // Legacy
    val shareholders: List<Shareholder> = emptyList(), // Legacy
    val defaultStrategyTitle: String = "",
    val customProfileId: String = "", 
    val defaultLabel: String = "سهم یکجا",
    val calculateZivar: Boolean = true,
    val isNimehkari: Boolean = false,
    val nimehkariPool: WalnutUnit = WalnutUnit.ZERO,
    val targetGroup: String = "کل عبدالرحیمی‌ها",
    val transferDadallah: Boolean = false
)

object DistributionEngine {

    // حفظ متد قدیمی برای کارکرد صحیح سایر مراحل
    private fun processModeBTree(pool: WalnutUnit, parentName: String, countInput: String, isBoyGirlSplit: Boolean, isDetailed: Boolean, children: List<PersonNode>): List<ResultItem> {
        val count = countInput.toDoubleOrNull() ?: 1.0
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

    /**
     * موتور بازگشتی تسهیم جامع (نفر، سهم، درصد) با دقت بالا و منطق انتقال
     */
    private fun processComprehensiveTree(pool: BigDecimal, nodes: List<ShareholderNode>, rootMode: ComprehensiveMode, parentName: String = ""): List<ResultItem> {
        val activeNodes = nodes.filter { it.isActive }
        if (activeNodes.isEmpty()) return emptyList()

        var totalWeight = BigDecimal.ZERO
        val nodeWeights = mutableMapOf<String, BigDecimal>()

        for (node in activeNodes) {
            val weight = when (rootMode) {
                ComprehensiveMode.PERSON -> {
                    val base = BigDecimal.parseString(node.rawValue.ifEmpty { "1" })
                    if (node.isFemale) base.multiply(BigDecimal.parseString("0.5")) else base
                }
                ComprehensiveMode.SHARE_QYAS, ComprehensiveMode.PERCENTAGE -> BigDecimal.parseString(node.rawValue.ifEmpty { "0" })
            }
            nodeWeights[node.id] = weight
            totalWeight = totalWeight.add(weight)
        }

        val denominator = if (rootMode == ComprehensiveMode.PERCENTAGE) BigDecimal.parseString("100") else totalWeight
        val rawShares = mutableMapOf<String, BigDecimal>()
        
        // بای‌پس قطعی کامپایلر: پیدا کردن داینامیکِ حالتِ گرد کردن بدون استفاده از نام ثابت
        val safeHalfUpMode = RoundingMode.values().firstOrNull { it.name.contains("HALF") } ?: RoundingMode.NONE
        val decMode = DecimalMode(decimalPrecision = 15, roundingMode = safeHalfUpMode)
        
        if (denominator.compareTo(BigDecimal.ZERO) > 0) {
            for (node in activeNodes) {
                val weight = nodeWeights[node.id] ?: BigDecimal.ZERO
                // استفاده از حالت داینامیک برای جلوگیری از خطای Unresolved Reference
                val share = pool.multiply(weight).divide(denominator, decMode).roundToDigitPositionAfterDecimalPoint(3, safeHalfUpMode)
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

            if (node.hasSubDistribution && share.compareTo(BigDecimal.ZERO) > 0 && node.transferredToId.isEmpty()) {
                results.addAll(processComprehensiveTree(share, node.children, node.subDistributionMode, nodeName))
            } else {
                results.add(ResultItem(fullLabel, WalnutUnit(share.doubleValue(false))))
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
            DistributionMode.MODE_CUSTOM_BUILDER -> listOf(ResultItem(if (input.groupName.isNotBlank()) input.groupName else "سهم محاسبات اختصاصی (در حال توسعه)", input.poolAmount))
        }
    }
}
