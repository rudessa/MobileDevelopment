package com.example.moviesapp.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.model.WatchlistItem
import com.example.moviesapp.model.WatchlistRepository

class WatchlistAdapter(
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    inner class WatchlistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxWatched)
        val tvTitle: TextView = view.findViewById(R.id.tvWatchlistTitle)
        val tvGenre: TextView = view.findViewById(R.id.tvWatchlistGenre)
        val tvYear: TextView = view.findViewById(R.id.tvWatchlistYear)
        val tvRating: TextView = view.findViewById(R.id.tvWatchlistRating)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_watchlist, parent, false)
        return WatchlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        val item: WatchlistItem = WatchlistRepository.watchlist[position]
        val movie = item.movie

        holder.tvTitle.text = movie.title
        holder.tvGenre.text = movie.genre
        holder.tvYear.text = movie.year.toString()
        holder.tvRating.text = "%.1f".format(movie.rating)

        // Зачёркивание просмотренных
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isWatched
        applyStrikeThrough(holder.tvTitle, item.isWatched)

        holder.checkBox.setOnCheckedChangeListener { _, _ ->
            WatchlistRepository.toggleWatched(item)
            notifyItemChanged(position)
            onChanged()
        }

        holder.btnRemove.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_ID.toInt()) {
                WatchlistRepository.remove(WatchlistRepository.watchlist[currentPos])
                notifyItemRemoved(currentPos)
                notifyItemRangeChanged(currentPos, WatchlistRepository.watchlist.size)
                onChanged()
            }
        }
    }

    private fun applyStrikeThrough(tv: TextView, apply: Boolean) {
        if (apply) {
            tv.paintFlags = tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tv.alpha = 0.45f
        } else {
            tv.paintFlags = tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            tv.alpha = 1.0f
        }
    }

    override fun getItemCount() = WatchlistRepository.watchlist.size
}