package com.example.monumental.view.landmark

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import com.example.monumental.model.Landmark
import kotlinx.android.synthetic.main.landmark_item.view.*

class LandmarkAdapter(
    var landmarks: ArrayList<Landmark>,
    private val onLandmarkClick: (Landmark) -> Unit,
    private val onLandmarkDelete: (Landmark) -> Unit
):
    RecyclerView.Adapter<LandmarkAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.landmark_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return landmarks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(landmarks[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { onLandmarkClick(landmarks[adapterPosition]) }
            itemView.btnRemove.setOnClickListener { onLandmarkDelete(landmarks[adapterPosition]) }
        }

        fun bind(landmark: Landmark) {

            itemView.tvName.text = landmark.name

//            Glide.with(context).load(savedPokemon.poster_url).into(itemView.ivSavedPokemon)
        }
    }
}
