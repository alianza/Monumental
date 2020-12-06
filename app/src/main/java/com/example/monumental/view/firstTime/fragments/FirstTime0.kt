package com.example.monumental.view.firstTime.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.monumental.R
import kotlinx.android.synthetic.main.fragment_first_time_0.*

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
    }

    private fun doAnimations() {
        ivLogo?.animate()?.translationY(0F)?.setDuration(2000)?.withEndAction{
            ivLogoShadow?.animate()?.alpha(.5F)?.setDuration(6500)?.start()
            ivLogoShadow?.animate()?.translationY(12F)?.setDuration(5000)?.start()
            ivLogoShadow?.animate()?.translationX(12F)?.setDuration(5000)?.start()
        }?.start()

        view?.animate()?.alpha(1F)?.setDuration(4500)?.start()
    }

    private fun onLogoClick() {
            ivLogoShadow?.animate()?.alpha(0F)?.setDuration(1000)?.start()
            ivLogoShadow?.animate()?.translationY(0F)?.setDuration(1000)?.start()
            ivLogoShadow?.animate()?.translationX(0F)?.setDuration(1000)?.start()

        Handler(Looper.getMainLooper()).postDelayed({
            doAnimations() // Do animations again after
        }, 1000)
    }

    private fun resetAnimations() {
        view?.alpha = 0F
        ivLogo?.translationY = 1000F
        ivLogoShadow?.alpha = 0F
        ivLogoShadow?.translationX = 0F
        ivLogoShadow?.translationY = 0F
    }

    override fun onResume() {
        super.onResume()
        onLogoClick()
    }
}