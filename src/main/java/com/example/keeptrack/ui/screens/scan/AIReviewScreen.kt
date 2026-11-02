package com.example.keeptrack.ui.screens.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keeptrack.ui.components.KeepTrackButton

@Composable
fun AIReviewScreen(
    initialTitle: String = "",
    initialCategory: String = "",
    initialBrand: String = "",
    initialModel: String = "",
    initialPrice: String = "",
    initialStore: String = "",
    initialNotes: String = "",
    onSave: (
        title: String,
        category: String,
        brand: String,
        model: String,
        price: String,
        store: String,
        notes: String
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onCancel: () -> Unit = {}
) {

    var title by remember {
        mutableStateOf(initialTitle)
    }

    var category by remember {
        mutableStateOf(initialCategory)
    }

    var brand by remember {
        mutableStateOf(initialBrand)
    }

    var model by remember {
        mutableStateOf(initialModel)
    }

    var price by remember {
        mutableStateOf(initialPrice)
    }

    var store by remember {
        mutableStateOf(initialStore)
    }

    var notes by remember {
        mutableStateOf(initialNotes)
    }

    val isFormValid =
        title.isNotBlank() &&
                category.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 24.dp
            ),
        verticalArrangement =
            Arrangement.Top
    ) {

        Text(
            text = "Review Extracted Information",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = "Review and correct the information extracted by AI before saving.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Title")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = category,
            onValueChange = {
                category = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Category")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = brand,
            onValueChange = {
                brand = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Brand")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Model")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Purchase Price")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = store,
            onValueChange = {
                store = it
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text("Store / Provider")
            }
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            label = {
                Text("Notes")
            }
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        KeepTrackButton(
            text = "Save Record",
            enabled = isFormValid,
            onClick = {

                onSave(
                    title.trim(),
                    category.trim(),
                    brand.trim(),
                    model.trim(),
                    price.trim(),
                    store.trim(),
                    notes.trim()
                )
            }
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        KeepTrackButton(
            text = "Cancel",
            onClick = onCancel
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}