package ir.ghiyas.app

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupJson: String? = null

    // لانچر بازیابی فایل: متصل به تگ <input type="file"> در کدهای PWA
    private val fileChooserLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            filePathCallback?.onReceiveValue(arrayOf(uri))
        } else {
            // بسیار مهم: اگر کاربر انصراف داد باید null برگردانیم تا وب‌ویو قفل نشود
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    // لانچر ذخیره فایل بومی: پشتیبانی تا اندروید 17 بدون نیاز به پرمیشن
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
        
        val webView = WebView(this)
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
            // اتصال سیستم آپلود فایل وب به فایل‌منیجر بومی اندروید
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

        // تزریق پل ارتباطی (JS Bridge) با نام AndroidBridge به وب‌ویو
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    // کلاسی که متدهای آن مستقیماً از داخل کاتلین/JS صدا زده می‌شوند
    inner class AndroidBridge {
        @JavascriptInterface
        fun saveBackup(jsonContent: String, filename: String) {
            pendingBackupJson = jsonContent
            // باز کردن رابط کاربری اندروید باید حتماً روی ترد اصلی (UI Thread) باشد
            runOnUiThread {
                createDocumentLauncher.launch(filename)
            }
        }
    }
}
