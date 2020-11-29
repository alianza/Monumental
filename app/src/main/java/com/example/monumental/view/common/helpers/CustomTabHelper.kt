package com.example.monumental.view.common.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import com.example.monumental.R
import com.example.monumental.view.webView.WebViewActivity

class CustomTabHelper {

    companion object {
        var sPackageNameToUse: String? = null
        const val STABLE_PACKAGE = "com.android.chrome"
        const val BETA_PACKAGE = "com.chrome.beta"
        const val DEV_PACKAGE = "com.chrome.dev"
        const val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    }

    fun startIntent(result: String, context: Context) {
        val packageName =
            getPackageNameToUse(context, context.getString(R.string.info_url, result))

        // check if chrome is available
        if (packageName == null) {
            // If chrome not available open in web view
            val intentOpenUri = Intent(context, WebViewActivity::class.java)
            intentOpenUri.putExtra(WebViewActivity.URL, context.getString(R.string.info_url, result))
            intentOpenUri.putExtra(WebViewActivity.NAME, result)
            context.startActivity(intentOpenUri)
            (context as Activity).overridePendingTransition(
                R.anim.anim_slide_in_left,
                R.anim.anim_slide_out_left)
        } else {
            // Open chrome custom tab
            val customTabsIntent = build(context)
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(context, Uri.parse(context.getString(R.string.info_url, result)))
        }
    }

    private fun build(context: Context): CustomTabsIntent {
        val builder = CustomTabsIntent.Builder()

        // modify toolbar color
        builder.setToolbarColor(context.getColor(R.color.colorPrimary))

        // add share button to overflow menu
        builder.addDefaultShareMenuItem()

        // modify back button icon
        builder.setCloseButtonIcon(
            BitmapFactory.decodeResource(context.resources, R.drawable.baseline_arrow_back_black_24dp))

        // show website title
        builder.setShowTitle(true)

        // animation for enter and exit of tab
        builder.setStartAnimations(context, R.anim.anim_slide_in_left, R.anim.anim_slide_out_left)
        builder.setExitAnimations(context, R.anim.anim_slide_in_right, R.anim.anim_slide_out_right)

        return builder.build()
    }

    private fun getPackageNameToUse(context: Context, url: String): String? {
        sPackageNameToUse?.let {
            return it
        }

        val pm = context.packageManager

        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
        var defaultViewHandlerPackageName: String? = null

        defaultViewHandlerInfo?.let {
            defaultViewHandlerPackageName = it.activityInfo.packageName
        }

        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = ArrayList<String>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)

            pm.resolveService(serviceIntent, 0)?.let {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        when {
            packagesSupportingCustomTabs.isEmpty() -> sPackageNameToUse = null
            packagesSupportingCustomTabs.size == 1 -> sPackageNameToUse =
                packagesSupportingCustomTabs[0]
            !TextUtils.isEmpty(defaultViewHandlerPackageName)
                    && !hasSpecializedHandlerIntents(context, activityIntent)
                    && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName) ->
                sPackageNameToUse = defaultViewHandlerPackageName
            packagesSupportingCustomTabs.contains(STABLE_PACKAGE) -> sPackageNameToUse = STABLE_PACKAGE
            packagesSupportingCustomTabs.contains(BETA_PACKAGE) -> sPackageNameToUse = BETA_PACKAGE
            packagesSupportingCustomTabs.contains(DEV_PACKAGE) -> sPackageNameToUse = DEV_PACKAGE
            packagesSupportingCustomTabs.contains(LOCAL_PACKAGE) -> sPackageNameToUse = LOCAL_PACKAGE
        }
        return sPackageNameToUse
    }

    private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
        try {
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER)
            if (handlers.size == 0) {
                return false
            }
            for (resolveInfo in handlers) {
                val filter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        } catch (e: RuntimeException) {
        }
        return false
    }
}