package ir.ghiyas.alimaa.core.pwa

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.events.Event

/**
 * مدیریت یکپارچه چرخه حیات PWA، تشخیص به‌روزرسانی نسخه و SDK پیام‌رسان ایتا.
 */
object PwaManager {

    private val _isInstallable = MutableStateFlow(false)
    val isInstallable: StateFlow<Boolean> = _isInstallable.asStateFlow()

    private val _hasUpdateAvailable = MutableStateFlow(false)
    val hasUpdateAvailable: StateFlow<Boolean> = _hasUpdateAvailable.asStateFlow()

    private var deferredPrompt: dynamic = null
    private var waitingWorker: dynamic = null

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
            // در مرورگرهای استاندارد بدون خطا رد می‌شود
        }
    }

    private fun registerServiceWorker() {
        val nav = window.navigator.asDynamic()
        if (nav.serviceWorker != null) {
            nav.serviceWorker.register("./sw.js").then({ reg: dynamic ->
                // بررسی نسخه جدید هنگام لود
                reg.addEventListener("updatefound", {
                    val newWorker = reg.installing
                    if (newWorker != null) {
                        newWorker.addEventListener("statechange", {
                            if (newWorker.state == "installed" && nav.serviceWorker.controller != null) {
                                waitingWorker = newWorker
                                _hasUpdateAvailable.value = true
                            }
                        })
                    }
                })

                if (reg.waiting != null && nav.serviceWorker.controller != null) {
                    waitingWorker = reg.waiting
                    _hasUpdateAvailable.value = true
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

    private fun requestPersistentStorage() {
        try {
            val storage = window.navigator.asDynamic().storage
            if (storage != null && storage.persist != null) {
                storage.persist()
            }
        } catch (_: Throwable) {
            // مرورگرهایی که پشتیبانی نمی‌کنند
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

    /**
     * فعال‌سازی و اعمال نسخه جدید نرم‌افزار به درخواست کاربر
     */
    fun applyUpdate() {
        if (waitingWorker != null) {
            waitingWorker.postMessage(kotlin.js.json("type" to "SKIP_WAITING"))
        } else {
            window.location.reload()
        }
    }
}
