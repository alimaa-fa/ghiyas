package ir.ghiyas.alimaa.data

import ir.ghiyas.alimaa.domain.models.CustomProfile
import kotlinx.browser.window
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object CustomProfileRepository {
    private const val KEY = "ghiyas_custom_profiles"
    private val json = Json { 
        ignoreUnknownKeys = true 
        classDiscriminator = "blockType" // حیاتی برای سریالایز کردن کلاس‌های Sealed (بلوک‌ها)
    }

    fun getAllProfiles(): List<CustomProfile> {
        val data = window.localStorage.getItem(KEY) ?: return emptyList()
        return try {
            json.decodeFromString(data)
        } catch (e: Exception) {
            println("Error parsing custom profiles: ${e.message}")
            emptyList()
        }
    }

    fun saveProfile(profile: CustomProfile) {
        val currentProfiles = getAllProfiles().toMutableList()
        val existingIndex = currentProfiles.indexOfFirst { it.id == profile.id }
        if (existingIndex != -1) {
            currentProfiles[existingIndex] = profile
        } else {
            currentProfiles.add(profile)
        }
        window.localStorage.setItem(KEY, json.encodeToString(currentProfiles))
    }

    fun deleteProfile(id: String) {
        val currentProfiles = getAllProfiles().filter { it.id != id }
        window.localStorage.setItem(KEY, json.encodeToString(currentProfiles))
    }
}
