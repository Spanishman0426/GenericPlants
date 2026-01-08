package com.example.plantasapi

import android.util.Base64
import java.io.File
import java.io.FileInputStream

object Base64Utils {
    fun encodeImageToBase64(file: File): String {
        val inputStream = FileInputStream(file)
        try {
            val bytes = inputStream.readBytes()
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally {
            inputStream.close()
        }
    }
}