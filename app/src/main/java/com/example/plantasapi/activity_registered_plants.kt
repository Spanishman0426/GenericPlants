package com.example.plantasapi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plantasapi.models.Plant
import com.google.android.material.navigation.NavigationView

class RegisteredPlantsActivity : BaseActivity() {

    private lateinit var plantsRecyclerView: RecyclerView
    private lateinit var plantAdapter: PlantAdapter
    private val plants = mutableListOf<Plant>()

    private var photoUri: Uri? = null
    private val plantsList = mutableListOf<Plant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registered_plants)

        // Inicializar RecyclerView
        plantsRecyclerView = findViewById(R.id.plantsRecyclerView)
        plantsRecyclerView.layoutManager = GridLayoutManager(this, 2) // Dos columnas

        // Configurar adaptador
        plantAdapter = PlantAdapter(this, plants) { plant ->
            // Acción cuando se hace clic en una ficha
            Toast.makeText(this, "Planta seleccionada: ${plant.name}", Toast.LENGTH_SHORT).show()
        }
        plantsRecyclerView.adapter = plantAdapter

        // Cargar plantas
        loadPlants()

        // Configuración de la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        // Configuración del DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Configuración del menú lateral
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_registered_plants -> {
                    val intent = Intent(this, RegisteredPlantsActivity::class.java)
                    intent.putParcelableArrayListExtra("plantsList", ArrayList(plantsList))
                    startActivity(intent)
                }
                R.id.nav_about_api -> {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.kindwise.com/plant-id"))
                    startActivity(browserIntent)
                }
                R.id.nav_about_us -> {
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_exit -> finish()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun loadPlants() {
        val plantsList = intent.getParcelableArrayListExtra<Plant>("plantsList") ?: arrayListOf()
        plants.clear()
        plants.addAll(plantsList)
        plantAdapter.notifyDataSetChanged()
    }
}
