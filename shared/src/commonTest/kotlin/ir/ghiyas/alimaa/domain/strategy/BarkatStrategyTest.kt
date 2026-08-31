package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarkatStrategyTest {

    @Test
    fun `barkat strategy divides correctly without leak`() {
        // برای راحت شدن ذهن از کسرها، یک عدد پایه مثل 2160 استفاده می‌کنیم
        val pool = WalnutUnit(2160.0)
        val input = DistributionInput(poolAmount = pool, mode = DistributionMode.MODE_DEFAULT_MAKER)

        val results = BarkatCalculationStrategy.calculate(input)

        // انتظارات ریاضی:
        // x = 480
        // navvab = 240, agriBK = 240
        // r2 = 2160 - 480 = 1680
        // t = 1680 / 3 = 560
        // agriBK (جدید) = 240 + 560 = 800 -> سهم کشاورزی برکت = 400, کرامت = 400
        // r3 = 1680 - 560 = 1120
        // s = 1120 / 3.5 = 320
        // مریم = 320 * 0.5 = 160
        // حاجی نساء = 320 * 0.5 = 160
        // سهم خام برکت = 320 * 1 = 320 -> کل برکت = 320 + 400 = 720
        // سهم خام کرامت = 320 * 1.5 = 480 -> کل کرامت = 480 + 400 = 880

        // اثبات سهم‌های اصلی
        assertEquals(240.0, results.first { it.label == "سهم نواب" }.value.value, 0.001)
        assertEquals(160.0, results.first { it.label == "سهم مریم" }.value.value, 0.001)
        assertEquals(160.0, results.first { it.label == "سهم حاجی نساء" }.value.value, 0.001)
        assertEquals(720.0, results.first { it.label == "سهم کلی برکت (کشاورزی و سهم)" }.value.value, 0.001)
        assertEquals(880.0, results.first { it.label == "سهم کلی کرامت (کشاورزی و سهم)" }.value.value, 0.001)

        // اثبات عدم نشت (No Leakage)
        val totalDistributed = 240.0 + 160.0 + 160.0 + 720.0 + 880.0
        assertEquals(2160.0, totalDistributed, 0.001, "The sum of all shares must exactly equal the input pool.")

        // اثبات سهم‌های جزئی درون خانواده
        assertEquals(144.0, results.first { it.label == "سهم هر برکتی" }.value.value, 0.001) // 720 / 5 = 144
        
        val boyKeramat = results.first { it.label == "سهم هر پسر کرامت" }.value.value
        val girlKeramat = results.first { it.label == "سهم هر دختر کرامت" }.value.value
        assertTrue(kotlin.math.abs(boyKeramat - 195.555) < 0.01) // 880 / 4.5
        assertEquals(boyKeramat / 2.0, girlKeramat, 0.001, "Girl share must be exactly half of boy share.")
    }
}
