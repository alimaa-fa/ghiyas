package ir.ghiyas.alimaa.core.utils

fun String.toPersianDigitsLocal(): String {
    var result = this
    val english = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
    val persian = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "٫")
    for (i in english.indices) { result = result.replace(english[i], persian[i]) }
    return result
}

fun Double.toGhiyasFormat(baseUnit: String): String {
    // ۱. تشخیص دقیق تعداد رقم اعشار بر اساس نوع واحد ورودی
    val decimals = when {
        baseUnit.contains("کیلو") || baseUnit.contains("گرم") -> 3
        baseUnit.contains("متر") -> 2
        baseUnit.contains("دست") || baseUnit.contains("دانه") || baseUnit.contains("دان") -> 1
        else -> 0
    }

    // اگر واحد بدون اعشار بود (مثل تومان)، بخش اعشاری کاملاً حذف می‌شود
    if (decimals == 0) {
        return this.toLong().toString().toPersianDigitsLocal()
    }

    // ۲. پردازش بر پایه رشته برای دور زدن باگ‌های ممیز شناور سخت‌افزاری
    val rawStr = this.toString()
    val parts = rawStr.split('.')
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else ""

    // پر کردن انتهای اعشار با صفر جهت بررسی دقیق موقعیت‌ها
    val paddedDec = decPart.padEnd(decimals + 5, '0')
    
    val targetDecVal = paddedDec.substring(0, decimals)
    val checkDigit = paddedDec[decimals].toString().toIntOrNull() ?: 0

    var finalInt = intPart.toLongOrNull() ?: 0L
    var finalDecStr = targetDecVal

    // قانون بومی قیاس: تنها در صورتی که رقم بعدی ۸ یا ۹ باشد رو به بالا گرد کن، در غیر این صورت قیچی کن
    if (checkDigit == 8 || checkDigit == 9) {
        val decNum = targetDecVal.toLongOrNull() ?: 0L
        val maxDecNum = ("1" + "0".repeat(decimals)).toLong()
        val newDecNum = decNum + 1
        
        if (newDecNum >= maxDecNum) {
            finalInt += 1
            finalDecStr = "0".repeat(decimals)
        } else {
            finalDecStr = newDecNum.toString().padStart(decimals, '0')
        }
    }

    return "$finalInt.$finalDecStr".toPersianDigitsLocal()
}

expect fun formatTimestampToPersianDateTime(timestamp: Long): String
