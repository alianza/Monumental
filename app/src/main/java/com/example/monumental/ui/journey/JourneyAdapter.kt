package com.example.monumental.ui.journey

import android.content.Context
import android.os.Handler
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import com.example.monumental.model.Journey
import kotlinx.android.synthetic.main.journey_item.view.*


class JourneyAdapter(
    var journeys: ArrayList<Journey>,
    val actionDelayVal: Long,
    private val onJourneyClick: (Journey) -> Unit,
    private val onJourneyDelete: (Journey) -> Unit,
    private val onJourneyEdit: (String, Journey) -> Unit
):
    RecyclerView.Adapter<JourneyAdapter.ViewHolder>() {

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

            itemView.etName.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    // If the event is a key-down event on the "enter" button
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        itemView.ivDone.performClick()
                        return true
                    }
                    return false
                }
            })

            itemView.ivEdit.setOnClickListener { Handler().postDelayed(
                { editJourney(itemView) },
                actionDelayVal!!
            ) }

            itemView.ivDone.setOnClickListener { Handler().postDelayed(
                { updateJourney(itemView) },
                actionDelayVal!!
            ) }
        }

        fun bind(journey: Journey) {

            itemView.tvName.text = journey.name

//            Glide.with(context).load(savedPokemon.poster_url).into(itemView.ivSavedPokemon)
        }

        private fun updateJourney(itemView: View) {
            itemView.ivEdit.visibility = View.VISIBLE
            itemView.tvName.visibility = View.VISIBLE
            itemView.ivDone.visibility = View.INVISIBLE
            itemView.etName.visibility = View.INVISIBLE

            onJourneyEdit(itemView.etName.text.toString(), journeys[adapterPosition])

            Toast.makeText(context, context.getString(R.string.journey_updated), Toast.LENGTH_SHORT)
                .show()
        }

        private fun editJourney(itemView: View) {
            itemView.ivEdit.visibility = View.INVISIBLE
            itemView.tvName.visibility = View.INVISIBLE
            itemView.ivDone.visibility = View.VISIBLE
            itemView.etName.visibility = View.VISIBLE

            itemView.etName.setText(itemView.tvName.text)
            itemView.etName.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(itemView.etName, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
