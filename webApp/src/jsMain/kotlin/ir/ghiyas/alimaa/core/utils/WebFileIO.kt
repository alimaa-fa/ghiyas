package ir.ghiyas.alimaa.core.utils

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.json

object WebFileIO {
    
    // متد تشخیص محیط: آیا در اپلیکیشن بومی اندروید هستیم؟
    fun isAndroidNativeApp(): Boolean {
        return js("typeof window.AndroidBridge !== 'undefined'").unsafeCast<Boolean>()
    }

    // متد فراخوانی ذخیره‌ساز بومی اندروید
    fun exportViaAndroidNative(filename: String, content: String) {
        if (isAndroidNativeApp()) {
            js("window.AndroidBridge.saveBackup(content, filename)")
        }
    }

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
                            console.error("اشتراک‌گذاری لغو شد یا در این مرورگر پشتیبانی نمی‌شود")
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

    fun copyToClipboard(text: String, onResult: (Boolean) -> Unit) {
        val nav = window.navigator.asDynamic()
        if (nav.clipboard != undefined) {
            nav.clipboard.writeText(text)
                .then { onResult(true) }
                .catch { _: Throwable ->
                    fallbackCopy(text, onResult)
                }
        } else {
            fallbackCopy(text, onResult)
        }
    }

    private fun fallbackCopy(text: String, onResult: (Boolean) -> Unit) {
        try {
            val textArea = document.createElement("textarea") as HTMLTextAreaElement
            textArea.value = text
            textArea.style.position = "fixed"
            textArea.style.left = "-999999px"
            textArea.style.top = "-999999px"
            document.body?.appendChild(textArea)
            textArea.focus()
            textArea.select()
            val successful = document.asDynamic().execCommand("copy") as Boolean
            document.body?.removeChild(textArea)
            onResult(successful)
        } catch (e: Exception) {
            console.error("خطا در کپی کلیپ‌بورد", e)
            onResult(false)
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

    fun importFromUrl(url: String, onResult: (String?, String?) -> Unit) {
        val processText = { text: String ->
            val textStr = text.trim()
            if (textStr.startsWith("{") || textStr.startsWith("[")) {
                onResult(textStr, null)
            } else {
                onResult(null, "این لینک حاوی یک صفحه اینترنتی است. لطفاً لینک دانلود مستقیم (Direct Link) را وارد کنید.")
            }
        }

        window.fetch(url).asDynamic()
            .then { response ->
                if (response.ok) return@then response.text()
                else throw Exception("HTTP_ERROR")
            }
            .then { text -> processText(text as String) }
            .catch { _: Throwable ->
                val encodedUrl = js("encodeURIComponent(url)") as String
                val proxyUrl = "https://api.allorigins.win/raw?url=$encodedUrl"
                
                window.fetch(proxyUrl).asDynamic()
                    .then { response ->
                        if (response.ok) return@then response.text()
                        else throw Exception("PROXY_ERROR")
                    }
                    .then { text -> processText(text as String) }
                    .catch { _: Throwable ->
                        onResult(null, "سرور میزبان اجازه خواندن فایل را نمی‌دهد. لطفاً لینک را در مرورگر باز کرده و فایل را مستقیماً دانلود کنید.")
                    }
            }
    }
}
