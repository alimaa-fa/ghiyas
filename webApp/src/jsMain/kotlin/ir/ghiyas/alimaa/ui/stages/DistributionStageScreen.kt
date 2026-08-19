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
import ir.ghiyas.alimaa.domain.strategy.PersonNode
import ir.ghiyas.alimaa.domain.models.CustomProfile
import ir.ghiyas.alimaa.domain.models.ProfileIntegrationType
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

@Composable
fun RecursivePersonNode(node: PersonNode, path: List<String>, target: PoolTarget, viewModel: DistributionStageViewModel) {
    // ... [محتوای این تابع دقیقاً مشابه قبل است، هیچ تغییری ندارد]
    Div(attrs = { style { padding(12.px); marginTop(8.px); property("border-left", "4px solid #81C784"); backgroundColor(Color("#F8FBF8")); borderRadius(4.px) } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(8.px) } }) {
            Div(attrs = { style { flex(2) } }) { DistTextInput("نام شخص", node.name, false) { v -> viewModel.updatePersonNode(target, path) { it.copy(name = v) } } }
            Label(attrs = { style { flex(1); display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.isFemale); onChange { event -> viewModel.updatePersonNode(target, path) { it.copy(isFemale = event.value) } }; style { marginRight(4.px) } })
                Text("دختر (۰.۵)")
            }
            Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removePersonNode(target, path.dropLast(1), node.id) } }) { Text("-") }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px) } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isSubDivided); onChange { event -> viewModel.updatePersonNode(target, path) { it.copy(isSubDivided = event.value) } }; style { marginRight(8.px) } })
            Text("آیا سهم این شخص در خودش خرد می‌شود؟")
        }

        if (node.isSubDivided) {
            Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")) } }) {
                DistTextInput("تعداد نفرات زیرمجموعه", node.subCountInput, true) { v -> viewModel.updatePersonNode(target, path) { it.copy(subCountInput = v) } }
                val maxLimit = node.subCountInput.toDoubleOrNull() ?: 0.0
                val currentSum = node.subNodes.sumOf { it.weight }
                val hasDecimal = (maxLimit > 0 && maxLimit % 1.0 != 0.0)

                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(node.isDetailedFurther); onChange { event -> viewModel.updatePersonNode(target, path) { it.copy(isDetailedFurther = event.value) } }; style { marginRight(8.px) } })
                    Text("تقسیم جزئی‌تر؟ (درختی)")
                }

                if (!node.isDetailedFurther) {
                    if (!hasDecimal) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(node.isSubBoyGirlSplit); onChange { event -> viewModel.updatePersonNode(target, path) { it.copy(isSubBoyGirlSplit = event.value) } }; style { marginRight(8.px) } })
                            Text("تسهیم پسر و دختری؟")
                        }
                    }
                } else {
                    if (currentSum > maxLimit) {
                        P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); fontWeight("bold"); margin(4.px, 0.px) } }) { Text("خطا: مجموع سهم زیرمجموعه ($currentSum) از حد مجاز ($maxLimit) بیشتر است!") }
                    }
                    node.subNodes.forEach { child -> RecursivePersonNode(child, path + child.id, target, viewModel) }
                    if (currentSum + 0.5 <= maxLimit) {
                        Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addPersonNode(target, path) } }) { Text("+ افزودن عضو به ${if (node.name.isEmpty()) "شخص" else node.name}") }
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
fun PoolSettingsCard(
    title: String, target: PoolTarget, state: PoolDistributionState, 
    viewModel: DistributionStageViewModel, agricultureInput: AgricultureInputState, 
    customProfiles: List<CustomProfile>, onNavigateToBuilder: () -> Unit
) {
    Div(attrs = { style { marginBottom(24.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#AED581")); borderRadius(12.px); backgroundColor(Color("#F1F8E9")) } }) {
        H4(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#1B5E20")) } }) { Text(title) }

        Div(attrs = { style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px); marginBottom(24.px) } }) {
            val modes = listOf(
                DistributionMode.MODE_B_SIMPLE to "بر اساس نفر",
                DistributionMode.MODE_A_NO_BREAKDOWN to "بدون خرد کردن",
                DistributionMode.MODE_C_GHIYAS to "بر اساس قیاس",
                DistributionMode.MODE_DEFAULT_MAKER to "پیش‌فرض سازنده",
                DistributionMode.MODE_CUSTOM_BUILDER to "محاسبات اختصاصی"
            )
            modes.forEach { (mode, label) ->
                val isActive = state.mode == mode
                Button(attrs = {
                    style {
                        flex(1); minWidth(120.px); padding(10.px); borderRadius(8.px); fontWeight(if (isActive) "bold" else "normal")
                        color(if (isActive) Color("white") else Color("#2E7D32")); backgroundColor(if (isActive) Color("#2E7D32") else Color("white"))
                        property("border", "1px solid #2E7D32"); property("cursor", "pointer")
                    }
                    onClick { viewModel.updateMode(target, mode) }
                }) { Text(label) }
            }
        }

        Div(attrs = { style { padding(16.px); backgroundColor(Color("white")); borderRadius(8.px); property("border", "1px dashed #C5E1A5") } }) {
            when (state.mode) {
                DistributionMode.MODE_CUSTOM_BUILDER -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("انتخاب محاسبات اختصاصی (وابسته)") }
                    
                    // لیست الگوها فیلتر و رندر می‌شود
                    val dependentProfiles = customProfiles.filter { it.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4 }
                    
                    Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontSize(1.cssRem); fontFamily("inherit"); marginBottom(12.px) }; onChange { e -> viewModel.updateCustomProfile(target, e.value ?: "") } }) {
                        if (dependentProfiles.isEmpty()) {
                            Option(value = "", attrs = { attr("disabled", "true"); attr("selected", "true") }) { Text("هنوز هیچ الگوی وابسته‌ای ساخته نشده است.") }
                        } else {
                            Option(value = "", attrs = { attr("disabled", "true"); if(state.customProfileId.isEmpty()) attr("selected", "true") }) { Text("یک الگوی اختصاصی انتخاب کنید...") }
                            dependentProfiles.forEach { prof ->
                                Option(value = prof.id, attrs = { if(state.customProfileId == prof.id) attr("selected", "true") }) { Text(prof.name) }
                            }
                        }
                    }
                    Button(attrs = { style { width(100.percent); padding(12.px); backgroundColor(Color("#FF9800")); color(Color("white")); border(0.px); borderRadius(8.px); fontWeight("bold"); cursor("pointer"); fontSize(1.cssRem) }; onClick { onNavigateToBuilder() } }) { Text("➕ افزودن محاسبه اختصاصی جدید") }
                }
                // ... سایر حالت‌ها دقیقاً مطابق قبل هستند و تغییری نکرده‌اند ...
                DistributionMode.MODE_A_NO_BREAKDOWN -> {
                    DistTextInput("نام گروه یا شخص گیرنده (اختیاری)", state.groupName, false) { viewModel.updateGroupName(target, it) }
                    P(attrs = { style { fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) { Text("در این حالت کل سهم این بخش بدون تغییر به نام وارد شده اختصاص می‌یابد.") }
                }
                DistributionMode.MODE_B_SIMPLE -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("تنظیمات محاسبه (بر اساس نفر)") }
                    val bState = state.modeBState
                    DistTextInput("تعداد کل نفرات", bState.countInput, true) { v -> viewModel.updateModeBState(target) { it.copy(countInput = v) } }
                    val maxLimit = bState.countInput.toDoubleOrNull() ?: 0.0
                    val currentSum = bState.children.sumOf { it.weight }
                    val hasDecimal = (maxLimit > 0 && maxLimit % 1.0 != 0.0)

                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontWeight("bold"); marginTop(16.px) } }) {
                        Input(type = InputType.Checkbox, attrs = { checked(bState.isDetailed); onChange { event -> viewModel.updateModeBState(target) { st -> st.copy(isDetailed = event.value) } }; style { marginRight(12.px); width(20.px); height(20.px) } })
                        Text("تقسیم جزئی شود؟ (ساختار درختی)")
                    }

                    if (!bState.isDetailed) {
                        if (!hasDecimal) {
                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontWeight("bold"); marginTop(16.px) } }) {
                                Input(type = InputType.Checkbox, attrs = { checked(bState.isBoyGirlSplit); onChange { event -> viewModel.updateModeBState(target) { st -> st.copy(isBoyGirlSplit = event.value) } }; style { marginRight(12.px); width(20.px); height(20.px) } })
                                Text("تسهیم پسر و دختری؟")
                            }
                        }
                    } else {
                        if (currentSum > maxLimit) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(8.px); borderRadius(4.px); fontSize(0.9.cssRem); fontWeight("bold"); margin(12.px, 0.px) } }) { Text("خطا: مجموع وزن افراد ($currentSum) از حد مجاز ($maxLimit) فراتر رفته است!") } }
                        bState.children.forEach { node -> RecursivePersonNode(node, listOf(node.id), target, viewModel) }
                        if (currentSum + 0.5 <= maxLimit) { Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "2px dashed #4CAF50"); borderRadius(8.px); padding(12.px); fontWeight("bold"); property("cursor", "pointer"); marginTop(16.px) }; onClick { viewModel.addPersonNode(target, emptyList()) } }) { Text("+ افزودن شخص جدید") } }
                    }
                }
                DistributionMode.MODE_C_GHIYAS -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("تسهیم بر اساس قیاس (وزن اختصاصی)") }
                    state.shareholders.forEachIndexed { index, shareholder ->
                        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(12.px) } }) {
                            Div(attrs = { style { flex(2) } }) { DistTextInput("نام سهام‌دار", shareholder.name, false) { n -> viewModel.updateShareholder(target, index, n, shareholder.ghiyasInput) } }
                            Div(attrs = { style { flex(1) } }) { DistTextInput("قیاس", shareholder.ghiyasInput, true) { q -> viewModel.updateShareholder(target, index, shareholder.name, q) } }
                            if (state.shareholders.size > 1) { Button(attrs = { style { backgroundColor(Color("#D32F2F")); color(Color("white")); border(0.px); borderRadius(4.px); padding(10.px, 16.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeShareholder(target, index) } }) { Text("-") } }
                        }
                    }
                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "2px dashed #4CAF50"); borderRadius(8.px); padding(12.px); fontWeight("bold"); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addShareholder(target) } }) { Text("+ افزودن سهام‌دار جدید") }
                }
                DistributionMode.MODE_DEFAULT_MAKER -> {
                    H5(attrs = { style { marginTop(0.px); marginBottom(16.px); color(Color("#424242")) } }) { Text("انتخاب از محاسبات آماده") }
                    Select(attrs = { style { width(100.percent); padding(12.px); borderRadius(8.px); property("border", "1px solid #BDBDBD"); fontSize(1.cssRem); fontFamily("inherit") }; onChange { event -> viewModel.updateDefaultStrategy(target, event.value ?: "") } }) {
                        Option(value = "", attrs = { if (state.defaultStrategyTitle.isEmpty()) { attr("selected", "true"); attr("disabled", "true") } }) { Text("محاسبات پیش‌فرض سازنده را انتخاب کنید...") }
                        DefaultCalculationsRegistry.strategies.forEach { strategy -> Option(value = strategy.title, attrs = { if (state.defaultStrategyTitle == strategy.title) attr("selected", "true") }) { Text(strategy.title) } }
                    }
                    if (state.defaultStrategyTitle == "دانگ ماریکی(کِجِینو)") {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(16.px); fontWeight("bold") } }) { Input(type = InputType.Checkbox, attrs = { checked(state.calculateZivar); onChange { event -> viewModel.updateCalculateZivar(target, event.value) }; style { marginRight(8.px); width(20.px); height(20.px) } }); Text("سهم زیور/نواب حساب شود؟") }
                    }
                    if (state.defaultStrategyTitle == "عبدالرحیم(کِجینو)") {
                        H6(attrs = { style { marginTop(16.px); marginBottom(6.px); color(Color("#424242")) } }) { Text("انتخاب گروه هدف عبدالرحیم:") }
                        Select(attrs = { style { width(100.percent); padding(10.px); borderRadius(8.px); marginBottom(12.px); border(1.px, LineStyle.Solid, Color("#2E7D32")) }; onChange { event -> viewModel.updateTargetGroup(target, event.value ?: "کل عبدالرحیمی‌ها") } }) {
                            Option(value = "کل عبدالرحیمی‌ها", attrs = { if (state.targetGroup == "کل عبدالرحیمی‌ها") attr("selected", "true") }) { Text("کل عبدالرحیمی‌ها") }
                            Option(value = "مابین نوری و صغری", attrs = { if (state.targetGroup == "مابین نوری و صغری") attr("selected", "true") }) { Text("مابین نوری و صغری") }
                        }
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginBottom(12.px); fontWeight("bold") } }) { Input(type = InputType.Checkbox, attrs = { checked(state.calculateZivar); onChange { event -> viewModel.updateCalculateZivar(target, event.value) }; style { marginRight(8.px); width(20.px); height(20.px) } }); Text("سهم زیور(نواب) حساب شود؟") }
                        if (agricultureInput.isNimehkari) {
                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("pointer", "cursor"); fontWeight("bold") } }) { Input(type = InputType.Checkbox, attrs = { checked(state.transferDadallah); onChange { event -> viewModel.updateTransferDadallah(target, event.value) }; style { marginRight(8.px); width(20.px); height(20.px) } }); Text("سهم دادالله(نیمه‌کاری) به عبدالرحیم منتقل شود؟") }
                        }
                    }
                }
            }
        }
    }
}
