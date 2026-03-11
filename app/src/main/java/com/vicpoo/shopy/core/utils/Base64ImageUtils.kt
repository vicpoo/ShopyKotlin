// Base64ImageUtils.kt
package com.vicpoo.shopy.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

object Base64ImageUtils {

    private const val MAX_IMAGE_SIZE_KB = 500 // Tamaño máximo en KB
    private const val COMPRESSION_QUALITY = 70 // Calidad de compresión (0-100)
    private const val MIN_QUALITY = 20 // Calidad mínima para compresión

    /**
     * Convierte un archivo de imagen a Base64 con compresión
     */
    fun fileToBase64(file: File): String? {
        return try {
            // Decodificar el archivo a Bitmap
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.RGB_565 // Reduce calidad para ahorrar espacio
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

            // Comprimir y convertir a Base64
            bitmap?.let { bmp ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                var quality = COMPRESSION_QUALITY
                var byteArray: ByteArray

                do {
                    byteArrayOutputStream.reset()
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                    byteArray = byteArrayOutputStream.toByteArray()
                    quality -= 10
                } while (byteArray.size / 1024 > MAX_IMAGE_SIZE_KB && quality > MIN_QUALITY)

                Base64.encodeToString(byteArray, Base64.DEFAULT)
            } ?: run {
                // Si falla la decodificación, intentar leer directamente
                val bytes = file.readBytes()
                Base64.encodeToString(bytes, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte Base64 a Bitmap para mostrar en la UI
     */
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene el tamaño aproximado de una imagen Base64 en KB
     */
    fun getBase64SizeInKB(base64: String): Int {
        return (base64.length * 3 / 4) / 1024
    }

    /**
     * Verifica si una imagen Base64 es válida
     */
    fun isValidBase64Image(base64: String): Boolean {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            bitmap != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Comprime una imagen Base64 si excede el tamaño máximo
     */
    fun compressBase64IfNeeded(base64: String, maxSizeKB: Int = MAX_IMAGE_SIZE_KB): String {
        val currentSizeKB = getBase64SizeInKB(base64)
        if (currentSizeKB <= maxSizeKB) return base64

        // Si es muy grande, decodificar, comprimir y recodificar
        try {
            val bitmap = base64ToBitmap(base64)
            bitmap?.let { bmp ->
                val byteArrayOutputStream = ByteArrayOutputStream()
                var quality = COMPRESSION_QUALITY
                var byteArray: ByteArray

                do {
                    byteArrayOutputStream.reset()
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                    byteArray = byteArrayOutputStream.toByteArray()
                    quality -= 10
                } while (byteArray.size / 1024 > maxSizeKB && quality > MIN_QUALITY)

                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return base64
    }
}