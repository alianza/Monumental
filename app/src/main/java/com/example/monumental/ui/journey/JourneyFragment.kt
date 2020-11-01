package com.example.monumental.ui.journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.monumental.R
import com.example.monumental.model.Journey

class JourneyFragment : Fragment() {

    companion object {
        fun newInstance() = JourneyFragment()
    }

    private lateinit var viewModel: JourneyViewModel

    private var journeys = arrayListOf<Journey>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.journey_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JourneyViewModel::class.java)

        viewModel.journeys.observe(this, Observer { journeys ->
            journeys?.forEach { it -> println(it.name) }
        })
    }

}