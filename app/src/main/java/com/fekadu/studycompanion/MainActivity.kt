package com.fekadu.studycompanion

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("sc", Context.MODE_PRIVATE) }
    private var leftAt = 0L
    private var prompting = false
    private val auth = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    private fun lockOn() = prefs.getBoolean("lock", false)

    private fun canAuth() =
        BiometricManager.from(this).canAuthenticate(auth) == BiometricManager.BIOMETRIC_SUCCESS

    inner class Bridge {
        @JavascriptInterface fun getLock(): Boolean = lockOn()
        @JavascriptInterface fun canLock(): Boolean = canAuth()
        @JavascriptInterface fun setLock(on: Boolean) {
            runOnUiThread {
                prefs.edit().putBoolean("lock", on && canAuth()).apply()
                applySecure()
            }
        }
    }

    // Hides app content in Recent Apps and blocks screenshots while the lock is on
    private fun applySecure() {
        if (lockOn()) window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySecure()
        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient() // needed for alert() / confirm()
        webView.addJavascriptInterface(Bridge(), "Native")

        if (savedInstanceState != null) webView.restoreState(savedInstanceState)
        else webView.loadUrl("file:///android_asset/index.html")

        if (lockOn()) {
            webView.visibility = View.INVISIBLE
            authenticate()
        }
    }

    override fun onStop() {
        super.onStop()
        leftAt = System.currentTimeMillis()
        if (lockOn()) webView.visibility = View.INVISIBLE
    }

    override fun onStart() {
        super.onStart()
        if (leftAt == 0L || !lockOn()) {
            webView.visibility = View.VISIBLE
            return
        }
        if (System.currentTimeMillis() - leftAt > 30_000) authenticate()
        else webView.visibility = View.VISIBLE
    }

    private fun authenticate() {
        if (prompting) return
        if (!canAuth()) { // nothing set up on the phone: do not lock the user out
            webView.visibility = View.VISIBLE
            return
        }
        prompting = true
        val prompt = BiometricPrompt(
            this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    prompting = false
                    webView.visibility = View.VISIBLE
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    prompting = false
                    finish()
                }
            })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Study Companion")
                .setAllowedAuthenticators(auth)
                .build()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
