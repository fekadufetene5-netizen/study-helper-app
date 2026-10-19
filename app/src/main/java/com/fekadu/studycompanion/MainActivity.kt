
package com.fekadu.studycompanion

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val prefs by lazy {
        getSharedPreferences("sc", Context.MODE_PRIVATE)
    }

    private var leftAt = 0L
    private var prompting = false

    private val auth =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    private fun lockOn() = prefs.getBoolean("lock", false)

    private fun canAuth(): Boolean =
        BiometricManager.from(this).canAuthenticate(auth) ==
            BiometricManager.BIOMETRIC_SUCCESS

    inner class Bridge {

        @JavascriptInterface
        fun getLock(): Boolean = lockOn()

        @JavascriptInterface
        fun canLock(): Boolean = canAuth()

        @JavascriptInterface
        fun canNotify(): Boolean =
            Reminders.canNotify(this@MainActivity)

        @JavascriptInterface
        fun setReminders(json: String) {
            prefs.edit().putString("rem", json).apply()
            Reminders.schedule(this@MainActivity)
        }

        @JavascriptInterface
        fun askNotify() {
            runOnUiThread {
                if (
                    Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        7
                    )
                }
            }
        }

        @JavascriptInterface
        fun batterySettings() {
            runOnUiThread {
                try {
                    startActivity(
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    )
                } catch (_: Exception) {
                    // Settings screen may not be available.
                }
            }
        }

        @JavascriptInterface
        fun lockNow() {
            runOnUiThread { finishAndRemoveTask() }
        }

        @JavascriptInterface
        fun setLock(on: Boolean) {
            runOnUiThread {
                prefs.edit()
                    .putBoolean("lock", on && canAuth())
                    .apply()
            }
        }
    }

    private fun secure(on: Boolean) {
        if (on && lockOn()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onPause() {
        super.onPause()
        secure(true)
    }

    override fun onResume() {
        super.onResume()
        secure(false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // IMPORTANT: Must match window.Native in index.html.
        webView.addJavascriptInterface(Bridge(), "Native")

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl("file:///android_asset/index.html")
        }

        if (lockOn()) {
            webView.visibility = View.INVISIBLE
            authenticate()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 7) {
            // Re-apply the saved reminders after the permission response.
            Reminders.schedule(this)
        }
    }

    override fun onStop() {
        super.onStop()
        leftAt = System.currentTimeMillis()

        if (::webView.isInitialized && lockOn()) {
            webView.visibility = View.INVISIBLE
        }
    }

    override fun onStart() {
        super.onStart()

        if (!::webView.isInitialized) return

        if (leftAt == 0L || !lockOn()) {
            webView.visibility = View.VISIBLE
            return
        }

        if (System.currentTimeMillis() - leftAt > 30_000) {
            authenticate()
        } else {
            webView.visibility = View.VISIBLE
        }
    }

    private fun authenticate() {
        if (prompting) return

        if (!canAuth()) {
            webView.visibility = View.VISIBLE
            return
        }

        prompting = true

        val prompt = BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    prompting = false
                    webView.visibility = View.VISIBLE
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    prompting = false
                    finish()
                }
            }
        )

        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Study Companion")
                .setAllowedAuthenticators(auth)
                .build()
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
