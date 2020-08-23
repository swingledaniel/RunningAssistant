package com.swingledaniel.runningassistant

import com.google.gson.annotations.SerializedName

// API documentation:
// https://openweathermap.org/api/one-call-api

data class FeelsLike (
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class Temp (
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class Daily (
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Temp,
    val feels_like: FeelsLike,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<Weather>,
    val clouds: Int,
    val pop: Double,
    val rain: Double,
    val uvi: Double
)

data class Hourly (
    val dt: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<Weather>,
    val pop: Double,
    val rain: Rain
)

data class Rain (
    @SerializedName("1h") val one_h: Double
)

data class Weather (
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Current (
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Double,
    val feels_like: Double,
    val pressure: Int,
    val humidity: Int,
    val dew_point: Double,
    val uvi: Double,
    val clouds: Int,
    val visibility: Int,
    val wind_speed: Double,
    val wind_deg: Int,
    val weather: List<Weather>,
    val pop: Double,
    val rain: Rain
)

data class Forecast (
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: Int,
    val current: Current,
    val hourly: List<Hourly>,
    val daily: List<Daily>
)
