package ir.ghiyas.alimaa.ui.components

import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import androidx.compose.runtime.Composable
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet

@Composable
fun NavigationDrawer(
    onClose: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // لایه تاریک پس‌زمینه
    Div(attrs = {
        classes(AppStyleSheet.drawerOverlay)
        onClick { onClose() }
    })

    // پنل اصلی کشویی
    Div(attrs = {
        classes(AppStyleSheet.drawerPanel)
    }) {
        // هدر منو
        Div(attrs = {
            style {
                padding(20.px)
                backgroundColor(Color("#4CAF50"))
                color(Color("white"))
                fontWeight("bold")
                fontSize(1.2.cssRem)
            }
        }) {
            Text("داشبورد مدیریت قیاس")
        }

        // آیتم‌های منو بر اساس مستند معماری
        Div(attrs = { 
            style { padding(16.px); display(DisplayStyle.Flex); flexDirection(FlexDirection.Column); gap(8.px) } 
        }) {
            
            Div(attrs = { style { fontWeight("bold"); color(Color("#2E7D32")); marginTop(8.px); marginBottom(4.px) } }) { 
                Text("محاسبات اختصاصی") 
            }
            
            Div(attrs = { classes(AppStyleSheet.drawerMenuItem); onClick { onNavigate("builder") } }) {
                Text("🛠 ساخت الگو جدید")
            }
            Div(attrs = { classes(AppStyleSheet.drawerMenuItem); onClick { onNavigate("profile_manager") } }) {
                Text("📁 مدیریت و ویرایش الگوها")
            }

            Div(attrs = { style { height(1.px); backgroundColor(Color("#E0E0E0")); margin(8.px, 0.px) } })

            Div(attrs = { classes(AppStyleSheet.drawerMenuItem); onClick { onNavigate("backup_restore") } }) {
                Text("💾 پشتیبان‌گیری و بازیابی")
            }
            
            Div(attrs = { style { height(1.px); backgroundColor(Color("#E0E0E0")); margin(8.px, 0.px) } })

            Div(attrs = { classes(AppStyleSheet.drawerMenuItem); onClick { onNavigate("main") } }) {
                Text("🏠 بازگشت به داشبورد اصلی")
            }
        }
    }
}
