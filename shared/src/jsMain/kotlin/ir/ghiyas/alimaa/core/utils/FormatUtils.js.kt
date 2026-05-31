package ir.ghiyas.alimaa.core.utils

import kotlin.js.Date

actual fun formatTimestampToPersianDateTime(timestamp: Long): String {
    val date = Date(timestamp.toDouble())
    val options = js("({ year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false })")
    val formatted = date.toLocaleDateString("fa-IR", options)
    return formatted.replace(",", " -").replace("،", " -")
}
