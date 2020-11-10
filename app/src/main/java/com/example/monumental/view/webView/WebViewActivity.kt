package com.example.monumental.view.webView

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.monumental.R
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : AppCompatActivity() {

    companion object {
        const val URL = "URL"
        const val NAME = "NAME"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bundle = intent.extras

        if (bundle != null) { // Check for title in bundle
            if (bundle.getString(NAME) != null) {
                supportActionBar?.title = bundle.getString(NAME)!!.replace("+", " ")
            }
            if (bundle.getString(URL) != null) { // Check for url in bundle
                webview.settings.javaScriptEnabled = true
                webview.loadUrl(Uri.parse(bundle.getString(URL)).toString()) // Load webPage url
                supportActionBar?.subtitle = Uri.parse(bundle.getString(URL)).toString()
            }
        } else {
            this.finish() // If no bundle (url) close activity
            doTransition()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish() // Close activity on back press
        doTransition()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        this.finish() // Close activity on back press
        doTransition()
        super.onBackPressed()
    }

    private fun doTransition() {
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
    }
}