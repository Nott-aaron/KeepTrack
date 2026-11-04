package com.example.keeptrack

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.compose.rememberNavController

import com.example.keeptrack.data.local.DatabaseProvider
import com.example.keeptrack.data.repository.RecordRepository

import com.example.keeptrack.navigation.AppNavigation

import com.example.keeptrack.ui.theme.KeepTrackTheme

import com.example.keeptrack.viewmodel.RecordViewModel
import com.example.keeptrack.viewmodel.RecordViewModelFactory


class MainActivity : ComponentActivity() {

    companion object {

        private const val PREFS_NAME =
            "KeepTrackPreferences"

        private const val DARK_THEME_KEY =
            "darkTheme"
    }


    // =========================================================
    // THEME STATE
    // =========================================================

    private var darkThemeState by mutableStateOf(false)


    // =========================================================
    // ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )


        // =====================================================
        // LOAD SAVED THEME
        // =====================================================

        val preferences =
            getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
            )

        darkThemeState =
            preferences.getBoolean(
                DARK_THEME_KEY,
                false
            )


        // =====================================================
        // EDGE TO EDGE
        // =====================================================

        enableEdgeToEdge()


        // =====================================================
        // DATABASE
        // =====================================================

        val database =
            DatabaseProvider.getDatabase(
                applicationContext
            )


        // =====================================================
        // DAO
        // =====================================================

        val recordDao =
            database.recordDao()


        // =====================================================
        // REPOSITORY
        // =====================================================

        val repository =
            RecordRepository(
                recordDao
            )


        // =====================================================
        // VIEWMODEL FACTORY
        // =====================================================

        val viewModelFactory =
            RecordViewModelFactory(
                repository
            )


        // =====================================================
        // COMPOSE
        // =====================================================

        setContent {

            /*
             * The entire application is placed inside
             * KeepTrackTheme.
             *
             * Whenever darkThemeState changes,
             * Compose recomposes this theme.
             */

            KeepTrackTheme(
                darkTheme = darkThemeState
            ) {

                val recordViewModel:
                        RecordViewModel =
                    viewModel(
                        factory = viewModelFactory
                    )


                val navController =
                    rememberNavController()


                // =================================================
                // NAVIGATION
                // =================================================

                AppNavigation(

                    navController =
                        navController,

                    recordViewModel =
                        recordViewModel,

                    /*
                     * SEND CURRENT THEME STATE
                     * TO APP NAVIGATION
                     */

                    darkTheme =
                        darkThemeState,

                    /*
                     * SEND THEME CHANGE CALLBACK
                     */

                    onDarkThemeChange = { enabled ->

                        setDarkTheme(
                            enabled
                        )
                    }
                )
            }
        }
    }


    // =============================================================
    // CHANGE THEME
    // =============================================================

    fun setDarkTheme(
        enabled: Boolean
    ) {

        // ---------------------------------------------------------
        // UPDATE COMPOSE STATE
        // ---------------------------------------------------------

        darkThemeState =
            enabled


        // ---------------------------------------------------------
        // SAVE PREFERENCE
        // ---------------------------------------------------------

        getSharedPreferences(
            PREFS_NAME,
            MODE_PRIVATE
        )
            .edit()
            .putBoolean(
                DARK_THEME_KEY,
                enabled
            )
            .apply()
    }
}