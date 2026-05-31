package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.presentation.stages.distribution.DistributionStageViewModel
import ir.ghiyas.alimaa.presentation.stages.distribution.PoolDistributionState
import ir.ghiyas.alimaa.presentation.stages.distribution.PoolTarget
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureInputState
import ir.ghiyas.alimaa.domain.strategy.DistributionMode
import ir.ghiyas.alimaa.domain.strategy.DefaultCalculationsRegistry
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet

private fun String.standardizeDigitsLocal(): String {
    var result = this
    result = result.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
        .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
        .replace('٫', '.')
    return result
}

private fun String.toPersianDigitsLocal(): String {
    var result = this
    val english = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
    val persian = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "٫")
    for (i in english.indices) { result = result.replace(english[i], persian[i]) }
    return result
}

@Composable
private fun DistTextInput(label: String, value: String, isNumber: Boolean = false, onValueChange: (String) -> Unit) {
    Div(attrs = { classes(AppStyleSheet.floatingContainer); style { marginBottom(0.px); width(100.percent) } }) {
        Input(type = InputType.Text, attrs = {
            classes(AppStyleSheet.floatingInput)
            classes("floating-input")
            if (isNumber) attr("inputmode", "decimal")
            // اعمال تبدیل به اعداد فارسی
            value(value.toPersianDigitsLocal())
            onInput { event -> 
                val finalVal = if (isNumber) event.value.standardizeDigitsLocal() else event.value
                onValueChange(finalVal) 
            }
            placeholder(" ") 
        })
        Label(attrs = { classes(AppStyleSheet.floatingLabel); classes("floating-label") }) { Text(label) }
    }
}

@Composable
fun DistributionStageScreen(
    viewModel: DistributionStageViewModel,
    agricultureInput: AgricultureInputState
) {
    val state by viewModel.state.collectAsState()

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
        }) { Text("مرحله چهارم: موتور تسهیم قیاس") }

        if (agricultureInput.isNimehkari) {
            val p1Name = if (agricultureInput.partner1Name.isNotBlank()) agricultureInput.partner1Name else "شریک ۱"
            val p2Name = if (agricultureInput.partner2Name.isNotBlank()) agricultureInput.partner2Name else "شریک ۲"
            
            PoolSettingsCard("تنظیمات سهم $p1Name", PoolTarget.PARTNER_1, state.partner1PoolState, viewModel)
            PoolSettingsCard("تنظیمات سهم $p2Name", PoolTarget.PARTNER_2, state.partner2PoolState, viewModel)
        } else {
            PoolSettingsCard("تنظیمات تسهیم کل بار", PoolTarget.MAIN, state.mainPoolState, viewModel)
        }
    }
}

@Composable
fun PoolSettingsCard(
    title: String,
    target: PoolTarget,
    state: PoolDistributionState,
    viewModel: DistributionStageViewModel
) {
    Div(attrs = {
        style {
            marginBottom(24.px)
            padding(16.px)
            border(1.px, LineStyle.Solid, Color("#AED581"))
            borderRadius(12.px)
            backgroundColor(Color("#F1F8E9"))
        }
    }) {
        H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#1B5E20")) } }) { Text(title) }

        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexWrap(FlexWrap.Wrap)
                gap(8.px)
                marginBottom(24.px)
            }
        }) {
            val modes = listOf(
                DistributionMode.MODE_B_SIMPLE to "بر اساس نفر",
                DistributionMode.MODE_A_NO_BREAKDOWN to "بدون خرد کردن",
                DistributionMode.MODE_C_GHIYAS to "بر اساس قیاس",
                DistributionMode.MODE_DEFAULT_MAKER to "پیش‌فرض سازنده"
            )

            modes.forEach { (mode, label) ->
                val isActive = state.mode == mode
                Button(attrs = {
                    style {
                        flex(1)
                        minWidth(120.px)
                        padding(10.px)
                        borderRadius(8.px)
                        fontWeight(if (isActive) "bold" else "normal")
                        color(if (isActive) Color("white") else Color("#2E7D32"))
                        backgroundColor(if (isActive) Color("#2E7D32") else Color("white"))
                        property("border", "1px solid #2E7D32")
                        property("cursor", "pointer")
                        property("transition", "all 0.2s")
                    }
                    onClick { viewModel.updateMode(target, mode) }
                }) { Text(label) }
            }
        }

        Div(attrs = {
            style {
                padding(16.px)
                backgroundColor(Color("white"))
                borderRadius(8.px)
                property("border", "1px dashed #C5E1A5")
            }
        }) {
            when (state.mode) {
                
                DistributionMode.MODE_A_NO_BREAKDOWN -> {
                    // نام هوشمند و کلمه‌ی (اختیاری) اضافه شد
                    DistTextInput("نام گروه یا شخص گیرنده (اختیاری)", state.groupName, false) { viewModel.updateGroupName(target, it) }
                    P(attrs = { style { fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) {
                        Text("در این حالت کل سهم این بخش بدون تغییر به نام وارد شده اختصاص می‌یابد.")
                    }
                }
                
                DistributionMode.MODE_B_SIMPLE -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("تنظیمات محاسبه (بر اساس نفر)") }
                    
                    DistTextInput("تعداد نفرات", state.peopleCountInput, true) { viewModel.updatePeopleCount(target, it) }
                    
                    P(attrs = { style { fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px); marginBottom(16.px) } }) {
                        Text("مثلا ۳.۵ - یعنی ۳ پسر یک دختر. محاسبه هم بر اساس ارقامی مثل ۴ و هم ۴.۵ یعنی ۴ پسر و یک دختر (پسر مساوی ۱، دختر مساوی ۰.۵) صورت می‌گیرد!")
                    }

                    val count = state.peopleCountInput.toDoubleOrNull()
                    val hasDecimal = count != null && (count % 1.0 != 0.0)

                    if (!hasDecimal) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontWeight("bold") } }) {
                            Input(type = InputType.Checkbox, attrs = {
                                checked(state.isBoyGirlSplit)
                                onChange { viewModel.updateIsBoyGirlSplit(target, it.value) }
                                style { marginRight(12.px); width(20.px); height(20.px) }
                            })
                            Text("تسهیم پسر و دختری؟")
                        }
                    }
                }
                
                DistributionMode.MODE_C_GHIYAS -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("تسهیم بر اساس قیاس (وزن اختصاصی)") }
                    
                    state.shareholders.forEachIndexed { index, shareholder ->
                        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(12.px) } }) {
                            Div(attrs = { style { flex(2) } }) {
                                DistTextInput("نام سهام‌دار", shareholder.name, false) { name -> viewModel.updateShareholder(target, index, name, shareholder.ghiyasInput) }
                            }
                            Div(attrs = { style { flex(1) } }) {
                                DistTextInput("مقدار قیاس", shareholder.ghiyasInput, true) { ghiyas -> viewModel.updateShareholder(target, index, shareholder.name, ghiyas) }
                            }
                            if (state.shareholders.size > 1) {
                                Button(attrs = {
                                    style {
                                        backgroundColor(Color("#D32F2F")); color(Color("white"))
                                        border(0.px); borderRadius(4.px); padding(10.px, 16.px)
                                        fontWeight("bold"); property("cursor", "pointer")
                                    }
                                    onClick { viewModel.removeShareholder(target, index) }
                                }) { Text("-") }
                            }
                        }
                    }
                    
                    Button(attrs = {
                        style {
                            width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32"))
                            property("border", "2px dashed #4CAF50"); borderRadius(8.px); padding(12.px)
                            fontWeight("bold"); property("cursor", "pointer"); marginTop(8.px)
                        }
                        onClick { viewModel.addShareholder(target) }
                    }) { Text("+ افزودن سهام‌دار جدید") }
                }

                DistributionMode.MODE_DEFAULT_MAKER -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("انتخاب از محاسبات آماده") }
                    
                    Select(attrs = {
                        style {
                            width(100.percent); padding(12.px); borderRadius(8.px)
                            property("border", "1px solid #BDBDBD"); fontSize(1.cssRem)
                            fontFamily("Vazirmatn", "system-ui", "sans-serif")
                        }
                        onChange { event -> viewModel.updateDefaultStrategy(target, event.value ?: "") }
                    }) {
                        Option(value = "", attrs = { if (state.defaultStrategyTitle.isEmpty()) selected(); disabled() }) {
                            Text("محاسبات پیش‌فرض سازنده را انتخاب کنید...")
                        }
                        
                        DefaultCalculationsRegistry.strategies.forEach { strategy ->
                            Option(value = strategy.title, attrs = { if (state.defaultStrategyTitle == strategy.title) selected() }) {
                                Text(strategy.title)
                            }
                        }
                    }
                }
            }
        }
    }
}
