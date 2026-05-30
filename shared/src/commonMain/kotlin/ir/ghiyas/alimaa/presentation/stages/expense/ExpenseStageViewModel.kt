package ir.ghiyas.alimaa.presentation.stages.expense

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.strategy.KharjkardStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExpenseCategoryState(
    val mizan: String = "",
    val tedad: String = "",
    val isFixed: Boolean = false,
    val hasExtra: Boolean = false,
    val extraValue: String = ""
)

data class SarkariCategoryState(
    val mizan: String = "",
    val tedad: String = "",
    val isFixed: Boolean = false,
    val isHalfKari: Boolean = false,
    val group1Count: String = "",
    val group2Count: String = "",
    val hasExtra: Boolean = false,
    val extraValue: String = ""
)

data class KharjkardInputState(
    val isCalculated: Boolean = false,
    val globalFixedExpense_Input: String = "",
    val extraExpense_Input: String = "",
    val tekani: ExpenseCategoryState = ExpenseCategoryState(),
    val jamkoni: ExpenseCategoryState = ExpenseCategoryState(),
    val kooleh: ExpenseCategoryState = ExpenseCategoryState(),
    val sarkari: SarkariCategoryState = SarkariCategoryState()
)

class ExpenseStageViewModel {
    private val _inputState = MutableStateFlow(KharjkardInputState())
    val inputState: StateFlow<KharjkardInputState> = _inputState.asStateFlow()

    private val _snapshot = MutableStateFlow<CalculationHistoryRecord?>(null)
    val snapshot: StateFlow<CalculationHistoryRecord?> = _snapshot.asStateFlow()

    var totalWalnuts: WalnutUnit = WalnutUnit.ZERO
    var baseUnit: String = "هزار"

    fun setTotalWalnuts(unit: WalnutUnit) {
        totalWalnuts = unit
    }

    fun clearForm() {
        _inputState.value = KharjkardInputState()
        _snapshot.value = null
    }

    fun updateIsCalculated(isCalculated: Boolean) {
        _inputState.update { it.copy(isCalculated = isCalculated) }
    }
    
    fun updateGlobalFixedExpense(value: String) {
        _inputState.update { it.copy(globalFixedExpense_Input = value) }
    }
    
    fun updateExtraExpense(value: String) {
        _inputState.update { it.copy(extraExpense_Input = value) }
    }

    fun updateTekani(update: (ExpenseCategoryState) -> ExpenseCategoryState) {
        _inputState.update { it.copy(tekani = update(it.tekani)) }
    }

    fun updateJamkoni(update: (ExpenseCategoryState) -> ExpenseCategoryState) {
        _inputState.update { it.copy(jamkoni = update(it.jamkoni)) }
    }

    fun updateKooleh(update: (ExpenseCategoryState) -> ExpenseCategoryState) {
        _inputState.update { it.copy(kooleh = update(it.kooleh)) }
    }

    fun updateSarkari(update: (SarkariCategoryState) -> SarkariCategoryState) {
        _inputState.update { it.copy(sarkari = update(it.sarkari)) }
    }

    fun calculateAndSnapshot(
        calculationName: String, 
        baseUnit: String, 
        currentYear: String, 
        timestampLong: Long
    ) {
        // ۱. اجرای استراتژی اصلی محاسبات خرجکرد
        val output = KharjkardStrategy.calculate(_inputState.value, totalWalnuts)

        // ۲. تبدیل و نگاشت خروجی‌های ساختاریافته به مدل نمایشی کارت نتایج
        val resultsList = mutableListOf<ResultItem>()
        
        if (_inputState.value.isCalculated) {
            if (output.globalFixed.value > 0) {
                resultsList.add(ResultItem("خرج کل به صورت مقطوع", output.globalFixed))
            }
            
            resultsList.add(ResultItem("کل تکانی", output.totalTekani))
            resultsList.add(ResultItem("سهم هر تکان", output.perPersonTekani))
            
            resultsList.add(ResultItem("کل جمع‌کنی", output.totalJamkoni))
            resultsList.add(ResultItem("سهم هر جمع‌کن", output.perPersonJamkoni))
            
            resultsList.add(ResultItem("کل کوله‌کشی", output.totalKooleh))
            resultsList.add(ResultItem("سهم هر کوله‌کش", output.perPersonKooleh))
            
            resultsList.add(ResultItem("کل سرکاری", output.totalSarkari))
            
            if (_inputState.value.sarkari.isHalfKari) {
                resultsList.add(ResultItem("سهم سرکاری (گروه ۱)", output.perPersonSarkariGroup1))
                resultsList.add(ResultItem("سهم سرکاری (گروه ۲)", output.perPersonSarkariGroup2))
            } else {
                resultsList.add(ResultItem("سهم هر سرکار", output.perPersonSarkari))
            }
            
            if (output.extraExpense.value > 0) {
                resultsList.add(ResultItem("خرج اضافی متفرقه", output.extraExpense))
            }
        }

        // ۳. الصاق خودکار سال به عنوان و ایجاد کپسول تاریخچه
        val finalName = if (calculationName.isNotBlank()) "$calculationName - $currentYear" else "بدون نام - $currentYear"

        val record = CalculationHistoryRecord(
            id = "temp_id", // در فاز پیاده‌سازی دیتابیس با شناسه یکتا جایگزین می‌شود
            timestamp = timestampLong,
            calculationName = finalName,
            persianYear = currentYear,
            baseUnit = baseUnit,
            inputAmount = totalWalnuts,
            expensesResults = resultsList,
            agricultureResults = emptyList(),
            nimehkariResults = emptyList(),
            finalSharesResults = emptyList()
        )
        
        _snapshot.value = record
    }
}
