package com.example.yungman3_2

data class Movie(
    val title: String,
    val poster: String,
    val description: String,
    val year: String = "",
    val imdbID: String = ""
)