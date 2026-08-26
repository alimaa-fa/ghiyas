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
import ir.ghiyas.alimaa.domain.models.ShareholderNode
import ir.ghiyas.alimaa.domain.models.ComprehensiveMode
import ir.ghiyas.alimaa.domain.models.CustomProfile
import ir.ghiyas.alimaa.domain.models.ProfileIntegrationType
import ir.ghiyas.alimaa.domain.models.SavedDistributionTemplate
import ir.ghiyas.alimaa.data.DistributionTemplateRepository
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import kotlinx.browser.window

private fun String.standardizeDigitsLocal(): String {
    var result = this
    result = result.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9').replace('٫', '.')
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
private fun DistTextInput(label: String, value: String, isNumber: Boolean = false, isReadonly: Boolean = false, onValueChange: (String) -> Unit) {
    Div(attrs = { classes(AppStyleSheet.floatingContainer); style { marginBottom(0.px); width(100.percent) } }) {
        Input(type = InputType.Text, attrs = {
            classes(AppStyleSheet.floatingInput); classes("floating-input")
            if (isNumber) attr("inputmode", "decimal")
            if (isReadonly) { attr("disabled", "true"); style { backgroundColor(Color("#F5F5F5")); color(Color("#9E9E9E")) } }
            value(value.toPersianDigitsLocal())
            onInput { event -> val finalVal = if (isNumber) event.value.standardizeDigitsLocal() else event.value; onValueChange(finalVal) }
            placeholder(" ") 
        })
        Label(attrs = { classes(AppStyleSheet.floatingLabel); classes("floating-label") }) { Text(label) }
    }
}

private fun flattenTree(nodes: List<ShareholderNode>): List<Pair<String, String>> {
    return nodes.flatMap { listOf(it.id to it.name) + flattenTree(it.children) }
}

@Composable
fun RecursiveComprehensiveNode(
    node: ShareholderNode, path: List<String>, currentMode: ComprehensiveMode, 
    target: PoolTarget, viewModel: DistributionStageViewModel, 
    allAvailableNodes: List<Pair<String, String>>, isExecutionMode: Boolean
) {
    val isVisuallyExcluded = isExecutionMode && node.isExcluded
    val borderColor = if (isVisuallyExcluded) "#BDBDBD" else "#4CAF50"
    val bgColor = if (isVisuallyExcluded) "#F5F5F5" else "#F8FBF8"
    val opacityValue = if (isVisuallyExcluded) 0.6 else 1.0

    Div(attrs = { style { padding(12.px); marginTop(12.px); property("border-right", "4px solid $borderColor"); backgroundColor(Color(bgColor)); borderRadius(4.px); opacity(opacityValue) } }) {
        
        // ردیف اول: نام، مقدار
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(12.px) } }) {
            Div(attrs = { style { flex(2) } }) { 
                DistTextInput("نام شریک", node.name, false, isReadonly = isExecutionMode) { v -> viewModel.updateNode(target, path) { it.copy(name = v) } } 
            }
            
            if (currentMode != ComprehensiveMode.PERSON) {
                Div(attrs = { style { flex(1) } }) { 
                    DistTextInput(if (currentMode == ComprehensiveMode.PERCENTAGE) "درصد" else "قیاس", node.rawValue, true, isReadonly = isExecutionMode) { v -> viewModel.updateNode(target, path) { it.copy(rawValue = v) } } 
                }
            } else {
                Label(attrs = { style { flex(1); display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", if (isExecutionMode) "not-allowed" else "pointer"); fontSize(0.9.cssRem) } }) {
                    Input(type = InputType.Checkbox, attrs = { 
                        checked(node.isFemale)
                        if (isExecutionMode) attr("disabled", "true")
                        onChange { e -> viewModel.updateNode(target, path) { it.copy(isFemale = e.value) } }; style { marginRight(4.px) } 
                    })
                    Text("دختر (۰.۵)")
                }
            }
            if (!isExecutionMode) {
                Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeNode(target, path.dropLast(1), node.id) } }) { Text("-") }
            }
        }

        if (!isExecutionMode) {
            // --- حالت ویرایش (Edit Mode) ---
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); color(Color("#F57C00")); marginBottom(12.px); fontWeight("bold") } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.canBeExcluded); onChange { e -> viewModel.updateNode(target, path) { it.copy(canBeExcluded = e.value) } }; style { marginRight(4.px) } })
                Text("حساب شود/نشود؟ (مجوز حذف در اجرا)")
            }

            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); color(Color("#1976D2")); marginBottom(12.px); fontWeight("bold") } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.canBeTransferred); onChange { e -> viewModel.updateNode(target, path) { it.copy(canBeTransferred = e.value) } }; style { marginRight(4.px) } })
                Text("امکان انتقال سهم در زمان اجرا؟")
            }

            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); fontWeight("bold"); color(Color("#2E7D32")) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.hasSubDistribution); onChange { e -> viewModel.updateNode(target, path) { it.copy(hasSubDistribution = e.value) } }; style { marginRight(8.px) } })
                Text("تقسیم جزئی (وارث جدید)؟")
            }
            
            if (node.hasSubDistribution) {
                Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")) } }) {
                    Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(4.px); border(1.px, LineStyle.Solid, Color("#81C784")); marginBottom(8.px) }; onChange { e -> ComprehensiveMode.entries.find { m -> m.name == e.value }?.let { m -> viewModel.updateNode(target, path) { it.copy(subDistributionMode = m) } } } }) {
                        ComprehensiveMode.entries.forEach { mode -> 
                            key(mode.name) { Option(value = mode.name, attrs = { if (node.subDistributionMode == mode) attr("selected", "true") }) { Text("زیرمجموعه " + mode.displayName) } }
                        }
                    }
                    node.children.forEach { child -> 
                        key(child.id) { RecursiveComprehensiveNode(child, path + child.id, node.subDistributionMode, target, viewModel, allAvailableNodes, isExecutionMode) }
                    }
                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addNode(target, path) } }) { Text("+ افزودن عضو زیرمجموعه") }
                }
            }
        } 
        // --- فاز اجرا (Execution Mode) ---
        else {
            if (node.canBeExcluded) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); color(Color("#D32F2F")); marginBottom(12.px); fontWeight("bold") } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(node.isExcluded); onChange { e -> viewModel.updateNode(target, path) { it.copy(isExcluded = e.value) } }; style { marginRight(4.px) } })
                    Text("حساب نشود؟ (حذف از محاسبه)")
                }
            }

            if (!node.isExcluded) {
                if (node.canBeTransferred) {
                    Div(attrs = { style { marginBottom(12.px); padding(8.px); backgroundColor(Color("#FFFDE7")); borderRadius(4.px); border(1.px, LineStyle.Dashed, Color("#FFEB3B")) } }) {
                        Label(attrs = { style { fontSize(0.85.cssRem); color(Color("#F57F17")); display(DisplayStyle.Block); marginBottom(4.px) } }) { Text("انتقال سهم این شخص به:") }
                        Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(4.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")) }; onChange { e -> viewModel.updateNode(target, path) { it.copy(transferredToId = e.value ?: "") } } }) {
                            Option(value = "", attrs = { if (node.transferredToId.isEmpty()) attr("selected", "true") }) { Text("بدون انتقال (خودش دریافت کند)") }
                            allAvailableNodes.filter { it.first != node.id }.forEach { (id, name) -> 
                                key(id) { Option(value = id, attrs = { if (node.transferredToId == id) attr("selected", "true") }) { Text(name.ifEmpty { "ناشناس" }) } }
                            }
                        }
                    }
                }

                if (node.hasSubDistribution) {
                    P(attrs = { style { fontSize(0.9.cssRem); color(Color("#2E7D32")); fontWeight("bold") } }) { Text("🔻 زیرمجموعه وارثین:") }
                    Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")) } }) {
                        node.children.forEach { child -> 
                            key(child.id) { RecursiveComprehensiveNode(child, path + child.id, node.subDistributionMode, target, viewModel, allAvailableNodes, isExecutionMode) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionStageScreen(viewModel: DistributionStageViewModel, agricultureInput: AgricultureInputState, customProfiles: List<CustomProfile>, onNavigateToBuilder: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Div(attrs = { style { backgroundColor(Color("white")); borderRadius(16.px); padding(24.px); margin(16.px); property("box-shadow", "0 4px 8px rgba(0,0,0,0.1)") } }) {
        H3(attrs = { style { color(Color("#2E7D32")); fontWeight("bold"); fontSize(1.2.cssRem); property("border-bottom", "2px solid #2E7D32"); paddingBottom(8.px); marginBottom(24.px); display(DisplayStyle.InlineBlock) } }) { Text("مرحله چهارم: موتور تسهیم قیاس") }

        if (agricultureInput.isNimehkari) {
            val p1Name = if (agricultureInput.partner1Name.isNotBlank()) agricultureInput.partner1Name else "شریک ۱"
            val p2Name = if (agricultureInput.partner2Name.isNotBlank()) agricultureInput.partner2Name else "شریک ۲"
            
            PoolSettingsCard("تنظیمات سهم $p1Name", PoolTarget.PARTNER_1, state.partner1PoolState, viewModel, agricultureInput, customProfiles, onNavigateToBuilder)
            
            val p1Strategy = DefaultCalculationsRegistry.strategies.find { it.title == state.partner1PoolState.defaultStrategyTitle }
            val isP1GlobalMacro = state.partner1PoolState.mode == DistributionMode.MODE_DEFAULT_MAKER && p1Strategy?.isGlobalMacro == true

            if (isP1GlobalMacro) {
                Div(attrs = { style { marginBottom(24.px); padding(20.px); border(2.px, LineStyle.Dashed, Color("#90CAF9")); borderRadius(12.px); backgroundColor(Color("#E3F2FD")); color(Color("#0D47A1")); textAlign("center"); fontWeight("bold"); fontSize(1.05.cssRem) } }) { Text("🔒 تنظیمات سهم $p2Name به صورت خودکار توسط محاسبه یکپارچه (${p1Strategy.title}) مدیریت و تسهیم می‌شود.") }
            } else {
                PoolSettingsCard("تنظیمات سهم $p2Name", PoolTarget.PARTNER_2, state.partner2PoolState, viewModel, agricultureInput, customProfiles, onNavigateToBuilder)
            }
        } else {
            PoolSettingsCard("تنظیمات تسهیم کل بار", PoolTarget.MAIN, state.mainPoolState, viewModel, agricultureInput, customProfiles, onNavigateToBuilder)
        }
    }
}

@Composable
fun PoolSettingsCard(title: String, target: PoolTarget, state: PoolDistributionState, viewModel: DistributionStageViewModel, agricultureInput: AgricultureInputState, customProfiles: List<CustomProfile>, onNavigateToBuilder: () -> Unit) {
    Div(attrs = { style { marginBottom(24.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#AED581")); borderRadius(12.px); backgroundColor(Color("#F1F8E9")) } }) {
        H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#1B5E20")) } }) { Text(title) }

        Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px); marginBottom(24.px) } }) {
            val modes = listOf(
                DistributionMode.MODE_A_NO_BREAKDOWN to "بدون خرد کردن (یکجا)",
                DistributionMode.MODE_COMPREHENSIVE to "بر اساس نفر / سهم / درصد",
                DistributionMode.MODE_DEFAULT_MAKER to "پیش‌فرض سازنده",
                DistributionMode.MODE_CUSTOM_BUILDER to "محاسبات اختصاصی"
            )
            modes.forEach { (mode, label) ->
                val isActive = state.mode == mode
                key(mode.name) {
                    Button(attrs = {
                        style { width(100.percent); padding(14.px); borderRadius(8.px); fontWeight(if (isActive) "bold" else "normal"); fontSize(if (isActive) 1.1.cssRem else 1.cssRem); color(if (isActive) Color("white") else Color("#2E7D32")); backgroundColor(if (isActive) Color("#2E7D32") else Color("white")); property("border", "1px solid #2E7D32"); property("cursor", "pointer") }
                        onClick { viewModel.updateMode(target, mode) }
                    }) { Text(label) }
                }
            }
        }

        Div(attrs = { style { padding(16.px); backgroundColor(Color("white")); borderRadius(8.px); property("border", "1px dashed #C5E1A5") } }) {
            when (state.mode) {
                DistributionMode.MODE_COMPREHENSIVE -> {
                    val compState = state.comprehensiveState
                    var savedTemplates by remember { mutableStateOf(DistributionTemplateRepository.getAllTemplates()) }
                    var newTemplateTitle by remember { mutableStateOf("") }
                    var activeExecutionTemplateId by remember { mutableStateOf<String?>(null) }
                    val isExecutionMode = activeExecutionTemplateId != null
                    
                    // --- پنل الگوهای ذخیره شده ---
                    if (savedTemplates.isNotEmpty()) {
                        Div(attrs = { style { marginBottom(24.px); padding(12.px); backgroundColor(Color("#F3E5F5")); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#CE93D8")) } }) {
                            H6(attrs = { style { marginTop(0.px); marginBottom(12.px); color(Color("#6A1B9A")) } }) { Text("💳 الگوهای آماده (دسترسی سریع)") }
                            Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px) } }) {
                                savedTemplates.forEach { template ->
                                    key(template.id) {
                                        Div(attrs = { style { backgroundColor(Color("white")); border(1.px, LineStyle.Solid, Color("#AB47BC")); borderRadius(8.px); padding(12.px) } }) {
                                            P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#4A148C")); fontWeight("bold") } }) { Text(template.title) }
                                            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px) } }) {
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#4CAF50")); color(Color("white")); border(0.px); borderRadius(4.px); padding(6.px); cursor("pointer") }
                                                    onClick { 
                                                        viewModel.updateComprehensiveState(target) { it.copy(rootMode = template.rootMode, countLimitInput = template.totalCountLimit, nodes = template.nodes) }
                                                        activeExecutionTemplateId = template.id
                                                    }
                                                }) { Text("انتخاب برای محاسبه") }
                                                
                                                Button(attrs = { style { flex(1); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(4.px); padding(6.px); cursor("pointer") }
                                                    onClick { 
                                                        viewModel.updateComprehensiveState(target) { it.copy(rootMode = template.rootMode, countLimitInput = template.totalCountLimit, nodes = template.nodes) }
                                                        activeExecutionTemplateId = null 
                                                    }
                                                }) { Text("ویرایش") }
                                                
                                                Button(attrs = { style { backgroundColor(Color("#F44336")); color(Color("white")); border(0.px); borderRadius(4.px); padding(6.px); cursor("pointer") }
                                                    onClick { 
                                                        if (window.confirm("آیا از حذف این الگو مطمئن هستید؟")) {
                                                            DistributionTemplateRepository.deleteTemplate(template.id)
                                                            savedTemplates = DistributionTemplateRepository.getAllTemplates()
                                                            if (activeExecutionTemplateId == template.id) activeExecutionTemplateId = null
                                                        }
                                                    }
                                                }) { Text("حذف") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isExecutionMode) {
                        Div(attrs = { style { backgroundColor(Color("#E8F5E9")); border(1.px, LineStyle.Solid, Color("#4CAF50")); padding(12.px); borderRadius(8.px); marginBottom(16.px); textAlign("center") } }) {
                            P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#2E7D32")); fontWeight("bold") } }) { Text("شما در حال استفاده از الگوی آماده (حالت اجرا) هستید.") }
                            Button(attrs = { style { backgroundColor(Color("white")); color(Color("#D32F2F")); border(1.px, LineStyle.Solid, Color("#D32F2F")); borderRadius(4.px); padding(6.px, 12.px); cursor("pointer") }; onClick { activeExecutionTemplateId = null } }) { Text("خروج از حالت اجرا و بازگشت به ویرایش") }
                        }
                    } else {
                        Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#81C784")); marginBottom(16.px); fontSize(1.cssRem) }; onChange { e -> ComprehensiveMode.entries.find { m -> m.name == e.value }?.let { m -> viewModel.updateComprehensiveState(target) { st -> st.copy(rootMode = m) } } } }) {
                            ComprehensiveMode.entries.forEach { mode -> 
                                key(mode.name) { Option(value = mode.name, attrs = { if (compState.rootMode == mode) attr("selected", "true") }) { Text(mode.displayName) } }
                            }
                        }
                    }

                    if (compState.rootMode == ComprehensiveMode.PERSON) {
                        DistTextInput("تعداد کل نفرات (الزامی)", compState.countLimitInput, true, isReadonly = isExecutionMode) { v -> viewModel.updateComprehensiveState(target) { it.copy(countLimitInput = v) } }
                    }

                    val allNodesFlat = flattenTree(compState.nodes)
                    var currentTotal = 0.0
                    compState.nodes.forEach { node ->
                        if (!node.isExcluded) {
                            val v = node.rawValue.toDoubleOrNull() ?: if (compState.rootMode == ComprehensiveMode.PERSON) 1.0 else 0.0
                            currentTotal += if (compState.rootMode == ComprehensiveMode.PERSON && node.isFemale) v * 0.5 else v
                        }
                    }
                    
                    val maxLimit = when (compState.rootMode) {
                        ComprehensiveMode.PERSON -> compState.countLimitInput.toDoubleOrNull() ?: 0.0
                        ComprehensiveMode.PERCENTAGE -> 100.0
                        ComprehensiveMode.SHARE_QYAS -> Double.MAX_VALUE
                    }
                    
                    val isLimitReached = maxLimit > 0 && currentTotal >= maxLimit
                    val isPersonAndEmpty = compState.rootMode == ComprehensiveMode.PERSON && maxLimit == 0.0

                    if (!isExecutionMode) {
                        if (isPersonAndEmpty) {
                            P(attrs = { style { color(Color("#D32F2F")); fontWeight("bold") } }) { Text("⚠️ فیلد تعداد نفرات الزامی است. لطفاً آن را پر کنید.") }
                        } else if (isLimitReached) {
                            P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(8.px); borderRadius(4.px); fontSize(0.9.cssRem); fontWeight("bold"); margin(12.px, 0.px) } }) { 
                                Text(if (compState.rootMode == ComprehensiveMode.PERCENTAGE) "خطا: جمع درصدها نمی‌تواند بیشتر از ۱۰۰ باشد!" else "اخطار: سقف نفرات پر شده است.") 
                            }
                        }
                    }

                    // رندر امن گره‌ها با استفاده از key
                    compState.nodes.forEach { node ->
                        key(node.id) {
                            RecursiveComprehensiveNode(node, listOf(node.id), compState.rootMode, target, viewModel, allNodesFlat, isExecutionMode)
                        }
                    }

                    if (!isExecutionMode && !isLimitReached && !isPersonAndEmpty) {
                        Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "2px dashed #4CAF50"); borderRadius(8.px); padding(12.px); fontWeight("bold"); property("cursor", "pointer"); marginTop(16.px) }; onClick { viewModel.addNode(target, emptyList()) } }) { Text("+ افزودن شریک جدید") }
                    }

                    if (!isExecutionMode && compState.nodes.isNotEmpty()) {
                        Div(attrs = { style { marginTop(24.px); padding(16.px); backgroundColor(Color("#FFF3E0")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#FFB74D")) } }) {
                            H6(attrs = { style { marginTop(0.px); marginBottom(12.px); color(Color("#E65100")) } }) { Text("💾 ذخیره ساختار برای دفعات بعد") }
                            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); alignItems(AlignItems.Center) } }) {
                                Div(attrs = { style { flex(2) } }) { DistTextInput("نام الگو (مثلاً: شرکای باغ)", newTemplateTitle, false) { newTemplateTitle = it } }
                                Button(attrs = { style { flex(1); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(8.px); padding(14.px); fontWeight("bold"); cursor("pointer") }; onClick {
                                    if (newTemplateTitle.isNotBlank()) {
                                        val template = SavedDistributionTemplate(id = kotlin.random.Random.nextInt().toString() + "_" + kotlin.js.Date.now().toString(), title = newTemplateTitle, rootMode = compState.rootMode, totalCountLimit = compState.countLimitInput, nodes = compState.nodes, createdAt = kotlin.js.Date.now().toLong())
                                        DistributionTemplateRepository.saveTemplate(template)
                                        savedTemplates = DistributionTemplateRepository.getAllTemplates()
                                        newTemplateTitle = "" 
                                    }
                                }}) { Text("ذخیره") }
                            }
                        }
                    }
                }
                DistributionMode.MODE_A_NO_BREAKDOWN -> { DistTextInput("نام گروه یا شخص گیرنده (اختیاری)", state.groupName, false) { viewModel.updateGroupName(target, it) } }
                DistributionMode.MODE_DEFAULT_MAKER -> { Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); property("border", "1px solid #BDBDBD"); fontSize(1.cssRem); fontFamily("inherit") }; onChange { event -> viewModel.updateDefaultStrategy(target, event.value ?: "") } }) { Option(value = "", attrs = { if (state.defaultStrategyTitle.isEmpty()) { attr("selected", "true"); attr("disabled", "true") } }) { Text("انتخاب...") } ; DefaultCalculationsRegistry.strategies.forEach { strategy -> key(strategy.title) { Option(value = strategy.title, attrs = { if (state.defaultStrategyTitle == strategy.title) attr("selected", "true") }) { Text(strategy.title) } } } } }
                DistributionMode.MODE_CUSTOM_BUILDER -> { Button(attrs = { style { width(100.percent); padding(12.px); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(8.px); fontWeight("bold"); cursor("pointer"); fontSize(1.cssRem) }; onClick { onNavigateToBuilder() } }) { Text("➕ افزودن محاسبه اختصاصی جدید") } }
                else -> { P(attrs = { style { color(Color("#D32F2F")) } }) { Text("گزینه برای سازگاری گذشته.") } }
            }
        }
    }
}
