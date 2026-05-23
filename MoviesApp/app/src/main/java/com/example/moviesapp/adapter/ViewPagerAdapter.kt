package com.example.moviesapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moviesapp.fragment.NowPlayingFragment
import com.example.moviesapp.fragment.TopRatedFragment
import com.example.moviesapp.fragment.WatchlistFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> NowPlayingFragment()
        1 -> TopRatedFragment()
        2 -> WatchlistFragment()
        else -> NowPlayingFragment()
    }
}