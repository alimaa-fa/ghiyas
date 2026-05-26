package ir.ghiyas.alimaa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.ui.components.GhiyasTopAppBar
import ir.ghiyas.alimaa.ui.components.HeroBanner
import ir.ghiyas.alimaa.ui.stages.InputStageScreen
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet // ایمپورت فایل استایل‌های جدید

@Composable
fun App() {
    var clearFormRequested by remember { mutableStateOf(false) }

    // تزریق کدهای CSS سفارشی (مثل افکت شناور و فونت) به کل برنامه
    Style(AppStyleSheet)

    Div(attrs = {
        dir(DirType.Rtl)
        style {
            property("margin", "0 auto")
            maxWidth(600.px) // افزایش عرض برای تناسب بهتر
            width(100.percent)
            minHeight(100.vh) // استفاده از minHeight برای اسکرول روان‌تر
            backgroundColor(Color("#F5F5F5"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            // اعمال فونت فلاتری (وزیرمتن)
            fontFamily("Vazirmatn", "system-ui", "-apple-system", "sans-serif")
            property("box-shadow", "0 0 15px rgba(0,0,0,0.05)") // سایه نرم برای حالت دسکتاپ
        }
    }) {
        GhiyasTopAppBar(
            onClearClick = { clearFormRequested = true },
            onHistoryClick = { /* TODO: اتصال منوی کشویی در فازهای بعدی */ }
        )
        
        Div(attrs = {
            style {
                property("flex", "1")
                property("overflow-y", "auto")
            }
        }) {
            HeroBanner()
            InputStageScreen(
                onClearRequested = clearFormRequested,
                onClearComplete = { clearFormRequested = false }
            )
        }
    }
}
