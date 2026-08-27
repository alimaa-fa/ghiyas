package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.data.LocalStorageRepository
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import ir.ghiyas.alimaa.core.utils.toGhiyasFormat

enum class SortOption(val label: String) {
    DATE_NEWEST("جدیدترین"),
    DATE_OLDEST("قدیمی‌ترین"),
    NAME("نام (الفبا)"),
    AMOUNT("بیشترین مقدار")
}

@Composable
fun HistoryItemCard(
    record: CalculationHistoryRecord,
    onDeleteRequest: (CalculationHistoryRecord) -> Unit,
    onNameUpdated: () -> Unit // کال‌بک برای به روز رسانی لیست پس از ویرایش نام
) {
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(record.calculationName) }
    
    val dateTimeOptions = kotlin.js.json("year" to "numeric", "month" to "long", "day" to "numeric", "hour" to "2-digit", "minute" to "2-digit").unsafeCast<kotlin.js.Date.LocaleOptions>()
    val liveTimeString = kotlin.js.Date(record.timestamp).toLocaleString("fa-IR", dateTimeOptions)

    Div(attrs = {
        style {
            padding(16.px)
            border(1.px, LineStyle.Solid, if (isEditing) Color("#81C784") else Color("#E0E0E0"))
            borderRadius(8.px)
            backgroundColor(if (isEditing) Color("#F1F8E9") else Color("#FAFAFA"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            gap(8.px)
            cursor(if (isEditing) "default" else "pointer")
            property("transition", "all 0.3s ease")
        }
        // اگر در حالت ویرایش نیستیم، کلیک روی کل کارت باعث باز و بسته شدن می‌شود
        onClick { if (!isEditing) expanded = !expanded }
    }) {
        // Visible Header
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                flexWrap(FlexWrap.Wrap)
                gap(12.px)
            }
        }) {
            // Title & Date Area
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(4.px)
                    flex(1)
                    minWidth(150.px)
                }
                // اصلاح مهم: stopPropagation از اینجا حذف شد تا کلیک روی عنوان به کارت اصلی برسد و آن را باز کند
            }) {
                if (isEditing) {
                    Div(attrs = { 
                        style { display(DisplayStyle.Flex); gap(8.px); alignItems(AlignItems.Center) }
                        // هنگام ویرایش فرم، از بسته شدن یا رفتار ناخواسته جلوگیری می‌کنیم
                        onClick { it.stopPropagation() } 
                    }) {
                        Input(type = InputType.Text, attrs = {
                            value(editNameInput)
                            onInput { e -> editNameInput = e.value }
                            style { 
                                flex(1)
                                padding(8.px)
                                borderRadius(4.px)
                                border(1.px, LineStyle.Solid, Color("#4CAF50"))
                                fontFamily("inherit")
                                fontSize(1.cssRem)
                            }
                        })
                        Button(attrs = {
                            style {
                                backgroundColor(Color("#4CAF50"))
                                color(Color("white"))
                                border(0.px)
                                borderRadius(4.px)
                                padding(8.px, 12.px)
                                cursor("pointer")
                                fontWeight("bold")
                            }
                            onClick {
                                if (editNameInput.isNotBlank()) {
                                    LocalStorageRepository.updateRecordName(record.id, editNameInput.trim())
                                    isEditing = false
                                    onNameUpdated()
                                }
                            }
                        }) { Text("ذخیره") }
                        Button(attrs = {
                            style {
                                backgroundColor(Color("transparent"))
                                color(Color("#757575"))
                                border(1.px, LineStyle.Solid, Color("#BDBDBD"))
                                borderRadius(4.px)
                                padding(8.px, 12.px)
                                cursor("pointer")
                            }
                            onClick { 
                                isEditing = false 
                                editNameInput = record.calculationName
                            }
                        }) { Text("لغو") }
                    }
                } else {
                    Span(attrs = {
                        style {
                            fontWeight("bold")
                            color(Color("#1B5E20"))
                            fontSize(1.1.cssRem)
                        }
                    }) {
                        val title = if (record.calculationName.contains(record.persianYear)) {
                            record.calculationName
                        } else {
                            "${record.calculationName} - ${record.persianYear}"
                        }
                        Text(title)
                    }

                    Span(attrs = {
                        style {
                            color(Color("#757575"))
                            fontSize(0.85.cssRem)
                        }
                    }) {
                        Text(liveTimeString)
                    }
                }
            }

            // Actions
            if (!isEditing) {
                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        gap(8.px)
                    }
                    // کلیک روی دکمه‌ها نباید باعث باز و بسته شدن کارت شود
                    onClick { it.stopPropagation() } 
                }) {
                    Button(attrs = {
                        style {
                            backgroundColor(Color("#E8F5E9"))
                            color(Color("#2E7D32"))
                            border(1.px, LineStyle.Solid, Color("#C8E6C9"))
                            borderRadius(8.px)
                            padding(6.px, 12.px)
                            cursor("pointer")
                            fontSize(0.9.cssRem)
                        }
                        onClick { isEditing = true }
                    }) { Text("ویرایش نام") }

                    Button(attrs = {
                        style {
                            backgroundColor(Color("#E3F2FD"))
                            color(Color("#1565C0"))
                            border(1.px, LineStyle.Solid, Color("#BBDEFB"))
                            borderRadius(8.px)
                            padding(6.px, 12.px)
                            cursor("pointer")
                            fontSize(0.9.cssRem)
                        }
                        onClick { ir.ghiyas.alimaa.export.WebExportEngine.shareText(record) }
                    }) { Text("اشتراک متنی") }

                    Button(attrs = {
                        style {
                            backgroundColor(Color("#FFEBEE"))
                            color(Color("#D32F2F"))
                            border(1.px, LineStyle.Solid, Color("#FFCDD2"))
                            borderRadius(8.px)
                            padding(6.px, 12.px)
                            cursor("pointer")
                            fontSize(0.9.cssRem)
                        }
                        onClick { onDeleteRequest(record) }
                    }) { Text("حذف") }
                }
            }
        }

        // Expandable Details (AnimatedVisibility equivalent for DOM)
        if (expanded && !isEditing) {
            Div(attrs = {
                style {
                    marginTop(12.px)
                    paddingTop(12.px)
                    property("border-top", "1px dashed #E0E0E0")
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(8.px)
                    cursor("default")
                }
                onClick { it.stopPropagation() }
            }) {
                P(attrs = { style { margin(0.px); fontWeight("bold"); color(Color("#424242")) } }) {
                    Text("مقدار اولیه: ${record.inputAmount.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
                }
                
                if (record.expensesResults.isNotEmpty()) {
                    Div {
                        Span(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); fontSize(0.9.cssRem) } }) { Text("هزینه‌ها:") }
                        Ul(attrs = { style { margin(4.px, 0.px); paddingLeft(0.px); paddingRight(20.px) } }) {
                            record.expensesResults.forEach { res ->
                                Li(attrs = { style { fontSize(0.9.cssRem); color(Color("#616161")) } }) {
                                    Text("${res.label}: ${res.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
                                }
                            }
                        }
                    }
                }
                
                if (record.agricultureResults.isNotEmpty()) {
                    Div {
                        Span(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); fontSize(0.9.cssRem) } }) { Text("کشاورزی:") }
                        Ul(attrs = { style { margin(4.px, 0.px); paddingLeft(0.px); paddingRight(20.px) } }) {
                            record.agricultureResults.forEach { res ->
                                Li(attrs = { style { fontSize(0.9.cssRem); color(Color("#616161")) } }) {
                                    Text("${res.label}: ${res.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
                                }
                            }
                        }
                    }
                }
                
                if (record.nimehkariResults.isNotEmpty()) {
                    Div {
                        Span(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); fontSize(0.9.cssRem) } }) { Text("نیمه‌کاری:") }
                        Ul(attrs = { style { margin(4.px, 0.px); paddingLeft(0.px); paddingRight(20.px) } }) {
                            record.nimehkariResults.forEach { res ->
                                Li(attrs = { style { fontSize(0.9.cssRem); color(Color("#616161")) } }) {
                                    Text("${res.label}: ${res.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
                                }
                            }
                        }
                    }
                }
                
                if (record.finalSharesResults.isNotEmpty()) {
                    Div {
                        Span(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); fontSize(0.9.cssRem) } }) { Text("سهم‌های نهایی:") }
                        Ul(attrs = { style { margin(4.px, 0.px); paddingLeft(0.px); paddingRight(20.px) } }) {
                            record.finalSharesResults.forEach { res ->
                                Li(attrs = { style { fontSize(0.9.cssRem); color(Color("#616161")) } }) {
                                    Text("${res.label}: ${res.value.value.toGhiyasFormat(record.baseUnit)} ${record.baseUnit}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.DATE_NEWEST) }
    var records by remember { mutableStateOf(LocalStorageRepository.getAllRecords()) }
    var recordToDelete by remember { mutableStateOf<CalculationHistoryRecord?>(null) }

    val filteredRecords = remember(searchQuery, sortOption, records) {
        val filtered = if (searchQuery.isBlank()) records
        else records.filter {
            it.calculationName.contains(searchQuery, ignoreCase = true) ||
            it.persianYear.contains(searchQuery, ignoreCase = true)
        }
        
        when (sortOption) {
            SortOption.DATE_NEWEST -> filtered.sortedByDescending { it.timestamp }
            SortOption.DATE_OLDEST -> filtered.sortedBy { it.timestamp }
            SortOption.NAME -> filtered.sortedBy { it.calculationName }
            SortOption.AMOUNT -> filtered.sortedByDescending { it.inputAmount.value }
        }
    }

    Div(attrs = {
        style {
            backgroundColor(Color("white"))
            borderRadius(16.px)
            padding(24.px)
            margin(16.px)
            property("box-shadow", "0 4px 8px rgba(0,0,0,0.1)")
            minHeight(60.vh)
            position(Position.Relative)
        }
    }) {
        // Header & Back button
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                marginBottom(24.px)
                property("border-bottom", "2px solid #2E7D32")
                paddingBottom(8.px)
            }
        }) {
            H3(attrs = {
                style {
                    color(Color("#2E7D32"))
                    fontWeight("bold")
                    fontSize(1.2.cssRem)
                    margin(0.px)
                }
            }) { Text("تاریخچه محاسبات") }

            Button(attrs = {
                style {
                    backgroundColor(Color("transparent"))
                    color(Color("#2E7D32"))
                    border(1.px, LineStyle.Solid, Color("#2E7D32"))
                    borderRadius(8.px)
                    padding(8.px, 16.px)
                    cursor("pointer")
                    fontWeight("bold")
                }
                onClick { onBack() }
            }) {
                Text("بازگشت")
            }
        }

        // Sort & Search Bar Row
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                gap(16.px)
                marginBottom(24.px)
                alignItems(AlignItems.Center)
                flexWrap(FlexWrap.Wrap)
            }
        }) {
            // Search Bar
            Div(attrs = {
                classes(AppStyleSheet.floatingContainer)
                style { 
                    flex(1)
                    minWidth(200.px)
                    marginBottom(0.px) 
                }
            }) {
                Input(type = InputType.Text, attrs = {
                    classes(AppStyleSheet.floatingInput)
                    classes("floating-input")
                    value(searchQuery)
                    onInput { event -> searchQuery = event.value }
                    placeholder(" ")
                })
                Label(attrs = {
                    classes(AppStyleSheet.floatingLabel)
                    classes("floating-label")
                }) { Text("جستجو (نام یا سال)...") }
            }

            // Sort Dropdown
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    alignItems(AlignItems.Center)
                    gap(8.px)
                }
            }) {
                Span(attrs = { style { color(Color("#424242")); fontWeight("bold") } }) { Text("مرتب‌سازی:") }
                Select(attrs = {
                    style {
                        padding(8.px)
                        borderRadius(8.px)
                        border(1.px, LineStyle.Solid, Color("#2E7D32"))
                        backgroundColor(Color("white"))
                        color(Color("#2E7D32"))
                        outline("none")
                        cursor("pointer")
                        fontFamily("inherit")
                    }
                    onChange { event ->
                        val selectedLabel = event.target.value
                        SortOption.entries.find { it.label == selectedLabel }?.let { sortOption = it }
                    }
                }) {
                    SortOption.entries.forEach { option ->
                        Option(value = option.label, attrs = {
                            if (option == sortOption) selected()
                        }) {
                            Text(option.label)
                        }
                    }
                }
            }
        }

        // List
        if (filteredRecords.isEmpty()) {
            Div(attrs = {
                style {
                    textAlign("center")
                    color(Color("#757575"))
                    marginTop(48.px)
                }
            }) {
                Text("رکوردی یافت نشد.")
            }
        } else {
            Div(attrs = {
                style {
                    display(DisplayStyle.Flex)
                    flexDirection(FlexDirection.Column)
                    gap(16.px)
                }
            }) {
                filteredRecords.forEach { record ->
                    HistoryItemCard(
                        record = record,
                        onDeleteRequest = { r -> recordToDelete = r },
                        onNameUpdated = { records = LocalStorageRepository.getAllRecords() } 
                    )
                }
            }
        }
    }

    // Modal Dialog for Safe Deletion
    if (recordToDelete != null) {
        Div(attrs = {
            style {
                position(Position.Fixed)
                top(0.px)
                left(0.px)
                width(100.vw)
                height(100.vh)
                backgroundColor(Color("rgba(0,0,0,0.5)"))
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
                property("z-index", "1000")
            }
        }) {
            Div(attrs = {
                style {
                    backgroundColor(Color("white"))
                    padding(24.px)
                    borderRadius(12.px)
                    maxWidth(400.px)
                    width(90.percent)
                    textAlign("center")
                    property("box-shadow", "0 10px 25px rgba(0,0,0,0.2)")
                }
            }) {
                H3(attrs = {
                    style { color(Color("#D32F2F")); marginTop(0.px) }
                }) { Text("حذف تاریخچه") }

                P(attrs = { style { fontSize(1.cssRem); color(Color("#424242")) } }) {
                    Text("آیا از حذف این محاسبه مطمئن هستید؟")
                }
                
                P(attrs = {
                    style {
                        fontWeight("bold")
                        color(Color("#D32F2F"))
                        fontSize(1.2.cssRem)
                        margin(16.px, 0.px)
                        padding(12.px)
                        backgroundColor(Color("#FFEBEE"))
                        borderRadius(8.px)
                    }
                }) {
                    Text(recordToDelete!!.calculationName)
                }

                Div(attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        justifyContent(JustifyContent.SpaceAround)
                        marginTop(24.px)
                    }
                }) {
                    Button(attrs = {
                        style {
                            backgroundColor(Color("#E0E0E0"))
                            color(Color("#424242"))
                            border(0.px)
                            borderRadius(8.px)
                            padding(10.px, 24.px)
                            cursor("pointer")
                            fontWeight("bold")
                        }
                        onClick { recordToDelete = null }
                    }) {
                        Text("انصراف")
                    }

                    Button(attrs = {
                        style {
                            backgroundColor(Color("#D32F2F"))
                            color(Color("white"))
                            border(0.px)
                            borderRadius(8.px)
                            padding(10.px, 24.px)
                            cursor("pointer")
                            fontWeight("bold")
                        }
                        onClick {
                            LocalStorageRepository.deleteRecord(recordToDelete!!.id)
                            records = LocalStorageRepository.getAllRecords()
                            recordToDelete = null
                        }
                    }) {
                        Text("حذف قطعی")
                    }
                }
            }
        }
    }
}
