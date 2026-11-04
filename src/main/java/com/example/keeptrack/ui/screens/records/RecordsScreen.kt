package com.example.keeptrack.ui.screens.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.ui.components.KeepTrackCard
import com.example.keeptrack.ui.components.KeepTrackSearchBar
import com.example.keeptrack.viewmodel.RecordViewModel


// =============================================================
// RECORDS SCREEN
// =============================================================

@Composable
fun RecordsScreen(
    recordViewModel: RecordViewModel,
    onRecordClick: (Long) -> Unit = {},
    onAddRecordClick: () -> Unit = {},
    selectedCategory: String? = null
) {

    // =========================================================
    // FAVORITES FILTER
    // =========================================================

    var showFavoritesOnly by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // DATABASE RECORDS
    // =========================================================

    val allRecords by recordViewModel
        .allRecords
        .collectAsState(initial = emptyList())


    // =========================================================
    // FAVORITE RECORDS
    // =========================================================

    val favoriteRecords by recordViewModel
        .favoriteRecords
        .collectAsState(initial = emptyList())


    // =========================================================
    // SEARCH QUERY
    // =========================================================

    val searchQuery by recordViewModel
        .searchQuery
        .collectAsState()


    // =========================================================
    // SOURCE RECORDS
    // =========================================================

    /*
     * We already have all records in memory.
     *
     * Therefore there is no need to create a new Room query
     * every time the user types a search character.
     */

    val sourceRecords =
        if (showFavoritesOnly) {
            favoriteRecords
        } else {
            allRecords
        }


    // =========================================================
    // DISPLAYED RECORDS
    // =========================================================

    val displayedRecords =
        remember(
            sourceRecords,
            searchQuery,
            selectedCategory
        ) {

            if (
                searchQuery.isBlank() &&
                selectedCategory == null
            ) {

                sourceRecords

            } else {

                sourceRecords.filter { record ->

                    // -----------------------------------------
                    // SEARCH
                    // -----------------------------------------

                    val matchesSearch =
                        searchQuery.isBlank() ||
                                record.title.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) ||
                                record.category.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) ||
                                record.subCategory?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.brand?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.model?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.serialNumber?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.storeOrProvider?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.contactName?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.policyNumber?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true ||
                                record.notes?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true


                    // -----------------------------------------
                    // CATEGORY
                    // -----------------------------------------

                    val matchesCategory =
                        selectedCategory == null ||
                                record.category.equals(
                                    selectedCategory,
                                    ignoreCase = true
                                )


                    matchesSearch &&
                            matchesCategory
                }
            }
        }


    // =========================================================
    // SCREEN
    // =========================================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            )
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text =
                        if (showFavoritesOnly) {
                            "Favorites"
                        } else {
                            selectedCategory
                                ?: "My Records"
                        },

                    style =
                        MaterialTheme.typography.headlineMedium,

                    color =
                        MaterialTheme.colorScheme.onBackground
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text =
                        when (displayedRecords.size) {

                            0 ->
                                "No records"

                            1 ->
                                "1 record"

                            else ->
                                "${displayedRecords.size} records"
                        },

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // =================================================
            // ADD BUTTON
            // =================================================

            IconButton(
                onClick = onAddRecordClick
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Add record",

                    tint =
                        MaterialTheme.colorScheme.primary
                )
            }
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // SEARCH
        // =====================================================

        KeepTrackSearchBar(
            query = searchQuery,

            /*
             * IMPORTANT:
             *
             * Update the ViewModel's search query only.
             *
             * We no longer start a Room search Flow for
             * every character.
             */

            onQueryChange = { query ->

                recordViewModel.setSearchQuery(
                    query
                )
            },

            placeholder =
                "Search your records"
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =====================================================
        // FILTERS
        // =====================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected =
                    !showFavoritesOnly,

                onClick = {
                    showFavoritesOnly = false
                },

                label = {
                    Text("All Records")
                }
            )


            FilterChip(
                selected =
                    showFavoritesOnly,

                onClick = {
                    showFavoritesOnly = true
                },

                label = {
                    Text("Favorites")
                },

                leadingIcon = {

                    Icon(
                        imageVector =
                            if (showFavoritesOnly) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },

                        contentDescription =
                            null
                    )
                }
            )
        }


        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // RECORD LIST
        // =====================================================

        if (displayedRecords.isEmpty()) {

            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        when {

                            showFavoritesOnly &&
                                    searchQuery.isBlank() ->
                                "No favorite records"

                            searchQuery.isNotBlank() ->
                                "No matching records"

                            selectedCategory != null ->
                                "No records in this category"

                            else ->
                                "No records yet"
                        },

                    style =
                        MaterialTheme.typography.titleMedium,

                    color =
                        MaterialTheme.colorScheme.onBackground
                )


                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )


                Text(
                    text =
                        when {

                            showFavoritesOnly &&
                                    searchQuery.isBlank() ->
                                "Tap the star on a record to add it to Favorites."

                            searchQuery.isNotBlank() ->
                                "Try searching with a different keyword."

                            selectedCategory != null ->
                                "Add a record to this category."

                            else ->
                                "Add your first receipt, warranty, agreement or document."
                        },

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items =
                        displayedRecords,

                    key = {
                        it.id
                    }

                ) { record ->

                    RecordItem(
                        record = record,

                        onClick = {
                            onRecordClick(
                                record.id
                            )
                        },

                        onFavoriteClick = {
                            recordViewModel
                                .toggleFavorite(
                                    record
                                )
                        }
                    )
                }
            }
        }
    }
}


// =============================================================
// RECORD ITEM
// =============================================================

@Composable
private fun RecordItem(
    record: RecordEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {

    Row(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            KeepTrackCard(

                title =
                    record.title,

                subtitle =
                    buildSubtitle(
                        record
                    ),

                category =
                    record.category,

                trailingText =
                    buildTrailingText(
                        record
                    ),

                onClick =
                    onClick
            )
        }


        IconButton(
            onClick =
                onFavoriteClick
        ) {

            Icon(
                imageVector =
                    if (record.isFavorite) {
                        Icons.Default.Star
                    } else {
                        Icons.Default.StarBorder
                    },

                contentDescription =
                    if (record.isFavorite) {
                        "Remove from favorites"
                    } else {
                        "Add to favorites"
                    },

                tint =
                    if (record.isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
            )
        }
    }
}


// =============================================================
// SUBTITLE
// =============================================================

private fun buildSubtitle(
    record: RecordEntity
): String {

    val parts =
        mutableListOf<String>()


    record.brand?.let {

        if (it.isNotBlank()) {
            parts.add(it)
        }
    }


    record.model?.let {

        if (it.isNotBlank()) {
            parts.add(
                "Model $it"
            )
        }
    }


    record.storeOrProvider?.let {

        if (it.isNotBlank()) {
            parts.add(it)
        }
    }


    record.serialNumber?.let {

        if (it.isNotBlank()) {
            parts.add(
                "SN: $it"
            )
        }
    }


    return if (parts.isEmpty()) {

        "No additional information"

    } else {

        parts.joinToString(
            " • "
        )
    }
}


// =============================================================
// TRAILING TEXT
// =============================================================

private fun buildTrailingText(
    record: RecordEntity
): String? {

    if (
        record.warrantyExpiryDate != null
    ) {

        return "Warranty"
    }


    if (
        record.expiryDate != null
    ) {

        return "Expires"
    }


    if (
        record.agreementEndDate != null
    ) {

        return "Agreement"
    }


    return null
}