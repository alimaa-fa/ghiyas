package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem

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
        SoghraNouriIslamabadStrategy // ثبت پروفایل جدید صغری-نوری اسلام‌آباد
    )
}
