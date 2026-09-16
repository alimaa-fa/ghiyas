package ghiyas.alimaa.fa.domain.strategy

import ghiyas.alimaa.fa.domain.models.ResultItem

interface DefaultCalculationStrategy {
    val title: String
    val isGlobalMacro: Boolean
    fun calculate(input: DistributionInput): List<ResultItem>
}

object DefaultCalculationsRegistry {
    val strategies: List<DefaultCalculationStrategy> = listOf(
        AsadCalculationStrategy,
        DongMarikiCalculationStrategy,
        AbdolrahimCalculationStrategy,
        MohammadRahimCalculationStrategy,
        SoghraNouriIslamabadStrategy,
        BarkatCalculationStrategy // به انتهای لیست منتقل شد تا در لیست بازشو آخر نمایش داده شود
    )
}
