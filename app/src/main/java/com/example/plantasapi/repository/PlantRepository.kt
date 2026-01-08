// PlantRepository.kt
package com.example.plantasapi.repository

import android.net.Uri
import android.util.Log
import com.example.plantasapi.PlantAdapter
import com.example.plantasapi.models.ApiPlantResponse
import com.example.plantasapi.models.Plant
import com.example.plantasapi.network.ApiClient
import com.example.plantasapi.network.ApiService
import com.google.gson.Gson
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantRepository {

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    fun identifyPlantBase64(apiKey: String, requestBody: RequestBody): Call<ResponseBody> {
        return apiService.identifyPlantBase64(apiKey, requestBody)
    }

    fun fetchPlantData(apiKey: String, requestBody: RequestBody, plantAdapter: PlantAdapter) {
        val call = identifyPlantBase64(apiKey, requestBody)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        val plantResponse = Gson().fromJson(it.string(), ApiPlantResponse::class.java)

                        // Verificamos si la respuesta indica que es una planta válida
                        if (plantResponse.is_plant) {
                            val suggestions = plantResponse.classification.suggestions
                            if (suggestions.isNotEmpty()) {
                                val firstSuggestion = suggestions[0]
                                val name = firstSuggestion.name
                                val probability = firstSuggestion.probability

                                // Crear un objeto de tipo Plant con el nombre sugerido por la API
                                val newPlant = Plant(
                                    name = plantResponse.name, // El nombre del usuario sigue siendo el principal
                                    waterPeriod = plantResponse.waterPeriod,
                                    imageUri = Uri.parse(plantResponse.imageUrl),
                                    apiSuggestedName = firstSuggestion.name, // Nombre sugerido por la API
                                    probability = probability
                                )

                                // Agregar la nueva planta al adaptador
                                plantAdapter.addPlant(newPlant)
                            }
                        }
                    }
                } else {
                    Log.e("PlantRepository", "Error en la respuesta de la API: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("PlantRepository", "Error en la llamada a la API: ${t.message}")
            }
        })
    }
}
