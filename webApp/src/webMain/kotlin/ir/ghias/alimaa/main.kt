package ir.ghias.alimaa

import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        Div({
            style {
                padding(30.px)
                backgroundColor(Color.green)
                color(Color.white)
                borderRadius(16.px)
                property("box-shadow", "0 4px 15px rgba(0, 0, 0, 0.3)")
                property("direction", "rtl")
                fontFamily("Tahoma, sans-serif")
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
                alignItems(AlignItems.Center)
            }
        }) {
            Div({
                style {
                    fontSize(24.px)
                    fontWeight("bold")
                    marginBottom(10.px)
                }
            }) {
                Text("سیستم مالی قیاس")
            }
            Div({
                style {
                    fontSize(14.px)
                    opacity(0.9)
                }
            }) {
                Text("موتور محاسبات در حال راه‌اندازی...")
            }
        }
    }
}
