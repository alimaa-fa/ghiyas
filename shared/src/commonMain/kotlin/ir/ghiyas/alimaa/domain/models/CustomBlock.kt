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
    abstract val system_alias: String // متغیر پنهان سیستمی برای فرمول‌نویسی (مثل: var_12345)
    abstract val isInteractive: Boolean // آیا در زمان اجرا کاربر باید با آن تعامل کند؟
}

@Serializable
@SerialName("STAGE")
data class StageBlock(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean = false,
    val name: String,
    val description: String = "",
    val isRequired: Boolean = false, // اجباری یا اختیاری بودن عبور از مرحله
    val childBlocks: List<CustomBlock> = emptyList()
) : CustomBlock()

enum class LogicType { CALCULATE_ONLY, VISIBILITY_ONLY }

@Serializable
@SerialName("CONDITION")
data class ConditionGate(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean = true,
    val title: String,
    val logicType: LogicType,
    val isCheckedByDefault: Boolean = false,
    val childBlocks: List<CustomBlock> = emptyList()
) : CustomBlock()

enum class DistributionType { SHARE_BASED, COUNT_BASED, PERCENTAGE, CUSTOM_UNIT }

@Serializable
@SerialName("MEMBER")
data class MemberBlock(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean = true,
    val title: String,
    val distributionType: DistributionType,
    val customUnitDecimals: Int? = null,
    val childBlocks: List<CustomBlock> = emptyList() // وراث فقط توسعه طولی می‌گیرند
) : CustomBlock()

@Serializable
@SerialName("PARTNER")
data class PartnerBlock(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean = true,
    val title: String,
    val distributionType: DistributionType,
    val customUnitDecimals: Int? = null,
    val siblingBlocks: List<CustomBlock> = emptyList() // شرکا توسعه عرضی (هم‌رده) می‌گیرند
) : CustomBlock()

@Serializable
@SerialName("FORMULA")
data class FormulaBlock(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean = false, // خود فرمول پنهان است، نتیجه‌اش نمایان می‌شود
    val outputName: String,
    val rawFormula: String,
    val attachedConditionId: String? = null // اتصال به یک دروازه شرطی
) : CustomBlock()

enum class UIElementType { HEADER, ACCORDION_GUIDE, TEXT_INPUT, NUMBER_INPUT, WARNING_ALERT }

@Serializable
@SerialName("UI_ELEMENT")
data class UIElementBlock(
    override val block_id: String,
    override val system_alias: String,
    override val isInteractive: Boolean,
    val elementType: UIElementType,
    val labelOrContent: String,
    val isRequired: Boolean = false // کاربر در هنگام ساخت می‌تواند فیلد را اجباری یا اختیاری کند
) : CustomBlock()
