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
    val booleanInputs: Map<String, Boolean> = emptyMap(),
    val transferInputs: Map<String, String> = emptyMap() // اضافه شده برای نگهداری مقاصد انتقال سهم
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
                booleanInputs = emptyMap(),
                transferInputs = emptyMap()
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

    // متد جدید برای آپدیت تارگتِ انتقال سهم
    fun updateTransferInput(sourceId: String, transferToId: String) {
        _state.update { currentState ->
            val newMap = currentState.transferInputs.toMutableMap()
            newMap[sourceId] = transferToId
            currentState.copy(transferInputs = newMap)
        }
    }

    fun executeCalculation(currentYear: String, timestampLong: Long, baseUnit: String = "کیلوگرم"): CalculationHistoryRecord? {
        val profile = _state.value.activeProfile ?: return null
        
        var totalInput = 0.0
        var mainInputName = profile.name

        val baseBlock = profile.rootBlocks.filterIsInstance<BaseInputBlock>().firstOrNull()
        if (baseBlock != null) {
            totalInput = _state.value.textInputs[baseBlock.block_id + "_amount"]?.toDoubleOrNull() ?: 0.0
            val potentialName = _state.value.textInputs[baseBlock.block_id + "_name"]
            if (!potentialName.isNullOrBlank()) {
                mainInputName = potentialName
            }
        }

        val distInput = DistributionInput(
            poolAmount = WalnutUnit(totalInput),
            mode = DistributionMode.MODE_CUSTOM_BUILDER,
            customProfileId = profile.id,
            dynamicBooleans = _state.value.booleanInputs,
            dynamicTransfers = _state.value.transferInputs // متصل کردن نقشه انتقال
        )
        
        val sharesList = DistributionEngine.calculate(distInput)

        return CalculationHistoryRecord(
            id = timestampLong.toString(),
            timestamp = timestampLong,
            calculationName = mainInputName,
            persianYear = currentYear,
            baseUnit = baseUnit,
            inputAmount = WalnutUnit(totalInput),
            expensesResults = emptyList(),
            agricultureResults = emptyList(),
            nimehkariResults = emptyList(),
            finalSharesResults = sharesList,
            associated_profile_id = profile.id
        )
    }

    fun clearState() {
        _state.update { DynamicPlayerState() }
    }
}
