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
private fun DistTextInput(label: String, value: String, isNumber: Boolean = false, onValueChange: (String) -> Unit) {
    Div(attrs = { classes(AppStyleSheet.floatingContainer); style { marginBottom(0.px); width(100.percent) } }) {
        Input(type = InputType.Text, attrs = {
            classes(AppStyleSheet.floatingInput); classes("floating-input")
            if (isNumber) attr("inputmode", "decimal")
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
fun RecursiveComprehensiveNode(node: ShareholderNode, path: List<String>, currentMode: ComprehensiveMode, target: PoolTarget, viewModel: DistributionStageViewModel, allAvailableNodes: List<Pair<String, String>>) {
    // تغییر رنگ حاشیه اگر غیرفعال باشد
    val borderColor = if (node.isActive) "#4CAF50" else "#BDBDBD"
    val bgColor = if (node.isActive) "#F8FBF8" else "#F5F5F5"

    Div(attrs = { style { padding(12.px); marginTop(12.px); property("border-right", "4px solid $borderColor"); backgroundColor(Color(bgColor)); borderRadius(4.px) } }) {
        
        // ردیف اول: فعال بودن، نام و مقدار
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(12.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer") } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.isActive); onChange { e -> viewModel.updateNode(target, path) { it.copy(isActive = e.value) } } })
            }
            Div(attrs = { style { flex(2) } }) { DistTextInput("نام شریک", node.name, false) { v -> viewModel.updateNode(target, path) { it.copy(name = v) } } }
            
            if (currentMode != ComprehensiveMode.PERSON) {
                Div(attrs = { style { flex(1) } }) { DistTextInput(if (currentMode == ComprehensiveMode.PERCENTAGE) "درصد" else "قیاس", node.rawValue, true) { v -> viewModel.updateNode(target, path) { it.copy(rawValue = v) } } }
            } else {
                Label(attrs = { style { flex(1); display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(node.isFemale); onChange { e -> viewModel.updateNode(target, path) { it.copy(isFemale = e.value) } }; style { marginRight(4.px) } })
                    Text("دختر (۰.۵)")
                }
            }
            Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeNode(target, path.dropLast(1), node.id) } }) { Text("-") }
        }

        // ردیف دوم و سوم فقط در صورت فعال بودن شخص نمایش داده می‌شود
        if (node.isActive) {
            Div(attrs = { style { marginBottom(12.px); padding(8.px); backgroundColor(Color("#FFFDE7")); borderRadius(4.px); border(1.px, LineStyle.Dashed, Color("#FFEB3B")) } }) {
                Label(attrs = { style { fontSize(0.85.cssRem); color(Color("#F57F17")); display(DisplayStyle.Block); marginBottom(4.px) } }) { Text("انتقال سهم این شخص به:") }
                Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(4.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")) }; onChange { e -> viewModel.updateNode(target, path) { it.copy(transferredToId = e.value ?: "") } } }) {
                    Option(value = "", attrs = { if (node.transferredToId.isEmpty()) attr("selected", "true") }) { Text("بدون انتقال (خودش دریافت کند)") }
                    allAvailableNodes.filter { it.first != node.id }.forEach { (id, name) ->
                        Option(value = id, attrs = { if (node.transferredToId == id) attr("selected", "true") }) { Text(name.ifEmpty { "ناشناس" }) }
                    }
                }
            }

            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); fontWeight("bold"); color(Color("#2E7D32")) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.hasSubDistribution); onChange { e -> viewModel.updateNode(target, path) { it.copy(hasSubDistribution = e.value) } }; style { marginRight(8.px) } })
                Text("تقسیم جزئی (وارث جدید)؟")
            }

            if (node.hasSubDistribution) {
                Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")) } }) {
                    Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(4.px); border(1.px, LineStyle.Solid, Color("#81C784")); marginBottom(8.px) }; onChange { e -> ComprehensiveMode.entries.find { m -> m.name == e.value }?.let { m -> viewModel.updateNode(target, path) { it.copy(subDistributionMode = m) } } } }) {
                        ComprehensiveMode.entries.forEach { mode ->
                            Option(value = mode.name, attrs = { if (node.subDistributionMode == mode) attr("selected", "true") }) { Text("زیرمجموعه " + mode.displayName) }
                        }
                    }
                    
                    node.children.forEach { child -> RecursiveComprehensiveNode(child, path + child.id, node.subDistributionMode, target, viewModel, allAvailableNodes) }
                    
                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addNode(target, path) } }) { Text("+ افزودن عضو زیرمجموعه") }
                }
            }
        } else {
            P(attrs = { style { margin(0.px); fontSize(0.85.cssRem); color(Color("#9E9E9E")) } }) { Text("این شخص موقتاً از محاسبه خط خورده است.") }
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
                Div(attrs = { style { marginBottom(24.px); padding(20.px); border(2.px, LineStyle.Dashed, Color("#90CAF9")); borderRadius(12.px); backgroundColor(Color("#E3F2FD")); color(Color("#0D47A1")); textAlign("center"); fontWeight("bold"); fontSize(1.05.cssRem) } }) {
                    Text("🔒 تنظیمات سهم $p2Name به صورت خودکار توسط محاسبه یکپارچه (${p1Strategy.title}) مدیریت و تسهیم می‌شود.")
                }
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
                Button(attrs = {
                    style {
                        width(100.percent); padding(14.px); borderRadius(8.px); fontWeight(if (isActive) "bold" else "normal")
                        fontSize(if (isActive) 1.1.cssRem else 1.cssRem)
                        color(if (isActive) Color("white") else Color("#2E7D32")); backgroundColor(if (isActive) Color("#2E7D32") else Color("white"))
                        property("border", "1px solid #2E7D32"); property("cursor", "pointer")
                    }
                    onClick { viewModel.updateMode(target, mode) }
                }) { Text(label) }
            }
        }

        Div(attrs = { style { padding(16.px); backgroundColor(Color("white")); borderRadius(8.px); property("border", "1px dashed #C5E1A5") } }) {
            when (state.mode) {
                DistributionMode.MODE_COMPREHENSIVE -> {
                    val compState = state.comprehensiveState
                    
                    // استیت‌های لوکال برای ذخیره و بازیابی
                    var savedTemplates by remember { mutableStateOf(DistributionTemplateRepository.getAllTemplates()) }
                    var newTemplateTitle by remember { mutableStateOf("") }
                    
                    // --- بخش دسترسی سریع (کارت‌های ذخیره‌شده) ---
                    if (savedTemplates.isNotEmpty()) {
                        Div(attrs = { style { marginBottom(24.px); padding(12.px); backgroundColor(Color("#F3E5F5")); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#CE93D8")) } }) {
                            H6(attrs = { style { marginTop(0.px); marginBottom(12.px); color(Color("#6A1B9A")) } }) { Text("💳 الگوهای آماده (دسترسی سریع)") }
                            Div(attrs = { style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px) } }) {
                                savedTemplates.forEach { template ->
                                    Div(attrs = { 
                                        style { 
                                            display(DisplayStyle.Flex); alignItems(AlignItems.Center); 
                                            backgroundColor(Color("white")); border(1.px, LineStyle.Solid, Color("#AB47BC")); 
                                            borderRadius(16.px); padding(6.px, 12.px); cursor("pointer") 
                                        }
                                    }) {
                                        Span(attrs = { 
                                            style { fontSize(0.95.cssRem); color(Color("#4A148C")); fontWeight("bold") }
                                            onClick {
                                                viewModel.updateComprehensiveState(target) { 
                                                    it.copy(rootMode = template.rootMode, countLimitInput = template.totalCountLimit, nodes = template.nodes) 
                                                }
                                            }
                                        }) { Text(template.title) }
                                        
                                        Span(attrs = { 
                                            style { marginLeft(12.px); color(Color("#D32F2F")); fontWeight("bold"); fontSize(1.2.cssRem) }
                                            onClick {
                                                DistributionTemplateRepository.deleteTemplate(template.id)
                                                savedTemplates = DistributionTemplateRepository.getAllTemplates()
                                            }
                                        }) { Text("×") }
                                    }
                                }
                            }
                        }
                    }

                    // --- تنظیمات اصلی موتور ---
                    Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#81C784")); marginBottom(16.px); fontSize(1.cssRem) }; onChange { e -> ComprehensiveMode.entries.find { m -> m.name == e.value }?.let { m -> viewModel.updateComprehensiveState(target) { st -> st.copy(rootMode = m) } } } }) {
                        ComprehensiveMode.entries.forEach { mode ->
                            Option(value = mode.name, attrs = { if (compState.rootMode == mode) attr("selected", "true") }) { Text(mode.displayName) }
                        }
                    }

                    if (compState.rootMode == ComprehensiveMode.PERSON) {
                        DistTextInput("تعداد کل نفرات (اختیاری)", compState.countLimitInput, true) { v -> viewModel.updateComprehensiveState(target) { it.copy(countLimitInput = v) } }
                    }

                    val allNodesFlat = flattenTree(compState.nodes)
                    
                    compState.nodes.forEach { node ->
                        RecursiveComprehensiveNode(node, listOf(node.id), compState.rootMode, target, viewModel, allNodesFlat)
                    }

                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "2px dashed #4CAF50"); borderRadius(8.px); padding(12.px); fontWeight("bold"); property("cursor", "pointer"); marginTop(16.px) }; onClick { viewModel.addNode(target, emptyList()) } }) { Text("+ افزودن شریک جدید") }

                    // --- بخش ذخیره الگو برای استفاده بعدی ---
                    if (compState.nodes.isNotEmpty()) {
                        Div(attrs = { style { marginTop(24.px); padding(16.px); backgroundColor(Color("#FFF3E0")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#FFB74D")) } }) {
                            H6(attrs = { style { marginTop(0.px); marginBottom(12.px); color(Color("#E65100")) } }) { Text("💾 ذخیره این ترکیب برای دفعات بعد") }
                            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); alignItems(AlignItems.Center) } }) {
                                Div(attrs = { style { flex(2) } }) {
                                    DistTextInput("نام الگو (مثلاً: شرکای باغ بالا)", newTemplateTitle, false) { newTemplateTitle = it }
                                }
                                Button(attrs = {
                                    style { 
                                        flex(1); backgroundColor(Color("#FF9800")); color(Color("white")); 
                                        border(0.px); borderRadius(8.px); padding(14.px); fontWeight("bold"); cursor("pointer") 
                                    }
                                    onClick {
                                        if (newTemplateTitle.isNotBlank()) {
                                            val template = SavedDistributionTemplate(
                                                id = kotlin.random.Random.nextInt().toString() + "_" + kotlin.js.Date.now().toString(),
                                                title = newTemplateTitle,
                                                rootMode = compState.rootMode,
                                                totalCountLimit = compState.countLimitInput,
                                                nodes = compState.nodes,
                                                createdAt = kotlin.js.Date.now().toLong()
                                            )
                                            DistributionTemplateRepository.saveTemplate(template)
                                            savedTemplates = DistributionTemplateRepository.getAllTemplates()
                                            newTemplateTitle = "" 
                                        }
                                    }
                                }) { Text("ذخیره الگو") }
                            }
                        }
                    }
                }
                
                DistributionMode.MODE_A_NO_BREAKDOWN -> {
                    DistTextInput("نام گروه یا شخص گیرنده (اختیاری)", state.groupName, false) { viewModel.updateGroupName(target, it) }
                    P(attrs = { style { fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) { Text("در این حالت کل سهم این بخش بدون تغییر به نام وارد شده اختصاص می‌یابد.") }
                }
                
                DistributionMode.MODE_DEFAULT_MAKER -> {
                    Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); property("border", "1px solid #BDBDBD"); fontSize(1.cssRem); fontFamily("inherit") }; onChange { event -> viewModel.updateDefaultStrategy(target, event.value ?: "") } }) {
                        Option(value = "", attrs = { if (state.defaultStrategyTitle.isEmpty()) { attr("selected", "true"); attr("disabled", "true") } }) { Text("محاسبات پیش‌فرض سازنده را انتخاب کنید...") }
                        DefaultCalculationsRegistry.strategies.forEach { strategy -> Option(value = strategy.title, attrs = { if (state.defaultStrategyTitle == strategy.title) attr("selected", "true") }) { Text(strategy.title) } }
                    }
                    if (state.defaultStrategyTitle == "دانگ ماریکی(کِجِینو)") {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(16.px); fontWeight("bold") } }) { Input(type = InputType.Checkbox, attrs = { checked(state.calculateZivar); onChange { event -> viewModel.updateCalculateZivar(target, event.value) }; style { marginRight(8.px); width(20.px); height(20.px) } }); Text("سهم زیور/نواب حساب شود؟") }
                    }
                }
                
                DistributionMode.MODE_CUSTOM_BUILDER -> {
                    Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontSize(1.cssRem); fontFamily("inherit"); marginBottom(12.px) }; onChange { e -> viewModel.updateCustomProfile(target, e.value ?: "") } }) {
                        val dependentProfiles = customProfiles.filter { it.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4 }
                        if (dependentProfiles.isEmpty()) {
                            Option(value = "", attrs = { attr("disabled", "true"); attr("selected", "true") }) { Text("هنوز هیچ الگوی وابسته‌ای ساخته نشده است.") }
                        } else {
                            Option(value = "", attrs = { attr("disabled", "true"); if(state.customProfileId.isEmpty()) attr("selected", "true") }) { Text("یک الگوی اختصاصی انتخاب کنید...") }
                            dependentProfiles.forEach { prof -> Option(value = prof.id, attrs = { if(state.customProfileId == prof.id) attr("selected", "true") }) { Text(prof.name) } }
                        }
                    }
                    Button(attrs = { style { width(100.percent); padding(12.px); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(8.px); fontWeight("bold"); cursor("pointer"); fontSize(1.cssRem) }; onClick { onNavigateToBuilder() } }) { Text("➕ افزودن محاسبه اختصاصی جدید") }
                }
                
                else -> {
                    P(attrs = { style { color(Color("#D32F2F")) } }) { Text("این گزینه برای سازگاری با تاریخچه گذشته است. لطفاً از گزینه جامع استفاده کنید.") }
                }
            }
        }
    }
}
