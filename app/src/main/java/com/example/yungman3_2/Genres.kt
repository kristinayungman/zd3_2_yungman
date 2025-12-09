package com.example.yungman3_2

object Genres {
    val genres = listOf(
        Genre("Все", ""),
        Genre("Боевик", "action"),
        Genre("Комедия", "comedy"),
        Genre("Драма", "drama"),
        Genre("Фантастика", "sci-fi"),
        Genre("Ужасы", "horror"),
        Genre("Триллер", "thriller"),
        Genre("Романтика", "romance"),
        Genre("Приключения", "adventure"),
        Genre("Аниме", "anime"),
        Genre("Детектив", "mystery"),
        Genre("Фэнтези", "fantasy")
    )
}

data class Genre(
    val name: String,
    val searchQuery: String
)