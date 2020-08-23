package com.swingledaniel.runningassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


@SuppressLint("DefaultLocale")
fun capitalize(text: String): String {
    return text.split(' ').joinToString(" ") { it.capitalize() }
}

fun getBitmapFromURL(src: String): Bitmap {
    return BitmapFactory.decodeStream(URL(src).openStream())
}

fun getWeatherIcon(code: String): Bitmap {
    return getBitmapFromURL("https://openweathermap.org/img/wn/$code@2x.png").scale(256, 256)
}

class MainActivity : AppCompatActivity() {

    private val requestLocationCode = 99
    private val gson = Gson()

    private lateinit var sharedPref: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var currentWeather: TextView

    private lateinit var forecastGrid: GridView
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        currentWeather = findViewById(R.id.currentWeather)

        val searchBar =
            findViewById<LocationAutoTextView>(R.id.autoCompleteSearch)

        val searchBarAdapter = LocationSearchAdapter(this, android.R.layout.simple_dropdown_item_1line)

        searchBar.setAdapter(searchBarAdapter)
        searchBar.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                view.applicationWindowToken,
                0
            )
            if (position == 0) {
                checkLocationPermission()
            } else {
                loadWeatherData(searchBarAdapter.coordinateList[position-1].lat, searchBarAdapter.coordinateList[position-1].lon)
            }
        }
        searchBar.setOnClickListener { searchBar.setText("") }

        forecastGrid = findViewById(R.id.forecast_grid)
        forecastAdapter = ForecastAdapter(this, sharedPref)

        forecastGrid.adapter = forecastAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getForecast(latitude: Double, longitude: Double, units: String?, lang: String?): Forecast {
        val response = URL(
            "https://api.openweathermap.org/data/2.5/onecall?lat=${latitude}&lon=${longitude}&exclude=minutely&appid=${getString(R.string.OWM_api_key)}&units=$units&lang=$lang").readText()

        return gson.fromJson(response, Forecast::class.java)
    }

    @SuppressLint("SetTextI18n")
    private fun loadWeatherData(lat: Double, lon: Double) {
        GlobalScope.launch(Dispatchers.IO) {
            val lang = sharedPref.getString("lang", "en")
            val units = sharedPref.getString("unit", "imperial")

            val forecast = getForecast(lat, lon, units, lang)
            val icon = getWeatherIcon(forecast.current.weather[0].icon).toDrawable(resources)
            withContext(Dispatchers.Main) {
                currentWeather.text = capitalize(forecast.current.weather[0].description)
                currentWeather.setCompoundDrawablesRelativeWithIntrinsicBounds(null, icon, null, null)

                forecastAdapter.hourly.clear()
                forecastAdapter.hourly.addAll(forecast.hourly)
                forecastAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestLocationCode
            )
        } else {
            fusedLocationClient.lastLocation?.addOnCompleteListener {
                when (val loc = it.result) {
                    is Location -> {
                        loadWeatherData(loc.latitude, loc.longitude)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestLocationCode -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient.lastLocation?.addOnCompleteListener {
                            when (val loc = it.result) {
                                is Location -> {
                                    loadWeatherData(loc.latitude, loc.longitude)
                                }
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}














