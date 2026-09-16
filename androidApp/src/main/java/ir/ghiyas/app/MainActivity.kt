package ir.ghiyas.app

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupJson: String? = null
    private lateinit var webView: WebView
    
    // متغیر برای مدیریت دوبار فشردن کلید بازگشت
    private var doubleBackToExitPressedOnce = false

    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            filePathCallback?.onReceiveValue(arrayOf(uri))
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let { destinationUri ->
            pendingBackupJson?.let { json ->
                try {
                    contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        pendingBackupJson = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this)
        setContentView(webView)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                fileChooserLauncher.launch("application/json")
                return true
            }
        }

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        
        // اصلاحیه ۱: قفل کردن مقیاس متن روی ۱۰۰٪ برای جلوگیری از بزرگ شدن فرم‌ها و تب‌ها در گوشی‌های مختلف
        settings.textZoom = 100
        // اصلاحیه ۲: مجبور کردن وب‌ویو به استفاده از ابعاد تعیین شده در تگ viewport
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        // اصلاحیه ۳: غیرفعال کردن زوم دستی
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        // مدیریت کلید بازگشت (Back Button)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // اگر وب‌ویو دارای تاریخچه باشد (مثلاً داخل فرم‌ها باشیم)، فقط یک مرحله به عقب برمی‌گردد
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    // اگر در صفحه اصلی باشیم، منطق دوبار کلیک اجرا می‌شود
                    if (doubleBackToExitPressedOnce) {
                        finish() // خروج از برنامه
                        return
                    }

                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@MainActivity, "برای خروج یک‌بار دیگر کلید بازگشت را بزنید", Toast.LENGTH_SHORT).show()

                    // ریست کردن وضعیت پس از ۲ ثانیه
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })

        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun saveBackup(jsonContent: String, filename: String) {
            pendingBackupJson = jsonContent
            runOnUiThread {
                createDocumentLauncher.launch(filename)
            }
        }
    }
}
