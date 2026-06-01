package ir.ghiyas.alimaa.ui.calculator

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import ir.ghiyas.alimaa.presentation.calculator.CalculatorViewModel

private fun triggerHapticFeedback() {
    js("if (window.navigator && window.navigator.vibrate) { window.navigator.vibrate(40); }")
}

private var focusTrackerInitialized = false
private fun initFocusTracker() {
    if (!focusTrackerInitialized) {
        js("""
            window.lastFocusedGhiyasInput = null;
            document.addEventListener('focusin', function(e) {
                if(e.target && e.target.tagName === 'INPUT') {
                    window.lastFocusedGhiyasInput = e.target;
                }
            });
        """)
        focusTrackerInitialized = true
    }
}

private fun pasteToActiveField(value: String, onSuccess: () -> Unit) {
    val target: dynamic = js("window.lastFocusedGhiyasInput")
    if (target != null && value != "خطا" && value.isNotEmpty()) {
        val inputElement = target.unsafeCast<HTMLInputElement>()
        inputElement.value = value
        inputElement.dispatchEvent(Event("input", js("{ bubbles: true }")))
        inputElement.dispatchEvent(Event("change", js("{ bubbles: true }")))
        onSuccess()
    } else {
        window.alert("ابتدا روی یک فیلد متنی در فرم کلیک کنید (تا چشمک‌زن شود)، سپس دکمه درج را بزنید.")
    }
}

@Composable
fun FloatingCalculatorWidget(viewModel: CalculatorViewModel) {
    val state by viewModel.state.collectAsState()
    initFocusTracker()

    Div(attrs = {
        style {
            position(Position.Fixed); bottom(0.px); left(50.percent)
            property("transform", "translateX(-50%)"); width(100.percent); maxWidth(600.px) 
            height(100.percent); property("pointer-events", "none"); property("z-index", 9998)
        }
    }) {
        // FAB
        Div(attrs = {
            style {
                position(Position.Absolute); bottom(20.px); left(20.px); property("pointer-events", "auto") 
                display(if (state.isVisible) DisplayStyle.None else DisplayStyle.Flex)
            }
        }) {
            Button(attrs = {
                style {
                    width(48.px); height(48.px); borderRadius(50.percent); backgroundColor(Color("rgba(255, 255, 255, 0.85)"))
                    property("backdrop-filter", "blur(8px)"); property("-webkit-backdrop-filter", "blur(8px)")
                    border(1.px, LineStyle.Solid, Color("rgba(46, 125, 50, 0.3)")); color(Color("#2E7D32"))
                    property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)"); fontSize(1.3.cssRem)
                    property("cursor", "pointer"); display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center)
                }
                onClick { viewModel.toggleVisibility() }
            }) { Text("🧮") }
        }

        // بدنه اصلی ماشین حساب
        Div(attrs = {
            style {
                position(Position.Absolute); bottom(0.px); left(0.px); width(100.percent)
                property("height", if (state.isFullScreen) "100%" else "auto") 
                backgroundColor(Color("#F5F5F5"))
                property("pointer-events", "auto") 
                property("border-top-left-radius", if (state.isFullScreen) "0px" else "28px")
                property("border-top-right-radius", if (state.isFullScreen) "0px" else "28px")
                property("box-shadow", "0 -12px 32px rgba(0,0,0,0.2)")
                property("transition", "transform 0.3s cubic-bezier(0.2, 0.8, 0.2, 1)")
                property("transform", if (state.isVisible) "translateY(0)" else "translateY(110%)")
                display(DisplayStyle.Flex); flexDirection(FlexDirection.Column)
            }
        }) {
            // هدر ماشین حساب (اضافه شدن دکمه تاریخچه)
            Div(attrs = {
                style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); padding(8.px, 20.px); backgroundColor(Color("transparent")) }
            }) {
                Div(attrs = { style { display(DisplayStyle.Flex); gap(12.px) } }) {
                    Button(attrs = {
                        style { background("none"); border(0.px); fontSize(1.6.cssRem); property("cursor", "pointer"); color(Color("#757575")); padding(0.px, 4.px) }
                        onClick { viewModel.closeCalculator() }
                    }) { Text("×") }
                    
                    // دکمه باز و بسته کردن تاریخچه
                    Button(attrs = {
                        style { background("none"); border(0.px); fontSize(1.2.cssRem); property("cursor", "pointer"); color(if(state.isHistoryOpen) Color("#2E7D32") else Color("#757575")); padding(0.px, 4.px) }
                        onClick { viewModel.toggleHistory() }
                    }) { Text("🕒") }
                }

                Div(attrs = { style { width(40.px); height(5.px); backgroundColor(Color("#E0E0E0")); borderRadius(4.px) } })

                Button(attrs = {
                    style { background("none"); border(0.px); fontSize(1.1.cssRem); property("cursor", "pointer"); color(Color("#757575")); padding(0.px, 4.px) }
                    onClick { viewModel.toggleFullScreen() }
                }) { Text(if (state.isFullScreen) "🗗" else "🗖") }
            }

            // نمایشگر
            Div(attrs = {
                dir(DirType.Ltr) 
                style { 
                    padding(if(state.isFullScreen) 16.px else 10.px, 20.px)
                    property("flex", if (state.isFullScreen && !state.isHistoryOpen) "1" else "none") 
                    display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); justifyContent(JustifyContent.FlexEnd); alignItems(AlignItems.FlexEnd) 
                    property("border-bottom", "1px solid #E0E0E0")
                }
            }) {
                Div(attrs = { style { minHeight(20.px); color(Color("#9E9E9E")); fontSize(0.9.cssRem); fontFamily("monospace"); marginBottom(4.px) } }) {
                    if (state.history.isNotEmpty() && !state.isHistoryOpen) {
                        Text(state.history.last())
                    }
                }
                Div(attrs = { style { width(100.percent); property("overflow-x", "auto"); property("white-space", "nowrap"); textAlign("right"); fontSize(if(state.isFullScreen) 3.cssRem else 2.2.cssRem); fontWeight("bold"); color(if (state.isPostEquals) Color("#2E7D32") else Color("#212121")); fontFamily("monospace"); paddingBottom(4.px) } }) {
                    Text(if (state.expression.isEmpty()) "0" else state.expression)
                }
            }

            // محوطه تعاملی (یا تاریخچه یا کیبورد)
            if (state.isHistoryOpen) {
                // پنل تاریخچه
                Div(attrs = {
                    dir(DirType.Ltr)
                    style { 
                        property("flex", "1") // گرفتن کل فضای باقی‌مانده
                        property("min-height", "300px")
                        property("max-height", if(state.isFullScreen) "none" else "380px")
                        property("overflow-y", "auto")
                        padding(16.px); backgroundColor(Color("#FAFAFA"))
                        display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px)
                    }
                }) {
                    if (state.history.isEmpty()) {
                        Div(attrs = { style { textAlign("center"); color(Color("#9E9E9E")); marginTop(32.px); fontFamily("Vazirmatn", "sans-serif") } }) { Text("تاریخچه خالی است") }
                    } else {
                        state.history.reversed().forEach { item ->
                            val parts = item.split("=")
                            val expr = parts.getOrNull(0)?.trim() ?: ""
                            val res = parts.getOrNull(1)?.trim() ?: ""
                            
                            Div(attrs = {
                                style {
                                    backgroundColor(Color("white")); padding(12.px); borderRadius(12.px)
                                    property("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                                    property("cursor", "pointer")
                                    display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); alignItems(AlignItems.FlexEnd)
                                }
                                onClick { viewModel.loadFromHistory(res) }
                            }) {
                                Div(attrs = { style { color(Color("#757575")); fontSize(1.cssRem); fontFamily("monospace"); marginBottom(4.px) } }) { Text(expr) }
                                Div(attrs = { style { color(Color("#2E7D32")); fontSize(1.4.cssRem); fontWeight("bold"); fontFamily("monospace") } }) { Text("= $res") }
                            }
                        }
                        Button(attrs = {
                            style { marginTop(16.px); padding(12.px); backgroundColor(Color("#FFEBEE")); color(Color("#C62828")); border(0.px); borderRadius(12.px); fontWeight("bold"); fontSize(1.cssRem); property("cursor", "pointer"); fontFamily("Vazirmatn", "sans-serif") }
                            onClick { viewModel.clearHistory() }
                        }) { Text("🗑 پاک کردن تاریخچه") }
                    }
                }
            } else {
                // کیبورد ماشین حساب (حالت عادی)
                Div(attrs = {
                    dir(DirType.Ltr)
                    style { 
                        display(DisplayStyle.Grid); property("grid-template-columns", "repeat(4, 1fr)")
                        gap(if (state.isFullScreen) 12.px else 6.px) 
                        padding(if (state.isFullScreen) 16.px else 10.px)
                        backgroundColor(Color("transparent"))
                    }
                }) {
                    val buttons = listOf(
                        "C", "(", ")", "⌫",
                        "7", "8", "9", "/",
                        "4", "5", "6", "*",
                        "1", "2", "3", "-",
                        "0", ".", "%", "+"
                    )
                    
                    val btnHeight = if (state.isFullScreen) 64.px else 46.px 
                    val btnFontSize = if (state.isFullScreen) 1.5.cssRem else 1.25.cssRem

                    buttons.forEach { label ->
                        val isOperator = label in listOf("/", "*", "-", "+", "%")
                        val isAction = label in listOf("C", "⌫")
                        val isParen = label in listOf("(", ")")
                        
                        val bgColor = when {
                            isAction -> "#FFEBEE"
                            isOperator -> "#E3F2FD" 
                            isParen -> "#EEEEEE"
                            else -> "#FFFFFF"
                        }
                        val textColor = when {
                            isAction -> "#C62828"
                            isOperator -> "#1565C0" 
                            else -> "#212121"
                        }
                        val shadow = if (bgColor == "#FFFFFF") "0 2px 4px rgba(0,0,0,0.05)" else "none"

                        Button(attrs = {
                            style {
                                backgroundColor(Color(bgColor)); color(Color(textColor))
                                borderRadius(12.px); border(0.px); fontSize(btnFontSize)
                                fontWeight(if (bgColor == "#FFFFFF") "normal" else "bold")
                                property("box-shadow", shadow)
                                height(btnHeight) 
                                property("cursor", "pointer")
                            }
                            onClick {
                                when (label) {
                                    "C" -> viewModel.onClearAll()
                                    "⌫" -> viewModel.onBackspace()
                                    else -> viewModel.onInput(label)
                                }
                            }
                        }) { Text(label) }
                    }

                    // دکمه درج
                    Button(attrs = {
                        style {
                            property("grid-column", "span 3") 
                            backgroundColor(Color("#E8F5E9")); color(Color("#1B5E20"))
                            borderRadius(12.px); border(0.px); fontSize(if(state.isFullScreen) 1.1.cssRem else 1.cssRem); fontWeight("bold")
                            height(btnHeight)
                            property("cursor", "pointer")
                            display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center); gap(6.px)
                        }
                        onClick { pasteToActiveField(state.expression, onSuccess = { viewModel.closeCalculator() }) }
                    }) { Text("✔️ درج در فیلد انتخابی") }

                    // دکمه مساوی
                    Button(attrs = {
                        style {
                            property("grid-column", "span 1") 
                            backgroundColor(Color("#2E7D32")); color(Color("white"))
                            borderRadius(12.px); border(0.px); fontSize(if(state.isFullScreen) 1.8.cssRem else 1.5.cssRem); fontWeight("bold")
                            height(btnHeight)
                            property("box-shadow", "0 4px 10px rgba(46, 125, 50, 0.3)")
                            property("cursor", "pointer")
                        }
                        onClick { triggerHapticFeedback(); viewModel.onEquals() }
                    }) { Text("=") }
                }
            }
        }
    }
}
