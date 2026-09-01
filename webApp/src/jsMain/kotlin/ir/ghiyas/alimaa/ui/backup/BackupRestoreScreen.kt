package ir.ghiyas.alimaa.ui.backup

import androidx.compose.runtime.*
import ir.ghiyas.alimaa.core.backup.BackupOrchestrator
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*

@Composable
fun BackupRestoreScreen(onNavigateBack: () -> Unit) {
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var backupUrl by remember { mutableStateOf("") }

    Div({
        style {
            padding(16.px)
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(16.px)
            fontFamily("DimaWeb, Tahoma, sans-serif")
            property("direction", "rtl")
            height(100.percent)
            backgroundColor(Color("#ffffff"))
        }
    }) {
        // هدر
        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.SpaceBetween)
                property("border-bottom", "1px solid #e0e0e0")
                paddingBottom(12.px)
            }
        }) {
            H3({ style { margin(0.px); color(Color("#2e7d32")) } }) {
                Text("پشتیبان‌گیری و بازیابی")
            }
            Button({
                onClick { onNavigateBack() }
                style {
                    backgroundColor(Color("transparent"))
                    border(0.px)
                    color(Color("#d32f2f"))
                    cursor("pointer")
                    fontWeight("bold")
                    fontFamily("inherit")
                }
            }) {
                Text("بازگشت 🏠")
            }
        }

        // پیام وضعیت
        if (statusMessage.isNotEmpty()) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(if (isError) Color("#ffebee") else Color("#e8f5e9"))
                    color(if (isError) Color("#c62828") else Color("#2e7d32"))
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, if (isError) Color("#ef9a9a") else Color("#a5d6a7"))
                    fontSize(14.px)
                    lineHeight("1.5")
                }
            }) {
                Text(statusMessage)
            }
        }

        // بخش تهیه نسخه پشتیبان (Export)
        Div({
            style {
                backgroundColor(Color("#f5f5f5"))
                padding(16.px)
                borderRadius(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(12.px)
                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            }
        }) {
            H4({ style { margin(0.px); color(Color("#333")) } }) { Text("تهیه نسخه پشتیبان (Export)") }
            P({ style { margin(0.px); fontSize(14.px); color(Color("#555")) } }) {
                Text("بر اساس مرورگر یا اپلیکیشنی که در آن هستید، بهترین روش را انتخاب کنید.")
            }
            
            // دکمه اشتراک‌گذاری (فایرفاکس / اندروید)
            Button({
                onClick {
                    statusMessage = "در حال آماده‌سازی پنجره اشتراک‌گذاری..."
                    isError = false
                    BackupOrchestrator.exportBackupShare(
                        onFallbackRequested = {
                            statusMessage = "اشتراک‌گذاری پشتیبانی نشد. فایل روی دستگاه دانلود می‌شود."
                            BackupOrchestrator.exportBackupDirect()
                        }
                    )
                }
                style {
                    backgroundColor(Color("#FF9800"))
                    color(Color("white"))
                    border(0.px)
                    borderRadius(8.px)
                    padding(12.px)
                    cursor("pointer")
                    fontWeight("bold")
                    fontFamily("inherit")
                }
            }) {
                Text("📤 اشتراک‌گذاری فایل (مناسب فایرفاکس)")
            }

            // دکمه دانلود مستقیم (کروم / PWA)
            Button({
                onClick {
                    BackupOrchestrator.exportBackupDirect()
                    statusMessage = "فایل با موفقیت در پوشه دانلودها ذخیره شد."
                    isError = false
                }
                style {
                    backgroundColor(Color("#2e7d32"))
                    color(Color("white"))
                    border(0.px)
                    borderRadius(8.px)
                    padding(12.px)
                    cursor("pointer")
                    fontWeight("bold")
                    fontFamily("inherit")
                }
            }) {
                Text("💾 دانلود فایل فیزیکی (مناسب کروم / PWA)")
            }

            // دکمه ابری ایتا
            Button({
                onClick {
                    BackupOrchestrator.exportBackupCloud { success, msg ->
                        isError = !success
                        statusMessage = msg
                    }
                }
                style {
                    backgroundColor(Color("#1976d2"))
                    color(Color("white"))
                    border(0.px)
                    borderRadius(8.px)
                    padding(12.px)
                    cursor("pointer")
                    fontWeight("bold")
                    fontFamily("inherit")
                    opacity(0.7) // کمی کدر برای حالت در حال توسعه
                }
            }) {
                Text("☁️ ذخیره در فضای ابری ایتا (به‌زودی)")
            }
        }

        // بخش بازیابی اطلاعات (Import)
        Div({
            style {
                backgroundColor(Color("#f5f5f5"))
                padding(16.px)
                borderRadius(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(12.px)
                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            }
        }) {
            H4({ style { margin(0.px); color(Color("#333")) } }) { Text("بازیابی اطلاعات (Import)") }
            
            // زیربخش: از طریق فایل آفلاین
            Div({ style { property("border-bottom", "1px dashed #ccc"); paddingBottom(12.px) } }) {
                P({ style { margin(0.px, 0.px, 8.px, 0.px); fontSize(14.px); color(Color("#555")) } }) {
                    Text("اگر فایل پشتیبان روی گوشی شماست:")
                }
                Button({
                    onClick {
                        statusMessage = ""
                        isError = false
                        BackupOrchestrator.importBackupFromFile(
                            onStartProcessing = { statusMessage = "در حال خواندن فایل..."; isError = false },
                            onComplete = { success, msg -> isError = !success; statusMessage = msg }
                        )
                    }
                    style {
                        backgroundColor(Color("#4CAF50"))
                        color(Color("white"))
                        border(0.px)
                        borderRadius(8.px)
                        padding(12.px)
                        cursor("pointer")
                        fontWeight("bold")
                        fontFamily("inherit")
                        width(100.percent)
                    }
                }) {
                    Text("📂 انتخاب فایل از حافظه گوشی")
                }
            }

            // زیربخش: از طریق لینک (ابری)
            Div({ style { paddingTop(8.px) } }) {
                P({ style { margin(0.px, 0.px, 8.px, 0.px); fontSize(14.px); color(Color("#555")) } }) {
                    Text("اگر فایل پشتیبان را در فضای ابری آپلود کرده‌اید، لینک مستقیم آن را وارد کنید:")
                }
                Input(InputType.Text) {
                    value(backupUrl)
                    onInput { backupUrl = it.value }
                    style {
                        width(100.percent)
                        property("box-sizing", "border-box")
                        padding(10.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, Color("#ccc"))
                        fontFamily("inherit")
                        marginBottom(8.px)
                        property("direction", "ltr")
                        textAlign("left")
                    }
                    attr("placeholder", "https://example.com/backup.json")
                }
                Button({
                    onClick {
                        if (backupUrl.isBlank()) {
                            isError = true
                            statusMessage = "لطفاً ابتدا لینک دانلود را وارد کنید."
                            return@onClick
                        }
                        statusMessage = ""
                        isError = false
                        BackupOrchestrator.importBackupFromUrl(
                            url = backupUrl,
                            onStartProcessing = { statusMessage = "در حال ارتباط با سرور و دریافت فایل..."; isError = false },
                            onComplete = { success, msg -> isError = !success; statusMessage = msg; if(success) backupUrl = "" }
                        )
                    }
                    style {
                        backgroundColor(Color("#0288D1"))
                        color(Color("white"))
                        border(0.px)
                        borderRadius(8.px)
                        padding(12.px)
                        cursor("pointer")
                        fontWeight("bold")
                        fontFamily("inherit")
                        width(100.percent)
                    }
                }) {
                    Text("🌐 دریافت و بازیابی از لینک")
                }
            }
        }
    }
}
