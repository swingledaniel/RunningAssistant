package com.swingledaniel.runningassistant

// Photon API:
// https://photon.komoot.de

data class Geometry (
    val coordinates: List<Double>
)

data class Properties (
    val osm_id: Long,
    val osm_type: String,
    val extent: List<Double>,
    val country: String,
    val osm_key: String,
    val osm_value: String,
    val postcode: String,
    val name: String,
    val state: String
)

data class Feature (
    val geometry: Geometry,
    val type: String,
    val properties: Properties
)

data class GeoJSON (
    val features: List<Feature>,
    val type: String
)
