
package com.example.keeptrack.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.example.keeptrack.data.storage.FileStorage
import com.example.keeptrack.ui.components.KeepTrackButton

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun ScanDocumentScreen(
    onImageSelected: (Uri) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /*
     * Saved documents
     */
    var savedImages by remember {
        mutableStateOf(emptyList<Uri>())
    }

    /*
     * Keep camera URI as String so it survives
     * configuration/process recreation.
     */
    var cameraImageUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    /*
     * Load saved images without blocking
     * the main UI thread.
     */
    LaunchedEffect(Unit) {
        savedImages = withContext(Dispatchers.IO) {
            runCatching {
                FileStorage.listSavedImages(context)
            }.getOrElse {
                emptyList()
            }
        }
    }

    /*
     * Refresh saved documents.
     */
    fun refreshSavedImages() {
        scope.launch {
            savedImages = withContext(Dispatchers.IO) {
                runCatching {
                    FileStorage.listSavedImages(context)
                }.getOrElse {
                    emptyList()
                }
            }
        }
    }

    /*
     * Camera result
     */

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val uri = cameraImageUriString?.let {
                Uri.parse(it)
            }

            scope.launch {
                if (success && uri != null) {

                    val finalized = withContext(Dispatchers.IO) {
                        runCatching {
                            FileStorage.finalizeCameraImage(
                                context = context,
                                uri = uri
                            )
                        }.getOrElse {
                            false
                        }
                    }

                    if (finalized) {

                        // Refresh the saved receipts list.
                        val images = withContext(Dispatchers.IO) {
                            runCatching {
                                FileStorage.listSavedImages(context)
                            }.getOrElse {
                                emptyList()
                            }
                        }

                        savedImages = images

                        // Stay on the Scan page.
                        // Do not call onImageSelected(uri) here.

                    } else {

                        withContext(Dispatchers.IO) {
                            FileStorage.deleteDocument(
                                context,
                                uri.toString()
                            )
                        }

                        Toast.makeText(
                            context,
                            "Could not save the captured image.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {

                    if (uri != null) {
                        withContext(Dispatchers.IO) {
                            FileStorage.deleteDocument(
                                context,
                                uri.toString()
                            )
                        }
                    }
                }

                cameraImageUriString = null
            }
        }

    /*
     * Gallery result
     */
    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {
                scope.launch {

                    /*
                     * Copy gallery image into KeepTrack storage.
                     */
                    val saved = withContext(Dispatchers.IO) {
                        runCatching {
                            FileStorage.saveImage(
                                context,
                                uri
                            )
                        }.getOrNull()
                    }

                    if (saved != null) {
                        val savedUri = Uri.parse(saved)

                        /*
                         * Preserve existing gallery workflow.
                         */
                        val images = withContext(Dispatchers.IO) {
                            runCatching {
                                FileStorage.listSavedImages(context)
                            }.getOrElse {
                                emptyList()
                            }
                        }

                        savedImages = images

                        /*
                         * Open selected image.
                         */
                        onImageSelected(savedUri)

                    } else {
                        Toast.makeText(
                            context,
                            "Could not save the selected image.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    /*
     * Camera permission launcher
     */
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                val uri = FileStorage.createCameraImageUri(context)

                if (uri != null) {
                    cameraImageUriString = uri.toString()
                    cameraLauncher.launch(uri)
                } else {
                    Toast.makeText(
                        context,
                        "Unable to create image file.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                Toast.makeText(
                    context,
                    "Camera permission is required.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    /*
     * Launch camera
     */
    fun launchCamera() {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            val uri = FileStorage.createCameraImageUri(context)

            if (uri != null) {
                cameraImageUriString = uri.toString()
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(
                    context,
                    "Unable to create image file.",
                    Toast.LENGTH_LONG
                ).show()
            }

        } else {
            cameraPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    /*
     * UI
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Scan Document",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Capture a document using your camera or select an image from your gallery.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        /*
         * Camera + Gallery buttons
         */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeepTrackButton(
                text = "Camera",
                onClick = {
                    launchCamera()
                },
                modifier = Modifier.weight(1f)
            )

            KeepTrackButton(
                text = "Gallery",
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Saved Documents",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        /*
         * No documents
         */
        if (savedImages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No saved documents",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        } else {

            /*
             * Saved documents list
             */
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = savedImages,
                    key = { it.toString() }
                ) { uri ->

                    SavedDocumentCard(
                        uri = uri,

                        onOpen = {
                            onImageSelected(uri)
                        },

                        onDelete = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    FileStorage.deleteDocument(
                                        context,
                                        uri.toString()
                                    )
                                }

                                refreshSavedImages()
                            }
                        }
                    )
                }
            }
        }
    }
}

/*
 * Saved document card
 */
@Composable
private fun SavedDocumentCard(
    uri: Uri,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    /*
     * Load a small thumbnail instead of decoding
     * the complete high-resolution image.
     */
    val thumbnail by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = uri
    ) {
        value = withContext(Dispatchers.IO) {
            runCatching {

                val bounds = BitmapFactory.Options()
                bounds.inJustDecodeBounds = true

                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->
                        BitmapFactory.decodeStream(
                            input,
                            null,
                            bounds
                        )
                    }

                val targetSize = 192
                var sampleSize = 1

                val width = bounds.outWidth
                val height = bounds.outHeight

                while (
                    width / sampleSize > targetSize ||
                    height / sampleSize > targetSize
                ) {
                    sampleSize *= 2
                }

                val options = BitmapFactory.Options()
                options.inSampleSize = sampleSize

                context.contentResolver
                    .openInputStream(uri)
                    ?.use { input ->
                        BitmapFactory.decodeStream(
                            input,
                            null,
                            options
                        )?.asImageBitmap()
                    }

            }.getOrNull()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /*
             * Thumbnail
             */
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!,
                    contentDescription = "Saved document",
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Document",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Saved Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to open",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete document",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}