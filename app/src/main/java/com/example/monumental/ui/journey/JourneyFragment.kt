package com.example.monumental.ui.journey

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.MainActivity
import com.example.monumental.R
import com.example.monumental.model.Journey
import kotlinx.android.synthetic.main.journey_fragment.*


class JourneyFragment : Fragment() {

    companion object {
        fun newInstance() = JourneyFragment()
    }

    private lateinit var viewModel: JourneyViewModel
    private val actionDelayVal = 250L

    private var journeys = arrayListOf<Journey>()

    private val journeyAdapter = JourneyAdapter(journeys,
        { journey: Journey -> journeyClick(journey) },
        { journey: Journey -> journeyDelete(journey) },
        { newName: String, journey: Journey -> journeyEdit(newName, journey) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.journey_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JourneyViewModel::class.java)

        initViews()
        setListeners()
    }

    private fun setListeners() {
        fab.setOnClickListener { journeyCreate() }
        btnClose.setOnClickListener { closeFragment() }
    }

    private fun initViews() {
        rvJourneys.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvJourneys.adapter = journeyAdapter

        viewModel.journeys.observe(viewLifecycleOwner, { journeys ->
            if (journeys != null) {
                this.journeys.clear()
                journeys.forEach { journey ->
                    this.journeys.add(journey)
                    println("Got: $journey")
                }
            }
//            this.journeys.sortByDescending { it.name }
            journeyAdapter.notifyDataSetChanged()
            if (this.journeys.isEmpty()) {
                tvNoJourneys.visibility = View.VISIBLE
            } else {
                tvNoJourneys.visibility = View.GONE
            }
        })
    }

    private fun journeyClick(journey: Journey) {
        println("Click! " + journey.name)
        Handler().postDelayed({
            (activity as MainActivity?)?.fragmentHelper?.openLandmarkFragment(journey.id!!)
        }, actionDelayVal)
    }

    private fun journeyDelete(journey: Journey) {
        println("Delete!")
        Handler().postDelayed({
            viewModel.deleteJourney(journey)
        }, actionDelayVal)
    }

    private fun journeyEdit(newName: String, journey: Journey) {
        println("Updatename: " + journey.name)
        journey.name = newName
        viewModel.updateJourney(journey)
    }

    private fun journeyCreate() {
        viewModel.createJourney()
        Toast.makeText(context, this.getString(R.string.new_journey), Toast.LENGTH_SHORT).show()
    }

    private fun closeFragment() {
        (activity as MainActivity?)?.fragmentHelper?.closeJourneyFragment()
    }
}
