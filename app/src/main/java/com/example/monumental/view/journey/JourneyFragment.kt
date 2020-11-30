package com.example.monumental.view.journey

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.view.main.MainActivity
import com.example.monumental.viewModel.journey.JourneyViewModel
import kotlinx.android.synthetic.main.journey_fragment.*

class JourneyFragment : Fragment() {

    companion object { fun newInstance() = JourneyFragment() }

    private var actionDelayVal: Long = 0
    private var journeys = arrayListOf<Journey>()

    private lateinit var viewModel: JourneyViewModel
    private lateinit var journeyAdapter: JourneyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.journey_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JourneyViewModel::class.java)
        actionDelayVal = (activity as MainActivity?)?.actionDelayVal!!

        initViews()
        setListeners()
    }

    private fun initViews() {
        journeyAdapter = JourneyAdapter(journeys, actionDelayVal,
            { journey: Journey -> journeyClick(journey) },
            { journey: Journey -> journeyDelete(journey) },
            { newName: String, journey: Journey -> journeyEdit(newName, journey) })

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
            this.journeys.sortByDescending { it.id }
            journeyAdapter.notifyDataSetChanged()
            if (this.journeys.isEmpty()) {
                tvNoJourneys.visibility = View.VISIBLE
            } else {
                tvNoJourneys.visibility = View.GONE
            }
        })

        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)
    }

    private fun setListeners() {
        fab.setOnClickListener { journeyCreate() }
        btnClose.setOnClickListener { closeFragment() }
    }

    private fun journeyClick(journey: Journey) {
        println("Click! " + journey.name)
        Handler().postDelayed({
            (activity as MainActivity?)?.fragmentManager?.openLandmarkFragment(journey)
        }, actionDelayVal)
    }

    private fun journeyDelete(journey: Journey) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle("Remove " + journey.name + "?")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            println("Delete!")
            viewModel.deleteJourney(journey)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert: AlertDialog = builder.create()
        Handler().postDelayed({
            alert.show()
        }, actionDelayVal)
    }

    private fun journeyEdit(newName: String, journey: Journey) {
        println("Updatename: " + journey.name)
        journey.name = newName
        viewModel.updateJourney(journey)
    }

    private fun journeyCreate() {
        viewModel.setActiveJourney(Journey(viewModel.createJourney().toInt(), "", true))
        Toast.makeText(context, this.getString(R.string.new_journey), Toast.LENGTH_SHORT).show()
    }

    private fun closeFragment() {
        (activity as MainActivity?)?.fragmentManager?.closeJourneyFragment()
    }
}
