package ir.ghias.alimaa.ui.theme

import org.jetbrains.compose.web.css.*

object AppStyleSheet : StyleSheet() {
    init {
        // اولویت با فونت دیما
        "*" style {
            fontFamily("DimaWeb", "IRANSans", "Vazirmatn", "Tahoma", "sans-serif")
        }
        
        // بزرگتر کردن سایز پایه کل برنامه
        "html, body" style {
            fontSize(20.px)
        }
        
        // تنظیمات لیبل وقتی فیلد فعال است (موقعیت بالاتر برای جلوگیری از تلاقی با خط)
        ".floating-input:focus ~ .floating-label, .floating-input:not(:placeholder-shown) ~ .floating-label" style {
            top((-14).px) 
            fontSize(16.px) // سایز لیبل شناور بزرگتر شد
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
        marginBottom(32.px) // فاصله بیشتر بین فیلدها برای جلوگیری از فشردگی
    }

    val floatingInput by style {
        width(100.percent)
        padding(18.px, 16.px) // افزایش چشمگیر فضای داخلی و ارتفاع فیلد
        border(1.px, LineStyle.Solid, Color("#BDBDBD"))
        borderRadius(8.px)
        fontSize(20.px) // سایز متن ورودی بسیار خواناتر شد
        property("box-sizing", "border-box")
        backgroundColor(Color("transparent"))
        fontFamily("inherit")
    }

    val floatingLabel by style {
        position(Position.Absolute)
        right(16.px)
        top(18.px) // تنظیم جایگاه اولیه متناسب با ارتفاع جدید فیلد
        color(Color("#757575"))
        fontSize(18.px) // سایز اولیه لیبل ارتقا یافت
        property("transition", "0.2s ease all")
        property("pointer-events", "none")
        backgroundColor(Color("white"))
        padding(0.px, 6.px)
    }
}
