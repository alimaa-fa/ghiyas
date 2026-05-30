package ir.ghiyas.alimaa.ui.stages

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.*
import ir.ghiyas.alimaa.domain.models.UnitType
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import ir.ghiyas.alimaa.presentation.stages.input.InputStageViewModel

private fun String.standardizeDigits(): String {
    var result = this
    result = result.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
        .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
        .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
        .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
        .replace('٫', '.').replace('/', '.')
    return result
}


@Composable
fun InputStageScreen(
    viewModel: InputStageViewModel,
    onClearRequested: Boolean,
    onClearComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(onClearRequested) {
        if (onClearRequested) {
            viewModel.clearForm()
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
                color(Color("#2E7D32"))
                fontWeight("bold")
                fontSize(1.2.cssRem)
                property("border-bottom", "2px solid #2E7D32")
                paddingBottom(8.px)
                marginBottom(24.px)
                display(DisplayStyle.InlineBlock)
            }
        }) { Text("اطلاعات اولیه") }

        // فیلد ۱: اسم محاسبه
        Div(attrs = { classes(AppStyleSheet.floatingContainer) }) {
            Input(type = InputType.Text, attrs = {
                classes(AppStyleSheet.floatingInput)
                classes("floating-input") // اسم ثابت برای پیدا شدن توسط CSS
                value(state.calculationName)
                onInput { viewModel.onCalculationNameChange(it.value) }
                placeholder(" ") 
            })
            Label(attrs = { 
                classes(AppStyleSheet.floatingLabel)
                classes("floating-label") // اسم ثابت
            }) { Text("اسم محاسبه (اختیاری)") }
        }

        // فیلد ۲: دراپ‌داون نوع واحد
        Div(attrs = { style { position(Position.Relative); marginBottom(24.px) } }) {
            Select(attrs = {
                classes(AppStyleSheet.floatingInput)
                classes("floating-input")
                onChange { event ->
                    event.value?.let { selectedName ->
                        UnitType.entries.find { it.name == selectedName }?.let { viewModel.onUnitTypeChange(it) }
                    }
                }
            }) {
                UnitType.getOrderedValues().forEach { type ->
                    Option(value = type.name, attrs = {
                        if (type == state.unitType) selected()
                    }) { Text(type.displayName) }
                }
            }
            Label(attrs = {
                style {
                    position(Position.Absolute); right(12.px); top((-10).px)
                    fontSize(12.px); color(Color("#4CAF50")); backgroundColor(Color("white"))
                    padding(0.px, 4.px); fontWeight("bold")
                }
            }) { Text("نوع واحد") }
        }

        // فیلد ۳: مقدار کل
        Div(attrs = { classes(AppStyleSheet.floatingContainer) }) {
            Input(type = InputType.Text, attrs = {
                classes(AppStyleSheet.floatingInput)
                classes("floating-input") // اسم ثابت
                attr("inputmode", "decimal")
                value(state.totalAmount.toPersianDigits())
                onInput { viewModel.onTotalAmountChange(it.value.standardizeDigits()) }
                placeholder(" ") 
            })
            Label(attrs = { 
                classes(AppStyleSheet.floatingLabel)
                classes("floating-label") // اسم ثابت
            }) { Text(state.unitType.dynamicLabel) }
        }
    }
}