package com.swingledaniel.runningassistant

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.google.gson.Gson
import java.net.URL

data class Coordinates (
    val lat: Double,
    val lon: Double
)

class LocationSearchAdapter(context: Context, resource: Int) : ArrayAdapter<String>(context, resource),
    Filterable {

    internal var coordinateList = listOf<Coordinates>()
    private var resultList = listOf<String>()
    private val gson = Gson()

    // TODO: determine whether to provide location bias to Photon url in autocomplete
    // private val locationHint = Coordinates(lat, lon)
    // &lat=${locationHint.lat}&lon=${locationHint.lon}"

    private fun autocomplete(constraint: CharSequence?): List<String> {
        val response = URL("https://photon.komoot.de/api/?q=$constraint")
            .readText()

        val geoJSON = gson.fromJson(response, GeoJSON::class.java)
        coordinateList = geoJSON.features.map {
            Coordinates(it.geometry.coordinates.last(), it.geometry.coordinates.first())
        }

        return listOf(context.getString(R.string.current_location)) + geoJSON.features.map {
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
                resultList = if (constraint == null)
                    listOf(context.getString(R.string.current_location))
                else autocomplete(constraint)

                filterResults.values = resultList
                filterResults.count = resultList.size
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