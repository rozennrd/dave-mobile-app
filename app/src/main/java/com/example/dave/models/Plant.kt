package com.example.dave.models

data class Plant(
    val id: Number,
    val commonName: String,
    val scientificName: List<String>,
    val plantName: String,
    val family: String,
    val type: String,
    val imageUrl: String,
    val careLevel: String,
    val sunlight: List<String>,
    val watering: String,
    val indoor: Boolean,
    val poisonousToHumans: Boolean,
    val poisonousToPets: Boolean,
    val droughtTolerant: Boolean,
    val soil: List<String>,
    val notes: String
)
