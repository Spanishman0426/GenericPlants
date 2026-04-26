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
    import com.example.plantasapi.models.ApiPlantResponse
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response
    import java.io.File
    import java.io.IOException
    import okhttp3.MediaType.Companion.toMediaType
    import okhttp3.RequestBody
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.content.Context
    import android.os.Build
    import android.Manifest
    import android.content.pm.PackageManager
    import androidx.core.content.ContextCompat
    import androidx.appcompat.app.AlertDialog

    class MainActivity : AppCompatActivity() {

        companion object {
            const val CHANNEL_ID = "plant_watering_channel"
        }

        private lateinit var drawerLayout: DrawerLayout
        private lateinit var imageView: ImageView
        private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
        private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
        private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
        
        private var photoUri: Uri? = null
        private val plantsList = mutableListOf<Plant>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            createNotificationChannel()

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

            imageView = findViewById(R.id.imageView)
            val btnCamera: Button = findViewById(R.id.btnCamera)
            val btnGallery: Button = findViewById(R.id.btnGallery)
            val btnSave: Button = findViewById(R.id.btnSave)
            val etPlantName: EditText = findViewById(R.id.etPlantName)
            val etWaterPeriod: EditText = findViewById(R.id.etWaterPeriod)

            // Configurar lanzadores
            setupLaunchers()

            // Botón para abrir la cámara
            btnCamera.setOnClickListener {
                checkCameraPermission()
            }

            // Botón para abrir la galería
            btnGallery.setOnClickListener {
                openGallery()
            }

            // Botón para guardar la planta
            btnSave.setOnClickListener {
                val plantName = etPlantName.text.toString()
                val waterPeriod = etWaterPeriod.text.toString().toIntOrNull() ?: 0

                if (plantName.isNotEmpty() && waterPeriod > 0 && photoUri != null) {
                    val newPlant = Plant(
                        name = plantName,
                        waterPeriod = waterPeriod,
                        imageUri = photoUri!!,
                        apiSuggestedName = null,
                        probability = 0.0f
                    )
                    plantsList.add(newPlant)
                    sendImageToApi(photoUri!!, newPlant)
                    Toast.makeText(this, "Planta registrada y en proceso de identificación", Toast.LENGTH_SHORT).show()

                    etPlantName.text.clear()
                    etWaterPeriod.text.clear()
                    imageView.setImageDrawable(null)
                    photoUri = null
                } else {
                    Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun setupLaunchers() {
            cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    photoUri?.let { uri -> imageView.setImageURI(uri) }
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

            requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera()
                } else {
                    handlePermissionDenied()
                }
            }
        }

        private fun checkCameraPermission() {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                    openCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        private fun showPermissionRationaleDialog() {
            AlertDialog.Builder(this)
                .setTitle("Permiso de Cámara Necesario")
                .setMessage("La aplicación necesita acceder a la cámara para tomar fotos de las plantas y poder identificarlas.")
                .setPositiveButton("Solicitar de nuevo") { _, _ ->
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .setNegativeButton("Usar Galería") { _, _ ->
                    openGallery()
                }
                .show()
        }

        private fun handlePermissionDenied() {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Permanently denied
                Toast.makeText(this, "Permiso denegado permanentemente. Por favor, actívalo en ajustes o usa la galería.", Toast.LENGTH_LONG).show()
                showSettingsDialog()
            } else {
                Toast.makeText(this, "Acceso a cámara denegado. Se recomienda usar la galería.", Toast.LENGTH_SHORT).show()
                openGallery()
            }
        }

        private fun showSettingsDialog() {
            AlertDialog.Builder(this)
                .setTitle("Permisos requeridos")
                .setMessage("Has denegado el permiso de cámara permanentemente. Puedes habilitarlo en los ajustes de la aplicación o continuar usando la galería.")
                .setPositiveButton("Ajustes") { _, _ ->
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        private fun openCamera() {
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

        private fun openGallery() {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Plant Watering"
                val descriptionText = "Notifications for watering your plants"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        private val plantRepository = PlantRepository()

        private fun sendImageToApi(uri: Uri, plant: Plant) {
            try {
                val imageFile = FileUtils.copyUriToFile(this, uri)
                val base64Image = Base64Utils.encodeImageToBase64(imageFile)

                val apiKey = BuildConfig.PLANT_API_KEY
                if (apiKey.isEmpty()) {
                    Toast.makeText(this, "Error: API Key no configurada.", Toast.LENGTH_LONG).show()
                    return
                }
                
                val imagesJson = """{ "images": ["data:image/jpg;base64,$base64Image"] }"""
                val requestBody = RequestBody.create("application/json".toMediaType(), imagesJson)

                plantRepository.identifyPlantBase64(apiKey, requestBody).enqueue(object : Callback<ApiPlantResponse> {
                    override fun onResponse(call: Call<ApiPlantResponse>, response: Response<ApiPlantResponse>) {
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            val suggestion = apiResponse?.result?.classification?.suggestions?.firstOrNull()
                            
                            val suggestedName = suggestion?.name ?: "Desconocido"
                            val probability = suggestion?.probability ?: 0.0f

                            plant.apiSuggestedName = suggestedName
                            plant.probability = probability

                            if (probability < 0.2f) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Baja confianza en la identificación ($probability). Por favor verifica.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Planta identificada como: $suggestedName",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                            Log.e("API Error", "Error: ${response.code()} - $errorBody")
                            Toast.makeText(this@MainActivity, "Error en la API: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<ApiPlantResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
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

        private fun createImageFile(): File {
            return try {
                val storageDir = cacheDir
                File.createTempFile("plant_image", ".jpg", storageDir)
            } catch (e: IOException) {
                throw RuntimeException("Error al crear el archivo de imagen", e)
            }
        }
    }
