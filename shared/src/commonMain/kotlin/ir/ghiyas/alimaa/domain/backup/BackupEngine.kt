package ir.ghiyas.alimaa.domain.backup

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.CustomProfile
import ir.ghiyas.alimaa.domain.models.SavedDistributionTemplate
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupEngine {
    
    // تنظیمات سریالایزر: نادیده گرفتن کلیدهای ناشناس در آینده (برای سازگاری نسخه‌ها)
    val jsonParser = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = false // برای کاهش حجم فایل بکاپ
    }

    fun generateBackupJson(payload: BackupPayload): String {
        return jsonParser.encodeToString(payload)
    }

    fun parseBackupJson(jsonString: String): BackupPayload? {
        return try {
            jsonParser.decodeFromString<BackupPayload>(jsonString)
        } catch (e: Exception) {
            null // در صورت خرابی فایل، نال برمی‌گرداند تا جلوی کرش برنامه گرفته شود
        }
    }

    // ادغام تاریخچه: فقط UUID چک می‌شود و مرتب‌سازی بر اساس زمان است
    fun mergeHistory(
        existing: List<CalculationHistoryRecord>, 
        imported: List<CalculationHistoryRecord>
    ): List<CalculationHistoryRecord> {
        val resultMap = existing.associateBy { it.id }.toMutableMap()
        imported.forEach { item ->
            if (!resultMap.containsKey(item.id)) {
                resultMap[item.id] = item
            }
        }
        return resultMap.values.sortedByDescending { it.timestamp }
    }

    // ادغام الگوهای تسهیم: حل مشکل نام‌های تکراری با شماره‌گذاری
    fun mergeTemplates(
        existing: List<SavedDistributionTemplate>, 
        imported: List<SavedDistributionTemplate>
    ): List<SavedDistributionTemplate> {
        val result = existing.toMutableList()
        imported.forEach { importedItem ->
            if (result.none { it.id == importedItem.id }) {
                var finalTitle = importedItem.title
                var counter = 1
                while (result.any { it.title == finalTitle }) {
                    finalTitle = "${importedItem.title} ($counter)"
                    counter++
                }
                result.add(importedItem.copy(title = finalTitle))
            }
        }
        return result.sortedByDescending { it.createdAt }
    }

    // ادغام پروفایل‌های محاسباتی اختصاصی
    fun mergeCustomProfiles(
        existing: List<CustomProfile>, 
        imported: List<CustomProfile>
    ): List<CustomProfile> {
        val result = existing.toMutableList()
        imported.forEach { importedItem ->
            if (result.none { it.id == importedItem.id }) {
                var finalName = importedItem.name
                var counter = 1
                while (result.any { it.name == finalName }) {
                    finalName = "${importedItem.name} ($counter)"
                    counter++
                }
                result.add(importedItem.copy(name = finalName))
            }
        }
        return result.sortedByDescending { it.createdAt }
    }

    // ادغام تقویم‌های کاری
    fun mergeCalendars(
        existing: List<WorkCalendarProfile>, 
        imported: List<WorkCalendarProfile>
    ): List<WorkCalendarProfile> {
        val result = existing.toMutableList()
        imported.forEach { importedItem ->
            if (result.none { it.id == importedItem.id }) {
                var finalName = importedItem.name
                var counter = 1
                while (result.any { it.name == finalName }) {
                    finalName = "${importedItem.name} ($counter)"
                    counter++
                }
                // اگر تقویم وارداتی حالت پیش‌فرض داشته باشد اما ما از قبل تقویم پیش‌فرض داریم، اولویت با تقویم فعلی است
                val isSafeDefault = if (result.isEmpty()) importedItem.isDefault else false
                result.add(importedItem.copy(name = finalName, isDefault = isSafeDefault))
            }
        }
        return result
    }
}
