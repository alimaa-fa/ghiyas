package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AsadStrategyTest {

    @Test
    fun testAsadDistributionMath() {
        val poolAmount = 900.0 // عددی که راحت به ۴.۵ تقسیم شود
        val input = DistributionInput(
            poolAmount = WalnutUnit(poolAmount),
            mode = DistributionMode.MODE_DEFAULT_MAKER
        )

        val results = AsadCalculationStrategy.calculate(input)

        val boyShare = results.find { it.label == "سهم هر پسر" }?.value?.value
        val girlShare = results.find { it.label == "سهم هر دختر" }?.value?.value

        assertNotNull(boyShare)
        assertNotNull(girlShare)

        // ۹۰۰ تقسیم بر ۴.۵ باید ۲۰۰ شود
        assertEquals(200.0, boyShare, 0.0001, "سهم پسر باید دقیقاً تقسیم بر ۴.۵ باشد.")
        // سهم دختر باید نصف پسر یعنی ۱۰۰ باشد
        assertEquals(100.0, girlShare, 0.0001, "سهم دختر باید دقیقاً نصف سهم پسر باشد.")
    }
}
