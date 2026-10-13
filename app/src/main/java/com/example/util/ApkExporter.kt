package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

object ApkExporter {

    private const val APK_FILE_NAME = "NCStudio-ESP32.apk"

    fun getApkSizeMb(context: Context): Float {
        return try {
            val sourceFile = File(context.applicationInfo.sourceDir)
            if (sourceFile.exists()) {
                sourceFile.length() / (1024f * 1024f)
            } else 24.0f
        } catch (_: Exception) {
            24.0f
        }
    }

    fun saveApkToDownloads(context: Context): Pair<Boolean, String> {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                return Pair(false, "Source APK file not found on device.")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, APK_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(sourceApk).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    Pair(true, "Successfully saved $APK_FILE_NAME to your device Downloads folder!")
                } else {
                    Pair(false, "Failed to create destination file in Downloads.")
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val destFile = File(downloadsDir, APK_FILE_NAME)
                FileInputStream(sourceApk).use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Pair(true, "Saved to ${destFile.absolutePath}")
            }
        } catch (e: Exception) {
            Pair(false, "Error saving APK: ${e.message}")
        }
    }

    fun shareApk(context: Context): Pair<Boolean, String> {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                return Pair(false, "Source APK file not found on device.")
            }

            // Copy to app's cache directory so FileProvider can grant access
            val cacheFile = File(context.cacheDir, APK_FILE_NAME)
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(cacheFile).use { output ->
                    input.copyTo(output)
                }
            }

            val authority = "${context.packageName}.provider"
            val uri = FileProvider.getUriForFile(context, authority, cacheFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "NC Studio ESP32 CNC Controller APK")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Install NC Studio ESP32 CNC router controller on your tablet or smartphone."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share / Send NC Studio APK").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Pair(true, "Opened sharing menu")
        } catch (e: Exception) {
            Pair(false, "Error sharing APK: ${e.message}")
        }
    }
}
