package com.example.monumental.view.journeys

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.monumental.R
import com.example.monumental.model.entity.Journey
import kotlinx.android.synthetic.main.journey_item.view.*


class JourneysAdapter(
    var journeys: ArrayList<Journey>,
    val actionDelayVal: Long,
    private val onJourneyClick: (Journey) -> Unit,
    private val onJourneyDelete: (Journey) -> Unit,
    private val onJourneyEdit: (String, Journey) -> Unit
):
    RecyclerView.Adapter<JourneysAdapter.ViewHolder>() {

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
            LayoutInflater.from(parent.context).inflate(R.layout.journey_item, parent, false)
        )
    }

    /**
     * Gets the total count of items
     *
     * @return Integer number of items
     */
    override fun getItemCount(): Int {
        return journeys.size
    }

    /**
     * Binds the Journey to the ViewHolder
     *
     * @param holder ViewHolder
     * @param position Current page index
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(journeys[position])
    }

    /**
     * Inner class for Journey item ViewHolder
     * Sets ItemView Listeners and binds Journey object
     *
     * @param itemView View
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener { onJourneyClick(journeys[adapterPosition]) }
            itemView.btnRemove.setOnClickListener { onJourneyDelete(journeys[adapterPosition]) }

            itemView.etName.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        itemView.ivDone.performClick()
                        return true
                    }
                    return false
                }
            })

            itemView.ivEdit.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                        editJourney(itemView)
                }, actionDelayVal)
            }

            itemView.ivDone.setOnClickListener {
                Handler(Looper.getMainLooper()).postDelayed({
                        updateJourney(itemView)
                }, actionDelayVal)
            }
        }

        /**
         * Binds Journey object with ItemView
         *
         * @param journey Journey object to bind
         */
        fun bind(journey: Journey) {

            itemView.tvName.text = journey.name

            itemView.rCurrent.isChecked = journey.current
            itemView.rCurrent.isSelected = journey.current
        }

        /**
         * Callback for Journey Update
         *
         * @param itemView ItemView where event originates from
         */
        private fun updateJourney(itemView: View) {
            itemView.ivEdit.visibility = View.VISIBLE
            itemView.tvName.visibility = View.VISIBLE
            itemView.ivDone.visibility = View.INVISIBLE
            itemView.etName.visibility = View.INVISIBLE

            onJourneyEdit(itemView.etName.text.toString(), journeys[adapterPosition])

            Toast.makeText(context, context.getString(R.string.journey_updated), Toast.LENGTH_SHORT)
                .show()
        }

        /**
         * Callback for Journey edit
         *
         * @param itemView ItemView where event originates from
         */
        private fun editJourney(itemView: View) {
            itemView.ivEdit.visibility = View.INVISIBLE
            itemView.tvName.visibility = View.INVISIBLE
            itemView.ivDone.visibility = View.VISIBLE
            itemView.etName.visibility = View.VISIBLE

            itemView.etName.setText(itemView.tvName.text)
            itemView.etName.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(itemView.etName, InputMethodManager.SHOW_IMPLICIT)

            if (itemView.etName.text.toString() == context.getString(R.string.new_journey)) {
                itemView.etName.selectAll()
            }
        }
    }
}
