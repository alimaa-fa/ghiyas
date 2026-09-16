package ghiyas.alimaa.fa.domain.strategy

import ghiyas.alimaa.fa.domain.models.ResultItem

object AsadCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "اسد"
    override val isGlobalMacro: Boolean = false // اسد یک ماکروی سراسری نیست و فقط روی یک استخر کار میکند

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val baseShare = input.poolAmount / 4.5
        val halfShare = baseShare / 2.0
        
        return listOf(
            ResultItem("سهم هر پسر", baseShare),
            ResultItem("سهم هر دختر", halfShare)
        )
    }
}
