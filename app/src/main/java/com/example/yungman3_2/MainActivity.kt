package com.example.yungman3_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var movieList: MutableList<Movie>
    private lateinit var adapter: MovieAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var titleText: TextView
    private lateinit var genreContainer: LinearLayout

    private var currentGenre: Genre = Genres.genres[0] // По умолчанию "Все"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.gridView)
        progressBar = findViewById(R.id.progressBar)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        titleText = findViewById(R.id.titleText)
        genreContainer = findViewById(R.id.genreContainer)

        movieList = mutableListOf()
        adapter = MovieAdapter(movieList)
        gridView.adapter = adapter

        // Создаем меню жанров
        setupGenreMenu()

        // Загружаем фильмы при запуске (все жанры)
        loadMoviesByGenre(currentGenre)

        // Обработчик кнопки поиска
        searchButton.setOnClickListener {
            performSearch()
        }

        // Обработчик клавиши Enter в поле поиска
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Для телевизора - обработка нажатия OK/Enter на поле поиска
        searchEditText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else {
                false
            }
        }

        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val movie = movieList[position]
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("title", movie.title)
                putExtra("poster", movie.poster)
                putExtra("year", movie.year)
                putExtra("imdbID", movie.imdbID)
            }
            startActivity(intent)
        }

        // Для телевизора настраиваем фокус
        gridView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Можно добавить визуальную обратную связь при выборе элемента
                view?.setBackgroundColor(android.graphics.Color.parseColor("#333333"))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupGenreMenu() {
        Genres.genres.forEach { genre ->
            val button = Button(this).apply {
                text = genre.name
                setTextColor(resources.getColor(R.color.black))
                setBackgroundColor(resources.getColor(R.color.aquamarine))
                textSize = 16f
                setPadding(40, 20, 40, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 15
                }

                // Стиль для выбранного жанра
                if (genre == currentGenre) {
                    setBackgroundColor(android.graphics.Color.parseColor("#00cc00"))
                }

                setOnClickListener {
                    selectGenre(genre)
                }

                // Для TV-версии - обработка фокуса
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        setBackgroundColor(android.graphics.Color.parseColor("#00cc00"))
                    } else {
                        if (genre == currentGenre) {
                            setBackgroundColor(android.graphics.Color.parseColor("#00cc00"))
                        } else {
                            setBackgroundColor(resources.getColor(R.color.aquamarine))
                        }
                    }
                }
            }
            genreContainer.addView(button)
        }
    }

    private fun selectGenre(genre: Genre) {
        currentGenre = genre
        titleText.text = if (genre.searchQuery.isEmpty()) "Все фильмы" else "Жанр: ${genre.name}"

        // Обновляем стили кнопок
        updateGenreButtons()

        // Загружаем фильмы выбранного жанра
        loadMoviesByGenre(genre)
    }

    private fun updateGenreButtons() {
        for (i in 0 until genreContainer.childCount) {
            val button = genreContainer.getChildAt(i) as Button
            val genre = Genres.genres[i]

            if (genre == currentGenre) {
                button.setBackgroundColor(android.graphics.Color.parseColor("#00cc00"))
            } else {
                button.setBackgroundColor(resources.getColor(R.color.aquamarine))
            }
        }
    }

    private fun loadMoviesByGenre(genre: Genre) {
        progressBar.visibility = View.VISIBLE


        val searchQuery = if (genre.searchQuery.isEmpty()) {
            "movie"
        } else {
            genre.searchQuery
        }

        lifecycleScope.launch {
            try {
                val response = ApiService.api.searchMovies(searchQuery)
                if (response.response == "True") {
                    movieList.clear()
                    response.search?.forEach { movieResult ->
                        val movie = Movie(
                            title = movieResult.title,
                            poster = if (movieResult.poster != "N/A") movieResult.poster else "https://via.placeholder.com/300x450/333333/FFFFFF?text=No+Image",
                            description = "Year: ${movieResult.year}",
                            year = movieResult.year,
                            imdbID = movieResult.imdbID
                        )
                        movieList.add(movie)
                    }
                    adapter.notifyDataSetChanged()

                    if (movieList.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Фильмы не найдены", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Фильмы не найдены: ${response.error}", Toast.LENGTH_SHORT).show()
                    loadSampleMovies()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                loadSampleMovies()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            // Скрываем клавиатуру
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(searchEditText.windowToken, 0)

            // Выполняем поиск
            loadMovies(query)
            titleText.text = "Результаты поиска: $query"
        } else {
            // Если поиск пустой, возвращаемся к текущему жанру
            loadMoviesByGenre(currentGenre)
            titleText.text = if (currentGenre.searchQuery.isEmpty()) "Все фильмы" else "Жанр: ${currentGenre.name}"
        }
    }

    private fun loadMovies(searchQuery: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiService.api.searchMovies(searchQuery)
                if (response.response == "True") {
                    movieList.clear()
                    response.search?.forEach { movieResult ->
                        val movie = Movie(
                            title = movieResult.title,
                            poster = if (movieResult.poster != "N/A") movieResult.poster else "https://via.placeholder.com/300x450/333333/FFFFFF?text=No+Image",
                            description = "Year: ${movieResult.year}",
                            year = movieResult.year,
                            imdbID = movieResult.imdbID
                        )
                        movieList.add(movie)
                    }
                    adapter.notifyDataSetChanged()

                    if (movieList.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Фильмы не найдены", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Фильмы не найдены: ${response.error}", Toast.LENGTH_SHORT).show()
                    loadSampleMovies()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                loadSampleMovies()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadSampleMovies() {
        movieList.clear()
        movieList.addAll(
            listOf(
                Movie("Фильм 1", "https://via.placeholder.com/300x450/FF6B6B/FFFFFF?text=Film+1", "Описание фильма 1", "2020", "tt1234567"),
                Movie("Фильм 2", "https://via.placeholder.com/300x450/4ECDC4/FFFFFF?text=Film+2", "Описание фильма 2", "2021", "tt2345678"),
                Movie("Фильм 3", "https://via.placeholder.com/300x450/45B7D1/FFFFFF?text=Film+3", "Описание фильма 3", "2022", "tt3456789")
            )
        )
        adapter.notifyDataSetChanged()
    }

    private inner class MovieAdapter(private val movies: List<Movie>) : BaseAdapter() {

        override fun getCount(): Int = movies.size

        override fun getItem(position: Int): Any = movies[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_movie, parent, false)

            val imageView = view.findViewById<ImageView>(R.id.movie_image)
            val textView = view.findViewById<TextView>(R.id.movie_title)
            val container = view.findViewById<LinearLayout>(R.id.movie_container)

            val movie = movies[position]
            textView.text = movie.title

            Glide.with(this@MainActivity)
                .load(movie.poster)
                .into(imageView)

            return view
        }
    }
}