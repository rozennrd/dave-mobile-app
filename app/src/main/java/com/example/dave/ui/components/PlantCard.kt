package com.example.dave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dave.R
import com.example.dave.models.Plant
import com.example.dave.ui.theme.DaveTheme
import com.example.dave.ui.theme.components.LevelMaintenanceSmallDisplay

@Composable
fun PlantCard(plant: Plant, onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val fallbackImagePainter = painterResource(id = R.drawable.heart_plant)
    // Your card implementation here
    Card(
        modifier = Modifier
            .widthIn(max = 180.dp) // Max width
            .fillMaxWidth(0.9f)     // But responsive
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10)
                )
                .fillMaxWidth()
                .clip(RoundedCornerShape(10))
        ) {
            Row(
                Modifier.background(MaterialTheme.colorScheme.primary).padding(12.dp).fillMaxWidth()
            ) {
                Text(text = plant.commonName, color = MaterialTheme.colorScheme.onPrimary)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.5f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,     // Top left stays square
                            topEnd = 0.dp,       // Top right stays square
                            bottomStart = 50.dp, // Bottom left rounded
                            bottomEnd = 50.dp    // Bottom right rounded
                        )
                    )

            ) {
                FailsafeAsyncImage(
                    url = plant.imageUrl,
                    fallbackImage = fallbackImagePainter,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(Modifier.padding(12.dp)) {
                Text(
                    text = plant.plantName ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    plant.scientificName[0],
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
                Text(plant.family ?: "", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                LevelMaintenanceSmallDisplay(
                    watering = plant.watering,
                    sunlight = plant.sunlight,
                    careLevel = plant.careLevel
                )
            }
        }
    }
}


@Preview(showBackground = true,
    device = "spec:width=500px,height=700px,dpi=440"
)
@Composable
fun PlantCardPreview() {
    DaveTheme {
        PlantCard(
            plant = Plant(
            "1",
                "Pyramidalis Silver Fir",
                listOf("Abies alba 'Pyramidalis'"),
                "Mélissa",
                "Pinaceae",
                "tree",
                "https://s3.us-central-1.wasabisys.com/perenual/species_image/25_acer_negundo_flamingo/regular/5867345385_a9dff5bee7_b.jpg?X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=0MPGHU7CIPXNPMVWMXUW%2F20260109%2Fus-central-1%2Fs3%2Faws4_request&X-Amz-Date=20260109T084422Z&X-Amz-SignedHeaders=host&X-Amz-Expires=3600&X-Amz-Signature=7951294b8a7c33cbe995829f0c34e3eed183a420064d7a69cc7d40b96e29d49a",
                "Medium",
                listOf("full sun", "part sun/part shade"),
                "Average",
                false,
                true,
                true,
                true,
                listOf("Sandy Loamy Clay"),
                ""
            )
        )
    }
}