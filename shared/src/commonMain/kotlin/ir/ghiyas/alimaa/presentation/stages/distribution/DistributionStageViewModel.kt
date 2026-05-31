package ir.ghiyas.alimaa.presentation.stages.distribution

import ir.ghiyas.alimaa.domain.strategy.DistributionMode
import ir.ghiyas.alimaa.domain.strategy.ModeBState
import ir.ghiyas.alimaa.domain.strategy.PersonNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class ShareholderInput(val name: String = "", val ghiyasInput: String = "")

data class PoolDistributionState(
    val mode: DistributionMode = DistributionMode.MODE_B_SIMPLE,
    val groupName: String = "",                           
    val modeBState: ModeBState = ModeBState(),                  
    val shareholders: List<ShareholderInput> = listOf(ShareholderInput()), 
    val defaultStrategyTitle: String = "",
    val calculateZivar: Boolean = true,
    val targetGroup: String = "کل عبدالرحیمی‌ها", // وضعیت پیش‌فرض گروه هدف
    val transferDadallah: Boolean = false        // وضعیت پیش‌فرض تیک انتقال دادالله
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
    fun updateCalculateZivar(target: PoolTarget, isChecked: Boolean) { updatePoolState(target) { it.copy(calculateZivar = isChecked) } }
    fun updateTargetGroup(target: PoolTarget, group: String) { updatePoolState(target) { it.copy(targetGroup = group) } } // متد به‌روزرسانی گروه هدف
    fun updateTransferDadallah(target: PoolTarget, isChecked: Boolean) { updatePoolState(target) { it.copy(transferDadallah = isChecked) } } // متد به‌روزرسانی تیک انتقال سهم
    
    fun addShareholder(target: PoolTarget) { updatePoolState(target) { it.copy(shareholders = it.shareholders + ShareholderInput()) } }
    fun updateShareholder(target: PoolTarget, index: Int, name: String, ghiyasInput: String) {
        updatePoolState(target) { state ->
            val newList = state.shareholders.toMutableList()
            if (index in newList.indices) newList[index] = ShareholderInput(name, ghiyasInput)
            state.copy(shareholders = newList)
        }
    }
    fun removeShareholder(target: PoolTarget, index: Int) {
        updatePoolState(target) { state ->
            val newList = state.shareholders.toMutableList()
            if (index in newList.indices && newList.size > 1) newList.removeAt(index)
            state.copy(shareholders = newList)
        }
    }

    fun updateModeBState(target: PoolTarget, update: (ModeBState) -> ModeBState) {
        updatePoolState(target) { it.copy(modeBState = update(it.modeBState)) }
    }

    private fun List<PersonNode>.updateNodeRecursive(path: List<String>, transform: (PersonNode) -> PersonNode): List<PersonNode> {
        if (path.isEmpty()) return this
        val targetId = path.first()
        return this.map { node ->
            if (node.id == targetId) {
                if (path.size == 1) transform(node)
                else node.copy(subNodes = node.subNodes.updateNodeRecursive(path.drop(1), transform))
            } else node
        }
    }

    fun updatePersonNode(target: PoolTarget, path: List<String>, update: (PersonNode) -> PersonNode) {
        updateModeBState(target) { bState ->
            bState.copy(children = bState.children.updateNodeRecursive(path, update))
        }
    }

    fun addPersonNode(target: PoolTarget, path: List<String>) {
        val newNode = PersonNode(id = generateUniqueId())
        if (path.isEmpty()) {
            updateModeBState(target) { it.copy(children = it.children + newNode) }
        } else {
            updatePersonNode(target, path) { it.copy(subNodes = it.subNodes + newNode) }
        }
    }

    fun removePersonNode(target: PoolTarget, path: List<String>, idToRemove: String) {
        if (path.isEmpty()) {
            updateModeBState(target) { it.copy(children = it.children.filter { c -> c.id != idToRemove }) }
        } else {
            updatePersonNode(target, path) { it.copy(subNodes = it.subNodes.filter { c -> c.id != idToRemove }) }
        }
    }
}
