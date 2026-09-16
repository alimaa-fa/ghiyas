package ghiyas.alimaa.fa.presentation.builder

import ghiyas.alimaa.fa.domain.models.CustomBlock
import ghiyas.alimaa.fa.domain.models.ProfileIntegrationType

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
