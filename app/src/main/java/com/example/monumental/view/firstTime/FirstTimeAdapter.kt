package com.example.monumental.view.firstTime

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.monumental.view.firstTime.fragments.FirstTime0
import com.example.monumental.view.firstTime.fragments.FirstTime1
import com.example.monumental.view.firstTime.fragments.FirstTime2
import com.example.monumental.view.firstTime.fragments.FirstTime3

class FirstTimeAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    /**
     * Gets the total amount of items in ViewPager
     *
     * @return Integer number of items
     */
    override fun getItemCount(): Int = 4

    /**
     * Creates the fragments for each page of the ViewPager
     *
     * @param position Index of the current page
     * @return Fragment for current page
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstTime0()
            1 -> FirstTime1()
            2 -> FirstTime2()
            3 -> FirstTime3()
            else -> FirstTime2()
        }
    }
}