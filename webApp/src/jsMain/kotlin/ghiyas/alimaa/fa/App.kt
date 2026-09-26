package ghiyas.alimaa.fa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ghiyas.alimaa.fa.ui.components.GhiyasTopAppBar
import ghiyas.alimaa.fa.ui.components.HeroBanner
import ghiyas.alimaa.fa.ui.components.NavigationDrawer
import ghiyas.alimaa.fa.ui.stages.InputStageScreen
import ghiyas.alimaa.fa.ui.stages.ExpenseStageScreen
import ghiyas.alimaa.fa.ui.stages.AgricultureStageScreen
import ghiyas.alimaa.fa.ui.stages.DistributionStageScreen
import ghiyas.alimaa.fa.ui.stages.HistoryScreen
import ghiyas.alimaa.fa.ui.stages.WorkCalendarScreen
import ghiyas.alimaa.fa.ui.stages.CalendarManagerForm
import ghiyas.alimaa.fa.ui.stages.WorkCalendarFormState
import ghiyas.alimaa.fa.presentation.stages.input.InputStageViewModel
import ghiyas.alimaa.fa.presentation.stages.expense.ExpenseStageViewModel
import ghiyas.alimaa.fa.presentation.stages.agriculture.AgricultureStageViewModel
import ghiyas.alimaa.fa.presentation.stages.distribution.DistributionStageViewModel
import ghiyas.alimaa.fa.presentation.calculator.CalculatorViewModel
import ghiyas.alimaa.fa.ui.calculator.FloatingCalculatorWidget
import ghiyas.alimaa.fa.ui.theme.AppStyleSheet
import ghiyas.alimaa.fa.domain.models.WalnutUnit
import ghiyas.alimaa.fa.domain.models.ProfileIntegrationType
import ghiyas.alimaa.fa.core.utils.toGhiyasFormat
import ghiyas.alimaa.fa.core.pwa.PwaManager
import ghiyas.alimaa.fa.ui.backup.BackupRestoreScreen
import kotlinx.browser.window
import org.w3c.dom.events.Event
import ghiyas.alimaa.fa.domain.models.WorkCalendarProfile
import ghiyas.alimaa.fa.data.WorkCalendarRepository
import ghiyas.alimaa.fa.data.LocalStorageRepository

@Composable
fun DeleteConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Div(attrs = { style { position(Position.Fixed); top(0.px); left(0.px); width(100.percent); height(100.vh); backgroundColor(Color("rgba(0,0,0,0.5)")); display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center); property("z-index", "9999") } }) {
        Div(attrs = { dir(DirType.Rtl); style { backgroundColor(Color("white")); padding(24.px); borderRadius(16.px); width(90.percent); maxWidth(400.px) } }) {
            H3(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#D32F2F")) } }) { Text(title) }
            P(attrs = { style { margin(0.px, 0.px, 24.px, 0.px); color(Color("#424242")) } }) { Text(message) }
            Div(attrs = { style { display(DisplayStyle.Flex); gap(12.px) } }) {
                Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#F5F5F5")); color(Color("#424242")); border(0.px); borderRadius(8.px); cursor("pointer") }; onClick { onCancel() } }) { Text("لغو") }
                Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#D32F2F")); color(Color("white")); border(0.px); borderRadius(8.px); cursor("pointer") }; onClick { onConfirm() } }) { Text("بله، حذف کن") }
            }
        }
    }
}

@Composable
fun ResultRowItem(label: String, rawValue: Double, baseUnit: String, isHighlight: Boolean = false) {
    val textColor = if (isHighlight) Color("#BF360C") else Color("#33691E")
    Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); padding(12.px, 0.px); property("border-bottom", "1px dashed #AED581"); fontSize(if (isHighlight) 1.15.cssRem else 1.1.cssRem); color(textColor) } }) {
        Span(attrs = { style { flex(1); if(isHighlight) fontWeight("bold") } }) { Text(label) }
        Span(attrs = { style { fontWeight("bold"); flex(1); textAlign("left") } }) { Span(attrs = { style { fontFamily("Vazirmatn", "system-ui", "sans-serif"); fontWeight("bold"); property("direction", "ltr"); display(DisplayStyle.InlineBlock) } }) { Text(rawValue.toGhiyasFormat(baseUnit, label)) }; Text(" $baseUnit") }
    }
}

@Composable
fun ExitConfirmDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    Div(attrs = { style { position(Position.Fixed); top(0.px); left(0.px); width(100.percent); height(100.vh); backgroundColor(Color("rgba(0,0,0,0.5)")); display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center); property("z-index", "9999") } }) {
        Div(attrs = { dir(DirType.Rtl); style { backgroundColor(Color("white")); padding(24.px); borderRadius(16.px); width(90.percent); maxWidth(400.px) } }) {
            H3(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#D32F2F")) } }) { Text("خروج از برنامه") }
            P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#424242")) } }) { Text("آیا مطمئن هستید که می‌خواهید خارج شوید؟") }
            P(attrs = { style { margin(0.px, 0.px, 24.px, 0.px); color(Color("#757575")); fontSize(0.95.cssRem) } }) { Text("برای خروج دو بار کلید بازگشت را بزنید") }
            Div(attrs = { style { display(DisplayStyle.Flex); gap(12.px) } }) {
                Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#F5F5F5")); border(0.px); borderRadius(8.px); cursor("pointer") }; onClick { onCancel() } }) { Text("لغو") }
                Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#D32F2F")); color(Color("white")); border(0.px); borderRadius(8.px); cursor("pointer") }; onClick { onConfirm() } }) { Text("بله") }
            }
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf("main") }
    var currentMainTab by remember { mutableStateOf("default_pipeline") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    var clearFormRequested by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val inputViewModel = remember { InputStageViewModel() }
    val expenseViewModel = remember { ExpenseStageViewModel() }
    val agricultureViewModel = remember { AgricultureStageViewModel() } 
    val distributionViewModel = remember { DistributionStageViewModel() }
    val calculatorViewModel = remember { CalculatorViewModel() } 
    val builderViewModel = remember { ghiyas.alimaa.fa.presentation.builder.BuilderViewModel() }
    val dynamicPlayerViewModel = remember { ghiyas.alimaa.fa.presentation.player.DynamicPlayerViewModel() }
    
    var workCalendars by remember { mutableStateOf(emptyList<WorkCalendarProfile>()) }
    var activeCalendarId by remember { mutableStateOf<String?>(null) }
    val calendarFormState = remember { WorkCalendarFormState() }
    
    var showCalendarDeleteConfirmId by remember { mutableStateOf<String?>(null) }
    var showProfileDeleteConfirmId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) { 
        PwaManager.initialize() 
        val savedCalcHistory = LocalStorageRepository.getCalculatorHistory()
        if (savedCalcHistory.isNotEmpty()) {
            calculatorViewModel.restoreHistory(savedCalcHistory)
        }
    }

    LaunchedEffect(Unit) {
        window.history.pushState(null, "", window.location.href)
        val popStateHandler: (Event) -> Unit = { 
            if (currentScreen != "main") {
                currentScreen = "main"
                window.history.pushState(null, "", window.location.href)
            } else {
                if (!showExitDialog) {
                    showExitDialog = true
                    window.history.pushState(null, "", window.location.href)
                } else {
                    showExitDialog = false
                }
            }
        }
        window.addEventListener("popstate", popStateHandler)
    }

    val navigateTo: (String) -> Unit = { route ->
        if (route == "profile_manager") {
            currentMainTab = "standalone_runner"
            currentScreen = "main"
            window.history.pushState(null, "", "${window.location.pathname}#main")
        } else {
            currentScreen = route
            window.history.pushState(null, "", "${window.location.pathname}#$route")
        }
    }

    var customProfiles by remember { mutableStateOf(emptyList<ghiyas.alimaa.fa.domain.models.CustomProfile>()) }
    
    LaunchedEffect(currentScreen, currentMainTab) {
        if (currentScreen == "main") {
            try { customProfiles = ghiyas.alimaa.fa.data.CustomProfileRepository.getAllProfiles() } catch (e:Exception) {}
            if (currentMainTab == "work_calendar") {
                try { 
                    workCalendars = WorkCalendarRepository.getAllProfiles() 
                    if (activeCalendarId == null) {
                        activeCalendarId = workCalendars.find { it.isDefault }?.id ?: workCalendars.firstOrNull()?.id
                    }
                } catch (e:Exception) {}
            }
        }
    }

    val inputState by inputViewModel.state.collectAsState()
    val snapshot by expenseViewModel.snapshot.collectAsState()
    val calcState by calculatorViewModel.state.collectAsState()
    val agricultureInputState by agricultureViewModel.inputState.collectAsState()
    val distributionState by distributionViewModel.state.collectAsState()

    LaunchedEffect(calcState.history) {
        LocalStorageRepository.saveCalculatorHistory(calcState.history)
    }

    LaunchedEffect(inputState.totalAmount) {
        val amount = if (inputState.totalAmount.isNotBlank()) inputState.totalAmount else "0"
        expenseViewModel.setTotalWalnuts(WalnutUnit.fromInput(amount))
    }

    Style(AppStyleSheet)
    val mainPaddingBottom = if (calcState.isVisible && !calcState.isFullScreen) 440.px else 32.px

    Div(attrs = { dir(DirType.Rtl); style { property("margin", "0 auto"); maxWidth(600.px); width(100.percent); height(100.vh); position(Position.Relative); property("overflow", "hidden"); backgroundColor(Color("#F5F5F5")); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); fontFamily("Vazirmatn", "system-ui", "-apple-system", "sans-serif") } }) {
        
        val isCalendarTab = currentScreen == "main" && currentMainTab == "work_calendar"
        
        if (isCalendarTab) {
            GhiyasTopAppBar(
                onMenuClick = { isDrawerOpen = true },
                onClearClick = null,
                onHistoryClick = null,
                centerContent = {
                    if (calendarFormState.isVisible) {
                        Span(attrs = { style { fontSize(18.px); fontWeight("bold"); property("white-space", "nowrap") } }) { Text("مدیریت تقویم") }
                    } else if (workCalendars.isNotEmpty()) {
                        Div(attrs = { 
                            style { 
                                display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); width(100.percent)
                                property("min-width", "0") 
                            } 
                        }) {
                            Span(attrs = { style { fontSize(20.px); fontWeight("bold"); property("white-space", "nowrap") } }) { Text("قیاس") }
                            Select(attrs = {
                                style { 
                                    flex(1)
                                    property("min-width", "0") 
                                    padding(6.px, 12.px)
                                    borderRadius(6.px)
                                    border(0.px)
                                    backgroundColor(Color("#81C784"))
                                    color(Color("white"))
                                    fontSize(0.95.cssRem)
                                    fontFamily("Vazirmatn")
                                    fontWeight("bold")
                                    outline("none")
                                }
                                onChange { e -> activeCalendarId = e.value }
                            }) {
                                workCalendars.forEach { cal ->
                                    Option(value = cal.id, attrs = { if (activeCalendarId == cal.id) selected() }) { 
                                        Text(cal.name + if (cal.isDefault) " (پیش‌فرض)" else "") 
                                    }
                                }
                            }
                        }
                    } else {
                        Span(attrs = { style { fontSize(20.px); fontWeight("bold"); property("white-space", "nowrap") } }) { Text("تقویم کاری") }
                    }
                }
            )
        } else {
            GhiyasTopAppBar(
                onMenuClick = { isDrawerOpen = true },
                onClearClick = { clearFormRequested = true; expenseViewModel.clearForm(); agricultureViewModel.clearForm(); distributionViewModel.clearForm() },
                onHistoryClick = { navigateTo("history") }
            )
        }

        if (isDrawerOpen) { NavigationDrawer(onClose = { isDrawerOpen = false }, onNavigate = { route -> navigateTo(route); isDrawerOpen = false }) }
        
        Div(attrs = { style { property("flex", "1"); property("overflow-y", "auto"); paddingBottom(mainPaddingBottom) } }) {
            when (currentScreen) {
                "main" -> {
                    HeroBanner()
                    Div(attrs = { 
                        classes(AppStyleSheet.tabContainer); classes("hide-scrollbar")
                        style {
                            display(DisplayStyle.Flex)
                            property("overflow-x", "auto")
                            property("-webkit-overflow-scrolling", "touch")
                        } 
                    }) {
                        Div(attrs = { 
                            classes(AppStyleSheet.tabItem, if (currentMainTab == "default_pipeline") AppStyleSheet.tabActive else AppStyleSheet.tabInactive)
                            style { flexShrink(0) }
                            onClick { currentMainTab = "default_pipeline" } 
                        }) { Text("محاسبات پیش‌فرض") }
                        
                        Div(attrs = { 
                            classes(AppStyleSheet.tabItem, if (currentMainTab == "standalone_runner") AppStyleSheet.tabActive else AppStyleSheet.tabInactive)
                            style { flexShrink(0) }
                            onClick { currentMainTab = "standalone_runner" } 
                        }) { Text("مدیریت الگوها") }
                        
                        Div(attrs = { 
                            classes(AppStyleSheet.tabItem, if (currentMainTab == "work_calendar") AppStyleSheet.tabActive else AppStyleSheet.tabInactive)
                            style { flexShrink(0) }
                            onClick { currentMainTab = "work_calendar" } 
                        }) { Text("تقویم کاری") }
                    }

                    when (currentMainTab) {
                        "default_pipeline" -> {
                            InputStageScreen(viewModel = inputViewModel, onClearRequested = clearFormRequested, onClearComplete = { clearFormRequested = false })
                            ExpenseStageScreen(viewModel = expenseViewModel)
                            AgricultureStageScreen(viewModel = agricultureViewModel)
                            DistributionStageScreen(viewModel = distributionViewModel, agricultureInput = agricultureInputState, customProfiles = customProfiles, onNavigateToBuilder = { builderViewModel.clearForNewProfile(); navigateTo("builder") })

                            Button(attrs = {
                                style { property("width", "calc(100% - 32px)"); padding(16.px); property("margin", "0px 16px 16px 16px"); backgroundColor(Color("#2E7D32")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); property("cursor", "pointer") }
                                onClick {
                                    val yearOptions = kotlin.js.json("year" to "numeric").unsafeCast<kotlin.js.Date.LocaleOptions>()
                                    val rawPersianYear = kotlin.js.Date().toLocaleDateString("fa-IR", yearOptions).trim()
                                    
                                    expenseViewModel.calculateAndSnapshot(
                                        inputState.calculationName, 
                                        inputState.unitType.displayName, 
                                        rawPersianYear, 
                                        kotlin.js.Date().getTime().toLong(), 
                                        agricultureInputState, 
                                        distributionState
                                    )
                                    expenseViewModel.snapshot.value?.let { newRecord -> ghiyas.alimaa.fa.data.LocalStorageRepository.saveRecord(newRecord) }
                                }
                            }) { Text("محاسبه کن") }
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
                                                    Button(attrs = { style { flex(1); backgroundColor(Color("#4CAF50")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { dynamicPlayerViewModel.loadProfile(prof.id); navigateTo("dynamic_player") } }) { Text("▶ اجرا") }
                                                }
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { builderViewModel.loadProfileForEdit(prof); navigateTo("builder") } }) { Text("✏️ ویرایش") }
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#F44336")); color(Color("white")); border(0.px); borderRadius(6.px); padding(8.px); cursor("pointer") }; onClick { showProfileDeleteConfirmId = prof.id } }) { Text("🗑️ حذف") }
                                            }
                                        }
                                    }
                                }
                                Button(attrs = { style { width(100.percent); padding(16.px); backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); cursor("pointer"); marginTop(16.px) }; onClick { builderViewModel.clearForNewProfile(); navigateTo("builder") } }) { Text("➕ ساخت الگوی جدید") }
                            }
                        }
                        "work_calendar" -> { 
                            if (calendarFormState.isVisible) {
                                CalendarManagerForm(
                                    state = calendarFormState,
                                    onProfileSaved = {
                                        calendarFormState.isVisible = false
                                        calendarFormState.reset()
                                        workCalendars = WorkCalendarRepository.getAllProfiles()
                                        activeCalendarId = workCalendars.lastOrNull()?.id
                                    },
                                    onCancel = { calendarFormState.isVisible = false }
                                )
                            } else if (workCalendars.isEmpty()) {
                                Div(attrs = { style { padding(32.px); textAlign("center"); marginTop(40.px) } }) {
                                    Div(attrs = { style { fontSize(4.cssRem); marginBottom(16.px) } }) { Text("📅") }
                                    H3(attrs = { style { color(Color("#2E7D32")); marginBottom(8.px) } }) { Text("تقویم کاری وجود ندارد") }
                                    P(attrs = { style { color(Color("#757575")); marginBottom(24.px) } }) { Text("برای زمان‌بندی آبیاری یا شیفت‌های کاری، اولین تقویم خود را ایجاد کنید.") }
                                    Button(attrs = { 
                                        style { padding(12.px, 24.px); backgroundColor(Color("#4CAF50")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); cursor("pointer") }
                                        onClick { calendarFormState.reset(); calendarFormState.isVisible = true }
                                    }) { Text("➕ ایجاد تقویم جدید") }
                                }
                            } else {
                                val activeProfile = workCalendars.find { it.id == activeCalendarId }
                                WorkCalendarScreen(
                                    activeProfile = activeProfile,
                                    onAddNew = { calendarFormState.reset(); calendarFormState.isVisible = true },
                                    onEdit = {
                                        activeProfile?.let { prof ->
                                            calendarFormState.existingId = prof.id
                                            calendarFormState.calendarName = prof.name
                                            calendarFormState.parsedStartYear = prof.startYear
                                            calendarFormState.parsedStartMonth = prof.startMonth
                                            calendarFormState.parsedStartDay = prof.startDay
                                            calendarFormState.parsedTurnTime = prof.turnTime
                                            calendarFormState.shiftBeforeTemplate = prof.shiftBeforeTemplate
                                            calendarFormState.shiftAfterTemplate = prof.shiftAfterTemplate
                                            calendarFormState.parsedSchedule = prof.schedule
                                            calendarFormState.parsedQuotes = prof.quotes 
                                            calendarFormState.isVisible = true
                                        }
                                    },
                                    onDelete = { showCalendarDeleteConfirmId = activeCalendarId },
                                    onSetDefault = {
                                        activeProfile?.let { prof ->
                                            WorkCalendarRepository.saveProfile(prof.copy(isDefault = true))
                                            workCalendars = WorkCalendarRepository.getAllProfiles()
                                            window.alert("تقویم ${prof.name} به عنوان پیش‌فرض ثبت شد.")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // رندر کارت نتایج هوشمند: هر نتیجه فقط در تب مربوط به خودش نمایش داده می‌شود
                    if (snapshot != null && currentMainTab != "work_calendar") {
                        val isCustomProfileSnapshot = snapshot!!.associated_profile_id != null
                        val shouldShowInCurrentTab = (currentMainTab == "default_pipeline" && !isCustomProfileSnapshot) ||
                                                     (currentMainTab == "standalone_runner" && isCustomProfileSnapshot)
                        
                        if (shouldShowInCurrentTab) {
                            Div(attrs = { style { property("margin", "16px"); padding(24.px); backgroundColor(Color("#F1F8E9")); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")) } }) {
                                
                                Div(attrs = { style { backgroundColor(Color("#F5F5F5")); color(Color("#1B5E20")); padding(14.px, 24.px); borderRadius(8.px); textAlign("center"); fontWeight("bold"); fontSize(1.25.cssRem); marginBottom(20.px); property("border", "1px solid #C8E6C9"); property("border-left", "5px solid #2E7D32") } }) { 
                                    Text(if (isCustomProfileSnapshot) "نتایج محاسبه اختصاصی (${snapshot!!.calculationName})" else "نتایج محاسبات نهایی قیاس") 
                                }
                                val dateTimeOptions = kotlin.js.json("year" to "numeric", "month" to "long", "day" to "numeric", "hour" to "2-digit", "minute" to "2-digit").unsafeCast<kotlin.js.Date.LocaleOptions>()
                                val liveTimeString = kotlin.js.Date(snapshot!!.timestamp).toLocaleString("fa-IR", dateTimeOptions)
                                Div(attrs = { style { marginBottom(16.px); paddingBottom(16.px); property("border-bottom", "2px dashed #C8E6C9") } }) {
                                    P(attrs = { style { margin(0.px); fontWeight("bold"); color(Color("#2E7D32")); fontSize(1.1.cssRem) } }) { Text("نام محاسبه: ${snapshot!!.calculationName}") }
                                    P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text("کل مقدار اولیه: ${snapshot!!.inputAmount.value.toGhiyasFormat(snapshot!!.baseUnit)} ${snapshot!!.baseUnit}") }
                                    P(attrs = { style { property("margin", "8px 0px 0px 0px"); color(Color("#757575")); fontSize(0.85.cssRem) } }) { Text("زمان ثبت: $liveTimeString") }
                                }
                                
                                if (snapshot!!.expensesResults.isNotEmpty()) { 
                                    snapshot!!.expensesResults.forEach { item -> 
                                        key("exp_${item.label}") { ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                                    }
                                }
                                
                                val actualNimResults = snapshot!!.nimehkariResults.filter { it.label != "خالص باقی‌مانده برای تسهیم" }
                                val remainingItem = snapshot!!.nimehkariResults.find { it.label == "خالص باقی‌مانده برای تسهیم" }
                                
                                if (snapshot!!.agricultureResults.isNotEmpty() || actualNimResults.isNotEmpty()) {
                                    Div(attrs = { style { marginTop(16.px); paddingTop(16.px); property("border-top", "3px solid #AED581") } }) { H4(attrs = { style { color(Color("#2E7D32")); property("margin", "0px 0px 12px 0px") } }) { Text("کسورات کشاورزی و نیمه‌کاری") } }
                                    snapshot!!.agricultureResults.forEach { item -> 
                                        key("agr_${item.label}") { ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                                    }
                                    
                                    actualNimResults.forEach { item -> 
                                        key("nim_${item.label}") { ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit) }
                                    }
                                }
                                
                                if (remainingItem != null) {
                                    key("final_remaining_box") {
                                        Div(attrs = { style { backgroundColor(Color("#E8F5E9")); borderRadius(8.px); padding(4.px, 8.px); margin(16.px, 0.px); property("border-right", "4px solid #2E7D32") } }) {
                                            ResultRowItem("باقیمانده نهایی (جهت تسهیم)", remainingItem.value.value, snapshot!!.baseUnit, isHighlight = true)
                                        }
                                    }
                                }
                                
                                if (snapshot!!.finalSharesResults.isNotEmpty()) {
                                    Div(attrs = { style { marginTop(24.px); paddingTop(16.px); property("border-top", "4px double #4CAF50") } }) { H4(attrs = { style { color(Color("#1B5E20")); fontWeight("bold"); property("margin", "0px 0px 16px 0px") } }) { Text("سهم‌های نهایی (تسهیم)") } }
                                    snapshot!!.finalSharesResults.forEach { item -> 
                                        key("fin_${item.label}") {
                                            val isNimehkariRow = item.label.startsWith("🌾")
                                            Div(attrs = { style { backgroundColor(if (isNimehkariRow) Color("#FFF8E1") else Color("white")); property("border", if (isNimehkariRow) "1px dashed #FFB300" else "1px dashed #A5D6A7"); borderRadius(8.px); padding(12.px); property("margin", if (isNimehkariRow) "16px 0px 4px 0px" else "8px 0px"); property("box-shadow", "0 2px 4px rgba(0,0,0,0.02)") } }) { ResultRowItem(item.label, item.value.value, snapshot!!.baseUnit, isHighlight = true) }
                                        }
                                    }
                                }
                                
                                Button(attrs = { style { width(100.percent); padding(12.px); property("margin-top", "24px"); backgroundColor(Color("white")); color(Color("#2E7D32")); property("border", "2px solid #2E7D32"); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); property("cursor", "pointer") }; onClick { ghiyas.alimaa.fa.export.WebExportEngine.shareText(snapshot!!) } }) { Text("کپی نتایج به صورت متنی") }
                            }
                        }
                    }
                }
                "history" -> { HistoryScreen(onBack = { window.history.back() }) }
                "builder" -> { ghiyas.alimaa.fa.ui.builder.BuilderScreen(viewModel = builderViewModel, onBack = { window.history.back() }) }
                "dynamic_player" -> { 
                    ghiyas.alimaa.fa.ui.player.DynamicPlayerScreen(
                        viewModel = dynamicPlayerViewModel, 
                        baseUnit = inputState.unitType.displayName,
                        onBack = { dynamicPlayerViewModel.clearState(); window.history.back() },
                        onCalculationComplete = { record ->
                            ghiyas.alimaa.fa.data.LocalStorageRepository.saveRecord(record)
                            expenseViewModel.setExternalSnapshot(record)
                            dynamicPlayerViewModel.clearState()
                            window.history.back()
                        }
                    ) 
                }
                "backup_restore" -> {
                    BackupRestoreScreen(onNavigateBack = { window.history.back() })
                }
            }
        }
        
        if (showCalendarDeleteConfirmId != null) {
            DeleteConfirmDialog(
                title = "حذف تقویم کاری",
                message = "آیا مطمئن هستید که می‌خواهید این تقویم را به طور کامل حذف کنید؟ این عمل غیرقابل بازگشت است.",
                onConfirm = {
                    WorkCalendarRepository.deleteProfile(showCalendarDeleteConfirmId!!)
                    workCalendars = WorkCalendarRepository.getAllProfiles()
                    activeCalendarId = workCalendars.find { it.isDefault }?.id ?: workCalendars.firstOrNull()?.id
                    showCalendarDeleteConfirmId = null
                },
                onCancel = { showCalendarDeleteConfirmId = null }
            )
        }
        
        if (showProfileDeleteConfirmId != null) {
            DeleteConfirmDialog(
                title = "حذف الگوی اختصاصی",
                message = "آیا از حذف دائم این الگوی اختصاصی اطمینان دارید؟",
                onConfirm = {
                    ghiyas.alimaa.fa.data.CustomProfileRepository.deleteProfile(showProfileDeleteConfirmId!!)
                    try { customProfiles = ghiyas.alimaa.fa.data.CustomProfileRepository.getAllProfiles() } catch (e:Exception) {}
                    showProfileDeleteConfirmId = null
                },
                onCancel = { showProfileDeleteConfirmId = null }
            )
        }
        
        if (showExitDialog) { ExitConfirmDialog(onConfirm = { window.history.back() }, onCancel = { showExitDialog = false }) }
        FloatingCalculatorWidget(calculatorViewModel)
    }
}
