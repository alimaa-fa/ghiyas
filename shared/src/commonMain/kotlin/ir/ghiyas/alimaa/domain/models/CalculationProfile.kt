package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

enum class ProfileIntegrationType { 
    DEPENDENT_STEP_4, // اجرا به عنوان موتور تسهیم در مرحله ۴
    STANDALONE_MAIN_TAB // اجرا به صورت صفر-تا-صد در تب اختصاصی
}

@Serializable
data class CalculationProfile(
    val profile_id: String,
    val name: String,
    val description: String,
    val integrationType: ProfileIntegrationType,
    val nimehkariMacroEnabled: Boolean = false,
    val rootBlocks: List<CustomBlock> = emptyList(), // ریشه‌های اصلی درخت
    val createdAt: Long,
    val updatedAt: Long
)
