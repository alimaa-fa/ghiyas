# Ghiyas (قیاس) - Agricultural & Financial Calculator

پروژه «قیاس» یک مینی‌اپلیکیشن (PWA) فوق‌سبک و آفلاین (Offline-First) است که برای محاسبات پیچیده مالی، تسهیم سهام کشاورزی و مدیریت زمان‌بندی آبیاری طراحی شده است. این اپلیکیشن با هدف اجرای سریع و بدون لگ در بستر مرورگرها و WebView پیام‌رسان‌های ایرانی (مانند ایتا و بله) توسعه یافته است.

## 🏗 معماری و تکنولوژی‌ها (Tech Stack)
- **فریم‌ورک:** Kotlin Multiplatform (KMP)
- **رابط کاربری وب:** Compose HTML (Pure DOM) - **بدون استفاده از Canvas و Compose Wasm برای حفظ سبکی برنامه.**
- **هسته محاسباتی:** استفاده از کتابخانه‌های BigNum (مانند `com.ionspin.kotlin:bignum`) جهت جلوگیری از خطاهای اعشاری (Floating-point errors) با دقت ۳ رقم اعشار (RoundingMode.HALF_UP).
- **ذخیره‌سازی:** IndexedDB برای محیط وب (تحت مکانیزم PWA و Service Workers).
- **الگوی طراحی:** ماژولار مبتنی بر Clean Architecture و Strategy Pattern برای پروفایل‌های محاسباتی.

## 🚀 نحوه اجرای پروژه (وب)
از آنجا که تارگت ما مبتنی بر JS و DOM است، برای اجرای نسخه توسعه (Development) از دستور زیر استفاده کنید:

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
