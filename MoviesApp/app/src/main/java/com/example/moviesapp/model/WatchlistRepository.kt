package com.example.moviesapp.model

object WatchlistRepository {

    val watchlist = mutableListOf<WatchlistItem>()

    fun add(movie: Movie) {
        if (watchlist.none { it.movie.id == movie.id }) {
            watchlist.add(WatchlistItem(movie))
        }
    }

    fun remove(item: WatchlistItem) {
        watchlist.remove(item)
    }

    fun toggleWatched(item: WatchlistItem) {
        val index = watchlist.indexOfFirst { it.movie.id == item.movie.id }
        if (index != -1) {
            watchlist[index] = item.copy(isWatched = !item.isWatched)
        }
    }

    fun clearWatched() {
        watchlist.removeAll { it.isWatched }
    }

    fun contains(movieId: Int) = watchlist.any { it.movie.id == movieId }
}