package ir.ghias.alimaa.ui.stages

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.*
import ir.ghias.alimaa.domain.UnitType

@Composable
fun InputStageScreen(
    onClearRequested: Boolean,
    onClearComplete: () -> Unit
) {
    var calcName by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(UnitType.HAND_PIECE) }
    var totalAmount by remember { mutableStateOf("") }

    LaunchedEffect(onClearRequested) {
        if (onClearRequested) {
            calcName = ""
            selectedUnit = UnitType.HAND_PIECE
            totalAmount = ""
            onClearComplete()
        }
    }

    Div(attrs = {
        style {
            backgroundColor(Color("white"))
            borderRadius(16.px)
            padding(24.px)
            margin(16.px)
            property("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
        }
    }) {
        H3(attrs = {
            style {
                marginTop(0.px)
                marginBottom(20.px)
                color(Color("#212121"))
                fontSize(18.px)
                fontWeight("bold")
            }
        }) {
            Text("اطلاعات اولیه")
        }

        // فیلد ۱: اسم محاسبه
        Div(attrs = { style { position(Position.Relative); marginBottom(20.px) } }) {
            Input(type = InputType.Text, attrs = {
                value(calcName)
                onInput { calcName = it.value }
                placeholder("اسم محاسبه (اختیاری)")
                style {
                    width(100.percent); padding(12.px)
                    border(1.px, LineStyle.Solid, Color("#BDBDBD"))
                    borderRadius(8.px); fontSize(16.px)
                    property("box-sizing", "border-box")
                    fontFamily("inherit")
                }
            })
        }

        // فیلد ۲: دراپ‌داون نوع واحد
        Div(attrs = { style { marginBottom(20.px) } }) {
            // حل باگ بزرگ: افزودن صریح attrs =
            Label(attrs = {
                style {
                    display(DisplayStyle.Block); marginBottom(8.px)
                    color(Color("#757575")); fontSize(14.px)
                }
            }) { Text("نوع واحد") }
            
            Select(attrs = {
                style {
                    width(100.percent); padding(12.px)
                    border(1.px, LineStyle.Solid, Color("#BDBDBD"))
                    borderRadius(8.px); fontSize(16.px)
                    property("box-sizing", "border-box")
                    fontFamily("inherit")
                }
                onChange { event ->
                    event.value?.let { selectedName ->
                        UnitType.entries.find { it.name == selectedName }?.let { 
                            selectedUnit = it 
                        }
                    }
                }
            }) {
                UnitType.getOrderedValues().forEach { type ->
                    Option(value = type.name, attrs = {
                        if (type == selectedUnit) selected()
                    }) {
                        Text(type.displayName)
                    }
                }
            }
        }

        // فیلد ۳: مقدار کل
        Div(attrs = { style { marginBottom(12.px) } }) {
            // حل باگ بزرگ: افزودن صریح attrs =
            Label(attrs = {
                style {
                    display(DisplayStyle.Block); marginBottom(8.px)
                    color(Color("#757575")); fontSize(14.px)
                }
            }) { 
                Text(selectedUnit.dynamicLabel) 
            }
            
            Input(type = InputType.Number, attrs = {
                value(totalAmount)
                onInput { totalAmount = it.value?.toString() ?: "" }
                style {
                    width(100.percent); padding(12.px)
                    border(1.px, LineStyle.Solid, Color("#BDBDBD"))
                    borderRadius(8.px); fontSize(16.px)
                    property("box-sizing", "border-box")
                    fontFamily("inherit")
                }
            })
        }
    }
}
