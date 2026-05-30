package ir.ghiyas.alimaa.domain.strategy

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.presentation.stages.expense.KharjkardInputState
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseCategoryState

object KharjkardStrategy {

    data class Output(
        val globalFixed: WalnutUnit,
        val totalTekani: WalnutUnit, val perPersonTekani: WalnutUnit,
        val totalJamkoni: WalnutUnit, val perPersonJamkoni: WalnutUnit,
        val totalKooleh: WalnutUnit, val perPersonKooleh: WalnutUnit,
        val totalSarkari: WalnutUnit, val perPersonSarkari: WalnutUnit,
        val perPersonSarkariGroup1: WalnutUnit, val perPersonSarkariGroup2: WalnutUnit,
        val extraExpense: WalnutUnit
    )

    fun calculate(state: KharjkardInputState, totalWalnuts: WalnutUnit): Output {
        val globalFixed = WalnutUnit.fromInput(state.globalFixedExpense_Input)
        val extraExpense = WalnutUnit.fromInput(state.extraExpense_Input)

        if (!state.isCalculated) {
            return Output(globalFixed, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, WalnutUnit.ZERO, extraExpense)
        }

        fun calcStandard(catState: ExpenseCategoryState): Pair<WalnutUnit, WalnutUnit> {
            val total: WalnutUnit
            if (catState.isFixed) {
                total = WalnutUnit.fromInput(catState.mizan)
            } else {
                val mizanVal = catState.mizan.toDoubleOrNull() ?: 0.0
                val base = if (mizanVal > 0) totalWalnuts / mizanVal else WalnutUnit.ZERO
                total = if (catState.hasExtra) {
                    base + WalnutUnit.fromInput(catState.extraValue)
                } else {
                    base
                }
            }
            val tedadVal = catState.tedad.toDoubleOrNull() ?: 1.0
            val perPerson = if (tedadVal > 0) total / tedadVal else WalnutUnit.ZERO
            return Pair(total, perPerson)
        }

        val (totalTekani, perPersonTekani) = calcStandard(state.tekani)
        val (totalJamkoni, perPersonJamkoni) = calcStandard(state.jamkoni)
        val (totalKooleh, perPersonKooleh) = calcStandard(state.kooleh)

        // Sarkari
        val totalSarkari: WalnutUnit
        if (state.sarkari.isFixed) {
            totalSarkari = WalnutUnit.fromInput(state.sarkari.mizan)
        } else {
            val mizanVal = state.sarkari.mizan.toDoubleOrNull() ?: 0.0
            val base = if (mizanVal > 0) totalWalnuts / mizanVal else WalnutUnit.ZERO
            totalSarkari = if (state.sarkari.hasExtra) {
                base + WalnutUnit.fromInput(state.sarkari.extraValue)
            } else {
                base
            }
        }

        var perPersonSarkari = WalnutUnit.ZERO
        var perPersonSarkariGroup1 = WalnutUnit.ZERO
        var perPersonSarkariGroup2 = WalnutUnit.ZERO

        if (state.sarkari.isHalfKari) {
            val half = totalSarkari / 2.0
            val g1 = state.sarkari.group1Count.toDoubleOrNull() ?: 0.0
            val g2 = state.sarkari.group2Count.toDoubleOrNull() ?: 0.0
            perPersonSarkariGroup1 = if (g1 > 0) half / g1 else WalnutUnit.ZERO
            perPersonSarkariGroup2 = if (g2 > 0) half / g2 else WalnutUnit.ZERO
        } else {
            val tedadVal = state.sarkari.tedad.toDoubleOrNull() ?: 1.0
            perPersonSarkari = if (tedadVal > 0) totalSarkari / tedadVal else WalnutUnit.ZERO
        }

        return Output(
            globalFixed,
            totalTekani, perPersonTekani,
            totalJamkoni, perPersonJamkoni,
            totalKooleh, perPersonKooleh,
            totalSarkari, perPersonSarkari,
            perPersonSarkariGroup1, perPersonSarkariGroup2,
            extraExpense
        )
    }
}