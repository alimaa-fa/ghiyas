package ir.ghiyas.alimaa.ui.components

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.Composable

@Composable
fun GhiyasTopAppBar(
    onClearClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareClick: (() -> Unit)? = null
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
        }
    }) {
        // اولین آیتم در DOM: چون RTL هستیم، این در سمت "راست" صفحه می‌افتد
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(12.px)
            }
        }) {
            Span(attrs = {
                style { cursor("pointer"); fontSize(24.px) }
            }) {
                Text("☰") // منوی همبرگری
            }
            Span(attrs = {
                style { fontSize(20.px); fontWeight("bold") }
            }) {
                Text("قیاس")
            }
        }

        // دومین آیتم در DOM: با کمک SpaceBetween به سمت "چپ" هول داده می‌شود
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
            }
        }) {
            if (onShareClick != null) {
                Span(attrs = {
                    style { cursor("pointer"); fontSize(20.px) }
                    title("اشتراک‌گذاری نتایج")
                    onClick { onShareClick() }
                }) {
                    Text("📤")
                }
            }
            Span(attrs = {
                style { cursor("pointer"); fontSize(20.px) }
                title("پاک کردن فرم")
                onClick { onClearClick() }
            }) {
                Text("🧹")
            }
            Span(attrs = {
                style { cursor("pointer"); fontSize(20.px) }
                title("تاریخچه")
                onClick { onHistoryClick() }
            }) {
                Text("🕒")
            }
        }
    }
}
