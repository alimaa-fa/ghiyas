package ir.ghiyas.alimaa.core.pwa

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.events.Event

/**
 * مدیریت چرخه حیات PWA، سرویس‌ورکر، اعتبارسنجی خودکار نسخه و SDK ایتا.
 */
object PwaManager {

    private val _isInstallable = MutableStateFlow(false)
    val isInstallable: StateFlow<Boolean> = _isInstallable.asStateFlow()

    private var deferredPrompt: dynamic = null

    fun initialize() {
        initEitaaWebApp()
        registerServiceWorker()
        requestPersistentStorage()
        listenForInstallPrompt()
    }

    private fun initEitaaWebApp() {
        try {
            val eitaa = window.asDynamic().Eitaa
            if (eitaa != null && eitaa.WebApp != null) {
                eitaa.WebApp.ready()
                eitaa.WebApp.expand()
            }
        } catch (_: Throwable) {
            // در مرورگرهای معمولی بدون خطا رد می‌شود
        }
    }

    private fun registerServiceWorker() {
        val nav = window.navigator.asDynamic()
        if (nav.serviceWorker != null) {
            nav.serviceWorker.register("./sw.js").then({ reg: dynamic ->
                // اجبار مرورگر به چک کردن نسخه جدید سرور در هر ورود
                reg.update()
            })

            // رفرش خودکار و نامحسوس صفحه به محض نصب و فعال‌سازی سرویس‌ورکر جدید
            var refreshing = false
            nav.serviceWorker.addEventListener("controllerchange", {
                if (!refreshing) {
                    refreshing = true
                    window.location.reload()
                }
            })
        }
    }

    private fun requestPersistentStorage() {
        try {
            val storage = window.navigator.asDynamic().storage
            if (storage != null && storage.persist != null) {
                storage.persist()
            }
        } catch (_: Throwable) {
            // در صورت عدم پشتیبانی مرورگر نادیده گرفته می‌شود
        }
    }

    private fun listenForInstallPrompt() {
        window.addEventListener("beforeinstallprompt", { event: Event ->
            event.preventDefault()
            deferredPrompt = event
            _isInstallable.value = true
        })

        window.addEventListener("appinstalled", {
            _isInstallable.value = false
            deferredPrompt = null
        })
    }

    fun promptInstall() {
        if (deferredPrompt != null) {
            deferredPrompt.prompt()
            deferredPrompt = null
            _isInstallable.value = false
        }
    }
}
