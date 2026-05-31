package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

enum class DistributionMode {
    MODE_A_NO_BREAKDOWN, MODE_B_SIMPLE, MODE_C_GHIYAS, MODE_DEFAULT_MAKER
}

data class Shareholder(val name: String, val ghiyas: Double)

data class PersonNode(
    val id: String,
    val name: String = "",
    val isFemale: Boolean = false,
    val isSubDivided: Boolean = false,
    val subCountInput: String = "",
    val isSubBoyGirlSplit: Boolean = false,
    val isDetailedFurther: Boolean = false,
    val subNodes: List<PersonNode> = emptyList()
) {
    val weight: Double get() = if (isFemale) 0.5 else 1.0
}

data class ModeBState(
    val countInput: String = "",
    val isBoyGirlSplit: Boolean = false,
    val isDetailed: Boolean = false,
    val children: List<PersonNode> = emptyList()
)

data class DistributionInput(
    val poolAmount: WalnutUnit,
    val mode: DistributionMode,
    val groupName: String = "", 
    val modeBState: ModeBState = ModeBState(),
    val shareholders: List<Shareholder> = emptyList(),
    val defaultStrategyTitle: String = "",
    val defaultLabel: String = "سهم یکجا",
    val calculateZivar: Boolean = true,
    val isNimehkari: Boolean = false,
    val nimehkariPool: WalnutUnit = WalnutUnit.ZERO,
    val targetGroup: String = "کل عبدالرحیمی‌ها", // فیلد جدید گروه هدف عبدالرحیم
    val transferDadallah: Boolean = false        // فیلد جدید تیک انتقال سهم دادالله
)

object DistributionEngine {
    
    private fun processModeBTree(
        pool: WalnutUnit, parentName: String, countInput: String,
        isBoyGirlSplit: Boolean, isDetailed: Boolean, children: List<PersonNode>
    ): List<ResultItem> {
        val count = countInput.toDoubleOrNull() ?: 1.0
        val validCount = if (count > 0) count else 1.0
        val baseShare = pool / validCount

        if (!isDetailed || children.isEmpty()) {
            val hasDecimal = (validCount % 1.0 != 0.0)
            val suffix = if (parentName.isNotEmpty()) " [$parentName]" else ""
            return if (hasDecimal || isBoyGirlSplit) {
                listOf(
                    ResultItem("سهم هر پسر$suffix", baseShare),
                    ResultItem("سهم هر دختر$suffix", baseShare / 2.0)
                )
            } else {
                listOf(ResultItem("سهم هر فرد$suffix", baseShare))
            }
        } else {
            val results = mutableListOf<ResultItem>()
            children.forEach { child ->
                val childPool = baseShare * child.weight
                val label = if (child.name.isNotBlank()) child.name else "ناشناس"
                val fullLabel = if (parentName.isNotBlank()) "$label (زیرمجموعه $parentName)" else label

                if (!child.isSubDivided) {
                    val typePrefix = if (child.isFemale) "سهم دختر" else "سهم پسر"
                    results.add(ResultItem("$typePrefix $fullLabel", childPool))
                } else {
                    results.addAll(
                        processModeBTree(
                            pool = childPool, parentName = fullLabel, countInput = child.subCountInput,
                            isBoyGirlSplit = child.isSubBoyGirlSplit, isDetailed = child.isDetailedFurther,
                            children = child.subNodes
                        )
                    )
                }
            }
            return results
        }
    }

    fun calculate(input: DistributionInput): List<ResultItem> {
        return when (input.mode) {
            DistributionMode.MODE_A_NO_BREAKDOWN -> {
                val name = if (input.groupName.isNotBlank()) input.groupName else input.defaultLabel
                listOf(ResultItem(name, input.poolAmount))
            }
            DistributionMode.MODE_B_SIMPLE -> {
                processModeBTree(
                    pool = input.poolAmount, parentName = "", countInput = input.modeBState.countInput,
                    isBoyGirlSplit = input.modeBState.isBoyGirlSplit, isDetailed = input.modeBState.isDetailed, children = input.modeBState.children
                )
            }
            DistributionMode.MODE_C_GHIYAS -> {
                val totalGhiyas = input.shareholders.sumOf { it.ghiyas }
                val valuePerGhiyas = if (totalGhiyas > 0) input.poolAmount / totalGhiyas else WalnutUnit.ZERO
                val results = mutableListOf<ResultItem>()
                input.shareholders.forEach { sh ->
                    val finalValue = valuePerGhiyas * sh.ghiyas
                    results.add(ResultItem("سهم ${sh.name}", finalValue))
                }
                results
            }
            DistributionMode.MODE_DEFAULT_MAKER -> {
                val strategy = DefaultCalculationsRegistry.strategies.find { it.title == input.defaultStrategyTitle }
                strategy?.calculate(input) ?: emptyList()
            }
        }
    }
}
