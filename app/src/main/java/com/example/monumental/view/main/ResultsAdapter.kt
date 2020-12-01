package com.example.monumental.view.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import kotlinx.android.synthetic.main.info_item.view.*

open class ResultsAdapter(
    var landmarks: ArrayList<String>,
    private val onLandmarkClick: (String) -> Unit,
    private val onLandmarkSave: (String) -> Unit
):
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.info_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return landmarks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(landmarks[position])
    }

    fun getItem(position: Int): String? {
        return landmarks[position]
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { onLandmarkClick(landmarks[adapterPosition]) }
            itemView.btnSave.setOnClickListener { onLandmarkSave(landmarks[adapterPosition]) }
        }

        fun bind(landmarkName: String) {

            itemView.tvLandmarkResultName.text = landmarkName
            itemView.tvLandmarkResultName.setTextColor(context.getColor(R.color.colorPrimary))
            itemView.tvLandmarkResultName.textSize = 18.0F
            itemView.results_container.setPadding(24)
            itemView.tvLandmarkResultName.textAlignment = View.TEXT_ALIGNMENT_VIEW_START

            println("lmname$landmarkName")

//            Glide.with(context).load(savedPokemon.poster_url).into(itemView.ivSavedPokemon)
        }
    }

    fun reset() {
        clear()
        notifyDataSetChanged()
    }

    fun clear() {
        landmarks = ArrayList()
    }
}
