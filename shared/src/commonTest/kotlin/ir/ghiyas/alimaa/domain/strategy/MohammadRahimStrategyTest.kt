package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class MohammadRahimStrategyTest {

    @Test
    fun testTotalSharesMatchSourcePoolWithRemainders() {
        // ایجاد یک استخر فرضی بزرگ برای تست دقت بالا
        val sourceAmount = 10000.0
        val input = DistributionInput(
            poolAmount = WalnutUnit(sourceAmount),
            mode = DistributionMode.MODE_DEFAULT_MAKER
        )

        val results = MohammadRahimCalculationStrategy.calculate(input)

        // استخراج خروجی‌های فرمول
        val khalilNet = results.find { it.label == "سهم خلیل" }?.value?.value ?: 0.0
        val farajNet = results.find { it.label == "سهم فرج" }?.value?.value ?: 0.0
        val nasrollah = results.find { it.label == "سهم نصراله و هاجر" }?.value?.value ?: 0.0
        val yadollah = results.find { it.label == "سهم یداله" }?.value?.value ?: 0.0
        val asad = results.find { it.label == "سهم اسد" }?.value?.value ?: 0.0
        val ghasem = results.find { it.label.startsWith("سهم قاسم") }?.value?.value ?: 0.0

        val totalDistributed = khalilNet + farajNet + nasrollah + yadollah + asad + ghasem

        // بر اساس فرمول: مجموع سهم‌ها (۹۴.۷۴ + ۹۴.۷۴ + ۶۳.۸۳ + ۳۹.۸۹ + ۳۹.۸۹) = ۳۳۳.۰۹
        // اما مبنای تقسیم روی ۳۳۳.۱ قفل شده است. بنابراین باید نسبت توزیع شده با ۳۳۳.۰۹ / ۳۳۳.۱ برابر باشد.
        val expectedDistributed = (333.09 / 333.1) * sourceAmount
        
        // تلرانس خطای 0.0001 برای خطاهای ممیز شناور (Floating-point) جاوا اسکریپت و کاتلین
        assertEquals(
            expectedDistributed, 
            totalDistributed, 
            0.0001, 
            "مجموع سهم‌های توزیع شده باید دقیقاً با استخر اولیه و نسبت قیاس‌های کسر شده تطابق داشته باشد."
        )
        
        // تست ثانویه: بررسی صحت انتقال دقیق ۶ قیاس به قاسم
        val valuePerGhiyas = results.find { it.label == "هر قیاس" }?.value?.value ?: 0.0
        assertEquals(
            valuePerGhiyas * 6.0, 
            ghasem, 
            0.0001, 
            "سهم قاسم باید دقیقاً ۶ برابر یک قیاس باشد."
        )
    }
}
