package ghiyas.alimaa.fa.core.pwa

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.events.Event

/**
 * وضعیت‌های مختلف فرآیند به‌روزرسانی PWA
 */
enum class UpdateState {
    NONE,                   // نسخه‌ای نیست
    DETECTED_DOWNLOADING,   // نسخه جدید پیدا شده و در حال دانلود در پس‌زمینه است
    DOWNLOADED_READY        // دانلود تمام شده و آماده اعمال (نصب) است
}

/**
 * مدیریت یکپارچه چرخه حیات PWA، تشخیص به‌روزرسانی نسخه و SDK پیام‌رسان ایتا.
 */
object PwaManager {

    private val _isInstallable = MutableStateFlow(false)
    val isInstallable: StateFlow<Boolean> = _isInstallable.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateState.NONE)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private var deferredPrompt: dynamic = null
    private var waitingWorker: dynamic = null
    private var installRequestedAutomatically = false

    fun initialize() {
        initEitaaWebApp()
        registerServiceWorker()
        requestPersistentStorage()
        listenForInstallPrompt()
        checkForUpdatesIfOnline()
    }

    private fun initEitaaWebApp() {
        try {
            val eitaa = window.asDynamic().Eitaa
            if (eitaa != null && eitaa.WebApp != null) {
                eitaa.WebApp.ready()
                eitaa.WebApp.expand()
            }
        } catch (_: Throwable) {
            // در مرورگرهای استاندارد بدون خطا رد می‌شود
        }
    }

    private fun registerServiceWorker() {
        val nav = window.navigator.asDynamic()
        if (nav.serviceWorker != null) {
            nav.serviceWorker.register("./sw.js").then({ reg: dynamic ->
                
                // رویداد updatefound به محض اینکه مرورگر می‌فهمد sw.js در سرور تغییر کرده شلیک می‌شود
                reg.addEventListener("updatefound", {
                    val newWorker = reg.installing
                    if (newWorker != null) {
                        _updateState.value = UpdateState.DETECTED_DOWNLOADING

                        newWorker.addEventListener("statechange", {
                            // وقتی دانلود فایل‌های کش جدید تمام شد
                            if (newWorker.state == "installed" && nav.serviceWorker.controller != null) {
                                waitingWorker = newWorker
                                _updateState.value = UpdateState.DOWNLOADED_READY
                                
                                // اگر کاربر در زمان دانلود روی "دانلود و نصب" کلیک کرده بود، الان نصب را تکمیل کن
                                if (installRequestedAutomatically) {
                                    applyUpdate()
                                }
                            }
                        })
                    }
                })

                // اگر از قبل آپدیتی دانلود شده و منتظر است
                if (reg.waiting != null && nav.serviceWorker.controller != null) {
                    waitingWorker = reg.waiting
                    _updateState.value = UpdateState.DOWNLOADED_READY
                }
            })

            // بازنشانی ایمن پس از تعویض کنترلر
            var refreshing = false
            nav.serviceWorker.addEventListener("controllerchange", {
                if (!refreshing) {
                    refreshing = true
                    window.location.reload()
                }
            })
        }
    }

    private fun checkForUpdatesIfOnline() {
        val nav = window.navigator.asDynamic()
        if (nav.onLine == true && nav.serviceWorker != null) {
            nav.serviceWorker.ready.then({ reg: dynamic ->
                try { reg.update() } catch (_: Throwable) {}
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
            // مرورگرها
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

    fun requestInstallWhenReady() {
        installRequestedAutomatically = true
    }

    /**
     * فعال‌سازی و اعمال فوری نسخه جدید
     */
    fun applyUpdate() {
        // ثبت یک پرچم در حافظه سشن مرورگر قبل از اینکه صفحه رفرش و بسته شود
        window.sessionStorage.setItem("PWA_UPDATE_SUCCESS", "true")
        
        if (waitingWorker != null) {
            waitingWorker.postMessage(kotlin.js.json("type" to "SKIP_WAITING"))
        } else {
            window.location.reload()
        }
    }
}
