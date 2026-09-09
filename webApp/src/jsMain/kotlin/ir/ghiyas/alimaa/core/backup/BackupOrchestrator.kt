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

    private fun generatePayloadAndFilename(): Pair<String, String> {
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
        val fileName = "qiyas_backup_$dateString.json"
        return Pair(jsonString, fileName)
    }

    // فراخوانی پل بومی اندروید
    fun exportBackupAndroidNative() {
        val (jsonString, fileName) = generatePayloadAndFilename()
        WebFileIO.exportViaAndroidNative(fileName, jsonString)
    }

    fun exportBackupDirect() {
        val (jsonString, fileName) = generatePayloadAndFilename()
        WebFileIO.exportViaDirectDownload(fileName, jsonString)
    }

    fun exportBackupShare(onFallbackRequested: () -> Unit) {
        val (jsonString, fileName) = generatePayloadAndFilename()
        WebFileIO.exportViaWebShare(fileName, jsonString, onFallbackRequested)
    }

    fun getBackupRawString(): String {
        return generatePayloadAndFilename().first
    }

    fun importBackupFromFile(onStartProcessing: () -> Unit, onComplete: (Boolean, String) -> Unit) {
        WebFileIO.importJsonFile(
            onFileSelected = { onStartProcessing() },
            onResult = { jsonString ->
                if (jsonString == "CANCELED") {
                    onComplete(true, "") 
                    return@importJsonFile
                }
                if (jsonString == null) {
                    onComplete(false, "خطا در خواندن فایل پشتیبان.")
                    return@importJsonFile
                }
                processAndMergePayload(jsonString, onComplete)
            }
        )
    }

    fun importBackupFromUrl(url: String, onStartProcessing: () -> Unit, onComplete: (Boolean, String) -> Unit) {
        onStartProcessing()
        WebFileIO.importFromUrl(url) { jsonString, errorMsg ->
            if (errorMsg != null) {
                onComplete(false, errorMsg)
            } else if (jsonString != null) {
                processAndMergePayload(jsonString, onComplete)
            }
        }
    }

    fun importBackupFromRawText(rawText: String, onComplete: (Boolean, String) -> Unit) {
        val cleanText = rawText.replace("\n", "").replace("\r", "").trim()
        if (!cleanText.startsWith("{")) {
            onComplete(false, "متن وارد شده معتبر نیست. لطفاً دقت کنید که تمام بخش‌های فایل پشتیبان را به درستی کپی کرده باشید.")
            return
        }
        processAndMergePayload(cleanText, onComplete)
    }

    private fun processAndMergePayload(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        val payload = BackupEngine.parseBackupJson(jsonString)
        if (payload == null) {
            onComplete(false, "فایل پشتیبان نامعتبر است یا ساختار آن در هنگام کپی شدن ناقص شده است.")
            return
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

            onComplete(true, "اطلاعات با موفقیت استخراج و به صورت هوشمند ادغام شد.")
        } catch (e: Exception) {
            console.error("خطا در بازیابی اطلاعات:", e)
            onComplete(false, "خطای سیستمی در هنگام بازیابی و ادغام اطلاعات رخ داد.")
        }
    }
}
