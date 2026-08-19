package ir.ghiyas.alimaa.presentation.builder

import ir.ghiyas.alimaa.domain.models.CustomBlock
import ir.ghiyas.alimaa.domain.models.ProfileIntegrationType

data class BuilderState(
    // فیلدهای اصلی و ویرایش (استخراج شده از ویومدل)
    val editingProfileId: String? = null,
    val profileName: String = "",
    val profileDescription: String = "",
    val integrationType: ProfileIntegrationType = ProfileIntegrationType.DEPENDENT_STEP_4,
    val nimehkariMacroEnabled: Boolean = false,
    val rootBlocks: List<CustomBlock> = emptyList(),
    
    // وضعیت‌های تعاملی رابط کاربری (حفظ شده از نسخه مستقل)
    val activeBlockId: String? = null, // کدام بلوک در حال ویرایش است
    val isSaving: Boolean = false
)
