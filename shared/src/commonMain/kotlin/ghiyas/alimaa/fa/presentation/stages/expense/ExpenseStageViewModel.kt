package ghiyas.alimaa.fa.presentation.stages.expense

import ghiyas.alimaa.fa.domain.models.CalculationHistoryRecord
import ghiyas.alimaa.fa.domain.models.ResultItem
import ghiyas.alimaa.fa.domain.models.WalnutUnit
import ghiyas.alimaa.fa.domain.strategy.KharjkardStrategy
import ghiyas.alimaa.fa.domain.strategy.AgricultureStrategy
import ghiyas.alimaa.fa.domain.strategy.DistributionEngine
import ghiyas.alimaa.fa.domain.strategy.DistributionInput
import ghiyas.alimaa.fa.domain.strategy.Shareholder
import ghiyas.alimaa.fa.domain.strategy.DistributionMode
import ghiyas.alimaa.fa.presentation.stages.agriculture.AgricultureInputState
import ghiyas.alimaa.fa.presentation.stages.distribution.DistributionStageState
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
    val isCalculated: Boolean = false, val extraExpense_Input: String = "",
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
    fun updateExtraExpense(value: String) { _inputState.update { it.copy(extraExpense_Input = value) } }
    fun updateTekani(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(tekani = update(it.tekani)) } }
    fun updateJamkoni(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(jamkoni = update(it.jamkoni)) } }
    fun updateKooleh(update: (ExpenseCategoryState) -> ExpenseCategoryState) { _inputState.update { it.copy(kooleh = update(it.kooleh)) } }
    fun updateSarkari(update: (SarkariCategoryState) -> SarkariCategoryState) { _inputState.update { it.copy(sarkari = update(it.sarkari)) } }

    fun setExternalSnapshot(record: CalculationHistoryRecord) {
        _snapshot.value = record
    }

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

            totalExpensesValue = output.totalTekani.value + output.totalJamkoni.value + output.totalKooleh.value + output.totalSarkari.value + output.extraExpense.value
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

        var isDadallahTransferActive = false
        if (agricultureInput.isNimehkari) {
            val p1State = distributionInput.partner1PoolState
            if (p1State.defaultStrategyTitle.contains("عبدالرحیم") && p1State.targetGroup == "کل عبدالرحیمی‌ها" && p1State.transferDadallah) {
                isDadallahTransferActive = true
            }
        } else {
            val mainState = distributionInput.mainPoolState
            if (mainState.defaultStrategyTitle.contains("عبدالرحیم") && mainState.targetGroup == "کل عبدالرحیمی‌ها" && mainState.transferDadallah) {
                isDadallahTransferActive = true
            }
        }

        if (isDadallahTransferActive) {
            nimehkariResultsList.add(ResultItem("باقیمانده (جهت تسهیم سهم زیور)", agriOutput.remainingForStage4))
            nimehkariResultsList.add(ResultItem("باقیمانده (بعد از انتقال سهم دادالله)", agriOutput.remainingForStage4 + agriOutput.nimehkariTotal))
        } else {
            nimehkariResultsList.add(ResultItem("باقیمانده نهایی (جهت تسهیم)", agriOutput.remainingForStage4))
        }

        val finalSharesList = mutableListOf<ResultItem>()
        val poolAmount = agriOutput.remainingForStage4

        if (agricultureInput.isNimehkari) {
            val p1State = distributionInput.partner1PoolState
            val p1Strategy = ghiyas.alimaa.fa.domain.strategy.DefaultCalculationsRegistry.strategies.find { it.title == p1State.defaultStrategyTitle }
            val isP1GlobalMacro = p1State.mode == DistributionMode.MODE_DEFAULT_MAKER && p1Strategy?.isGlobalMacro == true

            if (isP1GlobalMacro) {
                val p1Input = DistributionInput(
                    poolAmount = agriOutput.remainingForStage4, mode = p1State.mode, groupName = p1State.groupName, 
                    comprehensiveState = p1State.comprehensiveState, modeBState = p1State.modeBState,
                    shareholders = p1State.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                    defaultStrategyTitle = p1State.defaultStrategyTitle, customProfileId = p1State.customProfileId,
                    defaultLabel = "سهم یکجا کل", calculateZivar = p1State.calculateZivar, isNimehkari = true, nimehkariPool = agriOutput.nimehkariTotal,
                    targetGroup = p1State.targetGroup, transferDadallah = p1State.transferDadallah,
                    dynamicBooleans = p1State.dynamicBooleans,
                    dynamicTransfers = p1State.dynamicTransfers // متصل کردن نقشه انتقال
                )
                finalSharesList.addAll(DistributionEngine.calculate(p1Input))
            } else {
                val p1Pool = agriOutput.nimehkariTotal 
                val p2Pool = agriOutput.remainingForStage4 
                
                val p1Input = DistributionInput(
                    poolAmount = p1Pool, mode = p1State.mode, groupName = p1State.groupName, 
                    comprehensiveState = p1State.comprehensiveState, modeBState = p1State.modeBState,
                    shareholders = p1State.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                    defaultStrategyTitle = p1State.defaultStrategyTitle, customProfileId = p1State.customProfileId,
                    defaultLabel = "سهم شریک ۱", calculateZivar = p1State.calculateZivar, isNimehkari = agricultureInput.isNimehkari, nimehkariPool = agriOutput.nimehkariTotal,
                    targetGroup = p1State.targetGroup, transferDadallah = p1State.transferDadallah,
                    dynamicBooleans = p1State.dynamicBooleans,
                    dynamicTransfers = p1State.dynamicTransfers // متصل کردن نقشه انتقال
                )
                val p1NameSuffix = if (agricultureInput.partner1Name.isNotBlank()) " (${agricultureInput.partner1Name})" else ""
                finalSharesList.addAll(DistributionEngine.calculate(p1Input).map { ResultItem(it.label + p1NameSuffix, it.value) })

                val p2State = distributionInput.partner2PoolState
                val p2Input = DistributionInput(
                    poolAmount = p2Pool, mode = p2State.mode, groupName = p2State.groupName, 
                    comprehensiveState = p2State.comprehensiveState, modeBState = p2State.modeBState,
                    shareholders = p2State.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                    defaultStrategyTitle = p2State.defaultStrategyTitle, customProfileId = p2State.customProfileId,
                    defaultLabel = "سهم شریک ۲", calculateZivar = p2State.calculateZivar, isNimehkari = agricultureInput.isNimehkari, nimehkariPool = agriOutput.nimehkariTotal,
                    targetGroup = p2State.targetGroup, transferDadallah = p2State.transferDadallah,
                    dynamicBooleans = p2State.dynamicBooleans,
                    dynamicTransfers = p2State.dynamicTransfers // متصل کردن نقشه انتقال
                )
                val p2NameSuffix = if (agricultureInput.partner2Name.isNotBlank()) " (${agricultureInput.partner2Name})" else ""
                finalSharesList.addAll(DistributionEngine.calculate(p2Input).map { ResultItem(it.label + p2NameSuffix, it.value) })
            }
        } else {
            val mainState = distributionInput.mainPoolState
            val distInput = DistributionInput(
                poolAmount = poolAmount, mode = mainState.mode, groupName = mainState.groupName, 
                comprehensiveState = mainState.comprehensiveState, modeBState = mainState.modeBState, 
                shareholders = mainState.shareholders.map { Shareholder(it.name, it.ghiyasInput.toDoubleOrNull() ?: 0.0) },
                defaultStrategyTitle = mainState.defaultStrategyTitle, customProfileId = mainState.customProfileId,
                defaultLabel = "سهم کل یکجا", calculateZivar = mainState.calculateZivar, isNimehkari = false, nimehkariPool = WalnutUnit.ZERO,
                targetGroup = mainState.targetGroup, transferDadallah = mainState.transferDadallah,
                dynamicBooleans = mainState.dynamicBooleans,
                dynamicTransfers = mainState.dynamicTransfers // متصل کردن نقشه انتقال
            )
            finalSharesList.addAll(DistributionEngine.calculate(distInput))
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
