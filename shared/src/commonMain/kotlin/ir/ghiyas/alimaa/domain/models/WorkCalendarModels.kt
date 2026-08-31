package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class CalendarType {
    DAY_BASED,
    TIMELINE_BASED
}

@Serializable
data class WorkTurn(
    val turnId: Int,
    val cycle: Int,
    val owner: String,
    val notes: String
)

@Serializable
data class QuoteItem(
    val text: String,
    val translation: String,
    val source: String
)

@Serializable
data class WorkCalendarProfile(
    val id: String,
    val name: String,
    val isDefault: Boolean,
    val type: CalendarType,
    val startYear: Int,
    val startMonth: Int,
    val startDay: Int,
    val turnTime: String,
    val shiftBeforeTemplate: String,
    val shiftAfterTemplate: String,
    val schedule: List<WorkTurn>,
    val quotes: List<QuoteItem>
)
