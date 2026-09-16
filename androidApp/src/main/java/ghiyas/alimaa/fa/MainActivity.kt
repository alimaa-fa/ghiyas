package ghiyas.alimaa.fa

import android.annotation.SuppressLint
import android.content.Intent
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

            // اضافه شدن متد مهم برای رهگیری لینک‌های خارجی (مثل ایتا) و جلوگیری از خطای Scheme
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                
                // اگر لینک مربوط به فایل‌های داخلی اپلیکیشن نیست، آن را بیرون از وب‌ویو باز کن
                if (!url.startsWith("https://appassets.androidplatform.net/")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        // اگر لینک مربوط به سایت ایتا بود، پکیج اختصاصی ایتا را برای اجرای مستقیم تنظیم می‌کنیم
                        if (url.contains("eitaa.com")) {
                            intent.setPackage("ir.eitaa.messenger")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        // در صورتی که اپلیکیشن ایتا نصب نباشد، لینک را به طور عادی در مرورگر باز می‌کنیم
                        try {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(fallbackIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                    return true // به وب‌ویو اعلام می‌کنیم که لینک توسط ما مدیریت شد و نیازی به لود کردن ندارد
                }
                return false
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
        
        settings.textZoom = 100
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false

        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                        return
                    }

                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@MainActivity, "برای خروج یک‌بار دیگر کلید بازگشت را بزنید", Toast.LENGTH_SHORT).show()

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
