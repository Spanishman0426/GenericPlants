package com.example.plantasapi.repository

import android.net.Uri
import android.util.Log
import com.example.plantasapi.PlantAdapter
import com.example.plantasapi.models.ApiPlantResponse
import com.example.plantasapi.models.Plant
import com.example.plantasapi.network.ApiClient
import com.example.plantasapi.network.ApiService
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlantRepository {

    private val apiService: ApiService = ApiClient.retrofit.create(ApiService::class.java)

    fun identifyPlantBase64(apiKey: String, requestBody: RequestBody): Call<ApiPlantResponse> {
        return apiService.identifyPlantBase64(apiKey, requestBody)
    }

    fun fetchPlantData(apiKey: String, requestBody: RequestBody, plantAdapter: PlantAdapter) {
        val call = identifyPlantBase64(apiKey, requestBody)
        call.enqueue(object : Callback<ApiPlantResponse> {
            override fun onResponse(call: Call<ApiPlantResponse>, response: Response<ApiPlantResponse>) {
                if (response.isSuccessful) {
                    val plantResponse = response.body()
                    
                    // Verificamos si la respuesta indica que es una planta válida (usando safe call)
                    if (plantResponse?.is_plant == true) {
                        val suggestion = plantResponse.result?.classification?.suggestions?.firstOrNull()
                        
                        if (suggestion != null) {
                            val name = suggestion.name ?: "Desconocido"
                            val probability = suggestion.probability ?: 0.0f

                            // Crear un objeto de tipo Plant
                            val newPlant = Plant(
                                name = plantResponse.name ?: "Planta Nueva",
                                waterPeriod = plantResponse.waterPeriod ?: 0,
                                imageUri = Uri.parse(plantResponse.imageUrl ?: ""),
                                apiSuggestedName = name,
                                probability = probability
                            )

                            // Agregar la nueva planta al adaptador
                            plantAdapter.addPlant(newPlant)
                        } else {
                            Log.e("PlantRepository", "No se encontraron sugerencias en la respuesta")
                        }
                    } else {
                        Log.e("PlantRepository", "La API indica que no es una planta o la respuesta es nula")
                    }
                } else {
                    Log.e("PlantRepository", "Error en la respuesta de la API: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ApiPlantResponse>, t: Throwable) {
                Log.e("PlantRepository", "Error en la llamada a la API: ${t.message}")
            }
        })
    }
}
