package com.example.dave.ui.screens

import ApiService
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.dave.R
import com.example.dave.ui.theme.components.LevelMaintenance
import com.example.dave.ui.theme.components.ConfirmActionDialog
import com.example.dave.ui.theme.*
import com.example.dave.ui.components.NavBar
import com.example.dave.ui.components.DaveNavItem
import com.example.dave.ui.components.FailsafeAsyncImage

data class PlantDetail(
    val id: Int,
    val surname: String?,
    val commonName: String,
    val scientificName: List<String>,
    val family: String?,
    val type: String?,
    val imageUrl: String?,
    val careLevel: String?,
    val sunlight: List<String>?,
    val watering: String?,
    val indoor: Boolean?,
    val poisonousToHumans: Boolean?,
    val poisonousToPets: Boolean?,
    val droughtTolerant: Boolean?,
    val soil: List<String>?,
    val notes: String?
)

@Composable
fun PlantDetailScreen(
    plant: PlantDetail,
    onDeleteClick: () -> Unit = {},
    onModifyClick: (surname: String, notes: String) -> Unit = { _, _ ->},
    onHomeClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onAddPlantClick: () -> Unit = {},
    isAddMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val apiService = remember { ApiService() }

    // 1. On crée une liste d'état qui contiendra nos paires (ID, Nom)
    var apiPlantOptions by remember { mutableStateOf(listOf<Pair<Int, String>>()) }

    // 2. Appel API au chargement
    LaunchedEffect(Unit) {
        apiService.fetchPlantList { list ->
            apiPlantOptions = list
        }
    }
    var isAddMode by remember { mutableStateOf(isAddMode) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    val fallbackImagePainter = painterResource(id = R.drawable.heart_plant)
//    var editedSurname by remember { mutableStateOf(plant.surname ?: "") }
//    var editedNotes by remember { mutableStateOf(plant.notes ?: "") }
    var showDropdown by remember { mutableStateOf(false) }

    // On utilise remember(currentPlant) pour que les champs se vident/se remplissent quand on change de plante
    var currentPlant by remember { mutableStateOf(plant) }
    var editedSurname by remember(currentPlant) { mutableStateOf(currentPlant.surname ?: "") }
    var editedNotes by remember(currentPlant) { mutableStateOf(currentPlant.notes ?: "") }

    var selectedPlantName by remember(currentPlant) { mutableStateOf(currentPlant.commonName) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header avec nom de la plante
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (isAddMode) {
                    // Dropdown pour sélectionner une plante
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { showDropdown = !showDropdown },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.search_24dp),
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = selectedPlantName,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SulphurPoint
                            )
                        }
                        Icon(
                            painter = painterResource(
                                if (showDropdown) R.drawable.arrow_drop_up_24dp else R.drawable.arrow_drop_down_24dp
                            ),
                            contentDescription = "Toggle dropdown",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Liste déroulante
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        // On boucle sur la liste venant de l'API
                        apiPlantOptions.forEach { (id, name) ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = name, fontFamily = SulphurPoint, fontSize = 18.sp)
                                },
                                onClick = {
                                    showDropdown = false

                                    // APPEL API pour les détails
                                    apiService.fetchPlantDetails(id) { detailedPlant ->
                                        currentPlant = detailedPlant
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = selectedPlantName,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SulphurPoint
                    )
                }
            }

            // Image de la plante
            FailsafeAsyncImage(
                plant.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 32.dp,
                            bottomEnd = 32.dp
                        )
                    ),
                contentDescription = plant.commonName,
                fallbackImage = fallbackImagePainter
            )

            // Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditMode) {
                    OutlinedTextField(
                        value = editedSurname,
                        onValueChange = { editedSurname = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BrownPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = SulphurPoint,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Dark
                        )
                    )
                } else {
                    Text(
                        text = plant.surname ?: "No surname",
                        color = Dark,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SulphurPoint
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PlantInfoRow(
                    label = "Scientific name",
                    value = currentPlant.scientificName.firstOrNull() ?: "Unknown"
                )
                PlantInfoRow(label = "Family", value = currentPlant.family ?: "Unknown")
                PlantInfoRow(label = "Type", value = currentPlant.type ?: "Unknown")

                Spacer(modifier = Modifier.height(8.dp))

                // Section Maintenance
                SectionTitle(title = "Maintenance")
                PlantInfoRow(
                    label = "Soil",
                    value = currentPlant.soil?.joinToString(", ") ?: "Unknown"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Location : ",
                        color = BrownPrimary,
                        fontSize = 16.sp,
                        fontFamily = SulphurPoint
                    )
                    Text(
                        text = if (currentPlant.indoor == true) "Indoor" else "Outdoor",
                        fontSize = 16.sp,
                        fontFamily = SulphurPoint
                    )
                    Icon(
                        painter = painterResource(
                            id = if (currentPlant.indoor == true) {
                                R.drawable.house_24dp
                            } else {
                                R.drawable.nature_24dp
                            }
                        ),
                        contentDescription = if (currentPlant.indoor == true) "Indoor plant" else "Outdoor plant",
                        tint = BlueSoft,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LevelMaintenance(
                    watering = currentPlant.watering,
                    sunlight = currentPlant.sunlight,
                    careLevel = currentPlant.careLevel,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Section About me
                SectionTitle(title = "About me")

                // Drought tolerance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Drought tolerance : ",
                        color = BrownPrimary,
                        fontSize = 16.sp,
                        fontFamily = SulphurPoint
                    )
                    Text(
                        text = if (currentPlant.droughtTolerant == true) "high" else "low",
                        color = Dark,
                        fontSize = 16.sp,
                        fontFamily = SulphurPoint
                    )
                    repeat(if (currentPlant.droughtTolerant == true) 3 else 1) {
                        Text(
                            text = "+",
                            color = GreenPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Poisonous to humans
                PoisonousInfoRow(
                    iconRes = R.drawable.skull_24dp,
                    text = "I'm poisonous to humans",
                    isPoisonous = currentPlant.poisonousToHumans == true
                )

                // Poisonous to pets
                PoisonousInfoRow(
                    iconRes = R.drawable.pets_24dp,
                    text = "I'm poisonous to animals",
                    isPoisonous = currentPlant.poisonousToPets == true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Section Notes
                if (isEditMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueSoft, shape = RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Notes",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SulphurPoint
                            )
                            OutlinedTextField(
                                value = editedNotes,
                                onValueChange = { editedNotes = it },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = SulphurPoint,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                ),
                                minLines = 3
                            )
                        }
                    }
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BlueSoft, shape = RoundedCornerShape(12.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Notes",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SulphurPoint
                            )
                            Text(
                                text = "",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = SulphurPoint,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons Delete/Modify/Save or Add Plant
                if (isAddMode) {
                    val canAddPlant = selectedPlantName != "Plant name"
                    Button(
                        onClick = {
                            isAddMode = false
                            onAddPlantClick()
                        },
                        enabled = canAddPlant, // Le bouton s'active si canAddPlant est vrai
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            // Précision de la couleur du bouton quand il est désactivé
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.add),
                            contentDescription = "Add plant",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add plant",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = SulphurPoint
                        )
                    }
                } else if (isEditMode) {
                    Button(
                        onClick = { showSaveDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check_24dp),
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = SulphurPoint
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.delete_24dp),
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = SulphurPoint
                            )
                        }

                        Button(
                            onClick = {
                                isEditMode = true
                                // On se base sur currentPlant ici aussi
                                editedSurname = currentPlant.surname ?: ""
                                editedNotes = currentPlant.notes ?: ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.edit_24dp),
                                contentDescription = "Modify",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modify",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = SulphurPoint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(120.dp)) // Espace pour la navbar
            }
        }

        // NavBar en bas
        NavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onAddClick = onAddClick,
            onAccountClick = onAccountClick,
            selected = DaveNavItem.HOME
        )
    }

    // Dialog de confirmation pour Save
    if (showSaveDialog) {
        ConfirmActionDialog(
            title = "Save changes",
            message = "Are you sure you want to save the modifications to this plant?",
            confirmLabel = "Save",
            onConfirm = {
                onModifyClick(editedSurname, editedNotes)  // Call the callback with edited data
                isEditMode = false
                showSaveDialog = false
            },
            onDismiss = {
                showSaveDialog = false
            },
            isDelete = false
        )
    }

    // Dialog de confirmation pour Delete
    if (showDeleteDialog) {
        ConfirmActionDialog(
            title = "Delete plant",
            message = "Are you sure you want to delete this plant? This action cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            },
            onDismiss = {
                showDeleteDialog = false
            },
            isDelete = true
        )
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label :",
            color = BrownPrimary,
            fontSize = 16.sp,
            fontFamily = SulphurPoint
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BrownPrimary
            ),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = SulphurPoint,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
fun AsyncImage(
    model: String?,
    contentDescription: String,
    modifier: Modifier,
    contentScale: ContentScale
) {
    Box(
        modifier = modifier.background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Image",
            color = Color.DarkGray
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Dark,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = SulphurPoint
    )
}

@Composable
fun PlantInfoRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label : ",
            color = BrownPrimary,
            fontSize = 16.sp,
            fontFamily = SulphurPoint
        )

        Text(
            text = value,
            color = Dark,
            fontSize = 16.sp,
            fontFamily = SulphurPoint
        )
    }
}

@Composable
fun PoisonousInfoRow(
    iconRes: Int,
    text: String,
    isPoisonous: Boolean
) {
    if (isPoisonous) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = text,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Red)
            )
            Text(
                text = text,
                color = Dark,
                fontSize = 16.sp,
                fontFamily = SulphurPoint
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantDetailScreenPreview() {
    DaveTheme {
        PlantDetailScreen(
            plant = PlantDetail(
                id = 1,
                surname = "Mimi",
                commonName = "European Silver Fir",
                scientificName = listOf("Abies alba"),
                family = "Pinaceae",
                type = "Tree",
                imageUrl = null,
                careLevel = "high",
                sunlight = listOf("Part shade", "Full sun"),
                watering = "frequent",
                indoor = false,
                poisonousToHumans = true,
                poisonousToPets = false,
                droughtTolerant = true,
                soil = listOf("Rocky", "Dry", "Well-drained"),
                notes = "Amazing garden plant that is sure to capture attention. This beautiful tree is perfect for landscaping."
            )
        )
    }
}