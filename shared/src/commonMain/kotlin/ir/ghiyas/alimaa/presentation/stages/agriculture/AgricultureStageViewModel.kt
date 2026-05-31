package ir.ghiyas.alimaa.presentation.stages.agriculture

import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.strategy.AgricultureStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// استیت ورودی‌های ماژول ۳
data class AgricultureInputState(
    val isKeshavarzi: Boolean = false,
    val keshavarziRatioInput: String = "",
    val isNimehkari: Boolean = false,
    val partner1Name: String = "",
    val partner2Name: String = ""
)

class AgricultureStageViewModel {
    private val _inputState = MutableStateFlow(AgricultureInputState())
    val inputState: StateFlow<AgricultureInputState> = _inputState.asStateFlow()

    private val _output = MutableStateFlow<AgricultureStrategy.Output?>(null)
    val output: StateFlow<AgricultureStrategy.Output?> = _output.asStateFlow()

    fun updateIsKeshavarzi(value: Boolean) {
        _inputState.update { it.copy(isKeshavarzi = value) }
    }

    fun updateKeshavarziRatio(value: String) {
        _inputState.update { it.copy(keshavarziRatioInput = value) }
    }

    fun updateIsNimehkari(value: Boolean) {
        _inputState.update { it.copy(isNimehkari = value) }
    }

    fun updatePartner1Name(value: String) {
        _inputState.update { it.copy(partner1Name = value) }
    }

    fun updatePartner2Name(value: String) {
        _inputState.update { it.copy(partner2Name = value) }
    }

    // متد محاسبه که باقی‌مانده‌ی ماژول قبل را به عنوان ورودی می‌گیرد
    fun calculate(remainingFromStage2: WalnutUnit) {
        val currentState = _inputState.value
        val strategyInput = AgricultureStrategy.Input(
            remainingFromStage2 = remainingFromStage2,
            isKeshavarzi = currentState.isKeshavarzi,
            keshavarziRatioInput = currentState.keshavarziRatioInput,
            isNimehkari = currentState.isNimehkari
        )
        
        val result = AgricultureStrategy.calculate(strategyInput)
        _output.value = result
    }

    fun clearForm() {
        _inputState.value = AgricultureInputState()
        _output.value = null
    }
}
