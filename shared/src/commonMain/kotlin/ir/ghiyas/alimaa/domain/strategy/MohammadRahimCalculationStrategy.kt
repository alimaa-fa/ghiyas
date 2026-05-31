package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem

object MohammadRahimCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "محمد رحیم(کَنِنَو)"
    override val isGlobalMacro: Boolean = false 

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val results = mutableListOf<ResultItem>()

        // ۱. متغیرهای پایه با ثابت قیاس قطعی
        val totalGhiyas = 333.1
        val valuePerGhiyas = input.poolAmount / totalGhiyas
        val valuePerSahm = valuePerGhiyas * 24.0

        // ۲. سهم‌های ناخالص (Gross)
        val khalilGross = valuePerGhiyas * 94.74
        val farajGross = valuePerGhiyas * 94.74
        val nasrollahHajarShare = valuePerGhiyas * 63.83
        val yadollahShare = valuePerGhiyas * 39.89
        val asadShare = valuePerGhiyas * 39.89

        // ۳. کسورات (قیاس‌های خریداری شده توسط قاسم)
        val ghasemShare = valuePerGhiyas * 6.0
        val khalilNet = khalilGross - (valuePerGhiyas * 3.0)
        val farajNet = farajGross - (valuePerGhiyas * 3.0)

        // ۴. زیرمجموعه اسد
        val asadBoy = asadShare / 4.5
        val asadGirl = asadBoy / 2.0

        // ۵. رندر نتایج با ترتیب دقیق درخواستی
        results.add(ResultItem("هر قیاس", valuePerGhiyas))
        results.add(ResultItem("هر سهم (حبه)", valuePerSahm))
        results.add(ResultItem("سهم خلیل", khalilNet))
        results.add(ResultItem("سهم فرج", farajNet))
        results.add(ResultItem("سهم نصراله و هاجر", nasrollahHajarShare))
        results.add(ResultItem("سهم یداله", yadollahShare))
        results.add(ResultItem("سهم اسد", asadShare))
        results.add(ResultItem("سهم قاسم (خریداری شده از خلیل و فرج - ۶ قیاس)", ghasemShare))
        results.add(ResultItem("سهم هر پسر اسد", asadBoy))
        results.add(ResultItem("سهم هر دختر اسد", asadGirl))

        return results
    }
}
