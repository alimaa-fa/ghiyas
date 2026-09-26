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
    val advancedTransfers: Map<String, RuntimeTransferAction> = emptyMap() // مخزن جدید انتقال پیشرفته
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
                advancedTransfers = emptyMap()
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

    // --- متدهای مدیریت انتقال پیشرفته در زمان اجرا ---
    fun initAdvancedTransfer(sourceId: String) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            if (!newMap.containsKey(sourceId)) {
                newMap[sourceId] = RuntimeTransferAction()
            }
            st.copy(advancedTransfers = newMap)
        }
    }

    fun removeAdvancedTransfer(sourceId: String) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            newMap.remove(sourceId)
            st.copy(advancedTransfers = newMap)
        }
    }

    fun updateTransferAmountType(sourceId: String, type: TransferAmountType) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(sourceAmountType = type)
            st.copy(advancedTransfers = newMap)
        }
    }

    fun updateTransferAmountValue(sourceId: String, value: String) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(sourceAmountValue = value)
            st.copy(advancedTransfers = newMap)
        }
    }

    fun updateTransferRule(sourceId: String, rule: TransferDistributionRule) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(distributionRule = rule)
            st.copy(advancedTransfers = newMap)
        }
    }

    fun addTransferTarget(sourceId: String, targetId: String, isFemale: Boolean = false) {
        if (targetId.isBlank()) return
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            if (current.targets.none { it.targetId == targetId }) {
                newMap[sourceId] = current.copy(targets = current.targets + AdvancedTransferTarget(targetId, isFemale))
            }
            st.copy(advancedTransfers = newMap)
        }
    }

    fun removeTransferTarget(sourceId: String, targetId: String) {
        _state.update { st ->
            val newMap = st.advancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: return@update st
            newMap[sourceId] = current.copy(targets = current.targets.filter { it.targetId != targetId })
            st.copy(advancedTransfers = newMap)
        }
    }
    // --------------------------------------------------

    fun executeCalculation(currentYear: String, timestampLong: Long, baseUnit: String = "کیلوگرم"): CalculationHistoryRecord? {
        val profile = _state.value.activeProfile ?: return null
        
        var totalInput = 0.0
        var mainInputName = profile.name

        val baseBlock = profile.rootBlocks.filterIsInstance<BaseInputBlock>().firstOrNull()
        if (baseBlock != null) {
            totalInput = _state.value.textInputs[baseBlock.block_id + "_amount"]?.toDoubleOrNull() ?: 0.0
            val potentialName = _state.value.textInputs[baseBlock.block_id + "_name"]
            if (!potentialName.isNullOrBlank()) { mainInputName = potentialName }
        }

        val distInput = DistributionInput(
            poolAmount = WalnutUnit(totalInput),
            mode = DistributionMode.MODE_CUSTOM_BUILDER,
            customProfileId = profile.id,
            dynamicBooleans = _state.value.booleanInputs,
            dynamicAdvancedTransfers = _state.value.advancedTransfers // پاس دادن اطلاعات پیشرفته به موتور
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

    fun clearState() { _state.update { DynamicPlayerState() } }
}
