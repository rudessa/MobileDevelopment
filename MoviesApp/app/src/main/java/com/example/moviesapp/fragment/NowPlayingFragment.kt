package com.example.moviesapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.adapter.MoviesAdapter
import com.example.moviesapp.model.MovieData

class NowPlayingFragment : Fragment() {

    private lateinit var adapter: MoviesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_movies, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MoviesAdapter(MovieData.nowPlaying)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        return view
    }

    // Обновляем кнопки при возврате на вкладку (мог измениться список)
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}