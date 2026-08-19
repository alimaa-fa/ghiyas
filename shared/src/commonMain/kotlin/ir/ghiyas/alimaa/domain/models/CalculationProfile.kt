package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

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
