package com.example.monumental.view.firstTime

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.monumental.view.firstTime.fragments.FirstTime0
import com.example.monumental.view.firstTime.fragments.FirstTime1
import com.example.monumental.view.firstTime.fragments.FirstTime2

class FirstTimeAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstTime0()
            1 -> FirstTime1()
            2 -> FirstTime2()
            3 -> FirstTime0()
            else -> FirstTime0()
        }
    }
}