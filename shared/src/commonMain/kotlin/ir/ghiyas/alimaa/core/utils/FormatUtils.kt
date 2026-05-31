package ir.ghiyas.alimaa.core.utils

import kotlin.math.round

fun Double.toGhiyasFormat(isKg: Boolean): String {
    val decimals = if (isKg) 3 else 1
    val factor = if (isKg) 1000.0 else 10.0
    val rounded = round(this * factor) / factor
    
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')
    
    val resultStr = if (dotIndex == -1) {
        str + "." + "0".repeat(decimals)
    } else {
        val intPart = str.substring(0, dotIndex)
        val decPart = str.substring(dotIndex + 1)
        val paddedDec = decPart.padEnd(decimals, '0').take(decimals)
        "$intPart.$paddedDec"
    }

    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val sb = StringBuilder(resultStr.length)
    for (char in resultStr) {
        if (char in '0'..'9') {
            sb.append(persianDigits[char - '0'])
        } else {
            sb.append(char)
        }
    }
    return sb.toString()
}

expect fun formatTimestampToPersianDateTime(timestamp: Long): String
