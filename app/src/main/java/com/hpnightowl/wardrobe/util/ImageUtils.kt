package com.hpnightowl.wardrobe.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    fun uriToBase64(context: Context, uri: Uri): String? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            // Safety check against null bitmap or excessively large sizes
            if (bitmap == null) return null

            val outputStream = ByteArrayOutputStream()
            // Downscale to save memory and ensure we fit inside Lambda limits
            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmap.width / 3,
                bitmap.height / 3,
                true
            )
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.tryClose()
        }
    }

    private fun InputStream.tryClose() {
        try {
            close()
        } catch (e: Exception) {
            // Ignored
        }
    }
}
