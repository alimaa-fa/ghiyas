package ir.ghiyas.alimaa.presentation.stages.expense

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.strategy.KharjkardStrategy
import ir.ghiyas.alimaa.domain.strategy.AgricultureStrategy
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureInputState
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
        timestampLong: Long,
        agricultureInput: AgricultureInputState // ورودی جدید از ماژول ۳
    ) {
        // ۱. اجرای استراتژی اصلی محاسبات خرجکرد
        val output = KharjkardStrategy.calculate(_inputState.value, totalWalnuts)

        val expensesList = mutableListOf<ResultItem>()
        var totalExpensesValue = 0.0 // نگهدارنده مجموع کل هزینه‌ها
        
        // اگر تیک خرجکرد خورده باشد، لیست را پر می‌کنیم و جمع می‌زنیم
        if (_inputState.value.isCalculated) {
            if (output.globalFixed.value > 0) {
                expensesList.add(ResultItem("خرج کل به صورت مقطوع", output.globalFixed))
            }
            
            expensesList.add(ResultItem("کل تکانی", output.totalTekani))
            expensesList.add(ResultItem("سهم هر تکان", output.perPersonTekani))
            
            expensesList.add(ResultItem("کل جمع‌کنی", output.totalJamkoni))
            expensesList.add(ResultItem("سهم هر جمع‌کن", output.perPersonJamkoni))
            
            expensesList.add(ResultItem("کل کوله‌کشی", output.totalKooleh))
            expensesList.add(ResultItem("سهم هر کوله‌کش", output.perPersonKooleh))
            
            expensesList.add(ResultItem("کل سرکاری", output.totalSarkari))
            
            if (_inputState.value.sarkari.isHalfKari) {
                expensesList.add(ResultItem("سهم سرکاری (گروه ۱)", output.perPersonSarkariGroup1))
                expensesList.add(ResultItem("سهم سرکاری (گروه ۲)", output.perPersonSarkariGroup2))
            } else {
                expensesList.add(ResultItem("سهم هر سرکار", output.perPersonSarkari))
            }
            
            if (output.extraExpense.value > 0) {
                expensesList.add(ResultItem("خرج اضافی متفرقه", output.extraExpense))
            }

            // جمع کل هزینه‌ها برای کسر از استخر ماژول بعد
            totalExpensesValue = output.globalFixed.value + 
                                 output.totalTekani.value + 
                                 output.totalJamkoni.value + 
                                 output.totalKooleh.value + 
                                 output.totalSarkari.value + 
                                 output.extraExpense.value
        }

        // ۲. محاسبه استخر و اجرای ماژول کشاورزی/نیمه‌کاری
        // اگر خرجکرد صفر باشد، دقیقاً کل بار (totalWalnuts) به ماژول ۳ می‌رود
        val remainingForStage3 = totalWalnuts - WalnutUnit(totalExpensesValue)

        val agriInput = AgricultureStrategy.Input(
            remainingFromStage2 = remainingForStage3,
            isKeshavarzi = agricultureInput.isKeshavarzi,
            keshavarziRatioInput = agricultureInput.keshavarziRatioInput,
            isNimehkari = agricultureInput.isNimehkari
        )
        
        val agriOutput = AgricultureStrategy.calculate(agriInput)

        val agricultureResultsList = mutableListOf<ResultItem>()
        if (agricultureInput.isKeshavarzi) {
            agricultureResultsList.add(ResultItem("کسر سهم کشاورز", agriOutput.keshavarziTotal))
        }

        val nimehkariResultsList = mutableListOf<ResultItem>()
        if (agricultureInput.isNimehkari) {
            val partnerName = if (agricultureInput.partner1Name.isNotBlank()) "(${agricultureInput.partner1Name})" else ""
            nimehkariResultsList.add(ResultItem("کسر سهم نیمه‌کاری $partnerName", agriOutput.nimehkariTotal))
        }

        // نشان دادن خطِ باقی‌مانده نهایی فقط در صورتی که یکی از این دو تیک خورده باشد
        if (agricultureInput.isKeshavarzi || agricultureInput.isNimehkari) {
            nimehkariResultsList.add(ResultItem("خالص باقی‌مانده نهایی", agriOutput.remainingForStage4))
        }

        // ۳. الصاق خودکار سال به عنوان و ایجاد کپسول تاریخچه یکپارچه
        val finalName = if (calculationName.isNotBlank()) "$calculationName - $currentYear" else "بدون نام - $currentYear"

        val record = CalculationHistoryRecord(
            id = timestampLong.toString(), // ذخیره با تایم‌استمپ یونیک
            timestamp = timestampLong,
            calculationName = finalName,
            persianYear = currentYear,
            baseUnit = baseUnit,
            inputAmount = totalWalnuts,
            expensesResults = expensesList,
            agricultureResults = agricultureResultsList,
            nimehkariResults = nimehkariResultsList,
            finalSharesResults = emptyList() // آماده برای دریافت نتایج ماژول ۴ در قدم‌های بعدی
        )
        
        _snapshot.value = record
    }
}
