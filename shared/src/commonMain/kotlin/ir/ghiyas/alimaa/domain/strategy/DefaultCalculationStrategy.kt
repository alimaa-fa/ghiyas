package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem

interface DefaultCalculationStrategy {
    val title: String
    val isGlobalMacro: Boolean // پرچم جدید برای تشخیص محاسباتی که کل بار را یکجا مدیریت می‌کنند
    fun calculate(input: DistributionInput): List<ResultItem>
}

object DefaultCalculationsRegistry {
    val strategies: List<DefaultCalculationStrategy> = listOf(
        AsadCalculationStrategy,
        DongMarikiCalculationStrategy // ثبت پادشاه محاسبات پیش‌فرض
    )
}
