package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class ComprehensiveMode(val displayName: String) {
    PERSON("بر اساس نفر"),
    SHARE_QYAS("بر اساس سهم (قیاس)"),
    PERCENTAGE("بر اساس درصد")
}

@Serializable
data class ShareholderNode(
    val id: String,
    val name: String = "",
    val isFemale: Boolean = false,
    val rawValue: String = "",
    val transferredToId: String = "",
    val hasSubDistribution: Boolean = false,
    val subDistributionMode: ComprehensiveMode = ComprehensiveMode.PERSON,
    val children: List<ShareholderNode> = emptyList(),
    
    // مقادیر اضافه‌شده برای رفع باگ ۳
    val canBeExcluded: Boolean = false, // فاز ویرایش: آیا این شریک شرطی است؟
    val isExcluded: Boolean = false,    // فاز اجرا: آیا کاربر تیک "حساب نشود؟" را زده است؟
    
    // اضافه‌شده برای رفع باگ انتقال سهم در زمان اجرا
    val canBeTransferred: Boolean = false
)

@Serializable
data class SavedDistributionTemplate(
    val id: String,
    val title: String,
    val rootMode: ComprehensiveMode = ComprehensiveMode.PERSON,
    val totalCountLimit: String = "",
    val nodes: List<ShareholderNode> = emptyList(),
    val createdAt: Long = 0L
)
