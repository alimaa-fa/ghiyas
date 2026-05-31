package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AbdolrahimStrategyTest {

    @Test
    fun testDadallahTransferAndZivar() {
        // ۱۰۲۰ گردو با تیک زیور -> هر قیاس ۱۰
        val input = DistributionInput(
            poolAmount = WalnutUnit(1020.0),
            mode = DistributionMode.MODE_DEFAULT_MAKER,
            calculateZivar = true,
            isNimehkari = true,
            nimehkariPool = WalnutUnit(100.0), // سهم دادالله
            transferDadallah = true, // باید به عبدالرحیم اضافه شود
            targetGroup = "کل عبدالرحیمی‌ها"
        )

        val results = AbdolrahimCalculationStrategy.calculate(input)

        val ghiyasValue = results.find { it.label == "هر قیاس" }?.value?.value ?: 0.0
        val abdolrahimFinal = results.find { it.label == "سهم عبدالرحیم" }?.value?.value ?: 0.0
        val dadallahSeparated = results.find { it.label.contains("دادالله") }

        assertEquals(10.0, ghiyasValue, 0.0001)
        // عبدالرحیم = (62 * 10) + 100 سهم دادالله = 720
        assertEquals(720.0, abdolrahimFinal, 0.0001, "سهم دادالله باید به عبدالرحیم منتقل شده باشد")
        assertNull(dadallahSeparated, "وقتی سهم دادالله منتقل می‌شود نباید مجزا رندر شود")
    }

    @Test
    fun testNouriAndSoghraTargetGroup() {
        // ۳۴۴ گردو بدون زیور برای گروه هدف نوری و صغری -> هر قیاس ۱۰ (مبنا ۳۴.۴)
        val input = DistributionInput(
            poolAmount = WalnutUnit(344.0),
            mode = DistributionMode.MODE_DEFAULT_MAKER,
            calculateZivar = false,
            isNimehkari = false,
            targetGroup = "مابین نوری و صغری"
        )

        val results = AbdolrahimCalculationStrategy.calculate(input)

        val nouriShare = results.find { it.label == "سهم نوری" }?.value?.value ?: 0.0
        val ezzatKobraShare = results.find { it.label.contains("عزت و کبری") }?.value?.value

        assertEquals(206.0, nouriShare, 0.0001, "سهم نوری باید ۲۰.۶ برابر قیاس باشد")
        assertNull(ezzatKobraShare, "در گروه مابین نوری و صغری، سهم عزت و کبری نباید محاسبه شود")
    }
}
