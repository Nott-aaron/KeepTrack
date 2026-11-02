package com.example.keeptrack.ui.screens.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.ui.components.KeepTrackButton
import com.example.keeptrack.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordDetailsScreen(
    record: RecordEntity,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onReceiptClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        // =====================================================
        // HEADER
        // =====================================================

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",

                    // DARK THEME FIX
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Record Details",
                style = MaterialTheme.typography.titleLarge,

                // DARK THEME FIX
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        // =====================================================
        // RECORD TITLE
        // =====================================================

        Text(
            text = record.title,
            style = MaterialTheme.typography.headlineMedium,

            // DARK THEME FIX
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )


        // =====================================================
        // CATEGORY
        // =====================================================

        Text(
            text = record.category,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )


        // =====================================================
        // STATUS
        // =====================================================

        StatusBadge(
            status = getRecordStatus(record)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // =====================================================
        // PRODUCT & PURCHASE INFORMATION
        // =====================================================

        if (
            listOf(
                record.brand,
                record.model,
                record.serialNumber,
                record.purchasePrice?.let(::formatPrice),
                record.purchaseDate?.let(::formatDate),
                record.storeOrProvider
            ).any { it != null }
        ) {

            InformationCard(
                title = "Product & Purchase Information",

                fields = listOfNotNull(
                    record.brand?.let {
                        "Brand" to it
                    },

                    record.model?.let {
                        "Model" to it
                    },

                    record.serialNumber?.let {
                        "Serial / Registration Number" to it
                    },

                    record.purchasePrice?.let {
                        "Purchase Price" to formatPrice(it)
                    },

                    record.purchaseDate?.let {
                        "Purchase Date" to formatDate(it)
                    },

                    record.storeOrProvider?.let {
                        "Store / Provider" to it
                    }
                )
            )
        }


        // =====================================================
        // WARRANTY
        // =====================================================

        if (
            record.warrantyPeriodMonths != null ||
            record.warrantyExpiryDate != null
        ) {

            InformationCard(
                title = "Warranty",

                fields = listOfNotNull(
                    record.warrantyPeriodMonths?.let {
                        "Warranty Period" to
                                formatWarrantyPeriod(it)
                    },

                    record.warrantyExpiryDate?.let {
                        "Warranty Expiry" to
                                formatDate(it)
                    }
                )
            )
        }


        // =====================================================
        // EXPIRY
        // =====================================================

        if (
            record.expiryDate != null
        ) {

            InformationCard(
                title = "Expiry",

                fields = listOf(
                    "Expiry Date" to
                            formatDate(record.expiryDate)
                )
            )
        }


        // =====================================================
        // AGREEMENT
        // =====================================================

        if (
            record.agreementStartDate != null ||
            record.agreementEndDate != null ||
            record.monthlyAmount != null ||
            record.securityDeposit != null
        ) {

            InformationCard(
                title = "Agreement",

                fields = listOfNotNull(
                    record.agreementStartDate?.let {
                        "Agreement Start" to
                                formatDate(it)
                    },

                    record.agreementEndDate?.let {
                        "Agreement End" to
                                formatDate(it)
                    },

                    record.monthlyAmount?.let {
                        "Monthly Amount" to
                                formatPrice(it)
                    },

                    record.securityDeposit?.let {
                        "Security Deposit" to
                                formatPrice(it)
                    }
                )
            )
        }


        // =====================================================
        // INSURANCE & CONTACT
        // =====================================================

        if (
            record.policyNumber != null ||
            record.contactName != null ||
            record.contactPhone != null
        ) {

            InformationCard(
                title = "Insurance & Contact",

                fields = listOfNotNull(
                    record.policyNumber?.let {
                        "Policy Number" to it
                    },

                    record.contactName?.let {
                        "Contact Name" to it
                    },

                    record.contactPhone?.let {
                        "Phone" to it
                    }
                )
            )
        }


        // =====================================================
        // ADDITIONAL INFORMATION
        // =====================================================

        if (
            record.notes?.isNotBlank() == true
        ) {

            InformationCard(
                title = "Additional Information",

                fields = listOf(
                    "Notes" to record.notes
                )
            )
        }


        // =====================================================
        // SAVED DOCUMENT
        // =====================================================

        if (
            !record.documentPath.isNullOrBlank()
        ) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Document",
                style = MaterialTheme.typography.titleLarge,

                // DARK THEME FIX
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Saved receipt / document",

                        // DARK THEME FIX
                        color =
                            MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text =
                            "Long-press the extracted text after opening to copy only what you need.",

                        // DARK THEME FIX
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,

                        style =
                            MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    KeepTrackButton(
                        text = "View Saved Document",
                        onClick = onReceiptClick
                    )
                }
            }
        }


        // =====================================================
        // EDIT BUTTON
        // =====================================================

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        KeepTrackButton(
            text = "Edit Record",
            onClick = onEditClick
        )


        // =====================================================
        // DELETE
        // =====================================================

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {

            TextButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "  Delete Record",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }


    // =========================================================
    // DELETE CONFIRMATION DIALOG
    // =========================================================

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
            },

            title = {
                Text(
                    text = "Delete Record?"
                )
            },

            text = {
                Text(
                    text =
                        "Are you sure you want to delete \"${record.title}\"? This action cannot be undone."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}


// =============================================================
// INFORMATION CARD
// =============================================================

@Composable
private fun InformationCard(
    title: String,
    fields: List<Pair<String, String>>
) {

    Spacer(
        modifier = Modifier.height(12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = title,

                style =
                    MaterialTheme.typography.titleMedium,

                color =
                    MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )


            fields.forEachIndexed { index, (label, value) ->

                if (index > 0) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = label,

                        modifier =
                            Modifier.weight(0.42f),

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        style =
                            MaterialTheme.typography.bodyMedium
                    )


                    SelectionContainer {

                        Text(
                            text = value,

                            modifier =
                                Modifier.weight(0.58f),

                            // IMPORTANT:
                            // Actual record VALUE follows theme
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface,

                            style =
                                MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// DATE
// =============================================================

private fun formatDate(
    timestamp: Long
): String =
    SimpleDateFormat(
        "dd MMMM yyyy",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )


// =============================================================
// PRICE
// =============================================================

private fun formatPrice(
    price: Double
): String =
    "₹%.2f".format(
        Locale.getDefault(),
        price
    )


// =============================================================
// WARRANTY
// =============================================================

private fun formatWarrantyPeriod(
    months: Int
): String =
    if (months < 12) {

        "$months months"

    } else {

        val years =
            months / 12

        val remainingMonths =
            months % 12

        if (remainingMonths == 0) {

            if (years == 1) {
                "1 year"
            } else {
                "$years years"
            }

        } else {

            "$years years $remainingMonths months"
        }
    }


// =============================================================
// RECORD STATUS
// =============================================================

private fun getRecordStatus(
    record: RecordEntity
): String {

    val expiry =
        record.warrantyExpiryDate
            ?: record.expiryDate
            ?: record.agreementEndDate
            ?: return "Active"

    return if (
        expiry >= System.currentTimeMillis()
    ) {
        "Active"
    } else {
        "Expired"
    }
}