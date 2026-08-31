package ir.ghiyas.alimaa.ui.stages

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.domain.models.CalendarType
import ir.ghiyas.alimaa.domain.models.QuoteItem
import ir.ghiyas.alimaa.domain.models.WorkCalendarProfile
import ir.ghiyas.alimaa.domain.models.WorkTurn
import ir.ghiyas.alimaa.data.WorkCalendarRepository
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlin.js.json

class WorkCalendarFormState {
    var isVisible by mutableStateOf(false)
    var existingId by mutableStateOf<String?>(null)
    
    var calendarType by mutableStateOf(CalendarType.DAY_BASED)
    var calendarName by mutableStateOf("")
    
    var configError by mutableStateOf<String?>(null)
    var scheduleError by mutableStateOf<String?>(null)
    var quotesError by mutableStateOf<String?>(null)
    
    // متغیرهای متنی برای جلوگیری از پرش فیلدها حین تایپ و پاک کردن
    var parsedStartYearText by mutableStateOf("1405")
    var parsedStartYear: Int
        get() = parsedStartYearText.toIntOrNull() ?: 1400
        set(value) { parsedStartYearText = value.toString() }

    var parsedStartMonthText by mutableStateOf("5")
    var parsedStartMonth: Int
        get() = parsedStartMonthText.toIntOrNull() ?: 1
        set(value) { parsedStartMonthText = value.toString() }

    var parsedStartDayText by mutableStateOf("15")
    var parsedStartDay: Int
        get() = parsedStartDayText.toIntOrNull() ?: 1
        set(value) { parsedStartDayText = value.toString() }

    var parsedTurnTime by mutableStateOf("18:00")
    
    var shiftBeforeTemplate by mutableStateOf("تا ساعت {time}")
    var shiftAfterTemplate by mutableStateOf("از ساعت {time}")
    
    var parsedSchedule by mutableStateOf<List<WorkTurn>?>(null)
    var parsedQuotes by mutableStateOf<List<QuoteItem>>(emptyList())

    fun reset() {
        existingId = null
        calendarType = CalendarType.DAY_BASED
        calendarName = ""
        configError = null
        scheduleError = null
        quotesError = null
        parsedStartYearText = "1405"
        parsedStartMonthText = "5"
        parsedStartDayText = "15"
        parsedTurnTime = "18:00"
        shiftBeforeTemplate = "تا ساعت {time}"
        shiftAfterTemplate = "از ساعت {time}"
        parsedSchedule = null
        parsedQuotes = emptyList()
    }
    
    val isFormValid: Boolean
        get() = calendarType == CalendarType.DAY_BASED && calendarName.isNotBlank() && parsedSchedule != null && configError == null && scheduleError == null
}

@Composable
fun CalendarManagerForm(
    state: WorkCalendarFormState,
    onProfileSaved: () -> Unit,
    onCancel: () -> Unit
) {
    Div(attrs = { style { padding(16.px); backgroundColor(Color("white")); borderRadius(12.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")) } }) {
        H3(attrs = { style { color(Color("#212121")); fontSize(1.2.cssRem); marginTop(0.px); marginBottom(20.px); textAlign("center") } }) { 
            Text(if (state.existingId != null) "ویرایش تقویم کاری" else "افزودن تقویم کاری جدید") 
        }

        Div(attrs = { style { marginBottom(16.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Block); marginBottom(8.px); color(Color("#424242")); fontSize(0.95.cssRem); fontWeight("bold") } }) { Text("نوع معماری تقویم را انتخاب کنید:") }
            Select(attrs = {
                style { width(100.percent); boxSizing("border-box"); padding(10.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontSize(0.95.cssRem); fontFamily("Vazirmatn"); property("text-overflow", "ellipsis"); overflow("hidden") }
                onChange { e -> state.calendarType = CalendarType.valueOf(e.value ?: CalendarType.DAY_BASED.name) }
            }) {
                Option(value = CalendarType.DAY_BASED.name, attrs = { if (state.calendarType == CalendarType.DAY_BASED) selected() }) { Text("مبتنی بر روز (آبیاری و کشاورزی)") }
                Option(value = CalendarType.TIMELINE_BASED.name, attrs = { if (state.calendarType == CalendarType.TIMELINE_BASED) selected() }) { Text("مبتنی بر خط زمانی (نگهبانی و... - به زودی)") }
            }
        }

        if (state.calendarType == CalendarType.TIMELINE_BASED) {
            Div(attrs = { style { backgroundColor(Color("#FFF3E0")); padding(16.px); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#FFB74D")); textAlign("center"); marginBottom(20.px) } }) {
                P(attrs = { style { color(Color("#E65100")); fontWeight("bold"); margin(0.px) } }) { Text("این موتور به زودی در بروزرسانی‌های بعدی اضافه خواهد شد.") }
            }
            Div(attrs = { style { display(DisplayStyle.Flex); gap(12.px) } }) {
                Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#F5F5F5")); color(Color("#424242")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); cursor("pointer") }; onClick { onCancel() } }) { Text("بازگشت") }
            }
            return@Div
        }

        Div(attrs = { style { marginBottom(16.px) } }) {
            Label(attrs = { style { display(DisplayStyle.Block); marginBottom(8.px); color(Color("#424242")); fontSize(0.95.cssRem) } }) { Text("نام تقویم (مثلاً: تقویم آبیاری اسلام‌آباد)") }
            Input(InputType.Text) {
                value(state.calendarName)
                onInput { state.calendarName = it.value ?: "" }
                style { width(100.percent); boxSizing("border-box"); padding(10.px); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontSize(1.cssRem); fontFamily("Vazirmatn") }
            }
        }

        Div(attrs = { style { marginBottom(16.px); padding(12.px); backgroundColor(Color("#FAFAFA")); borderRadius(8.px); border(1.px, LineStyle.Solid, Color("#E0E0E0")) } }) {
            H4(attrs = { style { margin(0.px, 0.px, 12.px, 0.px); color(Color("#424242")) } }) { Text("اطلاعات مبدأ و قالب‌های نمایشی") }
            
            Div(attrs = { style { display(DisplayStyle.Flex); gap(8.px); marginBottom(8.px) } }) {
                Div(attrs = { style { flex(1) } }) {
                    Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")) } }) { Text("سال مبدأ") }
                    // اصلاح نوع متغیر و جلوگیری از خطای Assignment type mismatch
                    Input(InputType.Number) { 
                        value(state.parsedStartYearText)
                        onInput { state.parsedStartYearText = it.value?.toString() ?: "" }
                        style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); boxSizing("border-box") } 
                    }
                }
                Div(attrs = { style { flex(1) } }) {
                    Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")) } }) { Text("ماه مبدأ") }
                    // اصلاح نوع متغیر و جلوگیری از خطای Assignment type mismatch
                    Input(InputType.Number) { 
                        value(state.parsedStartMonthText)
                        onInput { state.parsedStartMonthText = it.value?.toString() ?: "" }
                        style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); boxSizing("border-box") } 
                    }
                }
                Div(attrs = { style { flex(1) } }) {
                    Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")) } }) { Text("روز مبدأ") }
                    // اصلاح نوع متغیر و جلوگیری از خطای Assignment type mismatch
                    Input(InputType.Number) { 
                        value(state.parsedStartDayText)
                        onInput { state.parsedStartDayText = it.value?.toString() ?: "" }
                        style { width(100.percent); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); boxSizing("border-box") } 
                    }
                }
            }
            
            Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) { Text("ساعت تغییر نوبت (مثلاً 18:00)") }
            Input(InputType.Text) { value(state.parsedTurnTime); onInput { state.parsedTurnTime = it.value ?: "" }; style { width(100.percent); boxSizing("border-box"); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); property("direction", "ltr") } }

            Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) { Text("متن شیفت اول (متغیر: {time})") }
            Input(InputType.Text) { value(state.shiftBeforeTemplate); onInput { state.shiftBeforeTemplate = it.value ?: "" }; style { width(100.percent); boxSizing("border-box"); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontFamily("Vazirmatn") } }

            Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#757575")); marginTop(8.px) } }) { Text("متن شیفت دوم (متغیر: {time})") }
            Input(InputType.Text) { value(state.shiftAfterTemplate); onInput { state.shiftAfterTemplate = it.value ?: "" }; style { width(100.percent); boxSizing("border-box"); padding(8.px); borderRadius(6.px); border(1.px, LineStyle.Solid, Color("#BDBDBD")); fontFamily("Vazirmatn") } }
            
            Label(attrs = { style { display(DisplayStyle.Block); fontSize(0.85.cssRem); color(Color("#2E7D32")); marginTop(12.px); cursor("pointer") } }) { Text("یا آپلود فایل config.json (اختیاری - برای پر کردن خودکار فرم بالا)") }
            Input(InputType.File) {
                style { width(100.percent); fontFamily("Vazirmatn"); fontSize(0.8.cssRem); marginTop(4.px) }
                onChange { event ->
                    val file = event.target.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = { e ->
                            try {
                                val text = e.target.asDynamic().result as String
                                val jsonObj = JSON.parse<dynamic>(text)
                                state.parsedStartYear = (jsonObj.start_year as Number).toInt()
                                state.parsedStartMonth = (jsonObj.start_month as Number).toInt()
                                state.parsedStartDay = (jsonObj.start_day as Number).toInt()
                                state.parsedTurnTime = jsonObj.turn_time as String
                                if (jsonObj.shift_before_template != undefined) state.shiftBeforeTemplate = jsonObj.shift_before_template as String
                                if (jsonObj.shift_after_template != undefined) state.shiftAfterTemplate = jsonObj.shift_after_template as String
                                state.configError = null
                            } catch (ex: Exception) { state.configError = "ساختار JSON نامعتبر است." }
                        }
                        reader.readAsText(file)
                    }
                }
            }
        }

        Div(attrs = { style { marginBottom(16.px); padding(12.px); backgroundColor(Color("#FAFAFA")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#9E9E9E")) } }) {
            Label(attrs = { style { display(DisplayStyle.Block); marginBottom(8.px); color(Color("#424242")); fontSize(0.95.cssRem); fontWeight("bold") } }) { Text("بارگذاری فایل زمان‌بندی (schedule.json)") }
            Input(InputType.File) {
                style { width(100.percent); fontFamily("Vazirmatn"); fontSize(0.9.cssRem) }
                onChange { event ->
                    val file = event.target.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = { e ->
                            try {
                                val text = e.target.asDynamic().result as String
                                val jsonArray = JSON.parse<Array<dynamic>>(text)
                                state.parsedSchedule = jsonArray.map { 
                                    WorkTurn(turnId = (it.turn_id as Number).toInt(), cycle = (it.cycle as Number).toInt(), owner = it.owner as String, notes = if (it.notes != undefined) it.notes as String else "")
                                }
                                state.scheduleError = null
                            } catch (ex: Exception) { state.scheduleError = "خطا در خواندن فایل زمان‌بندی." }
                        }
                        reader.readAsText(file)
                    }
                }
            }
            if (state.scheduleError != null) { P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); marginTop(8.px) } }) { Text(state.scheduleError!!) } }
            else if (state.parsedSchedule != null) { P(attrs = { style { color(Color("#388E3C")); fontSize(0.85.cssRem); marginTop(8.px); fontWeight("bold") } }) { Text("✅ زمان‌بندی با ${state.parsedSchedule!!.size} شیفت بارگذاری شد.") } }
        }

        Div(attrs = { style { marginBottom(24.px); padding(12.px); backgroundColor(Color("#FAFAFA")); borderRadius(8.px); border(1.px, LineStyle.Dashed, Color("#9E9E9E")) } }) {
            Label(attrs = { style { display(DisplayStyle.Block); marginBottom(8.px); color(Color("#424242")); fontSize(0.95.cssRem); fontWeight("bold") } }) { Text("بارگذاری فایل جملات روز (quotes.json - اختیاری)") }
            Input(InputType.File) {
                style { width(100.percent); fontFamily("Vazirmatn"); fontSize(0.9.cssRem) }
                onChange { event ->
                    val file = event.target.files?.get(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = { e ->
                            try {
                                val text = e.target.asDynamic().result as String
                                val jsonArray = JSON.parse<Array<dynamic>>(text)
                                state.parsedQuotes = jsonArray.map { 
                                    QuoteItem(
                                        text = if (it.text != undefined) it.text as String else "",
                                        translation = if (it.translation != undefined) it.translation as String else "",
                                        source = if (it.source != undefined) it.source as String else ""
                                    )
                                }
                                state.quotesError = null
                            } catch (ex: Exception) { state.quotesError = "خطا در خواندن فایل جملات." }
                        }
                        reader.readAsText(file)
                    }
                }
            }
            if (state.quotesError != null) { P(attrs = { style { color(Color("#D32F2F")); fontSize(0.85.cssRem); marginTop(8.px) } }) { Text(state.quotesError!!) } }
            else if (state.parsedQuotes.isNotEmpty()) { P(attrs = { style { color(Color("#388E3C")); fontSize(0.85.cssRem); marginTop(8.px); fontWeight("bold") } }) { Text("✅ ${state.parsedQuotes.size} جمله روز بارگذاری شد.") } }
        }

        Div(attrs = { style { display(DisplayStyle.Flex); gap(12.px) } }) {
            Button(attrs = { style { flex(1); padding(12.px); backgroundColor(Color("#F5F5F5")); color(Color("#424242")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); cursor("pointer") }; onClick { onCancel() } }) { Text("لغو") }
            Button(attrs = { 
                style { flex(2); padding(12.px); backgroundColor(if(state.isFormValid) Color("#2E7D32") else Color("#9E9E9E")); color(Color("white")); border(0.px); borderRadius(8.px); fontSize(1.cssRem); fontWeight("bold"); cursor(if(state.isFormValid) "pointer" else "not-allowed") }
                if (state.isFormValid) {
                    onClick {
                        val newProfile = WorkCalendarProfile(
                            id = state.existingId ?: "calc_${kotlin.js.Date().getTime()}",
                            name = state.calendarName,
                            isDefault = false,
                            type = state.calendarType,
                            startYear = state.parsedStartYear,
                            startMonth = state.parsedStartMonth,
                            startDay = state.parsedStartDay,
                            turnTime = state.parsedTurnTime,
                            shiftBeforeTemplate = state.shiftBeforeTemplate,
                            shiftAfterTemplate = state.shiftAfterTemplate,
                            schedule = state.parsedSchedule!!,
                            quotes = state.parsedQuotes
                        )
                        WorkCalendarRepository.saveProfile(newProfile)
                        onProfileSaved()
                    }
                }
            }) { Text("ذخیره تقویم") }
        }
    }
}
