package com.example.plantasapi.models

import com.google.gson.annotations.SerializedName

data class ApiPlantResponse(
    @SerializedName("is_plant") val is_plant: Boolean? = null,
    @SerializedName("result") val result: ApiResult? = null,
    // The following fields were in the original file but don't seem to match the JSON structure 
    // seen in MainActivity (which looks for "result"). 
    // I'll keep them as nullable to avoid breaking other things if they exist.
    val name: String? = null,
    val imageUrl: String? = null,
    val waterPeriod: Int? = null,
    val classification: Classification? = null
)

data class ApiResult(
    @SerializedName("classification") val classification: Classification? = null
)

data class Classification(
    @SerializedName("suggestions") val suggestions: List<Suggestion>? = null
)

data class Suggestion(
    @SerializedName("name") val name: String? = null,
    @SerializedName("probability") val probability: Float? = null
)
