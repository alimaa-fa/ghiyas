package ir.ghiyas.alimaa.core.pwa

import kotlinx.browser.window

object PwaManager {
    // تشخیص اینکه آیا برنامه الان روی گوشی نصب است یا داخل مرورگر باز شده؟
    fun isStandalone(): Boolean {
        val isStandaloneMedia = js("window.matchMedia('(display-mode: standalone)').matches") as Boolean
        val isSafariStandalone = js("window.navigator.standalone === true") as Boolean
        return isStandaloneMedia || isSafariStandalone
    }

    // فراخوانی پنجره بومی نصب روی گوشی
    fun requestInstall() {
        js("""
            if (window.deferredInstallPrompt) {
                window.deferredInstallPrompt.prompt();
                window.deferredInstallPrompt.userChoice.then((choiceResult) => {
                    if (choiceResult.outcome === 'accepted') {
                        console.log('کاربر اپلیکیشن را نصب کرد');
                    }
                    window.deferredInstallPrompt = null;
                });
            } else {
                // این پیام مخصوص کاربرانی است که در داخل ایتا یا تلگرام دکمه نصب را می‌زنند
                alert("برای نصب برنامه، ابتدا از منوی سه نقطه پیام‌رسان گزینه «باز کردن در مرورگر / Open in Browser» را بزنید، سپس در کروم گزینه Install را انتخاب کنید.");
            }
        """)
    }
}
