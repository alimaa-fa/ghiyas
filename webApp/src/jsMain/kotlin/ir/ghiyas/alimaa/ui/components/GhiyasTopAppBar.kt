package ir.ghiyas.alimaa.ui.components

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.*

@Composable
fun GhiyasTopAppBar(
    onMenuClick: () -> Unit,
    onClearClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onShareClick: (() -> Unit)? = null,
    centerContent: (@Composable () -> Unit)? = null,
    onAddNewCalendar: (() -> Unit)? = null,
    onEditCalendar: (() -> Unit)? = null,
    onDeleteCalendar: (() -> Unit)? = null,
    onSetDefaultCalendar: (() -> Unit)? = null,
    hasActiveCalendar: Boolean = false
) {
    var isManagementMenuOpen by remember { mutableStateOf(false) }

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

        Div(attrs = { style { display(DisplayStyle.Flex); gap(16.px) } }) {
            if (onAddNewCalendar != null) {
                Span(attrs = { style { cursor("pointer"); fontSize(22.px) }; title("افزودن تقویم جدید"); onClick { onAddNewCalendar() } }) { Text("➕") }
                
                if (hasActiveCalendar) {
                    Span(attrs = { 
                        style { cursor("pointer"); fontSize(22.px) }
                        onClick { isManagementMenuOpen = !isManagementMenuOpen } 
                    }) { Text("⋮") }
                    
                    if (isManagementMenuOpen) {
                        // لایه نامرئی کل صفحه برای بستن منو با کلیک در بیرون
                        Div(attrs = {
                            style { position(Position.Fixed); top(0.px); left(0.px); width(100.percent); height(100.vh); property("z-index", "90") }
                            onClick { isManagementMenuOpen = false }
                        }) {}
                        
                        // خود منو
                        Div(attrs = {
                            style { position(Position.Absolute); top(44.px); left(16.px); backgroundColor(Color("white")); borderRadius(8.px); property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)"); padding(8.px, 0.px); minWidth(160.px); property("z-index", "100"); color(Color("#424242")) }
                        }) {
                            if (onSetDefaultCalendar != null) {
                                Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); property("border-bottom", "1px solid #EEEEEE") }; onClick { isManagementMenuOpen = false; onSetDefaultCalendar() } }) { Text("⭐ تنظیم پیش‌فرض") }
                            }
                            if (onEditCalendar != null) {
                                Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); property("border-bottom", "1px solid #EEEEEE") }; onClick { isManagementMenuOpen = false; onEditCalendar() } }) { Text("✏️ ویرایش تقویم") }
                            }
                            if (onDeleteCalendar != null) {
                                Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); color(Color("#D32F2F")) }; onClick { isManagementMenuOpen = false; onDeleteCalendar() } }) { Text("🗑️ حذف تقویم") }
                            }
                        }
                    }
                }
            } else {
                if (onShareClick != null) Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("اشتراک‌گذاری"); onClick { onShareClick() } }) { Text("📤") }
                Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("پاک کردن فرم"); onClick { onClearClick() } }) { Text("🧹") }
                Span(attrs = { style { cursor("pointer"); fontSize(20.px) }; title("تاریخچه"); onClick { onHistoryClick() } }) { Text("🕒") }
            }
        }
    }
}
