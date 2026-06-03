package ir.ghiyas.alimaa.ui.builder

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.*
import ir.ghiyas.alimaa.presentation.builder.BuilderViewModel

@Composable
fun RecursiveBuilderPersonNode(node: BuilderPersonNode, path: List<String>, blockId: String, viewModel: BuilderViewModel) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(8.px); css.borderRadius(4.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit") }
    Div(attrs = { style { padding(12.px); marginTop(8.px); property("border-left", "4px solid #81C784"); backgroundColor(Color("#F8FBF8")); borderRadius(4.px) } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(8.px) } }) {
            Div(attrs = { style { flex(2) } }) {
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام شخص"); value(node.name); onInput { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(name = e.value) } } })
            }
            Label(attrs = { style { flex(1); display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.isFemale); onChange { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(isFemale = e.value) } }; style { marginRight(4.px) } })
                Text("دختر (۰.۵)")
            }
            Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeHeadcountNode(blockId, path.dropLast(1), node.id) } }) { Text("-") }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px) } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isSubDivided); onChange { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(isSubDivided = e.value) } }; style { marginRight(8.px) } })
            Text("آیا سهم این شخص در خودش خرد می‌شود؟")
        }

        if (node.isSubDivided) {
            Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")) } }) {
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("تعداد نفرات زیرمجموعه"); value(node.subCountInput); onInput { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(subCountInput = e.value) } } })
                
                val maxLimit = node.subCountInput.toDoubleOrNull() ?: 0.0
                val currentSum = node.subNodes.sumOf { if(it.isFemale) 0.5 else 1.0 }
                val hasDecimal = (maxLimit > 0 && maxLimit % 1.0 != 0.0)

                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(node.isDetailedFurther); onChange { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(isDetailedFurther = e.value) } }; style { marginRight(8.px) } })
                    Text("تقسیم جزئی‌تر؟ (درختی)")
                }

                if (!node.isDetailedFurther) {
                    if (!hasDecimal) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(node.isSubBoyGirlSplit); onChange { e -> viewModel.updateHeadcountNode(blockId, path) { it.copy(isSubBoyGirlSplit = e.value) } }; style { marginRight(8.px) } })
                            Text("تسهیم پسر و دختری؟")
                        }
                    }
                } else {
                    if (currentSum > maxLimit) { P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); fontWeight("bold"); margin(4.px, 0.px) } }) { Text("خطا: مجموع سهم زیرمجموعه ($currentSum) از حد مجاز ($maxLimit) بیشتر است!") } }
                    node.subNodes.forEach { child -> RecursiveBuilderPersonNode(child, path + child.id, blockId, viewModel) }
                    if (currentSum + 0.5 <= maxLimit) {
                        Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addHeadcountNode(blockId, path) } }) { Text("+ افزودن عضو به ${if (node.name.isEmpty()) "شخص" else node.name}") }
                    }
                }
            }
        }
    }
}

@Composable
fun BuilderScreen(viewModel: BuilderViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var showRootMenu by remember { mutableStateOf(false) }
    var showMainGuide by remember { mutableStateOf(false) }
    var showNimehkariGuide by remember { mutableStateOf(false) }

    Div(attrs = { style { padding(16.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(24.px); property("box-sizing", "border-box"); width(100.percent) } }) {
        
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); color(Color("#1565C0")); fontWeight("bold"); fontSize(1.1.cssRem) }; onClick { onBack() } }) {
            Text("⬅ بازگشت به داشبورد")
        }

        Div(attrs = { style { backgroundColor(Color("white")); padding(20.px); borderRadius(12.px); property("box-shadow", "0 2px 8px rgba(0,0,0,0.05)"); border(1.px, LineStyle.Solid, Color("#E0E0E0")); property("box-sizing", "border-box"); width(100.percent) } }) {
            H3(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#2E7D32")) } }) { Text("تنظیمات اولیه الگو") }
            
            val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(12.px); css.marginBottom(12.px); css.borderRadius(8.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.property("box-sizing", "border-box") }
            Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("اسم پروفایل (مثلاً: باغ پدری)"); value(state.profileName); onInput { viewModel.updateProfileName(it.value) } })
            TextArea(attrs = { style { inputStyle(this) }; placeholder("درباره پروفایل (یادآوری)"); value(state.profileDescription); onInput { viewModel.updateProfileDescription(it.value) } })

            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); marginBottom(8.px) } }) {
                Button(attrs = { style { flex(1); padding(12.px); borderRadius(8.px); fontWeight("bold"); cursor("pointer"); backgroundColor(if (state.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) Color("#4CAF50") else Color("#F5F5F5")); color(if (state.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) Color("white") else Color("#757575")); border(1.px, LineStyle.Solid, if (state.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) Color("#388E3C") else Color("#E0E0E0")) }; onClick { viewModel.updateIntegrationType(ProfileIntegrationType.DEPENDENT_STEP_4) } }) { Text("تب وابسته (مرحله ۴)") }
                Button(attrs = { style { flex(1); padding(12.px); borderRadius(8.px); fontWeight("bold"); cursor("pointer"); backgroundColor(if (state.integrationType == ProfileIntegrationType.STANDALONE_MAIN_TAB) Color("#4CAF50") else Color("#F5F5F5")); color(if (state.integrationType == ProfileIntegrationType.STANDALONE_MAIN_TAB) Color("white") else Color("#757575")); border(1.px, LineStyle.Solid, if (state.integrationType == ProfileIntegrationType.STANDALONE_MAIN_TAB) Color("#388E3C") else Color("#E0E0E0")) }; onClick { viewModel.updateIntegrationType(ProfileIntegrationType.STANDALONE_MAIN_TAB) } }) { Text("مستقل (صفر تا صد)") }
            }

            if (state.integrationType == ProfileIntegrationType.STANDALONE_MAIN_TAB) {
                Div(attrs = { style { marginBottom(16.px) } }) {
                    Div(attrs = { style { cursor("pointer"); color(Color("#1976D2")); fontSize(0.9.cssRem); fontWeight("bold"); padding(4.px, 0.px) }; onClick { showMainGuide = !showMainGuide } }) { Text("💡 راهنمای تب مستقل" + if (showMainGuide) " (بستن)" else "") }
                    if (showMainGuide) { P(attrs = { style { fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#F5F5F5")); padding(12.px); borderRadius(6.px); margin(8.px, 0.px) } }) { Text("هنگامی گزینه‌ی «مستقل» را انتخاب کنید که مراحلِ محاسبه خرجکرد، کشاورزی و... ما، جوابگوی نیاز شما نیست و شما نیاز به ساخت مجدد مراحل قبلی و بعدی متناسب با نیاز خود دارید!") } }
                }
            }

            if (state.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); cursor("pointer"); backgroundColor(Color("#FFF3E0")); padding(12.px); borderRadius(8.px) } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(state.nimehkariMacroEnabled); onChange { viewModel.toggleNimehkariMacro(it.value) } }); Text("محاسبه هر دو شریک نیمه‌کاری در همین محاسبه")
                }
                Div(attrs = { style { marginTop(8.px) } }) {
                    Div(attrs = { style { cursor("pointer"); color(Color("#E65100")); fontSize(0.9.cssRem); fontWeight("bold"); padding(4.px, 0.px) }; onClick { showNimehkariGuide = !showNimehkariGuide } }) { Text("💡 راهنمای جلوگیری از باگ شریک دوم" + if (showNimehkariGuide) " (بستن)" else "") }
                    if (showNimehkariGuide) { P(attrs = { style { fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#FFF8E1")); padding(12.px); borderRadius(6.px); margin(8.px, 0.px) } }) { Text("اگر محصول شما نیمه‌کاری هست و شما قصد دارید سهم هر دو شریک در همین محاسبه انجام شوند و‌ نه جدا، این گزینه را تیک بزنید تا از باگ و ظاهر شدن تنظیمات شریک دوم در صفحه اصلی، پس از انتخاب نیمه‌کاری در مرحله قبل جلوگیری کنید!") } }
                }
            }
        }

        Div(attrs = { style { backgroundColor(Color("white")); padding(16.px); borderRadius(12.px); property("box-shadow", "0 2px 8px rgba(0,0,0,0.05)"); border(1.px, LineStyle.Solid, Color("#E0E0E0")); minHeight(400.px); property("box-sizing", "border-box"); width(100.percent); property("overflow-x", "auto"); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column) } }) {
            if (state.rootBlocks.isEmpty()) { Div(attrs = { style { textAlign("center"); padding(40.px); color(Color("#9E9E9E")) } }) { Text("بوم خالی است. اولین شاخه را اضافه کنید.") } } 
            else { Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column) } }) { state.rootBlocks.forEach { block -> RenderBlockRecursively(block, viewModel, depth = 0) } } }
            
            Div(attrs = { style { marginTop(24.px); minWidth(300.px) } }) {
                Button(attrs = { style { width(100.percent); padding(16.px); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(2.px, LineStyle.Dashed, Color("#81C784")); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); cursor("pointer"); property("box-sizing", "border-box") }; onClick { showRootMenu = !showRootMenu } }) { Text(if (showRootMenu) "✖ بستن منو" else "➕ افزودن اولین شاخه / ریشه اصلی") }
                if (showRootMenu) { BlockSelectorMenu(isSiblingContext = true, onSelect = { newBlock -> viewModel.addRootBlock(newBlock); showRootMenu = false }, viewModel = viewModel) }
            }
        }

        Button(attrs = {
            style { width(100.percent); padding(18.px); backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.2.cssRem); fontWeight("bold"); cursor("pointer"); property("box-shadow", "0 4px 12px rgba(21, 101, 192, 0.3)") }
            onClick { viewModel.saveProfile(onSuccess = { kotlinx.browser.window.alert("✅ الگوی شما با موفقیت در دیتابیس قیاس ذخیره شد."); onBack() }, onError = { errorMsg -> kotlinx.browser.window.alert("❌ خطا: $errorMsg") }) }
        }) { Text("💾 ذخیره و ثبت نهایی الگو") }
    }
}

@Composable
fun RenderBlockRecursively(block: CustomBlock, viewModel: BuilderViewModel, depth: Int) {
    val borderColors = listOf("#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336")
    val currentBorderColor = borderColors[depth % borderColors.size]
    val indentation = depth * 24 
    
    var showChildMenu by remember { mutableStateOf(false) }
    var showSiblingMenu by remember { mutableStateOf(false) }
    var showRoundingGuide by remember { mutableStateOf(false) }

    Div(attrs = { style { property("margin-right", "${indentation}px"); marginTop(12.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); property("border-right", "6px solid $currentBorderColor"); borderRadius(8.px); backgroundColor(Color("#FAFAFA")); position(Position.Relative); property("box-sizing", "border-box"); minWidth(320.px) } }) {
        Span(attrs = { style { position(Position.Absolute); top(12.px); left(12.px); cursor("pointer"); color(Color("#D32F2F")); fontWeight("bold"); fontSize(20.px) }; title("حذف این بلوک و زیرمجموعه‌ها"); onClick { viewModel.deleteBlock(block.block_id) } }) { Text("✖") }

        val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(10.px); css.marginBottom(8.px); css.borderRadius(6.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.property("box-sizing", "border-box") }

        when (block) {
            is BaseInputBlock -> {
                Div(attrs = { style { marginBottom(16.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)); fontSize(1.1.cssRem) } }) { Text("📥 ورودی پایه محصول") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("برچسب نام محاسبه (مثلاً: نام باغ)"); value(block.nameLabel); onInput { e -> viewModel.updateBlock(block.block_id) { (it as BaseInputBlock).copy(nameLabel = e.value) } } })
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("برچسب مقدار کل (مثلاً: کل محصول امسال)"); value(block.amountLabel); onInput { e -> viewModel.updateBlock(block.block_id) { (it as BaseInputBlock).copy(amountLabel = e.value) } } })
            }
            is StageBlock -> {
                Div(attrs = { style { marginBottom(16.px); paddingBottom(8.px); property("border-bottom", "2px solid $currentBorderColor"); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)); fontSize(1.1.cssRem) } }) { Text("📑 مرحله (هدر)") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام مرحله (مثلاً: هزینه‌های چیدن)"); value(block.name); onInput { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(name = e.value) } } })
                TextArea(attrs = { style { inputStyle(this) }; placeholder("توضیحات مرحله..."); value(block.description); onInput { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(description = e.value) } } })
                TextArea(attrs = { style { inputStyle(this) }; placeholder("متن راهنمای آکاردئونی (اختیاری)"); value(block.accordionGuide); onInput { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(accordionGuide = e.value) } } })
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontSize(0.9.cssRem) } }) { Input(type = InputType.Checkbox, attrs = { checked(block.isRequired); onChange { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(isRequired = e.value) } } }); Text("عبور از این مرحله اجباری است") }
            }
            is ConditionGate -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("✅ شرط") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("عنوان شرط (مثلاً: آیا هزینه حمل دارد؟)"); value(block.title); onInput { e -> viewModel.updateBlock(block.block_id) { (it as ConditionGate).copy(title = e.value) } } })
                Div(attrs = { style { backgroundColor(Color("#F3E5F5")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#CE93D8")) } }) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontSize(0.9.cssRem); marginBottom(8.px) } }) { Input(type = InputType.Checkbox, attrs = { checked(block.isCalculateEnabled); onChange { e -> viewModel.updateBlock(block.block_id) { (it as ConditionGate).copy(isCalculateEnabled = e.value) } } }); Text("محاسبه شود/نشود (تاثیر در ریاضیات)") }
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontSize(0.9.cssRem) } }) { Input(type = InputType.Checkbox, attrs = { checked(block.isVisibleEnabled); onChange { e -> viewModel.updateBlock(block.block_id) { (it as ConditionGate).copy(isVisibleEnabled = e.value) } } }); Text("ظاهر شود/نشود (تاثیر در ظاهر فیلدها)") }
                }
            }
            is MemberBlock, is PartnerBlock -> {
                val distType = if(block is MemberBlock) block.distributionType else (block as PartnerBlock).distributionType
                val updateDistType: (DistributionType) -> Unit = { d -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(distributionType = d) else (it as PartnerBlock).copy(distributionType = d) } }
                val updateTitle: (String) -> Unit = { t -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(title = t) else (it as PartnerBlock).copy(title = t) } }
                
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text(if(block is MemberBlock) "👤 وارث" else "🤝 شریک") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("عنوان اصلی گروه/شریک"); value(if(block is MemberBlock) block.title else (block as PartnerBlock).title); onInput { updateTitle(it.value) } })
                
                Select(attrs = { style { inputStyle(this); backgroundColor(Color("#FFFDE7")) }; onChange { e -> updateDistType(DistributionType.valueOf(e.target.value)) } }) {
                    Option(value = "HEADCOUNT_BASED", attrs = { if (distType == DistributionType.HEADCOUNT_BASED) attr("selected", "true") }) { Text("بر اساس نفر") }
                    Option(value = "GHIYAS_BASED", attrs = { if (distType == DistributionType.GHIYAS_BASED) attr("selected", "true") }) { Text("بر اساس قیاس") }
                    Option(value = "PERCENTAGE", attrs = { if (distType == DistributionType.PERCENTAGE) attr("selected", "true") }) { Text("درصدی") }
                    Option(value = "CUSTOM_UNIT", attrs = { if (distType == DistributionType.CUSTOM_UNIT) attr("selected", "true") }) { Text("واحد سفارشی") }
                }

                Div(attrs = { style { padding(12.px); backgroundColor(Color("white")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#BDBDBD")) } }) {
                    when (distType) {
                        DistributionType.HEADCOUNT_BASED -> {
                            val countInput = if(block is MemberBlock) block.totalHeadcountInput else (block as PartnerBlock).totalHeadcountInput
                            val isDetailed = if(block is MemberBlock) block.isDetailedHeadcount else (block as PartnerBlock).isDetailedHeadcount
                            val isBoyGirl = if(block is MemberBlock) block.isBoyGirlSplit else (block as PartnerBlock).isBoyGirlSplit
                            val headcountNodes = if(block is MemberBlock) block.headcountNodes else (block as PartnerBlock).headcountNodes
                            
                            Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("تعداد کل نفرات (مثلا 4.5)"); value(countInput); onInput { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(totalHeadcountInput = e.value) else (it as PartnerBlock).copy(totalHeadcountInput = e.value) } } })
                            val maxLimit = countInput.toDoubleOrNull() ?: 0.0
                            val currentSum = headcountNodes.sumOf { if(it.isFemale) 0.5 else 1.0 }
                            val hasDecimal = (maxLimit > 0 && maxLimit % 1.0 != 0.0)

                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontWeight("bold"); marginTop(12.px) } }) {
                                Input(type = InputType.Checkbox, attrs = { checked(isDetailed); onChange { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(isDetailedHeadcount = e.value) else (it as PartnerBlock).copy(isDetailedHeadcount = e.value) } } })
                                Text("تقسیم جزئی شود؟ (ساختار درختی)")
                            }
                            if (!isDetailed) {
                                if (!hasDecimal) {
                                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontWeight("bold"); marginTop(12.px) } }) {
                                        Input(type = InputType.Checkbox, attrs = { checked(isBoyGirl); onChange { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(isBoyGirlSplit = e.value) else (it as PartnerBlock).copy(isBoyGirlSplit = e.value) } } })
                                        Text("تسهیم پسر و دختری؟")
                                    }
                                }
                            } else {
                                if (currentSum > maxLimit) { P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); fontWeight("bold") } }) { Text("خطا: مجموع وزن افراد ($currentSum) از حد مجاز ($maxLimit) فراتر رفته است!") } }
                                headcountNodes.forEach { node -> RecursiveBuilderPersonNode(node, listOf(node.id), block.block_id, viewModel) }
                                if (currentSum + 0.5 <= maxLimit) { Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(12.px); fontWeight("bold"); cursor("pointer"); marginTop(16.px) }; onClick { viewModel.addHeadcountNode(block.block_id, emptyList()) } }) { Text("+ افزودن شخص جدید") } }
                            }
                        }
                        DistributionType.GHIYAS_BASED -> {
                            val shares = if(block is MemberBlock) block.ghiyasShareholders else (block as PartnerBlock).ghiyasShareholders
                            shares.forEachIndexed { index, sh ->
                                Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); marginBottom(8.px) } }) {
                                    Input(type = InputType.Text, attrs = { style { inputStyle(this); flex(2); marginBottom(0.px) }; placeholder("نام شریک"); value(sh.name); onInput { e -> viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list[index] = sh.copy(name = e.value); if(it is MemberBlock) it.copy(ghiyasShareholders = list) else (it as PartnerBlock).copy(ghiyasShareholders = list) } } })
                                    Input(type = InputType.Text, attrs = { style { inputStyle(this); flex(1); marginBottom(0.px) }; placeholder("قیاس"); value(sh.shareInput); onInput { e -> viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list[index] = sh.copy(shareInput = e.value); if(it is MemberBlock) it.copy(ghiyasShareholders = list) else (it as PartnerBlock).copy(ghiyasShareholders = list) } } })
                                    Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); cursor("pointer") }; onClick { viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list.removeAt(index); if(it is MemberBlock) it.copy(ghiyasShareholders = list) else (it as PartnerBlock).copy(ghiyasShareholders = list) } } }) { Text("-") }
                                }
                            }
                            Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(10.px); cursor("pointer") }; onClick { viewModel.updateBlock(block.block_id) { val list = shares + BuilderShareholder(viewModel.createBaseInput().block_id); if(it is MemberBlock) it.copy(ghiyasShareholders = list) else (it as PartnerBlock).copy(ghiyasShareholders = list) } } }) { Text("+ افزودن شریک جدید") }
                        }
                        DistributionType.PERCENTAGE -> {
                            val shares = if(block is MemberBlock) block.percentageShareholders else (block as PartnerBlock).percentageShareholders
                            val currentSum = shares.sumOf { it.shareInput.toDoubleOrNull() ?: 0.0 }
                            if (currentSum > 100.0) { P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); fontWeight("bold") } }) { Text("خطا: مجموع درصدها ($currentSum) از ۱۰۰٪ بیشتر است!") } }
                            shares.forEachIndexed { index, sh ->
                                Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); marginBottom(8.px) } }) {
                                    Input(type = InputType.Text, attrs = { style { inputStyle(this); flex(2); marginBottom(0.px) }; placeholder("نام شریک"); value(sh.name); onInput { e -> viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list[index] = sh.copy(name = e.value); if(it is MemberBlock) it.copy(percentageShareholders = list) else (it as PartnerBlock).copy(percentageShareholders = list) } } })
                                    Input(type = InputType.Text, attrs = { style { inputStyle(this); flex(1); marginBottom(0.px) }; placeholder("درصد (٪)"); value(sh.shareInput); onInput { e -> viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list[index] = sh.copy(shareInput = e.value); if(it is MemberBlock) it.copy(percentageShareholders = list) else (it as PartnerBlock).copy(percentageShareholders = list) } } })
                                    Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); cursor("pointer") }; onClick { viewModel.updateBlock(block.block_id) { val list = shares.toMutableList(); list.removeAt(index); if(it is MemberBlock) it.copy(percentageShareholders = list) else (it as PartnerBlock).copy(percentageShareholders = list) } } }) { Text("-") }
                                }
                            }
                            Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(10.px); cursor("pointer") }; onClick { viewModel.updateBlock(block.block_id) { val list = shares + BuilderShareholder(viewModel.createBaseInput().block_id); if(it is MemberBlock) it.copy(percentageShareholders = list) else (it as PartnerBlock).copy(percentageShareholders = list) } } }) { Text("+ افزودن شریک جدید") }
                        }
                        DistributionType.CUSTOM_UNIT -> {
                            val decimals = if(block is MemberBlock) block.customUnitDecimals else (block as PartnerBlock).customUnitDecimals
                            val isRounding = if(block is MemberBlock) block.isRoundingEnabled else (block as PartnerBlock).isRoundingEnabled
                            Select(attrs = { style { inputStyle(this); backgroundColor(Color("#E3F2FD")) }; onChange { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(customUnitDecimals = e.target.value.toInt()) else (it as PartnerBlock).copy(customUnitDecimals = e.target.value.toInt()) } } }) {
                                Option(value = "0", attrs = { if (decimals == 0) attr("selected", "true") }) { Text("بدون اعشار (پول/تومان)") }
                                Option(value = "1", attrs = { if (decimals == 1) attr("selected", "true") }) { Text("۱ رقم اعشار (دانه/بسته)") }
                                Option(value = "2", attrs = { if (decimals == 2) attr("selected", "true") }) { Text("۲ رقم اعشار (متر/سانتی‌متر)") }
                                Option(value = "3", attrs = { if (decimals == 3) attr("selected", "true") }) { Text("۳ رقم اعشار (وزن/کیلوگرم)") }
                                Option(value = "-1", attrs = { if (decimals == -1) attr("selected", "true") }) { Text("اعشار کامل (آزاد)") }
                            }
                            if (decimals > 0) {
                                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontWeight("bold"); marginTop(12.px) } }) {
                                    Input(type = InputType.Checkbox, attrs = { checked(isRounding); onChange { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(isRoundingEnabled = e.value) else (it as PartnerBlock).copy(isRoundingEnabled = e.value) } } })
                                    Text("ارقام گرد شوند؟")
                                }
                                Div(attrs = { style { marginTop(8.px) } }) {
                                    Div(attrs = { style { cursor("pointer"); color(Color("#E65100")); fontSize(0.85.cssRem); fontWeight("bold"); padding(4.px, 0.px) }; onClick { showRoundingGuide = !showRoundingGuide } }) { Text("💡 راهنمای قوانین گرد کردن" + if (showRoundingGuide) " (بستن)" else "") }
                                    if (showRoundingGuide) { P(attrs = { style { fontSize(0.8.cssRem); color(Color("#616161")); backgroundColor(Color("#FFF8E1")); padding(12.px); borderRadius(6.px); margin(8.px, 0.px) } }) { Text("قانون گرد کردن نمایشی: در ۱ رقم اعشار: اگر رقم صدم ۸ و ۹ بود دهم یک بالا برود. در ۲ رقم: اگر رقم هزارم ۸ و ۹ بود صدم یکی بالا برود. در ۳ رقم: اگر رقم ده هزارم ۸ و ۹ بود هزارم یکی بالا برود.") } }
                                }
                            }
                        }
                    }
                }
            }
            is FormulaBlock -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("🧮 فرمول") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام فیلد خروجی (مثلاً: سهم نهایی)"); value(block.outputName); onInput { e -> viewModel.updateBlock(block.block_id) { (it as FormulaBlock).copy(outputName = e.value) } } })
                Div(attrs = { style { marginBottom(4.px); fontSize(0.8.cssRem); color(Color("#9E9E9E")) } }) { Text("کلمات کلیدی مجاز: [کل] ، [باقیمانده]") }
                Input(type = InputType.Text, attrs = { style { inputStyle(this); property("direction", "ltr") }; placeholder("فرمول: (کل * 30) / 100"); value(block.rawFormula); onInput { e -> viewModel.updateBlock(block.block_id) { (it as FormulaBlock).copy(rawFormula = e.value) } } })
                
                val allConditions = viewModel.getAllConditionGates()
                Select(attrs = { style { inputStyle(this); backgroundColor(Color("#F3E5F5")) }; onChange { e -> viewModel.updateBlock(block.block_id) { (it as FormulaBlock).copy(attachedConditionId = e.target.value) } } }) {
                    Option(value = "NONE", attrs = { if (block.attachedConditionId == "NONE") attr("selected", "true") }) { Text("بدون اتصال به شرط (اجرای دائم)") }
                    allConditions.forEach { cond -> Option(value = cond.block_id, attrs = { if (block.attachedConditionId == cond.block_id) attr("selected", "true") }) { Text("وابسته به: ${cond.title}") } }
                }
            }
            is UIElementBlock -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("🎨 عنصر ظاهری") }; Span(attrs = { style { fontSize(11.px); color(Color("#9E9E9E")); fontFamily("monospace") } }) { Text(block.system_alias) } }
                Select(attrs = { style { inputStyle(this) }; onChange { e -> viewModel.updateBlock(block.block_id) { (it as UIElementBlock).copy(elementType = UIElementType.valueOf(e.target.value)) } } }) {
                    Option(value = "TEXT_FIELD", attrs = { if (block.elementType == UIElementType.TEXT_FIELD) attr("selected", "true") }) { Text("فیلد متنی") }
                    Option(value = "NUMBER_FIELD", attrs = { if (block.elementType == UIElementType.NUMBER_FIELD) attr("selected", "true") }) { Text("فیلد عددی") }
                    Option(value = "HEADER_TITLE", attrs = { if (block.elementType == UIElementType.HEADER_TITLE) attr("selected", "true") }) { Text("تیتر / عنوان بزرگ") }
                    Option(value = "SEPARATOR_LINE", attrs = { if (block.elementType == UIElementType.SEPARATOR_LINE) attr("selected", "true") }) { Text("خط جداکننده (هدر)") }
                    Option(value = "ACCORDION_GUIDE", attrs = { if (block.elementType == UIElementType.ACCORDION_GUIDE) attr("selected", "true") }) { Text("راهنمای آکاردئونی") }
                    Option(value = "TEXT_WARNING", attrs = { if (block.elementType == UIElementType.TEXT_WARNING) attr("selected", "true") }) { Text("هشدار متنی با آیکون خطر ⚠️") }
                }
                
                if (block.elementType != UIElementType.SEPARATOR_LINE) {
                    Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder(if (block.elementType == UIElementType.HEADER_TITLE) "متن تیتر" else "عنوان/برچسب فیلد"); value(block.elementTitle); onInput { e -> viewModel.updateBlock(block.block_id) { (it as UIElementBlock).copy(elementTitle = e.value) } } })
                }
                if (block.elementType == UIElementType.ACCORDION_GUIDE || block.elementType == UIElementType.TEXT_WARNING) {
                    TextArea(attrs = { style { inputStyle(this) }; placeholder("متن اصلی راهنما یا هشدار..."); value(block.elementContent); onInput { e -> viewModel.updateBlock(block.block_id) { (it as UIElementBlock).copy(elementContent = e.value) } } })
                }
                if (block.elementType == UIElementType.TEXT_FIELD || block.elementType == UIElementType.NUMBER_FIELD) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontSize(0.9.cssRem) } }) { Input(type = InputType.Checkbox, attrs = { checked(block.isRequired); onChange { e -> viewModel.updateBlock(block.block_id) { (it as UIElementBlock).copy(isRequired = e.value) } } }); Text("پر کردن این فیلد اجباری است") }
                }
            }
        }

        val children = when (block) {
            is BaseInputBlock -> block.childBlocks; is StageBlock -> block.childBlocks; is ConditionGate -> block.childBlocks; is MemberBlock -> block.childBlocks; else -> emptyList()
        }
        children.forEach { childBlock -> RenderBlockRecursively(childBlock, viewModel, depth + 1) }

        if (block is PartnerBlock) { block.siblingBlocks.forEach { sibling -> RenderBlockRecursively(sibling, viewModel, depth) } }

        Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px); marginTop(16.px) } }) {
            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px) } }) {
                Button(attrs = { style { flex(1); padding(10.px); backgroundColor(Color("#EEEEEE")); border(1.px, LineStyle.Solid, Color("#BDBDBD")); borderRadius(6.px); cursor("pointer") }; onClick { showChildMenu = !showChildMenu; showSiblingMenu = false } }) { Text(if(showChildMenu) "✖ بستن" else "⬇️ افزودن زیرمجموعه") }
                Button(attrs = { style { flex(1); padding(10.px); backgroundColor(Color("#EEEEEE")); border(1.px, LineStyle.Solid, Color("#BDBDBD")); borderRadius(6.px); cursor("pointer") }; onClick { showSiblingMenu = !showSiblingMenu; showChildMenu = false } }) { Text(if(showSiblingMenu) "✖ بستن" else "➡️ افزودن هم‌رده") }
            }
            if (showChildMenu) { BlockSelectorMenu(isSiblingContext = false, onSelect = { newBlock -> viewModel.addChildToBlock(block.block_id, newBlock); showChildMenu = false }, viewModel = viewModel) }
            if (showSiblingMenu) { BlockSelectorMenu(isSiblingContext = true, onSelect = { newBlock -> viewModel.addSiblingToBlock(block.block_id, newBlock); showSiblingMenu = false }, viewModel = viewModel) }
        }
    }
}

@Composable
fun BlockSelectorMenu(isSiblingContext: Boolean, onSelect: (CustomBlock) -> Unit, viewModel: BuilderViewModel) {
    Div(attrs = { style { display(DisplayStyle.Flex); flexWrap(FlexWrap.Wrap); gap(8.px); padding(12.px); backgroundColor(Color("#FFFDE7")); border(1.px, LineStyle.Dashed, Color("#FBC02D")); borderRadius(8.px); marginTop(8.px); property("box-sizing", "border-box"); width(100.percent) } }) {
        val btnStyle = { css: StyleScope, disabled: Boolean -> css.padding(6.px, 10.px); css.borderRadius(6.px); css.border(1.px, LineStyle.Solid, if(disabled) Color("#E0E0E0") else Color("#FBC02D")); css.backgroundColor(if(disabled) Color("#F5F5F5") else Color("white")); css.color(if(disabled) Color("#BDBDBD") else Color("black")); css.cursor(if(disabled) "not-allowed" else "pointer"); css.fontSize(0.85.cssRem) }
        
        Button(attrs = { style { btnStyle(this, false) }; onClick { onSelect(viewModel.createBaseInput()) } }) { Text("📥 ورودی پایه") }
        Button(attrs = { style { btnStyle(this, false) }; onClick { onSelect(viewModel.createStage()) } }) { Text("📑 مرحله") }
        Button(attrs = { style { btnStyle(this, false) }; onClick { onSelect(viewModel.createCondition()) } }) { Text("✅ شرط") }
        
        Button(attrs = { style { btnStyle(this, isSiblingContext) }; if (isSiblingContext) attr("disabled", "true") else onClick { onSelect(viewModel.createMember()) } }) { Text("👤 وارث") }
        Button(attrs = { style { btnStyle(this, !isSiblingContext) }; if (!isSiblingContext) attr("disabled", "true") else onClick { onSelect(viewModel.createPartner()) } }) { Text("🤝 شریک") }
        
        Button(attrs = { style { btnStyle(this, false) }; onClick { onSelect(viewModel.createFormula()) } }) { Text("🧮 فرمول") }
        Button(attrs = { style { btnStyle(this, false) }; onClick { onSelect(viewModel.createUIElement()) } }) { Text("🎨 عنصر ظاهری") }
    }
}
