package com.example.sunnyweather.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeather(weather)
            } else {
                Toast.makeText(this, "无法获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.swipeRefresh.isRefreshing = false
        })
        binding.swipeRefresh.setColorSchemeColors(R.color.design_default_color_primary)
        refreshWeather()
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        binding.weatherNowLayout.navBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            @SuppressLint("ServiceCast")
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        })
    }

    fun closeDrawer() {
        binding.drawerLayout.closeDrawers()
    }

    fun refreshWeather() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
    }

    private fun showWeather(weather: Weather) {
        binding.weatherNowLayout.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // now
        binding.weatherNowLayout.currentTemp.text = "${realtime.temperature.toInt()} ºC"
        binding.weatherNowLayout.currentSky.text = getSky(realtime.skycon).info
        binding.weatherNowLayout.currentAQI.text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.weatherNowLayout.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //forecast
        binding.weatherForecastLayout.forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temprature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.weatherForecastLayout.forecastLayout, false)
            val dataInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dataInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            temperatureInfo.text = "${temprature.min.toInt()} ~ ${temprature.max.toInt()}"
            binding.weatherForecastLayout.forecastLayout.addView(view)
        }

        //life_index
        val lifeIndex = daily.lifeIndex
        binding.weatherLifeIndexLayout.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.weatherLifeIndexLayout.dressingText.text = lifeIndex.dressing[0].desc
        binding.weatherLifeIndexLayout.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.weatherLifeIndexLayout.carWashingText.text = lifeIndex.carWashing[0].desc
        binding.weatherLayout.visibility = View.VISIBLE
    }
}