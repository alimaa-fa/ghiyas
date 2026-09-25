package ghiyas.alimaa.fa.domain.models

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlinx.serialization.Serializable

@Serializable
data class WalnutUnit(val value: Double) {
    
    // تنظیمات طلایی قیاس برای محاسبات مالی (دقت بالا و گرد کردن استاندارد)
    private fun toBigDecimal(num: Double): BigDecimal {
        return try {
            // تبدیل امن به استرینگ برای جلوگیری از خطای ممیز شناور
            BigDecimal.parseString(num.toString())
        } catch (e: Exception) {
            BigDecimal.fromDouble(num)
        }
    }

    operator fun plus(other: WalnutUnit): WalnutUnit {
        val a = toBigDecimal(this.value)
        val b = toBigDecimal(other.value)
        val result = a.add(b)
        // حفظ ۳ رقم اعشار و ROUND_HALF_AWAY_FROM_ZERO
        return WalnutUnit(result.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))
    }
    
    operator fun minus(other: WalnutUnit): WalnutUnit {
        val a = toBigDecimal(this.value)
        val b = toBigDecimal(other.value)
        val result = a.subtract(b)
        return WalnutUnit(result.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))
    } 
    
    operator fun div(divisor: Double): WalnutUnit {
        if (divisor == 0.0) return WalnutUnit(0.0)
        val a = toBigDecimal(this.value)
        val b = toBigDecimal(divisor)
        val result = a.divide(b, DecimalMode(decimalPrecision = 15, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO))
        return WalnutUnit(result.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))
    }
    
    operator fun times(multiplier: Double): WalnutUnit {
        val a = toBigDecimal(this.value)
        val b = toBigDecimal(multiplier)
        val result = a.multiply(b)
        return WalnutUnit(result.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))
    }
    
    override fun toString(): String {
        // حذف اعشارهای صفر اضافی در زمان نمایش
        val str = value.toString()
        return if (str.endsWith(".0")) str.dropLast(2) else str
    }

    companion object {
        fun fromInput(input: String): WalnutUnit {
            val clean = input.replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3')
                             .replace('۴', '4').replace('۵', '5').replace('۶', '6').replace('۷', '7')
                             .replace('۸', '8').replace('۹', '9').replace('٫', '.')
            return try {
                val parsed = BigDecimal.parseString(clean)
                WalnutUnit(parsed.roundToDigitPositionAfterDecimalPoint(3, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO).doubleValue(false))
            } catch (e: Exception) {
                val num = clean.toDoubleOrNull() ?: 0.0
                WalnutUnit(num)
            }
        }
        val ZERO = WalnutUnit(0.0)
    }
}
