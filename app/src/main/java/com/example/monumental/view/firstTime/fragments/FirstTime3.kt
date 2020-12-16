package com.example.monumental.view.firstTime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.monumental.R
import kotlinx.android.synthetic.main.fragment_first_time_3.*

class FirstTime3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_time_3, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViews()
        doAnimations()
    }

    private fun initViews() {
        Glide.with(requireContext()).asGif().diskCacheStrategy(DiskCacheStrategy.DATA).load(R.raw.first_time_3_1).into(iv1)

        view?.alpha = 0F
        iv1?.alpha = 0F
        iv1?.translationY = 400F
        tvTitleLarge3?.translationY = -200F
        tvTitleSmall3?.alpha = 0F
    }

    private fun doAnimations() {
        tvTitleLarge3?.animate()?.translationY(0F)?.setDuration(2000)?.start()
        tvTitleSmall3?.animate()?.alpha(1F)?.setDuration(2000)?.start()

        view?.animate()?.alpha(1F)?.setDuration(4500)?.withEndAction {
            iv1?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            iv1?.animate()?.translationY(0F)?.setDuration(1000)?.setInterpolator(OvershootInterpolator())?.start()
        }?.start()
    }

}