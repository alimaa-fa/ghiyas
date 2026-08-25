package ir.ghiyas.alimaa.domain.models

// نگهداری قوانین اعشار بدون وابستگی به لایه UI
enum class DecimalDisplayRule(val displayName: String, val decimals: Int) {
    NONE("بدون اعشار (پول/تومان)", 0),
    ONE("۱ رقم اعشار (دست/دانه)", 1),
    TWO("۲ رقم اعشار (متر/سانتی‌متر)", 2),
    THREE("۳ رقم اعشار (وزن/کیلوگرم)", 3),
    FREE("اعشار کامل (آزاد)", -1)
}

enum class UnitType(val displayName: String, val dynamicLabel: String) {
    HAND_PIECE("دست/دانه", "کل گردو (دست/دانه)"),
    KILOGRAM("کیلوگرم", "کل محصول (کیلوگرم)"),
    METER_CM("متر/سانتی", "کل متراژ (متر/سانتی)"),
    TOMAN("تومان", "کل پول (تومان)"),
    HOUR_MINUTE("ساعت/دقیقه", "کل زمان (ساعت/دقیقه)"),
    CUSTOM("واحد سفارشی", "مقدار کل (سفارشی)"), // اضافه شدن واحد سفارشی
    OTHER("سایر", "کل محصول");

    companion object {
        fun getOrderedValues(): List<UnitType> {
            val allExceptOther = entries.filter { it != OTHER }
            return allExceptOther + OTHER
        }
    }
}
