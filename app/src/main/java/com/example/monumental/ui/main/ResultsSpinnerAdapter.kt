package com.example.monumental.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.example.monumental.R


class ResultsSpinnerAdapter(context: Context, resource: Int) : ArrayAdapter<CharSequence>(context, resource) {

    init {
        // Specify the layout to use when the list of choices appears
        setDropDownViewResource(R.layout.spinner_dropdown_item)

        // Add empty list item
        addAll(mutableListOf(context.getString(R.string.more_info)))

        //Refresh list
        notifyDataSetChanged()
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 0
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        return initView(position, convertView, parent)
//    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
//        return initView(position, convertView, parent)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.spinner_item_save, parent, false)
        val tv = view.findViewById(R.id.tvLandmarkResultName) as TextView
        val constraintLayout = view.findViewById(R.id.spinner_container) as RelativeLayout
        val btnSave = view.findViewById(R.id.btnSave) as ImageView

        btnSave.visibility = View.INVISIBLE

//        if (v == null) {
//            val mContext: Context = this.context
//            val vi =
//                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            v = vi.inflate(R.layout.spinner_item, parent, false)
//        }
//        val tv = v!!.findViewById<View>(android.R.id.text1) as TextView

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
                constraintLayout.setPadding(24)
                tv.isSingleLine = false
            }
            else -> {
                tv.setTextColor(context.getColor(R.color.colorPrimary))
                tv.textSize = 18.0F
                constraintLayout.setPadding(24)
                tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                tv.text = getItem(position).toString()
                btnSave.visibility = View.VISIBLE
            }
        }

        return view
    }

//    private fun initView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var cv = convertView
//        if (convertView == null) {
//            cv = LayoutInflater.from(context).inflate(R.layout.spinner_item_save, parent, false)
//        }
//
//        val tv = cv?.findViewById(R.id.tvLandmarkResultName) as TextView
//        val constraintLayout = cv.findViewById(R.id.spinner_container) as RelativeLayout
//        val btnSave = cv.findViewById(R.id.btnSave) as ImageView
//
//        btnSave.visibility = View.INVISIBLE
//
////        btnSave.setOnClickListener { tv.text = tv.text.toString() + " SAVE" }
//
//        when (position) {
//            0 -> {
//                if (this.count == 1) {
//                    tv.text = context.getString(R.string.no_landmark_tip)
//                    tv.isSingleLine = false
//                } else {
//                    tv.text = context.getString(R.string.choose_dropdown)
//                    tv.isSingleLine = true
//                }
//                tv.setTextColor(Color.GRAY)
//                tv.textSize = 22.0F
//                constraintLayout.setPadding(24)
//                tv.isSingleLine = false
//            }
//            else -> {
//                tv.setTextColor(context.getColor(R.color.colorPrimary))
//                tv.textSize = 18.0F
//                constraintLayout.setPadding(24)
//                tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
//                tv.text = getItem(position).toString()
//                btnSave.visibility = View.VISIBLE
//            }
//        }
//        return cv
//    }

    fun reset() {
        clear()
        addAll(mutableListOf(context.getString(R.string.more_info)))
        notifyDataSetChanged()
    }
}