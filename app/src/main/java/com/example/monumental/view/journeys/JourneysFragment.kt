package com.example.monumental.view.journeys

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import com.example.monumental.BuildConfig
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import com.example.monumental.view.main.MainActivity
import com.example.monumental.viewModel.journey.JourneysViewModel
import kotlinx.android.synthetic.main.journeys_fragment.*
import java.lang.StringBuilder
import kotlin.properties.Delegates

class JourneysFragment : Fragment() {

    companion object { fun newInstance() = JourneysFragment() }

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

        initViews()
        setListeners()
    }

    /**
     * Initiates the views
     * Sets up JourneysAdapter and Observers
     */
    private fun initViews() {
        (activity as MainActivity?)?.supportActionBar?.title = getString(R.string.journeys)

        journeysAdapter = JourneysAdapter(journeys,
            { journey: Journey -> journeyClick(journey) },
            { journey: Journey -> journeyDelete(journey) },
            { newName: String, journey: Journey -> journeyEdit(newName, journey) },
            { journey: Journey -> journeyShare(journey) })

        rvJourneys.layoutManager = StaggeredGridLayoutManager(1, RecyclerView.VERTICAL)
        rvJourneys.adapter = journeysAdapter

        viewModel.journeys.observe(viewLifecycleOwner, { journeys ->
            if (journeys != null) {
                this.journeys.clear()
                journeys.forEach { journey -> this.journeys.add(journey) }
            }

            this.journeys.sortByDescending { it.id }

            journeysAdapter.notifyDataSetChanged()

            if (this.journeys.isEmpty()) { tvNoJourneys.visibility = View.VISIBLE }
            else { tvNoJourneys.visibility = View.GONE }
        })
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
        (activity as MainActivity?)?.fragmentManager?.openLandmarkFragment(journey)
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
            deleteJourney(journey)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun deleteJourney(journey: Journey) {
        if (viewModel.deleteJourney(journey) == 1) {
            Toast.makeText(requireContext(), getString(R.string.journey_deleted, journey.name),
                Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.journey_not_deleted, journey.name),
                Toast.LENGTH_SHORT).show()
        }
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
     * Shares a Journey with all saved landmarks in it
     *
     * @param journey Journey to share
     */
    private fun journeyShare(journey: Journey) {
        viewModel.getLandmarksByJourney(journey.id!!).observe(viewLifecycleOwner, { landmarks ->
            if (landmarks!!.isNotEmpty()) {
                val imageUris: ArrayList<Uri> = arrayListOf()
                val messageString = StringBuilder(getString(R.string.share_journey_message_start, journey.name))

                landmarks.forEachIndexed { index, landmark ->
                    imageUris.add((Uri.parse(landmark.imgUri)))

                    if (index < landmarks.size.minus(1)) {
                        messageString.append(landmark.name)
                        if (index < landmarks.size.minus(2)) {
                            messageString.append((getString(R.string.share_journey_message_middle)))
                        }
                    } else {
                        messageString.append(getString(R.string.share_journey_message_end, landmark.name))
                    }
                }

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putExtra(Intent.EXTRA_TEXT, messageString.toString())
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                    type = "image/*"
                }
                startActivity(Intent.createChooser(shareIntent, "${getString(R.string.share)} ${journey.name}"))
            } else {
                Toast.makeText(context, getString(R.string.no_landmarks_to_share, journey.name), Toast.LENGTH_SHORT).show()
            }
        })
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
