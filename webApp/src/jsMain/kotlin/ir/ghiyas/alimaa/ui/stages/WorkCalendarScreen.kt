package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import ir.ghiyas.alimaa.domain.models.CalendarType
import ir.ghiyas.alimaa.domain.calculator.WorkCalendarEngine
import kotlinx.browser.window

@Composable
fun WorkCalendarScreen(activeProfile: WorkCalendarProfile?) {
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

    // انتخاب یک جمله تصادفی در زمان رندر صفحه
    val dailyQuote = remember(activeProfile.id) { 
        if (activeProfile.quotes.isNotEmpty()) activeProfile.quotes.random() else null 
    }

    val baseJdn = WorkCalendarEngine.jalaliToJdn(activeProfile.startYear, activeProfile.startMonth, activeProfile.startDay)
    val daysInMonth = WorkCalendarEngine.getJalaliMonthLength(viewingJy, viewingJm)
    val firstDayJdn = WorkCalendarEngine.jalaliToJdn(viewingJy, viewingJm, 1)
    val startDayOfWeek = WorkCalendarEngine.getJalaliDayOfWeek(firstDayJdn)

    val selectedDaysPassed = selectedJdn - baseJdn
    val turnBefore = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, selectedDaysPassed - 1)
    val turnAfter = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, selectedDaysPassed)
    val (selJy, selJm, selJd) = WorkCalendarEngine.jdnToJalali(selectedJdn)
    
    val lblShiftBefore = activeProfile.shiftBeforeTemplate.replace("{time}", activeProfile.turnTime)
    val lblShiftAfter = activeProfile.shiftAfterTemplate.replace("{time}", activeProfile.turnTime)

    Div(attrs = { style { padding(12.px); boxSizing("border-box") } }) {
        
        // 1. زیباسازی عنوان تقویم (هدر کارتی)
        Div(attrs = { 
            style { 
                backgroundColor(Color("#E8F5E9")); borderRadius(12.px); padding(16.px); marginBottom(24.px)
                border(1.px, LineStyle.Solid, Color("#C8E6C9")); display(DisplayStyle.Flex)
                alignItems(AlignItems.Center); justifyContent(JustifyContent.Center); gap(12.px)
                property("box-shadow", "0 2px 4px rgba(0,0,0,0.05)")
            } 
        }) {
            Span(attrs = { style { fontSize(2.cssRem) } }) { Text("📅") }
            H2(attrs = { style { color(Color("#1B5E20")); margin(0.px); fontSize(1.3.cssRem) } }) { Text(activeProfile.name) }
        }
        
        // گرید تقویم
        Div(attrs = { style { backgroundColor(Color("white")); borderRadius(12.px); padding(12.px); border(1.px, LineStyle.Solid, Color("#C5E1A5")); marginBottom(20.px); property("box-shadow", "0 2px 8px rgba(0,0,0,0.05)") } }) {
            Div(attrs = { style { display(DisplayStyle.Flex); justifyContent(JustifyContent.SpaceBetween); alignItems(AlignItems.Center); marginBottom(16.px) } }) {
                Span(attrs = { 
                    style { color(Color("#33691E")); fontSize(1.2.cssRem); cursor("pointer"); padding(4.px, 12.px); backgroundColor(Color("#F1F8E9")); borderRadius(8.px) }
                    onClick { if (viewingJm == 12) { viewingJy++; viewingJm = 1 } else { viewingJm++ }; expandedBefore = false; expandedAfter = false } 
                }) { Text("❯") }
                
                Span(attrs = { style { fontWeight("bold"); color(Color("#33691E")); fontSize(1.1.cssRem) } }) { Text("${WorkCalendarEngine.getJalaliMonthName(viewingJm)} $viewingJy") }
                
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
                            minHeight(60.px); borderRadius(6.px); padding(4.px, 2.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); justifyContent(JustifyContent.Center); alignItems(AlignItems.Center)
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
                            Span(attrs = { style { fontSize(1.cssRem); fontWeight(if(isToday || isSelected) "bold" else "normal"); color(if (isSelected) Color("#1B5E20") else Color("#424242")) } }) { Text("$currentDay") }
                            if (turnStart != null && turnStart.owner.isNotBlank()) {
                                Span(attrs = { style { fontSize(0.65.cssRem); color(Color("#2E7D32")); marginTop(4.px); width(100.percent); textAlign("center"); property("word-wrap", "break-word"); property("hyphens", "auto"); lineHeight("1.2") } }) { Text(turnStart.owner) }
                            }
                        }
                    }
                }
            }
        }

        // کارت جزئیات شیفت‌ها (Card-based UI)
        Div(attrs = { style { backgroundColor(Color("#FAFAFA")); borderRadius(12.px); padding(16.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")); property("box-shadow", "0 2px 4px rgba(0,0,0,0.05)") } }) {
            H4(attrs = { style { margin(0.px, 0.px, 16.px, 0.px); color(Color("#424242")); fontSize(1.1.cssRem); textAlign("center") } }) { 
                Text(if(selectedJdn == tehranNow.jdn) "جزئیات نوبت امروز ($selJd ${WorkCalendarEngine.getJalaliMonthName(selJm)})" else "جزئیات نوبت $selJd ${WorkCalendarEngine.getJalaliMonthName(selJm)}") 
            }
            
            // کارت شیفت اول
            if (turnBefore != null) {
                Div(attrs = { style { backgroundColor(Color("white")); borderRadius(8.px); padding(12.px); marginBottom(12.px); border(1.px, LineStyle.Solid, Color("#B3E5FC")); property("border-right", "4px solid #1E88E5") } }) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.95.cssRem) } }) { 
                        Span(attrs = { style { fontWeight("bold"); color(Color("#1565C0")) } }) { Text("⏳ $lblShiftBefore: ") }
                        Span(attrs = { style { color(Color("#424242")); fontWeight("bold") } }) { Text(turnBefore.owner) }
                    }
                    if (turnBefore.notes.isNotBlank()) {
                        P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#F5F5F5")); padding(6.px); borderRadius(6.px) } }) { Text("📝 " + turnBefore.notes) }
                    }
                    
                    Div(attrs = { style { cursor("pointer"); color(Color("#1976D2")); fontSize(0.85.cssRem); fontWeight("bold") }; onClick { expandedBefore = !expandedBefore } }) { Text(if (expandedBefore) "🔼 بستن تقویم" else "🔽 مشاهده تقویم این شخص") }
                    
                    if (expandedBefore) {
                        val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnBefore.owner, baseJdn, selectedJdn)
                        Div(attrs = { style { marginTop(12.px); padding(8.px); borderRadius(6.px); backgroundColor(Color("#E3F2FD")) } }) {
                            if (ups.isEmpty()) {
                                P(attrs = { style { margin(0.px); color(Color("#757575")); fontSize(0.8.cssRem) } }) { Text("نوبتی در ماه آینده یافت نشد.") }
                            } else {
                                ups.take(4).forEach { (uJdn, uTurn) ->
                                    val diffDays = uJdn - selectedJdn
                                    val relStr = when { diffDays == 0 -> "همین روز"; diffDays == 1 -> "روز بعد"; diffDays > 1 -> "$diffDays روز بعد"; else -> "گذشته" }
                                    if (diffDays >= 0) {
                                        val (uJy, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                        P(attrs = { style { margin(0.px, 0.px, 4.px, 0.px); fontSize(0.85.cssRem); color(Color("#0D47A1")); property("border-bottom", "1px dotted #BBDEFB"); paddingBottom(4.px) } }) { 
                                            Text("🔹 دور ${uTurn.cycle}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} $uJd ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($relStr)") 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // کارت شیفت دوم
            if (turnAfter != null) {
                Div(attrs = { style { backgroundColor(Color("white")); borderRadius(8.px); padding(12.px); marginBottom(16.px); border(1.px, LineStyle.Solid, Color("#FFE0B2")); property("border-right", "4px solid #FB8C00") } }) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.95.cssRem) } }) { 
                        Span(attrs = { style { fontWeight("bold"); color(Color("#E65100")) } }) { Text("⏳ $lblShiftAfter: ") }
                        Span(attrs = { style { color(Color("#424242")); fontWeight("bold") } }) { Text(turnAfter.owner) }
                    }
                    if (turnAfter.notes.isNotBlank()) {
                        P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); fontSize(0.85.cssRem); color(Color("#616161")); backgroundColor(Color("#F5F5F5")); padding(6.px); borderRadius(6.px) } }) { Text("📝 " + turnAfter.notes) }
                    }
                    
                    Div(attrs = { style { cursor("pointer"); color(Color("#F57F17")); fontSize(0.85.cssRem); fontWeight("bold") }; onClick { expandedAfter = !expandedAfter } }) { Text(if (expandedAfter) "🔼 بستن تقویم" else "🔽 مشاهده تقویم این شخص") }
                    
                    if (expandedAfter) {
                        val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, turnAfter.owner, baseJdn, selectedJdn)
                        Div(attrs = { style { marginTop(12.px); padding(8.px); borderRadius(6.px); backgroundColor(Color("#FFF3E0")) } }) {
                            if (ups.isEmpty()) {
                                P(attrs = { style { margin(0.px); color(Color("#757575")); fontSize(0.8.cssRem) } }) { Text("نوبتی در ماه آینده یافت نشد.") }
                            } else {
                                ups.take(4).forEach { (uJdn, uTurn) ->
                                    val diffDays = uJdn - selectedJdn
                                    val relStr = when { diffDays == 0 -> "همین روز"; diffDays == 1 -> "روز بعد"; diffDays > 1 -> "$diffDays روز بعد"; else -> "گذشته" }
                                    if (diffDays >= 0) {
                                        val (uJy, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                        P(attrs = { style { margin(0.px, 0.px, 4.px, 0.px); fontSize(0.85.cssRem); color(Color("#E65100")); property("border-bottom", "1px dotted #FFE0B2"); paddingBottom(4.px) } }) { 
                                            Text("🔹 دور ${uTurn.cycle}: ${WorkCalendarEngine.getJalaliDayName(uJdn)} $uJd ${WorkCalendarEngine.getJalaliMonthName(uJm)} ($relStr)") 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // تولید متن خروجی به روش استاندارد String Builder جهت جلوگیری از به هم ریختگی در اندروید و ایتا
            Button(attrs = { 
                style { width(100.percent); padding(12.px); backgroundColor(Color("#2E7D32")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); fontWeight("bold"); cursor("pointer") }
                onClick {
                    val effectiveJdnForMessage = selectedJdn
                    val tDayName = WorkCalendarEngine.getJalaliDayName(effectiveJdnForMessage)
                    val tMonthName = WorkCalendarEngine.getJalaliMonthName(selJm)
                    
                    val yestJdn = effectiveJdnForMessage - 1
                    val yestTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, yestJdn - baseJdn)
                    val (yy, ym, yd) = WorkCalendarEngine.jdnToJalali(yestJdn)
                    
                    val currTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, effectiveJdnForMessage - baseJdn)
                    
                    val nextJdn = effectiveJdnForMessage + 1
                    val nextTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, nextJdn - baseJdn)
                    val (ny, nm, nd) = WorkCalendarEngine.jdnToJalali(nextJdn)
                    
                    val futJdn = effectiveJdnForMessage + 2
                    val futTurn = WorkCalendarEngine.calculateTurnByDaysPassed(activeProfile.schedule, futJdn - baseJdn)
                    
                    // استفاده از buildString به جای trimIndent برای کنترل دقیق روی کاراکترها و خطوط
                    val notificationText = buildString {
                        appendLine("💧 اطلاعیه ${activeProfile.name}")
                        appendLine("📅 $tDayName $selJd $tMonthName $selJy")
                        appendLine("───────────────────")
                        
                        appendLine("⏮ نوبت دیروز ($lblShiftBefore، $yd ${WorkCalendarEngine.getJalaliMonthName(ym)}):")
                        appendLine("👤 ${yestTurn?.owner ?: ""}")
                        if (yestTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${yestTurn.notes}")
                        appendLine()
                        
                        appendLine("⏳ نوبت جاری ($lblShiftAfter، $selJd $tMonthName):")
                        appendLine("👤 ${turnBefore?.owner ?: ""}")
                        if (turnBefore?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${turnBefore.notes}")
                        appendLine()
                        
                        appendLine("🔔 نوبت جدید (پیش‌آگاهی - $lblShiftAfter، $selJd $tMonthName $lblShiftBefore، $nd ${WorkCalendarEngine.getJalaliMonthName(nm)}):")
                        appendLine("👤 ${currTurn?.owner ?: ""}")
                        if (currTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${currTurn.notes}")
                        
                        // اضافه کردن نوبت‌های پیش رو در صورت وجود
                        if (currTurn != null && currTurn.owner.isNotBlank()) {
                            val ups = WorkCalendarEngine.getUpcomingTurns(activeProfile.schedule, currTurn.owner, baseJdn, effectiveJdnForMessage)
                            if (ups.isNotEmpty()) {
                                appendLine()
                                appendLine("🗓 نوبت‌های پیش‌روی ${currTurn.owner} (۱ ماه آینده):")
                                appendLine("───────────────────")
                                ups.take(4).forEach { (uJdn, uTurn) ->
                                    val uDiff = uJdn - effectiveJdnForMessage
                                    val uRelStr = when { uDiff == 0 -> "همین روز"; uDiff == 1 -> "روز بعد"; else -> "$uDiff روز بعد" }
                                    val (_, uJm, uJd) = WorkCalendarEngine.jdnToJalali(uJdn)
                                    val uDayName = WorkCalendarEngine.getJalaliDayName(uJdn)
                                    val uMonthName = WorkCalendarEngine.getJalaliMonthName(uJm)
                                    appendLine("🔹 دور ${uTurn.cycle}: $uDayName $uJd $uMonthName ($uRelStr)")
                                }
                                appendLine("───────────────────")
                            }
                        }
                        
                        appendLine()
                        appendLine("⏭ نوبت پس‌فردا ($lblShiftAfter، $nd ${WorkCalendarEngine.getJalaliMonthName(nm)}):")
                        appendLine("👤 ${nextTurn?.owner ?: ""}")
                        if (nextTurn?.notes?.isNotBlank() == true) appendLine("📝 توضیحات: ${nextTurn.notes}")
                        
                        appendLine("───────────────────")
                        appendLine("✨ ذکر و صلوات روز:")
                        appendLine("🌸 اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ وَعَجِّلْ فَرَجَهُمْ 🌸")
                        appendLine("🌷 صلوات حضرت فاطمه زهرا (سلام‌الله‌علیها):")
                        appendLine("«اللَّهُمَّ صَلِّ عَلَى فَاطِمَةَ وَأَبِيهَا وَبَعْلِهَا وَبَنِيهَا وَالسِّرِّ الْمُسْتَوْدَعِ فِيهَا بِعَدَدِ مَا أَحَاطَ بِهِ عِلْمُكَ»")
                        appendLine("───────────────────")
                        
                        // اضافه کردن جمله روز در صورت وجود
                        if (dailyQuote != null) {
                            appendLine("💬 جمله روز:")
                            appendLine("«${dailyQuote.text}»")
                            if (dailyQuote.translation.isNotBlank()) appendLine(dailyQuote.translation)
                            if (dailyQuote.source.isNotBlank()) appendLine("📖 منبع: ${dailyQuote.source}")
                            appendLine("───────────────────")
                        }
                        
                        // درج آیدی به صورت امن و ایزوله شده از متن راست‌چین
                        append("🆔 #WATER_${selJy}${selJm.toString().padStart(2, '0')}${selJd.toString().padStart(2, '0')}_DAILY")
                    }

                    window.navigator.clipboard.writeText(notificationText.trimEnd()).then { window.alert("متن اطلاعیه با فرمت صحیح کپی شد.") }
                }
            }) { Text("💬 کپی پیام اطلاعیه این روز") }
        }

        // نمایش بصری جمله روز در پایین تقویم (در صورت وجود)
        if (dailyQuote != null) {
            Div(attrs = { style { marginTop(24.px); padding(16.px); backgroundColor(Color("white")); borderRadius(12.px); border(1.px, LineStyle.Dashed, Color("#BDBDBD")); textAlign("center") } }) {
                P(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#757575")); fontSize(0.9.cssRem) } }) { Text("💬 جمله روز") }
                P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#2E7D32")); fontSize(1.1.cssRem); fontWeight("bold") } }) { Text("«${dailyQuote.text}»") }
                if (dailyQuote.translation.isNotBlank()) {
                    P(attrs = { style { margin(0.px, 0.px, 8.px, 0.px); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text(dailyQuote.translation) }
                }
                if (dailyQuote.source.isNotBlank()) {
                    P(attrs = { style { margin(0.px); color(Color("#9E9E9E")); fontSize(0.85.cssRem) } }) { Text("📖 منبع: ${dailyQuote.source}") }
                }
            }
        }
    }
}
