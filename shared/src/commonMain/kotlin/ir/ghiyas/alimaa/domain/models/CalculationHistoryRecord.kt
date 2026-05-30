package ir.ghiyas.alimaa.domain.models

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
    val finalSharesResults: List<ResultItem> = emptyList()
)

data class ResultItem(val label: String, val value: WalnutUnit)