package com.howiehye.tinnytoolpro

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONObject


class MainActivity : AppCompatActivity(){
    private var cookieStr = ""
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val urlJd = getString(R.string.url)
        val urlApi = getString(R.string.urlAPI)
        val myWebView: WebView = findViewById(R.id.webview)
        val btn_copy: Button = findViewById(R.id.btn_copy)
        val btn_upload: Button = findViewById(R.id.btn_upload)
        val btn_clear: Button = findViewById(R.id.btn_clear)

        val viewUpload = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_upload, null)
        val remarkEdit: EditText = viewUpload.findViewById(R.id.remark)
        val btnLogin: Button = viewUpload.findViewById(R.id.btn_dialog_input_login)

        val builder = AlertDialog.Builder(this)
            .setTitle("请输入备注信息")
            .setView(viewUpload)
            .create()

        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.loadWithOverviewMode = true

        myWebView.loadUrl(urlJd)
        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        Toast.makeText(this@MainActivity, "请登录后再进行操作！！！", Toast.LENGTH_SHORT).show()

        btn_copy.setOnClickListener {
            val cookieManager: CookieManager = CookieManager.getInstance()
            this.cookieStr = cookieManager.getCookie(urlJd)
            val jdCookie = getJdCookie(this.cookieStr)[0]
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("simple text", jdCookie)
            clipboardManager.setPrimaryClip(clipData)
            println(jdCookie)
            // Toast 提示
            Toast.makeText(this@MainActivity,"已复制到剪贴板",Toast.LENGTH_SHORT).show()
        }



        btn_upload.setOnClickListener {
            val cookieManager: CookieManager = CookieManager.getInstance()
            this.cookieStr = cookieManager.getCookie(urlJd)
            val jdCookie = getJdCookie(this.cookieStr)
            val queue = newRequestQueue(this@MainActivity)
            val json = JSONObject()

            json.put("pt_key", jdCookie[1])
            json.put("pt_pin", jdCookie[2])
            println(json)

            builder.show()

            btnLogin.setOnClickListener {
                var remark = remarkEdit.text.toString()
                json.put("remark", remark)
                val jsonObject = object : JsonObjectRequest(
                    Method.POST,
                    urlApi,
                    json,
                    Response.Listener { response ->
                        val result = response.optString("message")
                        Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener { error ->
                        println(error)
                        val result = "Upload Error"
                        Toast.makeText(this@MainActivity, result, Toast.LENGTH_SHORT).show()
                    },
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-Type"] = "application/json; charset=utf-8"
                        headers["Connection"] = "keep-alive"
                        headers["User-Agent"] = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36"
                        return headers
                    }
                }
                queue.add(jsonObject)
                builder.dismiss()
            }

        }
        btn_clear.setOnClickListener {
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookies(null)
            WebView(applicationContext).clearCache(true)
            WebView(applicationContext).clearHistory()
            myWebView.loadUrl(urlJd)

        }
    }
    private fun getJdCookie(cookieStr: String): Array<String> {
        val cookieTmp = cookieStr.split(";")
        var jdPin: String = ""
        var jdKey: String = ""
        for (ar1 in cookieTmp) {
            if (ar1.contains("pt_pin")) {
                jdPin = ar1.split("=")[1]
            }
            if (ar1.contains("pt_key")) {
                jdKey = ar1.split("=")[1]
            }
        }
        return arrayOf("pt_key=$jdKey;pt_pin=$jdPin;", jdKey, jdPin)
    }
}



