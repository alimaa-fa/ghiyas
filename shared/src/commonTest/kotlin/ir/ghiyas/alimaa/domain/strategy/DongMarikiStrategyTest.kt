package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DongMarikiStrategyTest {

    @Test
    fun testWithZivarAndNimehkari() {
        // ۳۸۴۰ گردو -> هر قیاس باید دقیقاً ۱۰ شود
        val input = DistributionInput(
            poolAmount = WalnutUnit(3840.0),
            mode = DistributionMode.MODE_DEFAULT_MAKER,
            calculateZivar = true,
            isNimehkari = true,
            nimehkariPool = WalnutUnit(1000.0) // ۵۰۰ به برکت، ۵۰۰ به کرامت
        )

        val results = DongMarikiCalculationStrategy.calculate(input)

        val ghiyasValue = results.find { it.label == "هر قیاس" }?.value?.value ?: 0.0
        val zivarShare = results.find { it.label == "سهم زیور (نواب)" }?.value?.value ?: 0.0
        val abdolrahimShare = results.find { it.label == "سهم عبدالرحیم" }?.value?.value ?: 0.0
        val barkatShare = results.find { it.label.contains("سهم برکت (بخش نیمه‌کاری)") }?.value?.value ?: 0.0

        assertEquals(10.0, ghiyasValue, 0.0001)
        assertEquals(400.0, zivarShare, 0.0001, "سهم زیور باید ۴۰ برابر یک قیاس باشد")
        assertEquals(620.0, abdolrahimShare, 0.0001, "سهم عبدالرحیم باید ۶۲ برابر یک قیاس باشد")
        assertEquals(500.0, barkatShare, 0.0001, "سهم برکت باید دقیقاً نصف استخر نیمه‌کاری باشد")
    }

    @Test
    fun testWithoutZivarAndNoNimehkari() {
        // ۳۴۴۰ گردو بدون تیک زیور -> هر قیاس باید دقیقاً ۱۰ شود
        val input = DistributionInput(
            poolAmount = WalnutUnit(3440.0),
            mode = DistributionMode.MODE_DEFAULT_MAKER,
            calculateZivar = false,
            isNimehkari = false
        )

        val results = DongMarikiCalculationStrategy.calculate(input)

        val ghiyasValue = results.find { it.label == "هر قیاس" }?.value?.value ?: 0.0
        val zivarResult = results.find { it.label == "سهم زیور (نواب)" }
        val barkatResult = results.find { it.label.contains("سهم برکت") }

        assertEquals(10.0, ghiyasValue, 0.0001, "مجموع قیاس‌ها بدون زیور باید ۳۴۴ باشد")
        assertNull(zivarResult, "وقتی تیک زیور فعال نیست نباید در خروجی رندر شود")
        assertNull(barkatResult, "وقتی نیمه‌کاری فعال نیست نباید در خروجی رندر شود")
    }
}
