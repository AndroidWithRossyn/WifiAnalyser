package com.example.wifianalyser.ui.ViewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList: ArrayList<Fragment> = ArrayList()

    override fun getItemCount(): Int {
return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
return fragmentList[position]
    }

    fun addFragment(fragment: Fragment?) {
        fragmentList.add(fragment!!)
    }

}