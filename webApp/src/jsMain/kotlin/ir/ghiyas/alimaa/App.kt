package ir.ghiyas.alimaa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.ui.components.GhiyasTopAppBar
import ir.ghiyas.alimaa.ui.components.HeroBanner
import ir.ghiyas.alimaa.ui.components.NavigationDrawer
import ir.ghiyas.alimaa.ui.stages.InputStageScreen
import ir.ghiyas.alimaa.ui.stages.ExpenseStageScreen
import ir.ghiyas.alimaa.ui.stages.AgricultureStageScreen
import ir.ghiyas.alimaa.ui.stages.DistributionStageScreen
import ir.ghiyas.alimaa.ui.stages.HistoryScreen
import ir.ghiyas.alimaa.presentation.stages.input.InputStageViewModel
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseStageViewModel
import ir.ghiyas.alimaa.presentation.stages.agriculture.AgricultureStageViewModel
import ir.ghiyas.alimaa.presentation.stages.distribution.DistributionStageViewModel
import ir.ghiyas.alimaa.presentation.calculator.CalculatorViewModel
import ir.ghiyas.alimaa.ui.calculator.FloatingCalculatorWidget
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import ir.ghiyas.alimaa.domain.models.ProfileIntegrationType
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat
import ir.ghiyas.alimaa.core.pwa.PwaManager

@Composable
fun ResultRowItem(label: String, rawValue: Double, baseUnit: String, isHighlight: Boolean = false) {
    val textColor = if (isHighlight) Color("#BF360C") else Color("#33691E")
    Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); padding(12.px, 0.px); property("border-bottom", "1px dashed #AED581"); fontSize(if (isHighlight) 1.15.cssRem else 1.1.cssRem); color(textColor) } }) {
        Span(attrs = { style { flex(1); if(isHighlight) fontWeight("bold") } }) { Text(label) }
        Span(attrs = { style { fontWeight("bold"); flex(1); textAlign("left") } }) { Span(attrs = { style { fontFamily("Vazirmatn", "system-ui", "sans-serif"); fontWeight("bold"); property("direction", "ltr"); display(DisplayStyle.InlineBlock) } }) { Text(rawValue.toGhiyasFormat(baseUnit)) }; Text(" $baseUnit") }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("main") }
    var currentMainTab by remember { mutableStateOf("default_pipeline") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var clearFormRequested by remember { mutableStateOf(false) }

    val inputViewModel = remember { InputStageViewModel() }
    val expenseViewModel = remember { ExpenseStageViewModel() }
    val agricultureViewModel = remember { AgricultureStageViewModel() } 
    val distributionViewModel = remember { DistributionStageViewModel() }
    val calculatorViewModel = remember { CalculatorViewModel() } 
    val builderViewModel = remember { ir.ghiyas.alimaa.presentation.builder.BuilderViewModel() }
    val dynamicPlayerViewModel = remember { ir.ghiyas.alimaa.presentation.player.DynamicPlayerViewModel() }
    
    // راه‌اندازی اولیه PWA Manager و ایتا SDK در زمان اجرای برنامه
    LaunchedEffect(Unit) {
        PwaManager.initialize()
    }

    var customProfiles by remember { mutableStateOf(emptyList<ir.ghiyas.alimaa.domain.models.CustomProfile>()) }
    LaunchedEffect(currentScreen, currentMainTab) {
        if (currentScreen == "main") {
            try { customProfiles = ir.ghiyas.alimaa.data.CustomProfileRepository.getAllProfiles() } catch (e:Exception) {}
        }
    }

    val inputState by inputViewModel.state.collectAsState()
    val snapshot by expenseViewModel.snapshot.collectAsState()
    val calcState by calculatorViewModel.state.collectAsState()
    val agricultureInputState by agricultureViewModel.inputState.collectAsState()
    
    // وضعیت دسترسی به نسخه جدید و وضعیت قابلیت نصب
    val isInstallable by PwaManager.isInstallable.collectAsState()
    val hasUpdateAvailable by PwaManager.hasUpdateAvailable.collectAsState()

    LaunchedEffect(inputState.totalAmount) {
        val amount = if (inputState.totalAmount.isNotBlank()) inputState.totalAmount else "0"
        expenseViewModel.setTotalWalnuts(WalnutUnit.fromInput(amount))
    }

    Style(AppStyleSheet)
    val mainPaddingBottom = if (calcState.isVisible && !calcState.isFullScreen) 440.px else 32.px

    Div(attrs = { dir(DirType.Rtl); style { property("margin", "0 auto"); maxWidth(600.px); width(100.percent); height(100.vh); position(Position.Relative); property("overflow", "hidden"); backgroundColor(Color("#F5F5F5")); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); fontFamily("Vazirmatn", "system-ui", "-apple-system", "sans-serif"); property("box-shadow", "0 0 15px rgba(0,0,0,0.05)") } }) {
        
        // ۱. نوار اعلان نسخه جدید نرم‌افزار
        if (hasUpdateAvailable) {
            Div(attrs = { style { backgroundColor(Color("#FFF3E0")); color(Color("#E65100")); padding(10.px, 16.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); property("border-bottom", "1px solid #FFE0B2") } }) {
                Div(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold") } }) { Text("نسخه جدید قیاس آماده است 🚀") }
                Button(attrs = { style { backgroundColor(Color("#EF6C00")); color(Color("white")); border(0.px); borderRadius(6.px); padding(6.px, 12.px); fontSize(0.85.cssRem); fontWeight("bold"); property("cursor", "pointer") }; onClick { PwaManager.applyUpdate() } }) { Text("بروزرسانی") }
            }
        }
        // ۲. نوار اعلان نصب اپلیکیشن
        else if (isInstallable) {
            Div(attrs = { style { backgroundColor(Color("#E3F2FD")); color(Color("#0D47A1")); padding(10.px, 16.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); property("border-bottom", "1px solid #90CAF9") } }) {
                Div(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold") } }) { Text("برای استفاده کاملاً آفلاین، قیاس را نصب کنید 📥") }
                Button(attrs = { style { backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(6.px); padding(6.px, 12.px); fontSize(0.85.cssRem); fontWeight("bold"); property("cursor", "pointer") }; onClick { PwaManager.promptInstall() } }) { Text("نصب اپلیکیشن") }
            }
        }

        GhiyasTopAppBar(onMenuClick = { isDrawerOpen = true }, onClearClick = { clearFormRequested = true; expenseViewModel.clearForm(); agricultureViewModel.clearForm(); distributionViewModel.clearForm() }, onHistoryClick = { currentScreen = "history" }, onShareClick = null)

        if (isDrawerOpen) { NavigationDrawer(onClose = { isDrawerOpen = false }, onNavigate = { route -> currentScreen = route; isDrawerOpen = false }) }
        
        Div(attrs = { style { property("flex", "1"); property("overflow-y", "auto"); paddingBottom(mainPaddingBottom); property("transition", "padding-bottom 0.3s cubic-bezier(0.2, 0.8, 0.2, 1)") } }) {
            when (currentScreen) {
                "main" -> {
                    HeroBanner()
                    Div(attrs = { classes(AppStyleSheet.tabContainer); classes("hide-scrollbar") }) {
                        Div(attrs = { classes(AppStyleSheet.tabItem, if (currentMainTab == "default_pipeline") AppStyleSheet.tabActive else AppStyleSheet.tabInactive); onClick { currentMainTab = "default_pipeline" } }) { Text("محاسبات پیش‌فرض") }
                        Div(attrs = { classes(AppStyleSheet.tabItem, if (currentMainTab == "standalone_runner") AppStyleSheet.tabActive else AppStyleSheet.tabInactive); onClick { currentMainTab = "standalone_runner" } }) { Text("مدیریت الگوها") }
                        Div(attrs = { classes(AppStyleSheet.tabItem, if (currentMainTab == "irrigation_calendar") AppStyleSheet.tabActive else AppStyleSheet.tabInactive); onClick { currentMainTab = "irrigation_calendar" } }) { Text("تقویم آبیاری") }
                    }

                    when (currentMainTab) {
                        "default_pipeline" -> {
                            InputStageScreen(viewModel = inputViewModel, onClearRequested = clearFormRequested, onClearComplete = { clearFormRequested = false })
                            ExpenseStageScreen(viewModel = expenseViewModel)
                            AgricultureStageScreen(viewModel = agricultureViewModel)
                            DistributionStageScreen(viewModel = distributionViewModel, agricultureInput = agricultureInputState, customProfiles = customProfiles, onNavigateToBuilder = { builderViewModel.clearForNewProfile(); currentScreen = "builder" })

                            Button(attrs = {
                                style { property("width", "calc(100% - 32px)"); padding(16.px); property("margin", "0px 16px 16px 16px"); backgroundColor(Color("#2E7D32")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); property("cursor", "pointer") }
                                onClick {
                                    val yearOptions = kotlin.js.json("year" to "numeric").unsafeCast<kotlin.js.Date.LocaleOptions>()
                                    val rawPersianYear = kotlin.js.Date().toLocaleDateString("fa-IR", yearOptions).trim()
                                    expenseViewModel.calculateAndSnapshot(inputState.calculationName, inputState.unitType.displayName, rawPersianYear, kotlin.js.Date().getTime().toLong(), agricultureInputState, distributionViewModel.state.value)
                                    expenseViewModel.snapshot.value?.let { newRecord -> ir.ghiyas.alimaa.data.LocalStorageRepository.saveRecord(newRecord) }
                                }
                            }) { Text("محاسبه کن") }

                            if (snapshot != null) {
                                Div(attrs = { style { property("margin", "16px"); padding(24.px); backgroundColor(Color("#F1F8E9")); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")) } }) {
                                    Div(attrs = { style { backgroundColor(Color("#F5F5F5")); color(Color("#1B5E20")); padding(14.px, 24.px); borderRadius(8.px); textAlign("center"); fontWeight("bold"); fontSize(1.25.cssRem); marginBottom(20.px); property("border", "1px solid #C8E6C9"); property("border-left", "5px solid #2E7D32") } }) { Text("نتایج محاسبات نهایی قیاس") }
                                    val dateTimeOptions = kotlin.js.json("year" to "numeric", "month" to "long", "day" to "numeric", "hour" to "2-digit", "minute" to "2-digit").unsafeCast<kotlin.js.Date.LocaleOptions>()
                                    val liveTimeString = kotlin.js.Date(snapshot!!.timestamp).toLocaleString("fa-IR", dateTimeOptions)
                                    Div(attrs = { style { marginBottom(16.px); paddingBottom(16.px); property("border-bottom", "2px dashed #C8E6C9") } }) {
                                        P(attrs = { style { margin(0.px); fontWeight("bold"); color(Color("#2E7D32")); fontSize(1.1.cssRem) } }) { Text("نام محاسبه: ${snapshot!!.calculationName}") }
                                        P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text("کل مقدار اولیه: ${snapshot!!.inputAmount.value.toGhiyasFormat(snapshot!!.baseUnit)} ${snapshot!!.baseUnit}") }
                                        P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#757575")); fontSize(0.85.cssRem) } }) { Text("زمان ثبت: $liveTimeString") }
                                    }
                                    if (snapshot!!.expensesResults.isNotEmpty()) { snapshot!!.expensesResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) } }
                                    if (snapshot!!.agricultureResults.isNotEmpty() || snapshot!!.nimehkariResults.isNotEmpty()) {
                                        Div(attrs = { style { marginTop(16.px); paddingTop(16.px); property("border-top", "3px solid #AED581") } }) { H4(attrs = { style { color(Color("#2E7D32")); property("margin", "0px 0px 12px 0px") } }) { Text("کسورات کشاورزی و نیمه‌کاری") } }
                                        snapshot!!.agricultureResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                                        snapshot!!.nimehkariResults.forEach { item -> ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                                    }
                                    if (snapshot!!.finalSharesResults.isNotEmpty()) {
                                        Div(attrs = { style { marginTop(24.px); paddingTop(16.px); property("border-top", "4px double #4CAF50") } }) { H4(attrs = { style { color(Color("#1B5E20")); fontWeight("bold"); property("margin", "0px 0px 16px 0px") } }) { Text("سهم‌های نهایی (تسهیم)") } }
                                        snapshot!!.finalSharesResults.forEach { item -> 
                                            val isNimehkariRow = item.label.startsWith("🌾")
                                            Div(attrs = { style { backgroundColor(if (isNimehkariRow) Color("#FFF8E1") else Color("white")); property("border", if (isNimehkariRow) "1px dashed #FFB300" else "1px dashed #A5D6A7"); borderRadius(8.px); padding(12.px); property("margin", if (isNimehkariRow) "16px 0px 4px 0px" else "8px 0px"); property("box-shadow", "0 2px 4px rgba(0,0,0,0.02)") } }) { ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit, isHighlight = true) }
                                        }
                                    }
                                    Button(attrs = { style { width(100.percent); padding(12.px); property("margin-top", "24px"); backgroundColor(Color("white")); color(Color("#2E7D32")); property("border", "2px solid #2E7D32"); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); property("cursor", "pointer") }; onClick { ir.ghiyas.alimaa.export.WebExportEngine.shareText(snapshot!!) } }) { Text("کپی نتایج به صورت متنی") }
                                }
                            }
                        }
                        "standalone_runner" -> {
                            Div(attrs = { style { padding(24.px) } }) {
                                H4(attrs = { style { color(Color("#1B5E20")); marginTop(0.px) } }) { Text("مدیریت الگوهای اختصاصی:") }
                                if (customProfiles.isEmpty()) {
                                    Div(attrs = { style { textAlign("center"); color(Color("#757575")); marginBottom(24.px) } }) { Text("هنوز هیچ الگویی نساخته‌اید.") }
                                } else {
                                    customProfiles.forEach { prof ->
                                        Div(attrs = { style { backgroundColor(Color("white")); padding(16.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(12.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(12.px) } }) {
                                            Span(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); fontSize(1.1.cssRem) } }) { 
                                                Text(prof.name + if(prof.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) " (وابسته)" else " (مستقل)") 
                                            }
                                            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px) } }) {
                                                if(prof.integrationType == ProfileIntegrationType.STANDALONE_MAIN_TAB) {
                                                    Button(attrs = { style { flex(1); backgroundColor(Color("#4CAF50")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { dynamicPlayerViewModel.loadProfile(prof.id); currentScreen = "dynamic_player" } }) { Text("▶ اجرا") }
                                                }
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { builderViewModel.loadProfileForEdit(prof); currentScreen = "builder" } }) { Text("✏️ ویرایش") }
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#F44336")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { ir.ghiyas.alimaa.data.CustomProfileRepository.deleteProfile(prof.id); try { customProfiles = ir.ghiyas.alimaa.data.CustomProfileRepository.getAllProfiles() } catch (e:Exception) {} } }) { Text("🗑️ حذف") }
                                            }
                                        }
                                    }
                                }
                                Button(attrs = { style { width(100.percent); padding(16.px); backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); cursor("pointer"); marginTop(16.px) }; onClick { builderViewModel.clearForNewProfile(); currentScreen = "builder" } }) { Text("➕ ساخت الگوی جدید") }
                            }
                        }
                        "irrigation_calendar" -> { Div(attrs = { style { padding(32.px); textAlign("center"); color(Color("#757575")) } }) { Text("تقویم آبیاری (به زودی)") } }
                    }
                }
                "history" -> { HistoryScreen(onBack = { currentScreen = "main" }) }
                "builder" -> { ir.ghiyas.alimaa.ui.builder.BuilderScreen(viewModel = builderViewModel, onBack = { currentScreen = "main" }) }
                "dynamic_player" -> { 
                    ir.ghiyas.alimaa.ui.player.DynamicPlayerScreen(viewModel = dynamicPlayerViewModel, onBack = { currentScreen = "main"; dynamicPlayerViewModel.clearState() }) 
                }
            }
        }
        FloatingCalculatorWidget(calculatorViewModel)
    }
}
