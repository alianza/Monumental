package com.example.monumental

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.setPadding

class ResultsSpinnerAdapter(context: Context, resource: Int)
    : ArrayAdapter<CharSequence>(context, resource) {

    override fun isEnabled(position: Int): Boolean {
        return position != 0
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var v = convertView

        if (v == null) {
            val mContext: Context = this.context
            val vi =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(R.layout.spinner_item, parent, false)
        }
        val tv = v!!.findViewById<View>(android.R.id.text1) as TextView

        when (position) {
            0 -> {
                if (this.count == 1) {
                    tv.text = context.getString(R.string.no_landmark_tip)
                    tv.isSingleLine = false
                } else {
                    tv.text = context.getString(R.string.choose_dropdown)
                    tv.isSingleLine = true
                }
                tv.setTextColor(Color.GRAY)
                tv.textSize = 22.0F
                tv.setPadding(24)
                tv.isSingleLine = false
            }
            else -> {
                tv.setTextColor(context.getColor(R.color.colorPrimary))
                tv.textSize = 18.0F
                tv.setPadding(24)
                tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                tv.text = getItem(position).toString()
            }
        }

        return v
    }
}