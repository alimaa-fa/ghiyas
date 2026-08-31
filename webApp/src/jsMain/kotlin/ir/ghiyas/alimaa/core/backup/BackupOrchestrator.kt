package ir.ghiyas.alimaa.core.backup

import ir.ghiyas.alimaa.core.utils.WebFileIO
import ir.ghiyas.alimaa.data.CustomProfileRepository
import ir.ghiyas.alimaa.data.DistributionTemplateRepository
import ir.ghiyas.alimaa.data.LocalStorageRepository
import ir.ghiyas.alimaa.data.WorkCalendarRepository
import ir.ghiyas.alimaa.domain.backup.BackupEngine
import ir.ghiyas.alimaa.domain.backup.BackupPayload
import kotlin.js.Date

object BackupOrchestrator {

    fun exportBackup() {
        val payload = BackupPayload(
            version = 1,
            history = LocalStorageRepository.getAllRecords(),
            calendars = WorkCalendarRepository.getAllProfiles(),
            distributionTemplates = DistributionTemplateRepository.getAllTemplates(),
            customProfiles = CustomProfileRepository.getAllProfiles()
        )

        val jsonString = BackupEngine.generateBackupJson(payload)
        
        val date = Date()
        val dateString = "${date.getFullYear()}${(date.getMonth() + 1).toString().padStart(2, '0')}${date.getDate().toString().padStart(2, '0')}"
        val fileName = "qiyas_full_backup_$dateString.json"

        WebFileIO.downloadJsonFile(fileName, jsonString)
    }

    fun importBackup(onStartProcessing: () -> Unit, onComplete: (Boolean, String) -> Unit) {
        WebFileIO.importJsonFile(
            onFileSelected = { onStartProcessing() },
            onResult = { jsonString ->
                if (jsonString == "CANCELED") {
                    onComplete(true, "") // کنسل شدن خطا نیست، فقط پیام را پاک می‌کنیم
                    return@importJsonFile
                }
                
                if (jsonString == null) {
                    onComplete(false, "خطا در خواندن فایل پشتیبان.")
                    return@importJsonFile
                }

                val payload = BackupEngine.parseBackupJson(jsonString)
                if (payload == null) {
                    onComplete(false, "فایل پشتیبان نامعتبر است یا خراب شده است.")
                    return@importJsonFile
                }

                try {
                    val currentHistory = LocalStorageRepository.getAllRecords()
                    val currentCalendars = WorkCalendarRepository.getAllProfiles()
                    val currentTemplates = DistributionTemplateRepository.getAllTemplates()
                    val currentProfiles = CustomProfileRepository.getAllProfiles()

                    val mergedHistory = BackupEngine.mergeHistory(currentHistory, payload.history)
                    val mergedCalendars = BackupEngine.mergeCalendars(currentCalendars, payload.calendars)
                    val mergedTemplates = BackupEngine.mergeTemplates(currentTemplates, payload.distributionTemplates)
                    val mergedProfiles = BackupEngine.mergeCustomProfiles(currentProfiles, payload.customProfiles)

                    LocalStorageRepository.saveAll(mergedHistory)
                    WorkCalendarRepository.saveAll(mergedCalendars)
                    DistributionTemplateRepository.saveAll(mergedTemplates)
                    CustomProfileRepository.saveAll(mergedProfiles)

                    onComplete(true, "اطلاعات با موفقیت بازیابی و به صورت هوشمند ادغام شد.")
                } catch (e: Exception) {
                    console.error("خطا در بازیابی اطلاعات:", e)
                    onComplete(false, "خطای سیستمی در هنگام بازیابی اطلاعات.")
                }
            }
        )
    }
}
