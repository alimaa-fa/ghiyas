package ir.ghiyas.alimaa.presentation.builder

import ir.ghiyas.alimaa.domain.models.CustomBlock
import ir.ghiyas.alimaa.domain.models.ProfileIntegrationType

data class BuilderState(
    val profileName: String = "",
    val profileDescription: String = "",
    val integrationType: ProfileIntegrationType = ProfileIntegrationType.STANDALONE_MAIN_TAB,
    val nimehkariMacroEnabled: Boolean = false,
    val rootBlocks: List<CustomBlock> = emptyList(),
    
    // وضعیت‌های تعاملی رابط کاربری
    val activeBlockId: String? = null, // کدام بلوک در حال ویرایش است
    val isSaving: Boolean = false
)
