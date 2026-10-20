package com.fekadu.studycompanion

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: View
    private lateinit var prefs: SharedPreferences
    private var authenticated = false
    private var filePathCallback: android.webkit.ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            filePathCallback = null
            if (callback == null) return@registerForActivityResult
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data?.data != null) {
                callback.onReceiveValue(arrayOf(data.data!!))
            } else {
                callback.onReceiveValue(null)
            }
        }

    private val authLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                authenticated = true
            } else {
                finish()
            }
        }

    private val importLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            try {
                val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                val escaped = JSONObject.quote(text)
                webView.post {
                    webView.evaluateJavascript("window.onBackupImported && window.onBackupImported($escaped)", null)
                }
            } catch (e: Exception) { /* ignore unreadable file */ }
        }

    inner class NativeBridge {
        @JavascriptInterface
        fun canLock(): Boolean {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            return km.isDeviceSecure
        }

        @JavascriptInterface
        fun getLock(): Boolean = prefs.getBoolean("lock_enabled", false)

        @JavascriptInterface
        fun setLock(enabled: Boolean) {
            prefs.edit().putBoolean("lock_enabled", enabled).apply()
            if (!enabled) authenticated = true
        }

        @JavascriptInterface
        fun lockNow() {
            authenticated = false
            runOnUiThread { moveTaskToBack(true) }
        }

        @JavascriptInterface
        fun exportBackup(json: String): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            return try {
                val name = "StudyCompanion_backup_" + System.currentTimeMillis() + ".json"
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    true
                } else false
            } catch (e: Exception) { false }
        }

        @JavascriptInterface
        fun importBackup() {
            runOnUiThread { importLauncher.launch(arrayOf("application/json", "*/*")) }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("sc_prefs", MODE_PRIVATE)

        val root = FrameLayout(this)
        webView = WebView(this)
        progressBar = ProgressBar(this).apply { isIndeterminate = true }
        errorView = buildErrorView()
        errorView.visibility = View.GONE

        root.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        root.addView(progressBar, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        root.addView(errorView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(root)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        webView.addJavascriptInterface(NativeBridge(), "Native")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                if (url.scheme == "file") {
                    return false
                }
                openExternally(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String?) {
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    progressBar.visibility = View.GONE
                    webView.visibility = View.GONE
                    errorView.visibility = View.VISIBLE
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean {
                        openExternally(request.url)
                        return true
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                transport.webView = newWebView
                resultMsg.sendToTarget()
                return true
            }

            override fun onShowFileChooser(
                webView: WebView,
                callback: android.webkit.ValueCallback<Array<Uri>>,
                params: FileChooserParams
            ): Boolean {
                filePathCallback = callback
                val intent = params.createIntent()
                return try {
                    fileChooserLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    filePathCallback = null
                    false
                }
            }
        }

        webView.setDownloadListener { url, _, _, _, _ -> openExternally(Uri.parse(url)) }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(STUDY_APP_URL)
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs.getBoolean("lock_enabled", false) && !authenticated) {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isDeviceSecure) {
                val intent = km.createConfirmDeviceCredentialIntent(getString(R.string.app_name), null)
                if (intent != null) {
                    authLauncher.launch(intent)
                } else {
                    authenticated = true
                }
            } else {
                authenticated = true
            }
        }
    }

    private fun openExternally(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) { /* no app can handle it */ }
    }

    private fun buildErrorView(): View {
        val container = FrameLayout(this)
        val text = TextView(this).apply {
            text = "መጫን አልተቻለም። ኢንተርኔትህን አረጋግጥና ንካ እንደገና ለመሞከር።"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        container.addView(text, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        container.setOnClickListener {
            errorView.visibility = View.GONE
            webView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            webView.reload()
        }
        return container
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    companion object {
        private const val STUDY_APP_URL = "file:///android_asset/index.html"
    }
}
