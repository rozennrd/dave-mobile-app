package com.example.dave.ui.components

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dave.R
import com.example.dave.models.Plant
import com.example.dave.ui.theme.DaveTheme

// State sealed class for loading/empty/success
sealed class PlantListState {
    object Loading : PlantListState()
    data class Success(val plants: List<Plant>) : PlantListState()
    object Empty : PlantListState()
    data class Error(val message: String) : PlantListState()
}

@Composable
fun PlantList(
    state: PlantListState,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    gutter: Dp = 12.dp,
    padding: Dp = 12.dp,
    onPlantClick: (Plant) -> Unit = {}
) {
    when (state) {
        is PlantListState.Loading -> {
            LoadingState(modifier = modifier)
        }

        is PlantListState.Success -> {
            if (state.plants.isEmpty()) {
                EmptyState(modifier = modifier)
            } else {
                GridState(
                    plants = state.plants,
                    modifier = modifier,
                    columns = columns,
                    gutter = gutter,
                    padding = padding,
                    onPlantClick = onPlantClick
                )
            }
        }

        is PlantListState.Empty -> {
            EmptyState(modifier = modifier)
        }

        is PlantListState.Error -> {
            ErrorState(
                message = state.message,
                modifier = modifier
            )
        }
    }
}

// Grid display when plants are loaded
@Composable
private fun GridState(
    plants: List<Plant>,
    modifier: Modifier,
    columns: Int,
    gutter: Dp,
    padding: Dp,
    onPlantClick: (Plant) -> Unit
) {
    // In PlantList:
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // ← Gutter between
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(12.dp), // ← Padding around grid
        modifier = Modifier.fillMaxSize()
    ) {
        items(plants) { plant ->
            PlantCard(
                plant = plant,
                modifier = Modifier.fillMaxWidth() // Takes 50% minus half gutter
            )
        }
    }
}

// Loading state
@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading plants...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Empty state
@Composable
private fun EmptyState(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.heart_plant), // Your plant icon
                contentDescription = "No plants",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No plants found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add some plants to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Error state
@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_error), // Add error icon
//                contentDescription = "Error",
//                modifier = Modifier.size(64.dp),
//                tint = MaterialTheme.colorScheme.error
//            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun PlantListLoadingPreview() {
    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(state = PlantListState.Loading)
        }
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
fun PlantListEmptyPreview() {
    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(state = PlantListState.Empty)
        }
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun PlantListErrorPreview() {
    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(state = PlantListState.Error("Failed to load plants. Check your connection."))
        }
    }
}

@Preview(showBackground = true, name = "Grid - 5 Plants")
@Composable
fun PlantListSuccessPreview() {
    val samplePlants = listOf(
        Plant(
            id = 1,
            commonName = "Pyramidalis Silver Fir",
            scientificName = listOf("Abies alba 'Pyramidalis'"),
            plantName = "Silver Fir",
            family = "Pinaceae",
            type = "tree",
            imageUrl = "https://example.com/fir.jpg",
            careLevel = "Average",
            sunlight = listOf("full sun", "part shade"),
            watering = "Medium",
            indoor = false,
            poisonousToHumans = false,
            poisonousToPets = false,
            droughtTolerant = true,
            soil = listOf("Sandy", "Loamy", "Clay"),
            notes = "Evergreen conifer with pyramidal shape"
        ),
        Plant(
            id = 2,
            commonName = "Peace Lily",
            scientificName = listOf("Spathiphyllum wallisii"),
            plantName = "Peace Lily",
            family = "Araceae",
            type = "flowering",
            imageUrl = "https://example.com/peace-lily.jpg",
            careLevel = "Easy",
            sunlight = listOf("low light", "indirect light"),
            watering = "Frequent",
            indoor = true,
            poisonousToHumans = true,
            poisonousToPets = true,
            droughtTolerant = false,
            soil = listOf("Peat-based", "Well-draining"),
            notes = "Great for purifying indoor air"
        ),
        Plant(
            id = 3,
            commonName = "Snake Plant",
            scientificName = listOf("Sansevieria trifasciata", "Dracaena trifasciata"),
            plantName = "Mother-in-law's Tongue",
            family = "Asparagaceae",
            type = "succulent",
            imageUrl = "https://example.com/snake-plant.jpg",
            careLevel = "Very Easy",
            sunlight = listOf("low light", "bright indirect"),
            watering = "Low",
            indoor = true,
            poisonousToHumans = false,
            poisonousToPets = true,
            droughtTolerant = true,
            soil = listOf("Sandy", "Cactus mix"),
            notes = "Nearly indestructible, great for beginners"
        ),
        Plant(
            id = 4,
            commonName = "English Lavender",
            scientificName = listOf("Lavandula angustifolia"),
            plantName = "Lavender",
            family = "Lamiaceae",
            type = "herb",
            imageUrl = "https://example.com/lavender.jpg",
            careLevel = "Medium",
            sunlight = listOf("full sun"),
            watering = "Low",
            indoor = false,
            poisonousToHumans = false,
            poisonousToPets = false,
            droughtTolerant = true,
            soil = listOf("Sandy", "Well-draining"),
            notes = "Fragrant flowers, attracts pollinators"
        ),
        Plant(
            id = 5,
            commonName = "Golden Pothos",
            scientificName = listOf("Epipremnum aureum"),
            plantName = "Devil's Ivy",
            family = "Araceae",
            type = "vine",
            imageUrl = "https://example.com/pothos.jpg",
            careLevel = "Easy",
            sunlight = listOf("low light", "indirect light"),
            watering = "Medium",
            indoor = true,
            poisonousToHumans = true,
            poisonousToPets = true,
            droughtTolerant = true,
            soil = listOf("General potting mix"),
            notes = "Fast-growing trailing plant"
        )
    )

    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(
                state = PlantListState.Success(samplePlants),
                onPlantClick = { println("Clicked: ${it.commonName}") }
            )
        }
    }
}

@Preview(showBackground = true, name = "Single Plant Preview")
@Composable
fun OnePlantCardPreview() {
    val samplePlant = Plant(
        id = 1,
        commonName = "Pyramidalis Silver Fir",
        scientificName = listOf("Abies alba 'Pyramidalis'"),
        plantName = "Silver Fir",
        family = "Pinaceae",
        type = "tree",
        imageUrl = "",
        careLevel = "Average",
        sunlight = listOf("full sun", "part shade"),
        watering = "Medium",
        indoor = false,
        poisonousToHumans = false,
        poisonousToPets = false,
        droughtTolerant = true,
        soil = listOf("Sandy", "Loamy", "Clay"),
        notes = "Evergreen conifer with pyramidal shape"
    )

    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(
                state = PlantListState.Success(listOf(samplePlant)),
                onPlantClick = { println("Clicked: ${it.commonName}") }
            )
        }
    }
}

@Preview(showBackground = true, name = "Grid - 2x2 Plants", widthDp = 360, heightDp = 640)
@Composable
fun PlantListGridPreview() {
    val samplePlants = listOf(
        Plant(
            id = 1,
            commonName = "Silver Fir",
            scientificName = listOf("Abies alba"),
            plantName = "Fir Tree",
            family = "Pinaceae",
            type = "tree",
            imageUrl = "",
            careLevel = "Medium",
            sunlight = listOf("full sun"),
            watering = "Regular",
            indoor = false,
            poisonousToHumans = false,
            poisonousToPets = false,
            droughtTolerant = false,
            soil = listOf("Loamy"),
            notes = "Tall evergreen"
        ),
        Plant(
            id = 2,
            commonName = "Peace Lily",
            scientificName = listOf("Spathiphyllum"),
            plantName = "Lily",
            family = "Araceae",
            type = "flowering",
            imageUrl = "",
            careLevel = "Easy",
            sunlight = listOf("shade"),
            watering = "Frequent",
            indoor = true,
            poisonousToHumans = true,
            poisonousToPets = true,
            droughtTolerant = false,
            soil = listOf("Potting mix"),
            notes = "Indoor plant"
        ),
        Plant(
            id = 3,
            commonName = "Snake Plant",
            scientificName = listOf("Sansevieria"),
            plantName = "Snake",
            family = "Asparagaceae",
            type = "succulent",
            imageUrl = "",
            careLevel = "Very Easy",
            sunlight = listOf("any light"),
            watering = "Low",
            indoor = true,
            poisonousToHumans = false,
            poisonousToPets = true,
            droughtTolerant = true,
            soil = listOf("Cactus mix"),
            notes = "Hardy indoor"
        ),
        Plant(
            id = 4,
            commonName = "Lavender",
            scientificName = listOf("Lavandula"),
            plantName = "Lavender",
            family = "Lamiaceae",
            type = "herb",
            imageUrl = "",
            careLevel = "Medium",
            sunlight = listOf("full sun"),
            watering = "Low",
            indoor = false,
            poisonousToHumans = false,
            poisonousToPets = false,
            droughtTolerant = true,
            soil = listOf("Sandy"),
            notes = "Fragrant herb"
        )
    )

    DaveTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PlantList(
                state = PlantListState.Success(samplePlants),
                columns = 2
            )
        }
    }
}