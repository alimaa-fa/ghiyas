package ir.ghiyas.alimaa.core.utils

actual fun formatTimestampToPersianDateTime(timestamp: Long): String {
    return formatPersianDateWasm(timestamp.toDouble())
}

@Suppress("UNUSED_PARAMETER")
private fun formatPersianDateWasm(timestamp: Double): String =
    js("new Date(Number(timestamp)).toLocaleDateString('fa-IR', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false }).replace(/,/g, ' -').replace(/،/g, ' -')")
