package ghiyas.alimaa.fa.domain.backup

import ghiyas.alimaa.fa.domain.models.CalculationHistoryRecord
import ghiyas.alimaa.fa.domain.models.CustomProfile
import ghiyas.alimaa.fa.domain.models.SavedDistributionTemplate
import ghiyas.alimaa.fa.domain.models.WorkCalendarProfile
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val history: List<CalculationHistoryRecord> = emptyList(),
    val calendars: List<WorkCalendarProfile> = emptyList(),
    val distributionTemplates: List<SavedDistributionTemplate> = emptyList(),
    val customProfiles: List<CustomProfile> = emptyList(),
    val calculatorHistory: List<String> = emptyList() // اضافه شدن فیلد ماشین‌حساب
)
