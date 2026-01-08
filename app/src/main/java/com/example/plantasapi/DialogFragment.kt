package com.example.plantasapi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.plantasapi.models.Plant


class PlantDetailDialogFragment(private val plant: Plant) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plant_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plantName = view.findViewById<TextView>(R.id.plantNameDetail)
        val plantImage = view.findViewById<ImageView>(R.id.plantImageDetail)
        val plantPeriod = view.findViewById<TextView>(R.id.plantPeriod)
        val plantProbability = view.findViewById<TextView>(R.id.plantProbability)
        val apiName = view.findViewById<TextView>(R.id.plantApiName)  // Nueva TextView para el nombre de la API

        // Configurar los datos de la planta
        plantName.text = plant.name
        Glide.with(requireContext())
            .load(plant.imageUri.toString())
            .into(plantImage)

        plantProbability.text = "Probabilidad: ${plant.probability * 100}%"
        plantPeriod.text = "Regar cada ${plant.waterPeriod} dias"
        // Mostrar el nombre sugerido por la API debajo de la probabilidad
        apiName.text = "Con mas coincidencia: ${plant.apiSuggestedName ?: "No disponible"}"

    }
}