# قوانین حفظ کلاس‌ها و متدهای رابط کاربری وب‌ویو و جاوااسکریپت برای پروژه قیاس
-keepattributes JavascriptInterface
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# جلوگیری از حذف یا تغییر نام اینترفیس‌های جاوااسکریپت در وب‌ویو
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# حفظ کدهای ماژول اندرویدایکس وب‌کیت جهت اجرای پایدار لایه نمایش وب
-keep class androidx.webkit.** { *; }

# جلوگیری از هشدارهای کامپایلر کاتلین در فاز کوچک‌سازی R8
-dontwarn kotlin.**
-dontwarn javax.annotation.**
