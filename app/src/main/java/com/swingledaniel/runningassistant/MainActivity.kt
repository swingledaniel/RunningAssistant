package com.swingledaniel.runningassistant

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.*
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.roundToInt

const val units = "imperial"

const val REQUEST_LOCATION = 99

@SuppressLint("DefaultLocale")
fun capitalize(text: String): String {
    return text.split(' ').joinToString(" ") { it.capitalize() }
}

fun getBitmapFromURL(src: String): Bitmap {
    return BitmapFactory.decodeStream(URL(src).openStream())
}

fun getWeatherIcon(code: String): Bitmap {
    return getBitmapFromURL("https://openweathermap.org/img/wn/$code@2x.png")
}

class MainActivity : AppCompatActivity() {

    private val gson = Gson()

    private fun getWeatherData(latitude: Double, longitude: Double): OpenWeatherMap {
        val response = URL("https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&appid=${getString(R.string.OWM_api_key)}&units=${units}")
            .readText()

        return gson.fromJson(response, OpenWeatherMap::class.java)
    }


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var temp: TextView
    private lateinit var feelsLike: TextView
    private lateinit var description: TextView
    private lateinit var humidity: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var locationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temp = findViewById(R.id.temp)
        feelsLike = findViewById(R.id.feels_like)
        description = findViewById(R.id.description)
        humidity = findViewById(R.id.humidity)
        weatherIcon = findViewById(R.id.weatherIcon)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationButton = findViewById<Button>(R.id.location_button)
        locationButton.setOnClickListener {
            checkLocationPermission()
        }

        val searchBar =
            findViewById<AutoCompleteTextView>(R.id.autoCompleteSearch)

        var coordinateList = listOf<Coordinates>()
        val searchBarAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line), Filterable {

            private var resultList = listOf<String>()

            // TODO: change to user location
            private val locationHint = Coordinates(29.708090, -95.816380)

            private fun autocomplete(constraint: CharSequence?): List<String> {
                val response = URL("https://photon.komoot.de/api/?q=$constraint&lat=${locationHint.lat}&lon=${locationHint.lon}")
                    .readText()

                val geoJSON = gson.fromJson(response, GeoJSON::class.java)
                coordinateList = geoJSON.features.map {
                    Coordinates(it.geometry.coordinates.last(), it.geometry.coordinates.first())
                }

                return geoJSON.features.map {
                    "${it.properties.name}, ${it.properties.state}, ${it.properties.country}"
                }
            }

            override fun getCount(): Int {
                return resultList.size
            }

            override fun getItem(position: Int): String? {
                return resultList.get(position)
            }

            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val filterResults = FilterResults()
                        if (constraint != null) {
                            resultList = autocomplete(constraint)

                            filterResults.values = resultList
                            filterResults.count = resultList.size
                        }
                        return filterResults
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        if (results != null && results.count > 0) {
                            notifyDataSetChanged()
                        } else {
                            notifyDataSetInvalidated()
                        }
                    }

                }
            }
        }

        searchBar.setAdapter(searchBarAdapter)
        searchBar.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.applicationWindowToken, 0)
            loadWeatherData(coordinateList.get(position).lat, coordinateList.get(position).lon)
        }

        searchBar.setOnClickListener { searchBar.setText("") }
    }

    @SuppressLint("SetTextI18n")
    private fun loadWeatherData(lat: Double, lon: Double) {
        GlobalScope.launch(Dispatchers.IO) {
            val weatherData = getWeatherData(lat, lon)
            val icon = getWeatherIcon(weatherData.weather[0].icon)
            withContext(Dispatchers.Main) {
                temp.text = weatherData.main.temp.roundToInt().toString()
                feelsLike.text = weatherData.main.feels_like.roundToInt().toString()
                description.text = capitalize(weatherData.weather[0].description)
                humidity.text = "${weatherData.main.humidity}%"
                weatherIcon.setImageBitmap(icon)
                locationButton.text = weatherData.name
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
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
        grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.lastLocation?.addOnCompleteListener {
                            when (val loc = it.result) {
                                is Location -> {
                                    loadWeatherData(loc.latitude, loc.longitude)
                                }
                            }
                        }
                    } else {
                        println("swing else")
                    }

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}














