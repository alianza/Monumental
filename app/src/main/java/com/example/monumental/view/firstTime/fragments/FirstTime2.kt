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
import kotlinx.android.synthetic.main.fragment_first_time_2.*

class FirstTime2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first_time_2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViews()
        doAnimations()
    }

    private fun initViews() {
        Glide.with(context!!).asGif().diskCacheStrategy(DiskCacheStrategy.DATA).load(R.drawable.first_time_2_1).into(ivTutorialGif1)

        view?.alpha = 0F
        ivTutorialGif1?.alpha = 0F
        ivTutorialGif1.translationY = 400F
    }

    private fun doAnimations() {
        tvTitle?.animate()?.translationY(0F)?.setDuration(2000)?.start()

        view?.animate()?.alpha(1F)?.setDuration(4500)?.withEndAction {
            ivTutorialGif1?.animate()?.alpha(1F)?.setDuration(1000)?.start()
            ivTutorialGif1?.animate()?.translationY(0F)?.setDuration(1000)?.setInterpolator(OvershootInterpolator())?.start()
        }?.start()
    }

    override fun onResume() {
        super.onResume() // When scrolled to in pager
    }
}