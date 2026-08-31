package ir.ghiyas.alimaa.domain.backup

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.CustomProfile
import ir.ghiyas.alimaa.domain.models.SavedDistributionTemplate
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val history: List<CalculationHistoryRecord> = emptyList(),
    val calendars: List<WorkCalendarProfile> = emptyList(),
    val distributionTemplates: List<SavedDistributionTemplate> = emptyList(),
    val customProfiles: List<CustomProfile> = emptyList()
)
