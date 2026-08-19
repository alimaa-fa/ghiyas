package ir.ghiyas.alimaa.presentation.builder

import ir.ghiyas.alimaa.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BuilderViewModel {
    private val _state = MutableStateFlow(BuilderState())
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    // پاکسازی بوم برای ساخت الگوی جدید
    fun clearForNewProfile() { _state.update { BuilderState() } }
    
    // بارگذاری الگو برای ویرایش
    fun loadProfileForEdit(profile: CustomProfile) {
        _state.update {
            BuilderState(
                editingProfileId = profile.id,
                profileName = profile.name,
                profileDescription = profile.description,
                integrationType = profile.integrationType,
                nimehkariMacroEnabled = profile.nimehkariMacroEnabled,
                rootBlocks = profile.rootBlocks
            )
        }
    }

    fun updateProfileName(name: String) { _state.update { it.copy(profileName = name) } }
    fun updateProfileDescription(desc: String) { _state.update { it.copy(profileDescription = desc) } }
    fun updateIntegrationType(type: ProfileIntegrationType) { _state.update { it.copy(integrationType = type) } }
    fun toggleNimehkariMacro(isEnabled: Boolean) { _state.update { it.copy(nimehkariMacroEnabled = isEnabled) } }

    private fun generateId(): String = "blk_${kotlin.random.Random.nextLong(1000000, 9999999)}"
    private fun generateAlias(): String = "var_${kotlin.random.Random.nextLong(1000000, 9999999)}"

    fun addRootBlock(block: CustomBlock) { _state.update { it.copy(rootBlocks = it.rootBlocks + block) } }
    fun addChildToBlock(parentId: String, newChild: CustomBlock) { _state.update { it.copy(rootBlocks = addNodeRecursively(it.rootBlocks, parentId, newChild, asSibling = false)) } }
    fun addSiblingToBlock(targetSiblingId: String, newSibling: CustomBlock) { _state.update { it.copy(rootBlocks = addNodeRecursively(it.rootBlocks, targetSiblingId, newSibling, asSibling = true)) } }
    fun deleteBlock(targetId: String) { _state.update { it.copy(rootBlocks = deleteNodeRecursively(it.rootBlocks, targetId)) } }

    fun updateBlock(targetId: String, updater: (CustomBlock) -> CustomBlock) { _state.update { it.copy(rootBlocks = updateNodeRecursively(it.rootBlocks, targetId, updater)) } }

    fun getAllConditionGates(): List<ConditionGate> {
        val conditions = mutableListOf<ConditionGate>()
        fun traverse(blocks: List<CustomBlock>) {
            blocks.forEach { block ->
                if (block is ConditionGate) conditions.add(block)
                when (block) {
                    is BaseInputBlock -> traverse(block.childBlocks)
                    is StageBlock -> traverse(block.childBlocks)
                    is ConditionGate -> traverse(block.childBlocks)
                    is MemberBlock -> traverse(block.childBlocks)
                    is PartnerBlock -> traverse(block.siblingBlocks)
                    else -> {}
                }
            }
        }
        traverse(_state.value.rootBlocks)
        return conditions
    }

    private fun updateNodeRecursively(list: List<CustomBlock>, targetId: String, updater: (CustomBlock) -> CustomBlock): List<CustomBlock> {
        return list.map { block ->
            if (block.block_id == targetId) updater(block)
            else when (block) {
                is BaseInputBlock -> block.copy(childBlocks = updateNodeRecursively(block.childBlocks, targetId, updater))
                is StageBlock -> block.copy(childBlocks = updateNodeRecursively(block.childBlocks, targetId, updater))
                is ConditionGate -> block.copy(childBlocks = updateNodeRecursively(block.childBlocks, targetId, updater))
                is MemberBlock -> block.copy(childBlocks = updateNodeRecursively(block.childBlocks, targetId, updater))
                is PartnerBlock -> block.copy(siblingBlocks = updateNodeRecursively(block.siblingBlocks, targetId, updater))
                else -> block
            }
        }
    }

    private fun addNodeRecursively(currentList: List<CustomBlock>, targetId: String, newNode: CustomBlock, asSibling: Boolean): List<CustomBlock> {
        val updatedList = mutableListOf<CustomBlock>()
        for (block in currentList) {
            if (block.block_id == targetId) {
                if (asSibling) { updatedList.add(block); updatedList.add(newNode) }
                else {
                    val updatedBlock = when (block) {
                        is BaseInputBlock -> block.copy(childBlocks = block.childBlocks + newNode)
                        is StageBlock -> block.copy(childBlocks = block.childBlocks + newNode)
                        is ConditionGate -> block.copy(childBlocks = block.childBlocks + newNode)
                        is MemberBlock -> block.copy(childBlocks = block.childBlocks + newNode)
                        else -> block
                    }
                    updatedList.add(updatedBlock)
                }
            } else {
                val updatedBlock = when (block) {
                    is BaseInputBlock -> block.copy(childBlocks = addNodeRecursively(block.childBlocks, targetId, newNode, asSibling))
                    is StageBlock -> block.copy(childBlocks = addNodeRecursively(block.childBlocks, targetId, newNode, asSibling))
                    is ConditionGate -> block.copy(childBlocks = addNodeRecursively(block.childBlocks, targetId, newNode, asSibling))
                    is MemberBlock -> block.copy(childBlocks = addNodeRecursively(block.childBlocks, targetId, newNode, asSibling))
                    is PartnerBlock -> block.copy(siblingBlocks = addNodeRecursively(block.siblingBlocks, targetId, newNode, asSibling))
                    else -> block
                }
                updatedList.add(updatedBlock)
            }
        }
        return updatedList
    }

    private fun deleteNodeRecursively(currentList: List<CustomBlock>, targetId: String): List<CustomBlock> {
        val updatedList = mutableListOf<CustomBlock>()
        for (block in currentList) {
            if (block.block_id == targetId) continue
            val updatedBlock = when (block) {
                is BaseInputBlock -> block.copy(childBlocks = deleteNodeRecursively(block.childBlocks, targetId))
                is StageBlock -> block.copy(childBlocks = deleteNodeRecursively(block.childBlocks, targetId))
                is ConditionGate -> block.copy(childBlocks = deleteNodeRecursively(block.childBlocks, targetId))
                is MemberBlock -> block.copy(childBlocks = deleteNodeRecursively(block.childBlocks, targetId))
                is PartnerBlock -> block.copy(siblingBlocks = deleteNodeRecursively(block.siblingBlocks, targetId))
                else -> block
            }
            updatedList.add(updatedBlock)
        }
        return updatedList
    }

    private fun List<BuilderPersonNode>.updatePersonNode(path: List<String>, transform: (BuilderPersonNode) -> BuilderPersonNode): List<BuilderPersonNode> {
        if (path.isEmpty()) return this
        val targetId = path.first()
        return this.map { node -> if (node.id == targetId) { if (path.size == 1) transform(node) else node.copy(subNodes = node.subNodes.updatePersonNode(path.drop(1), transform)) } else node }
    }

    fun updateHeadcountNode(blockId: String, path: List<String>, transform: (BuilderPersonNode) -> BuilderPersonNode) {
        updateBlock(blockId) { block ->
            when (block) {
                is MemberBlock -> block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path, transform))
                is PartnerBlock -> block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path, transform))
                else -> block
            }
        }
    }

    fun addHeadcountNode(blockId: String, path: List<String>) {
        val newNode = BuilderPersonNode(id = generateId())
        updateBlock(blockId) { block ->
            when (block) {
                is MemberBlock -> {
                    if (path.isEmpty()) block.copy(headcountNodes = block.headcountNodes + newNode)
                    else block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path) { it.copy(subNodes = it.subNodes + newNode) })
                }
                is PartnerBlock -> {
                    if (path.isEmpty()) block.copy(headcountNodes = block.headcountNodes + newNode)
                    else block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path) { it.copy(subNodes = it.subNodes + newNode) })
                }
                else -> block
            }
        }
    }

    fun removeHeadcountNode(blockId: String, path: List<String>, idToRemove: String) {
        updateBlock(blockId) { block ->
            when (block) {
                is MemberBlock -> {
                    if (path.isEmpty()) block.copy(headcountNodes = block.headcountNodes.filter { it.id != idToRemove })
                    else block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path) { it.copy(subNodes = it.subNodes.filter { c -> c.id != idToRemove }) })
                }
                is PartnerBlock -> {
                    if (path.isEmpty()) block.copy(headcountNodes = block.headcountNodes.filter { it.id != idToRemove })
                    else block.copy(headcountNodes = block.headcountNodes.updatePersonNode(path) { it.copy(subNodes = it.subNodes.filter { c -> c.id != idToRemove }) })
                }
                else -> block
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentState = _state.value
        if (currentState.profileName.isBlank()) {
            onError("لطفاً نام الگو را در تنظیمات اولیه وارد کنید.")
            return
        }
        if (currentState.rootBlocks.isEmpty()) {
            onError("بوم خالی است! حداقل یک بلوک باید ایجاد کنید.")
            return
        }

        // اگر در حال ویرایش هستیم همان آیدی قبلی حفظ شود، در غیر این صورت جدید ساخته شود
        val profileId = currentState.editingProfileId ?: "prof_${kotlin.random.Random.nextLong(100000, 999999)}"

        val customProfile = CustomProfile(
            id = profileId,
            name = currentState.profileName,
            description = currentState.profileDescription,
            integrationType = currentState.integrationType,
            nimehkariMacroEnabled = currentState.nimehkariMacroEnabled,
            rootBlocks = currentState.rootBlocks,
            createdAt = kotlin.js.Date().getTime().toLong()
        )

        try {
            ir.ghiyas.alimaa.data.CustomProfileRepository.saveProfile(customProfile)
            onSuccess()
        } catch (e: Exception) {
            onError("خطا در ذخیره‌سازی: ${e.message}")
        }
    }

    fun createBaseInput(): BaseInputBlock = BaseInputBlock(generateId(), generateAlias())
    fun createStage(): StageBlock = StageBlock(generateId(), generateAlias(), name = "")
    fun createCondition(): ConditionGate = ConditionGate(generateId(), generateAlias(), title = "")
    fun createFormula(): FormulaBlock = FormulaBlock(generateId(), generateAlias(), outputName = "", rawFormula = "")
    fun createMember(): MemberBlock = MemberBlock(generateId(), generateAlias(), title = "", distributionType = DistributionType.HEADCOUNT_BASED)
    fun createPartner(): PartnerBlock = PartnerBlock(generateId(), generateAlias(), title = "", distributionType = DistributionType.HEADCOUNT_BASED)
    fun createUIElement(): UIElementBlock = UIElementBlock(generateId(), generateAlias(), elementType = UIElementType.TEXT_FIELD)
}
