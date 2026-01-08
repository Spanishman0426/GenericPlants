package com.example.plantasapi.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


object FileUtils {
    fun copyUriToFile(context: Context, uri: Uri): File {
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream: InputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el URI.")
        val outputDir: File = context.cacheDir // Guardar en el caché de la app
        val outputFile = File(outputDir, "temp_image_${System.currentTimeMillis()}.jpg")

        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()
        return outputFile
    }
}
