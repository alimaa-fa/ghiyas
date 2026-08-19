package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("blockType")
sealed class CustomBlock {
    abstract val block_id: String
    abstract val system_alias: String
    abstract val isInteractive: Boolean
}

@Serializable
@SerialName("BASE_INPUT")
data class BaseInputBlock(override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = true, val nameLabel: String = "نام محاسبه", val amountLabel: String = "مقدار کل محصول", val childBlocks: List<CustomBlock> = emptyList()) : CustomBlock()

@Serializable
@SerialName("STAGE")
data class StageBlock(override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = false, val name: String, val description: String = "", val isRequired: Boolean = false, val accordionGuide: String = "", val childBlocks: List<CustomBlock> = emptyList()) : CustomBlock()

@Serializable
@SerialName("CONDITION")
data class ConditionGate(override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = true, val title: String, val isCalculateEnabled: Boolean = true, val isVisibleEnabled: Boolean = true, val childBlocks: List<CustomBlock> = emptyList()) : CustomBlock()

enum class DistributionType { HEADCOUNT_BASED, GHIYAS_BASED, PERCENTAGE, CUSTOM_UNIT }
enum class UIElementType { TEXT_FIELD, NUMBER_FIELD, HEADER_TITLE, SEPARATOR_LINE, ACCORDION_GUIDE, TEXT_WARNING }

@Serializable
data class BuilderPersonNode(
    val id: String, val name: String = "", val weightInput: String = "1",
    val isFemale: Boolean = false, val isSubDivided: Boolean = false,
    val subCountInput: String = "", val isDetailedFurther: Boolean = false,
    val isSubBoyGirlSplit: Boolean = false,
    val hasToggle: Boolean = false, val toggleLabel: String = "لحاظ شود؟", // اضافه شده
    val subNodes: List<BuilderPersonNode> = emptyList()
)

@Serializable
data class BuilderShareholder(
    val id: String, val name: String = "", val shareInput: String = "",
    val hasToggle: Boolean = false, val toggleLabel: String = "لحاظ شود؟" // اضافه شده
)

@Serializable
@SerialName("MEMBER")
data class MemberBlock(
    override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = true,
    val title: String, val distributionType: DistributionType, 
    val customUnitDecimals: Int = 0, val isRoundingEnabled: Boolean = false,
    val totalHeadcountInput: String = "", val isDetailedHeadcount: Boolean = false, val isBoyGirlSplit: Boolean = false,
    val headcountNodes: List<BuilderPersonNode> = emptyList(),
    val ghiyasShareholders: List<BuilderShareholder> = emptyList(),
    val percentageShareholders: List<BuilderShareholder> = emptyList(),
    val childBlocks: List<CustomBlock> = emptyList()
) : CustomBlock()

@Serializable
@SerialName("PARTNER")
data class PartnerBlock(
    override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = true,
    val title: String, val distributionType: DistributionType, 
    val customUnitDecimals: Int = 0, val isRoundingEnabled: Boolean = false,
    val totalHeadcountInput: String = "", val isDetailedHeadcount: Boolean = false, val isBoyGirlSplit: Boolean = false,
    val headcountNodes: List<BuilderPersonNode> = emptyList(),
    val ghiyasShareholders: List<BuilderShareholder> = emptyList(),
    val percentageShareholders: List<BuilderShareholder> = emptyList(),
    val siblingBlocks: List<CustomBlock> = emptyList()
) : CustomBlock()

@Serializable
@SerialName("FORMULA")
data class FormulaBlock(override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = false, val outputName: String, val rawFormula: String, val attachedConditionId: String = "NONE") : CustomBlock()

@Serializable
@SerialName("UI_ELEMENT")
data class UIElementBlock(override val block_id: String, override val system_alias: String, override val isInteractive: Boolean = true, val elementType: UIElementType, val elementTitle: String = "", val elementContent: String = "", val isRequired: Boolean = false) : CustomBlock()
