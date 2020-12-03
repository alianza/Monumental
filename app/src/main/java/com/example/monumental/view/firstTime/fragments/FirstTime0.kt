package com.example.monumental.view.firstTime.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.monumental.R
import com.example.monumental.view.firstTime.FirstTimeActivity
import kotlinx.android.synthetic.main.fragment_first_time_0.*


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FirstTime0 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_time_0, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViews()
        setListeners()
        doAnimations()
    }

    private fun initViews() {
        view?.alpha = 0F
        ivLogoShadow?.alpha = 0F
        ivLogo.translationY = 1000F
    }

    private fun setListeners() {
        ivLogo?.setOnClickListener { onLogoClick() }

        fab.setOnClickListener { onFabClick() }
    }

    private fun doAnimations() {
        ivLogo!!.animate().translationY(0F).setDuration(2000).withEndAction{
            ivLogoShadow?.animate()?.alpha(.5F)?.setDuration(6500)?.start()
            ivLogoShadow?.animate()?.translationY(12F)?.setDuration(5000)?.start()
            ivLogoShadow?.animate()?.translationX(12F)?.setDuration(5000)?.start()
        }.start()

        view?.animate()?.alpha(1F)?.setDuration(4500)?.withEndAction {
            tvSwipe?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            tvSwipe?.animate()?.translationX(-156F)?.setDuration(1000)?.start()
        }?.start()
    }

    private fun onLogoClick() {
            ivLogoShadow!!.animate().alpha(0F).setDuration(1000).start()
            ivLogoShadow!!.animate().translationY(0F).setDuration(1000).start()
            ivLogoShadow!!.animate().translationX(0F).setDuration(1000).start()

            tvSwipe!!.animate().alpha(0F).setDuration(1000).start()
            tvSwipe!!.animate().translationX(0F).setDuration(1000).start()

        Handler(Looper.getMainLooper()).postDelayed({

//            ivLogo.animate().translationY(1000F).setDuration(1000).withEndAction {
            doAnimations() // Do animations again after
//            }.start()
        }, 1000)
    }

    private fun onFabClick() {
        (activity as FirstTimeActivity?)?.goToNextPage()
    }

    private fun resetAnimations() {
        view?.alpha = 0F
        tvSwipe.alpha = 0F
        tvSwipe.translationX = 0F
        ivLogo.translationY = 1000F
        ivLogoShadow?.alpha = 0F
        ivLogoShadow?.translationX = 0F
        ivLogoShadow?.translationY = 0F
    }

    override fun onResume() {
        super.onResume()
        onLogoClick()
    }
}