package com.example.keeptrack.ui.screens.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.keeptrack.ui.components.KeepTrackButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AIProcessingScreen(
    imageUri: String?,
    onProcessingComplete: () -> Unit = {},
    onCancel: () -> Unit = {}
) {

    val context = LocalContext.current

    val uri = imageUri?.let {
        Uri.parse(it)
    }

    // ---------------------------------------------------------
    // LOAD IMAGE
    // ---------------------------------------------------------

    val bitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = uri
    ) {

        if (uri != null) {

            value = withContext(
                Dispatchers.IO
            ) {

                try {

                    context
                        .contentResolver
                        .openInputStream(uri)
                        ?.use { inputStream ->

                            BitmapFactory.decodeStream(
                                inputStream
                            )
                        }

                } catch (
                    exception: Exception
                ) {

                    null
                }
            }
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        // -----------------------------------------------------
        // IMAGE PREVIEW
        // -----------------------------------------------------

        if (bitmap != null) {

            Image(
                bitmap =
                    bitmap!!.asImageBitmap(),

                contentDescription =
                    "Selected document",

                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    ),

                contentScale =
                    ContentScale.Fit
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )
        }

        // -----------------------------------------------------
        // PROCESSING INDICATOR
        // -----------------------------------------------------

        CircularProgressIndicator(
            color =
                MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // -----------------------------------------------------
        // TITLE
        // -----------------------------------------------------

        Text(
            text =
                "Processing Document",

            style =
                MaterialTheme.typography.headlineMedium,

            color =
                MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -----------------------------------------------------
        // DESCRIPTION
        // -----------------------------------------------------

        Text(
            text =
                if (uri != null) {

                    "Your document has been received. KeepTrack is preparing it for information extraction."

                } else {

                    "No document was selected."
                },

            style =
                MaterialTheme.typography.bodyLarge,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        // -----------------------------------------------------
        // CONTINUE
        // -----------------------------------------------------

        KeepTrackButton(
            text =
                "Continue to Review",

            onClick =
                onProcessingComplete
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // -----------------------------------------------------
        // CANCEL
        // -----------------------------------------------------

        KeepTrackButton(
            text =
                "Cancel",

            onClick =
                onCancel
        )
    }
}