package com.example.monumental.view.journeys

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.monumental.viewModel.journey.JourneysViewModel
import kotlinx.android.synthetic.main.journeys_fragment.*

class JourneysFragment : Fragment() {

    companion object { fun newInstance() = JourneysFragment() }

    private var actionDelayVal: Long = 0
    private var journeys = arrayListOf<Journey>()

    private lateinit var viewModel: JourneysViewModel
    private lateinit var journeysAdapter: JourneysAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.journeys_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JourneysViewModel::class.java)
        actionDelayVal = (activity as MainActivity?)?.actionDelayVal!!

        initViews()
        setListeners()
    }

    /**
     * Initiates the views
     * Sets up JourneysAdapter and Observers
     */
    private fun initViews() {
        journeysAdapter = JourneysAdapter(journeys, actionDelayVal,
            { journey: Journey -> journeyClick(journey) },
            { journey: Journey -> journeyDelete(journey) },
            { newName: String, journey: Journey -> journeyEdit(newName, journey) })

        rvJourneys.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvJourneys.adapter = journeysAdapter

        viewModel.journeys.observe(viewLifecycleOwner, { journeys ->
            if (journeys != null) {
                this.journeys.clear()
                journeys.forEach { journey ->
                    this.journeys.add(journey)
                    println("Got: $journey")
                }
            }
            this.journeys.sortByDescending { it.id }
            journeysAdapter.notifyDataSetChanged()
            if (this.journeys.isEmpty()) {
                tvNoJourneys.visibility = View.VISIBLE
            } else {
                tvNoJourneys.visibility = View.GONE
            }
        })

        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)
    }

    /**
     * Sets onClick listeners
     */
    private fun setListeners() {
        fab.setOnClickListener { onFabClick() }

        btnClose.setOnClickListener { onCloseButtonClick() }
    }

    /**
     * When Journey is Clicked
     *
     * @param journey Journey that has been Clicked
     */
    private fun journeyClick(journey: Journey) {
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as MainActivity?)?.fragmentManager?.openLandmarkFragment(journey)
        }, actionDelayVal)
    }

    /**
     * When Journey delete button is clicked
     * Builds dialog for delete confirmation
     *
     * @param journey Journey to delete
     */
    private fun journeyDelete(journey: Journey) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)

        builder.setTitle("Remove " + journey.name + "?")
        builder.setMessage("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, _ ->
            viewModel.deleteJourney(journey)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert: AlertDialog = builder.create()
        Handler(Looper.getMainLooper()).postDelayed({
            alert.show()
        }, actionDelayVal)
    }

    /**
     * When Journey edit button is clicked
     *
     * @param newName New name for Journey
     * @param journey Journey to update
     */
    private fun journeyEdit(newName: String, journey: Journey) {
        journey.name = newName
        viewModel.updateJourney(journey)
    }

    /**
     * When the FAB is clicked
     * Create new journey and set it as the active journey
     */
    private fun onFabClick() {
        viewModel.setActiveJourney(Journey(viewModel.createJourney().toInt(), "", true))
        Toast.makeText(context, this.getString(R.string.new_journey_msg), Toast.LENGTH_SHORT).show()
    }

    /**
     * When the close button is clicked
     * Close the Journey fragment using the FragmentManager
     */
    private fun onCloseButtonClick() {
        (activity as MainActivity?)?.fragmentManager?.closeJourneyFragment()
    }
}
