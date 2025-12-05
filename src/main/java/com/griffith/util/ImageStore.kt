package com.griffith.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

fun saveImage(context: Context, src: Uri): String {
    val ext = ".jpg"
    val name = "dish_${UUID.randomUUID()}$ext"
    val file = File(context.filesDir, name)

    context.contentResolver.openInputStream(src).use { input ->
        file.outputStream().use { output ->
            if (input != null) {
                input.copyTo(output)
            }
        }
    }
    return file.toUri().toString()
}
