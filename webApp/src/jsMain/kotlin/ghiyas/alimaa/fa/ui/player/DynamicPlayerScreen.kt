package ghiyas.alimaa.fa.ui.player

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ghiyas.alimaa.fa.domain.models.*
import ghiyas.alimaa.fa.presentation.player.DynamicPlayerViewModel
import ghiyas.alimaa.fa.presentation.player.DynamicPlayerState
import kotlin.js.Date

// --- توابع کمکی در سطح فایل برای پشتیبانی از بازگشت دوطرفه (Mutual Recursion) ---
private fun extractPersons(nodes: List<BuilderPersonNode>, result: MutableList<Pair<String, String>>) {
    nodes.forEach {
        result.add(it.id to it.name)
        extractPersons(it.subNodes, result)
        extractShareholders(it.subShareholders, result)
    }
}

private fun extractShareholders(nodes: List<BuilderShareholder>, result: MutableList<Pair<String, String>>) {
    nodes.forEach {
        result.add(it.id to it.name)
        extractShareholders(it.subNodes, result)
        extractPersons(it.subHeadcounts, result)
    }
}

fun extractAllDynamicNodes(blocks: List<CustomBlock>): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    fun traverse(bList: List<CustomBlock>) {
        bList.forEach { b ->
            when (b) {
                is MemberBlock -> {
                    extractPersons(b.headcountNodes, result)
                    extractShareholders(b.ghiyasShareholders, result)
                    extractShareholders(b.percentageShareholders, result)
                    traverse(b.childBlocks)
                }
                is PartnerBlock -> {
                    extractPersons(b.headcountNodes, result)
                    extractShareholders(b.ghiyasShareholders, result)
                    extractShareholders(b.percentageShareholders, result)
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

fun hasAnyInteractivePerson(n: BuilderPersonNode): Boolean = n.hasToggle || n.isAdvancedTransferAllowed || n.subNodes.any { hasAnyInteractivePerson(it) } || n.subShareholders.any { hasAnyInteractiveShareholder(it) }

fun hasAnyInteractiveShareholder(sh: BuilderShareholder): Boolean = sh.hasToggle || sh.isAdvancedTransferAllowed || sh.subNodes.any { hasAnyInteractiveShareholder(it) } || sh.subHeadcounts.any { hasAnyInteractivePerson(it) }
// -------------------------------------------------------------------------------

@Composable
fun RuntimeAdvancedTransferPanel(
    sourceId: String,
    sourceName: String,
    action: RuntimeTransferAction,
    allAvailableNodes: List<Pair<String, String>>,
    onActionUpdate: (RuntimeTransferAction) -> Unit
) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(8.px); css.borderRadius(6.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.property("box-sizing", "border-box") }
    
    Div(attrs = { style { padding(12.px); backgroundColor(Color("#FFFDE7")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#FBC02D")); marginTop(8.px) } }) {
        H5(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#F57F17")) } }) { Text("انتقال سهم در زمان اجرا: $sourceName") }

        Div(attrs = { style { marginBottom(12.px) } }) {
            Label(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold"); display(DisplayStyle.Block); marginBottom(4.px) } }) { Text("مقدار کسر از مبدأ:") }
            Select(attrs = { style { inputStyle(this) }; onChange { e -> onActionUpdate(action.copy(sourceAmountType = TransferAmountType.valueOf(e.target.value))) } }) {
                Option(value = "FULL", attrs = { if(action.sourceAmountType == TransferAmountType.FULL) attr("selected","true") }) { Text("کل سهم (کامل)") }
                Option(value = "PERCENTAGE", attrs = { if(action.sourceAmountType == TransferAmountType.PERCENTAGE) attr("selected","true") }) { Text("درصدی از سهم") }
                Option(value = "FIXED", attrs = { if(action.sourceAmountType == TransferAmountType.FIXED) attr("selected","true") }) { Text("مقدار ثابت") }
                Option(value = "FORMULA", attrs = { if(action.sourceAmountType == TransferAmountType.FORMULA) attr("selected","true") }) { Text("فرمول ریاضی") }
            }
        }

        if (action.sourceAmountType != TransferAmountType.FULL) {
            Div(attrs = { style { marginBottom(12.px) } }) {
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder(if(action.sourceAmountType == TransferAmountType.FORMULA) "فرمول ریاضی (مثلا [باقیمانده]*20/100)" else "مقدار را وارد کنید"); value(action.sourceAmountValue); onInput { e -> onActionUpdate(action.copy(sourceAmountValue = e.value)) } })
            }
        }

        Div(attrs = { style { marginBottom(12.px) } }) {
            Label(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold"); display(DisplayStyle.Block); marginBottom(4.px) } }) { Text("قانون توزیع بین گیرندگان:") }
            Select(attrs = { style { inputStyle(this) }; onChange { e -> onActionUpdate(action.copy(distributionRule = TransferDistributionRule.valueOf(e.target.value))) } }) {
                Option(value = "EQUAL", attrs = { if(action.distributionRule == TransferDistributionRule.EQUAL) attr("selected","true") }) { Text("مساوی بین همه") }
                Option(value = "BOY_GIRL", attrs = { if(action.distributionRule == TransferDistributionRule.BOY_GIRL) attr("selected","true") }) { Text("پسر و دختری (نسبت ۲ به ۱)") }
                Option(value = "BY_ORIGINAL_SHARE", attrs = { if(action.distributionRule == TransferDistributionRule.BY_ORIGINAL_SHARE) attr("selected","true") }) { Text("به نسبت سهم اولیه آن‌ها") }
                Option(value = "CUSTOM_PERCENTAGE", attrs = { if(action.distributionRule == TransferDistributionRule.CUSTOM_PERCENTAGE) attr("selected","true") }) { Text("درصد دلخواه برای هر کدام") }
            }
        }

        Label(attrs = { style { fontSize(0.85.cssRem); fontWeight("bold"); display(DisplayStyle.Block); marginBottom(8.px) } }) { Text("لیست گیرندگان (با تیک زدن انتخاب کنید):") }
        Div(attrs = { style { backgroundColor(Color("white")); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); maxHeight(200.px); property("overflow-y", "auto") } }) {
            allAvailableNodes.filter { it.first != sourceId }.forEach { (id, name) ->
                val isChecked = action.targets.any { it.targetId == id }
                val targetObj = action.targets.find { it.targetId == id }
                
                Div(attrs = { style { padding(6.px); property("border-bottom", "1px dashed #EEEEEE") } }) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer"); fontSize(0.9.cssRem); color(if(isChecked) Color("#1B5E20") else Color("#424242")); fontWeight(if(isChecked) "bold" else "normal") } }) {
                        Input(type = InputType.Checkbox, attrs = { 
                            style { property("margin-left", "8px"); width(16.px); height(16.px) }
                            checked(isChecked)
                            onChange { e -> 
                                if (e.value) onActionUpdate(action.copy(targets = action.targets + AdvancedTransferTarget(id)))
                                else onActionUpdate(action.copy(targets = action.targets.filter { it.targetId != id }))
                            }
                        })
                        Text(name.ifEmpty { "ناشناس" })
                    }
                    
                    if (isChecked && action.distributionRule == TransferDistributionRule.BOY_GIRL) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); marginTop(4.px); property("margin-right", "24px"); fontSize(0.8.cssRem); color(Color("#E65100")) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(targetObj?.isFemale ?: false); onChange { e -> onActionUpdate(action.copy(targets = action.targets.map { if(it.targetId == id) it.copy(isFemale = e.value) else it })) } })
                            Text("سهم دخترانه بگیرد؟")
                        }
                    }
                    if (isChecked && action.distributionRule == TransferDistributionRule.CUSTOM_PERCENTAGE) {
                        Input(type = InputType.Text, attrs = { style { inputStyle(this); marginTop(4.px); property("margin-right", "24px"); width(80.percent); padding(4.px) }; placeholder("درصد این شخص (مثلا 20)"); value(targetObj?.customPercentage ?: ""); onInput { e -> onActionUpdate(action.copy(targets = action.targets.map { if(it.targetId == id) it.copy(customPercentage = e.value) else it })) } })
                    }
                }
            }
        }
    }
}

@Composable
fun RenderPlayerPersonNode(node: BuilderPersonNode, viewModel: DynamicPlayerViewModel, state: DynamicPlayerState, allAvailableNodes: List<Pair<String, String>>, depth: Int) {
    if (!hasAnyInteractivePerson(node)) return
    
    val marginRightValue = (depth * 16).px
    if (node.hasToggle || node.isAdvancedTransferAllowed) {
        Div(attrs = { style { backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(8.px); property("margin-right", "${marginRightValue.value}px"); minWidth(280.px) } }) {
            val isChecked = state.booleanInputs[node.id] ?: true
            if (node.hasToggle) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); fontWeight("bold"); color(Color("#33691E")); marginBottom(if (node.isAdvancedTransferAllowed && isChecked) 12.px else 0.px) } }) {
                    Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(node.id, e.value) } })
                    Text("${node.toggleLabel.ifBlank { "لحاظ شود؟" }} (${node.name.ifBlank { "ناشناس" }})")
                }
            }

            if (node.isAdvancedTransferAllowed && isChecked) {
                val defaultAction = node.predefinedTransfer ?: RuntimeTransferAction()
                val action = state.advancedTransfers[node.id] ?: defaultAction
                RuntimeAdvancedTransferPanel(sourceId = node.id, sourceName = node.name.ifBlank { "ناشناس" }, action = action, allAvailableNodes = allAvailableNodes) { updatedAction ->
                    viewModel.updateAdvancedTransfer(node.id, updatedAction)
                }
            }
        }
    }
    
    node.subNodes.forEach { RenderPlayerPersonNode(it, viewModel, state, allAvailableNodes, depth + 1) }
    node.subShareholders.forEach { RenderPlayerShareholderNode(it, viewModel, state, allAvailableNodes, depth + 1) }
}

@Composable
fun RenderPlayerShareholderNode(node: BuilderShareholder, viewModel: DynamicPlayerViewModel, state: DynamicPlayerState, allAvailableNodes: List<Pair<String, String>>, depth: Int) {
    if (!hasAnyInteractiveShareholder(node)) return
    
    val marginRightValue = (depth * 16).px
    if (node.hasToggle || node.isAdvancedTransferAllowed) {
        Div(attrs = { style { backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#FFE082")); marginBottom(8.px); property("margin-right", "${marginRightValue.value}px"); minWidth(280.px) } }) {
            val isChecked = state.booleanInputs[node.id] ?: true
            if (node.hasToggle) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); fontWeight("bold"); color(Color("#F57F17")); marginBottom(if (node.isAdvancedTransferAllowed && isChecked) 12.px else 0.px) } }) {
                    Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(node.id, e.value) } })
                    Text("${node.toggleLabel.ifBlank { "لحاظ شود؟" }} (${node.name.ifBlank { "ناشناس" }})")
                }
            }

            if (node.isAdvancedTransferAllowed && isChecked) {
                val defaultAction = node.predefinedTransfer ?: RuntimeTransferAction()
                val action = state.advancedTransfers[node.id] ?: defaultAction
                RuntimeAdvancedTransferPanel(sourceId = node.id, sourceName = node.name.ifBlank { "ناشناس" }, action = action, allAvailableNodes = allAvailableNodes) { updatedAction ->
                    viewModel.updateAdvancedTransfer(node.id, updatedAction)
                }
            }
        }
    }
    
    node.subNodes.forEach { RenderPlayerShareholderNode(it, viewModel, state, allAvailableNodes, depth + 1) }
    node.subHeadcounts.forEach { RenderPlayerPersonNode(it, viewModel, state, allAvailableNodes, depth + 1) }
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
                        Div(attrs = { style { backgroundColor(Color("#FFF3E0")); border(1.px, LineStyle.Solid, Color("#FFB74D")); property("border-right", "4px solid #F57C00"); padding(16.px); borderRadius(8.px) } }) {
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
            val ghiyasShareholders = if (block is MemberBlock) block.ghiyasShareholders else (block as PartnerBlock).ghiyasShareholders
            val percentageShareholders = if (block is MemberBlock) block.percentageShareholders else (block as PartnerBlock).percentageShareholders
            
            val shouldShowCard = nodes.any { hasAnyInteractivePerson(it) } || ghiyasShareholders.any { hasAnyInteractiveShareholder(it) } || percentageShareholders.any { hasAnyInteractiveShareholder(it) }
            
            if (shouldShowCard) {
                // اعمال اسکرول افقی روی والد اصلی
                Div(attrs = { style { backgroundColor(Color("#FAFAFA")); padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); marginBottom(12.px); property("overflow-x", "auto") } }) {
                    H4(attrs = { style { property("margin", "0 0 12px 0"); color(Color("#1B5E20")) } }) { Text("تنظیمات زمان اجرا: $title") }
                    
                    nodes.forEach { RenderPlayerPersonNode(it, viewModel, state, allAvailableNodes, 0) }
                    ghiyasShareholders.forEach { RenderPlayerShareholderNode(it, viewModel, state, allAvailableNodes, 0) }
                    percentageShareholders.forEach { RenderPlayerShareholderNode(it, viewModel, state, allAvailableNodes, 0) }
                }
            }
        }
        else -> {}
    }

    val children = when (block) {
        is BaseInputBlock -> block.childBlocks; is StageBlock -> block.childBlocks; is ConditionGate -> block.childBlocks; is MemberBlock -> block.childBlocks; is PartnerBlock -> block.siblingBlocks; else -> emptyList()
    }

    val shouldRenderChildren = if (block is ConditionGate) {
        val isChecked = state.booleanInputs[block.block_id] ?: false
        if (block.isVisibleEnabled) isChecked else true 
    } else true

    if (shouldRenderChildren && children.isNotEmpty()) {
        Div(attrs = { style { paddingLeft(16.px); marginTop(8.px) } }) { // تغییر به padding-left
            children.forEach { childBlock -> RenderPlayerBlock(childBlock, viewModel, state, allAvailableNodes) }
        }
    }
}
