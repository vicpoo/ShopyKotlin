// ImagePickerHandler.kt
package com.vicpoo.shopy.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberImagePickerHandler(): ImagePickerState {
    val context = LocalContext.current

    val imageFile = remember { mutableStateOf<File?>(null) }
    val showImageOptions = remember { mutableStateOf(false) }
    val permissionDenied = remember { mutableStateOf(false) }
    val tempCameraFile = remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageFile.value = tempCameraFile.value
        }
        tempCameraFile.value = null
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageFile.value = uriToFile(context, it)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val tempFile = createTempImageFile(context)
            tempCameraFile.value = tempFile
            takePictureLauncher.launch(
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
            )
        } else {
            permissionDenied.value = true
        }
        showImageOptions.value = false
    }

    val storagePermissionLauncher = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                pickImageLauncher.launch("image/*")
            } else {
                permissionDenied.value = true
            }
            showImageOptions.value = false
        }
    } else null

    val photosPermissionLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                pickImageLauncher.launch("image/*")
            } else {
                permissionDenied.value = true
            }
            showImageOptions.value = false
        }
    } else null

    val requestCamera: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                val tempFile = createTempImageFile(context)
                tempCameraFile.value = tempFile
                takePictureLauncher.launch(
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        tempFile
                    )
                )
                showImageOptions.value = false
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val requestGallery: () -> Unit = {
        when {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    pickImageLauncher.launch("image/*")
                    showImageOptions.value = false
                } else {
                    photosPermissionLauncher?.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    pickImageLauncher.launch("image/*")
                    showImageOptions.value = false
                } else {
                    storagePermissionLauncher?.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    val openSettings: () -> Unit = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
        showImageOptions.value = false
        permissionDenied.value = false
    }

    return remember {
        ImagePickerState(
            imageFile = imageFile,
            showImageOptions = showImageOptions,
            permissionDenied = permissionDenied,
            requestCamera = requestCamera,
            requestGallery = requestGallery,
            openSettings = openSettings,
            clearImage = { imageFile.value = null },
            showOptions = { showImageOptions.value = true },
            hideOptions = {
                showImageOptions.value = false
                permissionDenied.value = false
            }
        )
    }
}

data class ImagePickerState(
    val imageFile: MutableState<File?>,
    val showImageOptions: MutableState<Boolean>,
    val permissionDenied: MutableState<Boolean>,
    val requestCamera: () -> Unit,
    val requestGallery: () -> Unit,
    val openSettings: () -> Unit,
    val clearImage: () -> Unit,
    val showOptions: () -> Unit,
    val hideOptions: () -> Unit
)

private fun createTempImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.cacheDir
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}