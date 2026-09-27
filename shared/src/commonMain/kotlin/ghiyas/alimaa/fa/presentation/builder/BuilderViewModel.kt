package ghiyas.alimaa.fa.presentation.builder

import ghiyas.alimaa.fa.domain.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BuilderViewModel {
    private val _state = MutableStateFlow(BuilderState())
    val state: StateFlow<BuilderState> = _state.asStateFlow()

    fun clearForNewProfile() { _state.update { BuilderState() } }
    
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

    // =========================================================================
    // توابع جادویی و بازگشتی برای پیدا کردن و تغییر نُدها بر اساس ID (بدون Path)
    // =========================================================================
    
    private fun updateBlockLists(blockId: String, personTransformer: (List<BuilderPersonNode>) -> List<BuilderPersonNode>, shareholderTransformer: (List<BuilderShareholder>) -> List<BuilderShareholder>) {
        updateBlock(blockId) { block ->
            when (block) {
                is MemberBlock -> block.copy(headcountNodes = personTransformer(block.headcountNodes), ghiyasShareholders = shareholderTransformer(block.ghiyasShareholders), percentageShareholders = shareholderTransformer(block.percentageShareholders))
                is PartnerBlock -> block.copy(headcountNodes = personTransformer(block.headcountNodes), ghiyasShareholders = shareholderTransformer(block.ghiyasShareholders), percentageShareholders = shareholderTransformer(block.percentageShareholders))
                else -> block
            }
        }
    }

    // 1. منطق بازگشتی آپدیت Person
    private fun List<BuilderPersonNode>.replacePerson(nodeId: String, updater: (BuilderPersonNode) -> BuilderPersonNode): List<BuilderPersonNode> = map { node ->
        if (node.id == nodeId) updater(node) else node.copy(subNodes = node.subNodes.replacePerson(nodeId, updater), subShareholders = node.subShareholders.replaceShareholderForPersonUpdate(nodeId, updater))
    }
    private fun List<BuilderShareholder>.replaceShareholderForPersonUpdate(nodeId: String, updater: (BuilderPersonNode) -> BuilderPersonNode): List<BuilderShareholder> = map { sh ->
        sh.copy(subHeadcounts = sh.subHeadcounts.replacePerson(nodeId, updater), subNodes = sh.subNodes.replaceShareholderForPersonUpdate(nodeId, updater))
    }

    // 2. منطق بازگشتی آپدیت Shareholder
    private fun List<BuilderShareholder>.replaceShareholder(nodeId: String, updater: (BuilderShareholder) -> BuilderShareholder): List<BuilderShareholder> = map { sh ->
        if (sh.id == nodeId) updater(sh) else sh.copy(subHeadcounts = sh.subHeadcounts.replacePersonForShareholderUpdate(nodeId, updater), subNodes = sh.subNodes.replaceShareholder(nodeId, updater))
    }
    private fun List<BuilderPersonNode>.replacePersonForShareholderUpdate(nodeId: String, updater: (BuilderShareholder) -> BuilderShareholder): List<BuilderPersonNode> = map { node ->
        node.copy(subNodes = node.subNodes.replacePersonForShareholderUpdate(nodeId, updater), subShareholders = node.subShareholders.replaceShareholder(nodeId, updater))
    }

    // 3. منطق بازگشتی حذف کلی (Delete)
    private fun List<BuilderPersonNode>.removeNodeFromPersonList(nodeId: String): List<BuilderPersonNode> = filter { it.id != nodeId }.map { node ->
        node.copy(subNodes = node.subNodes.removeNodeFromPersonList(nodeId), subShareholders = node.subShareholders.removeNodeFromShareholderList(nodeId))
    }
    private fun List<BuilderShareholder>.removeNodeFromShareholderList(nodeId: String): List<BuilderShareholder> = filter { it.id != nodeId }.map { sh ->
        sh.copy(subHeadcounts = sh.subHeadcounts.removeNodeFromPersonList(nodeId), subNodes = sh.subNodes.removeNodeFromShareholderList(nodeId))
    }

    // 4. منطق بازگشتی افزودن به والد (Add to Parent)
    private fun List<BuilderPersonNode>.addNodeToPersonParent(parentId: String, newNode: Any): List<BuilderPersonNode> = map { node ->
        if (node.id == parentId) {
            when (newNode) {
                is BuilderPersonNode -> node.copy(subNodes = node.subNodes + newNode)
                is BuilderShareholder -> node.copy(subShareholders = node.subShareholders + newNode)
                else -> node
            }
        } else node.copy(subNodes = node.subNodes.addNodeToPersonParent(parentId, newNode), subShareholders = node.subShareholders.addNodeToShareholderParent(parentId, newNode))
    }
    private fun List<BuilderShareholder>.addNodeToShareholderParent(parentId: String, newNode: Any): List<BuilderShareholder> = map { sh ->
        if (sh.id == parentId) {
            when (newNode) {
                is BuilderPersonNode -> sh.copy(subHeadcounts = sh.subHeadcounts + newNode)
                is BuilderShareholder -> sh.copy(subNodes = sh.subNodes + newNode)
                else -> sh
            }
        } else sh.copy(subHeadcounts = sh.subHeadcounts.addNodeToPersonParent(parentId, newNode), subNodes = sh.subNodes.addNodeToShareholderParent(parentId, newNode))
    }

    // --- توابع عمومی (Public) برای استفاده در رابط کاربری ---

    fun updatePersonNode(blockId: String, nodeId: String, updater: (BuilderPersonNode) -> BuilderPersonNode) {
        updateBlockLists(blockId, { it.replacePerson(nodeId, updater) }, { it.replaceShareholderForPersonUpdate(nodeId, updater) })
    }

    fun updateShareholderNode(blockId: String, nodeId: String, updater: (BuilderShareholder) -> BuilderShareholder) {
        updateBlockLists(blockId, { it.replacePersonForShareholderUpdate(nodeId, updater) }, { it.replaceShareholder(nodeId, updater) })
    }

    fun removeNode(blockId: String, nodeId: String) {
        updateBlockLists(blockId, { it.removeNodeFromPersonList(nodeId) }, { it.removeNodeFromShareholderList(nodeId) })
    }

    fun addPersonNode(blockId: String, parentId: String?) {
        val newNode = BuilderPersonNode(id = generateId())
        if (parentId == null) {
            updateBlockLists(blockId, { it + newNode }, { it }) // اضافه به ریشه نفرات بلوک
        } else {
            updateBlockLists(blockId, { it.addNodeToPersonParent(parentId, newNode) }, { it.addNodeToShareholderParent(parentId, newNode) })
        }
    }

    fun addShareholderNode(blockId: String, parentId: String?, isPercentageRoot: Boolean = false) {
        val newNode = BuilderShareholder(id = generateId())
        if (parentId == null) {
            updateBlock(blockId) { block ->
                when (block) {
                    is MemberBlock -> if (isPercentageRoot) block.copy(percentageShareholders = block.percentageShareholders + newNode) else block.copy(ghiyasShareholders = block.ghiyasShareholders + newNode)
                    is PartnerBlock -> if (isPercentageRoot) block.copy(percentageShareholders = block.percentageShareholders + newNode) else block.copy(ghiyasShareholders = block.ghiyasShareholders + newNode)
                    else -> block
                }
            }
        } else {
            updateBlockLists(blockId, { it.addNodeToPersonParent(parentId, newNode) }, { it.addNodeToShareholderParent(parentId, newNode) })
        }
    }
    // =========================================================================

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
            ghiyas.alimaa.fa.data.CustomProfileRepository.saveProfile(customProfile)
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
