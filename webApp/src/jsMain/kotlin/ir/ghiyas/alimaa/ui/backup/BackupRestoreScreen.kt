package ir.ghiyas.alimaa.ui.backup

import androidx.compose.runtime.*
import ir.ghiyas.alimaa.core.backup.BackupOrchestrator
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*

@Composable
fun BackupRestoreScreen(onNavigateBack: () -> Unit) {
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

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

        if (statusMessage.isNotEmpty()) {
            Div({
                style {
                    padding(12.px)
                    backgroundColor(if (isError) Color("#ffebee") else Color("#e8f5e9"))
                    color(if (isError) Color("#c62828") else Color("#2e7d32"))
                    borderRadius(8.px)
                    border(1.px, LineStyle.Solid, if (isError) Color("#ef9a9a") else Color("#a5d6a7"))
                    fontSize(14.px)
                }
            }) {
                Text(statusMessage)
            }
        }

        Div({
            style {
                backgroundColor(Color("#f5f5f5"))
                padding(16.px)
                borderRadius(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(8.px)
                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            }
        }) {
            H4({ style { margin(0.px); color(Color("#333")) } }) { Text("تهیه نسخه پشتیبان (Export)") }
            P({ style { margin(0.px); fontSize(14.px); color(Color("#555")) } }) {
                Text("یک فایل یکپارچه شامل تاریخچه محاسبات، تقویم‌های کاری و الگوهای اختصاصی ساخته شده و قابلیت ذخیره در مدیریت فایل یا اشتراک‌گذاری در پیام‌رسان را دارد.")
            }
            Button({
                onClick {
                    BackupOrchestrator.exportBackup()
                    statusMessage = "آماده‌سازی فایل انجام شد. در صورت باز شدن پنجره، محل ذخیره را انتخاب کنید."
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
                    marginTop(12.px)
                    fontFamily("inherit")
                }
            }) {
                Text("💾 ایجاد فایل پشتیبان")
            }
        }

        Div({
            style {
                backgroundColor(Color("#f5f5f5"))
                padding(16.px)
                borderRadius(12.px)
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                gap(8.px)
                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
            }
        }) {
            H4({ style { margin(0.px); color(Color("#333")) } }) { Text("بازیابی اطلاعات (Import)") }
            P({ style { margin(0.px); fontSize(14.px); color(Color("#555")) } }) {
                Text("فایل پشتیبان قیاس را انتخاب کنید. اطلاعات جدید به صورت هوشمند و بدون پاک شدن اطلاعات قبلی اضافه می‌شوند.")
            }
            Button({
                onClick {
                    statusMessage = ""
                    isError = false
                    BackupOrchestrator.importBackup(
                        onStartProcessing = { 
                            statusMessage = "در حال خواندن فایل و ادغام اطلاعات..."
                            isError = false
                        },
                        onComplete = { success, message ->
                            isError = !success
                            statusMessage = message
                        }
                    )
                }
                style {
                    backgroundColor(Color("#1976d2"))
                    color(Color("white"))
                    border(0.px)
                    borderRadius(8.px)
                    padding(12.px)
                    cursor("pointer")
                    fontWeight("bold")
                    marginTop(12.px)
                    fontFamily("inherit")
                }
            }) {
                Text("📂 انتخاب فایل و بازیابی هوشمند")
            }
        }
    }
}
