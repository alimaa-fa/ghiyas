package ir.ghiyas.alimaa

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.dom.*
import ir.ghiyas.alimaa.ui.components.GhiyasTopAppBar
import ir.ghiyas.alimaa.ui.components.HeroBanner
import ir.ghiyas.alimaa.ui.stages.InputStageScreen
import ir.ghiyas.alimaa.ui.stages.ExpenseStageScreen
import ir.ghiyas.alimaa.presentation.stages.input.InputStageViewModel
import ir.ghiyas.alimaa.presentation.stages.expense.ExpenseStageViewModel
import ir.ghiyas.alimaa.ui.theme.AppStyleSheet
import ir.ghiyas.alimaa.domain.models.WalnutUnit

@Composable
fun App() {
    var clearFormRequested by remember { mutableStateOf(false) }

    val inputViewModel = remember { InputStageViewModel() }
    val expenseViewModel = remember { ExpenseStageViewModel() }
    
    val inputState by inputViewModel.state.collectAsState()

    LaunchedEffect(inputState.totalAmount) {
        val amount = if (inputState.totalAmount.isNotBlank()) inputState.totalAmount else "0"
        expenseViewModel.setTotalWalnuts(WalnutUnit.fromInput(amount))
    }

    Style(AppStyleSheet)

    Div(attrs = {
        dir(DirType.Rtl)
        style {
            property("margin", "0 auto")
            maxWidth(600.px)
            width(100.percent)
            minHeight(100.vh)
            backgroundColor(Color("#F5F5F5"))
            display(DisplayStyle.Flex)
            flexDirection(FlexDirection.Column)
            fontFamily("Vazirmatn", "system-ui", "-apple-system", "sans-serif")
            property("box-shadow", "0 0 15px rgba(0,0,0,0.05)")
        }
    }) {
        GhiyasTopAppBar(
            onClearClick = { 
                clearFormRequested = true
                expenseViewModel.clearForm()
            },
            onHistoryClick = { /* TODO */ }
        )
        
        Div(attrs = {
            style {
                property("flex", "1")
                property("overflow-y", "auto")
                paddingBottom(32.px)
            }
        }) {
            HeroBanner()
            
            InputStageScreen(
                viewModel = inputViewModel,
                onClearRequested = clearFormRequested,
                onClearComplete = { clearFormRequested = false }
            )
            
            ExpenseStageScreen(
                viewModel = expenseViewModel,
                baseUnit = inputState.unitType.displayName,
                calculationName = inputState.calculationName,
                totalInputAmount = inputState.totalAmount
            )
        }
    }
}
