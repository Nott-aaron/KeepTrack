package com.example.keeptrack.data.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

object FileStorage {

    private const val FOLDER_NAME = "KeepTrack"
    private const val FILE_PREFIX = "KeepTrack_"


    /**
     * Saves an image into Pictures/KeepTrack.
     *
     * Used mainly for Gallery images.
     *
     * Returns the saved MediaStore Uri as a String.
     */
    fun saveImage(
        context: Context,
        sourceUri: Uri
    ): String? {

        val resolver =
            context.contentResolver

        val fileName =
            "${FILE_PREFIX}${System.currentTimeMillis()}.jpg"

        val values =
            ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.Q
                ) {

                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/$FOLDER_NAME"
                    )

                    put(
                        MediaStore.Images.Media.IS_PENDING,
                        1
                    )
                }
            }

        val destinationUri: Uri?

        try {

            destinationUri =
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

        } catch (e: Exception) {

            e.printStackTrace()
            return null
        }

        if (destinationUri == null) {
            return null
        }

        try {

            val inputStream =
                resolver.openInputStream(
                    sourceUri
                )

            val outputStream =
                resolver.openOutputStream(
                    destinationUri
                )

            if (
                inputStream == null ||
                outputStream == null
            ) {

                inputStream?.close()
                outputStream?.close()

                resolver.delete(
                    destinationUri,
                    null,
                    null
                )

                return null
            }

            inputStream.use { input ->

                outputStream.use { output ->

                    input.copyTo(output)
                }
            }

            /*
             * Gallery images use IS_PENDING.
             */
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.Q
            ) {

                val completedValues =
                    ContentValues().apply {

                        put(
                            MediaStore.Images.Media.IS_PENDING,
                            0
                        )
                    }

                resolver.update(
                    destinationUri,
                    completedValues,
                    null,
                    null
                )
            }

            return destinationUri.toString()

        } catch (e: Exception) {

            e.printStackTrace()

            try {

                resolver.delete(
                    destinationUri,
                    null,
                    null
                )

            } catch (_: Exception) {
                // Ignore cleanup error
            }

            return null
        }
    }


    /**
     * Creates a MediaStore URI for the camera.
     *
     * IMPORTANT:
     * The camera image is NOT created as IS_PENDING.
     *
     * This avoids compatibility problems with some
     * camera applications, including Samsung Camera.
     */
    fun createCameraImageUri(
        context: Context
    ): Uri? {

        return try {

            val resolver =
                context.contentResolver

            val fileName =
                "${FILE_PREFIX}${System.currentTimeMillis()}.jpg"

            val values =
                ContentValues().apply {

                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        fileName
                    )

                    put(
                        MediaStore.Images.Media.MIME_TYPE,
                        "image/jpeg"
                    )

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.Q
                    ) {

                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/$FOLDER_NAME"
                        )

                        /*
                         * DO NOT set IS_PENDING here.
                         *
                         * The Samsung Camera app receives
                         * a normal writable MediaStore URI.
                         */
                    }
                }

            resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }


    /**
     * Finalizes a camera image.
     *
     * Camera images are already visible because
     * IS_PENDING is not used for camera capture.
     *
     * We simply verify that the URI can be opened.
     */
    fun finalizeCameraImage(
        context: Context,
        uri: Uri
    ): Boolean {

        return try {

            context.contentResolver
                .openInputStream(uri)
                ?.use {
                    true
                }
                ?: false

        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }


    /**
     * Returns all KeepTrack images.
     */
    fun listSavedImages(
        context: Context
    ): List<Uri> {

        val resolver =
            context.contentResolver

        val collection =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val images =
            mutableListOf<Uri>()

        val projection =
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE
            )

        try {

            resolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->

                val idColumn =
                    cursor.getColumnIndex(
                        MediaStore.Images.Media._ID
                    )

                val nameColumn =
                    cursor.getColumnIndex(
                        MediaStore.Images.Media.DISPLAY_NAME
                    )

                val mimeColumn =
                    cursor.getColumnIndex(
                        MediaStore.Images.Media.MIME_TYPE
                    )

                if (
                    idColumn >= 0 &&
                    nameColumn >= 0 &&
                    mimeColumn >= 0
                ) {

                    while (
                        cursor.moveToNext()
                    ) {

                        val id =
                            cursor.getLong(
                                idColumn
                            )

                        val name =
                            cursor.getString(
                                nameColumn
                            )

                        val mime =
                            cursor.getString(
                                mimeColumn
                            )

                        /*
                         * Only show images created
                         * by KeepTrack.
                         */
                        if (
                            name.startsWith(
                                FILE_PREFIX
                            ) &&
                            mime != null &&
                            mime.startsWith(
                                "image/"
                            )
                        ) {

                            val uri =
                                Uri.withAppendedPath(
                                    collection,
                                    id.toString()
                                )

                            images.add(uri)
                        }
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        return images
    }


    /**
     * Gets the filename of a saved image.
     */
    fun fileName(
        context: Context,
        uri: Uri
    ): String {

        var result =
            "Saved Document"

        try {

            context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME
                ),
                null,
                null,
                null
            )?.use { cursor ->

                if (
                    cursor.moveToFirst()
                ) {

                    val columnIndex =
                        cursor.getColumnIndex(
                            MediaStore.Images.Media.DISPLAY_NAME
                        )

                    if (
                        columnIndex >= 0
                    ) {

                        result =
                            cursor.getString(
                                columnIndex
                            )
                    }
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        return result
    }


    /**
     * Deletes a saved KeepTrack image.
     */
    fun deleteDocument(
        context: Context,
        uriString: String?
    ) {

        if (
            uriString.isNullOrBlank()
        ) {
            return
        }

        try {

            val uri =
                Uri.parse(
                    uriString
                )

            context.contentResolver.delete(
                uri,
                null,
                null
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}
