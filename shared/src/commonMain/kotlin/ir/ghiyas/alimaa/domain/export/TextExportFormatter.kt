package ir.ghiyas.alimaa.domain.export

import ir.ghiyas.alimaa.domain.config.AppLinksConfig
import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.core.utils.formatTimestampToPersianDateTime
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat

object TextExportFormatter {
    fun formatRecord(record: CalculationHistoryRecord): String {
        val sb = StringBuilder()
        
        val isKg = record.baseUnit.contains("کیلوگرم") || record.baseUnit.equals("kg", ignoreCase = true)
        
        sb.appendLine("عنوان: ${record.calculationName}")
        sb.appendLine("تاریخ و زمان: ${formatTimestampToPersianDateTime(record.timestamp)}")
        sb.appendLine("مقدار کل: ${record.inputAmount.value.toGhiyasFormat(isKg)} ${record.baseUnit}")
        sb.appendLine("-------------------")
        
        if (record.expensesResults.isNotEmpty()) {
            sb.appendLine("هزینه‌ها:")
            record.expensesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(isKg)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.agricultureResults.isNotEmpty()) {
            sb.appendLine("کشاورزی:")
            record.agricultureResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(isKg)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.nimehkariResults.isNotEmpty()) {
            sb.appendLine("نیمه‌کاری:")
            record.nimehkariResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(isKg)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        if (record.finalSharesResults.isNotEmpty()) {
            sb.appendLine("سهم‌های نهایی:")
            record.finalSharesResults.forEach { 
                sb.appendLine("- ${it.label}: ${it.value.value.toGhiyasFormat(isKg)} ${record.baseUnit}")
            }
            sb.appendLine("-------------------")
        }
        
        sb.appendLine(AppLinksConfig.appName)
        sb.appendLine("ایتا: ${AppLinksConfig.eitaaLink}")
        sb.appendLine("بله: ${AppLinksConfig.baleLink}")
        
        return sb.toString()
    }
}
