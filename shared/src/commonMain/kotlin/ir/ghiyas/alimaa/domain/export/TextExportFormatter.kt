package ir.ghiyas.alimaa.domain.export

import ir.ghiyas.alimaa.domain.config.AppLinksConfig
import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.core.utils.formatTimestampToPersianDateTime
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat

object TextExportFormatter {
    fun formatRecord(record: CalculationHistoryRecord): String {
        val sb = StringBuilder()
        
        sb.appendLine("عنوان: ${record.calculationName}")
        sb.appendLine("تاریخ و زمان: ${formatTimestampToPersianDateTime(record.timestamp)}")
        // ارسال مستقیم نام واحد (String) به جای Boolean
        sb.appendLine("مقدار کل: ${record.inputAmount.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
        sb.appendLine("-------------------")
        
        if (record.expensesResults.isNotEmpty()) {
            sb.appendLine("هزینه‌ها:")
            record.expensesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.agricultureResults.isNotEmpty()) {
            sb.appendLine("کشاورزی:")
            record.agricultureResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.nimehkariResults.isNotEmpty()) {
            sb.appendLine("نیمه‌کاری:")
            record.nimehkariResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.finalSharesResults.isNotEmpty()) {
            sb.appendLine("سهم‌های نهایی:")
            record.finalSharesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        sb.appendLine(AppLinksConfig.appName)
        sb.appendLine("لینک ایتا: https://eitaa.com/ghiyas_app")
        
        return sb.toString()
    }
}
