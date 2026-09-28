# ========================================================
# قوانین حفظ کلاس‌ها و متدهای رابط کاربری (پروژه قیاس)
# ========================================================
-keepattributes JavascriptInterface
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# جلوگیری از حذف یا تغییر نام متدهای جاوااسکریپت در وب‌ویو
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# حفظ صریح کلاس پل ارتباطی (AndroidBridge) درون اکتیویتی اصلی
-keep class ghiyas.alimaa.fa.MainActivity$AndroidBridge { *; }

# حفظ کدهای ماژول اندرویدایکس وب‌کیت جهت اجرای پایدار لایه نمایش وب
-keep class androidx.webkit.** { *; }

# ========================================================
# قوانین حیاتی برای Kotlinx Serialization (جهت حفظ ساختار JSON بکاپ)
# ========================================================
# جلوگیری از تغییر نام کلیدهای دیتابیس و فایل‌های پشتیبان هنگام کوچک‌سازی کد
-keep @kotlinx.serialization.Serializable class * { *; }

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========================================================
# جلوگیری از هشدارهای بی‌مورد کامپایلر در فاز کوچک‌سازی R8
# ========================================================
-dontwarn kotlin.**
-dontwarn javax.annotation.**
-dontwarn kotlinx.serialization.**
