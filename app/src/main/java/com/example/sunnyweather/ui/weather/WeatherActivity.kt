package com.example.sunnyweather.ui.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityWeatherBinding

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}