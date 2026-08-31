package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

object BarkatCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "برکت (۹ حبه)"
    override val isGlobalMacro: Boolean = false // چون مرحله کشاورزی را خودش انجام می‌دهد اما ماکروی یکپارچه دو شریک نیست

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val pool = input.poolAmount
        if (pool.value <= 0.0) return emptyList()

        val results = mutableListOf<ResultItem>()

        // فرمول ۱: ۴۸ قیاس از ۲۱۶ قیاس (معادل ۲/۹ کل بار)
        val x = pool * (48.0 / 216.0)
        val halfX = x / 2.0

        val navvabShare = halfX
        var agriBK = halfX

        // فرمول ۲: باقیمانده (۱۶۸ قیاس از ۲۱۶)
        val r2 = pool - x

        // باقیمانده تقسیم بر ۳ و افزودن به کشاورزی
        val t = r2 / 3.0
        agriBK += t

        // فرمول ۳: باقیمانده جدید (دو سومِ R2) تقسیم بر ۳.۵
        val r3 = r2 - t
        val s = r3 / 3.5

        // دو سهم دختر (۰.۵)
        val marymShare = s * 0.5
        val hajiNesaShare = s * 0.5

        // دو و نیم سهم باقیمانده (۱ برای برکت، ۱.۵ برای کرامت)
        val barkatShare = s * 1.0
        val keramatShare = s * 1.5

        // تقسیم متغیر کشاورزی برکت-کرامت به دو قسمت مساوی
        val agriB = agriBK / 2.0
        val agriK = agriBK / 2.0

        // جمع‌بندی سهم خالص هر برادر با سهم کشاورزی‌اش
        val totalBarkat = barkatShare + agriB
        val totalKeramat = keramatShare + agriK

        // تسهیم درونی وارثین
        val perBarkat = totalBarkat / 5.0
        val baseKeramatBoy = totalKeramat / 4.5
        val baseKeramatGirl = baseKeramatBoy / 2.0

        results.add(ResultItem("سهم نواب", navvabShare))
        results.add(ResultItem("سهم مریم", marymShare))
        results.add(ResultItem("سهم حاجی نساء", hajiNesaShare))
        results.add(ResultItem("سهم کلی برکت (کشاورزی و سهم)", totalBarkat))
        results.add(ResultItem("سهم هر برکتی", perBarkat))
        results.add(ResultItem("سهم کلی کرامت (کشاورزی و سهم)", totalKeramat))
        results.add(ResultItem("سهم هر پسر کرامت", baseKeramatBoy))
        results.add(ResultItem("سهم هر دختر کرامت", baseKeramatGirl))

        return results
    }
}
