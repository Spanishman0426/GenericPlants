package com.example.plantasapi.managers

import com.example.plantasapi.models.Plant

class PlantManager {
    private val plantList = mutableListOf<Plant>()

    fun addPlant(plant: Plant) {
        plantList.add(plant)
    }

    fun getPlants(): List<Plant> {
        return plantList
    }

    fun removePlant(name: String): Boolean {
        return plantList.removeIf { it.name == name }
    }
}
