package ir.ghias.alimaa.ui.theme

import org.jetbrains.compose.web.css.*

object AppStyleSheet : StyleSheet() {
    init {
        // تغییر اولویت فونت به دیما وب
        "*" style {
            fontFamily("DimaWeb", "IRANSans", "Vazirmatn", "Tahoma", "sans-serif")
        }
        
        // اجرای انیمیشن بر اساس کلاس‌های ثابت
        ".floating-input:focus ~ .floating-label, .floating-input:not(:placeholder-shown) ~ .floating-label" style {
            top((-10).px)
            fontSize(12.px)
            color(Color("#4CAF50"))
            fontWeight("bold")
        }
        
        ".floating-input:focus" style {
            property("outline", "none")
            border(2.px, LineStyle.Solid, Color("#4CAF50"))
        }
    }

    val floatingContainer by style {
        position(Position.Relative)
        marginBottom(24.px)
    }

    val floatingInput by style {
        width(100.percent)
        padding(14.px, 12.px)
        border(1.px, LineStyle.Solid, Color("#BDBDBD"))
        borderRadius(8.px)
        fontSize(16.px)
        property("box-sizing", "border-box")
        backgroundColor(Color("transparent"))
    }

    val floatingLabel by style {
        position(Position.Absolute)
        right(12.px)
        top(14.px)
        color(Color("#757575"))
        fontSize(14.px)
        property("transition", "0.2s ease all")
        property("pointer-events", "none")
        backgroundColor(Color("white"))
        padding(0.px, 4.px)
    }
}
