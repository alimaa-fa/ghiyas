package ghiyas.alimaa.fa.domain.strategy

import ghiyas.alimaa.fa.domain.models.WalnutUnit
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
            targetGroup = "نوری و صغری"
        )

        val results = AbdolrahimCalculationStrategy.calculate(input)

        val nouriShare = results.find { it.label == "سهم نوری" }?.value?.value ?: 0.0
        val ezzatKobraShare = results.find { it.label.contains("عزت و کبری") }?.value?.value

        assertEquals(206.0, nouriShare, 0.0001, "سهم نوری باید ۲۰.۶ برابر قیاس باشد")
        assertNull(ezzatKobraShare, "در گروه نوری و صغری، سهم عزت و کبری نباید محاسبه شود")
    }

    @Test
    fun testDirtyStateIgnoredWhenTargetGroupChanges() {
        // تست باگ رابط کاربری: کاربر گروه را به "نوری و صغری" تغییر داده 
        // اما UI هنوز مقادیر true مربوط به زیور و دادالله را ارسال می‌کند
        val input = DistributionInput(
            poolAmount = WalnutUnit(344.0),
            mode = DistributionMode.MODE_DEFAULT_MAKER,
            calculateZivar = true, // State کثیف
            isNimehkari = true,
            nimehkariPool = WalnutUnit(100.0),
            transferDadallah = true, // State کثیف
            targetGroup = "نوری و صغری"
        )

        val results = AbdolrahimCalculationStrategy.calculate(input)

        // با وجود تیک خوردن زیور، مبنای قیاس باید همان ۳۴.۴ بماند (۳۴۴ تقسیم بر ۳۴.۴ = ۱۰)
        val ghiyasValue = results.find { it.label == "هر قیاس" }?.value?.value ?: 0.0
        val nouriShare = results.find { it.label == "سهم نوری" }?.value?.value ?: 0.0
        val zivarShare = results.find { it.label == "سهم زیور" }
        val abdolrahimShare = results.find { it.label == "سهم عبدالرحیم" }
        val dadallahSeparated = results.find { it.label.contains("دادالله") }

        assertEquals(10.0, ghiyasValue, 0.0001, "محاسبه قیاس نباید تحت تاثیر State کثیف زیور قرار بگیرد")
        assertEquals(206.0, nouriShare, 0.0001, "سهم نوری باید به درستی محاسبه شود")
        assertNull(zivarShare, "وقتی گروه هدف تغییر می‌کند زیور نباید سهم بگیرد")
        assertNull(abdolrahimShare, "سهم کل عبدالرحیم نباید در این سناریو نمایش داده شود")
        // چون انتقال سهم باطل شده اما نیمه‌کاری true است، دادالله باید به عنوان یک آیتم جداگانه رندر شود
        assertEquals(100.0, dadallahSeparated?.value?.value ?: 0.0, 0.0001, "سهم دادالله باید مستقل بماند و منتقل نشود")
    }
}
