package ir.ghiyas.alimaa.presentation.calculator

import ir.ghiyas.alimaa.domain.calculator.CalculatorMathEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorState(
    val expression: String = "",
    val history: List<String> = emptyList(),
    val isPostEquals: Boolean = false,
    val isVisible: Boolean = false,
    val isFullScreen: Boolean = false,
    val isHistoryOpen: Boolean = false // پرچم جدید برای نمایش پنل تاریخچه
)

class CalculatorViewModel {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun toggleVisibility() { _state.update { it.copy(isVisible = !it.isVisible, isHistoryOpen = false) } }
    fun closeCalculator() { _state.update { it.copy(isVisible = false, isHistoryOpen = false) } }
    fun toggleFullScreen() { _state.update { it.copy(isFullScreen = !it.isFullScreen) } }
    
    // کنترلرهای جدید تاریخچه
    fun toggleHistory() { _state.update { it.copy(isHistoryOpen = !it.isHistoryOpen) } }
    fun clearHistory() { _state.update { it.copy(history = emptyList(), isHistoryOpen = false) } }
    fun loadFromHistory(result: String) {
        _state.update { it.copy(expression = result, isPostEquals = false, isHistoryOpen = false) }
    }

    fun onInput(value: String) {
        _state.update { curr ->
            val isOperator = value in listOf("+", "-", "*", "/", "%")
            
            if (curr.isPostEquals) {
                if (curr.expression == "خطا") {
                    curr.copy(expression = value, isPostEquals = false)
                } else if (isOperator) {
                    val newHistory = curr.history.toMutableList()
                    newHistory.add("${curr.expression} $value")
                    curr.copy(expression = curr.expression + value, isPostEquals = false)
                } else {
                    curr.copy(expression = value, isPostEquals = false)
                }
            } else {
                curr.copy(expression = curr.expression + value)
            }
        }
    }

    fun onBackspace() {
        _state.update { curr ->
            if (curr.isPostEquals) {
                if (curr.expression == "خطا") curr.copy(expression = "", isPostEquals = false)
                else curr.copy(isPostEquals = false)
            } else {
                if (curr.expression.isNotEmpty()) {
                    curr.copy(expression = curr.expression.dropLast(1))
                } else curr
            }
        }
    }

    fun onClearAll() {
        _state.update { it.copy(expression = "", isPostEquals = false) }
    }

    fun onEquals() {
        _state.update { curr ->
            if (curr.expression.isBlank() || curr.expression == "خطا" || curr.isPostEquals) return@update curr
            
            val balancedExpr = CalculatorMathEngine.autoBalanceParentheses(curr.expression)
            val result = CalculatorMathEngine.evaluate(balancedExpr)
            
            val newHistory = curr.history.toMutableList()
            // افزایش ظرفیت تاریخچه به ۳۰ آیتم
            if (newHistory.size >= 30) newHistory.removeFirst()
            newHistory.add("$balancedExpr = $result")

            curr.copy(
                expression = result,
                history = newHistory,
                isPostEquals = true
            )
        }
    }
}
