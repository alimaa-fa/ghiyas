package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit

object AbdolrahimCalculationStrategy : DefaultCalculationStrategy {
    override val title: String = "عبدالرحیم(کِجینو)"
    override val isGlobalMacro: Boolean = true // به عنوان یک ماکروی سراسری استخر نیمه‌کاری دادالله را مدیریت می‌کند

    override fun calculate(input: DistributionInput): List<ResultItem> {
        val results = mutableListOf<ResultItem>()

        // ۱. تعیین دقیق مجموع قیاس بر اساس تیک زیور و گروه هدف
        val totalGhiyas = if (input.calculateZivar) {
            102.0
        } else {
            if (input.targetGroup == "نوری و صغری") 34.4 else 62.0
        }

        // ۲. محاسبات پایه (ارزش قیاس از استخر کل)
        val valuePerGhiyas = input.poolAmount / totalGhiyas
        
        // اصلاح نام نمایشی "هر قیاس" بر اساس وضعیت انتقال
        val ghiyasLabel = if (input.isNimehkari && input.transferDadallah && input.targetGroup == "کل عبدالرحیمی‌ها") {
            "هر قیاس (قبل از انتقال سهم دادالله)"
        } else {
            "هر قیاس"
        }
        results.add(ResultItem(ghiyasLabel, valuePerGhiyas))

        // ۳. محاسبه سهم زیور در صورت انتخاب شدن
        if (input.calculateZivar) {
            val zivarShare = valuePerGhiyas * 40.0
            results.add(ResultItem("سهم زیور", zivarShare))
        }

        // ۴. منطق انتقال سهم نیمه‌کاری دادالله به عبدالرحیم
        var abdolrahimFinal = valuePerGhiyas * 62.0
        if (input.isNimehkari) {
            if (!input.transferDadallah) {
                // ارسال با نشانه 🌾 جهت رندر خودکار در کادر تفکیک‌شده کهربایی با فاصله واضح
                results.add(ResultItem("🌾 سهم نیمه‌کاری دادالله", input.nimehkariPool))
            } else {
                abdolrahimFinal += input.nimehkariPool
            }
        }

        if (input.targetGroup == "کل عبدالرحیمی‌ها") {
            results.add(ResultItem("سهم عبدالرحیم", abdolrahimFinal))
            
            // اضافه شدن "هر قیاس" دوم (بعد از انتقال) فقط زمانی که انتقال انجام شده باشد
            if (input.isNimehkari && input.transferDadallah) {
                val newValuePerGhiyas = abdolrahimFinal / 62.0
                results.add(ResultItem("هر قیاس (بعد از انتقال سهم دادالله)", newValuePerGhiyas))
            }
        }

        // ۵. تسهیم مابین ورثه بر اساس سناریوی انتخاب شده
        val nouriShare: WalnutUnit
        val soghraShare: WalnutUnit
        val ezzatKobraShare: WalnutUnit

        if (input.targetGroup == "نوری و صغری") {
            nouriShare = valuePerGhiyas * 20.6
            soghraShare = valuePerGhiyas * 13.8
            ezzatKobraShare = WalnutUnit.ZERO
        } else {
            // سناریوی کل عبدالرحیمی‌ها
            nouriShare = (abdolrahimFinal * 20.6) / 62.0
            soghraShare = (abdolrahimFinal * 13.8) / 62.0
            ezzatKobraShare = (abdolrahimFinal * 13.8) / 62.0
        }

        val soghraBoy = soghraShare / 4.5
        val soghraGirl = soghraBoy / 2.0

        // خروجی نهایی نتایج به ترتیب دقیق درخواستی
        results.add(ResultItem("سهم نوری", nouriShare))
        results.add(ResultItem("سهم صغری", soghraShare))
        if (input.targetGroup == "کل عبدالرحیمی‌ها") {
            results.add(ResultItem("سهم هر یک (عزت و کبری)", ezzatKobraShare))
        }
        results.add(ResultItem("سهم هر پسر صغری", soghraBoy))
        results.add(ResultItem("سهم هر دختر صغری", soghraGirl))

        return results
    }
}
