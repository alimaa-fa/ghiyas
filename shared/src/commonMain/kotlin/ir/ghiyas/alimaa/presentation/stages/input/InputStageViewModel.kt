package ir.ghiyas.alimaa.presentation.stages.input

import androidx.lifecycle.ViewModel
import ir.ghiyas.alimaa.domain.models.UnitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InputStageState(
    val calculationName: String = "",
    val unitType: UnitType = UnitType.HAND_PIECE,
    val totalAmount: String = ""
)

class InputStageViewModel : ViewModel() {
    private val _state = MutableStateFlow(InputStageState())
    val state: StateFlow<InputStageState> = _state.asStateFlow()

    fun onCalculationNameChange(name: String) {
        _state.update { it.copy(calculationName = name) }
    }

    fun onUnitTypeChange(type: UnitType) {
        _state.update { it.copy(unitType = type) }
    }

    fun onTotalAmountChange(amount: String) {
        _state.update { it.copy(totalAmount = amount) }
    }

    fun clearForm() {
        _state.update { InputStageState() }
    }
}
