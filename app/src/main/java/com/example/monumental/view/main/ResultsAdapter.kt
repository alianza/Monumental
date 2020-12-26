package com.example.monumental.view.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import kotlinx.android.synthetic.main.result_item.view.*

class ResultsAdapter(
    var landmarks: ArrayList<String>,
    private val onLandmarkClick: (String) -> Unit,
    private val onLandmarkShare: (String) -> Unit,
    private val onLandmarkSave: (String) -> Unit
): RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    private lateinit var context: Context

    /**
     * When ViewHolder is created inflate layout
     *
     * @param parent ViewGroup
     * @param viewType Integer
     * @return ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.result_item, parent, false)
        )
    }

    /**
     * Gets the total count of items
     *
     * @return Integer number of items
     */
    override fun getItemCount(): Int {
        return landmarks.size
    }

    /**
     * Binds the Journey to the ViewHolder
     *
     * @param holder ViewHolder
     * @param position Current page index
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(landmarks[position])
    }

    /**
     * Gets an item according to index
     *
     * @param position Index of item to retrieve
     * @return String of received item
     */
    fun getItem(position: Int): String {
        return landmarks[position]
    }

    /**
     * Inner class for LandmarkResult item ViewHolder
     * Sets ItemView Listeners and binds LandmarkResult object
     *
     * @param itemView View
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener          { onLandmarkClick(landmarks[adapterPosition]) }
            itemView.btnShare.setOnClickListener { onLandmarkShare(landmarks[adapterPosition]) }
            itemView.btnSave.setOnClickListener  { onLandmarkSave(landmarks[adapterPosition]) }
        }

        /**
         * Binds LandmarkResult object with ItemView
         *
         * @param landmarkName LandmarkResult object to bind
         */
        fun bind(landmarkName: String) {
            itemView.results_container.setPadding(24, 24, 24, 24)
            itemView.tvLandmarkResultName.text = landmarkName
            itemView.tvLandmarkResultName.setTextColor(context.getColor(R.color.colorPrimary))
            itemView.tvLandmarkResultName.textSize = 18.0F
            itemView.tvLandmarkResultName.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
            Handler(Looper.getMainLooper()).postDelayed({ itemView.tvLandmarkResultName.isSelected = true }, 1500)
        }
    }

    /**
     * Resets the data
     */
    fun reset() {
        clear()
        notifyDataSetChanged()
    }

    /**
     * Empties the dataSet
     */
    fun clear() {
        landmarks = ArrayList()
    }
}
