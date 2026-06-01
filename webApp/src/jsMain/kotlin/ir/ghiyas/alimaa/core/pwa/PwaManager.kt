package ir.ghiyas.alimaa.core.pwa

object PwaManager {
    fun isStandalone(): Boolean {
        val isStandaloneMedia = js("window.matchMedia('(display-mode: standalone)').matches") as Boolean
        val isSafariStandalone = js("window.navigator.standalone === true") as Boolean
        return isStandaloneMedia || isSafariStandalone
    }

    fun requestInstall() {
        js("""
            if (window.deferredInstallPrompt) {
                // مرورگرهای کرومیوم (Chrome, Edge, Samsung)
                window.deferredInstallPrompt.prompt();
                window.deferredInstallPrompt.userChoice.then((choiceResult) => {
                    window.deferredInstallPrompt = null;
                });
            } else {
                // فایرفاکس، سافاری، یا داخل محیط پیام‌رسان‌ها
                alert("برای نصب برنامه:\n\n۱. در کروم، فایرفاکس یا اج: از منوی مرورگر گزینه «Install» یا «Add to Home screen» را انتخاب کنید.\n۲. در آیفون (سافاری): دکمه Share را زده و «Add to Home Screen» را انتخاب کنید.");
            }
        """)
    }
}
