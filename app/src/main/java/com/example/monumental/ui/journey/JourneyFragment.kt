package com.example.monumental.ui.journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.R
import com.example.monumental.model.Journey
import kotlinx.android.synthetic.main.journey_fragment.*

class JourneyFragment : Fragment() {

    companion object {
        fun newInstance() = JourneyFragment()
    }

    private lateinit var viewModel: JourneyViewModel

    private var journeys = arrayListOf<Journey>()
    private val journeyAdapter = JourneyAdapter(journeys, { journey: Journey -> onJourneyClick(journey)  }, {journey: Journey -> onJourneyDelete(journey)})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.journey_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JourneyViewModel::class.java)

        rvJourneys.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvJourneys.adapter = journeyAdapter

        viewModel.journeys.observe(viewLifecycleOwner, { journeys ->
            if (journeys != null) {
                this.journeys.clear()
                journeys.forEach { journey ->
                    this.journeys.add(journey)
                    println(journey)
                }
            }
            this.journeys.sortByDescending { it.name }
            journeyAdapter.notifyDataSetChanged()
            if (this.journeys.isEmpty()) {
                tvNoJourneys.visibility = View.VISIBLE
            } else {
                tvNoJourneys.visibility = View.GONE
            }
        })

        fab.setOnClickListener { viewModel.createJourney() }
        btnClose.setOnClickListener { activity?.onBackPressed() }
    }

    private fun onJourneyClick(journey: Journey) {
        println("Click!")
    }

    private fun onJourneyDelete(journey: Journey) {
        println("Delete!")
    }
}
