package com.example.monumental.view.firstTime

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.monumental.R
import com.example.monumental.viewModel.firstTime.FirstTimeViewModel
import kotlinx.android.synthetic.main.activity_first_time.*

class FirstTimeActivity : AppCompatActivity() {

    private lateinit var firstTimeAdapter: FirstTimeAdapter
    private lateinit var viewModel: FirstTimeViewModel

    private var animationsHandler = Handler(Looper.getMainLooper())

    /** onCreate method to set layout, theme and initiate the views */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_time)
        viewModel = ViewModelProvider(this).get(FirstTimeViewModel::class.java)
        supportActionBar?.hide()

        initViews()
        setListeners()
        doAnimations()
    }

    /**
     * Initiates the Activity, sets ViewPager Adapter and progressbar
     */
    private fun initViews() {
        firstTimeAdapter = FirstTimeAdapter(this)

        vpFirstTime.adapter = firstTimeAdapter

        pgProgress.max = 100
    }

    /**
     * Performs animations
     */
    private fun doAnimations() {
        animationsHandler.postDelayed({
            tvSwipe?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            tvSwipe?.animate()?.translationX(-148F)?.setDuration(1000)?.start()
        }, 5000)
    }

    /**
     * Sets event listeners, ViewPager on page change listener and Fab onClick listener
     */
    private fun setListeners() {
        vpFirstTime.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                val progressPercentage = position.toFloat().div(firstTimeAdapter.itemCount.minus(1f)).times(100).toInt()

                ObjectAnimator.ofInt(pgProgress, "progress", progressPercentage)
                    .setDuration(300)
                    .start()

                if (position != 0) { // When move from first page
                    animationsHandler.removeCallbacksAndMessages(null)
                    tvSwipe?.animate()?.alpha(0F)?.setDuration(1000)?.start()
                    tvSwipe?.animate()?.translationX(0F)?.setDuration(1000)?.start()
                }

                super.onPageSelected(position)
            }
        })

        fab.setOnClickListener { goToNextPage() }
    }

    /**
     * Moves the ViewPager to the next page
     */
    private fun goToNextPage() {
        if (vpFirstTime.currentItem.plus(1) != firstTimeAdapter.itemCount) {
            vpFirstTime.setCurrentItem(vpFirstTime.currentItem + 1, true)
            firstTimeAdapter.notifyItemChanged(vpFirstTime.currentItem)
        } else { // When at last page
            setSharedPref()
            returnIntent()
            finish()
        }
    }

    /**
     * Sets the First Time shared preference
     */
    private fun setSharedPref() {
        viewModel.setSharedPref()
    }

    /**
     * sets Intent to end Activity and result
     */
    private fun returnIntent() {
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
    }

    /**
     * Overrides onBackPressed to set shared preference and set return intent before finish is called
     */
    override fun onBackPressed() {
        setSharedPref()
        returnIntent()
        finish()
    }

    /**
     * Overrides onDestroy to first set shared preference
     */
    override fun onDestroy() {
        setSharedPref()
        super.onDestroy()
    }
}