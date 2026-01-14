package com.example.dave.ui.theme.components
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dave.R
import com.example.dave.ui.theme.Beige
import com.example.dave.ui.theme.BlueSoft
import com.example.dave.ui.theme.BrownPrimary
import com.example.dave.ui.theme.DaveTheme
import com.example.dave.ui.theme.GreenPrimary
import com.example.dave.ui.theme.SulphurPoint

@Composable
fun LevelMaintenance(
    watering: String? = null,
    sunlight: List<String>? = null,
    careLevel: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BrownPrimary, shape = RoundedCornerShape(50))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Watering
        MaintenanceItem(
            title = "Watering",
            iconResource = getWateringIcon(watering)
        )

        // Sunlight
        MaintenanceItem(
            title = "Sunlight",
            iconResource = getSunlightIcon(sunlight)
        )

        // Care
        MaintenanceItem(
            title = "Care",
            iconResource = getCareIcon(careLevel)
        )
    }
}

@Composable
private fun MaintenanceItem(
    title: String,
    @DrawableRes iconResource: Int
) {
    val iconColor = when (title.lowercase()) {
        "watering" -> BlueSoft
        "sunlight" -> Beige
        "care" -> GreenPrimary
        else -> Color.White
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = SulphurPoint
        )

        Image(
            painter = painterResource(id = iconResource),
            contentDescription = title,
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}

// Watering icons - 3 niveaux différents
@DrawableRes
private fun getWateringIcon(watering: String?): Int {
    return when (watering?.lowercase()) {
        "rare", "minimum" -> R.drawable.humidity_low_24dp // Arrosage rare
        "average", "moderate" -> R.drawable.humidity_mid_24dp // Arrosage modéré
        "frequent" -> R.drawable.humidity_high_24dp // Arrosage fréquent
        else -> R.drawable.humidity_mid_24dp
    }
}

// Sunlight icons - 3 niveaux différents
@DrawableRes
private fun getSunlightIcon(sunlight: List<String>?): Int {
    val sunlightLevel = getSunlightLevel(sunlight)
    return when (sunlightLevel?.lowercase()) {
        "low" -> R.drawable.brightness_empty_24dp // Ensoleillement faible
        "part shade" -> R.drawable.brightness_medium_24dp // Ensoleillement moyen
        "full" -> R.drawable.brightness_full_24dp // Ensoleillement fort
        else -> R.drawable.brightness_medium_24dp
    }
}

// Care level icons - 3 niveaux différents
@DrawableRes
private fun getCareIcon(careLevel: String?): Int {
    return when (careLevel?.lowercase()) {
        "high", "difficult" -> R.drawable.heart_plus_24dp // Coeur + = Difficile
        "medium" -> R.drawable.heart_medium_24dp // Coeur vide = Medium
        "easy", "low" -> R.drawable.heart_minus_24dp // Coeur - = Facile
        else -> R.drawable.heart_medium_24dp
    }
}

private fun getSunlightLevel(sunlight: List<String>?): String {
    if (sunlight.isNullOrEmpty()) return "medium"

    return when {
        sunlight.any { it.contains("full sun", ignoreCase = true) } -> "high"
        sunlight.any { it.contains("part", ignoreCase = true) } -> "medium"
        sunlight.any { it.contains("shade", ignoreCase = true) } -> "low"
        else -> "medium"
    }
}

@Preview(showBackground = true)
@Composable
fun LevelMaintenancePreview() {
    DaveTheme {
        LevelMaintenance(
            watering = "average",
            sunlight = listOf("full sun", "partial shade"),
            careLevel = "medium"
        )
    }
}
