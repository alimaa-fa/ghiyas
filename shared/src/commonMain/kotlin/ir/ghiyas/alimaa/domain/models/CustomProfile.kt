package ir.ghiyas.alimaa.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class CustomProfile(
    val id: String,
    val name: String,
    val description: String,
    val integrationType: ProfileIntegrationType,
    val nimehkariMacroEnabled: Boolean,
    val rootBlocks: List<CustomBlock>,
    val createdAt: Long
)
