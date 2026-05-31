package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

enum class DistributionMode {
    MODE_A_NO_BREAKDOWN, MODE_B_SIMPLE, MODE_C_GHIYAS, MODE_DEFAULT_MAKER
}

data class Shareholder(val name: String, val ghiyas: Double)

data class DistributionInput(
    val poolAmount: WalnutUnit,
    val mode: DistributionMode,
    val groupName: String = "", 
    val peopleCountInput: String = "", 
    val isBoyGirlSplit: Boolean = false, 
    val shareholders: List<Shareholder> = emptyList(),
    val defaultStrategyTitle: String = "",
    val defaultLabel: String = "سهم یکجا" // افزوده شده برای نام‌های هوشمند
)

object DistributionEngine {
    fun calculate(input: DistributionInput): List<ResultItem> {
        return when (input.mode) {
            
            DistributionMode.MODE_A_NO_BREAKDOWN -> {
                // منطق هوشمند جایگذاری نام (اگر خالی بود، لیبل پیش‌فرض ارکستراتور را می‌گذارد)
                val name = if (input.groupName.isNotBlank()) input.groupName else input.defaultLabel
                listOf(ResultItem(name, input.poolAmount))
            }
            
            DistributionMode.MODE_B_SIMPLE -> {
                val count = input.peopleCountInput.toDoubleOrNull() ?: 1.0
                val validCount = if (count > 0) count else 1.0
                val baseShare = input.poolAmount / validCount
                val hasDecimal = (count % 1.0 != 0.0)
                
                if (hasDecimal || input.isBoyGirlSplit) {
                    val halfShare = baseShare / 2.0
                    listOf(
                        ResultItem("سهم هر پسر", baseShare),
                        ResultItem("سهم هر دختر", halfShare)
                    )
                } else {
                    listOf(
                        ResultItem("سهم هر فرد", baseShare)
                    )
                }
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
                strategy?.calculate(input.poolAmount) ?: emptyList()
            }
        }
    }
}
