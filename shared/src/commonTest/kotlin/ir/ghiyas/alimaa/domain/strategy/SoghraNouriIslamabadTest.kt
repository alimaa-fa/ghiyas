package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class SoghraNouriIslamabadTest {

    @Test
    fun testSoghraNouriPoolConservation() {
        // تخصیص مقدار فرضی هماهنگ با مبنای ۱۵۲ جهت راستی‌آزمایی دقیق
        val sourceAmount = 15200.0
        val input = DistributionInput(
            poolAmount = WalnutUnit(sourceAmount),
            mode = DistributionMode.MODE_DEFAULT_MAKER
        )

        val results = SoghraNouriIslamabadStrategy.calculate(input)

        // استخراج سهم‌های توزیع شده
        val soghraShare = results.find { it.label == "سهم صغری" }?.value?.value ?: 0.0
        val nouriShare = results.find { it.label == "سهم نوری" }?.value?.value ?: 0.0

        val totalDistributed = soghraShare + nouriShare

        // بررسی قانون بقای مقدار استخر (72 + 80 = 152 قیاس)
        assertEquals(
            sourceAmount,
            totalDistributed,
            0.0001,
            "مجموع سهم صغری و نوری باید دقیقاً بدون کم و کسر برابر با کل بار ورودی استخر تسهیم باشد."
        )
    }
}
