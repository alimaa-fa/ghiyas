package ir.ghiyas.alimaa.data

import ir.ghiyas.alimaa.domain.models.CalendarType
import ir.ghiyas.alimaa.domain.models.QuoteItem
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import ir.ghiyas.alimaa.domain.models.WorkTurn
import kotlinx.browser.window
import kotlin.js.json

object WorkCalendarRepository {
    private const val STORAGE_KEY = "ghiyas_work_calendars"

    fun saveProfile(profile: WorkCalendarProfile) {
        val profiles = getAllProfiles().toMutableList()
        if (profile.isDefault) {
            for (i in profiles.indices) profiles[i] = profiles[i].copy(isDefault = false)
        }
        val existingIndex = profiles.indexOfFirst { it.id == profile.id }
        if (existingIndex >= 0) {
            profiles[existingIndex] = profile
        } else {
            val finalProfile = if (profiles.isEmpty()) profile.copy(isDefault = true) else profile
            profiles.add(finalProfile)
        }
        saveAll(profiles)
    }

    fun deleteProfile(id: String) {
        val profiles = getAllProfiles().filter { it.id != id }.toMutableList()
        if (profiles.isNotEmpty() && profiles.none { it.isDefault }) {
            profiles[0] = profiles[0].copy(isDefault = true)
        }
        saveAll(profiles)
    }

    fun getAllProfiles(): List<WorkCalendarProfile> {
        val jsonString = window.localStorage.getItem(STORAGE_KEY)
        if (jsonString.isNullOrEmpty()) return emptyList()
        return try {
            val jsArray = js("JSON.parse(jsonString)") as Array<dynamic>
            jsArray.map { jsToProfile(it) }
        } catch (e: Exception) {
            console.error("Error parsing work calendars", e)
            emptyList()
        }
    }

    fun saveAll(profiles: List<WorkCalendarProfile>) {
        val jsArray = profiles.map { profileToJs(it) }.toTypedArray()
        window.localStorage.setItem(STORAGE_KEY, js("JSON.stringify(jsArray)") as String)
    }

    private fun profileToJs(profile: WorkCalendarProfile): dynamic {
        val scheduleArray = profile.schedule.map { 
            json("turnId" to it.turnId, "cycle" to it.cycle, "owner" to it.owner, "notes" to it.notes)
        }.toTypedArray()

        val quotesArray = profile.quotes.map {
            json("text" to it.text, "translation" to it.translation, "source" to it.source)
        }.toTypedArray()

        return json(
            "id" to profile.id,
            "name" to profile.name,
            "isDefault" to profile.isDefault,
            "type" to profile.type.name,
            "startYear" to profile.startYear,
            "startMonth" to profile.startMonth,
            "startDay" to profile.startDay,
            "turnTime" to profile.turnTime,
            "shiftBeforeTemplate" to profile.shiftBeforeTemplate,
            "shiftAfterTemplate" to profile.shiftAfterTemplate,
            "schedule" to scheduleArray,
            "quotes" to quotesArray
        )
    }

    private fun jsToProfile(jsObj: dynamic): WorkCalendarProfile {
        val scheduleJs: Array<dynamic> = if (jsObj.schedule != undefined) jsObj.schedule else emptyArray()
        val parsedSchedule = scheduleJs.map { 
            WorkTurn(
                turnId = (it.turnId as Number).toInt(),
                cycle = (it.cycle as Number).toInt(),
                owner = it.owner as String,
                notes = if (it.notes != undefined) it.notes as String else ""
            )
        }

        val quotesJs: Array<dynamic> = if (jsObj.quotes != undefined) jsObj.quotes else emptyArray()
        val parsedQuotes = quotesJs.map {
            QuoteItem(
                text = if (it.text != undefined) it.text as String else "",
                translation = if (it.translation != undefined) it.translation as String else "",
                source = if (it.source != undefined) it.source as String else ""
            )
        }

        val typeStr = if (jsObj.type != undefined) jsObj.type as String else CalendarType.DAY_BASED.name
        val cType = try { CalendarType.valueOf(typeStr) } catch (e: Exception) { CalendarType.DAY_BASED }

        return WorkCalendarProfile(
            id = jsObj.id as String,
            name = jsObj.name as String,
            isDefault = jsObj.isDefault as Boolean? ?: false,
            type = cType,
            startYear = (jsObj.startYear as Number).toInt(),
            startMonth = (jsObj.startMonth as Number).toInt(),
            startDay = (jsObj.startDay as Number).toInt(),
            turnTime = jsObj.turnTime as String,
            shiftBeforeTemplate = if (jsObj.shiftBeforeTemplate != undefined) jsObj.shiftBeforeTemplate as String else "تا ساعت {time}",
            shiftAfterTemplate = if (jsObj.shiftAfterTemplate != undefined) jsObj.shiftAfterTemplate as String else "از ساعت {time}",
            schedule = parsedSchedule,
            quotes = parsedQuotes
        )
    }
}
