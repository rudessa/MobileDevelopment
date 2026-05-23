package com.example.moviesapp.model

data class WatchlistItem(
    val movie: Movie,
    val isWatched: Boolean = false
)