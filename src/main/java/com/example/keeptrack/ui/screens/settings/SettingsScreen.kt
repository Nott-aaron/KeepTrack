package com.example.keeptrack.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.keeptrack.ui.components.KeepTrackCard


@Composable
fun SettingsScreen(
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onGoogleDriveBackupClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onDeleteAllClick: () -> Unit = {},
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            )
    ) {

        // =========================================================
        // HEADER
        // =========================================================

        Text(
            text = "Settings",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // =========================================================
        // ACCOUNT
        // =========================================================

        Text(
            text = "Account",

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        KeepTrackCard(
            title = "Edit Profile",

            subtitle =
                "Update your personal information",

            category =
                "Profile",

            onClick =
                onEditProfileClick
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        KeepTrackCard(
            title = "Log Out",

            subtitle =
                "Sign out of your account on this device",

            category =
                "Account",

            onClick =
                onLogoutClick
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // =========================================================
        // PREFERENCES
        // =========================================================

        Text(
            text = "Preferences",

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // =========================================================
        // NOTIFICATIONS
        // =========================================================

        KeepTrackCard(
            title = "Notifications",

            subtitle =
                "Manage expiry and reminder notifications",

            category =
                "Preferences",

            onClick =
                onNotificationsClick
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // =========================================================
        // DARK THEME
        // =========================================================

        KeepTrackCard(

            title =
                "Dark Theme",

            subtitle =
                if (darkTheme) {

                    "Dark theme is currently enabled"

                } else {

                    "Use a dark appearance throughout KeepTrack"
                },

            category =
                if (darkTheme) {

                    "Appearance • Dark"

                } else {

                    "Appearance • Light"
                },

            /*
             * Actual Switch is now INSIDE the card.
             */

            trailingContent = {

                Switch(

                    checked =
                        darkTheme,

                    onCheckedChange = { enabled ->

                        onDarkThemeChange(
                            enabled
                        )
                    }
                )
            },

            /*
             * Tapping the card itself also toggles the theme.
             */

            onClick = {

                onDarkThemeChange(
                    !darkTheme
                )
            }
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // =========================================================
        // DATA & BACKUP
        // =========================================================

        Text(
            text = "Data & Backup",

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        // =========================================================
        // EXPORT BACKUP
        // =========================================================

        KeepTrackCard(

            title =
                "Export Backup",

            subtitle =
                "Save records and saved images as one backup file",

            category =
                "Local / device storage",

            onClick =
                onExportClick
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // =========================================================
        // GOOGLE DRIVE BACKUP
        // =========================================================

        KeepTrackCard(

            title =
                "Backup to Google Drive",

            subtitle =
                "Choose your Google Drive account and save the backup there",

            category =
                "Cloud backup",

            onClick =
                onGoogleDriveBackupClick
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // =========================================================
        // IMPORT BACKUP
        // =========================================================

        KeepTrackCard(

            title =
                "Import Backup",

            subtitle =
                "Restore records and saved images from a backup file",

            category =
                "Restore",

            onClick =
                onImportClick
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        // =========================================================
        // DELETE ALL RECORDS
        // =========================================================

        KeepTrackCard(

            title =
                "Delete All Records",

            subtitle =
                "Permanently remove all saved records",

            category =
                "Danger Zone",

            onClick =
                onDeleteAllClick
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        // =========================================================
        // ABOUT US
        // =========================================================

        Text(
            text = "About Us",

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        KeepTrackCard(

            title =
                "KeepTrack",

            subtitle =
                "KeepTrack helps you store, organize and manage " +
                        "your important records, documents and expiry " +
                        "information in one place.",

            category =
                "Version 1.0"
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Text(
            text =
                "Your records are stored locally on your device. " +
                        "You can create backups to protect your " +
                        "important information.",

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )
    }
}