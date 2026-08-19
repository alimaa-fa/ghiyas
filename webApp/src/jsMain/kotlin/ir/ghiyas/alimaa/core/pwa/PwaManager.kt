package ir.ghiyas.alimaa.core.pwa

import kotlinx.browser.window

object PwaManager {
    
    fun isStandalone(): Boolean {
        // استفاده از API های رسمی کاتلین/وب به جای تابع js
        val isStandaloneMedia = window.matchMedia("(display-mode: standalone)").matches
        val isSafariStandalone = window.navigator.asDynamic().standalone == true
        
        return isStandaloneMedia || isSafariStandalone
    }

    fun requestInstall() {
        // دسترسی امن به متغیرهای گلوبال با asDynamic
        val deferredPrompt = window.asDynamic().deferredInstallPrompt
        
        if (deferredPrompt != undefined && deferredPrompt != null) {
            // مرورگرهای کرومیوم (Chrome, Edge, Samsung)
            deferredPrompt.prompt()
            deferredPrompt.userChoice.then {
                window.asDynamic().deferredInstallPrompt = null
            }
        } else {
            // فایرفاکس، سافاری، یا داخل محیط پیام‌رسان‌ها
            window.alert(
                "برای نصب برنامه:\n\n" +
                "۱. در کروم، فایرفاکس یا اج: از منوی مرورگر گزینه «Install» یا «Add to Home screen» را انتخاب کنید.\n" +
                "۲. در آیفون (سافاری): دکمه Share را زده و «Add to Home Screen» را انتخاب کنید."
            )
        }
    }
}
