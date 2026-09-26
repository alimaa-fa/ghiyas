package ghiyas.alimaa.fa.ui.player

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ghiyas.alimaa.fa.domain.models.*
import ghiyas.alimaa.fa.presentation.player.DynamicPlayerViewModel
import ghiyas.alimaa.fa.presentation.player.DynamicPlayerState
import kotlin.js.Date

fun extractAllDynamicNodes(blocks: List<CustomBlock>): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    fun extractPersons(nodes: List<BuilderPersonNode>) {
        nodes.forEach {
            result.add(it.id to it.name)
            extractPersons(it.subNodes)
        }
    }
    fun traverse(bList: List<CustomBlock>) {
        bList.forEach { b ->
            when (b) {
                is MemberBlock -> {
                    extractPersons(b.headcountNodes)
                    b.ghiyasShareholders.forEach { result.add(it.id to it.name) }
                    b.percentageShareholders.forEach { result.add(it.id to it.name) }
                    traverse(b.childBlocks)
                }
                is PartnerBlock -> {
                    extractPersons(b.headcountNodes)
                    b.ghiyasShareholders.forEach { result.add(it.id to it.name) }
                    b.percentageShareholders.forEach { result.add(it.id to it.name) }
                    traverse(b.siblingBlocks)
                }
                is StageBlock -> traverse(b.childBlocks)
                is ConditionGate -> traverse(b.childBlocks)
                is BaseInputBlock -> traverse(b.childBlocks)
                else -> {}
            }
        }
    }
    traverse(blocks)
    return result
}

// کامپوننت مشترک برای پنل پیشرفته انتقال سهم
@Composable
fun AdvancedTransferPanel(
    sourceId: String,
    sourceName: String,
    action: RuntimeTransferAction,
    allAvailableNodes: List<Pair<String, String>>,
    onUpdateType: (TransferAmountType) -> Unit,
    onUpdateValue: (String) -> Unit,
    onUpdateRule: (TransferDistributionRule) -> Unit,
    onAddTarget: (String, Boolean) -> Unit,
    onRemoveTarget: (String) -> Unit
) {
    Div(attrs = { style { padding(12.px); backgroundColor(Color("#FFFDE7")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#FBC02D")); marginTop(8.px) } }) {
        H5(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#F57F17")) } }) { Text("انتقال پیشرفته سهم: $sourceName") }

        Div(attrs = { style { marginBottom(12.px) } }) {
            Label(attrs = { style { fontSize(0.9.cssRem); fontWeight("bold"); display(DisplayStyle.Block); marginBottom(4.px); color(Color("#424242")) } }) { Text("مقدار کسر از مبدأ:") }
            Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")) }; onChange { e -> onUpdateType(TransferAmountType.valueOf(e.target.value)) } }) {
                Option(value = "FULL", attrs = { if(action.sourceAmountType == TransferAmountType.FULL) attr("selected","true") }) { Text("کل سهم (کامل)") }
                Option(value = "PERCENTAGE", attrs = { if(action.sourceAmountType == TransferAmountType.PERCENTAGE) attr("selected","true") }) { Text("درصدی از سهم") }
                Option(value = "FIXED", attrs = { if(action.sourceAmountType == TransferAmountType.FIXED) attr("selected","true") }) { Text("مقدار ثابت (مثلا ۲.۵)") }
                Option(value = "FORMULA", attrs = { if(action.sourceAmountType == TransferAmountType.FORMULA) attr("selected","true") }) { Text("فرمول (مثلا [باقیمانده]/2)") }
            }
        }

        if (action.sourceAmountType != TransferAmountType.FULL) {
            Div(attrs = { style { marginBottom(12.px) } }) {
                Input(type = InputType.Text, attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); property("box-sizing", "border-box") }; placeholder(if(action.sourceAmountType == TransferAmountType.FORMULA) "فرمول ریاضی" else "مقدار را وارد کنید"); value(action.sourceAmountValue); onInput { e -> onUpdateValue(e.value) } })
            }
        }

        Div(attrs = { style { marginBottom(12.px) } }) {
            Label(attrs = { style { fontSize(0.9.cssRem); fontWeight("bold"); display(DisplayStyle.Block); marginBottom(4.px); color(Color("#424242")) } }) { Text("نحوه تقسیم بین گیرندگان:") }
            Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")) }; onChange { e -> onUpdateRule(TransferDistributionRule.valueOf(e.target.value)) } }) {
                Option(value = "EQUAL", attrs = { if(action.distributionRule == TransferDistributionRule.EQUAL) attr("selected","true") }) { Text("مساوی بین همه") }
                Option(value = "BOY_GIRL", attrs = { if(action.distributionRule == TransferDistributionRule.BOY_GIRL) attr("selected","true") }) { Text("پسر و دختری (نسبت ۲ به ۱)") }
            }
        }

        if (action.targets.isNotEmpty()) {
            Div(attrs = { style { padding(8.px); backgroundColor(Color("white")); borderRadius(6.px); marginBottom(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")) } }) {
                Label(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold"); color(Color("#1B5E20")); display(DisplayStyle.Block); marginBottom(8.px) } }) { Text("لیست گیرندگان:") }
                action.targets.forEach { t ->
                    val targetName = allAvailableNodes.find { it.first == t.targetId }?.second ?: "ناشناس"
                    Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); padding(6.px); property("border-bottom", "1px dashed #E0E0E0") } }) {
                        Span(attrs = { style { fontSize(0.9.cssRem); color(Color("#424242")) } }) { Text("- $targetName" + if(t.isFemale && action.distributionRule == TransferDistributionRule.BOY_GIRL) " (دختر/نیم‌سهم)" else "") }
                        Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(4.px, 8.px); cursor("pointer"); fontSize(0.8.cssRem) }; onClick { onRemoveTarget(t.targetId) } }) { Text("حذف") }
                    }
                }
            }
        }

        var tempTargetId by remember(sourceId) { mutableStateOf("") }
        var tempIsFemale by remember(sourceId) { mutableStateOf(false) }

        Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px); padding(12.px); backgroundColor(Color("#FFF8E1")); borderRadius(6.px); border(1.px, LineStyle.Dashed, Color("#FFB300")) } }) {
            Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")) }; onChange { e -> tempTargetId = e.target.value } }) {
                Option(value = "", attrs = { if(tempTargetId.isEmpty()) attr("selected","true") }) { Text("انتخاب گیرنده جدید...") }
                allAvailableNodes.filter { it.first != sourceId && action.targets.none { t -> t.targetId == it.first } }.forEach { (id, name) ->
                    key(id) { Option(value = id, attrs = { if(tempTargetId == id) attr("selected","true") }) { Text(name.ifEmpty { "ناشناس" }) } }
                }
            }
            if (action.distributionRule == TransferDistributionRule.BOY_GIRL) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); fontSize(0.9.cssRem); cursor("pointer") } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(tempIsFemale); onChange { e -> tempIsFemale = e.value }; style { marginRight(8.px); width(16.px); height(16.px) } })
                    Text("این گیرنده سهم دخترانه (نصف) بگیرد؟")
                }
            }
            Button(attrs = { style { width(100.percent); padding(10.px); backgroundColor(Color("#4CAF50")); color(Color("white")); border(0.px); borderRadius(6.px); cursor("pointer"); fontWeight("bold") }; onClick { if (tempTargetId.isNotBlank()) { onAddTarget(tempTargetId, tempIsFemale); tempTargetId = ""; tempIsFemale = false } } }) { Text("+ افزودن به لیست گیرندگان") }
        }
    }
}

@Composable
fun DynamicPlayerScreen(
    viewModel: DynamicPlayerViewModel, 
    baseUnit: String,
    onBack: () -> Unit,
    onCalculationComplete: (CalculationHistoryRecord) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val profile = state.activeProfile

    if (profile == null) {
        Div(attrs = { style { padding(24.px); textAlign("center"); color(Color("#D32F2F")) } }) {
            Text("خطا: الگو پیدا نشد یا حذف شده است.")
            Button(attrs = { style { marginTop(16.px); padding(8.px); cursor("pointer") }; onClick { onBack() } }) { Text("بازگشت") }
        }
        return
    }

    val allAvailableNodes = remember(profile) { extractAllDynamicNodes(profile.rootBlocks) }

    Div(attrs = { style { padding(16.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(24.px); width(100.percent); property("box-sizing", "border-box") } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); color(Color("#1565C0")); fontWeight("bold"); fontSize(1.1.cssRem) }; onClick { onBack() } }) {
            Text("⬅ بازگشت به داشبورد")
        }

        Div(attrs = { style { backgroundColor(Color("white")); padding(24.px); borderRadius(16.px); property("box-shadow", "0 4px 12px rgba(0,0,0,0.08)"); border(1.px, LineStyle.Solid, Color("#C5E1A5")) } }) {
            H2(attrs = { style { marginTop(0.px); color(Color("#1B5E20")); property("border-bottom", "2px solid #4CAF50"); paddingBottom(8.px) } }) { Text(profile.name) }
            if (profile.description.isNotBlank()) {
                P(attrs = { style { color(Color("#616161")); fontSize(0.95.cssRem); lineHeight("1.6"); backgroundColor(Color("#F1F8E9")); padding(12.px); borderRadius(8.px) } }) { Text(profile.description) }
            }

            Div(attrs = { style { marginTop(24.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(16.px) } }) {
                profile.rootBlocks.forEach { block -> RenderPlayerBlock(block, viewModel, state, allAvailableNodes) }
            }

            Button(attrs = {
                style {
                    width(100.percent); padding(18.px); backgroundColor(Color("#FF9800")); color(Color("white"))
                    border(0.px); borderRadius(8.px); fontSize(1.2.cssRem); fontWeight("bold"); cursor("pointer")
                    marginTop(32.px); property("box-shadow", "0 4px 12px rgba(255, 152, 0, 0.3)")
                }
                onClick {
                    val yearOptions = kotlin.js.json("year" to "numeric").unsafeCast<Date.LocaleOptions>()
                    val rawPersianYear = Date().toLocaleDateString("fa-IR", yearOptions).trim()
                    val timestamp = Date().getTime().toLong()
                    
                    val record = viewModel.executeCalculation(rawPersianYear, timestamp, baseUnit)
                    if (record != null) {
                        onCalculationComplete(record)
                    } else {
                        kotlinx.browser.window.alert("خطا در ایجاد خروجی محاسبه.")
                    }
                }
            }) { Text("🧮 محاسبه نهایی نتایج") }
        }
    }
}

@Composable
fun RenderPlayerBlock(block: CustomBlock, viewModel: DynamicPlayerViewModel, state: DynamicPlayerState, allAvailableNodes: List<Pair<String, String>>) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(14.px); css.borderRadius(8.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.fontSize(1.05.cssRem); css.property("box-sizing", "border-box") }

    when (block) {
        is BaseInputBlock -> {
            Div(attrs = { style { backgroundColor(Color("#FAFAFA")); padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")) } }) {
                Div(attrs = { style { marginBottom(16.px) } }) {
                    Label(attrs = { style { display(DisplayStyle.Block); fontWeight("bold"); color(Color("#424242")); marginBottom(8.px) } }) { Text(block.nameLabel) }
                    Input(type = InputType.Text, attrs = { style { inputStyle(this) }; value(state.textInputs[block.block_id + "_name"] ?: ""); onInput { e -> viewModel.updateTextInput(block.block_id + "_name", e.value) } })
                }
                Div {
                    Label(attrs = { style { display(DisplayStyle.Block); fontWeight("bold"); color(Color("#424242")); marginBottom(8.px) } }) { Text(block.amountLabel) }
                    Input(type = InputType.Text, attrs = { style { inputStyle(this) }; attr("inputmode", "decimal"); value(state.textInputs[block.block_id + "_amount"] ?: ""); onInput { e -> viewModel.updateTextInput(block.block_id + "_amount", e.value) } })
                }
            }
        }
        is StageBlock -> {
            var isAccordionOpen by remember { mutableStateOf(false) }
            Div(attrs = { style { backgroundColor(Color("white")); padding(20.px); borderRadius(12.px); property("box-shadow", "0 2px 6px rgba(0,0,0,0.05)"); border(1.px, LineStyle.Solid, Color("#81C784")); marginTop(16.px) } }) {
                H3(attrs = { style { color(Color("#2E7D32")); property("margin", "0px 0px 8px 0px") } }) { Text(block.name) }
                if (block.description.isNotBlank()) { P(attrs = { style { color(Color("#757575")); fontSize(0.9.cssRem); property("margin", "0px 0px 16px 0px") } }) { Text(block.description) } }
                
                if (block.accordionGuide.isNotBlank()) {
                    Div(attrs = { style { marginBottom(16.px) } }) {
                        Div(attrs = { style { cursor("pointer"); color(Color("#1976D2")); fontWeight("bold"); fontSize(0.9.cssRem) }; onClick { isAccordionOpen = !isAccordionOpen } }) { Text("💡 راهنمای مرحله" + if(isAccordionOpen) " (بستن)" else "") }
                        if (isAccordionOpen) { P(attrs = { style { backgroundColor(Color("#E3F2FD")); padding(12.px); borderRadius(8.px); fontSize(0.85.cssRem); color(Color("#0D47A1")) } }) { Text(block.accordionGuide) } }
                    }
                }
            }
        }
        is UIElementBlock -> {
            Div(attrs = { style { marginBottom(12.px) } }) {
                when (block.elementType) {
                    UIElementType.TEXT_FIELD, UIElementType.NUMBER_FIELD -> {
                        Label(attrs = { style { display(DisplayStyle.Block); fontWeight("bold"); color(Color("#424242")); marginBottom(8.px) } }) { 
                            Text(block.elementTitle + if (block.isRequired) " *" else "") 
                        }
                        Input(type = InputType.Text, attrs = { 
                            style { inputStyle(this) }
                            if (block.elementType == UIElementType.NUMBER_FIELD) attr("inputmode", "decimal")
                            value(state.textInputs[block.block_id] ?: "")
                            onInput { e -> viewModel.updateTextInput(block.block_id, e.value) }
                        })
                    }
                    UIElementType.HEADER_TITLE -> { H4(attrs = { style { color(Color("#37474F")); marginTop(16.px); property("border-bottom", "1px solid #CFD8DC"); paddingBottom(8.px) } }) { Text(block.elementTitle) } }
                    UIElementType.SEPARATOR_LINE -> { Hr(attrs = { style { property("border", "0"); height(1.px); backgroundColor(Color("#E0E0E0")); property("margin", "24px 0") } }) }
                    UIElementType.ACCORDION_GUIDE -> {
                        var isOpen by remember { mutableStateOf(false) }
                        Div {
                            Div(attrs = { style { cursor("pointer"); color(Color("#00796B")); fontWeight("bold"); backgroundColor(Color("#ECEFF1")); padding(12.px); borderRadius(8.px) }; onClick { isOpen = !isOpen } }) { Text("🔽 " + block.elementTitle) }
                            if (isOpen) { P(attrs = { style { backgroundColor(Color("#FAFAFA")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#CFD8DC")); fontSize(0.9.cssRem) } }) { Text(block.elementContent) } }
                        }
                    }
                    UIElementType.TEXT_WARNING -> {
                        Div(attrs = { style { backgroundColor(Color("#FFF3E0")); border(1.px, LineStyle.Solid, Color("#FFB74D")); property("border-left", "4px solid #F57C00"); padding(16.px); borderRadius(8.px) } }) {
                            Div(attrs = { style { fontWeight("bold"); color(Color("#E65100")); marginBottom(8.px) } }) { Text("⚠️ " + block.elementTitle) }
                            P(attrs = { style { property("margin", "0px"); fontSize(0.9.cssRem); color(Color("#424242")) } }) { Text(block.elementContent) }
                        }
                    }
                }
            }
        }
        is ConditionGate -> {
            val isChecked = state.booleanInputs[block.block_id] ?: false
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); backgroundColor(Color("#FFFDE7")); padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#FFF59D")); cursor("pointer"); fontWeight("bold"); color(Color("#F57F17")) } }) {
                Input(type = InputType.Checkbox, attrs = { style { width(24.px); height(24.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(block.block_id, e.value) } })
                Text(block.title)
            }
        }
        is MemberBlock, is PartnerBlock -> {
            val title = if (block is MemberBlock) block.title else (block as PartnerBlock).title
            val nodes = if (block is MemberBlock) block.headcountNodes else (block as PartnerBlock).headcountNodes
            val shareholders = if (block is MemberBlock) block.ghiyasShareholders + block.percentageShareholders else (block as PartnerBlock).ghiyasShareholders + (block as PartnerBlock).percentageShareholders
            
            fun hasAnyInteractive(n: BuilderPersonNode): Boolean = if (n.hasToggle || n.isAdvancedTransferAllowed) true else n.subNodes.any { hasAnyInteractive(it) }
            val shouldShowCard = nodes.any { hasAnyInteractive(it) } || shareholders.any { it.hasToggle || it.isAdvancedTransferAllowed }
            
            if (shouldShowCard) {
                Div(attrs = { style { backgroundColor(Color("#FAFAFA")); padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); marginBottom(12.px) } }) {
                    H4(attrs = { style { property("margin", "0 0 12px 0"); color(Color("#1B5E20")) } }) { Text("تنظیمات زمان اجرا: $title") }
                    
                    @Composable
                    fun renderPlayerNodeToggles(n: BuilderPersonNode) {
                        if (n.hasToggle || n.isAdvancedTransferAllowed) {
                            Div(attrs = { style { backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(8.px) } }) {
                                val isChecked = state.booleanInputs[n.id] ?: true
                                if (n.hasToggle) {
                                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); fontWeight("bold"); color(Color("#33691E")); marginBottom(if (n.isAdvancedTransferAllowed && isChecked) 12.px else 0.px) } }) {
                                        Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(n.id, e.value) } })
                                        Text("${n.toggleLabel.ifBlank { "لحاظ شود؟" }} (${n.name.ifBlank { "ناشناس" }})")
                                    }
                                }

                                if (n.isAdvancedTransferAllowed && isChecked) {
                                    val action = state.advancedTransfers[n.id] ?: RuntimeTransferAction()
                                    AdvancedTransferPanel(
                                        sourceId = n.id, sourceName = n.name.ifBlank { "ناشناس" }, action = action, allAvailableNodes = allAvailableNodes,
                                        onUpdateType = { t -> viewModel.updateTransferAmountType(n.id, t) },
                                        onUpdateValue = { v -> viewModel.updateTransferAmountValue(n.id, v) },
                                        onUpdateRule = { r -> viewModel.updateTransferRule(n.id, r) },
                                        onAddTarget = { tId, isF -> viewModel.addTransferTarget(n.id, tId, isF) },
                                        onRemoveTarget = { tId -> viewModel.removeTransferTarget(n.id, tId) }
                                    )
                                }
                            }
                        }
                        n.subNodes.forEach { renderPlayerNodeToggles(it) }
                    }
                    
                    nodes.forEach { renderPlayerNodeToggles(it) }
                    
                    shareholders.forEach { sh ->
                        if (sh.hasToggle || sh.isAdvancedTransferAllowed) {
                            Div(attrs = { style { backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(8.px) } }) {
                                val isChecked = state.booleanInputs[sh.id] ?: true
                                if (sh.hasToggle) {
                                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); fontWeight("bold"); color(Color("#33691E")); marginBottom(if (sh.isAdvancedTransferAllowed && isChecked) 12.px else 0.px) } }) {
                                        Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(sh.id, e.value) } })
                                        Text("${sh.toggleLabel.ifBlank { "لحاظ شود؟" }} (${sh.name.ifBlank { "ناشناس" }})")
                                    }
                                }
                                
                                if (sh.isAdvancedTransferAllowed && isChecked) {
                                    val action = state.advancedTransfers[sh.id] ?: RuntimeTransferAction()
                                    AdvancedTransferPanel(
                                        sourceId = sh.id, sourceName = sh.name.ifBlank { "ناشناس" }, action = action, allAvailableNodes = allAvailableNodes,
                                        onUpdateType = { t -> viewModel.updateTransferAmountType(sh.id, t) },
                                        onUpdateValue = { v -> viewModel.updateTransferAmountValue(sh.id, v) },
                                        onUpdateRule = { r -> viewModel.updateTransferRule(sh.id, r) },
                                        onAddTarget = { tId, isF -> viewModel.addTransferTarget(sh.id, tId, isF) },
                                        onRemoveTarget = { tId -> viewModel.removeTransferTarget(sh.id, tId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }

    val children = when (block) {
        is BaseInputBlock -> block.childBlocks
        is StageBlock -> block.childBlocks
        is ConditionGate -> block.childBlocks
        is MemberBlock -> block.childBlocks
        is PartnerBlock -> block.siblingBlocks
        else -> emptyList()
    }

    val shouldRenderChildren = if (block is ConditionGate) {
        val isChecked = state.booleanInputs[block.block_id] ?: false
        if (block.isVisibleEnabled) isChecked else true 
    } else true

    if (shouldRenderChildren && children.isNotEmpty()) {
        Div(attrs = { style { paddingRight(16.px); marginTop(8.px) } }) {
            children.forEach { childBlock -> RenderPlayerBlock(childBlock, viewModel, state, allAvailableNodes) }
        }
    }
}
