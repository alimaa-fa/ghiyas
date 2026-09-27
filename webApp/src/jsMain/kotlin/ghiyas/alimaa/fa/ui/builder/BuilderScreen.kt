package ghiyas.alimaa.fa.ui.builder

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ghiyas.alimaa.fa.domain.models.*
import ghiyas.alimaa.fa.presentation.builder.BuilderViewModel

private fun String.toEnglishDecimals(): String = this.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9').replace('٫', '.').replace(',', '.')

fun extractAllBuilderNodes(blocks: List<CustomBlock>): List<Pair<String, String>> {
    val result = mutableListOf<Pair<String, String>>()
    
    fun extractAny(nodes: List<Any>) {
        nodes.forEach { node ->
            if (node is BuilderPersonNode) {
                if (node.name.isNotBlank()) result.add(node.id to node.name)
                extractAny(node.subNodes)
                extractAny(node.subShareholders)
            } else if (node is BuilderShareholder) {
                if (node.name.isNotBlank()) result.add(node.id to node.name)
                extractAny(node.subNodes)
                extractAny(node.subHeadcounts)
            }
        }
    }

    fun traverse(bList: List<CustomBlock>) {
        bList.forEach { b ->
            when (b) {
                is MemberBlock -> { extractAny(b.headcountNodes); extractAny(b.ghiyasShareholders); extractAny(b.percentageShareholders); traverse(b.childBlocks) }
                is PartnerBlock -> { extractAny(b.headcountNodes); extractAny(b.ghiyasShareholders); extractAny(b.percentageShareholders); traverse(b.siblingBlocks) }
                is StageBlock -> traverse(b.childBlocks); is ConditionGate -> traverse(b.childBlocks); is BaseInputBlock -> traverse(b.childBlocks); else -> {}
            }
        }
    }
    traverse(blocks)
    return result
}

@Composable
fun BuilderAdvancedTransferPanel(sourceId: String, action: RuntimeTransferAction, allAvailableNodes: List<Pair<String, String>>, onActionUpdate: (RuntimeTransferAction) -> Unit) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(8.px); css.borderRadius(6.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.property("box-sizing", "border-box") }
    
    Div(attrs = { style { padding(12.px); backgroundColor(Color("#E8F5E9")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#81C784")); marginTop(8.px) } }) {
        H5(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#2E7D32")) } }) { Text("تنظیمات قطعیِ انتقال در زمان ساخت") }

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
        Div(attrs = { style { backgroundColor(Color("white")); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); maxHeight(200.px); overflowY("auto") } }) {
            allAvailableNodes.filter { it.first != sourceId }.forEach { (id, name) ->
                val isChecked = action.targets.any { it.targetId == id }
                val targetObj = action.targets.find { it.targetId == id }
                
                Div(attrs = { style { padding(6.px); property("border-bottom", "1px dashed #EEEEEE") } }) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); cursor("pointer"); fontSize(0.9.cssRem); color(if(isChecked) Color("#1B5E20") else Color("#424242")); fontWeight(if(isChecked) "bold" else "normal") } }) {
                        Input(type = InputType.Checkbox, attrs = { 
                            style { marginRight(8.px); width(16.px); height(16.px) }
                            checked(isChecked)
                            onChange { e -> 
                                if (e.value) onActionUpdate(action.copy(targets = action.targets + AdvancedTransferTarget(id)))
                                else onActionUpdate(action.copy(targets = action.targets.filter { it.targetId != id }))
                            }
                        })
                        Text(name)
                    }
                    
                    if (isChecked && action.distributionRule == TransferDistributionRule.BOY_GIRL) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); marginTop(4.px); marginRight(24.px); fontSize(0.8.cssRem); color(Color("#E65100")) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(targetObj?.isFemale ?: false); onChange { e -> onActionUpdate(action.copy(targets = action.targets.map { if(it.targetId == id) it.copy(isFemale = e.value) else it })) } })
                            Text("سهم دخترانه بگیرد؟")
                        }
                    }
                    if (isChecked && action.distributionRule == TransferDistributionRule.CUSTOM_PERCENTAGE) {
                        Input(type = InputType.Text, attrs = { style { inputStyle(this); marginTop(4.px); marginRight(24.px); width(80.percent); padding(4.px) }; placeholder("درصد این شخص (مثلا 20)"); value(targetObj?.customPercentage ?: ""); onInput { e -> onActionUpdate(action.copy(targets = action.targets.map { if(it.targetId == id) it.copy(customPercentage = e.value) else it })) } })
                    }
                }
            }
        }
    }
}

@Composable
fun RecursiveBuilderPersonNode(node: BuilderPersonNode, blockId: String, viewModel: BuilderViewModel, allAvailableNodes: List<Pair<String, String>>) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(8.px); css.borderRadius(4.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.property("box-sizing", "border-box") }
    
    // اصلاح RTL و افزودن minWidth برای جلوگیری از مچاله شدن فرم
    Div(attrs = { style { padding(12.px); marginTop(8.px); property("border-right", "4px solid #81C784"); backgroundColor(Color("#F8FBF8")); borderRadius(4.px); minWidth(280.px) } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(8.px) } }) {
            Div(attrs = { style { flex(2) } }) { Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام شخص"); value(node.name); onInput { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(name = e.value) } } }) }
            Label(attrs = { style { flex(1); display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.isFemale); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isFemale = e.value) } }; style { marginLeft(4.px) } })
                Text("دختر (۰.۵)")
            }
            Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeNode(blockId, node.id) } }) { Text("-") }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.85.cssRem); marginBottom(8.px); color(Color("#E65100")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isDisplayOnly); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isDisplayOnly = e.value) } }; style { marginLeft(8.px) } })
            Text("فقط برای نمایش (در جمع کل سهام محاسبه نشود)")
        }

        Div(attrs = { style { backgroundColor(Color("#FFFDE7")); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Dashed, Color("#FBC02D")); marginBottom(8.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.hasToggle); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(hasToggle = e.value) } }; style { marginLeft(8.px) } })
                Text("آیا این شخص دارای چک‌باکس شرطی باشد؟")
            }
            if (node.hasToggle) { Input(type = InputType.Text, attrs = { style { inputStyle(this); marginTop(8.px) }; placeholder("برچسب شرط"); value(node.toggleLabel); onInput { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(toggleLabel = e.value) } } }) }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); color(Color("#1976D2")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isAdvancedTransferAllowed); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isAdvancedTransferAllowed = e.value) } }; style { marginLeft(8.px) } })
            Text("امکان انتقال سهم در زمان اجرا؟")
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); color(Color("#2E7D32")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { 
                checked(node.predefinedTransfer != null); 
                onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(predefinedTransfer = if (e.value) RuntimeTransferAction() else null) } }; style { marginLeft(8.px) } 
            })
            Text("انتقال قطعی در همینجا تعیین شود؟")
        }

        node.predefinedTransfer?.let { action ->
            BuilderAdvancedTransferPanel(sourceId = node.id, action = action, allAvailableNodes = allAvailableNodes) { updatedAction ->
                viewModel.updatePersonNode(blockId, node.id) { it.copy(predefinedTransfer = updatedAction) }
            }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); marginTop(8.px) } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isSubDivided); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isSubDivided = e.value) } }; style { marginLeft(8.px) } })
            Text("آیا سهم این شخص در خودش خرد می‌شود؟")
        }

        if (node.isSubDivided) {
            // اعمال اسکرول افقی (overflow-x: auto) برای تو در تو شدن‌های عمیق
            Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#B2DFDB")); borderRadius(8.px); backgroundColor(Color("white")); property("overflow-x", "auto") } }) {
                
                Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#81C784")); marginBottom(8.px) }; onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(subDistributionType = DistributionType.valueOf(e.target.value)) } } }) {
                    Option(value = "HEADCOUNT_BASED", attrs = { if (node.subDistributionType == DistributionType.HEADCOUNT_BASED || node.subDistributionType == null) attr("selected", "true") }) { Text("بر اساس نفر") }
                    Option(value = "GHIYAS_BASED", attrs = { if (node.subDistributionType == DistributionType.GHIYAS_BASED) attr("selected", "true") }) { Text("بر اساس قیاس") }
                    Option(value = "PERCENTAGE", attrs = { if (node.subDistributionType == DistributionType.PERCENTAGE) attr("selected", "true") }) { Text("درصدی") }
                }

                val distType = node.subDistributionType ?: DistributionType.HEADCOUNT_BASED

                if (distType == DistributionType.HEADCOUNT_BASED) {
                    Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("تعداد نفرات زیرمجموعه"); value(node.subCountInput); onInput { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(subCountInput = e.value) } } })
                    val maxLimit = node.subCountInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0
                    val currentSum = node.subNodes.sumOf { if(it.isFemale) 0.5 else 1.0 }
                    
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                        Input(type = InputType.Checkbox, attrs = { checked(node.isDetailedFurther); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isDetailedFurther = e.value) } }; style { marginLeft(8.px) } })
                        Text("تقسیم جزئی‌تر؟ (درختی)")
                    }

                    if (!node.isDetailedFurther) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(node.isSubBoyGirlSplit); onChange { e -> viewModel.updatePersonNode(blockId, node.id) { it.copy(isSubBoyGirlSplit = e.value) } }; style { marginLeft(8.px) } })
                            Text("تسهیم پسر و دختری؟")
                        }
                    } else {
                        if (currentSum >= maxLimit && maxLimit > 0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: ظرفیت نفرات پر شده است!") } }
                        
                        node.subNodes.forEach { child -> RecursiveBuilderPersonNode(child, blockId, viewModel, allAvailableNodes) }
                        
                        if (maxLimit == 0.0 || currentSum < maxLimit) {
                            Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addPersonNode(blockId, node.id) } }) { Text("+ افزودن عضو جدید") }
                        }
                    }
                } else if (distType == DistributionType.PERCENTAGE) {
                    val currentSum = node.subShareholders.sumOf { it.shareInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0 }
                    if (currentSum >= 100.0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: مجموع درصدها پر شده است!") } }
                    
                    node.subShareholders.forEach { child -> RecursiveBuilderShareholderNode(child, blockId, true, viewModel, allAvailableNodes) }
                    if (currentSum < 100.0) {
                        Button(attrs = { style { width(100.percent); backgroundColor(Color("#FFF8E1")); color(Color("#F57F17")); property("border", "1px dashed #FFCA28"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addShareholderNode(blockId, node.id, true) } }) { Text("+ افزودن شریک درصدی") }
                    }
                } else if (distType == DistributionType.GHIYAS_BASED) {
                    node.subShareholders.forEach { child -> RecursiveBuilderShareholderNode(child, blockId, false, viewModel, allAvailableNodes) }
                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#FFF8E1")); color(Color("#F57F17")); property("border", "1px dashed #FFCA28"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addShareholderNode(blockId, node.id, false) } }) { Text("+ افزودن شریک قیاس") }
                }
            }
        }
    }
}

@Composable
fun RecursiveBuilderShareholderNode(node: BuilderShareholder, blockId: String, isPercentage: Boolean, viewModel: BuilderViewModel, allAvailableNodes: List<Pair<String, String>>) {
    val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(8.px); css.borderRadius(4.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.property("box-sizing", "border-box") }
    
    // اصلاح RTL و افزودن minWidth
    Div(attrs = { style { padding(12.px); marginTop(8.px); property("border-right", "4px solid #FFCA28"); backgroundColor(Color("#FFFDE7")); borderRadius(4.px); minWidth(280.px) } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); marginBottom(8.px) } }) {
            Div(attrs = { style { flex(2) } }) { Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام شریک"); value(node.name); onInput { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(name = e.value) } } }) }
            Div(attrs = { style { flex(1) } }) { Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder(if(isPercentage) "درصد" else "قیاس"); value(node.shareInput); onInput { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(shareInput = e.value) } } }) }
            Button(attrs = { style { backgroundColor(Color("#EF5350")); color(Color("white")); border(0.px); borderRadius(4.px); padding(8.px, 12.px); fontWeight("bold"); property("cursor", "pointer") }; onClick { viewModel.removeNode(blockId, node.id) } }) { Text("-") }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.85.cssRem); marginBottom(8.px); color(Color("#E65100")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isDisplayOnly); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(isDisplayOnly = e.value) } }; style { marginLeft(8.px) } })
            Text("فقط برای نمایش (در جمع کل سهام محاسبه نشود)")
        }

        Div(attrs = { style { backgroundColor(Color("white")); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Dashed, Color("#BDBDBD")); marginBottom(8.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem) } }) {
                Input(type = InputType.Checkbox, attrs = { checked(node.hasToggle); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(hasToggle = e.value) } }; style { marginLeft(8.px) } })
                Text("آیا این شخص دارای چک‌باکس شرطی باشد؟")
            }
            if (node.hasToggle) { Input(type = InputType.Text, attrs = { style { inputStyle(this); marginTop(8.px) }; placeholder("برچسب شرط"); value(node.toggleLabel); onInput { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(toggleLabel = e.value) } } }) }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); color(Color("#1976D2")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isAdvancedTransferAllowed); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(isAdvancedTransferAllowed = e.value) } }; style { marginLeft(8.px) } })
            Text("امکان انتقال سهم در زمان اجرا؟")
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); color(Color("#2E7D32")); fontWeight("bold") } }) {
            Input(type = InputType.Checkbox, attrs = { 
                checked(node.predefinedTransfer != null); 
                onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(predefinedTransfer = if (e.value) RuntimeTransferAction() else null) } }; style { marginLeft(8.px) } 
            })
            Text("انتقال قطعی در همینجا تعیین شود؟")
        }

        node.predefinedTransfer?.let { action ->
            BuilderAdvancedTransferPanel(sourceId = node.id, action = action, allAvailableNodes = allAvailableNodes) { updatedAction ->
                viewModel.updateShareholderNode(blockId, node.id) { it.copy(predefinedTransfer = updatedAction) }
            }
        }

        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); fontSize(0.9.cssRem); marginBottom(8.px); marginTop(8.px) } }) {
            Input(type = InputType.Checkbox, attrs = { checked(node.isSubDivided); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(isSubDivided = e.value) } }; style { marginLeft(8.px) } })
            Text("آیا سهم این شخص خرد شود (ساختار درختی)؟")
        }

        if (node.isSubDivided) {
            // اعمال اسکرول افقی
            Div(attrs = { style { padding(8.px); border(1.px, LineStyle.Dashed, Color("#FFE082")); borderRadius(8.px); backgroundColor(Color("white")); property("overflow-x", "auto") } }) {
                Select(attrs = { style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#FBC02D")); marginBottom(8.px) }; onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(subDistributionType = DistributionType.valueOf(e.target.value)) } } }) {
                    Option(value = "HEADCOUNT_BASED", attrs = { if (node.subDistributionType == DistributionType.HEADCOUNT_BASED || node.subDistributionType == null) attr("selected", "true") }) { Text("بر اساس نفر") }
                    Option(value = "GHIYAS_BASED", attrs = { if (node.subDistributionType == DistributionType.GHIYAS_BASED) attr("selected", "true") }) { Text("بر اساس قیاس") }
                    Option(value = "PERCENTAGE", attrs = { if (node.subDistributionType == DistributionType.PERCENTAGE) attr("selected", "true") }) { Text("درصدی") }
                }

                val distType = node.subDistributionType ?: DistributionType.HEADCOUNT_BASED

                if (distType == DistributionType.HEADCOUNT_BASED) {
                    Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("تعداد نفرات زیرمجموعه"); value(node.subCountInput); onInput { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(subCountInput = e.value) } } })
                    val maxLimit = node.subCountInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0
                    val currentSum = node.subHeadcounts.sumOf { if(it.isFemale) 0.5 else 1.0 }
                    
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                        Input(type = InputType.Checkbox, attrs = { checked(node.isDetailedFurther); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(isDetailedFurther = e.value) } }; style { marginLeft(8.px) } })
                        Text("تقسیم جزئی‌تر؟ (درختی)")
                    }

                    if (!node.isDetailedFurther) {
                        Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); property("cursor", "pointer"); marginTop(8.px); fontSize(0.9.cssRem) } }) {
                            Input(type = InputType.Checkbox, attrs = { checked(node.isSubBoyGirlSplit); onChange { e -> viewModel.updateShareholderNode(blockId, node.id) { it.copy(isSubBoyGirlSplit = e.value) } }; style { marginLeft(8.px) } })
                            Text("تسهیم پسر و دختری؟")
                        }
                    } else {
                        if (currentSum >= maxLimit && maxLimit > 0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: ظرفیت نفرات پر شده است!") } }
                        
                        node.subHeadcounts.forEach { child -> RecursiveBuilderPersonNode(child, blockId, viewModel, allAvailableNodes) }
                        
                        if (maxLimit == 0.0 || currentSum < maxLimit) {
                            Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); property("border", "1px dashed #4CAF50"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addPersonNode(blockId, node.id) } }) { Text("+ افزودن عضو جدید") }
                        }
                    }
                } else if (distType == DistributionType.PERCENTAGE) {
                    val currentSum = node.subNodes.sumOf { it.shareInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0 }
                    if (currentSum >= 100.0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: مجموع درصدها پر شده است!") } }
                    
                    node.subNodes.forEach { child -> RecursiveBuilderShareholderNode(child, blockId, true, viewModel, allAvailableNodes) }
                    if (currentSum < 100.0) {
                        Button(attrs = { style { width(100.percent); backgroundColor(Color("#FFF8E1")); color(Color("#F57F17")); property("border", "1px dashed #FFCA28"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addShareholderNode(blockId, node.id, true) } }) { Text("+ افزودن شریک درصدی") }
                    }
                } else if (distType == DistributionType.GHIYAS_BASED) {
                    node.subNodes.forEach { child -> RecursiveBuilderShareholderNode(child, blockId, false, viewModel, allAvailableNodes) }
                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#FFF8E1")); color(Color("#F57F17")); property("border", "1px dashed #FFCA28"); borderRadius(4.px); padding(8.px); property("cursor", "pointer"); marginTop(8.px) }; onClick { viewModel.addShareholderNode(blockId, node.id, false) } }) { Text("+ افزودن شریک قیاس") }
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

    val allAvailableNodes = remember(state.rootBlocks) { extractAllBuilderNodes(state.rootBlocks) }

    Div(attrs = { style { padding(16.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(24.px); property("box-sizing", "border-box"); width(100.percent) } }) {
        Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px); cursor("pointer"); color(Color("#1565C0")); fontWeight("bold"); fontSize(1.1.cssRem) }; onClick { onBack() } }) { Text("⬅ بازگشت به داشبورد") }

        Div(attrs = { style { backgroundColor(Color("#FFF3E0")); border(1.px, LineStyle.Solid, Color("#FFB74D")); property("border-right", "4px solid #F57C00"); padding(12.px); borderRadius(8.px); display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px) } }) {
            Span(attrs = { style { fontSize(1.5.cssRem) } }) { Text("⚠️") }
            P(attrs = { style { margin(0.px); color(Color("#E65100")); fontSize(0.9.cssRem); fontWeight("bold"); lineHeight("1.8") } }) {
                Text("نسخه آزمایشی: بوم سازنده محاسبات اختصاصی در حال توسعه است. ممکن است برخی ویژگی‌ها نهایی نشده باشند.")
                Br()
                Text("فعلا در مرحله‌ی چهارم از تب وابسته و یا محاسبه بر اساس نفر/سهام/درصد استفاده کنید.")
                Br()
                Text("در صورت مشکل در ساخت الگو با ما در ایتا تماس بگیرید. @AlirezaMariki")
            }
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
                    if (showMainGuide) { P(attrs = { style { fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#F5F5F5")); padding(12.px); borderRadius(6.px); margin(8.px, 0.px) } }) { Text("هنگامی گزینه‌ی «مستقل» را انتخاب کنید که مراحل پیش‌فرض جوابگو نیست.") } }
                }
            }

            if (state.integrationType == ProfileIntegrationType.DEPENDENT_STEP_4) {
                Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); cursor("pointer"); backgroundColor(Color("#FFF3E0")); padding(12.px); borderRadius(8.px) } }) {
                    Input(type = InputType.Checkbox, attrs = { checked(state.nimehkariMacroEnabled); onChange { viewModel.toggleNimehkariMacro(it.value) } }); Text("محاسبه هر دو شریک نیمه‌کاری در همین محاسبه")
                }
                Div(attrs = { style { marginTop(8.px) } }) {
                    Div(attrs = { style { cursor("pointer"); color(Color("#E65100")); fontSize(0.9.cssRem); fontWeight("bold"); padding(4.px, 0.px) }; onClick { showNimehkariGuide = !showNimehkariGuide } }) { Text("💡 راهنمای جلوگیری از باگ شریک دوم" + if (showNimehkariGuide) " (بستن)" else "") }
                    if (showNimehkariGuide) { P(attrs = { style { fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#FFF8E1")); padding(12.px); borderRadius(6.px); margin(8.px, 0.px) } }) { Text("اگر قصد دارید سهم هر دو شریک در همینجا محاسبه شود این را تیک بزنید.") } }
                }
            }
        }

        Div(attrs = { style { backgroundColor(Color("white")); padding(16.px); borderRadius(12.px); property("box-shadow", "0 2px 8px rgba(0,0,0,0.05)"); border(1.px, LineStyle.Solid, Color("#E0E0E0")); minHeight(400.px); property("box-sizing", "border-box"); width(100.percent); property("overflow-x", "auto"); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column) } }) {
            if (state.rootBlocks.isEmpty()) { Div(attrs = { style { textAlign("center"); padding(40.px); color(Color("#9E9E9E")) } }) { Text("بوم خالی است. اولین شاخه را اضافه کنید.") } } 
            else { Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column) } }) { state.rootBlocks.forEach { block -> RenderBlockRecursively(block, viewModel, depth = 0, allAvailableNodes = allAvailableNodes) } } }
            
            Div(attrs = { style { marginTop(24.px); minWidth(300.px) } }) {
                Button(attrs = { style { width(100.percent); padding(16.px); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(2.px, LineStyle.Dashed, Color("#81C784")); borderRadius(8.px); fontSize(1.1.cssRem); fontWeight("bold"); cursor("pointer"); property("box-sizing", "border-box") }; onClick { showRootMenu = !showRootMenu } }) { Text(if (showRootMenu) "✖ بستن منو" else "➕ افزودن اولین شاخه / ریشه اصلی") }
                if (showRootMenu) { BlockSelectorMenu(isSiblingContext = true, onSelect = { newBlock -> viewModel.addRootBlock(newBlock); showRootMenu = false }, viewModel = viewModel) }
            }
        }

        Button(attrs = {
            style { width(100.percent); padding(18.px); backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.2.cssRem); fontWeight("bold"); cursor("pointer"); property("box-shadow", "0 4px 12px rgba(21, 101, 192, 0.3)") }
            onClick { viewModel.saveProfile(onSuccess = { kotlinx.browser.window.alert("✅ الگوی شما با موفقیت ذخیره شد."); onBack() }, onError = { errorMsg -> kotlinx.browser.window.alert("❌ خطا: $errorMsg") }) }
        }) { Text("💾 ذخیره و ثبت نهایی الگو") }
    }
}

@Composable
fun RenderBlockRecursively(block: CustomBlock, viewModel: BuilderViewModel, depth: Int, allAvailableNodes: List<Pair<String, String>>) {
    val borderColors = listOf("#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#F44336")
    val currentBorderColor = borderColors[depth % borderColors.size]
    val indentation = depth * 24 
    var showChildMenu by remember { mutableStateOf(false) }
    var showSiblingMenu by remember { mutableStateOf(false) }

    Div(attrs = { style { property("margin-right", "${indentation}px"); marginTop(12.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); property("border-right", "6px solid $currentBorderColor"); borderRadius(8.px); backgroundColor(Color("#FAFAFA")); position(Position.Relative); property("box-sizing", "border-box"); minWidth(320.px) } }) {
        Span(attrs = { style { position(Position.Absolute); top(12.px); left(12.px); cursor("pointer"); color(Color("#D32F2F")); fontWeight("bold"); fontSize(20.px) }; title("حذف این بلوک"); onClick { viewModel.deleteBlock(block.block_id) } }) { Text("✖") }

        val inputStyle = { css: StyleScope -> css.width(100.percent); css.padding(10.px); css.marginBottom(8.px); css.borderRadius(6.px); css.border(1.px, LineStyle.Solid, Color("#BDBDBD")); css.fontFamily("inherit"); css.property("box-sizing", "border-box") }

        when (block) {
            is BaseInputBlock -> {
                Div(attrs = { style { marginBottom(16.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)); fontSize(1.1.cssRem) } }) { Text("📥 ورودی پایه محصول") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("برچسب نام محاسبه"); value(block.nameLabel); onInput { e -> viewModel.updateBlock(block.block_id) { (it as BaseInputBlock).copy(nameLabel = e.value) } } })
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("برچسب مقدار کل"); value(block.amountLabel); onInput { e -> viewModel.updateBlock(block.block_id) { (it as BaseInputBlock).copy(amountLabel = e.value) } } })
            }
            is StageBlock -> {
                Div(attrs = { style { marginBottom(16.px); paddingBottom(8.px); property("border-bottom", "2px solid $currentBorderColor"); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)); fontSize(1.1.cssRem) } }) { Text("📑 مرحله (هدر)") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام مرحله"); value(block.name); onInput { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(name = e.value) } } })
                TextArea(attrs = { style { inputStyle(this) }; placeholder("توضیحات مرحله..."); value(block.description); onInput { e -> viewModel.updateBlock(block.block_id) { (it as StageBlock).copy(description = e.value) } } })
            }
            is ConditionGate -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("✅ شرط") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("عنوان شرط"); value(block.title); onInput { e -> viewModel.updateBlock(block.block_id) { (it as ConditionGate).copy(title = e.value) } } })
                Div(attrs = { style { backgroundColor(Color("#F3E5F5")); padding(12.px); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#CE93D8")) } }) {
                    Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontSize(0.9.cssRem); marginBottom(8.px) } }) { Input(type = InputType.Checkbox, attrs = { checked(block.isCalculateEnabled); onChange { e -> viewModel.updateBlock(block.block_id) { (it as ConditionGate).copy(isCalculateEnabled = e.value) } } }); Text("محاسبه شود/نشود (تاثیر در ریاضیات)") }
                }
            }
            is MemberBlock, is PartnerBlock -> {
                val distType = if(block is MemberBlock) block.distributionType else (block as PartnerBlock).distributionType
                val updateDistType: (DistributionType) -> Unit = { d -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(distributionType = d) else (it as PartnerBlock).copy(distributionType = d) } }
                val updateTitle: (String) -> Unit = { t -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(title = t) else (it as PartnerBlock).copy(title = t) } }
                
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text(if(block is MemberBlock) "👤 وارث" else "🤝 شریک") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("عنوان اصلی گروه/شریک"); value(if(block is MemberBlock) block.title else (block as PartnerBlock).title); onInput { updateTitle(it.value) } })
                
                Select(attrs = { style { inputStyle(this); backgroundColor(Color("#FFFDE7")) }; onChange { e -> updateDistType(DistributionType.valueOf(e.target.value)) } }) {
                    Option(value = "HEADCOUNT_BASED", attrs = { if (distType == DistributionType.HEADCOUNT_BASED) attr("selected", "true") }) { Text("بر اساس نفر") }
                    Option(value = "GHIYAS_BASED", attrs = { if (distType == DistributionType.GHIYAS_BASED) attr("selected", "true") }) { Text("بر اساس قیاس") }
                    Option(value = "PERCENTAGE", attrs = { if (distType == DistributionType.PERCENTAGE) attr("selected", "true") }) { Text("درصدی") }
                    Option(value = "CUSTOM_UNIT", attrs = { if (distType == DistributionType.CUSTOM_UNIT) attr("selected", "true") }) { Text("واحد سفارشی") }
                }

                Div(attrs = { style { padding(12.px); backgroundColor(Color("white")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#BDBDBD")); property("overflow-x", "auto") } }) {
                    when (distType) {
                        DistributionType.HEADCOUNT_BASED -> {
                            val countInput = if(block is MemberBlock) block.totalHeadcountInput else (block as PartnerBlock).totalHeadcountInput
                            val isDetailed = if(block is MemberBlock) block.isDetailedHeadcount else (block as PartnerBlock).isDetailedHeadcount
                            val headcountNodes = if(block is MemberBlock) block.headcountNodes else (block as PartnerBlock).headcountNodes
                            
                            Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("تعداد کل نفرات"); value(countInput); onInput { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(totalHeadcountInput = e.value) else (it as PartnerBlock).copy(totalHeadcountInput = e.value) } } })
                            val maxLimit = countInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0
                            val currentSum = headcountNodes.sumOf { if(it.isFemale) 0.5 else 1.0 }

                            Label(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px); fontWeight("bold"); marginTop(12.px) } }) {
                                Input(type = InputType.Checkbox, attrs = { checked(isDetailed); onChange { e -> viewModel.updateBlock(block.block_id) { if(it is MemberBlock) it.copy(isDetailedHeadcount = e.value) else (it as PartnerBlock).copy(isDetailedHeadcount = e.value) } } })
                                Text("تقسیم جزئی شود؟ (ساختار درختی)")
                            }
                            if (isDetailed) {
                                if (currentSum >= maxLimit && maxLimit > 0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: ظرفیت نفرات پر شده است!") } }
                                headcountNodes.forEach { node -> RecursiveBuilderPersonNode(node, block.block_id, viewModel, allAvailableNodes) }
                                
                                if (maxLimit == 0.0 || currentSum < maxLimit) {
                                    Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(12.px); fontWeight("bold"); cursor("pointer"); marginTop(16.px) }; onClick { viewModel.addPersonNode(block.block_id, null) } }) { Text("+ افزود شخص جدید") }
                                }
                            }
                        }
                        DistributionType.GHIYAS_BASED -> {
                            val shares = if(block is MemberBlock) block.ghiyasShareholders else (block as PartnerBlock).ghiyasShareholders
                            shares.forEach { sh -> RecursiveBuilderShareholderNode(sh, block.block_id, false, viewModel, allAvailableNodes) }
                            Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(10.px); cursor("pointer"); marginTop(12.px) }; onClick { viewModel.addShareholderNode(block.block_id, null, false) } }) { Text("+ افزودن شریک جدید") }
                        }
                        DistributionType.PERCENTAGE -> {
                            val shares = if(block is MemberBlock) block.percentageShareholders else (block as PartnerBlock).percentageShareholders
                            val currentSum = shares.sumOf { it.shareInput.toEnglishDecimals().toDoubleOrNull() ?: 0.0 }
                            if (currentSum >= 100.0) { P(attrs = { style { color(Color("white")); backgroundColor(Color("#D32F2F")); padding(6.px); borderRadius(4.px); fontSize(0.85.cssRem); fontWeight("bold"); margin(8.px, 0.px) } }) { Text("خطا: مجموع درصدها پر شده است!") } }
                            
                            shares.forEach { sh -> RecursiveBuilderShareholderNode(sh, block.block_id, true, viewModel, allAvailableNodes) }
                            if (currentSum < 100.0) {
                                Button(attrs = { style { width(100.percent); backgroundColor(Color("#E8F5E9")); color(Color("#2E7D32")); border(1.px, LineStyle.Dashed, Color("#4CAF50")); borderRadius(8.px); padding(10.px); cursor("pointer"); marginTop(12.px) }; onClick { viewModel.addShareholderNode(block.block_id, null, true) } }) { Text("+ افزودن شریک جدید") }
                            }
                        }
                        DistributionType.CUSTOM_UNIT -> { Text("تنظیمات واحد در دسترس است.") }
                    }
                }
            }
            is FormulaBlock -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("🧮 فرمول") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("نام فیلد خروجی"); value(block.outputName); onInput { e -> viewModel.updateBlock(block.block_id) { (it as FormulaBlock).copy(outputName = e.value) } } })
                Input(type = InputType.Text, attrs = { style { inputStyle(this); property("direction", "ltr") }; placeholder("فرمول: (کل * 30) / 100"); value(block.rawFormula); onInput { e -> viewModel.updateBlock(block.block_id) { (it as FormulaBlock).copy(rawFormula = e.value) } } })
            }
            is UIElementBlock -> {
                Div(attrs = { style { marginBottom(12.px); display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); paddingRight(24.px) } }) { Span(attrs = { style { fontWeight("bold"); color(Color(currentBorderColor)) } }) { Text("🎨 عنصر ظاهری") } }
                Input(type = InputType.Text, attrs = { style { inputStyle(this) }; placeholder("عنوان/برچسب فیلد"); value(block.elementTitle); onInput { e -> viewModel.updateBlock(block.block_id) { (it as UIElementBlock).copy(elementTitle = e.value) } } })
            }
        }

        val children = when (block) {
            is BaseInputBlock -> block.childBlocks; is StageBlock -> block.childBlocks; is ConditionGate -> block.childBlocks; is MemberBlock -> block.childBlocks; is PartnerBlock -> block.siblingBlocks; else -> emptyList()
        }
        children.forEach { childBlock -> RenderBlockRecursively(childBlock, viewModel, depth + 1, allAvailableNodes) }

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
