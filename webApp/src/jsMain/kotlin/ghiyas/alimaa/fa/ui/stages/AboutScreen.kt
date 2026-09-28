package ghiyas.alimaa.fa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import kotlinx.browser.window
import kotlinx.coroutines.await
import ghiyas.alimaa.fa.AppConfig
import ghiyas.alimaa.fa.core.pwa.PwaManager

@Composable
fun AboutScreen(onBack: () -> Unit) {
    var swVersion by remember { mutableStateOf("در حال بررسی...") }
    var showCopyToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val timestamp = kotlin.js.Date().getTime()
            val response = window.fetch("./sw.js?t=$timestamp").await()
            if (response.ok) {
                val text = response.text().await()
                val regex = Regex("CACHE_NAME\\s*=\\s*['\"]([^'\"]+)['\"]")
                val match = regex.find(text)
                swVersion = match?.groupValues?.get(1) ?: "نامشخص"
            } else {
                swVersion = "خطا در خواندن"
            }
        } catch (e: Exception) {
            swVersion = "آفلاین / در دسترس نیست"
        }
    }

    Div(attrs = { 
        style { 
            padding(24.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); 
            alignItems(AlignItems.Center); fontFamily("Vazirmatn", "system-ui", "sans-serif"); 
            color(Color("#2E7D32")) 
        } 
    }) {
        Img(src = "./icon-512.png", attrs = {
            style {
                width(120.px); height(120.px); borderRadius(50.percent);
                backgroundColor(Color("transparent")); marginBottom(16.px);
                property("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            }
        })

        H2(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#1B5E20")) } }) { Text("قیاس") }
        P(attrs = { style { margin(0.px, 0.px, 24.px, 0.px); color(Color("#4CAF50")); fontWeight("bold") } }) { 
            Text("(محاسبه‌گر محلی و تقویم آبیاری)") 
        }

        Div(attrs = { 
            style { 
                width(100.percent); maxWidth(400.px); backgroundColor(Color("white")); 
                padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#C8E6C9"));
                marginBottom(24.px)
            } 
        }) {
            Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); marginBottom(12.px); property("border-bottom", "1px dashed #E8F5E9"); paddingBottom(8.px) } }) {
                Span(attrs = { style { color(Color("#757575")) } }) { Text("نسخه کَش وب (PWA):") }
                Span(attrs = { style { fontWeight("bold"); property("direction", "ltr") } }) { Text(swVersion) }
            }
            Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween) } }) {
                Span(attrs = { style { color(Color("#757575")) } }) { Text("نسخه اندروید:") }
                Span(attrs = { style { fontWeight("bold"); property("direction", "ltr") } }) { Text(AppConfig.ANDROID_VERSION) }
            }
        }

        val bazaarUrl = "http://cafebazaar.ir/app/?id=ghiyas.alimaa.fa&ref=share"
        A(href = PwaManager.getSafeUrl(bazaarUrl), attrs = {
            style { 
                width(100.percent); maxWidth(400.px); backgroundColor(Color("#4CAF50")); color(Color("white")); 
                padding(14.px); borderRadius(8.px); textAlign("center"); textDecoration("none");
                fontWeight("bold"); fontSize(1.1.cssRem); marginBottom(24.px);
                display(DisplayStyle.Block); cursor("pointer")
            }
            onClick { e -> PwaManager.handleLinkClick(e.nativeEvent, bazaarUrl) }
        }) { Text("دریافت اپلیکیشن از کافه بازار") }

        Div(attrs = { 
            style { 
                width(100.percent); maxWidth(400.px); backgroundColor(Color("#F1F8E9")); 
                padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#AED581"));
                textAlign("center")
            } 
        }) {
            P(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#33691E")); fontSize(0.95.cssRem); lineHeight("1.6") } }) {
                Text("برای پیشنهاد، گزارش باگ و ساختن محاسبه و یا تقویم بوسیله‌ی برنامه‌نویس با شناسه من در ایتا تماس بگیرید:")
            }
            
            val eitaaUrl = "https://eitaa.com/AlirezaMariki"
            A(href = PwaManager.getSafeUrl(eitaaUrl), attrs = {
                style { 
                    display(DisplayStyle.InlineBlock); backgroundColor(Color("#FF9800")); color(Color("white")); 
                    padding(8.px, 16.px); borderRadius(6.px); textDecoration("none"); fontWeight("bold");
                    marginBottom(8.px); cursor("pointer")
                }
                onClick { e -> PwaManager.handleLinkClick(e.nativeEvent, eitaaUrl) }
            }) { Text("ارتباط مستقیم در ایتا") }
            
            P(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#E65100")); fontSize(0.85.cssRem) } }) {
                Text("نکته: به دلیل باگ ایتا، پس از بازگشت به برنامه برای کارکرد مجدد لمس، یک‌بار کلید بازگشت (Back) را بزنید.")
            }
            
            Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center); gap(8.px) } }) {
                Span(attrs = { style { color(Color("#555")); fontWeight("bold"); property("direction", "ltr") } }) { Text("@AlirezaMariki") }
                Button(attrs = { 
                    style { 
                        backgroundColor(Color("#E0E0E0")); border(0.px); borderRadius(4.px); 
                        padding(4.px, 8.px); cursor("pointer"); fontSize(0.85.cssRem); color(Color("#424242"))
                    }
                    onClick {
                        window.navigator.clipboard.writeText("@AlirezaMariki")
                        showCopyToast = true
                        window.setTimeout({ showCopyToast = false }, 2000)
                    }
                }) { Text(if (showCopyToast) "کپی شد ✓" else "کپی") }
            }
        }
    }
}
