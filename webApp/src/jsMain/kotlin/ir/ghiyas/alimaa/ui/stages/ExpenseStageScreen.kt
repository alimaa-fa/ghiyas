package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseStageViewModel
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseCategoryState
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet

private fun String.standardizeDigits(): String {
    var result = this
    result = result.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
        .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
        .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
        .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
        .replace('٫', '.').replace('/', '.')
    return result
}

fun String.toPersianDigits(): String {
    var result = this
    val english = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
    val persian = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "٫")
    for (i in english.indices) { result = result.replace(english[i], persian[i]) }
    return result
}

@Composable
fun ExpenseStageScreen(
    viewModel: ExpenseStageViewModel,
    baseUnit: String,
    calculationName: String,
    totalInputAmount: String
) {
    val inputState by viewModel.inputState.collectAsState()
    val snapshot by viewModel.snapshot.collectAsState()

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
        }) { Text("مرحله دوم: خرجکرد") }

        // Main Checkbox
        Label(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                marginBottom(24.px)
                cursor("pointer")
                fontWeight("bold")
                fontSize(1.1.cssRem)
            }
        }) {
            Input(type = InputType.Checkbox, attrs = {
                checked(inputState.isCalculated)
                onChange { viewModel.updateIsCalculated(it.value) }
                style { marginRight(12.px); width(24.px); height(24.px) }
            })
            Text("آیا خرجکرد محاسبه شود؟")
        }

        if (inputState.isCalculated) {
            // Global Fixed
            Div(attrs = {
                style {
                    marginBottom(24.px)
                    padding(16.px)
                    border(1.px, LineStyle.Solid, Color("#E0E0E0"))
                    borderRadius(8.px)
                    backgroundColor(Color("#FAFAFA"))
                }
            }) {
                H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("خرج کل به صورت مقطوع") }
                CustomNumberInput("مبلغ مقطوع کل", inputState.globalFixedExpense_Input) { v -> viewModel.updateGlobalFixedExpense(v) }
            }

            // Tekani
            ExpenseCategoryView(
                title = "تکانی",
                mizanLabel = "میزان تکانی",
                state = inputState.tekani,
                onStateChange = { viewModel.updateTekani { _ -> it } }
            )

            // Jamkoni
            ExpenseCategoryView(
                title = "جمع‌کنی",
                mizanLabel = "میزان جمع‌کنی",
                state = inputState.jamkoni,
                onStateChange = { viewModel.updateJamkoni { _ -> it } }
            )

            // Kooleh
            ExpenseCategoryView(
                title = "کوله‌کشی",
                mizanLabel = "میزان کوله‌کشی",
                state = inputState.kooleh,
                onStateChange = { viewModel.updateKooleh { _ -> it } }
            )

            // Sarkari (Custom)
            Div(attrs = {
                style {
                    marginBottom(32.px)
                    padding(16.px)
                    border(1.px, LineStyle.Solid, Color("#E0E0E0"))
                    borderRadius(8.px)
                }
            }) {
                H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("سرکاری") }

                // Checkboxes for Sarkari
                Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(12.px); marginBottom(16.px) } }) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer") } }) {
                        Input(type = InputType.Checkbox, attrs = {
                            checked(inputState.sarkari.isFixed)
                            onChange { checked -> viewModel.updateSarkari { it.copy(isFixed = checked.value) } }
                            style { marginRight(8.px) }
                        })
                        Text("عدد، مقطوع و ثابت هست؟")
                    }
                    
                    if (!inputState.sarkari.isFixed) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer") } }) {
                            Input(type = InputType.Checkbox, attrs = {
                                checked(inputState.sarkari.hasExtra)
                                onChange { checked -> viewModel.updateSarkari { it.copy(hasExtra = checked.value) } }
                                style { marginRight(8.px) }
                            })
                            Text("آیا هزینه و خرجکرد اضافی (ثابت) دارد؟")
                        }
                        
                        if (inputState.sarkari.hasExtra) {
                            CustomNumberInput("مبلغ اضافه", inputState.sarkari.extraValue) { v -> viewModel.updateSarkari { it.copy(extraValue = v) } }
                        }
                    }

                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer") } }) {
                        Input(type = InputType.Checkbox, attrs = {
                            checked(inputState.sarkari.isHalfKari)
                            onChange { checked -> viewModel.updateSarkari { it.copy(isHalfKari = checked.value) } }
                            style { marginRight(8.px) }
                        })
                        Text("نیمه‌کاری است؟ (دو گروه مجزا)")
                    }
                }

                // Row for Mizan and Tedad
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        flexDirection(FlexDirection.Row)
                        gap(12.px)
                        width(100.percent)
                    }
                }) {
                    Div(attrs = { style { flex(1) } }) {
                        CustomNumberInput("میزان سرکاری", inputState.sarkari.mizan) { v -> viewModel.updateSarkari { it.copy(mizan = v) } }
                    }
                    if (!inputState.sarkari.isHalfKari) {
                        Div(attrs = { style { flex(1) } }) {
                            CustomNumberInput("تعداد نفرات", inputState.sarkari.tedad) { v -> viewModel.updateSarkari { it.copy(tedad = v) } }
                        }
                    }
                }
                
                if (inputState.sarkari.isHalfKari) {
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            flexDirection(FlexDirection.Row)
                            gap(12.px)
                            width(100.percent)
                            marginTop(16.px)
                        }
                    }) {
                        Div(attrs = { style { flex(1) } }) {
                            CustomNumberInput("تعداد گروه ۱", inputState.sarkari.group1Count) { v -> viewModel.updateSarkari { it.copy(group1Count = v) } }
                        }
                        Div(attrs = { style { flex(1) } }) {
                            CustomNumberInput("تعداد گروه ۲", inputState.sarkari.group2Count) { v -> viewModel.updateSarkari { it.copy(group2Count = v) } }
                        }
                    }
                }
            }

            // Global Extra
            Div(attrs = {
                style {
                    marginBottom(32.px)
                    padding(16.px)
                    border(1.px, LineStyle.Solid, Color("#E0E0E0"))
                    borderRadius(8.px)
                    backgroundColor(Color("#FAFAFA"))
                }
            }) {
                H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("خرج اضافی متفرقه") }
                CustomNumberInput("مبلغ اضافی متفرقه", inputState.extraExpense_Input) { v -> viewModel.updateExtraExpense(v) }
            }
        }

        // Calculate Button
        Button(attrs = {
            style {
                width(100.percent)
                padding(16.px)
                backgroundColor(Color("#2E7D32"))
                color(Color("white"))
                border(0.px)
                borderRadius(8.px)
                fontSize(1.1.cssRem)
                fontWeight("bold")
                cursor("pointer")
                marginTop(16.px)
            }
            onClick {
                val yearOptions = kotlin.js.json("year" to "numeric").unsafeCast<kotlin.js.Date.LocaleOptions>()
                val rawPersianYear = kotlin.js.Date().toLocaleDateString("fa-IR", yearOptions).trim()
                val currentUnixTimestamp = kotlin.js.Date().getTime().toLong()

                viewModel.calculateAndSnapshot(calculationName, baseUnit, rawPersianYear, currentUnixTimestamp)
            }
        }) {
            Text("محاسبه کن")
        }

        // Results Card
        if (snapshot != null) {
            Div(attrs = {
                style {
                    marginTop(32.px)
                    padding(24.px)
                    backgroundColor(Color("#F1F8E9"))
                    borderRadius(12.px)
                    border(1.px, LineStyle.Solid, Color("#C5E1A5"))
                }
            }) {
                Div(attrs = {
                    style {
                        backgroundColor(Color("#F5F5F5"))
                        color(Color("#1B5E20"))
                        padding(14.px, 24.px)
                        borderRadius(8.px)
                        textAlign("center")
                        fontWeight("bold")
                        fontSize(1.25.cssRem)
                        marginBottom(20.px)
                        property("border", "1px solid #C8E6C9")
                        property("border-left", "5px solid #2E7D32")
                    }
                }) { Text("نتایج محاسبات نهایی خرجکرد") }
                
                val dateTimeOptions = kotlin.js.json("year" to "numeric", "month" to "long", "day" to "numeric", "hour" to "2-digit", "minute" to "2-digit").unsafeCast<kotlin.js.Date.LocaleOptions>()
                val liveTimeString = kotlin.js.Date(snapshot!!.timestamp).toLocaleString("fa-IR", dateTimeOptions)

                Div(attrs = { style { marginBottom(16.px); paddingBottom(16.px); property("border-bottom", "2px dashed #C8E6C9") } }) {
                    P(attrs = { style { margin(0.px); fontWeight("bold"); color(Color("#2E7D32")); fontSize(1.1.cssRem) } }) {
                        Text("نام محاسبه: ${snapshot!!.calculationName}")
                    }
                    P(attrs = { style { margin(8.px, 0.px, 0.px, 0.px); color(Color("#424242")); fontSize(0.95.cssRem) } }) {
                        val amt = snapshot!!.inputAmount.value.toString()
                        Text("کل مقدار اولیه: $amt ${snapshot!!.baseUnit}")
                    }
                    P(attrs = { style { margin(8.px, 0.px, 0.px, 0.px); color(Color("#757575")); fontSize(0.85.cssRem) } }) {
                        Text("زمان ثبت: $liveTimeString")
                    }
                }
                
                snapshot!!.expensesResults.forEach { item ->
                    val rawValue = item.value.value

                    val roundedValue = when {
                        snapshot!!.baseUnit.contains("کیلو") || snapshot!!.baseUnit.contains("گرم") -> {
                            (kotlin.math.round(rawValue * 1000.0) / 1000.0).toString()
                        }
                        snapshot!!.baseUnit.contains("متر") || snapshot!!.baseUnit.contains("سانت") || snapshot!!.baseUnit.contains("ساعت") -> {
                            (kotlin.math.round(rawValue * 100.0) / 100.0).toString()
                        }
                        snapshot!!.baseUnit.contains("تومان") || snapshot!!.baseUnit.contains("ریال") -> {
                            kotlin.math.round(rawValue).toLong().toString()
                        }
                        else -> {
                            val shifted = rawValue * 10.0
                            val truncated = kotlin.math.floor(shifted)
                            val remainder = shifted - truncated
                            val finalShifted = if (remainder >= 0.79) truncated + 1.0 else truncated
                            (finalShifted / 10.0).toString()
                        }
                    }.removeSuffix(".0")

                    fun formatNumber(numStr: String): String {
                        var res = numStr
                        val eng = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")
                        val per = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹", "٫")
                        for (i in eng.indices) { res = res.replace(eng[i], per[i]) }
                        return res
                    }

                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            justifyContent(JustifyContent.SpaceBetween)
                            padding(12.px, 0.px)
                            property("border-bottom", "1px dashed #AED581")
                            fontSize(1.1.cssRem)
                            color(Color("#33691E"))
                        }
                    }) {
                        Span { Text(item.label) }
                        Span(attrs = { style { fontWeight("bold") } }) { 
                            Span(attrs = {
                                style {
                                    fontFamily("Vazirmatn", "system-ui", "sans-serif")
                                    fontWeight("bold")
                                    property("direction", "ltr")
                                    display(DisplayStyle.InlineBlock)
                                }
                            }) {
                                Text(formatNumber(roundedValue))
                            }
                            Text(" ${snapshot!!.baseUnit}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCategoryView(
    title: String,
    mizanLabel: String,
    state: ExpenseCategoryState,
    onStateChange: (ExpenseCategoryState) -> Unit
) {
    Div(attrs = {
        style {
            marginBottom(32.px)
            padding(16.px)
            border(1.px, LineStyle.Solid, Color("#E0E0E0"))
            borderRadius(8.px)
        }
    }) {
        H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text(title) }

        Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(12.px); marginBottom(16.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer") } }) {
                Input(type = InputType.Checkbox, attrs = {
                    checked(state.isFixed)
                    onChange { onStateChange(state.copy(isFixed = it.value)) }
                    style { marginRight(8.px) }
                })
                Text("عدد، مقطوع و ثابت هست؟")
            }
            
            if (!state.isFixed) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer") } }) {
                    Input(type = InputType.Checkbox, attrs = {
                        checked(state.hasExtra)
                        onChange { onStateChange(state.copy(hasExtra = it.value)) }
                        style { marginRight(8.px) }
                    })
                    Text("آیا هزینه و خرجکرد اضافی (ثابت) دارد؟")
                }
                
                if (state.hasExtra) {
                    CustomNumberInput("مبلغ اضافه", state.extraValue) { v -> onStateChange(state.copy(extraValue = v)) }
                }
            }
        }

        // Side-by-Side Flexbox
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                gap(12.px)
                width(100.percent)
            }
        }) {
            Div(attrs = { style { flex(1) } }) {
                CustomNumberInput(mizanLabel, state.mizan) { v -> onStateChange(state.copy(mizan = v)) }
            }
            Div(attrs = { style { flex(1) } }) {
                CustomNumberInput("تعداد نفرات", state.tedad) { v -> onStateChange(state.copy(tedad = v)) }
            }
        }
    }
}

@Composable
fun CustomNumberInput(label: String, value: String, onValueChange: (String) -> Unit) {
    Div(attrs = { classes(AppStyleSheet.floatingContainer); style { marginBottom(0.px) } }) {
        Input(type = InputType.Text, attrs = {
            classes(AppStyleSheet.floatingInput)
            classes("floating-input")
            attr("inputmode", "decimal")
            value(value.toPersianDigits())
            onInput { event -> onValueChange(event.value.standardizeDigits()) }
            placeholder(" ") 
        })
        Label(attrs = { 
            classes(AppStyleSheet.floatingLabel)
            classes("floating-label")
        }) { Text(label) }
    }
}