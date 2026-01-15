package com.example.dave.ui.components
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.example.dave.ui.theme.DaveTheme
import com.example.dave.ui.theme.SulphurPoint


@Composable
fun LevelMaintenanceSmallDisplay(
    watering: String? = null,
    sunlight: List<String>? = null,
    careLevel: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp), // Even smaller padding
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Watering - icon only, no text
        MaintenanceIcon(
            iconResource = getWateringIcon(watering),
            contentDescription = "Watering: $watering",
            color = MaterialTheme.colorScheme.secondary
        )

        // Sunlight - icon only, no text
        MaintenanceIcon(
            iconResource = getSunlightIcon(sunlight),
            contentDescription = "Sunlight: ${sunlight?.joinToString()}",
            color = MaterialTheme.colorScheme.surface
        )

        // Care - icon only, no text
        MaintenanceIcon(
            iconResource = getCareIcon(careLevel),
            contentDescription = "Care: $careLevel",
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MaintenanceIcon(
    @DrawableRes iconResource: Int,
    contentDescription: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = iconResource),
        contentDescription = contentDescription,
        modifier = modifier.size(16.dp), // Small icon
        colorFilter = ColorFilter.tint(color)
    )
}

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
            .background(
                MaterialTheme.colorScheme.tertiary, // Use theme color instead of BrownPrimary
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp), // Reduced padding
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Watering
        MaintenanceItem(
            title = "Watering",
            iconResource = getWateringIcon(watering),
            modifier = Modifier.weight(1f) // Distribute space evenly
        )

        Spacer(modifier = Modifier.width(4.dp)) // Smaller spacer

        // Sunlight
        MaintenanceItem(
            title = "Sunlight",
            iconResource = getSunlightIcon(sunlight),
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Care
        MaintenanceItem(
            title = "Care",
            iconResource = getCareIcon(careLevel),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MaintenanceItem(
    title: String,
    @DrawableRes iconResource: Int,
    modifier: Modifier = Modifier
) {
    val iconColor = when (title.lowercase()) {
        "watering" -> MaterialTheme.colorScheme.secondary // Use theme color
        "sunlight" -> MaterialTheme.colorScheme.surface
        "care" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onTertiary, // Use theme contrast color
            fontSize = 12.sp, // Smaller font size
            fontWeight = FontWeight.Medium,
            fontFamily = SulphurPoint
        )

        Image(
            painter = painterResource(id = iconResource),
            contentDescription = title,
            modifier = Modifier.size(20.dp), // Smaller icon
            colorFilter = ColorFilter.tint(iconColor)
        )
    }
}

// Watering icons - 3 niveaux différents
@DrawableRes
private fun getWateringIcon(watering: String?): Int {
    return when (watering?.lowercase()) {
        "rare", "minimum" -> R.drawable.humidity_low_24dp
        "average", "moderate" -> R.drawable.humidity_mid_24dp
        "frequent" -> R.drawable.humidity_high_24dp
        else -> R.drawable.humidity_mid_24dp
    }
}

// Sunlight icons - 3 niveaux différents
@DrawableRes
private fun getSunlightIcon(sunlight: List<String>?): Int {
    val sunlightLevel = getSunlightLevel(sunlight)
    return when (sunlightLevel?.lowercase()) {
        "low" -> R.drawable.brightness_empty_24dp
        "part shade" -> R.drawable.brightness_medium_24dp
        "full" -> R.drawable.brightness_full_24dp
        else -> R.drawable.brightness_medium_24dp
    }
}

// Care level icons - 3 niveaux différents
@DrawableRes
private fun getCareIcon(careLevel: String?): Int {
    return when (careLevel?.lowercase()) {
        "high", "difficult" -> R.drawable.heart_plus_24dp
        "medium" -> R.drawable.heart_medium_24dp
        "easy", "low" -> R.drawable.heart_minus_24dp
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
            careLevel = "medium",
            modifier = Modifier.width(200.dp) // Test with constrained width
        )
    }
}

@Preview(showBackground = true, widthDp = 150)
@Composable
fun LevelMaintenanceSmallPreview() {
    DaveTheme {
        LevelMaintenance(
            watering = "average",
            sunlight = listOf("full sun"),
            careLevel = "easy",
            modifier = Modifier.fillMaxWidth()
        )
    }
}