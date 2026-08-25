package ir.ghiyas.alimaa.presentation.stages.distribution

import ir.ghiyas.alimaa.domain.strategy.DistributionMode
import ir.ghiyas.alimaa.domain.strategy.ComprehensiveState
import ir.ghiyas.alimaa.domain.strategy.ModeBState
import ir.ghiyas.alimaa.domain.models.ComprehensiveMode
import ir.ghiyas.alimaa.domain.models.ShareholderNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

// این کلاس ShareholderInput باید حتماً اینجا باشد تا ExpenseStageViewModel خطا ندهد
data class ShareholderInput(val name: String = "", val ghiyasInput: String = "")

data class PoolDistributionState(
    val mode: DistributionMode = DistributionMode.MODE_A_NO_BREAKDOWN, 
    val groupName: String = "",                           
    val comprehensiveState: ComprehensiveState = ComprehensiveState(), 
    val modeBState: ModeBState = ModeBState(), // Legacy
    val shareholders: List<ShareholderInput> = listOf(ShareholderInput()), // نوع لیست بازگردانی شد
    val defaultStrategyTitle: String = "",
    val customProfileId: String = "", 
    val calculateZivar: Boolean = true,
    val targetGroup: String = "کل عبدالرحیمی‌ها",
    val transferDadallah: Boolean = false        
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
    
    // === متدهای موتور جدید جامع (Comprehensive) ===
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
