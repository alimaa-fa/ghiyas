package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureStageViewModel
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet

@Composable
fun CustomTextInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Div(attrs = { 
        classes(AppStyleSheet.floatingContainer)
        style { 
            marginBottom(0.px)
            position(Position.Relative)
            width(100.percent)
        } 
    }) {
        Input(type = InputType.Text, attrs = {
            classes(AppStyleSheet.floatingInput)
            classes("floating-input") 
            value(value)
            onInput { event -> onValueChange(event.value) }
            placeholder(" ") 
            style {
                // افزایش ارتفاع و پدینگ برای جا دادن لیبل‌های دو خطی بدون سایه انداختن روی متن
                property("min-height", "64px")
                padding(32.px, 12.px, 8.px, 12.px) 
                width(100.percent)
                boxSizing("border-box")
            }
        })
        Label(attrs = { 
            classes(AppStyleSheet.floatingLabel)
            classes("floating-label")
            style {
                // حذف سه‌نقطه و اجازه شکستن آزادانه متن به خط دوم طبق دستور شما
                whiteSpace("normal") 
                property("word-break", "break-word")
                property("max-width", "100%")
            }
        }) { Text(label) }
    }
}

@Composable
fun AgricultureStageScreen(viewModel: AgricultureStageViewModel) {
    val inputState by viewModel.inputState.collectAsState()

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
                color(Color("#2E7D32"))
                fontWeight("bold")
                fontSize(1.2.cssRem)
                property("border-bottom", "2px solid #2E7D32")
                paddingBottom(8.px)
                marginBottom(24.px)
                display(DisplayStyle.InlineBlock)
            }
        }) { Text("مرحله سوم: کشاورزی و نیمه‌کاری") }

        // 1. سهم کشاورزی
        Div(attrs = { style { marginBottom(24.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); borderRadius(8.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontWeight("bold") } }) {
                Input(type = InputType.Checkbox, attrs = {
                    checked(inputState.isKeshavarzi)
                    onChange { viewModel.updateIsKeshavarzi(it.value) }
                    style { marginRight(12.px); width(20.px); height(20.px) }
                })
                Text("کشاورزی حساب شود؟") 
            }

            if (inputState.isKeshavarzi) {
                Div(attrs = { style { marginTop(16.px) } }) {
                    CustomNumberInput("نسبت سهم کشاورز", inputState.keshavarziRatioInput) { v -> viewModel.updateKeshavarziRatio(v) }
                    Span(attrs = { style { fontSize(0.85.cssRem); color(Color("#757575")) } }) { Text("مثال: ۴ یعنی ۱/۴ کل بار کسر می‌شود") }
                }
            }
        }

        // 2. سهم نیمه‌کاری
        Div(attrs = { style { marginBottom(8.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); borderRadius(8.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontWeight("bold") } }) {
                Input(type = InputType.Checkbox, attrs = {
                    checked(inputState.isNimehkari)
                    onChange { viewModel.updateIsNimehkari(it.value) }
                    style { marginRight(12.px); width(20.px); height(20.px) }
                })
                Text("بار نیمه‌کاری است؟ (نصف می‌شود)")
            }

            if (inputState.isNimehkari) {
                Div(attrs = { style { marginTop(16.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Row); gap(12.px) } }) {
                    Div(attrs = { style { flex(1); property("min-width", "0") } }) {
                        CustomTextInput("نام شریک ۱ (اختیاری)", inputState.partner1Name) { v -> viewModel.updatePartner1Name(v) }
                    }
                    Div(attrs = { style { flex(1); property("min-width", "0") } }) {
                        CustomTextInput("نام شریک ۲ (اختیاری)", inputState.partner2Name) { v -> viewModel.updatePartner2Name(v) }
                    }
                }
            }
        }
    }
}
