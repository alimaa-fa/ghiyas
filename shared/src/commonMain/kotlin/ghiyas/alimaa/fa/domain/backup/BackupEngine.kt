package ghiyas.alimaa.fa.domain.backup

import ghiyas.alimaa.fa.domain.models.CalculationHistoryRecord
import ghiyas.alimaa.fa.domain.models.CustomProfile
import ghiyas.alimaa.fa.domain.models.SavedDistributionTemplate
import ghiyas.alimaa.fa.domain.models.WorkCalendarProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object BackupEngine {
    
    val jsonParser = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = false 
    }

    fun generateBackupJson(payload: BackupPayload): String {
        return jsonParser.encodeToString(payload)
    }

    fun parseBackupJson(jsonString: String): BackupPayload? {
        return try {
            jsonParser.decodeFromString<BackupPayload>(jsonString)
        } catch (e: Exception) {
            null 
        }
    }

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
                val isSafeDefault = if (result.isEmpty()) importedItem.isDefault else false
                result.add(importedItem.copy(name = finalName, isDefault = isSafeDefault))
            }
        }
        return result
    }

    // متد جدید: ادغام تاریخچه ماشین‌حساب (حذف تکراری‌ها و نگه داشتن نهایتا ۳۰ آیتم آخر)
    fun mergeCalculatorHistory(
        existing: List<String>,
        imported: List<String>
    ): List<String> {
        return (existing + imported).distinct().takeLast(30)
    }
}
