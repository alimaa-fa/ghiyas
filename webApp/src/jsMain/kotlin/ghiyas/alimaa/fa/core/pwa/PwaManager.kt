package ghiyas.alimaa.fa.core.pwa

import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.w3c.dom.events.Event

enum class UpdateState {
    NONE,
    DETECTED_DOWNLOADING,
    DOWNLOADED_READY
}

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
        } catch (_: Throwable) {}
    }

    private fun registerServiceWorker() {
        val nav = window.navigator.asDynamic()
        if (nav.serviceWorker != null) {
            nav.serviceWorker.register("./sw.js").then({ reg: dynamic ->
                reg.addEventListener("updatefound", {
                    val newWorker = reg.installing
                    if (newWorker != null) {
                        _updateState.value = UpdateState.DETECTED_DOWNLOADING
                        newWorker.addEventListener("statechange", {
                            if (newWorker.state == "installed") {
                                waitingWorker = newWorker
                                _updateState.value = UpdateState.DOWNLOADED_READY
                                if (installRequestedAutomatically) {
                                    applyUpdate()
                                }
                            } else if (newWorker.state == "redundant") {
                                _updateState.value = UpdateState.NONE
                                installRequestedAutomatically = false
                            }
                        })
                    }
                })

                if (reg.waiting != null) {
                    waitingWorker = reg.waiting
                    _updateState.value = UpdateState.DOWNLOADED_READY
                }
            })

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
        } catch (_: Throwable) {}
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

    fun applyUpdate() {
        window.sessionStorage.setItem("PWA_UPDATE_SUCCESS", "true")
        if (waitingWorker != null) {
            waitingWorker.postMessage(kotlin.js.json("type" to "SKIP_WAITING"))
        } else {
            window.location.reload()
        }
    }

    /**
     * تولید آدرس امن برای قرارگیری مستقیم در href تگ <a>
     * این کار Custom Tab کروم را دور می‌زند و جلوی سیاهی صفحه و قفل شدن لمس را می‌گیرد.
     */
    fun getSafeUrl(url: String): String {
        val isAndroid = window.navigator.userAgent.contains("Android", ignoreCase = true)
        if (!isAndroid) return url

        return try {
            if (url.contains("eitaa.com")) {
                val path = url.substringAfter("eitaa.com/")
                "intent://eitaa.com/$path#Intent;scheme=https;package=ir.eitaa.messenger;end"
            } else if (url.contains("cafebazaar.ir")) {
                val id = url.substringAfter("id=").substringBefore("&")
                "intent://details?id=$id#Intent;scheme=bazaar;package=com.farsitel.bazaar;end"
            } else {
                url
            }
        } catch (e: Throwable) {
            url
        }
    }

    /**
     * هندل کردن رویداد کلیک روی لینک‌ها با رعایت SRP.
     * اگر داخل ایتا باشیم، رفتار پیش‌فرض مرورگر متوقف شده و SDK وارد عمل می‌شود.
     * اگر در کروم باشیم، هیچ کاری نمی‌کند و اجازه می‌دهد سیستم‌عامل به صورت نیتیو Intent تگ A را باز کند.
     */
    fun handleLinkClick(event: Event, originalUrl: String) {
        try {
            val eitaa = window.asDynamic().Eitaa
            if (eitaa != null && eitaa.WebApp != null && eitaa.WebApp.openLink != undefined) {
                // محیط ایتا: جلوگیری از ناوبری مرورگر و استفاده از ابزار بومی
                event.preventDefault()
                eitaa.WebApp.openLink(originalUrl)
            }
        } catch (e: Throwable) {
            // در مرورگر عادی، خطا را نادیده می‌گیریم تا رفتار نیتیو تگ A ادامه یابد
        }
    }
}
