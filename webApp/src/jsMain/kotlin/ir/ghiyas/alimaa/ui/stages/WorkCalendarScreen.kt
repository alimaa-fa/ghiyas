package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import ir.ghiyas.alimaa.domain.models.CalendarType
import ir.ghiyas.alimaa.domain.calculator.WorkCalendarEngine
import kotlinx.browser.window
import org.w3c.dom.TouchEvent

private fun String.toGhiyasPersianDigits(): String {
    val english = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
    val persian = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    var result = this
    for (i in english.indices) {
        result = result.replace(english[i], persian[i])
    }
    return result
}
private fun Int.toGhiyasPersianDigits(): String = this.toString().toGhiyasPersianDigits()

@Composable
fun WorkCalendarScreen(
    activeProfile: WorkCalendarProfile?,
    onAddNew: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    if (activeProfile == null) return

    if (activeProfile.type != CalendarType.DAY_BASED) {
        Div(attrs = { style { padding(32.px); textAlign("center"); color(Color("#E65100")); fontWeight("bold") } }) { 
            Text("موتور این تقویم (خط زمانی) به زودی در دسترس قرار می‌گیرد.") 
        }
        return
    }

    val tehranNow = WorkCalendarEngine.getTehranDateInfo()
    var viewingJy by remember { mutableStateOf(tehranNow.jy) }
    var viewingJm by remember { mutableStateOf(tehranNow.jm) }
    var selectedJdn by remember { mutableStateOf(tehranNow.jdn) }
    
    var expandedBefore by remember { mutableStateOf(false) }
    var expandedAfter by remember { mutableStateOf(false) }
    
    var isManagementMenuOpen by remember { mutableStateOf(false) }

    var quoteIndex by remember(activeProfile.id) { mutableStateOf(if (activeProfile.quotes.isNotEmpty()) (activeProfile.quotes.indices).random() else -1) }
    val dailyQuote = if (quoteIndex >= 0 && quoteIndex < activeProfile.quotes.size) activeProfile.quotes[quoteIndex] else null

    val baseJdn = WorkCalendarEngine.jalaliToJdn(activeProfile.startYear, activeProfile.startMonth, activeProfile.startDay)
    val daysInMonth = WorkCalendarEngine.getJalaliMonthLength(viewingJy, viewingJm)
    val firstDayJdn = WorkCalendarEngine.jalaliToJdn(viewingJy, viewingJm, 1)
    val startDayOfWeek = WorkCalendarEngine.getJalaliDayOfWeek(firstDayJdn)

    val selectedDaysPassed = selectedJdn - baseJdn
    val turnBefore = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, selectedDaysPassed - 1)
    val turnAfter = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, selectedDaysPassed)
    val (selJy, selJm, selJd) = WorkCalendarEngine.jdnToJalali(selectedJdn)
    val selectedDayName = WorkCalendarEngine.getJalaliDayName(selectedJdn)
    
    val lblShiftBefore = activeProfile.shiftBeforeTemplate.replace("{time}", activeProfile.turnTime).toGhiyasPersianDigits()
    val lblShiftAfter = activeProfile.shiftAfterTemplate.replace("{time}", activeProfile.turnTime).toGhiyasPersianDigits()
    
    var touchStartX by remember { mutableStateOf(0f) }
    var touchStartY by remember { mutableStateOf(0f) }

    Div(attrs = { style { padding(12.px); boxSizing("border-box") } }) {
        
        Div(attrs = { 
            style { 
                backgroundColor(Color("#E8F5E9")); borderRadius(12.px); padding(12.px, 16.px); marginBottom(24.px)
                border(1.px, LineStyle.Solid, Color("#C8E6C9")); display(DisplayStyle.Flex)
                alignItems(AlignItems.Center); justifyContent(JustifyContent.SpaceBetween)
                property("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
                position(Position.Relative)
            } 
        }) {
            Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(12.px) } }) {
                Span(attrs = { style { fontSize(1.8.cssRem) } }) { Text("📅") }
                H2(attrs = { style { color(Color("#1B5E20")); margin(0.px); fontSize(1.2.cssRem) } }) { Text(activeProfile.name.toGhiyasPersianDigits()) }
            }
            
            Div(attrs = { style { display(DisplayStyle.Flex); alignItems(AlignItems.Center); gap(8.px) } }) {
                Span(attrs = { 
                    style { cursor("pointer"); fontSize(1.5.cssRem); color(Color("#2E7D32")); padding(4.px); borderRadius(50.percent); backgroundColor(Color("#C8E6C9")) }
                    title("افزودن تقویم جدید"); onClick { onAddNew() }
                }) { Text("➕") }
                
                Span(attrs = { 
                    style { cursor("pointer"); fontSize(1.5.cssRem); color(Color("#2E7D32")); padding(4.px); borderRadius(50.percent); backgroundColor(Color("transparent")) }
                    onClick { isManagementMenuOpen = !isManagementMenuOpen } 
                }) { Text("⋮") }
            }
            
            if (isManagementMenuOpen) {
                Div(attrs = { style { position(Position.Fixed); top(0.px); left(0.px); width(100.percent); height(100.vh); property("z-index", "90") }; onClick { isManagementMenuOpen = false } }) {}
                Div(attrs = { style { position(Position.Absolute); top(50.px); left(16.px); backgroundColor(Color("white")); borderRadius(8.px); property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)"); padding(8.px, 0.px); minWidth(160.px); property("z-index", "100"); color(Color("#424242")) } }) {
                    Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); property("border-bottom", "1px solid #EEEEEE") }; onClick { isManagementMenuOpen = false; onSetDefault() } }) { Text("⭐ تنظیم پیش‌فرض") }
                    Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); property("border-bottom", "1px solid #EEEEEE") }; onClick { isManagementMenuOpen = false; onEdit() } }) { Text("✏️ ویرایش تقویم") }
                    Div(attrs = { style { padding(12.px, 16.px); cursor("pointer"); color(Color("#D32F2F")) }; onClick { isManagementMenuOpen = false; onDelete() } }) { Text("🗑️ حذف تقویم") }
                }
            }
        }
        
        Div(attrs = { 
            style { 
                backgroundColor(Color("white")); borderRadius(12.px); padding(12.px)
                border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(20.px)
                property("box-shadow", "0 2px 8px rgba(0,0,0,0.05)") 
            }
            onTouchStart { e -> 
                val touchEvent = e.nativeEvent as? TouchEvent
                touchEvent?.touches?.item(0)?.let {
                    touchStartX = it.clientX.toFloat()
                    touchStartY = it.clientY.toFloat()
                }
            }
            onTouchEnd { e ->
                val touchEvent = e.nativeEvent as? TouchEvent
                val touch = touchEvent?.changedTouches?.item(0) ?: return@onTouchEnd
                val touchEndX = touch.clientX.toFloat()
                val touchEndY = touch.clientY.toFloat()
                
                val diffX = touchStartX - touchEndX
                val diffY = kotlin.math.abs(touchStartY - touchEndY)
                
                if (kotlin.math.abs(diffX) > 70 && kotlin.math.abs(diffX) > diffY) {
                    if (diffX > 0) { 
                        if (viewingJm == 1) { viewingJy--; viewingJm = 12 } else { viewingJm-- }
                        expandedBefore = false; expandedAfter = false
                    } else { 
                        if (viewingJm == 12) { viewingJy++; viewingJm = 1 } else { viewingJm++ }
                        expandedBefore = false; expandedAfter = false
                    }
                }
            }
        }) {
            Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); marginBottom(16.px) } }) {
                Span(attrs = { 
                    style { color(Color("#33691E")); fontSize(1.2.cssRem); cursor("pointer"); padding(4.px, 12.px); backgroundColor(Color("#F1F8E9")); borderRadius(8.px) }
                    onClick { if (viewingJm == 12) { viewingJy++; viewingJm = 1 } else { viewingJm++ }; expandedBefore = false; expandedAfter = false } 
                }) { Text("❯") }
                
                Div(attrs = { style { display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); alignItems(AlignItems.Center); gap(4.px) } }) {
                    Span(attrs = { style { fontWeight("bold"); color(Color("#33691E")); fontSize(1.1.cssRem) } }) { Text("${WorkCalendarEngine.getJalaliMonthName(viewingJm)} ${viewingJy.toGhiyasPersianDigits()}") }
                    if (viewingJy != tehranNow.jy || viewingJm != tehranNow.jm) {
                        Span(attrs = {
                            style { cursor("pointer"); fontSize(0.75.cssRem); color(Color("#1976D2")); backgroundColor(Color("#E3F2FD")); padding(2.px, 8.px); borderRadius(6.px) }
                            onClick { viewingJy = tehranNow.jy; viewingJm = tehranNow.jm }
                        }) { Text("بازگشت به امروز") }
                    }
                }
                
                Span(attrs = { 
                    style { color(Color("#33691E")); fontSize(1.2.cssRem); cursor("pointer"); padding(4.px, 12.px); backgroundColor(Color("#F1F8E9")); borderRadius(8.px) }
                    onClick { if (viewingJm == 1) { viewingJy--; viewingJm = 12 } else { viewingJm-- }; expandedBefore = false; expandedAfter = false } 
                }) { Text("❮") }
            }
            
            val daysOfWeek = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
            Div(attrs = { style { display(DisplayStyle.Grid); property("grid-template-columns", "repeat(7, minmax(0, 1fr))"); gap(4.px); textAlign("center"); marginBottom(8.px) } }) {
                daysOfWeek.forEach { day -> Span(attrs = { style { fontWeight("bold"); color(Color("#757575")); fontSize(0.85.cssRem) } }) { Text(day) } }
            }

            Div(attrs = { style { display(DisplayStyle.Grid); property("grid-template-columns", "repeat(7, minmax(0, 1fr))"); gap(4.px) } }) {
                val totalCells = if (startDayOfWeek + daysInMonth > 35) 42 else 35
                for (i in 0 until totalCells) {
                    val currentDay = i - startDayOfWeek + 1
                    val isWithinMonth = currentDay in 1..daysInMonth
                    val cellJdn = if (isWithinMonth) WorkCalendarEngine.jalaliToJdn(viewingJy, viewingJm, currentDay) else -1
                    val isToday = cellJdn == tehranNow.jdn
                    val isSelected = cellJdn == selectedJdn
                    
                    Div(attrs = { 
                        style {
                            height(65.px)
                            borderRadius(6.px); padding(4.px, 1.px)
                            display(DisplayStyle.Flex); flexDirection(FlexDirection.Column)
                            justifyContent(JustifyContent.SpaceBetween)
                            alignItems(AlignItems.Center)
                            if (isWithinMonth) {
                                backgroundColor(if (isSelected) Color("#C8E6C9") else if (isToday) Color("#FFF3E0") else Color("white"))
                                border(if (isSelected) 2.px else 1.px, LineStyle.Solid, if (isSelected) Color("#4CAF50") else Color("#E0E0E0"))
                                cursor("pointer"); boxSizing("border-box")
                            } else backgroundColor(Color("transparent"))
                        }
                        if (isWithinMonth) { onClick { selectedJdn = cellJdn; expandedBefore = false; expandedAfter = false } }
                    }) {
                        if (isWithinMonth) {
                            val turnStart = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, cellJdn - baseJdn)
                            Span(attrs = { style { fontSize(1.cssRem); fontWeight(if(isToday || isSelected) "bold" else "normal"); color(if (isSelected) Color("#1B5E20") else Color("#424242")) } }) { Text(currentDay.toGhiyasPersianDigits()) }
                            if (turnStart != null && turnStart.owner.isNotBlank()) {
                                Span(attrs = { 
                                    style { 
                                        fontSize(0.6.cssRem); color(Color("#2E7D32")); width(100.percent)
                                        textAlign("center"); property("word-wrap", "break-word"); property("overflow-wrap", "break-word")
                                        property("hyphens", "auto"); lineHeight("1.1") 
                                    } 
                                }) { Text(turnStart.owner) }
                            }
                        }
                    }
                }
            }
        }

        Div(attrs = { style { backgroundColor(Color("#FAFAFA")); borderRadius(12.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); property("box-shadow", "0 2px 4px rgba(0,0,0,0.05)") } }) {
            H4(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#424242")); fontSize(1.1.cssRem); textAlign("center") } }) { 
                Text(if(selectedJdn == tehranNow.jdn) "جزئیات نوبت امروز ($selectedDayName ${selJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(selJm)})" else "جزئیات نوبت $selectedDayName ${selJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(selJm)}") 
            }
            
            val timeParts = activeProfile.turnTime.split(":")
            val turnHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 18
            val turnMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            val hasShiftPassedToday = tehranNow.hour > turnHour || (tehranNow.hour == turnHour && tehranNow.minute >= turnMinute)

            val isTurnBeforePast = selectedJdn < tehranNow.jdn || (selectedJdn == tehranNow.jdn && hasShiftPassedToday)
            val isTurnBeforeActive = selectedJdn == tehranNow.jdn && !hasShiftPassedToday
            val isTurnAfterPast = selectedJdn < tehranNow.jdn
            val isTurnAfterActive = selectedJdn == tehranNow.jdn && hasShiftPassedToday

            if (turnBefore != null) {
                Div(attrs = { 
                    style { 
                        borderRadius(8.px); padding(12.px); marginBottom(12.px); property("transition", "all 0.3s ease")
                        if (isTurnBeforePast) {
                            backgroundColor(Color("#F5F5F5"))
                            border(1.px, LineStyle.Solid, Color("#E0E0E0"))
                            property("border-right", "4px solid #9E9E9E")
                            opacity(0.7)
                            property("filter", "grayscale(100%)")
                        } else if (isTurnBeforeActive) {
                            backgroundColor(Color("#E3F2FD"))
                            border(2.px, LineStyle.Solid, Color("#1E88E5"))
                            property("border-right", "6px solid #1565C0")
                            property("box-shadow", "0 2px 8px rgba(30,136,229,0.15)")
                        } else {
                            backgroundColor(Color("white"))
                            border(1.px, LineStyle.Solid, Color("#B3E5FC"))
                            property("border-right", "4px solid #1E88E5")
                        }
                    } 
                }) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.95.cssRem) } }) { 
                        Span(attrs = { style { fontWeight("bold"); color(if(isTurnBeforePast) Color("#757575") else Color("#1565C0")) } }) { Text("⏳ $lblShiftBefore: ") }
                        Span(attrs = { style { color(Color("#424242")); fontWeight("bold") } }) { Text(turnBefore.owner) }
                        if (isTurnBeforePast) {
                            Span(attrs = { style { color(Color("#9E9E9E")); fontSize(0.8.cssRem); marginLeft(8.px) } }) { Text("(پایان یافته)") }
                        } else if (isTurnBeforeActive) {
                            Span(attrs = { style { color(Color("#1565C0")); fontSize(0.8.cssRem); marginLeft(8.px); fontWeight("bold") } }) { Text("(نوبت جاری)") }
                        }
                    }
                    if (turnBefore.notes.isNotBlank()) {
                        P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(if(isTurnBeforePast) Color("#EEEEEE") else Color("#F5F5F5")); padding(6.px); borderRadius(6.px) } }) { Text("📝 " + turnBefore.notes.toGhiyasPersianDigits()) }
                    }
                    Div(attrs = { style { cursor("pointer"); color(if(isTurnBeforePast) Color("#757575") else Color("#1976D2")); fontSize(0.85.cssRem); fontWeight("bold") }; onClick { expandedBefore = !expandedBefore } }) { Text(if (expandedBefore) "🔼 بستن تقویم" else "🔽 مشاهده تقویم این شخص") }
                    if (expandedBefore) {
                        val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnBefore.owner, baseJdn, selectedJdn)
                        Div(attrs = { style { marginTop(12.px); padding(8.px); borderRadius(6.px); backgroundColor(if(isTurnBeforePast) Color("#EEEEEE") else Color("#E3F2FD")) } }) {
                            if (ups.isEmpty()) { P(attrs = { style { margin(0.px); color(Color("#757575")); fontSize(0.8.cssRem) } }) { Text("نوبتی در ماه آینده یافت نشد.") } } else {
                                ups.take(4).forEach { (uJdn, uTurn) ->
                                    val diffDays = uJdn - selectedJdn
                                    val relStr = when { diffDays == 0 -> "همین روز"; diffDays == 1 -> "روز بعد"; else -> "${diffDays.toGhiyasPersianDigits()} روز بعد" }
                                    if (diffDays >= 0) {
                                        val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                        P(attrs = { style { margin(0.px, 0.px, 4.px, 0.px); fontSize(0.85.cssRem); color(if(isTurnBeforePast) Color("#616161") else Color("#0D47A1")); property("border-bottom", "1px dotted ${if(isTurnBeforePast) "#BDBDBD" else "#BBDEFB"}"); paddingBottom(4.px) } }) { Text("🔹 دور ${uTurn.cycle.toGhiyasPersianDigits()}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} ${uJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($relStr)") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (turnAfter != null) {
                Div(attrs = { 
                    style { 
                        borderRadius(8.px); padding(12.px); marginBottom(16.px); property("transition", "all 0.3s ease")
                        if (isTurnAfterPast) {
                            backgroundColor(Color("#F5F5F5"))
                            border(1.px, LineStyle.Solid, Color("#E0E0E0"))
                            property("border-right", "4px solid #9E9E9E")
                            opacity(0.7)
                            property("filter", "grayscale(100%)")
                        } else if (isTurnAfterActive) {
                            backgroundColor(Color("#FFF3E0"))
                            border(2.px, LineStyle.Solid, Color("#EF6C00"))
                            property("border-right", "6px solid #E65100")
                            property("box-shadow", "0 2px 8px rgba(239,108,0,0.15)")
                        } else {
                            backgroundColor(Color("white"))
                            border(1.px, LineStyle.Solid, Color("#FFE0B2"))
                            property("border-right", "4px solid #FB8C00")
                        }
                    } 
                }) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.95.cssRem) } }) { 
                        Span(attrs = { style { fontWeight("bold"); color(if(isTurnAfterPast) Color("#757575") else Color("#E65100")) } }) { Text("⏳ $lblShiftAfter: ") }
                        Span(attrs = { style { color(Color("#424242")); fontWeight("bold") } }) { Text(turnAfter.owner) }
                        if (isTurnAfterPast) {
                            Span(attrs = { style { color(Color("#9E9E9E")); fontSize(0.8.cssRem); marginLeft(8.px) } }) { Text("(پایان یافته)") }
                        } else if (isTurnAfterActive) {
                            Span(attrs = { style { color(Color("#E65100")); fontSize(0.8.cssRem); marginLeft(8.px); fontWeight("bold") } }) { Text("(نوبت جاری)") }
                        }
                    }
                    if (turnAfter.notes.isNotBlank()) {
                        P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(if(isTurnAfterPast) Color("#EEEEEE") else Color("#F5F5F5")); padding(6.px); borderRadius(6.px) } }) { Text("📝 " + turnAfter.notes.toGhiyasPersianDigits()) }
                    }
                    Div(attrs = { style { cursor("pointer"); color(if(isTurnAfterPast) Color("#757575") else Color("#F57F17")); fontSize(0.85.cssRem); fontWeight("bold") }; onClick { expandedAfter = !expandedAfter } }) { Text(if (expandedAfter) "🔼 بستن تقویم" else "🔽 مشاهده تقویم این شخص") }
                    if (expandedAfter) {
                        val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnAfter.owner, baseJdn, selectedJdn)
                        Div(attrs = { style { marginTop(12.px); padding(8.px); borderRadius(6.px); backgroundColor(if(isTurnAfterPast) Color("#EEEEEE") else Color("#FFF3E0")) } }) {
                            if (ups.isEmpty()) { P(attrs = { style { margin(0.px); color(Color("#757575")); fontSize(0.8.cssRem) } }) { Text("نوبتی در ماه آینده یافت نشد.") } } else {
                                ups.take(4).forEach { (uJdn, uTurn) ->
                                    val diffDays = uJdn - selectedJdn
                                    val relStr = when { diffDays == 0 -> "همین روز"; diffDays == 1 -> "روز بعد"; else -> "${diffDays.toGhiyasPersianDigits()} روز بعد" }
                                    if (diffDays >= 0) {
                                        val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                        P(attrs = { style { margin(0.px, 0.px, 4.px, 0.px); fontSize(0.85.cssRem); color(if(isTurnAfterPast) Color("#616161") else Color("#E65100")); property("border-bottom", "1px dotted ${if(isTurnAfterPast) "#BDBDBD" else "#FFE0B2"}"); paddingBottom(4.px) } }) { Text("🔹 دور ${uTurn.cycle.toGhiyasPersianDigits()}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} ${uJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($relStr)") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (selectedJdn == tehranNow.jdn) {
                Button(attrs = { 
                    style { width(100.percent); padding(12.px); backgroundColor(Color("#2E7D32")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); fontWeight("bold"); cursor("pointer") }
                    onClick {
                        // منطق ریاضیاتی قطعی برای تشخیص نوبت‌های امروز بر اساس عبور از زمان شیفت
                        val J = tehranNow.jdn
                        val pastOwnerIdx: Int; val currOwnerIdx: Int; val newOwnerIdx: Int; val nextOwnerIdx: Int
                        val pastLabelJdn: Int; val currLabelJdn: Int; val newLabelJdn: Int; val nextLabelJdn: Int
                        val pastIsAfter: Boolean; val currIsAfter: Boolean; val newIsAfter: Boolean; val nextIsAfter: Boolean
                        val searchBaseJdn: Int

                        if (hasShiftPassedToday) {
                            searchBaseJdn = J
                            pastOwnerIdx = J - 1 - baseJdn; currOwnerIdx = J - baseJdn; newOwnerIdx = J + 1 - baseJdn; nextOwnerIdx = J + 2 - baseJdn
                            pastLabelJdn = J; currLabelJdn = J; newLabelJdn = J + 1; nextLabelJdn = J + 2
                            pastIsAfter = false; currIsAfter = true; newIsAfter = true; nextIsAfter = true
                        } else {
                            searchBaseJdn = J - 1
                            pastOwnerIdx = J - 2 - baseJdn; currOwnerIdx = J - 1 - baseJdn; newOwnerIdx = J - baseJdn; nextOwnerIdx = J + 1 - baseJdn
                            pastLabelJdn = J - 1; currLabelJdn = J; newLabelJdn = J; nextLabelJdn = J + 1
                            pastIsAfter = false; currIsAfter = false; newIsAfter = true; nextIsAfter = true
                        }

                        val pastTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, pastOwnerIdx)
                        val currTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, currOwnerIdx)
                        val newTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, newOwnerIdx)
                        val nextTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, nextOwnerIdx)

                        fun formatTurnDate(jdn: Int, isAfterShift: Boolean): String {
                            val (_, jm, jd) = WorkCalendarEngine.jdnToJalali(jdn)
                            val dayName = WorkCalendarEngine.getJalaliDayName(jdn)
                            val monthName = WorkCalendarEngine.getJalaliMonthName(jm)
                            val shiftLbl = if(isAfterShift) lblShiftAfter else lblShiftBefore
                            return "$shiftLbl، $dayName ${jd.toGhiyasPersianDigits()} $monthName"
                        }

                        val notificationText = buildString {
                            appendLine("💧 اطلاعیه ${activeProfile.name}".toGhiyasPersianDigits())
                            appendLine("📅 ${WorkCalendarEngine.getJalaliDayName(tehranNow.jdn)} ${tehranNow.jd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(tehranNow.jm)} ${tehranNow.jy.toGhiyasPersianDigits()}")
                            appendLine("───────────────────")
                            
                            appendLine("⏮ نوبت گذشته (${formatTurnDate(pastLabelJdn, pastIsAfter)}):")
                            appendLine("👤 ${pastTurn?.owner ?: ""}")
                            appendLine()
                            
                            appendLine("⏳ نوبت جاری (${formatTurnDate(currLabelJdn, currIsAfter)}):")
                            appendLine("👤 ${currTurn?.owner ?: ""}")
                            if (currTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${currTurn.notes.toGhiyasPersianDigits()}")
                            appendLine()
                            
                            appendLine("🔔 نوبت جدید (پیش‌آگاهی - ${formatTurnDate(newLabelJdn, newIsAfter)}):")
                            appendLine("👤 ${newTurn?.owner ?: ""}")
                            if (newTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${newTurn.notes.toGhiyasPersianDigits()}")
                            
                            if (currTurn != null && currTurn.owner.isNotBlank()) {
                                val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, currTurn.owner, baseJdn, searchBaseJdn)
                                if (ups.isNotEmpty()) {
                                    appendLine()
                                    appendLine("🗓 نوبت‌های پیش‌روی ${currTurn.owner} (۱ ماه آینده):".toGhiyasPersianDigits())
                                    appendLine("───────────────────")
                                    ups.take(4).forEach { (uJdn, uTurn) ->
                                        val uDiff = uJdn - searchBaseJdn
                                        val uRelStr = when { 
                                            uDiff == 0 -> "همین دور"
                                            uDiff == 1 -> "روز بعد" 
                                            else -> "${uDiff.toGhiyasPersianDigits()} روز بعد" 
                                        }
                                        if (uDiff >= 0) {
                                            val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                            val uDayName = WorkCalendarEngine.getJalaliDayName(uJdn)
                                            val uMonthName = WorkCalendarEngine.getJalaliMonthName(uJm)
                                            appendLine("🔹 دور ${uTurn.cycle.toGhiyasPersianDigits()}: $uDayName ${uJd.toGhiyasPersianDigits()} $uMonthName ($uRelStr)")
                                        }
                                    }
                                    appendLine("───────────────────")
                                }
                            }
                            
                            appendLine()
                            appendLine("⏭ نوبت پس‌فردا (${formatTurnDate(nextLabelJdn, nextIsAfter)}):")
                            appendLine("👤 ${nextTurn?.owner ?: ""}")
                            if (nextTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${nextTurn.notes.toGhiyasPersianDigits()}")
                            
                            appendLine("───────────────────")
                            appendLine("✨ ذکر و صلوات روز:")
                            appendLine("🌸 اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ وَعَجِّلْ فَرَجَهُمْ 🌸")
                            appendLine("🌷 صلوات حضرت فاطمه زهرا (سلام‌الله‌علیها):")
                            appendLine("«اللَّهُمَّ صَلِّ عَلَى فَاطِمَةَ وَأَبِيهَا وَبَعْلِهَا وَبَنِيهَا وَالسِّرِّ الْمُسْتَوْدَعِ فِيهَا بِعَدَدِ مَا أَحَاطَ بِهِ عِلْمُكَ»")
                            appendLine("───────────────────")
                            if (dailyQuote != null) {
                                appendLine("💬 جمله روز:")
                                appendLine("«${dailyQuote.text.toGhiyasPersianDigits()}»")
                                if (dailyQuote.translation.isNotBlank()) appendLine(dailyQuote.translation.toGhiyasPersianDigits())
                                if (dailyQuote.source.isNotBlank()) appendLine("📖 منبع: ${dailyQuote.source.toGhiyasPersianDigits()}")
                                appendLine("───────────────────")
                            }
                            val tJy = tehranNow.jy.toString()
                            val tJm = tehranNow.jm.toString().padStart(2, '0')
                            val tJd = tehranNow.jd.toString().padStart(2, '0')
                            append("🆔 #WATER_${tJy}${tJm}${tJd}_DAILY")
                        }
                        window.navigator.clipboard.writeText(notificationText.trimEnd()).then { window.alert("متن اطلاعیه با فرمت صحیح کپی شد.") }
                    }
                }) { Text("💬 کپی پیام اطلاعیه این روز") }
            } else {
                Button(attrs = { 
                    style { width(100.percent); padding(12.px); backgroundColor(Color("#1565C0")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); fontWeight("bold"); cursor("pointer") }
                    onClick {
                        val detailsText = buildString {
                            appendLine("📅 جزئیات نوبت $selectedDayName ${selJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(selJm)}")
                            appendLine("───────────────────")
                            if (turnBefore != null) {
                                appendLine("⏳ $lblShiftBefore:")
                                appendLine("👤 ${turnBefore.owner}")
                                if (turnBefore.notes.isNotBlank()) appendLine("📝 توضیحات: ${turnBefore.notes.toGhiyasPersianDigits()}")
                                
                                val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnBefore.owner, baseJdn, selectedJdn)
                                if (ups.isNotEmpty()) {
                                    appendLine("🗓 نوبت‌های پیش‌روی ${turnBefore.owner} (۱ ماه آینده):".toGhiyasPersianDigits())
                                    ups.take(4).forEach { (uJdn, uTurn) ->
                                        val uDiff = uJdn - selectedJdn
                                        val uRelStr = when { uDiff == 0 -> "همین روز"; uDiff == 1 -> "روز بعد"; else -> "${uDiff.toGhiyasPersianDigits()} روز بعد" }
                                        if (uDiff >= 0) {
                                            val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                            appendLine("🔹 دور ${uTurn.cycle.toGhiyasPersianDigits()}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} ${uJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($uRelStr)")
                                        }
                                    }
                                }
                                appendLine("───────────────────")
                            }
                            if (turnAfter != null) {
                                appendLine("⏳ $lblShiftAfter:")
                                appendLine("👤 ${turnAfter.owner}")
                                if (turnAfter.notes.isNotBlank()) appendLine("📝 توضیحات: ${turnAfter.notes.toGhiyasPersianDigits()}")
                                
                                val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnAfter.owner, baseJdn, selectedJdn)
                                if (ups.isNotEmpty()) {
                                    appendLine("🗓 نوبت‌های پیش‌روی ${turnAfter.owner} (۱ ماه آینده):".toGhiyasPersianDigits())
                                    ups.take(4).forEach { (uJdn, uTurn) ->
                                        val uDiff = uJdn - selectedJdn
                                        val uRelStr = when { uDiff == 0 -> "همین روز"; uDiff == 1 -> "روز بعد"; else -> "${uDiff.toGhiyasPersianDigits()} روز بعد" }
                                        if (uDiff >= 0) {
                                            val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                            appendLine("🔹 دور ${uTurn.cycle.toGhiyasPersianDigits()}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} ${uJd.toGhiyasPersianDigits()} ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($uRelStr)")
                                        }
                                    }
                                }
                            }
                        }
                        window.navigator.clipboard.writeText(detailsText.trimEnd()).then { window.alert("جزئیات این روز کپی شد.") }
                    }
                }) { Text("📋 کپی جزئیات این روز") }
            }
        }

        if (dailyQuote != null) {
            Div(attrs = { style { marginTop(24.px); padding(16.px); backgroundColor(Color("white")); borderRadius(12.px); border(1.px, LineStyle.Dashed, Color("#BDBDBD")); textAlign("center") } }) {
                P(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#757575")); fontSize(0.9.cssRem) } }) { Text("💬 جمله روز") }
                P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#2E7D32")); fontSize(1.1.cssRem); fontWeight("bold") } }) { Text("«${dailyQuote.text.toGhiyasPersianDigits()}»") }
                if (dailyQuote.translation.isNotBlank()) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text(dailyQuote.translation.toGhiyasPersianDigits()) }
                }
                if (dailyQuote.source.isNotBlank()) {
                    P(attrs = { style { margin(0.px); color(Color("#9E9E9E")); fontSize(0.85.cssRem) } }) { Text("📖 منبع: ${dailyQuote.source.toGhiyasPersianDigits()}") }
                }
                
                Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.Center); marginTop(16.px) } }) {
                    Span(attrs = {
                        style { cursor("pointer"); fontSize(1.4.cssRem); color(Color("#4CAF50")); backgroundColor(Color("#F1F8E9")); padding(8.px); borderRadius(50.percent); display(DisplayStyle.Flex); alignItems(AlignItems.Center); justifyContent(JustifyContent.Center); width(36.px); height(36.px) }
                        title("تغییر جمله")
                        onClick { quoteIndex = (activeProfile.quotes.indices).random() }
                    }) { Text("🔄") }
                }
            }
        }
    }
}
