package ir.ghiyas.alimaa.ui.player

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.*
import ir.ghiyas.alimaa.presentation.player.DynamicPlayerViewModel

@Composable
fun DynamicPlayerScreen(viewModel: DynamicPlayerViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val profile = state.activeProfile

    if (profile == null) {
        Div(attrs = { style { padding(24.px); textAlign("center"); color(Color("#D32F2F")) } }) {
            Text("خطا: الگو پیدا نشد یا حذف شده است.")
            Button(attrs = { style { marginTop(16.px); padding(8.px); cursor("pointer") }; onClick { onBack() } }) { Text("بازگشت") }
        }
        return
    }

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
                profile.rootBlocks.forEach { block -> RenderPlayerBlock(block, viewModel, state) }
            }

            Button(attrs = {
                style {
                    width(100.percent); padding(18.px); backgroundColor(Color("#FF9800")); color(Color("white"))
                    border(0.px); borderRadius(8.px); fontSize(1.2.cssRem); fontWeight("bold"); cursor("pointer")
                    marginTop(32.px); property("box-shadow", "0 4px 12px rgba(255, 152, 0, 0.3)")
                }
                onClick { kotlinx.browser.window.alert("در فاز بعدی (موتور ریاضی)، مقادیر این فرم محاسبه خواهند شد.") }
            }) { Text("🧮 محاسبه نهایی نتایج") }
        }
    }
}

@Composable
fun RenderPlayerBlock(block: CustomBlock, viewModel: DynamicPlayerViewModel, state: ir.ghiyas.alimaa.presentation.player.DynamicPlayerState) {
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
            
            fun hasAnyToggle(n: BuilderPersonNode): Boolean {
                if (n.hasToggle) return true
                return n.subNodes.any { hasAnyToggle(it) }
            }
            val shouldShowCard = nodes.any { hasAnyToggle(it) } || shareholders.any { it.hasToggle }
            
            if (shouldShowCard) {
                Div(attrs = { style { backgroundColor(Color("#FAFAFA")); padding(16.px); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); marginBottom(12.px) } }) {
                    H4(attrs = { style { property("margin", "0 0 12px 0"); color(Color("#1B5E20")) } }) { Text("تنظیمات شرطی: $title") }
                    
                    // رفع خطا: اضافه شدن انوتیشن کامپوز به تابع محلی
                    @Composable
                    fun renderPlayerNodeToggles(n: BuilderPersonNode) {
                        if (n.hasToggle) {
                            val isChecked = state.booleanInputs[n.id] ?: true // پیش‌فرض: محاسبه شود
                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); cursor("pointer"); marginBottom(8.px); fontWeight("bold"); color(Color("#33691E")) } }) {
                                Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(n.id, e.value) } })
                                Text(n.toggleLabel.ifBlank { "لحاظ شدن سهم ${n.name}" })
                            }
                        }
                        n.subNodes.forEach { renderPlayerNodeToggles(it) }
                    }
                    
                    nodes.forEach { renderPlayerNodeToggles(it) }
                    
                    shareholders.forEach { sh ->
                        if (sh.hasToggle) {
                            val isChecked = state.booleanInputs[sh.id] ?: true
                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); backgroundColor(Color("white")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); cursor("pointer"); marginBottom(8.px); fontWeight("bold"); color(Color("#33691E")) } }) {
                                Input(type = InputType.Checkbox, attrs = { style { width(20.px); height(20.px) }; checked(isChecked); onChange { e -> viewModel.updateBooleanInput(sh.id, e.value) } })
                                Text(sh.toggleLabel.ifBlank { "لحاظ شدن سهم ${sh.name}" })
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }

    val children = when (block) {
        is BaseInputBlock -> block.childBlocks; is StageBlock -> block.childBlocks; is ConditionGate -> block.childBlocks; is MemberBlock -> block.childBlocks; else -> emptyList()
    }

    val shouldRenderChildren = if (block is ConditionGate) {
        val isChecked = state.booleanInputs[block.block_id] ?: false
        if (block.isVisibleEnabled) isChecked else true 
    } else true

    if (shouldRenderChildren && children.isNotEmpty()) {
        Div(attrs = { style { paddingRight(16.px); marginTop(8.px) } }) {
            children.forEach { childBlock -> RenderPlayerBlock(childBlock, viewModel, state) }
        }
    }
}
