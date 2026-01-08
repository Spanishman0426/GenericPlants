    package com.example.plantasapi

    import com.example.plantasapi.BuildConfig
    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle
    import android.provider.MediaStore
    import android.util.Log
    import android.view.MenuItem
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.Toast
    import androidx.activity.result.ActivityResultLauncher
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.widget.Toolbar
    import androidx.core.content.FileProvider
    import androidx.core.view.GravityCompat
    import androidx.drawerlayout.widget.DrawerLayout
    import com.google.android.material.navigation.NavigationView
    import com.example.plantasapi.models.Plant
    import com.example.plantasapi.repository.PlantRepository
    import com.example.plantasapi.utils.FileUtils
    import okhttp3.ResponseBody
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import java.io.File
    import java.io.IOException
    import okhttp3.MediaType
    import okhttp3.MediaType.Companion.toMediaType
    import okhttp3.RequestBody
    import org.json.JSONObject

    class MainActivity : AppCompatActivity() {

        private lateinit var drawerLayout: DrawerLayout
        private lateinit var imageView: ImageView
        private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
        private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
        private var photoUri: Uri? = null
        private val plantsList = mutableListOf<Plant>()



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // Configuración de la Toolbar
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)

            // Configuración del DrawerLayout
            drawerLayout = findViewById(R.id.drawer_layout)

            // Botón de hamburguesa
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_menu)
            }

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

            // Configuración del botón "Atrás" para el menú lateral
            imageView = findViewById(R.id.imageView)
            val btnCamera: Button = findViewById(R.id.btnCamera)
            val btnGallery: Button = findViewById(R.id.btnGallery)
            val btnSave: Button = findViewById(R.id.btnSave)
            val etPlantName: EditText = findViewById(R.id.etPlantName)
            val etWaterPeriod: EditText = findViewById(R.id.etWaterPeriod)

            // Configurar lanzadores para cámara y galería
            cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    photoUri?.let { uri -> imageView.setImageURI(uri) } // Mostrar la imagen capturada
                } else {
                    Toast.makeText(this, "Captura cancelada", Toast.LENGTH_SHORT).show()
                }
            }

            galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val imageUri: Uri? = result.data?.data
                    photoUri = imageUri
                    imageView.setImageURI(imageUri)
                } else {
                    Toast.makeText(this, "Selección cancelada", Toast.LENGTH_SHORT).show()
                }
            }

            // Botón para abrir la cámara
            btnCamera.setOnClickListener {
                val photoFile = createImageFile()
                photoUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    photoFile
                )

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                }
                cameraLauncher.launch(cameraIntent)
            }

            // Botón para abrir la galería
            btnGallery.setOnClickListener {
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(galleryIntent)
            }

            // Botón para guardar la planta
            btnSave.setOnClickListener {
                val plantName = etPlantName.text.toString()
                val waterPeriod = etWaterPeriod.text.toString().toIntOrNull() ?: 0

                if (plantName.isNotEmpty() && waterPeriod > 0 && photoUri != null) {
                    // Crear un objeto inicial con los datos del usuario
                    val newPlant = Plant(
                        name = plantName, // Nombre ingresado por el usuario
                        waterPeriod = waterPeriod,
                        imageUri = photoUri!!,
                        apiSuggestedName = null, // Se llenará más tarde
                        probability = 0.0f // Se llenará más tarde
                    )

                    // Agregar a la lista temporalmente (se actualizará cuando la API responda)
                    plantsList.add(newPlant)

                    // Enviar la imagen a la API para obtener el nombre sugerido
                    sendImageToApi(photoUri!!, newPlant)

                    Toast.makeText(this, "Planta registrada y en proceso de identificación", Toast.LENGTH_SHORT).show()

                    // Limpiar campos
                    etPlantName.text.clear()
                    etWaterPeriod.text.clear()
                    imageView.setImageDrawable(null)
                    photoUri = null
                } else {
                    Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }



        }

        private val plantRepository = PlantRepository()

        // Lógica para enviar la imagen a la API y actualizar la ficha
        private fun sendImageToApi(uri: Uri, plant: Plant) {
            try {
                val imageFile = FileUtils.copyUriToFile(this, uri)
                val base64Image = Base64Utils.encodeImageToBase64(imageFile)

                // Obtener API Key desde BuildConfig (configurada en local.properties)
                val apiKey = BuildConfig.PLANT_API_KEY
                if (apiKey.isEmpty()) {
                    Toast.makeText(this, "Error: API Key no configurada. Por favor, configura PLANT_API_KEY en local.properties", Toast.LENGTH_LONG).show()
                    return
                }
                
                val imagesJson = """{ "images": ["data:image/jpg;base64,$base64Image"] }"""
                val requestBody = RequestBody.create("application/json".toMediaType(), imagesJson)

                plantRepository.identifyPlantBase64(apiKey, requestBody).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseString = response.body()?.string() ?: "Sin cuerpo de respuesta"
                            Log.d("API Response", "Respuesta exitosa: $responseString")

                            // Procesar la respuesta de la API
                            val jsonResponse = JSONObject(responseString)
                            val suggestions = jsonResponse.getJSONObject("result")
                                .getJSONObject("classification")
                                .getJSONArray("suggestions")

                            val suggestedName = suggestions.getJSONObject(0).getString("name")
                            val probability = suggestions.getJSONObject(0).getDouble("probability").toFloat()

                            // Actualizar la planta existente con datos de la API
                            plant.apiSuggestedName = suggestedName
                            plant.probability = probability

                            // Notificar éxito
                            Toast.makeText(
                                this@MainActivity,
                                "Planta identificada como: $suggestedName con probabilidad $probability",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Sin mensaje de error"
                            Log.e("API Error", "Error: ${response.code()} - $errorBody")
                            Toast.makeText(this@MainActivity, "Error: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error al conectar con la API: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

        override fun onBackPressed() {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }

        // Crear un archivo temporal para la imagen
        private fun createImageFile(): File {
            return try {
                val storageDir = cacheDir // Directorio temporal
                File.createTempFile("plant_image", ".jpg", storageDir)
            } catch (e: IOException) {
                throw RuntimeException("Error al crear el archivo de imagen", e)
            }
        }
    }
