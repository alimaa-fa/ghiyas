package ir.ghiyas.alimaa.domain.models

enum class CalendarType {
    DAY_BASED,
    TIMELINE_BASED
}

data class WorkTurn(
    val turnId: Int,
    val cycle: Int,
    val owner: String,
    val notes: String
)

// مدل مربوط به فایل جملات روز
data class QuoteItem(
    val text: String,
    val translation: String,
    val source: String
)

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
    val quotes: List<QuoteItem> // اضافه شدن لیست جملات
)
