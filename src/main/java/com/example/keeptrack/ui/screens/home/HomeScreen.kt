package com.example.keeptrack.ui.screens.home

import android.content.Context

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.example.keeptrack.ui.components.CategoryCard
import com.example.keeptrack.ui.components.KeepTrackButton
import com.example.keeptrack.ui.components.KeepTrackCard
import com.example.keeptrack.ui.components.KeepTrackSearchBar
import com.example.keeptrack.viewmodel.RecordViewModel


// =============================================================
// DASHBOARD CATEGORY
// =============================================================

private data class DashboardCategory(
    val title: String,
    val icon: ImageVector
)


// =============================================================
// DASHBOARD CATEGORIES
// =============================================================

private val dashboardCategories = listOf(

    DashboardCategory(
        title = "Electronics",
        icon = Icons.Default.PhoneAndroid
    ),

    DashboardCategory(
        title = "Home Appliance",
        icon = Icons.Default.Home
    ),

    DashboardCategory(
        title = "Vehicles",
        icon = Icons.Default.DirectionsCar
    ),

    DashboardCategory(
        title = "Documents",
        icon = Icons.Default.Description
    ),

    DashboardCategory(
        title = "Insurance",
        icon = Icons.Default.Security
    ),

    DashboardCategory(
        title = "Service & Maintenance",
        icon = Icons.Default.Build
    )
)


// =============================================================
// EXPIRY ALERT PREFERENCES
// =============================================================

private const val PREFS_NAME = "keeptrack_expiry_alerts"


// =============================================================
// HOME SCREEN
// =============================================================

@Composable
fun HomeScreen(
    recordViewModel: RecordViewModel,
    userName: String = "there",
    onSearchClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onAddRecordClick: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onExpiringRecordClick: () -> Unit = {}
) {

    val context = LocalContext.current

    // =========================================================
    // DATABASE DATA
    // =========================================================

    val allRecords by recordViewModel
        .allRecords
        .collectAsState(initial = emptyList())

    val expiringRecords by recordViewModel
        .expiringRecords
        .collectAsState(initial = emptyList())


    // =========================================================
    // EXPIRY TIME
    // =========================================================

    val now = remember {
        System.currentTimeMillis()
    }

    val expiryLimit =
        remember(now) {
            now + (30L * 24L * 60L * 60L * 1000L)
        }


    // =========================================================
    // AVAILABLE CATEGORIES
    // =========================================================

    /*
     * Build the set of categories currently stored in Room.
     *
     * This is calculated only when allRecords changes.
     */

    val availableCategoryTitles =
        remember(allRecords) {

            allRecords
                .asSequence()
                .map {
                    it.category.lowercase()
                }
                .toSet()
        }


    val availableCategories =
        remember(availableCategoryTitles) {

            dashboardCategories.filter { category ->

                category.title.lowercase() in
                        availableCategoryTitles
            }
        }


    // =========================================================
    // EXPIRY SIGNATURES
    // =========================================================

    /*
     * Calculate expiry information once whenever the
     * database records change.
     */

    val expirySignatures =
        remember(
            allRecords,
            now,
            expiryLimit
        ) {

            val signatures =
                mutableMapOf<String, String>()

            for (category in dashboardCategories) {

                val categorySignature =
                    allRecords
                        .asSequence()
                        .filter { record ->

                            if (
                                !record.category.equals(
                                    category.title,
                                    ignoreCase = true
                                )
                            ) {
                                false
                            } else {

                                val expiry =
                                    record.warrantyExpiryDate
                                        ?: record.expiryDate
                                        ?: record.agreementEndDate

                                expiry != null &&
                                        expiry in now..expiryLimit
                            }
                        }
                        .mapNotNull { record ->

                            val expiry =
                                record.warrantyExpiryDate
                                    ?: record.expiryDate
                                    ?: record.agreementEndDate

                            expiry?.let {
                                "${record.title}|$it"
                            }
                        }
                        .sorted()
                        .joinToString("|")

                signatures[category.title] =
                    categorySignature
            }

            signatures
        }


    // =========================================================
    // VIEWED EXPIRY SIGNATURES
    // =========================================================

    val viewedExpirySignatures =
        remember(allRecords) {

            val preferences =
                context.getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

            dashboardCategories.associate { category ->

                category.title to
                        preferences.getString(
                            category.title,
                            ""
                        ).orEmpty()
            }
        }


    // =========================================================
    // MAIN HOME CONTENT
    // =========================================================

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

        // =====================================================
        // GREETING
        // =====================================================

        Text(
            text = "Good morning, $userName 👋",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Keep your important things organized.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // SEARCH
        // =====================================================

        KeepTrackSearchBar(
            query = "",
            onQueryChange = {
                onSearchClick()
            },
            placeholder = "Search your records"
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================================
        // QUICK ACTIONS
        // =====================================================

        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            KeepTrackButton(
                text = "Scan Receipt",
                onClick = onScanClick,
                modifier = Modifier.weight(1f)
            )

            KeepTrackButton(
                text = "Add Record",
                onClick = onAddRecordClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // =====================================================
        // CATEGORIES
        // =====================================================

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (availableCategories.isEmpty()) {

            Text(
                text = "No categories available yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {

            /*
             * IMPORTANT:
             *
             * We are intentionally NOT using LazyVerticalGrid here.
             *
             * HomeScreen already has a verticalScroll().
             *
             * Since there are only a maximum of 6 dashboard
             * categories, normal Rows are much cheaper.
             */

            availableCategories
                .chunked(2)
                .forEach { rowCategories ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        rowCategories.forEach { category ->

                            val expirySignature =
                                expirySignatures[
                                    category.title
                                ].orEmpty()

                            val viewedSignature =
                                viewedExpirySignatures[
                                    category.title
                                ].orEmpty()

                            val hasExpiryAlert =
                                expirySignature.isNotBlank() &&
                                        expirySignature != viewedSignature


                            CategoryCard(
                                title = category.title,

                                icon = category.icon,

                                hasExpiryAlert =
                                    hasExpiryAlert,

                                onClick = {

                                    // ---------------------------------
                                    // MARK EXPIRY ALERT AS VIEWED
                                    // ---------------------------------

                                    if (
                                        expirySignature.isNotBlank()
                                    ) {

                                        context
                                            .getSharedPreferences(
                                                PREFS_NAME,
                                                Context.MODE_PRIVATE
                                            )
                                            .edit()
                                            .putString(
                                                category.title,
                                                expirySignature
                                            )
                                            .apply()
                                    }


                                    // ---------------------------------
                                    // OPEN CATEGORY
                                    // ---------------------------------

                                    onCategoryClick(
                                        category.title
                                    )
                                },

                                modifier =
                                    Modifier.weight(1f)
                            )
                        }

                        /*
                         * If the last row contains only one card,
                         * add an empty weighted space so that the
                         * card keeps the same width as the others.
                         */

                        if (rowCategories.size == 1) {

                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )
                }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // =====================================================
        // EXPIRING SOON
        // =====================================================

        Text(
            text = "Expiring Soon",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        if (expiringRecords.isEmpty()) {

            KeepTrackCard(

                title =
                    "No records expiring soon",

                subtitle =
                    "Your warranties and documents are up to date.",

                category =
                    "All clear",

                trailingText =
                    null,

                onClick =
                    onExpiringRecordClick
            )

        } else {

            val firstExpiringRecord =
                expiringRecords.first()

            KeepTrackCard(

                title =
                    firstExpiringRecord.title,

                subtitle =
                    firstExpiringRecord.category,

                category =
                    "Expiring Soon",

                trailingText =
                    "View",

                onClick =
                    onExpiringRecordClick
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================================
        // MAIN SCAN ACTION
        // =====================================================

        KeepTrackButton(
            text = "Scan a Receipt or Document",
            onClick = onScanClick
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )
    }
}