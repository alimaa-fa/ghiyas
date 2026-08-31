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
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px) } }) {
            Span(attrs = { style { cursor("pointer"); fontSize(24.px) }; onClick { onMenuClick() } }) { Text("☰") }
            if (centerContent != null) centerContent() else Span(attrs = { style { fontSize(20.px); fontWeight("bold") } }) { Text("قیاس") }
        }

        // آیکون‌ها فقط در صورتی رندر می‌شوند که تابع آن‌ها پاس داده شده باشد
        Div(attrs = { style { display(DisplayStyle.Flex); gap(16.px) } }) {
            if (onShareClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("اشتراک‌گذاری"); onClick { onShareClick() } }) { Text("📤") }
            if (onClearClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("پاک کردن فرم"); onClick { onClearClick() } }) { Text("🧹") }
            if (onHistoryClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("تاریخچه"); onClick { onHistoryClick() } }) { Text("🕒") }
        }
    }
}
