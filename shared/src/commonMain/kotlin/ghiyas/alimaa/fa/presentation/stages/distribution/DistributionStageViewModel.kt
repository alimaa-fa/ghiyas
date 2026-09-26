package ghiyas.alimaa.fa.presentation.stages.distribution

import ghiyas.alimaa.fa.domain.strategy.DistributionMode
import ghiyas.alimaa.fa.domain.strategy.ComprehensiveState
import ghiyas.alimaa.fa.domain.strategy.ModeBState
import ghiyas.alimaa.fa.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class ShareholderInput(val name: String = "", val ghiyasInput: String = "")

data class PoolDistributionState(
    val mode: DistributionMode = DistributionMode.MODE_A_NO_BREAKDOWN, 
    val groupName: String = "",                           
    val comprehensiveState: ComprehensiveState = ComprehensiveState(), 
    val modeBState: ModeBState = ModeBState(),
    val shareholders: List<ShareholderInput> = listOf(ShareholderInput()),
    val defaultStrategyTitle: String = "",
    val customProfileId: String = "", 
    val calculateZivar: Boolean = true,
    val targetGroup: String = "کل عبدالرحیمی‌ها",
    val transferDadallah: Boolean = false,
    val isUnifiedComprehensiveCalculation: Boolean = false,
    val dynamicBooleans: Map<String, Boolean> = emptyMap(),
    val dynamicAdvancedTransfers: Map<String, RuntimeTransferAction> = emptyMap() // مخزن پیشرفته
)

data class DistributionStageState(
    val mainPoolState: PoolDistributionState = PoolDistributionState(),
    val partner1PoolState: PoolDistributionState = PoolDistributionState(),
    val partner2PoolState: PoolDistributionState = PoolDistributionState()
)

enum class PoolTarget { MAIN, PARTNER_1, PARTNER_2 }

class DistributionStageViewModel {
    private val _state = MutableStateFlow(DistributionStageState())
    val state: StateFlow<DistributionStageState> = _state.asStateFlow()

    private fun generateUniqueId() = Random.nextLong().toString()
    fun clearForm() { _state.value = DistributionStageState() }

    private fun updatePoolState(target: PoolTarget, update: (PoolDistributionState) -> PoolDistributionState) {
        _state.update { currentState ->
            when (target) {
                PoolTarget.MAIN -> currentState.copy(mainPoolState = update(currentState.mainPoolState))
                PoolTarget.PARTNER_1 -> currentState.copy(partner1PoolState = update(currentState.partner1PoolState))
                PoolTarget.PARTNER_2 -> currentState.copy(partner2PoolState = update(currentState.partner2PoolState))
            }
        }
    }

    fun updateMode(target: PoolTarget, mode: DistributionMode) { updatePoolState(target) { it.copy(mode = mode) } }
    fun updateGroupName(target: PoolTarget, name: String) { updatePoolState(target) { it.copy(groupName = name) } }
    fun updateDefaultStrategy(target: PoolTarget, title: String) { updatePoolState(target) { it.copy(defaultStrategyTitle = title) } }
    fun updateCustomProfile(target: PoolTarget, id: String) { updatePoolState(target) { it.copy(customProfileId = id) } }
    fun updateCalculateZivar(target: PoolTarget, isChecked: Boolean) { updatePoolState(target) { it.copy(calculateZivar = isChecked) } }
    fun updateTargetGroup(target: PoolTarget, group: String) { updatePoolState(target) { it.copy(targetGroup = group) } }
    fun updateTransferDadallah(target: PoolTarget, isChecked: Boolean) { updatePoolState(target) { it.copy(transferDadallah = isChecked) } }
    fun updateUnifiedComprehensive(target: PoolTarget, isUnified: Boolean) { updatePoolState(target) { it.copy(isUnifiedComprehensiveCalculation = isUnified) } }

    fun updateDynamicBoolean(target: PoolTarget, blockId: String, isChecked: Boolean) {
        updatePoolState(target) {
            val newMap = it.dynamicBooleans.toMutableMap()
            newMap[blockId] = isChecked
            it.copy(dynamicBooleans = newMap)
        }
    }

    // --- متدهای مدیریت انتقال پیشرفته در تب وابسته ---
    fun initAdvancedTransfer(target: PoolTarget, sourceId: String) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            if (!newMap.containsKey(sourceId)) newMap[sourceId] = RuntimeTransferAction()
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun removeAdvancedTransfer(target: PoolTarget, sourceId: String) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            newMap.remove(sourceId)
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun updateTransferAmountType(target: PoolTarget, sourceId: String, type: TransferAmountType) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(sourceAmountType = type)
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun updateTransferAmountValue(target: PoolTarget, sourceId: String, value: String) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(sourceAmountValue = value)
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun updateTransferRule(target: PoolTarget, sourceId: String, rule: TransferDistributionRule) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            newMap[sourceId] = current.copy(distributionRule = rule)
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun addTransferTarget(target: PoolTarget, sourceId: String, targetId: String, isFemale: Boolean = false) {
        if (targetId.isBlank()) return
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: RuntimeTransferAction()
            if (current.targets.none { it.targetId == targetId }) {
                newMap[sourceId] = current.copy(targets = current.targets + AdvancedTransferTarget(targetId, isFemale))
            }
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }

    fun removeTransferTarget(target: PoolTarget, sourceId: String, targetId: String) {
        updatePoolState(target) { st ->
            val newMap = st.dynamicAdvancedTransfers.toMutableMap()
            val current = newMap[sourceId] ?: return@updatePoolState st
            newMap[sourceId] = current.copy(targets = current.targets.filter { it.targetId != targetId })
            st.copy(dynamicAdvancedTransfers = newMap)
        }
    }
    // --------------------------------------------------

    fun updateComprehensiveState(target: PoolTarget, update: (ComprehensiveState) -> ComprehensiveState) {
        updatePoolState(target) { it.copy(comprehensiveState = update(it.comprehensiveState)) }
    }

    private fun List<ShareholderNode>.updateNodeRecursive(path: List<String>, transform: (ShareholderNode) -> ShareholderNode): List<ShareholderNode> {
        if (path.isEmpty()) return this
        val targetId = path.first()
        return this.map { node -> 
            if (node.id == targetId) { 
                if (path.size == 1) transform(node) else node.copy(children = node.children.updateNodeRecursive(path.drop(1), transform)) 
            } else node 
        }
    }

    fun updateNode(target: PoolTarget, path: List<String>, update: (ShareholderNode) -> ShareholderNode) { 
        updateComprehensiveState(target) { st -> st.copy(nodes = st.nodes.updateNodeRecursive(path, update)) } 
    }

    fun addNode(target: PoolTarget, path: List<String>) { 
        val newNode = ShareholderNode(id = generateUniqueId())
        if (path.isEmpty()) { updateComprehensiveState(target) { it.copy(nodes = it.nodes + newNode) } } 
        else { updateNode(target, path) { it.copy(children = it.children + newNode) } } 
    }

    fun removeNode(target: PoolTarget, path: List<String>, idToRemove: String) { 
        if (path.isEmpty()) { updateComprehensiveState(target) { it.copy(nodes = it.nodes.filter { c -> c.id != idToRemove }) } } 
        else { updateNode(target, path) { it.copy(children = it.children.filter { c -> c.id != idToRemove }) } } 
    }
}
