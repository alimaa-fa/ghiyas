package ir.ghiyas.alimaa.domain.calculator

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

object CalculatorMathEngine {
    private val decimalMode = DecimalMode(decimalPrecision = 20, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    fun evaluate(expression: String): String {
        try {
            val balancedExpr = autoBalanceParentheses(expression)
            val sanitized = balancedExpr.replace(" ", "").let { if (it.startsWith("-")) "0$it" else it }.replace("(-", "(0-")
            
            val tokens = tokenize(sanitized)
            val postfix = infixToPostfix(tokens)
            val result = evaluatePostfix(postfix)
            
            val rounded = result.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
            
            var finalStr = rounded.toStringExpanded()
            if (finalStr.contains(".")) {
                finalStr = finalStr.trimEnd('0').trimEnd('.')
            }
            return finalStr
        } catch (e: Exception) {
            return "خطا"
        }
    }

    fun autoBalanceParentheses(expr: String): String {
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        return expr + ")".repeat(maxOf(0, openCount - closeCount))
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    var numStr = ""
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        numStr += expr[i]
                        i++
                    }
                    tokens.add(numStr)
                    continue
                }
                c in "+-*/%()" -> tokens.add(c.toString()) // اضافه شدن عملگر باقیمانده
            }
            i++
        }
        return tokens
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()
        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2, "%" to 2) // اولویت باقیمانده برابر ضرب و تقسیم

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> operators.add(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    if (operators.isNotEmpty() && operators.last() == "(") operators.removeLast()
                }
                else -> {
                    while (operators.isNotEmpty() && operators.last() != "(" &&
                        precedence.getOrElse(operators.last()) { 0 } >= precedence.getOrElse(token) { 0 }
                    ) {
                        output.add(operators.removeLast())
                    }
                    operators.add(token)
                }
            }
        }
        while (operators.isNotEmpty()) output.add(operators.removeLast())
        return output
    }

    private fun evaluatePostfix(postfix: List<String>): BigDecimal {
        val stack = mutableListOf<BigDecimal>()
        for (token in postfix) {
            if (token.toDoubleOrNull() != null) {
                stack.add(BigDecimal.parseString(token))
            } else {
                val b = stack.removeLast()
                val a = stack.removeLast()
                val res = when (token) {
                    "+" -> a.add(b)
                    "-" -> a.subtract(b)
                    "*" -> a.multiply(b)
                    "/" -> a.divide(b, decimalMode) 
                    "%" -> a.remainder(b) // محاسبه دقیق باقیمانده BigNum
                    else -> throw IllegalArgumentException("Unknown operator")
                }
                stack.add(res)
            }
        }
        return stack.first()
    }
}
