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
    
    // متغیرهای جدید برای مدیریت حافظه و جلوگیری از سرریز شدن Binder Buffer در اندرویدهای قدیمی
    private var pendingFilename: String? = null
    private val backupBuffer = java.lang.StringBuilder()
    private var pendingBackupJson: String? = null
    
    private lateinit var webView: WebView
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
        // پاکسازی حافظه پس از ذخیره
        pendingBackupJson = null
        backupBuffer.setLength(0) 
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

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: android.webkit.WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                
                if (!url.startsWith("https://appassets.androidplatform.net/")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        if (url.contains("eitaa.com")) {
                            intent.setPackage("ir.eitaa.messenger")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(fallbackIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                    return true 
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
        // مرحله اول: آماده‌سازی حافظه برای دریافت تکه‌تکه فایل
        @JavascriptInterface
        fun initBackup(filename: String) {
            pendingFilename = filename
            backupBuffer.setLength(0)
            pendingBackupJson = null
        }

        // مرحله دوم: دریافت امن بسته‌های ۲۵۶ کیلوبایتی (جلوگیری از خطای ۰ بایتی)
        @JavascriptInterface
        fun appendBackupChunk(chunk: String) {
            backupBuffer.append(chunk)
        }

        // مرحله سوم: پایان ارسال و شروع فرآیند ذخیره‌سازی بومی
        @JavascriptInterface
        fun saveBackupFile() {
            pendingBackupJson = backupBuffer.toString()
            runOnUiThread {
                createDocumentLauncher.launch(pendingFilename)
            }
        }
    }
}
