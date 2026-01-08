# 🌱 PlantasAPI - Aplicación de Identificación de Plantas

Una aplicación Android desarrollada en Kotlin que permite identificar plantas mediante inteligencia artificial utilizando la API de Plant.id. La aplicación está diseñada siguiendo principios de Programación Orientada a Objetos (POO) y arquitectura limpia.

## 📋 Descripción

PlantasAPI es una aplicación móvil que ayuda a los usuarios a identificar plantas tomando o seleccionando una fotografía. Utiliza la API de Plant.id para analizar la imagen y proporcionar información sobre la planta identificada, incluyendo su nombre científico y el nivel de confianza de la identificación. ademas dentro podran añadir recordatorios para sus riegos que se enviaran con notificaciones.

### Características Principales

- 📸 **Captura de Imágenes**: Toma fotos con la cámara del dispositivo o selecciona imágenes de la galería
- 🤖 **Identificación por IA**: Utiliza la API de Plant.id para identificar plantas mediante inteligencia artificial
- 📝 **Registro de Plantas**: Guarda plantas identificadas con información personalizada (nombre, período de riego)
- 📊 **Visualización**: Muestra las plantas registradas en una cuadrícula con sus imágenes
- 🔔 **Notificaciones**: Sistema de recordatorios para el cuidado de plantas (WorkManager)
- 🎨 **Interfaz Moderna**: Diseño Material Design con navegación lateral (Drawer Navigation)

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Arquitectura**: Programación Orientada a Objetos (POO)
- **Framework**: Android SDK
- **API Externa**: [Plant.id API](https://web.plant.id/plant-identification-api)
- **Librerías Principales**:
  - Retrofit 2.9.0 - Comunicación HTTP con la API
  - Gson - Serialización JSON
  - Glide 4.15.1 - Carga y manejo de imágenes
  - WorkManager 2.8.1 - Notificaciones programadas
  - Material Design Components - UI moderna
  - Navigation Component - Navegación entre pantallas

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/plantasapi/
├── activity_about_us.kt              # Actividad "Acerca de"
├── activity_registered_plants.kt      # Actividad de plantas registradas
├── BaseActivity.kt                    # Actividad base con navegación común
├── MainActivity.kt                    # Actividad principal
├── Base64Utils.kt                     # Utilidades para codificación Base64
├── DialogFragment.kt                  # Fragmento de diálogo
├── PlantAdapter.kt                    # Adaptador para RecyclerView
├── managers/
│   ├── NotificationHelper.kt         # Gestor de notificaciones
│   └── PlantManager.kt                # Gestor de plantas
├── models/
│   ├── ApiPlantResponse.kt           # Modelo de respuesta de la API
│   └── Plant.kt                      # Modelo de datos de planta
├── network/
│   ├── ApiClient.kt                  # Cliente Retrofit
│   └── ApiService.kt                 # Interfaz de servicios API
├── repository/
│   └── PlantRepository.kt            # Repositorio de datos
└── utils/
    └── FileUtils.kt                  # Utilidades de archivos
```

## 🚀 Configuración e Instalación

### Requisitos Previos

- Android Studio Hedgehog o superior
- JDK 11 o superior
- Android SDK (mínimo API 24, objetivo API 34)
- Una cuenta en [Plant.id](https://web.plant.id/) para obtener una API Key

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <url-del-repositorio>
   cd ProyectoPoo
   ```

2. **Configurar la API Key**
   
   - Copia el archivo `local.properties.example` a `local.properties`:
     ```bash
     cp local.properties.example local.properties
     ```
   
   - Edita `local.properties` y añade tu API Key de Plant.id:
     ```properties
     PLANT_API_KEY=tu_api_key_aqui
     ```

3. **Sincronizar el proyecto**
   - Abre el proyecto en Android Studio
   - Espera a que Gradle sincronice las dependencias automáticamente
   - Si es necesario, ejecuta: `File > Sync Project with Gradle Files`

4. **Ejecutar la aplicación**
   - Conecta un dispositivo Android o inicia un emulador
   - Haz clic en "Run" o presiona `Shift + F10`

## 🔐 Seguridad

Este proyecto implementa buenas prácticas de seguridad:

- ✅ **API Keys protegidas**: Las claves de API se almacenan en `local.properties`, que está excluido del control de versiones
- ✅ **BuildConfig**: Las claves se inyectan en tiempo de compilación mediante BuildConfig
- ✅ **Gitignore completo**: Archivos sensibles y de configuración local están protegidos

## 📱 Uso de la Aplicación

1. **Identificar una Planta**:
   - Abre la aplicación
   - Toma una foto con la cámara o selecciona una imagen de la galería
   - Ingresa un nombre personalizado para la planta
   - Especifica el período de riego en días
   - Guarda la planta

2. **Ver Plantas Registradas**:
   - Abre el menú lateral (ícono de hamburguesa)
   - Selecciona "Plantas Registradas"
   - Visualiza todas las plantas guardadas en formato de cuadrícula

3. **Información Adicional**:
   - El menú lateral incluye opciones para conocer más sobre la API utilizada y sobre la aplicación

## 🏗️ Arquitectura y Diseño

### Principios de POO Aplicados

- **Encapsulación**: Los datos de las plantas están encapsulados en la clase `Plant`
- **Herencia**: `BaseActivity` proporciona funcionalidad común a todas las actividades
- **Polimorfismo**: Uso de interfaces como `ApiService` para definir contratos
- **Abstracción**: Separación de responsabilidades mediante repositorios y managers

### Patrones de Diseño

- **Repository Pattern**: `PlantRepository` maneja la lógica de acceso a datos
- **Singleton**: `ApiClient` utiliza el patrón singleton para la instancia de Retrofit
- **Adapter Pattern**: `PlantAdapter` adapta los datos de plantas para el RecyclerView

## 🧪 Pruebas

El proyecto incluye:
- Tests unitarios en `app/src/test/`
- Tests de instrumentación en `app/src/androidTest/`

Para ejecutar las pruebas:
```bash
./gradlew test          # Tests unitarios
./gradlew connectedAndroidTest  # Tests de instrumentación
```

## 📄 Licencia

Este proyecto es de código abierto y está disponible para fines educativos y de portafolio.

## 👨‍💻 Autor

Desarrollado como proyecto de Programación Orientada a Objetos para demostración en portafolio profesional.

---

**Nota**: Esta aplicación requiere una conexión a Internet para funcionar, ya que utiliza una API externa para la identificación de plantas.
