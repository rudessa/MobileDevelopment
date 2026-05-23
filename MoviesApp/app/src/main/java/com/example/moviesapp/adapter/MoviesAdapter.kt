package com.example.moviesapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.moviesapp.R
import com.example.moviesapp.model.Movie
import com.example.moviesapp.model.WatchlistRepository
import com.google.android.material.button.MaterialButton

class MoviesAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerLayout: RelativeLayout = view.findViewById(R.id.headerLayout)
        val tvGenre: TextView = view.findViewById(R.id.tvGenre)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvOriginalTitle: TextView = view.findViewById(R.id.tvOriginalTitle)
        val tvDirector: TextView = view.findViewById(R.id.tvDirector)
        val tvYear: TextView = view.findViewById(R.id.tvYear)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnWatchlist: MaterialButton = view.findViewById(R.id.btnWatchlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        val ctx = holder.itemView.context

        // Цвет заголовка по жанру
        holder.headerLayout.setBackgroundColor(ctx.getColor(genreColor(movie.genre)))

        holder.tvGenre.text = movie.genre
        holder.tvDuration.text = "${movie.duration} мин"
        holder.tvTitle.text = movie.title
        holder.tvOriginalTitle.text = movie.originalTitle
        holder.tvDirector.text = "Реж. ${movie.director}"
        holder.tvYear.text = movie.year.toString()
        holder.ratingBar.rating = movie.rating / 2f   // шкала 10 → 5 звёзд
        holder.tvRating.text = "%.1f".format(movie.rating)
        holder.tvDescription.text = movie.description

        refreshWatchlistButton(holder.btnWatchlist, movie.id)

        holder.btnWatchlist.setOnClickListener {
            if (WatchlistRepository.contains(movie.id)) {
                Toast.makeText(ctx, "Уже в списке просмотра", Toast.LENGTH_SHORT).show()
            } else {
                WatchlistRepository.add(movie)
                refreshWatchlistButton(holder.btnWatchlist, movie.id)
                Toast.makeText(ctx, "«${movie.title}» добавлен в список", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshWatchlistButton(btn: MaterialButton, movieId: Int) {
        if (WatchlistRepository.contains(movieId)) {
            btn.text = "✓ В списке"
        } else {
            btn.text = "+ В список"
        }
    }

    private fun genreColor(genre: String): Int = when {
        genre.contains("Боевик") || genre.contains("Криминал") -> R.color.genre_action
        genre.contains("Биография") || genre.contains("История") -> R.color.genre_biography
        genre.contains("Комедия") -> R.color.genre_comedy
        genre.contains("Фантастика") -> R.color.genre_scifi
        genre.contains("Триллер") -> R.color.genre_thriller
        genre.contains("Фэнтези") || genre.contains("Приключения") -> R.color.genre_adventure
        genre.contains("Ужасы") -> R.color.genre_horror
        else -> R.color.genre_drama
    }

    override fun getItemCount() = movies.size
}