package ir.ghiyas.alimaa.domain.models

data class WalnutUnit(val value: Double) {
    operator fun plus(other: WalnutUnit): WalnutUnit = WalnutUnit(this.value + other.value)
    operator fun div(divisor: Double): WalnutUnit = WalnutUnit(this.value / divisor)
    
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