@file:Suppress("DEPRECATION")

package com.example.monumental.view.firstTime

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.monumental.R
import kotlinx.android.synthetic.main.activity_first_time.*

class FirstTimeActivity : AppCompatActivity() {

    private lateinit var firstTimeAdapter: FirstTimeAdapter

    private var animationsHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time)
        supportActionBar?.hide()

        initViews()
        setListeners()
        doAnimations()
    }

    private fun initViews() {
        firstTimeAdapter = FirstTimeAdapter(this)

        vpFirstTime.adapter = firstTimeAdapter

        pgProgress.max = 100
    }

    private fun doAnimations() {
        animationsHandler.postDelayed({
            tvSwipe?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            tvSwipe?.animate()?.translationX(-148F)?.setDuration(1000)?.start()
        }, 5000)
    }

    private fun setListeners() {
        vpFirstTime.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                val progressPercentage = position.toFloat().div(firstTimeAdapter.itemCount.minus(1f)).times(100).toInt()

                ObjectAnimator.ofInt(pgProgress, "progress", progressPercentage)
                    .setDuration(300)
                    .start()

                if (position != 0) {
                    animationsHandler.removeCallbacksAndMessages(null)
                    tvSwipe?.animate()?.alpha(0F)?.setDuration(1000)?.start()
                    tvSwipe?.animate()?.translationX(0F)?.setDuration(1000)?.start()
                }

                super.onPageSelected(position)
            }
        })

        fab.setOnClickListener { goToNextPage() }
    }

    private fun goToNextPage() {
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