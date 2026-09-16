package ir.ghiyas.alimaa.ui.components

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.*

@Composable
fun GhiyasTopAppBar(
    onMenuClick: () -> Unit,
    onClearClick: (() -> Unit)? = null,
    onHistoryClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    centerContent: (@Composable () -> Unit)? = null
) {
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            justifyContent(JustifyContent.SpaceBetween)
            alignItems(AlignItems.Center)
            backgroundColor(Color("#4CAF50"))
            color(Color("white"))
            padding(12.px, 16.px)
            property("box-shadow", "0 2px 4px rgba(0,0,0,0.2)")
            position(Position.Relative) 
        }
    }) {
        Div(attrs = { 
            style { 
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
                flex(1)
                property("min-width", "0")
            } 
        }) {
            Span(attrs = { 
                style { cursor("pointer"); fontSize(24.px); flexShrink(0) }
                onClick { onMenuClick() } 
            }) { Text("☰") }
            
            if (centerContent != null) {
                // کانتینر انعطاف‌پذیر با کوچک‌نمایی فونت داینامیک (جایگزین سه‌نقطه)
                Div(attrs = { 
                    style { 
                        flex(1)
                        property("min-width", "0")
                        property("white-space", "nowrap")
                        property("overflow", "hidden")
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        // کلید حل مشکل: استفاده از کانتینری که اجازه کوچک شدن به المان فرزند را می‌دهد
                    } 
                }) { centerContent() }
            } else {
                Span(attrs = { 
                    style { 
                        fontSize(20.px)
                        fontWeight("bold")
                        property("white-space", "nowrap")
                        property("overflow", "hidden")
                    } 
                }) { Text("قیاس") }
            }
        }

        Div(attrs = { style { display(DisplayStyle.Flex); gap(16.px); flexShrink(0) } }) {
            if (onShareClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("اشتراک‌گذاری"); onClick { onShareClick() } }) { Text("📤") }
            if (onClearClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("پاک کردن فرم"); onClick { onClearClick() } }) { Text("🧹") }
            if (onHistoryClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("تاریخچه"); onClick { onHistoryClick() } }) { Text("🕒") }
        }
    }
}
