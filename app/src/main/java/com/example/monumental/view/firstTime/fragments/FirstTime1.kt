package com.example.monumental.view.firstTime.fragments

import android.os.Bundle
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
        setListeners()
        doAnimations()
    }

    private fun initViews() {

    }

    private fun setListeners() {
        fab.setOnClickListener { onFabClick() }
    }

    private fun onFabClick() {
        (activity as FirstTimeActivity?)?.goToNextPage()
    }

    private fun doAnimations() {

    }

    override fun onResume() {
        super.onResume() // When scrolled to in pager
    }
}