package ir.ghiyas.alimaa.ui.backup

import androidx.compose.runtime.*
import ir.ghiyas.alimaa.core.backup.BackupOrchestrator
import ir.ghiyas.alimaa.core.utils.WebFileIO
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*

@Composable
fun BackupRestoreScreen(onNavigateBack: () -> Unit) {
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var backupUrl by remember { mutableStateOf("") }
    var manualPasteText by remember { mutableStateOf("") }
    
    // تشخیص هوشمند اجرای برنامه در اپلیکیشن بومی اندروید قیاس
    val isNativeAndroid = remember { WebFileIO.isAndroidNativeApp() }

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
            property("overflow-y", "auto")
            paddingBottom(60.px)
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
            H3({ style { margin(0.px); color(Color("#2e7d32")) } }) { Text("پشتیبان‌گیری و بازیابی") }
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
            }) { Text("بازگشت 🏠") }
        }

        if (statusMessage.isNotEmpty()) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(if (isError) Color("#ffebee") else Color("#e8f5e9"))
                    color(if (isError) Color("#c62828") else Color("#2e7d32"))
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, if (isError) Color("#ef9a9a") else Color("#a5d6a7"))
                    fontSize(14.px)
                    lineHeight("1.6")
                }
            }) { Text(statusMessage) }
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
            
            // رندر هوشمند دکمه‌های دانلود بر اساس پلتفرم
            if (isNativeAndroid) {
                Button({
                    onClick {
                        statusMessage = "در حال باز کردن فایل منیجر اندروید... محل ذخیره را انتخاب کنید."
                        isError = false
                        BackupOrchestrator.exportBackupAndroidNative()
                    }
                    style {
                        backgroundColor(Color("#00838F"))
                        color(Color("white"))
                        border(0.px)
                        borderRadius(8.px)
                        padding(12.px)
                        cursor("pointer")
                        fontWeight("bold")
                        fontFamily("inherit")
                    }
                }) { Text("💾 ذخیره فایل در گوشی (مخصوص نسخه اندروید قیاس)") }
            } else {
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
                }) { Text("📤 اشتراک‌گذاری فایل (مناسب فایرفاکس)") }

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
                }) { Text("💾 دانلود فایل فیزیکی (مناسب کروم / PWA)") }
            }

            // دکمه کپی متن همیشه برای مینی‌اپ ایتا در دسترس است
            Button({
                onClick {
                    isError = false
                    val rawJson = BackupOrchestrator.getBackupRawString()
                    
                    WebFileIO.copyToClipboard(rawJson) { success ->
                        if (success) {
                            if (rawJson.length > 4000) {
                                statusMessage = "⚠️ توجه: حجم فایل پشتیبان زیاد است.\nاگر آن را مستقیماً در شخصی ایتا Paste کنید، به چندین پیام خرد می‌شود و هنگام بازیابی باید تک‌تک آن‌ها را کپی کنید.\n\n💡 پیشنهاد بهتر: در گوشی خود یک فایل متنی با نام backup-ghiyas.json بسازید و این متن کپی شده را درون آن جای‌گذاری (Paste) کرده و ذخیره کنید."
                                isError = false
                            } else {
                                statusMessage = "فایل پشتیبان در حافظه موقت (کلیپ‌بورد) کپی شد. اکنون می‌توانید آن را در بخش «پیام‌های ذخیره‌شده» ایتا ارسال (Paste) کنید."
                                isError = false
                            }
                        } else {
                            isError = true
                            statusMessage = "خطا در کپی کردن متن. مرورگر شما از این قابلیت پشتیبانی نمی‌کند."
                        }
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
                }
            }) { Text("📋 کپی متن پشتیبان (مخصوص برنامک ایتا)") }
        }

        // بخش بازیابی اطلاعات (Import)
        Div({
            style {
                backgroundColor(Color("#f5f5f5"))
                padding(16.px)
                borderRadius(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(16.px) 
                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            }
        }) {
            H4({ style { margin(0.px); color(Color("#333")) } }) { Text("بازیابی اطلاعات (Import)") }
            
            // بازیابی از فایل (به‌طور جادویی در اندروید به فایل منیجر متصل است)
            Div({ style { property("border-bottom", "1px dashed #ccc"); paddingBottom(16.px) } }) {
                P({ style { margin(0.px, 0.px, 8.px, 0.px); fontSize(14.px); color(Color("#555")) } }) {
                    Text("۱. اگر فایل پشتیبان (.json) روی گوشی شماست:")
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
                }) { Text("📂 انتخاب فایل از حافظه گوشی") }
            }

            // بخش کپی/پیست متن (متن دقیقاً طبق دستور شما اصلاح شد)
            Div({ style { property("border-bottom", "1px dashed #ccc"); paddingBottom(16.px) } }) {
                P({ style { margin(0.px, 0.px, 8.px, 0.px); fontSize(14.px); color(Color("#555")) } }) {
                    Text("۲. متن پشتیبان را اینجا جای‌گذاری (Paste) کنید:")
                }
                TextArea(value = manualPasteText) {
                    onInput { manualPasteText = it.value }
                    style {
                        width(100.percent)
                        property("box-sizing", "border-box")
                        height(100.px)
                        padding(10.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, Color("#ccc"))
                        fontFamily("inherit")
                        marginBottom(8.px)
                        property("direction", "ltr")
                        textAlign("left")
                    }
                    attr("placeholder", "متن کپی شده را اینجا Paste کنید...")
                }
                Button({
                    onClick {
                        if (manualPasteText.isBlank()) {
                            isError = true
                            statusMessage = "کادر متن خالی است."
                            return@onClick
                        }
                        statusMessage = ""
                        isError = false
                        BackupOrchestrator.importBackupFromRawText(
                            rawText = manualPasteText,
                            onComplete = { success, msg -> 
                                isError = !success
                                statusMessage = msg
                                if (success) manualPasteText = ""
                            }
                        )
                    }
                    style {
                        backgroundColor(Color("#8E24AA"))
                        color(Color("white"))
                        border(0.px)
                        borderRadius(8.px)
                        padding(12.px)
                        cursor("pointer")
                        fontWeight("bold")
                        fontFamily("inherit")
                        width(100.percent)
                    }
                }) { Text("📝 استخراج و بازیابی از متن") }
            }

            // بخش لینک ابری
            Div({ style { paddingTop(8.px) } }) {
                P({ style { margin(0.px, 0.px, 8.px, 0.px); fontSize(14.px); color(Color("#555")) } }) {
                    Text("۳. اگر فایل پشتیبان را در آپلودسنتر ذخیره کرده‌اید، لینک مستقیم آن را وارد کنید:")
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
                            statusMessage = "لطفاً لینک دانلود را وارد کنید."
                            return@onClick
                        }
                        statusMessage = ""
                        isError = false
                        BackupOrchestrator.importBackupFromUrl(
                            url = backupUrl,
                            onStartProcessing = { statusMessage = "در حال دریافت و پردازش فایل از اینترنت..."; isError = false },
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
                }) { Text("🌐 دریافت و بازیابی از لینک") }
            }
        }
    }
}
