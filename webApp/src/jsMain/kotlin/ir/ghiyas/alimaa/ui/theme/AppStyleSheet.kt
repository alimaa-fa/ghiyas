package ir.ghiyas.alimaa.ui.theme

import org.jetbrains.compose.web.css.*

object AppStyleSheet : StyleSheet() {
    init {
        "*" style { fontFamily("DimaWeb", "IRANSans", "Vazirmatn", "Tahoma", "sans-serif") }
        
        // مخفی کردن کامل اسکرول‌بار مرورگر در کل اپلیکیشن (حس اپلیکیشن نیتیو)
        "html, body" style { 
            fontSize(20.px)
            property("-ms-overflow-style", "none") // IE and Edge
            property("scrollbar-width", "none") // Firefox
        }
        "::-webkit-scrollbar" style { 
            display(DisplayStyle.None) // Chrome, Safari and Opera
        }
        
        ".floating-input:focus ~ .floating-label, .floating-input:not(:placeholder-shown) ~ .floating-label" style {
            top((-14).px); fontSize(0.85.cssRem); color(Color("#4CAF50")); fontWeight("bold")
        }
        ".floating-input:focus" style { property("outline", "none"); border(2.px, LineStyle.Solid, Color("#4CAF50")) }
        "select.floating-input, select.floating-input option" style {
            property("font-size", "1.1rem !important"); property("font-weight", "normal !important"); color(Color("#212121"))
        }

        ".hide-scrollbar::-webkit-scrollbar" style { display(DisplayStyle.None) }
        ".hide-scrollbar" style { property("-ms-overflow-style", "none"); property("scrollbar-width", "none") }
    }

    val floatingContainer by style { position(Position.Relative); marginBottom(32.px) }
    val floatingInput by style {
        width(100.percent); padding(18.px, 16.px); border(1.px, LineStyle.Solid, Color("#BDBDBD"))
        borderRadius(8.px); fontSize(1.1.cssRem); property("box-sizing", "border-box")
        backgroundColor(Color("transparent")); fontFamily("inherit")
    }
    val floatingLabel by style {
        position(Position.Absolute); right(16.px); top(18.px); color(Color("#757575"))
        fontSize(1.1.cssRem); property("transition", "0.2s ease all"); property("pointer-events", "none")
        backgroundColor(Color("white")); padding(0.px, 6.px)
    }

    val drawerOverlay by style { position(Position.Absolute); top(0.px); left(0.px); right(0.px); bottom(0.px); backgroundColor(Color("rgba(0,0,0,0.5)")); property("z-index", "1000"); property("transition", "opacity 0.3s ease") }
    val drawerPanel by style { position(Position.Absolute); top(0.px); bottom(0.px); right(0.px); width(280.px); backgroundColor(Color("white")); property("z-index", "1001"); property("box-shadow", "-2px 0 8px rgba(0,0,0,0.2)"); property("transition", "transform 0.3s ease"); property("overflow-y", "auto") }
    val drawerMenuItem by style { padding(14.px, 16.px); borderRadius(8.px); cursor("pointer"); color(Color("#424242")); fontSize(1.05.cssRem); property("transition", "background-color 0.2s"); hover { backgroundColor(Color("#F1F8E9")); color(Color("#2E7D32")) } }

    val tabContainer by style { display(DisplayStyle.Flex); backgroundColor(Color("white")); position(Position.Sticky); top(0.px); property("z-index", "50"); property("margin", "0px 16px 16px 16px"); borderRadius(12.px); property("overflow-x", "auto"); property("white-space", "nowrap"); property("box-shadow", "0 2px 8px rgba(0,0,0,0.06)") }
    val tabItem by style { flex(1); textAlign("center"); padding(14.px, 16.px); cursor("pointer"); fontWeight("bold"); fontSize(0.9.cssRem); property("transition", "0.2s all"); property("min-width", "max-content") }
    val tabActive by style { color(Color("#2E7D32")); backgroundColor(Color("#F1F8E9")); borderRadius(12.px); property("box-shadow", "0 2px 4px rgba(0,0,0,0.1) inset") }
    val tabInactive by style { color(Color("#757575")); backgroundColor(Color("transparent")) }
}
