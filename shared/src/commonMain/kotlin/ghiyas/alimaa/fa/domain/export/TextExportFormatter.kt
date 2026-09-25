package ghiyas.alimaa.fa.domain.export

import ghiyas.alimaa.fa.domain.config.AppLinksConfig
import ghiyas.alimaa.fa.domain.models.CalculationHistoryRecord
import ghiyas.alimaa.fa.core.utils.formatTimestampToPersianDateTime
import ghiyas.alimaa.fa.core.utils.toGhiyasFormat

object TextExportFormatter {
    fun formatRecord(record: CalculationHistoryRecord): String {
        val sb = StringBuilder()
        
        sb.appendLine("عنوان: ${record.calculationName}")
        sb.appendLine("تاریخ و زمان: ${formatTimestampToPersianDateTime(record.timestamp)}")
        sb.appendLine("مقدار کل: ${record.inputAmount.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
        sb.appendLine("-------------------")
        
        if (record.expensesResults.isNotEmpty()) {
            sb.appendLine("هزینه‌ها:")
            record.expensesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit, it.label)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }

        val remainders = record.agricultureResults.filter { it.label.contains("باقیمانده") || it.label.contains("باقی‌مانده") } +
                         record.nimehkariResults.filter { it.label.contains("باقیمانده") || it.label.contains("باقی‌مانده") }
                         
        val pureAgri = record.agricultureResults.filterNot { it.label.contains("باقیمانده") || it.label.contains("باقی‌مانده") }
        val pureNimehkari = record.nimehkariResults.filterNot { it.label.contains("باقیمانده") || it.label.contains("باقی‌مانده") }
        
        if (pureAgri.isNotEmpty()) {
            sb.appendLine("کشاورزی:")
            pureAgri.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit, it.label)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (pureNimehkari.isNotEmpty()) {
            sb.appendLine("نیمه‌کاری:")
            pureNimehkari.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit, it.label)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (remainders.isNotEmpty()) {
            sb.appendLine("باقیمانده جهت تسهیم:")
            remainders.forEach { 
                sb.appendLine("✅ ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit, it.label)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.finalSharesResults.isNotEmpty()) {
            sb.appendLine("سهم‌های نهایی:")
            record.finalSharesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(record.baseUnit, it.label)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        sb.appendLine(AppLinksConfig.appName)
        sb.appendLine("لینک ایتا: https://eitaa.com/ghiyas_app")
        
        return sb.toString()
    }
}
