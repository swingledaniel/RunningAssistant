package com.swingledaniel.runningassistant

// API documentation:
// https://openweathermap.org/current

data class Coordinates (
    val lat: Double,
    val lon: Double
)

data class Weather (
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class OpenWeatherMain (
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind (
    val speed: Double,
    val deg: Int
)

data class Clouds (
    val all: Int
)

data class Sys (
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Int,
    val sunset: Int
)

data class OpenWeatherMap (
    val coord: Coordinates,
    val weather: List<Weather>,
    val base: String,
    val main: OpenWeatherMain,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
)