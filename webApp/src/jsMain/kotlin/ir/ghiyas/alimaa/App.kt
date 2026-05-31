package ir.ghiyas.alimaa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.ui.components.GhiyasTopAppBar
import ir.ghiyas.alimaa.ui.components.HeroBanner
import ir.ghiyas.alimaa.ui.stages.InputStageScreen
import ir.ghiyas.alimaa.ui.stages.ExpenseStageScreen
import ir.ghiyas.alimaa.ui.stages.AgricultureStageScreen
import ir.ghiyas.alimaa.ui.stages.DistributionStageScreen
import ir.ghiyas.alimaa.ui.stages.HistoryScreen
import ir.ghiyas.alimaa.presentation.stages.input.InputStageViewModel
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseStageViewModel
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureStageViewModel
import ir.ghiyas.alimaa.presentation.stages.distribution.DistributionStageViewModel
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat

@Composable
fun ResultRowItem(label: String, rawValue: Double, baseUnit: String, isHighlight: Boolean = false) {
    val textColor = if (isHighlight) Color("#BF360C") else Color("#33691E")
    
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            padding(12.px, 0.px)
            property("border-bottom", "1px dashed #AED581")
            fontSize(if (isHighlight) 1.15.cssRem else 1.1.cssRem)
            color(textColor)
        }
    }) {
        Span(attrs = { style { flex(1); if(isHighlight) fontWeight("bold") } }) { Text(label) }
        Span(attrs = { style { fontWeight("bold"); flex(1); textAlign("left") } }) { 
            Span(attrs = {
                style { 
                    fontFamily("Vazirmatn", "system-ui", "sans-serif")
                    fontWeight("bold")
                    property("direction", "ltr")
                    display(DisplayStyle.InlineBlock) 
                }
            }) { Text(rawValue.toGhiyasFormat(baseUnit)) } // استفاده از متد جدید تشخیص هوشمند اعشار و گردکردن
            Text(" $baseUnit")
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("main") }
    var clearFormRequested by remember { mutableStateOf(false) }

    val inputViewModel = remember { InputStageViewModel() }
    val expenseViewModel = remember { ExpenseStageViewModel() }
    val agricultureViewModel = remember { AgricultureStageViewModel() } 
    val distributionViewModel = remember { DistributionStageViewModel() }
    
    val inputState by inputViewModel.state.collectAsState()
    val snapshot by expenseViewModel.snapshot.collectAsState()
    
    val agricultureInputState by agricultureViewModel.inputState.collectAsState()

    LaunchedEffect(inputState.totalAmount) {
        val amount = if (inputState.totalAmount.isNotBlank()) inputState.totalAmount else "0"
        expenseViewModel.setTotalWalnuts(WalnutUnit.fromInput(amount))
    }

    Style(AppStyleSheet)

    Div(attrs = {
        dir(DirType.Rtl)
        style {
            property("margin", "0 auto")
            maxWidth(600.px)
            width(100.percent)
            minHeight(100.vh)
            backgroundColor(Color("#F5F5F5"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            fontFamily("Vazirmatn", "system-ui", "-apple-system", "sans-serif")
            property("box-shadow", "0 0 15px rgba(0,0,0,0.05)")
        }
    }) {
        GhiyasTopAppBar(
            onClearClick = { 
                clearFormRequested = true
                expenseViewModel.clearForm()
                agricultureViewModel.clearForm() 
                distributionViewModel.clearForm()
            },
            onHistoryClick = { currentScreen = "history" },
            onShareClick = null
        )
        
        Div(attrs = {
            style {
                property("flex", "1")
                property("overflow-y", "auto")
                paddingBottom(32.px)
            }
        }) {
            if (currentScreen == "main") {
                HeroBanner()
                
                InputStageScreen(viewModel = inputViewModel, onClearRequested = clearFormRequested, onClearComplete = { clearFormRequested = false })
                ExpenseStageScreen(viewModel = expenseViewModel)
                AgricultureStageScreen(viewModel = agricultureViewModel)
                DistributionStageScreen(viewModel = distributionViewModel, agricultureInput = agricultureInputState)

                Button(attrs = {
                    style {
                        width(100.percent)
                        padding(16.px)
                        property("margin", "16px 0px")
                        backgroundColor(Color("#2E7D32"))
                        color(Color("white"))
                        border(0.px)
                        borderRadius(8.px)
                        fontSize(1.1.cssRem)
                        fontWeight("bold")
                        property("cursor", "pointer")
                    }
                    onClick {
                        val yearOptions = kotlin.js.json("year" to "numeric").unsafeCast<kotlin.js.Date.LocaleOptions>()
                        val rawPersianYear = kotlin.js.Date().toLocaleDateString("fa-IR", yearOptions).trim()
                        
                        expenseViewModel.calculateAndSnapshot(
                            calculationName = inputState.calculationName, 
                            baseUnit = inputState.unitType.displayName, 
                            currentYear = rawPersianYear, 
                            timestampLong = kotlin.js.Date().getTime().toLong(),
                            agricultureInput = agricultureInputState,
                            distributionInput = distributionViewModel.state.value 
                        )

                        expenseViewModel.snapshot.value?.let { newRecord ->
                            ir.ghiyas.alimaa.data.LocalStorageRepository.saveRecord(newRecord)
                        }
                    }
                }) { Text("محاسبه کل مراحل") }

                if (snapshot != null) {
                    Div(attrs = { 
                        style { 
                            property("margin", "16px")
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
                        }) { Text("نتایج محاسبات نهایی قیاس") }
                        
                        val dateTimeOptions = kotlin.js.json("year" to "numeric", "month" to "long", "day" to "numeric", "hour" to "2-digit", "minute" to "2-digit").unsafeCast<kotlin.js.Date.LocaleOptions>()
                        val liveTimeString = kotlin.js.Date(snapshot!!.timestamp).toLocaleString("fa-IR", dateTimeOptions)

                        Div(attrs = { 
                            style { 
                                marginBottom(16.px)
                                paddingBottom(16.px)
                                property("border-bottom", "2px dashed #C8E6C9") 
                            } 
                        }) {
                            P(attrs = { style { margin(0.px); fontWeight("bold"); color(Color("#2E7D32")); fontSize(1.1.cssRem) } }) { Text("نام محاسبه: ${snapshot!!.calculationName}") }
                            P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text("کل مقدار اولیه: ${snapshot!!.inputAmount.value.toGhiyasFormat(snapshot!!.baseUnit)} ${snapshot!!.baseUnit}") }
                            P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#757575")); fontSize(0.85.cssRem) } }) { Text("زمان ثبت: $liveTimeString") }
                        }
                        
                        if (snapshot!!.expensesResults.isNotEmpty()) {
                            snapshot!!.expensesResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                        }

                        if (snapshot!!.agricultureResults.isNotEmpty() || snapshot!!.nimehkariResults.isNotEmpty()) {
                            Div(attrs = { style { marginTop(16.px); paddingTop(16.px); property("border-top", "3px solid #AED581") } }) {
                                H4(attrs = { style { color(Color("#2E7D32")); property("margin", "0px 0px 12px 0px") } }) { Text("کسورات کشاورزی و نیمه‌کاری") }
                            }
                            snapshot!!.agricultureResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                            snapshot!!.nimehkariResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                        }

                        // پیاده‌سازی نمایش درختی تفکیک‌شده با کادر جداگانه و فاصله واضح برای کل زیرمجموعه‌ها
                        if (snapshot!!.finalSharesResults.isNotEmpty()) {
                            Div(attrs = { style { marginTop(24.px); paddingTop(16.px); property("border-top", "4px double #4CAF50") } }) {
                                H4(attrs = { style { color(Color("#1B5E20")); fontWeight("bold"); property("margin", "0px 0px 16px 0px") } }) { Text("سهم‌های نهایی (تسهیم)") }
                            }
                            
                            // رندر هر سهم درختی داخل کادر سفید جداگانه با فاصله‌گذاری شیک
                            snapshot!!.finalSharesResults.forEach { item -> 
                                Div(attrs = {
                                    style {
                                        backgroundColor(Color("white"))
                                        property("border", "1px dashed #A5D6A7")
                                        borderRadius(8.px)
                                        padding(12.px)
                                        property("margin", "8px 0px")
                                        property("box-shadow", "0 2px 4px rgba(0,0,0,0.02)")
                                    }
                                }) {
                                    ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit, isHighlight = true)
                                }
                            }
                        }

                        Button(attrs = {
                            style {
                                width(100.percent)
                                padding(12.px)
                                property("margin-top", "24px")
                                backgroundColor(Color("white"))
                                color(Color("#2E7D32"))
                                property("border", "2px solid #2E7D32")
                                borderRadius(8.px)
                                fontSize(1.1.cssRem)
                                fontWeight("bold")
                                property("cursor", "pointer")
                            }
                            onClick { ir.ghiyas.alimaa.export.WebExportEngine.shareText(snapshot!!) }
                        }) { Text("کپی نتایج به صورت متنی") }
                    }
                }

            } else if (currentScreen == "history") {
                HistoryScreen(onBack = { currentScreen = "main" })
            }
        }
    }
}
