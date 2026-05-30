package ir.ghiyas.alimaa.ui.components

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.Composable

@Composable
fun HeroBanner() {
    Div({
        style {
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            alignItems(AlignItems.Center)
            justifyContent(JustifyContent.Center)
            backgroundColor(Color("rgba(76, 175, 80, 0.15)"))
            borderRadius(16.px)
            padding(32.px, 16.px)
            margin(16.px)
            textAlign("center")
        }
    }) {
        // Icon
        Div({
            style {
                fontSize(48.px)
                color(Color("#4CAF50"))
                marginBottom(16.px)
            }
        }) {
            Text("⚖️")
        }
        
        H2({
            style {
                margin(0.px)
                color(Color("#212121"))
                property("font-size", "clamp(1.2rem, 4vw, 1.8rem)")
                fontWeight("bold")
            }
        }) {
            Text("برنامه محاسبه‌گر محلی قیاس")
        }
        
        Span({
            style {
                marginTop(8.px)
                color(Color("#757575"))
                property("font-size", "clamp(0.9rem, 2vw, 1.1rem)")
            }
        }) {
            Text("برای شروع مقادیر درخواستی را وارد کنید")
        }
    }
}
