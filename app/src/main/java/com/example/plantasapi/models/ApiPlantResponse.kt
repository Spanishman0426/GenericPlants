package com.example.plantasapi.models

data class ApiPlantResponse(
    val is_plant: Boolean,
    val classification: Classification,
    val name: String,
    val imageUrl: String,
    val waterPeriod: Int
)

data class Classification(
    val suggestions: List<Suggestion>
)

data class Suggestion(
    val name: String,
    val probability: Float // Asegúrate de que esta propiedad exista
)
