package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

// 1. رابط کاربری استراتژی
interface DefaultCalculationStrategy {
    val title: String
    fun calculate(pool: WalnutUnit): List<ResultItem>
}

// 2. رجیستری: لیست تمام استراتژی‌های سازنده
// برای اضافه کردن محاسبات جدید (مثلاً پروفایل اختصاصی)، فقط کلاسش را بسازید و اینجا اضافه کنید.
object DefaultCalculationsRegistry {
    val strategies: List<DefaultCalculationStrategy> = listOf(
        AsadCalculationStrategy
    )
}
