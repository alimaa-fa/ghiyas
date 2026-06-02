package ir.ghiyas.alimaa.ui.theme

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
            fontSize(0.85.cssRem) // سایز لیبل شناور بزرگتر شد
            color(Color("#4CAF50"))
            fontWeight("bold")
        }
        
        ".floating-input:focus" style {
            property("outline", "none")
            border(2.px, LineStyle.Solid, Color("#4CAF50"))
        }

        // Dropdown uniform font fix
        "select.floating-input, select.floating-input option" style {
            property("font-size", "1.1rem !important")
            property("font-weight", "normal !important")
            color(Color("#212121"))
        }

        // مخفی کردن اسکرول‌بار مرورگر برای نوار تب‌ها (حفظ ظاهر نیتیو)
        ".hide-scrollbar::-webkit-scrollbar" style {
            display(DisplayStyle.None)
        }
        ".hide-scrollbar" style {
            property("-ms-overflow-style", "none") // IE and Edge
            property("scrollbar-width", "none") // Firefox
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
        fontSize(1.1.cssRem) // سایز متن ورودی بسیار خواناتر شد
        property("box-sizing", "border-box")
        backgroundColor(Color("transparent"))
        fontFamily("inherit")
    }

    val floatingLabel by style {
        position(Position.Absolute)
        right(16.px)
        top(18.px) // تنظیم جایگاه اولیه متناسب با ارتفاع جدید فیلد
        color(Color("#757575"))
        fontSize(1.1.cssRem) // سایز اولیه لیبل ارتقا یافت
        property("transition", "0.2s ease all")
        property("pointer-events", "none")
        backgroundColor(Color("white"))
        padding(0.px, 6.px)
    }

    // --- کلاس‌های معماری جدید (منوی کشویی و تب‌ها) ---

    val drawerOverlay by style {
        position(Position.Absolute) // اتصال به کادر ۶۰۰ پیکسلی
        top(0.px); left(0.px); right(0.px); bottom(0.px)
        backgroundColor(Color("rgba(0,0,0,0.5)"))
        property("z-index", "1000")
        property("transition", "opacity 0.3s ease")
    }

    val drawerPanel by style {
        position(Position.Absolute) // اتصال به کادر ۶۰۰ پیکسلی
        top(0.px); bottom(0.px); right(0.px)
        width(280.px)
        backgroundColor(Color("white"))
        property("z-index", "1001")
        property("box-shadow", "-2px 0 8px rgba(0,0,0,0.2)")
        property("transition", "transform 0.3s ease")
        property("overflow-y", "auto")
    }

    val drawerMenuItem by style {
        padding(14.px, 16.px)
        borderRadius(8.px)
        cursor("pointer")
        color(Color("#424242"))
        fontSize(1.05.cssRem)
        property("transition", "background-color 0.2s")
        hover {
            backgroundColor(Color("#F1F8E9"))
            color(Color("#2E7D32"))
        }
    }

    val tabContainer by style {
        display(DisplayStyle.Flex)
        backgroundColor(Color("white"))
        position(Position.Sticky)
        top(0.px)
        property("z-index", "50")
        
        // تغییرات جدید: هم‌عرض شدن با کادرها، گرد شدن گوشه‌ها و قابلیت اسکرول
        property("margin", "0px 16px 16px 16px") // ایجاد حاشیه در اطراف کادر تب
        borderRadius(12.px) // گرد کردن گوشه‌ها
        property("overflow-x", "auto") // فعال‌سازی اسکرول افقی
        property("white-space", "nowrap") // جلوگیری از شکستن خط تب‌ها
        property("box-shadow", "0 2px 8px rgba(0,0,0,0.06)") // اضافه کردن سایه ملایم
    }

    val tabItem by style {
        flex(1)
        textAlign("center")
        padding(14.px, 16.px)
        cursor("pointer")
        fontWeight("bold")
        fontSize(0.9.cssRem)
        property("transition", "0.2s all")
        property("min-width", "max-content") // جلوگیری از فشرده شدن متن در حالت اسکرول
    }

    val tabActive by style {
        color(Color("#2E7D32"))
        property("border-bottom", "3px solid #2E7D32")
        backgroundColor(Color("#F1F8E9"))
    }

    val tabInactive by style {
        color(Color("#757575"))
        property("border-bottom", "3px solid transparent")
    }
}
