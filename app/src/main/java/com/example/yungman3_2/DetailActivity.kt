package com.example.yungman3_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleView: TextView
    private lateinit var yearView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        imageView = findViewById(R.id.detail_image)
        titleView = findViewById(R.id.detail_title)
        yearView = findViewById(R.id.detail_year)
        descriptionView = findViewById(R.id.detail_description)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)

        val title = intent.getStringExtra("title") ?: ""
        val poster = intent.getStringExtra("poster") ?: ""
        val year = intent.getStringExtra("year") ?: ""
        val imdbID = intent.getStringExtra("imdbID") ?: ""

        titleView.text = title
        yearView.text = "Год: $year"
        descriptionView.text = "Загрузка описания..."

        // Показываем прогресс бар
        progressBar.visibility = View.VISIBLE

        // Загружаем изображение
        Glide.with(this)
            .load(poster)
            .into(imageView)

        // Настраиваем кнопку назад
        setupBackButton()

        // Если imdbID не пустой, загружаем детали фильма
        if (imdbID.isNotEmpty()) {
            loadMovieDetails(imdbID)
        } else {
            descriptionView.text = "Описание недоступно"
            progressBar.visibility = View.GONE
        }
    }

    private fun loadMovieDetails(imdbID: String) {
        lifecycleScope.launch {
            try {
                val response = ApiService.api.getMovieDetails(imdbID)
                if (response.response == "True") {
                    // Успешно получили детали
                    val plot = if (response.plot != "N/A") response.plot else "Описание отсутствует"
                    descriptionView.text = plot
                } else {
                    descriptionView.text = "Описание недоступно"
                }
            } catch (e: Exception) {
                descriptionView.text = "Ошибка загрузки описания: ${e.message}"
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }

        backButton.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                backButton.setBackgroundColor(android.graphics.Color.parseColor("#00cc00"))
            } else {
                backButton.setBackgroundColor(android.graphics.Color.parseColor("#00ff00"))
            }
        }
    }
}