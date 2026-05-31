package ir.ghiyas.alimaa.domain.models

data class WalnutUnit(val value: Double) {
    operator fun plus(other: WalnutUnit): WalnutUnit = WalnutUnit(this.value + other.value)
    
    operator fun minus(other: WalnutUnit): WalnutUnit = WalnutUnit(this.value - other.value) 
    
    operator fun div(divisor: Double): WalnutUnit = WalnutUnit(this.value / divisor)
    
    // این خط برای پشتیبانی از محاسبات موتور قیاس اضافه شد
    operator fun times(multiplier: Double): WalnutUnit = WalnutUnit(this.value * multiplier)
    
    override fun toString(): String {
        val str = value.toString()
        return if (str.endsWith(".0")) str.dropLast(2) else str
    }

    companion object {
        fun fromInput(input: String): WalnutUnit {
            val num = input.toDoubleOrNull() ?: 0.0
            return WalnutUnit(num)
        }
        val ZERO = WalnutUnit(0.0)
    }
}
