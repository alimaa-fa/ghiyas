package ghiyas.alimaa.fa.presentation.player

import ghiyas.alimaa.fa.domain.models.*
import ghiyas.alimaa.fa.domain.strategy.DistributionEngine
import ghiyas.alimaa.fa.domain.strategy.DistributionInput
import ghiyas.alimaa.fa.domain.strategy.DistributionMode
import ghiyas.alimaa.fa.data.CustomProfileRepository
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
                textInputs = emptyMap(),
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

    // Real Execution Engine Connection
    fun executeCalculation(currentYear: String, timestampLong: Long, baseUnit: String = "کیلوگرم"): CalculationHistoryRecord? {
        val profile = _state.value.activeProfile ?: return null
        
        var totalInput = 0.0
        var mainInputName = profile.name

        // Extract values from the dynamic UI forms
        val baseBlock = profile.rootBlocks.filterIsInstance<BaseInputBlock>().firstOrNull()
        if (baseBlock != null) {
            totalInput = _state.value.textInputs[baseBlock.block_id + "_amount"]?.toDoubleOrNull() ?: 0.0
            val potentialName = _state.value.textInputs[baseBlock.block_id + "_name"]
            if (!potentialName.isNullOrBlank()) {
                mainInputName = potentialName
            }
        }

        // Send to Engine to calculate actual shares based on profile structure
        val distInput = DistributionInput(
            poolAmount = WalnutUnit(totalInput),
            mode = DistributionMode.MODE_CUSTOM_BUILDER,
            customProfileId = profile.id,
            dynamicBooleans = _state.value.booleanInputs
        )
        
        val sharesList = DistributionEngine.calculate(distInput)

        return CalculationHistoryRecord(
            id = timestampLong.toString(),
            timestamp = timestampLong,
            calculationName = mainInputName,
            persianYear = currentYear,
            baseUnit = baseUnit, // Kept dynamic/customizable parameter
            inputAmount = WalnutUnit(totalInput),
            expensesResults = emptyList(), // Can add custom expenses logic later
            agricultureResults = emptyList(),
            nimehkariResults = emptyList(),
            finalSharesResults = sharesList, // Correctly calculated shares
            associated_profile_id = profile.id
        )
    }

    fun clearState() {
        _state.update { DynamicPlayerState() }
    }
}
