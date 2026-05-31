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

    fun shareText(record: CalculationHistoryRecord) {
        val text = TextExportFormatter.formatRecord(record)
        val nav = window.navigator.asDynamic()
        if (nav.share != undefined) {
            val sharePromise: dynamic = nav.share(json(
                "title" to "اشتراک‌گذاری ${record.calculationName}",
                "text" to text
            ))
            sharePromise.catch { err: dynamic -> console.error("Error sharing text", err) }
        } else {
            // Fallback: Copy to clipboard
            if (nav.clipboard != undefined) {
                val clipboardPromise: dynamic = nav.clipboard.writeText(text)
                clipboardPromise.then {
                    window.alert("متن کپی شد!")
                }.catch { err: dynamic ->
                    console.error("Clipboard write failed", err)
                }
            } else {
                window.alert("امکان اشتراک‌گذاری یا کپی وجود ندارد.")
            }
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

        // Header
        val isKg = record.baseUnit.contains("کیلو") || record.baseUnit.contains("گرم")
        val header = document.createElement("div") as HTMLDivElement
        header.style.textAlign = "center"
        header.style.marginBottom = "15px"
        header.innerHTML = """
            <h2 style="margin: 0; padding: 0;">${record.calculationName}</h2>
            <div style="font-size: 14px; color: #666;">${record.persianYear}</div>
            <div style="font-size: 16px; margin-top: 10px; font-weight: bold;">مقدار کل: <bdi>${record.inputAmount.value.toGhiyasFormat(isKg)}</bdi> ${record.baseUnit}</div>
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
                    <span style="flex: 1; text-align: left;"><bdi>${item.value.value.toGhiyasFormat(isKg)}</bdi> ${record.baseUnit}</span>
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
