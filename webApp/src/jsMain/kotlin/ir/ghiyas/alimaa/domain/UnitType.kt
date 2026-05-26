package ir.ghiyas.alimaa.domain

enum class UnitType(val displayName: String, val dynamicLabel: String) {
    HAND_PIECE("دست/دانه", "کل گردو (دست/دانه)"),
    KILOGRAM("کیلوگرم", "کل محصول (کیلوگرم)"),
    METER_CM("متر/سانتی", "کل متراژ (متر/سانتی)"),
    TOMAN("تومان", "کل پول (تومان)"),
    HOUR_MINUTE("ساعت/دقیقه", "کل زمان (ساعت/دقیقه)"),
    OTHER("سایر", "کل محصول");

    companion object {
        fun getOrderedValues(): List<UnitType> {
            val allExceptOther = entries.filter { it != OTHER }
            return allExceptOther + OTHER
        }
    }
}
