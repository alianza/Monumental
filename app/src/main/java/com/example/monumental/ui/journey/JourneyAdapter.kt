package com.example.monumental.ui.journey

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import com.example.monumental.model.Journey
import kotlinx.android.synthetic.main.journey_item.view.*

class JourneyAdapter(var journeys: ArrayList<Journey>, private val onJourneyClick: (Journey) -> Unit, private val onJourneyDelete: (Journey) -> Unit): RecyclerView.Adapter<JourneyAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.journey_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return journeys.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(journeys[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { onJourneyClick(journeys[adapterPosition]) }
            itemView.btnRemove.setOnClickListener { onJourneyDelete(journeys[adapterPosition]) }
        }

        fun bind(journey: Journey) {
            itemView.tvName.text = journey.name
//            Glide.with(context).load(savedPokemon.poster_url).into(itemView.ivSavedPokemon)
        }
    }
}
