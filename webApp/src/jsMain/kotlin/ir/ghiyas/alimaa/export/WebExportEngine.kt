package ir.ghiyas.alimaa.export

import ir.ghiyas.alimaa.domain.config.AppLinksConfig
import ir.ghiyas.alimaa.domain.export.TextExportFormatter
import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLScriptElement
import kotlin.js.json
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat

object WebExportEngine {

    // سیستم اختصاصی برای نمایش پیغام موفقیت کپی (زیبا، حرفه‌ای و بدون باگ مرورگر)
    private fun showToast(message: String, isError: Boolean = false) {
        val toast = document.createElement("div") as HTMLDivElement
        toast.innerText = message
        toast.style.apply {
            position = "fixed"
            bottom = "30px"
            left = "50%"
            transform = "translateX(-50%)"
            backgroundColor = if (isError) "#D32F2F" else "#388E3C" // سبز برای موفق، قرمز برای خطا
            color = "white"
            padding = "12px 24px"
            borderRadius = "8px"
            fontFamily = "Vazirmatn, Tahoma, sans-serif"
            fontSize = "14px"
            zIndex = "10000"
            boxShadow = "0 4px 6px rgba(0,0,0,0.2)"
            transition = "opacity 0.3s ease-in-out"
        }
        document.body?.appendChild(toast)

        // محو کردن خودکار بعد از ۲.۵ ثانیه
        window.setTimeout({
            toast.style.opacity = "0"
            window.setTimeout({ document.body?.removeChild(toast) }, 300)
        }, 2500)
    }

    fun shareText(record: CalculationHistoryRecord) {
        val text = TextExportFormatter.formatRecord(record)
        
        // تابع کلاسیک و کاملاً ضدگلوله برای کپی در مرورگر با استفاده از DOM
        fun fallbackCopyTextToClipboard(textToCopy: String) {
            try {
                val textArea = document.createElement("textarea").asDynamic()
                textArea.value = textToCopy
                textArea.style.top = "0"
                textArea.style.left = "0"
                textArea.style.position = "fixed" // جلوگیری از پرش اسکرول صفحه
                textArea.style.opacity = "0" // مخفی کردن کامل آن از دید کاربر
                
                document.body?.appendChild(textArea)
                textArea.focus()
                textArea.select()
                
                val successful = document.asDynamic().execCommand("copy") as Boolean
                document.body?.removeChild(textArea)
                
                if (successful) {
                    showToast("نتایج کپی شد!")
                } else {
                    showToast("مرورگر از کپی خودکار پشتیبانی نمی‌کند.", true)
                }
            } catch (err: dynamic) {
                console.error("Fallback: Oops, unable to copy", err)
                showToast("خطا در کپی کردن متن.", true)
            }
        }

        val nav = window.navigator.asDynamic()
        
        // در اپلیکیشن وب، مستقیماً از سیستم کلیپ‌بورد استفاده می‌کنیم (و نه Share موبایلی)
        if (nav.clipboard != undefined && nav.clipboard.writeText != undefined) {
            val clipboardPromise: dynamic = nav.clipboard.writeText(text)
            clipboardPromise.then {
                showToast("نتایج با موفقیت کپی شد!")
            }.catch { err: dynamic ->
                console.error("Async: Could not copy text: ", err)
                // اگر API مدرن قفل شده بود، بلافاصله با تکنیک کلاسیک کپی کن
                fallbackCopyTextToClipboard(text)
            }
        } else {
            // مرورگرهای قدیمی که API کلیپ‌بورد را ندارند
            fallbackCopyTextToClipboard(text)
        }
    }

    fun shareImage(record: CalculationHistoryRecord) {
        val container = document.createElement("div") as HTMLDivElement
        container.style.apply {
            position = "absolute"
            left = "-9999px" // Hidden but rendered
            top = "0"
            width = "400px" // Fixed standard mobile width
            backgroundColor = "#ffffff"
            padding = "20px"
            fontFamily = "Tahoma, Arial, sans-serif"
            direction = "rtl"
            color = "#333333"
            boxSizing = "border-box"
        }

        // جایگزینی isKg با ارسال مستقیم baseUnit برای سازگاری با تابع گرد کردن بومی
        val header = document.createElement("div") as HTMLDivElement
        header.style.textAlign = "center"
        header.style.marginBottom = "15px"
        header.innerHTML = """
            <h2 style="margin: 0; padding: 0;">${record.calculationName}</h2>
            <div style="font-size: 14px; color: #666;">${record.persianYear}</div>
            <div style="font-size: 16px; margin-top: 10px; font-weight: bold;">مقدار کل: <bdi>${record.inputAmount.value.toGhiyasFormat(record.baseUnit)}</bdi> ${record.baseUnit}</div>
        """.trimIndent()
        container.appendChild(header)

        // Body (Non-empty lists)
        fun appendSection(title: String, items: List<ir.ghiyas.alimaa.domain.models.ResultItem>) {
            if (items.isEmpty()) return
            val section = document.createElement("div") as HTMLDivElement
            section.style.apply {
                backgroundColor = "#f9f9f9"
                borderRadius = "8px"
                padding = "10px"
                marginBottom = "10px"
            }
            val sectionTitle = document.createElement("div") as HTMLDivElement
            sectionTitle.style.fontWeight = "bold"
            sectionTitle.style.marginBottom = "5px"
            sectionTitle.style.borderBottom = "1px solid #eee"
            sectionTitle.style.paddingBottom = "5px"
            sectionTitle.innerText = title
            section.appendChild(sectionTitle)

            items.forEach { item ->
                val row = document.createElement("div") as HTMLDivElement
                row.style.display = "flex"
                row.style.justifyContent = "space-between"
                row.style.padding = "4px 0"
                row.innerHTML = """
                    <span style="flex: 1;">${item.label}</span>
                    <span style="flex: 1; text-align: left;"><bdi>${item.value.value.toGhiyasFormat(record.baseUnit)}</bdi> ${record.baseUnit}</span>
                """.trimIndent()
                section.appendChild(row)
            }
            container.appendChild(section)
        }

        appendSection("هزینه‌ها", record.expensesResults)
        appendSection("کشاورزی", record.agricultureResults)
        appendSection("نیمه‌کاری", record.nimehkariResults)
        appendSection("سهم‌های نهایی", record.finalSharesResults)

        // Footer
        val footer = document.createElement("div") as HTMLDivElement
        footer.style.textAlign = "center"
        footer.style.marginTop = "20px"
        footer.style.paddingTop = "10px"
        footer.style.borderTop = "1px dashed #ccc"
        footer.style.fontSize = "12px"
        footer.style.color = "#888"
        footer.innerHTML = """
            <div style="font-weight: bold; color: #555; margin-bottom: 5px;">${AppLinksConfig.appName}</div>
            <div>تنظیم شده توسط نرم‌افزار قیاس</div>
            <div style="margin-top: 5px;" dir="ltr">
                Eitaa: ${AppLinksConfig.eitaaLink}<br>
                Bale: ${AppLinksConfig.baleLink}
            </div>
        """.trimIndent()
        container.appendChild(footer)

        document.body?.appendChild(container)

        // Use html2canvas via dynamic script injection if not available
        val w = window.asDynamic()
        if (w.html2canvas == undefined) {
            val script = document.createElement("script") as HTMLScriptElement
            script.src = "https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"
            script.onload = {
                generateAndShareImage(container)
            }
            document.head?.appendChild(script)
        } else {
            generateAndShareImage(container)
        }
    }

    private fun generateAndShareImage(container: HTMLDivElement) {
        val w = window.asDynamic()
        val promise: dynamic = w.html2canvas(container, json("scale" to 2))
        promise.then { canvas: dynamic ->
            val dataUrl = canvas.toDataURL("image/png") as String
            document.body?.removeChild(container)
            shareOrDownloadImage(dataUrl)
        }.catch { err: dynamic ->
            console.error("Error generating image", err)
            document.body?.removeChild(container)
        }
    }

    private fun shareOrDownloadImage(dataUrl: String) {
        js("""
            fetch(dataUrl)
                .then(function(res) { return res.blob(); })
                .then(function(blob) {
                    var file = new File([blob], 'ghiyas_export.png', { type: 'image/png' });
                    if (navigator.canShare && navigator.canShare({ files: [file] })) {
                        navigator.share({
                            title: 'اشتراک‌گذاری تصویر',
                            files: [file]
                        }).catch(function(err) {
                            console.error('Share failed:', err);
                            var a = document.createElement('a');
                            a.href = dataUrl;
                            a.download = 'ghiyas_export.png';
                            a.click();
                        });
                    } else {
                        var a = document.createElement('a');
                        a.href = dataUrl;
                        a.download = 'ghiyas_export.png';
                        a.click();
                    }
                });
        """)
    }
}
