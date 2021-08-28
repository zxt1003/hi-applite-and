package com.hidollars.app.activity


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.myhexaville.smartimagepicker.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private val _https = "https://"
    private val _webViewUrl: String = _https + "web.hi.com"
    private val _webViewBalance: String = _https + "web.hi.com/#/pages/details/Details?name=HI"
    private val _webViewHome: String = _https + "web.hi.com/#/pages/home/Home"
    private val _webViewUrlWithoutHttps: String = "web.hi.com"
    private val _webViewLogin: String = "https://web.hi.com/app"
    private val _webViewKyc: String = "https://web.hi.com/static/js/pages-kyc-kyc"
    private var _lastUrl: String = ""
    private var isReloadHome: Boolean = false
    private var _aswFilePath: ValueCallback<Array<Uri>>? = null
    var imagePicker: ImagePicker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.hidollars.app.R.layout.activity_main)
        setUpWebView()
    }

    private fun setUpWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
            ): Boolean {
                _aswFilePath = filePathCallback
                createImagePicker()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                Log.e("permissions", request.resources.toString())
                request.grant(request.resources)
            }
        }

        webView.webViewClient =
                object : WebViewClient() {
                    override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                    ): WebResourceResponse? {
                        if (request != null && request.url != null
                                && request.url.toString().startsWith(_webViewKyc)
                        ) {
                            checkPermissions()
                        } else if (request != null && request.url != null
                                && request.url.toString().startsWith(_webViewBalance)) {
                            isReloadHome = true
                        } else if (request != null && request.url != null
                                && request.url.toString().startsWith(_webViewHome) && isReloadHome) {
                            isReloadHome = false
                            webView.reload()
                        }
                        Log.e("URL-----", request!!.url.toString())
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return if (!url.startsWith(_webViewUrl) && !url.startsWith(_webViewUrlWithoutHttps)) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            true
                        } else {
                            if (url.startsWith(_webViewLogin)) {
                                webView.loadUrl(url)
                            }
                            _lastUrl = url
                            Log.e("URL", url)
                            false
                        }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    }
                }

        webView.loadUrl(_webViewUrl)
    }

    private fun createImagePicker() {
        imagePicker = ImagePicker(
                this@MainActivity, null
        ) {
            val results: Array<Uri>?
            results = arrayOf(it)
            _aswFilePath!!.onReceiveValue(results)
            _aswFilePath = null
        }
        imagePicker!!.choosePicture(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView.canGoBack() && _lastUrl != _webViewLogin) {
                        webView.goBack()
                        val url = webView.url
                        Log.e("url", url.toString())
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            _aswFilePath?.onReceiveValue(null)
        } else {
            imagePicker?.handleActivityResult(resultCode, requestCode, data)
        }
    }

    private val _mandatoryPermissions = mutableListOf<String>()

    private fun checkPermissions(isReload: Boolean = false) {
        var isAllPermissionGranted = true

        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            isAllPermissionGranted = false
            _mandatoryPermissions.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
        ) {
            isAllPermissionGranted = false
            _mandatoryPermissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            isAllPermissionGranted = false
            _mandatoryPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            isAllPermissionGranted = false
            _mandatoryPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (isReload && isAllPermissionGranted) {
            webView.reload()
        } else {
            _mandatoryPermissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
            requestPermissions(_mandatoryPermissions.toTypedArray(), 1010)
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1010) {
            checkPermissions(true)
        }
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()

    }
}