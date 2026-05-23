package com.example.moviesapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.adapter.WatchlistAdapter
import com.example.moviesapp.model.WatchlistRepository
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class WatchlistFragment : Fragment() {

    private lateinit var adapter: WatchlistAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_watchlist, container, false)

        tvEmpty = view.findViewById(R.id.tvEmpty)
        recyclerView = view.findViewById(R.id.recyclerViewWatchlist)

        adapter = WatchlistAdapter { updateEmptyState() }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        view.findViewById<ExtendedFloatingActionButton>(R.id.fabClearWatched)
            .setOnClickListener {
                WatchlistRepository.clearWatched()
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }

        updateEmptyState()
        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val empty = WatchlistRepository.watchlist.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
    }
}