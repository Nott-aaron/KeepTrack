package com.example.keeptrack.ui.screens.main

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.keeptrack.data.backup.BackupManager
import com.example.keeptrack.data.storage.FileStorage
import com.example.keeptrack.navigation.Routes

import com.example.keeptrack.ui.screens.home.HomeScreen
import com.example.keeptrack.ui.screens.records.RecordsScreen
import com.example.keeptrack.ui.screens.scan.DocumentOcrScreen
import com.example.keeptrack.ui.screens.scan.ScanDocumentScreen
import com.example.keeptrack.ui.screens.settings.SettingsScreen

import com.example.keeptrack.viewmodel.RecordViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// =============================================================
// INTERNAL MAIN SCREEN ROUTES
// =============================================================

private object MainRoutes {

    const val HOME =
        "main_home"

    const val RECORDS =
        "main_records"

    const val SCAN =
        "main_scan"

    const val SETTINGS =
        "main_settings"

    const val DOCUMENT =
        "main_document"
}


// =============================================================
// MAIN SCREEN
// =============================================================

@Composable
fun MainScreen(

    rootNavController:
    NavHostController,

    recordViewModel:
    RecordViewModel,

    userName:
    String = "there",

    darkTheme:
    Boolean = false,

    onDarkThemeChange:
        (Boolean) -> Unit = {},

    onLogoutConfirmed:
        () -> Unit = {}

) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // INTERNAL NAVIGATION CONTROLLER
    // =========================================================

    val mainNavController =
        rememberNavController()


    // =========================================================
    // MAIN TAB NAVIGATION
    // =========================================================

    /*
     * All four bottom-navigation tabs use the same navigation
     * behavior.
     *
     * This prevents Home / Records from getting stuck while
     * moving between Scan and Settings.
     */

    fun navigateToMainRoute(
        route: String
    ) {

        mainNavController.navigate(
            route
        ) {

            popUpTo(
                mainNavController.graph
                    .findStartDestination()
                    .id
            ) {
                saveState = true
            }

            launchSingleTop =
                true

            restoreState =
                true
        }
    }


    // =========================================================
    // RECORDS
    // =========================================================

    val allRecords by
    recordViewModel
        .allRecords
        .collectAsState(
            initial = emptyList()
        )


    var selectedCategory by
    remember {
        mutableStateOf<String?>(null)
    }


    // =========================================================
    // DELETE DIALOG
    // =========================================================

    var showDeleteDialog by
    remember {
        mutableStateOf(false)
    }


    // =========================================================
    // BACKUP FILE
    // =========================================================

    var pendingBackupFile by
    remember {
        mutableStateOf<java.io.File?>(null)
    }


    // =========================================================
    // CREATE BACKUP FILE PICKER
    // =========================================================

    val createBackupLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts
                .CreateDocument(
                    "application/zip"
                )

        ) { destination ->

            val source =
                pendingBackupFile

            pendingBackupFile =
                null


            if (
                destination == null ||
                source == null
            ) {

                return@rememberLauncherForActivityResult
            }


            scope.launch(
                Dispatchers.IO
            ) {

                runCatching {

                    context
                        .contentResolver
                        .openOutputStream(
                            destination
                        )
                        ?.use { output ->

                            source
                                .inputStream()
                                .use { input ->

                                    input.copyTo(
                                        output
                                    )
                                }
                        }
                        ?: error(
                            "Could not open backup destination"
                        )


                    source.delete()

                }.onSuccess {

                    withContext(
                        Dispatchers.Main
                    ) {

                        Toast.makeText(
                            context,
                            "Backup saved successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }.onFailure {

                    withContext(
                        Dispatchers.Main
                    ) {

                        Toast.makeText(
                            context,
                            "Backup failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


    // =========================================================
    // IMPORT / RESTORE
    // =========================================================

    val importLauncher =
        rememberLauncherForActivityResult(

            ActivityResultContracts
                .OpenDocument()

        ) { uri ->

            if (uri == null) {

                return@rememberLauncherForActivityResult
            }


            scope.launch {

                runCatching {

                    BackupManager.restore(
                        context,
                        uri
                    ) { records ->

                        recordViewModel
                            .replaceAllRecords(
                                records
                            )
                    }

                }.onSuccess {

                    Toast.makeText(
                        context,
                        "Backup/import restored successfully",
                        Toast.LENGTH_LONG
                    ).show()

                }.onFailure {

                    Toast.makeText(
                        context,
                        "Import failed: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }


    // =========================================================
    // START BACKUP
    // =========================================================

    fun startBackup() {

        scope.launch(
            Dispatchers.IO
        ) {

            runCatching {

                BackupManager.createBackup(
                    context,
                    allRecords
                )

            }.onSuccess { file ->

                pendingBackupFile =
                    file


                withContext(
                    Dispatchers.Main
                ) {

                    createBackupLauncher.launch(
                        "KeepTrack_Backup_${System.currentTimeMillis()}.zip"
                    )
                }

            }.onFailure { error ->

                withContext(
                    Dispatchers.Main
                ) {

                    Toast.makeText(
                        context,
                        "Could not create backup: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // =========================================================
    // MAIN SCAFFOLD
    // =========================================================

    Scaffold(

        topBar = {

            KeepTrackHeader()
        },


        // =====================================================
        // BOTTOM NAVIGATION
        // =====================================================

        bottomBar = {

            val navBackStackEntry by
            mainNavController
                .currentBackStackEntryAsState()


            val currentDestination =
                navBackStackEntry
                    ?.destination


            NavigationBar {


                // =================================================
                // HOME
                // =================================================

                NavigationBarItem(

                    selected =
                        currentDestination
                            ?.route ==
                                MainRoutes.HOME,

                    onClick = {

                        selectedCategory =
                            null

                        navigateToMainRoute(
                            MainRoutes.HOME
                        )
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Home,

                            contentDescription =
                                "Home"
                        )
                    },

                    label = {

                        Text(
                            "Home"
                        )
                    }
                )


                // =================================================
                // RECORDS
                // =================================================

                NavigationBarItem(

                    selected =
                        currentDestination
                            ?.route ==
                                MainRoutes.RECORDS,

                    onClick = {

                        selectedCategory =
                            null

                        navigateToMainRoute(
                            MainRoutes.RECORDS
                        )
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.List,

                            contentDescription =
                                "Records"
                        )
                    },

                    label = {

                        Text(
                            "Records"
                        )
                    }
                )


                // =================================================
                // SCAN
                // =================================================

                NavigationBarItem(

                    selected =
                        currentDestination
                            ?.route ==
                                MainRoutes.SCAN,

                    onClick = {

                        navigateToMainRoute(
                            MainRoutes.SCAN
                        )
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.CameraAlt,

                            contentDescription =
                                "Scan"
                        )
                    },

                    label = {

                        Text(
                            "Scan"
                        )
                    }
                )


                // =================================================
                // SETTINGS
                // =================================================

                NavigationBarItem(

                    selected =
                        currentDestination
                            ?.route ==
                                MainRoutes.SETTINGS,

                    onClick = {

                        navigateToMainRoute(
                            MainRoutes.SETTINGS
                        )
                    },

                    icon = {

                        Icon(
                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                "Settings"
                        )
                    },

                    label = {

                        Text(
                            "Settings"
                        )
                    }
                )
            }
        }

    ) { paddingValues ->


        // =========================================================
        // INTERNAL NAVIGATION
        // =========================================================

        NavHost(

            navController =
                mainNavController,

            startDestination =
                MainRoutes.HOME,

            modifier =
                Modifier.padding(
                    paddingValues
                )

        ) {


            // =====================================================
            // HOME
            // =====================================================

            composable(
                MainRoutes.HOME
            ) {

                HomeScreen(

                    recordViewModel =
                        recordViewModel,

                    userName =
                        userName,


                    // -------------------------------------------------
                    // SEARCH
                    // -------------------------------------------------

                    onSearchClick = {

                        selectedCategory =
                            null

                        navigateToMainRoute(
                            MainRoutes.RECORDS
                        )
                    },


                    // -------------------------------------------------
                    // SCAN
                    // -------------------------------------------------

                    onScanClick = {

                        navigateToMainRoute(
                            MainRoutes.SCAN
                        )
                    },


                    // -------------------------------------------------
                    // ADD RECORD
                    // -------------------------------------------------

                    onAddRecordClick = {

                        rootNavController
                            .navigate(
                                Routes.ADD_RECORD
                            )
                    },


                    // -------------------------------------------------
                    // CATEGORY
                    // -------------------------------------------------

                    onCategoryClick = { category ->

                        selectedCategory =
                            category

                        navigateToMainRoute(
                            MainRoutes.RECORDS
                        )
                    },


                    // -------------------------------------------------
                    // EXPIRING RECORD
                    // -------------------------------------------------

                    onExpiringRecordClick = {

                        selectedCategory =
                            null

                        navigateToMainRoute(
                            MainRoutes.RECORDS
                        )
                    }
                )
            }


            // =====================================================
            // RECORDS
            // =====================================================

            composable(
                MainRoutes.RECORDS
            ) {

                RecordsScreen(

                    recordViewModel =
                        recordViewModel,

                    selectedCategory =
                        selectedCategory,


                    onRecordClick = { recordId ->

                        rootNavController
                            .navigate(
                                Routes.detailsRoute(
                                    recordId
                                )
                            )
                    },


                    onAddRecordClick = {

                        rootNavController
                            .navigate(
                                Routes.ADD_RECORD
                            )
                    }
                )
            }


            // =====================================================
            // SCAN
            // =====================================================

            composable(
                MainRoutes.SCAN
            ) {

                ScanDocumentScreen(

                    onImageSelected = { uri ->

                        /*
                         * Store the selected document URI
                         * in the current Scan back-stack entry.
                         */

                        mainNavController
                            .currentBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "documentUri",
                                uri.toString()
                            )

                        /*
                         * Open the Document/OCR screen.
                         */

                        mainNavController.navigate(
                            MainRoutes.DOCUMENT
                        )
                    },

                    /*
                     * Back from Scan screen.
                     *
                     * Return to the previous main
                     * navigation destination.
                     */

                    onBackClick = {

                        mainNavController
                            .popBackStack()
                    }
                )
            }


            // =====================================================
            // DOCUMENT / OCR
            // =====================================================

            composable(
                MainRoutes.DOCUMENT
            ) {

                val imageUri =
                    remember {
                        mainNavController
                            .previousBackStackEntry
                            ?.savedStateHandle
                            ?.get<String>(
                                "documentUri"
                            )
                    }

                androidx.compose.runtime.LaunchedEffect(imageUri) {
                    mainNavController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("documentUri")
                }


                if (
                    !imageUri
                        .isNullOrBlank()
                ) {

                    DocumentOcrScreen(

                        imagePath =
                            imageUri,

                        recordViewModel =
                            recordViewModel,

                        onNavigateToRecords = {
                            navigateToMainRoute(
                                MainRoutes.RECORDS
                            )
                        },

                        onBackClick = {

                            mainNavController
                                .popBackStack()
                        }
                    )

                } else {

                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        mainNavController
                            .popBackStack()
                    }
                }
            }


            // =====================================================
            // SETTINGS
            // =====================================================

            composable(
                MainRoutes.SETTINGS
            ) {

                SettingsScreen(

                    // -------------------------------------------------
                    // EDIT PROFILE
                    // -------------------------------------------------

                    onEditProfileClick = {

                        rootNavController
                            .navigate(
                                Routes.EDIT_PROFILE
                            )
                    },

                    // -------------------------------------------------
                    // LOGOUT
                    // -------------------------------------------------

                    onLogoutClick = {
                        showLogoutDialog = true
                    },


                    // -------------------------------------------------
                    // NOTIFICATIONS
                    // -------------------------------------------------

                    onNotificationsClick = {

                        try {

                            if (
                                Build.VERSION.SDK_INT >=
                                Build.VERSION_CODES.O
                            ) {

                                val intent =
                                    Intent(
                                        Settings
                                            .ACTION_APP_NOTIFICATION_SETTINGS
                                    ).apply {

                                        putExtra(
                                            Settings
                                                .EXTRA_APP_PACKAGE,

                                            context
                                                .packageName
                                        )
                                    }


                                context
                                    .startActivity(
                                        intent
                                    )

                            } else {

                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_SETTINGS
                                    )
                                )
                            }

                        } catch (e: Exception) {

                            Toast.makeText(
                                context,
                                "Unable to open notification settings",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },


                    // -------------------------------------------------
                    // EXPORT
                    // -------------------------------------------------

                    onExportClick = {

                        startBackup()
                    },


                    // -------------------------------------------------
                    // GOOGLE DRIVE BACKUP
                    // -------------------------------------------------

                    onGoogleDriveBackupClick = {

                        startBackup()
                    },


                    // -------------------------------------------------
                    // IMPORT
                    // -------------------------------------------------

                    onImportClick = {

                        importLauncher.launch(

                            arrayOf(
                                "application/zip",
                                "application/json",
                                "text/*"
                            )
                        )
                    },


                    // -------------------------------------------------
                    // DELETE ALL
                    // -------------------------------------------------

                    onDeleteAllClick = {

                        showDeleteDialog =
                            true
                    },


                    // -------------------------------------------------
                    // DARK THEME
                    // -------------------------------------------------

                    darkTheme =
                        darkTheme,

                    onDarkThemeChange =
                        onDarkThemeChange
                )
            }
        }
    }


    // =============================================================
    // DELETE ALL DIALOG
    // =============================================================

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {

                showDeleteDialog =
                    false
            },


            title = {

                Text(
                    "Delete All Records?"
                )
            },


            text = {

                Text(
                    "This will permanently delete all saved records and their associated saved documents."
                )
            },


            // =====================================================
            // CONFIRM DELETE
            // =====================================================

            confirmButton = {

                TextButton(

                    onClick = {

                        // ---------------------------------------------
                        // DELETE SAVED IMAGES
                        // ---------------------------------------------

                        FileStorage
                            .listSavedImages(
                                context
                            )
                            .forEach { uri ->

                                FileStorage
                                    .deleteDocument(
                                        context,
                                        uri.toString()
                                    )
                            }


                        // ---------------------------------------------
                        // DELETE ROOM RECORDS
                        // ---------------------------------------------

                        recordViewModel
                            .deleteAllRecords()


                        showDeleteDialog =
                            false


                        Toast.makeText(
                            context,
                            "All records and saved documents deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                ) {

                    Text(
                        text = "Delete",

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            },


            // =====================================================
            // CANCEL
            // =====================================================

            dismissButton = {

                TextButton(

                    onClick = {

                        showDeleteDialog =
                            false
                    }

                ) {

                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }

    // =============================================================
    // LOGOUT CONFIRMATION DIALOG
    // =============================================================

    if (showLogoutDialog) {

        AlertDialog(

            onDismissRequest = {
                showLogoutDialog = false
            },

            title = {
                Text("Log Out?")
            },

            text = {
                Text("Are you sure you want to log out? Your saved records and documents will remain safe on this device.")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    }
                ) {
                    Text(
                        text = "Log Out",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


// =============================================================
// HEADER
// =============================================================

@Composable
private fun KeepTrackHeader() {

    Surface(

        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding(),

        /*
         * Uses the current MaterialTheme so the header
         * will also respond correctly when Dark Theme
         * is enabled.
         */

        color =
            MaterialTheme
                .colorScheme
                .primaryContainer

    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp
                    )

        ) {

            Text(

                text =
                    "KeepTrack",

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )


            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )


            Text(

                text =
                    "Keep your important things organized.",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer
            )
        }
    }
}