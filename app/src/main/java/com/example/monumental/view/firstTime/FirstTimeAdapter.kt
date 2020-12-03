package com.example.monumental.view.firstTime

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.monumental.view.firstTime.fragments.FirstTime0
import com.example.monumental.view.firstTime.fragments.FirstTime1

class FirstTimeAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstTime0()
            1 -> FirstTime1()
            2 -> FirstTime1()
            3 -> FirstTime1()
            4 -> FirstTime0()
            5 -> FirstTime1()
            else -> FirstTime0()
        }
    }
}