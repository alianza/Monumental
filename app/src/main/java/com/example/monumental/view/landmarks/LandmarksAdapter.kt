package com.example.monumental.view.landmarks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import com.example.monumental.model.entity.Landmark
import kotlinx.android.synthetic.main.landmark_item.view.*

class LandmarksAdapter(
    var landmarks: ArrayList<Landmark>,
    private val onLandmarkClick: (Landmark) -> Unit,
    private val onLandmarkDelete: (Landmark) -> Unit
):
    RecyclerView.Adapter<LandmarksAdapter.ViewHolder>() {

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
            LayoutInflater.from(parent.context).inflate(R.layout.landmark_item, parent, false)
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
     * Binds the Landmark to the ViewHolder
     *
     * @param holder ViewHolder
     * @param position Current page index
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(landmarks[position])
    }

    /**
     * Inner class for Landmark item ViewHolder
     * Sets ItemView Listeners and binds Landmark object
     *
     * @param itemView View
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { onLandmarkClick(landmarks[adapterPosition]) }
            itemView.btnRemove.setOnClickListener { onLandmarkDelete(landmarks[adapterPosition]) }
        }

        /**
         * Binds Journey object with ItemView
         *
         * @param landmark Journey object to bind
         */
        fun bind(landmark: Landmark) {

            itemView.tvName.text = landmark.name

//            Glide.with(context).load(savedPokemon.poster_url).into(itemView.ivSavedPokemon)
        }
    }
}
