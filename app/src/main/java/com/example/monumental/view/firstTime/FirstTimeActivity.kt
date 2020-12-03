@file:Suppress("DEPRECATION")

package com.example.monumental.view.firstTime

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.monumental.R
import kotlinx.android.synthetic.main.activity_first_time.*


class FirstTimeActivity : AppCompatActivity() {
    private lateinit var firstTimeAdapter: FirstTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time)
        supportActionBar?.hide()

        initViews()
        setListeners()
    }

    private fun initViews() {
        firstTimeAdapter = FirstTimeAdapter(this)

        vpFirstTime.adapter = firstTimeAdapter

        pgProgress.max = 100
    }

    private fun setListeners() {
        vpFirstTime.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                println("$position Page!")
                println("${firstTimeAdapter.itemCount} itemcount adap!")
                println((position.toFloat().div(firstTimeAdapter.itemCount.minus(1f))).times(100).toString() + " progress")

                val progressPercentage = position.toFloat().div(firstTimeAdapter.itemCount.minus(1f)).times(100).toInt()

                ObjectAnimator.ofInt(pgProgress, "progress", progressPercentage)
                    .setDuration(300)
                    .start()

                println(pgProgress.progress.toString() + " Progress!")

                when (position) {
                    0 -> {
                    }
                    1 -> {
                        // you are on the second page
                    }
                    2 -> {
                        // you are on the third page
                    }
                }
                super.onPageSelected(position)
            }
        })
    }

    fun goToNextPage() {
        vpFirstTime.currentItem = vpFirstTime.currentItem + 1
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putExtra("result", "KAK!!!!")
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onDestroy() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val edit = prefs.edit()
        edit.putBoolean(getString(R.string.pref_previously_started), true)
        edit.apply()
        super.onDestroy()
    }
}