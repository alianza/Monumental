package com.example.monumental.ui.webView

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

        if (bundle != null) {
            if (bundle.getString(NAME) != null) {
                supportActionBar?.title = bundle.getString(NAME)!!.replace("+", " ")
            }
            // Check for url in bundle
            if (bundle.getString(URL) != null) {
                // Load webpage url
                webview.settings.javaScriptEnabled = true
                webview.loadUrl(Uri.parse(bundle.getString(URL)).toString())

                supportActionBar?.subtitle = Uri.parse(bundle.getString(URL)).toString()
            }
        } else {
            // If no bundle (url) close activity
            this.finish()
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Close activity on back press
        this.finish()
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)
        return super.onOptionsItemSelected(item)
    }
}