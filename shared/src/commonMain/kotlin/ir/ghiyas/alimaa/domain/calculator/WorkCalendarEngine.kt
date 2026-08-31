package ir.ghiyas.alimaa.domain.calculator

import ir.ghiyas.alimaa.domain.models.WorkTurn
import kotlin.math.floor

object WorkCalendarEngine {

    fun calculateTurnByDaysPassed(schedule: List<WorkTurn>, daysPassed: Int, offset: Int = 0): WorkTurn? {
        if (schedule.isEmpty()) return null
        val totalTurns = schedule.size
        var index = (daysPassed + offset) % totalTurns
        if (index < 0) index += totalTurns
        return schedule[index]
    }

    // این نسخه به هیچ وجه تحت تاثیر Timezone مرورگر کاربر قرار نمی‌گیرد و همیشه وقت تهران است
    fun getTehranDateInfo(): TehranDate {
        val d = kotlin.js.Date()
        // متد getTime() در جاوااسکریپت مطلقاً UTC را برمی‌گرداند.
        // با اضافه کردن 12600000 میلی‌ثانیه (3.5 ساعت) آن را به مبدا تهران می‌بریم.
        val tehranMs = d.getTime() + 12600000.0 
        val tDate = kotlin.js.Date(tehranMs)
        
        // حالا حتماً باید مقادیر UTC را بخوانیم تا مرورگر آفست محلی خودش را دوباره اعمال نکند!
        val gy = tDate.getUTCFullYear()
        val gm = tDate.getUTCMonth() + 1
        val gd = tDate.getUTCDate()
        val hour = tDate.getUTCHours()
        val minute = tDate.getUTCMinutes()
        
        val jdn = gregorianToJdn(gy, gm, gd)
        val (jy, jm, jd) = jdnToJalali(jdn)
        
        return TehranDate(jy, jm, jd, hour, minute, jdn)
    }

    fun getEffectiveDaysPassed(baseJdn: Int, targetJdn: Int, targetHour: Int, turnHourStr: String): Int {
        val parts = turnHourStr.split(":")
        val turnHour = if (parts.isNotEmpty()) parts[0].toIntOrNull() ?: 18 else 18
        
        var effectiveTargetJdn = targetJdn
        if (targetHour < turnHour) {
            effectiveTargetJdn -= 1
        }
        
        return effectiveTargetJdn - baseJdn
    }

    fun getUpcomingTurns(schedule: List<WorkTurn>, owner: String, baseJdn: Int, currentEffectiveJdn: Int): List<Pair<Int, WorkTurn>> {
        if (owner.isBlank()) return emptyList()
        val upcoming = mutableListOf<Pair<Int, WorkTurn>>()
        val currentDaysPassed = currentEffectiveJdn - baseJdn
        
        for (i in 0..30) {
            val turn = calculateTurnByDaysPassed(schedule, currentDaysPassed, i)
            if (turn != null && turn.owner == owner) {
                upcoming.add(Pair(currentEffectiveJdn + i, turn))
            }
        }
        return upcoming
    }

    fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Int {
        var y = gy
        var m = gm
        if (m < 3) { y -= 1; m += 12 }
        return floor(365.25 * (y + 4716)).toInt() + floor(30.6001 * (m + 1)).toInt() + gd + 2 - floor(y / 100.0).toInt() + floor(y / 400.0).toInt() - 1524
    }

    fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Int {
        val epbase = jy - if (jy >= 0) 474 else 473
        val epyear = 474 + (epbase % 2820)
        val md = if (jm <= 7) (jm - 1) * 31 else (jm - 1) * 30 + 6
        // اصلاح قطعی باگ: تغییر عدد 1948319 به 1948320 برای تنظیم دقیق مبدأ تقویم جلالی
        return jd + md + floor(((epyear * 682) - 110) / 2816.0).toInt() + (epyear - 1) * 365 + floor(epbase / 2820.0).toInt() * 1029983 + 1948320
    }

    fun jdnToJalali(jdn: Int): Triple<Int, Int, Int> {
        val depoch = jdn - jalaliToJdn(475, 1, 1)
        val cycle = floor(depoch / 1029983.0).toInt()
        val cyear = depoch % 1029983
        var ycycle = 2820.0
        if (cyear == 1029982) {
            ycycle = 2820.0
        } else {
            val aux1 = floor(cyear / 366.0)
            val aux2 = cyear % 366.0
            ycycle = floor(((2134 * aux1) + (2816 * aux2) + 2815) / 1028522.0) + aux1 + 1
        }
        var jy = ycycle.toInt() + (2820 * cycle) + 474
        if (jy <= 0) jy -= 1
        val yday = (jdn - jalaliToJdn(jy, 1, 1)) + 1
        val jm = if (yday <= 186) kotlin.math.ceil(yday / 31.0).toInt() else kotlin.math.ceil((yday - 6) / 30.0).toInt()
        val jd = (jdn - jalaliToJdn(jy, jm, 1)) + 1
        return Triple(jy, jm, jd)
    }

    fun getJalaliDayOfWeek(jdn: Int): Int = (jdn + 2) % 7
    fun getJalaliDayName(jdn: Int): String = listOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")[getJalaliDayOfWeek(jdn)]
    fun getJalaliMonthName(jm: Int): String = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")[jm - 1]

    fun getJalaliMonthLength(jy: Int, jm: Int): Int {
        if (jm <= 6) return 31
        if (jm <= 11) return 30
        val isLeap = (jy % 33 == 1 || jy % 33 == 5 || jy % 33 == 9 || jy % 33 == 13 || jy % 33 == 17 || jy % 33 == 22 || jy % 33 == 26 || jy % 33 == 30)
        return if (isLeap) 30 else 29
    }
}

data class TehranDate(val jy: Int, val jm: Int, val jd: Int, val hour: Int, val minute: Int, val jdn: Int)
