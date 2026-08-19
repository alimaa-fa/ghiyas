package ir.ghiyas.alimaa.presentation.player

import ir.ghiyas.alimaa.domain.models.CustomProfile
import ir.ghiyas.alimaa.data.CustomProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DynamicPlayerState(
    val activeProfile: CustomProfile? = null,
    val textInputs: Map<String, String> = emptyMap(),
    val booleanInputs: Map<String, Boolean> = emptyMap()
)

class DynamicPlayerViewModel {
    private val _state = MutableStateFlow(DynamicPlayerState())
    val state: StateFlow<DynamicPlayerState> = _state.asStateFlow()

    fun loadProfile(profileId: String) {
        val profile = CustomProfileRepository.getAllProfiles().find { it.id == profileId }
        _state.update { 
            it.copy(
                activeProfile = profile,
                textInputs = emptyMap(), // ریست کردن مقادیر قبلی
                booleanInputs = emptyMap()
            ) 
        }
    }

    fun updateTextInput(blockId: String, value: String) {
        _state.update { currentState ->
            val newInputs = currentState.textInputs.toMutableMap()
            newInputs[blockId] = value
            currentState.copy(textInputs = newInputs)
        }
    }

    fun updateBooleanInput(blockId: String, isChecked: Boolean) {
        _state.update { currentState ->
            val newInputs = currentState.booleanInputs.toMutableMap()
            newInputs[blockId] = isChecked
            currentState.copy(booleanInputs = newInputs)
        }
    }

    fun clearState() {
        _state.update { DynamicPlayerState() }
    }
}
