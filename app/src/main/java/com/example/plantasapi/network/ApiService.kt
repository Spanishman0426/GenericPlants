package com.example.plantasapi.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("identification")
    fun identifyPlantBase64(
        @Header("Api-Key") apiKey: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>
}