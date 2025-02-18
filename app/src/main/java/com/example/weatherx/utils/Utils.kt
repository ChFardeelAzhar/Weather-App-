package com.example.weatherx.utils

import com.example.weatherx.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getBackgroundForCondition(condition: String): Int {
    return when {

        condition.contains("Sunny", ignoreCase = true) ||
                condition.contains("Fair", ignoreCase = true) ||  // Added "Fair"
                condition.contains("Clear", ignoreCase = true) -> R.drawable.sunny_bg

        condition.contains("Rain", ignoreCase = true) ||
                condition.contains("Drizzle", ignoreCase = true) ||
                condition.contains("Showers", ignoreCase = true) -> R.drawable.rainy_bg

        condition.contains("Snow", ignoreCase = true) ||
                condition.contains("Sleet", ignoreCase = true) ||
                condition.contains("Blizzard", ignoreCase = true) -> R.drawable.snowy_bg

        condition.contains("Cloudy", ignoreCase = true) ||
                condition.contains("Overcast", ignoreCase = true) ||
                condition.contains("Mist", ignoreCase = true) ||
                condition.contains("Fog", ignoreCase = true) -> R.drawable.cloudy_bg

        else -> R.drawable.cloudy_bg // Default if no match
    }
}

fun getCurrentIcon(condition: String): Int {
    return when {

        condition.contains("Sunny", ignoreCase = true) ||
                condition.contains("Fair", ignoreCase = true) ||  // Added "Fair"
                condition.contains("Clear", ignoreCase = true) -> R.drawable.single_sun

        condition.contains("Rain", ignoreCase = true) ||
                condition.contains("Drizzle", ignoreCase = true) ||
                condition.contains("Showers", ignoreCase = true) -> R.drawable.single_rain_cloud

        condition.contains("Snow", ignoreCase = true) ||
                condition.contains("Sleet", ignoreCase = true) ||
                condition.contains("Blizzard", ignoreCase = true) -> R.drawable.single_cloud

        condition.contains("Cloudy", ignoreCase = true) ||
                condition.contains("Overcast", ignoreCase = true) ||
                condition.contains("Mist", ignoreCase = true) ||
                condition.contains("Fog", ignoreCase = true) -> R.drawable.single_cloud

        else -> R.drawable.single_cloud // Default if no match
    }
}


fun formatDateTime(inputDate: String): Pair<String, String> {
    // Parse the input date
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val date = inputFormat.parse(inputDate)

    // Format date to "20 OCT 2025"
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val formattedDate = dateFormat.format(date ?: Date())

    // Format time to "09:10 PM"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    val formattedTime = timeFormat.format(date ?: Date())

    return Pair(formattedDate.uppercase(Locale.ENGLISH), formattedTime)

}

