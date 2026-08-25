package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

/**
 * متدهای زیرمجموعه برای تسهیم جامع (نفر، سهم، درصد)
 */
@Serializable
enum class ComprehensiveMode(val displayName: String) {
    PERSON("بر اساس نفر"),
    SHARE_QYAS("بر اساس سهم (قیاس)"),
    PERCENTAGE("بر اساس درصد")
}

/**
 * گره درختی هر شریک با قابلیت انتقال سهم و خرد شدن سلسله‌مراتبی
 */
@Serializable
data class ShareholderNode(
    val id: String,
    val name: String = "",
    val isActive: Boolean = true, // شرط «حساب شود؟»
    val isFemale: Boolean = false, // ضریب ۰.۵ برای حالت نفر
    val rawValue: String = "1", // مقدار ورودی (تعداد، قیاس یا درصد)
    val transferredToId: String = "", // شناسه شریک مقصد برای انتقال سهم
    val hasSubDistribution: Boolean = false, // آیا سهمش خرد می‌شود؟
    val subDistributionMode: ComprehensiveMode = ComprehensiveMode.PERSON, // روش تسهیم زیرمجموعه
    val children: List<ShareholderNode> = emptyList() // وارثین زیرمجموعه
)

/**
 * مدل ذخیره‌سازی الگوهای تسهیم برای استفاده مکرر (مثل کارت به کارت)
 */
@Serializable
data class SavedDistributionTemplate(
    val id: String,
    val title: String,
    val rootMode: ComprehensiveMode = ComprehensiveMode.PERSON,
    val totalCountLimit: String = "",
    val nodes: List<ShareholderNode> = emptyList(),
    val createdAt: Long = 0L
)
