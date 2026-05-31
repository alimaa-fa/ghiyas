package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

object DongMarikiCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "دانگ ماریکی(کِجِینو)"
    override val isGlobalMacro: Boolean = true // فعال‌سازی وضعیت ماکروی سراسری یکپارچه

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val results = mutableListOf<ResultItem>()
        
        // --- PART A: MAIN DONG (تسهیم بر روی استخر سهم اصلی مالک) ---
        val totalGhiyas = if (input.calculateZivar) 384.0 else 344.0
        val valuePerGhiyas = input.poolAmount / totalGhiyas
        val valuePerSahm = valuePerGhiyas * 24.0

        val zivarShare = if (input.calculateZivar) valuePerGhiyas * 40.0 else WalnutUnit.ZERO
        val abdolrahimShare = valuePerGhiyas * 62.0
        val shahNesaTotal = valuePerGhiyas * 6.0
        val shahNesaKhodamorad = valuePerGhiyas * 3.0
        val shahNesaMohammadRahim = valuePerGhiyas * 3.0
        val jalalShare = valuePerGhiyas * 94.0
        val khodamoradShare = valuePerGhiyas * 91.0
        val mohammadRahimShare = valuePerGhiyas * 91.0

        val mohammadRahimBoy = mohammadRahimShare / 5.5
        val mohammadRahimGirl = mohammadRahimBoy / 2.0

        val nouriShare = valuePerGhiyas * 20.6
        val soghraShare = valuePerGhiyas * 13.8
        val ezzatKobraShare = valuePerGhiyas * 13.8

        val asadiBoy = (mohammadRahimBoy + soghraShare) / 4.5
        val asadiGirl = asadiBoy / 2.0

        results.add(ResultItem("هر قیاس", valuePerGhiyas))
        results.add(ResultItem("هر سهم (حبه)", valuePerSahm))
        if (input.calculateZivar) {
            results.add(ResultItem("سهم زیور (نواب)", zivarShare))
        }
        results.add(ResultItem("سهم عبدالرحیم", abdolrahimShare))
        results.add(ResultItem("سهم شاه نساء (۶ قیاس)", shahNesaTotal))
        results.add(ResultItem("سهم شاه‌نساء روی خدامراد (۳ قیاس)", shahNesaKhodamorad))
        results.add(ResultItem("سهم شاه‌نساء روی محمد رحیم (۳ قیاس)", shahNesaMohammadRahim))
        results.add(ResultItem("سهم جلال", jalalShare))
        results.add(ResultItem("سهم خدامراد", khodamoradShare))
        results.add(ResultItem("سهم محمد رحیم", mohammadRahimShare))
        results.add(ResultItem("سهم هر پسر م.رحیم", mohammadRahimBoy))
        results.add(ResultItem("سهم هر دختر م.رحیم", mohammadRahimGirl))
        results.add(ResultItem("سهم نوری", nouriShare))
        results.add(ResultItem("سهم صغری", soghraShare))
        results.add(ResultItem("سهم هر یک از (کبری، عزت)", ezzatKobraShare))
        results.add(ResultItem("سهم هر پسر اسدی", asadiBoy))
        results.add(ResultItem("سهم هر دختر اسدی", asadiGirl))

        // --- PART B: NIMEH-KARI BARKAT (تسهیم بر روی استخر نیمه‌کاری) ---
        // اضافه کردن نشانه‌گذاری هوشمند 🌾 جهت تشخیص کادر مجزا و فاصله‌گذاری در UI نهایی نتایج
        if (input.isNimehkari) {
            val barkatShare = input.nimehkariPool / 2.0
            val keramatShare = input.nimehkariPool / 2.0
            val perBarkati = barkatShare / 5.0
            val perKeramatBoy = keramatShare / 4.5
            val perKeramatGirl = perKeramatBoy / 2.0

            results.add(ResultItem("🌾 سهم برکت (بخش نیمه‌کاری)", barkatShare))
            results.add(ResultItem("🌾 سهم کرامت (بخش نیمه‌کاری)", keramatShare))
            results.add(ResultItem("🌾 سهم هر برکتی (بخش نیمه‌کاری)", perBarkati))
            results.add(ResultItem("🌾 سهم هر پسر کرامت (بخش نیمه‌کاری)", perKeramatBoy))
            results.add(ResultItem("🌾 سهم هر دختر کرامت (بخش نیمه‌کاری)", perKeramatGirl))
        }

        return results
    }
}
