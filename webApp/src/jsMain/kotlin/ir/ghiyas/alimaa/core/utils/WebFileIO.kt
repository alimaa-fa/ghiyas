package ir.ghiyas.alimaa.core.utils

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.json

object WebFileIO {
    
    fun exportViaDirectDownload(filename: String, content: String) {
        val blob = Blob(arrayOf(content), BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = filename
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
    }

    fun exportViaWebShare(filename: String, content: String, onFailed: () -> Unit) {
        val blob = Blob(arrayOf(content), BlobPropertyBag(type = "application/json"))
        val nav = window.navigator.asDynamic()
        
        if (nav.canShare != undefined) {
            try {
                val file = js("new File([blob], filename, {type: 'application/json'})")
                val shareData = json(
                    "files" to arrayOf(file),
                    "title" to "پشتیبان قیاس",
                    "text" to "فایل پشتیبان اطلاعات قیاس"
                )
                
                if (nav.canShare(shareData).unsafeCast<Boolean>()) {
                    nav.share(shareData).asDynamic()
                        .then { console.log("اشتراک‌گذاری موفق بود") }
                        .catch { _: Throwable ->
                            console.error("اشتراک‌گذاری لغو شد")
                            onFailed()
                        }
                    return
                } else {
                    onFailed()
                }
            } catch (e: Exception) {
                console.error("خطا در تنظیمات اشتراک‌گذاری", e)
                onFailed()
            }
        } else {
            onFailed()
        }
    }

    fun importJsonFile(onFileSelected: () -> Unit, onResult: (String?) -> Unit) {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = ".json"
        
        var fileIsSelected = false
        
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                fileIsSelected = true
                onFileSelected()
                val reader = FileReader()
                FileReader::class.js 
                reader.onload = { _ ->
                    onResult(reader.result as? String)
                }
                reader.onerror = {
                    console.error("خطا در خواندن فایل")
                    onResult(null)
                }
                reader.readAsText(file)
            }
            null
        }
        
        window.addEventListener("focus", {
            window.setTimeout({
                val files = input.files
                val hasFiles = files != null && files.length > 0
                if (!fileIsSelected && !hasFiles) {
                    onResult("CANCELED")
                }
            }, 300)
        }, json("once" to true))
        
        input.click()
    }

    // متد جدید با مکانیزم دور زدن خطای CORS
    fun importFromUrl(url: String, onResult: (String?, String?) -> Unit) {
        
        val processText = { text: String ->
            val textStr = text.trim()
            if (textStr.startsWith("{") || textStr.startsWith("[")) {
                onResult(textStr, null)
            } else {
                onResult(null, "این لینک حاوی یک صفحه اینترنتی (مثل صفحه دانلود آپلودسنتر) است. لطفاً لینک دانلود مستقیم (Direct Link) را وارد کنید.")
            }
        }

        // تلاش اول: دریافت مستقیم
        window.fetch(url).asDynamic()
            .then { response ->
                if (response.ok) {
                    return@then response.text()
                } else {
                    throw Exception("HTTP_ERROR")
                }
            }
            .then { text ->
                processText(text as String)
            }
            .catch { _: Throwable ->
                console.warn("Direct fetch blocked by CORS. Attempting Proxy Fallback...")
                
                // تلاش دوم: دور زدن CORS با استفاده از پروکسی عمومی و معتبر AllOrigins
                val encodedUrl = js("encodeURIComponent(url)") as String
                val proxyUrl = "https://api.allorigins.win/raw?url=$encodedUrl"
                
                window.fetch(proxyUrl).asDynamic()
                    .then { response ->
                        if (response.ok) {
                            return@then response.text()
                        } else {
                            throw Exception("PROXY_ERROR")
                        }
                    }
                    .then { text ->
                        processText(text as String)
                    }
                    .catch { _: Throwable ->
                        // اگر سرور مبدا حتی پروکسی را هم بلاک کرد، راهنمای انسانی می‌دهیم
                        onResult(null, "سرور میزبان فایل (آپلودسنتر) اجازه خواندن مستقیم فایل را نمی‌دهد. \nراهکار: لینک را در مرورگر باز کنید، فایل را دانلود کرده و سپس از گزینه «انتخاب فایل از حافظه گوشی» استفاده کنید.")
                    }
            }
    }
}
