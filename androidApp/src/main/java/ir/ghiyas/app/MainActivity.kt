package ir.ghiyas.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView = WebView(this)
        setContentView(webView)

        // راه‌اندازی AssetLoader: رهگیری درخواست‌ها و خواندن آفلاین فایل‌ها از پوشه assets
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
        
        webView.webChromeClient = WebChromeClient()

        // فعال‌سازی موتورهای دیتابیس برای IndexedDB و اجرای فایل‌های JS قیاس
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        // بارگذاری فایل index.html از دامنه مجازی اختصاصی WebViewAssetLoader
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }
}
