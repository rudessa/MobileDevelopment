package com.example.moviesapp.model

data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val genre: String,
    val year: Int,
    val rating: Float,
    val duration: Int,         // в минутах
    val description: String,
    val director: String
)