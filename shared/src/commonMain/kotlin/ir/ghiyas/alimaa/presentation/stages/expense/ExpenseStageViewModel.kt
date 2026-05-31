package ir.ghiyas.alimaa.presentation.stages.expense

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.strategy.KharjkardStrategy
import ir.ghiyas.alimaa.domain.strategy.AgricultureStrategy
import ir.ghiyas.alimaa.domain.strategy.DistributionEngine
import ir.ghiyas.alimaa.domain.strategy.DistributionInput
import ir.ghiyas.alimaa.domain.strategy.Shareholder
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureInputState
import ir.ghiyas.alimaa.presentation.stages.distribution.DistributionStageState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ExpenseCategoryState(
    val mizan: String = "", val tedad: String = "", val isFixed: Boolean = false, val hasExtra: Boolean = false, val extraValue: String = ""
)

data class SarkariCategoryState(
    val mizan: String = "", val tedad: String = "", val isFixed: Boolean = false, val isHalfKari: Boolean = false,
    val group1Count: String = "", val group2Count: String = "", val hasExtra: Boolean = false, val extraValue: String = ""
)

data class KharjkardInputState(
    val isCalculated: Boolean = false, val globalFixedExpense_Input: String = "", val extraExpense_Input: String = "",
    val tekani: ExpenseCategoryState = ExpenseCategoryState(), val jamkoni: ExpenseCategoryState = ExpenseCategoryState(),
    val kooleh: ExpenseCategoryState = ExpenseCategoryState(), val sarkari: SarkariCategoryState = SarkariCategoryState()
)

class ExpenseStageViewModel {
    private val _inputState = MutableStateFlow(KharjkardInputState())
    val inputState: StateFlow<KharjkardInputState> = _inputState.asStateFlow()

    private val _snapshot = MutableStateFlow<CalculationHistoryRecord?>(null)
    val snapshot: StateFlow<CalculationHistoryRecord?> = _snapshot.asStateFlow()

    var totalWalnuts: WalnutUnit = WalnutUnit.ZERO

    fun setTotalWalnuts(unit: WalnutUnit) { totalWalnuts = unit }
    fun clearForm() { _inputState.value = KharjkardInputState(); _snapshot.value = null }
    fun updateIsCalculated(isCalculated: Boolean) { _inputState.update { it.copy(isCalculated = isCalculated) } }
    fun updateGlobalFixedExpense(value: String) { _inputState.update { it.copy(globalFixedExpense_Input = value) } }
    fun updateExtraExpense(value: String) { _inputState.update { it.copy(extraExpense_Input = value) } }
    fun updateTekani(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(tekani = update(it.tekani)) } }
    fun updateJamkoni(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(jamkoni = update(it.jamkoni)) } }
    fun updateKooleh(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(kooleh = update(it.kooleh)) } }
    fun updateSarkari(update: (SarkariCategoryState) -> SarkariCategoryState) { _inputState.update { it.copy(sarkari = update(it.sarkari)) } }

    fun calculateAndSnapshot(
        calculationName: String, 
        baseUnit: String, 
        currentYear: String, 
        timestampLong: Long,
        agricultureInput: AgricultureInputState,
        distributionInput: DistributionStageState
    ) {
        val output = KharjkardStrategy.calculate(_inputState.value, totalWalnuts)
        val expensesList = mutableListOf<ResultItem>()
        var totalExpensesValue = 0.0 
        
        if (_inputState.value.isCalculated) {
            if (output.globalFixed.value > 0) expensesList.add(ResultItem("خرج کل به صورت مقطوع", output.globalFixed))
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
            if (output.extraExpense.value > 0) expensesList.add(ResultItem("خرج اضافی متفرقه", output.extraExpense))

            totalExpensesValue = output.globalFixed.value + output.totalTekani.value + output.totalJamkoni.value + output.totalKooleh.value + output.totalSarkari.value + output.extraExpense.value
        }

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

        if (agricultureInput.isKeshavarzi || agricultureInput.isNimehkari) {
            nimehkariResultsList.add(ResultItem("خالص باقی‌مانده برای تسهیم", agriOutput.remainingForStage4))
        }

        val finalSharesList = mutableListOf<ResultItem>()
        val poolAmount = agriOutput.remainingForStage4

        if (agricultureInput.isNimehkari) {
            val halfPool = poolAmount / 2.0
            
            // پردازش استخر اول و الصاق هوشمند نام شریک به برچسب نتایج
            val p1State = distributionInput.partner1PoolState
            val p1Input = DistributionInput(
                poolAmount = halfPool, mode = p1State.mode, groupName = p1State.groupName,
                peopleCountInput = p1State.peopleCountInput, isBoyGirlSplit = p1State.isBoyGirlSplit,
                shareholders = p1State.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                defaultStrategyTitle = p1State.defaultStrategyTitle,
                defaultLabel = "نیمه اول"
            )
            val p1NameSuffix = if (agricultureInput.partner1Name.isNotBlank()) " [${agricultureInput.partner1Name}]" else " [شریک ۱]"
            val p1Results = DistributionEngine.calculate(p1Input).map { ResultItem(it.label + p1NameSuffix, it.value) }
            finalSharesList.addAll(p1Results)

            // پردازش استخر دوم و الصاق هوشمند نام شریک به برچسب نتایج
            val p2State = distributionInput.partner2PoolState
            val p2Input = DistributionInput(
                poolAmount = halfPool, mode = p2State.mode, groupName = p2State.groupName,
                peopleCountInput = p2State.peopleCountInput, isBoyGirlSplit = p2State.isBoyGirlSplit,
                shareholders = p2State.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                defaultStrategyTitle = p2State.defaultStrategyTitle,
                defaultLabel = "نیمه دوم"
            )
            val p2NameSuffix = if (agricultureInput.partner2Name.isNotBlank()) " [${agricultureInput.partner2Name}]" else " [شریک ۲]"
            val p2Results = DistributionEngine.calculate(p2Input).map { ResultItem(it.label + p2NameSuffix, it.value) }
            finalSharesList.addAll(p2Results)
            
        } else {
            val mainState = distributionInput.mainPoolState
            val distInput = DistributionInput(
                poolAmount = poolAmount, mode = mainState.mode, groupName = mainState.groupName,
                peopleCountInput = mainState.peopleCountInput, isBoyGirlSplit = mainState.isBoyGirlSplit,
                shareholders = mainState.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                defaultStrategyTitle = mainState.defaultStrategyTitle,
                defaultLabel = "سهم کل یکجا"
            )
            val results = DistributionEngine.calculate(distInput)
            finalSharesList.addAll(results)
        }

        val finalName = if (calculationName.isNotBlank()) "$calculationName - $currentYear" else "بدون نام - $currentYear"

        val record = CalculationHistoryRecord(
            id = timestampLong.toString(), 
            timestamp = timestampLong,
            calculationName = finalName,
            persianYear = currentYear,
            baseUnit = baseUnit,
            inputAmount = totalWalnuts,
            expensesResults = expensesList,
            agricultureResults = agricultureResultsList,
            nimehkariResults = nimehkariResultsList,
            finalSharesResults = finalSharesList
        )
        
        _snapshot.value = record
    }
}
