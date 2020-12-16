package com.example.monumental.view.firstTime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.monumental.R
import kotlinx.android.synthetic.main.fragment_first_time_1.*

class FirstTime1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_time_1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViews()
        doAnimations()
    }

    private fun initViews() {
        Glide.with(requireContext()).asGif().load(R.raw.first_time_1_1).into(iv1)
        Glide.with(requireContext()).asGif().load(R.raw.first_time_1_2).into(iv2)

        view?.alpha = 0F
        csGifs?.alpha = 0F
        csGifs.translationY = 400F
        tvTitleLarge1?.translationY = -200F
        tvTitleSmall1?.alpha = 0F
    }

    private fun doAnimations() {
        tvTitleLarge1?.animate()?.translationY(0F)?.setDuration(2000)?.withEndAction{
            tvTitleSmall1?.animate()?.alpha(1F)?.setDuration(2000)?.start()
        }?.start()

        view?.animate()?.alpha(1F)?.setDuration(4500)?.withEndAction {
            csGifs?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            csGifs?.animate()?.translationY(0F)?.setDuration(1000)?.setInterpolator(OvershootInterpolator())?.start()
        }?.start()
    }

}