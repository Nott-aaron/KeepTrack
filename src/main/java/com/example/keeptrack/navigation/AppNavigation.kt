package com.example.keeptrack.navigation

import android.net.Uri
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.keeptrack.data.local.UserPreferences
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.data.storage.FileStorage
import com.example.keeptrack.ui.screens.addrecord.AddRecordScreen
import com.example.keeptrack.ui.screens.auth.LoginScreen
import com.example.keeptrack.ui.screens.auth.RegisterScreen
import com.example.keeptrack.ui.screens.details.RecordDetailsScreen
import com.example.keeptrack.ui.screens.editrecord.EditRecordScreen
import com.example.keeptrack.ui.screens.main.MainScreen
import com.example.keeptrack.ui.screens.onboarding.OnboardingScreen
import com.example.keeptrack.ui.screens.profile.ProfileSetupScreen
import com.example.keeptrack.ui.screens.scan.AIProcessingScreen
import com.example.keeptrack.ui.screens.scan.AIReviewScreen
import com.example.keeptrack.ui.screens.scan.DocumentOcrScreen
import com.example.keeptrack.ui.screens.splash.SplashScreen
import com.example.keeptrack.viewmodel.RecordViewModel

import com.example.keeptrack.data.backup.BackupManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// =============================================================
// ROUTES
// =============================================================

object Routes {

    const val SPLASH = "splash"

    const val ONBOARDING = "onboarding"

    const val LOGIN = "login"

    const val REGISTER = "register"

    const val PROFILE = "profile"

    const val EDIT_PROFILE = "edit_profile"

    /*
     * MainScreen is the owner of:
     *
     * Home
     * Records
     * Scan
     * Settings
     */
    const val HOME = "home"

    const val DETAILS = "details/{recordId}"

    const val ADD_RECORD = "add_record"

    const val EDIT_RECORD = "edit_record/{recordId}"

    /*
     * Full-screen document/OCR destination.
     */
    const val DOCUMENT = "document"

    const val AI_PROCESSING = "ai_processing"

    const val AI_REVIEW = "ai_review"


    fun detailsRoute(recordId: Long): String {
        return "details/$recordId"
    }


    fun editRecordRoute(recordId: Long): String {
        return "edit_record/$recordId"
    }
}


// =============================================================
// APP NAVIGATION
// =============================================================

@Composable
fun AppNavigation(
    navController: NavHostController,
    recordViewModel: RecordViewModel,

    // =========================================================
    // DARK THEME
    // =========================================================
    //
    // These values come from MainActivity.
    // The entire AppNavigation is already inside KeepTrackTheme,
    // so Onboarding and ProfileSetup automatically receive the
    // correct MaterialTheme colors.
    //
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {}
) {

    val context = LocalContext.current

    val userPreferences = remember {
        UserPreferences(context)
    }


    // =========================================================
    // NAVIGATION HOST
    // =========================================================

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {


        // =====================================================
        // SPLASH
        // =====================================================

        composable(Routes.SPLASH) {

            SplashScreen(

                onSplashFinished = {

                    val destination = when {
                        userPreferences.isLoggedIn() -> Routes.HOME
                        userPreferences.isOnboardingCompleted() -> Routes.LOGIN
                        else -> Routes.ONBOARDING
                    }

                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // ONBOARDING
        // =====================================================

        composable(Routes.ONBOARDING) {

            OnboardingScreen(

                onGetStarted = {

                    userPreferences.setOnboardingCompleted(true)

                    navController.navigate(
                        Routes.LOGIN
                    ) {
                        popUpTo(Routes.ONBOARDING) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // LOGIN
        // =====================================================

        composable(Routes.LOGIN) {

            LoginScreen(

                onLoginSuccess = {

                    navController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToRegister = {

                    navController.navigate(
                        Routes.REGISTER
                    )
                },

                onLoginClick = { email, password ->

                    userPreferences.loginUser(email, password)
                }
            )
        }


        // =====================================================
        // REGISTER
        // =====================================================

        composable(Routes.REGISTER) {

            RegisterScreen(

                onRegisterSuccess = {

                    navController.navigate(
                        Routes.HOME
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },

                onNavigateToLogin = {

                    navController.popBackStack()
                },

                onRegisterClick = { name, email, password, phone ->

                    userPreferences.registerUser(
                        name = name,
                        email = email,
                        password = password,
                        phone = phone
                    )
                }
            )
        }


        // =====================================================
        // PROFILE SETUP
        // =====================================================

        composable(Routes.PROFILE) {

            ProfileSetupScreen(

                initialName =
                    userPreferences.getName(),

                initialEmail =
                    userPreferences.getEmail(),

                initialPhone =
                    userPreferences.getPhone(),

                initialGender =
                    userPreferences.getGender(),

                isEditing = false,

                onContinue = {
                        name,
                        email,
                        phone,
                        gender ->

                    userPreferences.saveProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        gender = gender
                    )

                    navController.navigate(
                        Routes.HOME
                    ) {

                        popUpTo(
                            Routes.PROFILE
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // EDIT PROFILE
        // =====================================================

        composable(Routes.EDIT_PROFILE) {

            ProfileSetupScreen(

                initialName =
                    userPreferences.getName(),

                initialEmail =
                    userPreferences.getEmail(),

                initialPhone =
                    userPreferences.getPhone(),

                initialGender =
                    userPreferences.getGender(),

                isEditing = true,

                onContinue = {
                        name,
                        email,
                        phone,
                        gender ->

                    userPreferences.saveProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        gender = gender
                    )

                    navController.popBackStack()
                },

                onCancel = {

                    navController.popBackStack()
                }
            )
        }


        // =====================================================
        // MAIN SCREEN
        // =====================================================

        composable(Routes.HOME) {

            MainScreen(

                rootNavController = navController,
                recordViewModel = recordViewModel,

                userName =
                    userPreferences.getName().ifBlank { "there" },

                darkTheme = darkTheme,

                onDarkThemeChange = onDarkThemeChange,

                onLogoutConfirmed = {

                    userPreferences.logout()

                    navController.navigate(
                        Routes.LOGIN
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }


        // =====================================================
        // RECORD DETAILS
        // =====================================================

        composable(

            route = Routes.DETAILS,

            arguments = listOf(

                navArgument("recordId") {

                    type =
                        NavType.LongType
                }
            )

        ) { backStackEntry ->

            val recordId =
                backStackEntry
                    .arguments
                    ?.getLong("recordId")


            LaunchedEffect(recordId) {

                if (recordId != null) {

                    recordViewModel
                        .loadRecord(recordId)
                }
            }


            val record by
            recordViewModel
                .selectedRecord
                .collectAsState()


            if (record != null) {

                RecordDetailsScreen(

                    record =
                        record!!,

                    onBackClick = {

                        recordViewModel
                            .clearSelectedRecord()

                        navController
                            .popBackStack()
                    },


                    onEditClick = {

                        navController.navigate(
                            Routes.editRecordRoute(
                                record!!.id
                            )
                        )
                    },


                    onDeleteClick = {

                        record!!
                            .documentPath
                            ?.let { path ->

                                FileStorage
                                    .deleteDocument(
                                        context,
                                        path
                                    )
                            }


                        recordViewModel
                            .deleteRecord(
                                record!!
                            )


                        recordViewModel
                            .clearSelectedRecord()


                        navController
                            .popBackStack()
                    },


                    onReceiptClick = {

                        val documentPath =
                            record!!
                                .documentPath


                        if (
                            !documentPath
                                .isNullOrBlank()
                        ) {

                            /*
                             * Save URI in SavedStateHandle.
                             *
                             * Never put content:// URI
                             * directly into a route.
                             */

                            navController
                                .currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(
                                    "documentUri",
                                    documentPath
                                )


                            navController.navigate(
                                Routes.DOCUMENT
                            )
                        }
                    }
                )
            }
        }


        // =====================================================
        // ADD RECORD
        // =====================================================

        composable(Routes.ADD_RECORD) {

            AddRecordScreen(

                onSave = { record ->

                    recordViewModel
                        .addRecord(record)

                    navController
                        .popBackStack()
                },

                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =====================================================
        // EDIT RECORD
        // =====================================================

        composable(

            route = Routes.EDIT_RECORD,

            arguments = listOf(

                navArgument("recordId") {

                    type =
                        NavType.LongType
                }
            )

        ) { backStackEntry ->

            val recordId =
                backStackEntry
                    .arguments
                    ?.getLong("recordId")


            LaunchedEffect(recordId) {

                if (recordId != null) {

                    recordViewModel
                        .loadRecord(recordId)
                }
            }


            val record by
            recordViewModel
                .selectedRecord
                .collectAsState()


            if (record != null) {

                EditRecordScreen(

                    record =
                        record!!,

                    onSave = { updatedRecord ->

                        recordViewModel
                            .updateRecord(
                                updatedRecord
                            )

                        navController
                            .popBackStack()
                    },

                    onCancel = {

                        navController
                            .popBackStack()
                    }
                )
            }
        }


        // =====================================================
        // DOCUMENT / OCR
        // =====================================================

        composable(Routes.DOCUMENT) {

            /*
             * The previous screen stores the image URI
             * in SavedStateHandle.
             */

            val imageUri =
                remember {
                    navController
                        .previousBackStackEntry
                        ?.savedStateHandle
                        ?.get<String>(
                            "documentUri"
                        )
                }

            LaunchedEffect(imageUri) {
                navController
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
                        navController.navigate(
                            Routes.HOME
                        ) {
                            popUpTo(
                                Routes.HOME
                            ) {
                                inclusive = true
                            }
                        }
                    },

                    onBackClick = {

                        navController
                            .popBackStack()
                    }
                )

            } else {

                LaunchedEffect(Unit) {
                    navController
                        .popBackStack()
                }
            }
        }


        // =====================================================
        // AI PROCESSING
        // =====================================================

        composable(
            Routes.AI_PROCESSING
        ) {

            val imageUri =
                navController
                    .previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<String>(
                        "aiImageUri"
                    )


            AIProcessingScreen(

                imageUri =
                    imageUri,

                onProcessingComplete = {

                    navController.navigate(
                        Routes.AI_REVIEW
                    ) {

                        popUpTo(
                            Routes.AI_PROCESSING
                        ) {
                            inclusive = true
                        }
                    }
                },

                onCancel = {

                    navController
                        .popBackStack()
                }
            )
        }


        // =====================================================
        // AI REVIEW
        // =====================================================

        composable(
            Routes.AI_REVIEW
        ) {

            AIReviewScreen(

                onSave = {
                        title,
                        category,
                        brand,
                        model,
                        price,
                        store,
                        notes ->

                    val purchasePrice =
                        price
                            .replace(
                                "₹",
                                ""
                            )
                            .replace(
                                ",",
                                ""
                            )
                            .trim()
                            .toDoubleOrNull()


                    val newRecord =
                        RecordEntity(

                            title =
                                title.trim(),

                            category =
                                category.trim(),

                            brand =
                                brand
                                    .trim()
                                    .ifBlank {
                                        null
                                    },

                            model =
                                model
                                    .trim()
                                    .ifBlank {
                                        null
                                    },

                            purchasePrice =
                                purchasePrice,

                            storeOrProvider =
                                store
                                    .trim()
                                    .ifBlank {
                                        null
                                    },

                            notes =
                                notes
                                    .trim()
                                    .ifBlank {
                                        null
                                    }
                        )


                    recordViewModel
                        .addRecord(
                            newRecord
                        )


                    navController.navigate(
                        Routes.HOME
                    ) {

                        popUpTo(
                            Routes.HOME
                        ) {
                            inclusive = true
                        }
                    }
                },

                onCancel = {

                    navController
                        .popBackStack()
                }
            )
        }
    }
}