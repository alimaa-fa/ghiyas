package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class CalculationHistoryRecord(
    val id: String,
    val timestamp: Long,
    val calculationName: String,
    val persianYear: String,
    val baseUnit: String,
    val inputAmount: WalnutUnit,
    val expensesResults: List<ResultItem>,
    val agricultureResults: List<ResultItem> = emptyList(),
    val nimehkariResults: List<ResultItem> = emptyList(),
    val finalSharesResults: List<ResultItem> = emptyList(),
    
    // --- فیلدهای جدید فاز ۲ (معماری اختصاصی و پشتیبان‌گیری امن) ---
    val associated_profile_id: String? = null, // پیوند به شناسه الگوی سازنده
    val customProfileSnapshotJson: String? = null // یک کپی منجمد از نقشه الگو در لحظه محاسبه
)

@Serializable
data class ResultItem(val label: String, val value: WalnutUnit)
