package ir.ghias.alimaa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghias.alimaa.ui.components.GhiyasTopAppBar
import ir.ghias.alimaa.ui.components.HeroBanner
import ir.ghias.alimaa.ui.stages.InputStageScreen

@Composable
fun App() {
    var clearFormRequested by remember { mutableStateOf(false) }

    Div(attrs = {
        dir(DirType.Rtl) // اصلاح شد: استفاده از نوع داده صحیح به جای استرینگ
        style {
            property("margin", "0 auto") // اصلاح شد
            maxWidth(480.px)
            width(100.percent)
            height(100.vh)
            backgroundColor(Color("#F5F5F5"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            fontFamily("system-ui, -apple-system, sans-serif")
        }
    }) {
        GhiyasTopAppBar(
            onClearClick = { clearFormRequested = true },
            onHistoryClick = { /* TODO */ }
        )
        
        Div(attrs = {
            style {
                property("flex", "1") // اصلاح شد
                property("overflow-y", "auto") // اصلاح شد
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
