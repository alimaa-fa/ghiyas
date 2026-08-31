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
    
    fun downloadJsonFile(filename: String, content: String) {
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
                        .then { 
                            console.log("اشتراک‌گذاری/ذخیره موفقیت‌آمیز بود") 
                        }
                        .catch { 
                            console.log("اشتراک‌گذاری لغو شد یا خطا داد، انتقال به دانلود معمولی")
                            fallbackDownload(blob, filename)
                        }
                    return
                }
            } catch (e: Exception) {
                console.error("خطا در سیستم اشتراک‌گذاری", e)
            }
        }
        
        fallbackDownload(blob, filename)
    }

    private fun fallbackDownload(blob: Blob, filename: String) {
        val url = URL.createObjectURL(blob)
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = url
        anchor.download = filename
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        URL.revokeObjectURL(url)
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
                    console.error("خطا در خواندن فایل پشتیبان قیاس")
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
}
