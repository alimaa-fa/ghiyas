package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem

object SoghraNouriIslamabadStrategy : DefaultCalculationStrategy {
    override val title: String = "صغری-نوری(اسلام‌آباد)"
    override val isGlobalMacro: Boolean = false // این استراتژی روی استخر محلی بخش تسهیم تخصیص‌یافته کار می‌کند

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val results = mutableListOf<ResultItem>()

        // ۱. قفل ثابت کل قیاس بر روی عدد ۱۵۲ قطعی
        val totalGhiyas = 152.0
        val valuePerGhiyas = input.poolAmount / totalGhiyas
        val valuePerSahm = valuePerGhiyas * 24.0

        // ۲. سهم‌های اصلی بر مبنای سهم‌الارث بومی
        val soghraShare = valuePerGhiyas * 72.0
        val nouriShare = valuePerGhiyas * 80.0

        // ۳. محاسبات ورثه صغری بر پایه تسهیم پسر و دختری (نسبت سهم ۴.۵ نفری)
        val soghraBoy = soghraShare / 4.5
        val soghraGirl = soghraBoy / 2.0

        // ۴. نگاشت نتایج نهایی به لایه نمایش به ترتیب دقیق و درخواستی سناریو
        results.add(ResultItem("هر قیاس", valuePerGhiyas))
        results.add(ResultItem("هر سهم (حبه)", valuePerSahm))
        results.add(ResultItem("سهم صغری", soghraShare))
        results.add(ResultItem("سهم نوری", nouriShare))
        results.add(ResultItem("سهم هر پسر صغری", soghraBoy))
        results.add(ResultItem("سهم هر دختر صغری", soghraGirl))

        return results
    }
}
