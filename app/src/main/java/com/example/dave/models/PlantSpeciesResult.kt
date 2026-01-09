package com.example.dave.models

/*
Class to handle the results of the plant API (https://perenual.com/).
There's a lot more data available for each specie but we only need these.
 */
data class PlantSpeciesResult(
    val id: Number,
    val commonName: String,
)
