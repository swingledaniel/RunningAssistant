package com.swingledaniel.runningassistant

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.time.Instant
import java.time.ZoneId

val headers = arrayOf("Time", "Temperature", "Humidity")
val numColumns = headers.size

class ForecastAdapter(private val context: Context, private val sharedPref: SharedPreferences) : BaseAdapter() {

    val hourly: MutableList<Hourly> = mutableListOf()

    override fun getCount(): Int {
        return hourly.size * 2
    }

    override fun getItem(position: Int): Any {
        return hourly[position / 2]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = if (convertView == null) TextView(context) else convertView as TextView

        if (position < numColumns) {

            textView.text = headers[position]
            textView.setTypeface(null, Typeface.BOLD)

        } else {
            val units = sharedPref.getString("unit", "imperial")
            val degreeSymbol = if (units.equals("imperial")) "℉" else "℃"

            val row = position / numColumns

            textView.text = when (position % numColumns) {
                0 -> {
                    val time = Instant.ofEpochSecond(hourly[row].dt).atZone(ZoneId.systemDefault())
                    val hour = time.hour
                    val minute = time.minute
                    "$hour:${if (minute > 9) {minute} else {
                        "0$minute"}}"
                }
                1 -> "${hourly[row].temp}$degreeSymbol"
                2 -> "${hourly[row].humidity}%"
                else -> "-"
            }
            textView.setTypeface(null, Typeface.NORMAL)
        }

        return textView
    }
}
