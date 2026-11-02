package com.example.keeptrack.ui.screens.scan

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.data.remote.GroqRepository
import com.example.keeptrack.ui.components.KeepTrackButton
import com.example.keeptrack.viewmodel.RecordViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DocumentOcrScreen(
    imagePath: String,
    recordViewModel: RecordViewModel? = null,
    onNavigateToRecords: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uri = remember(imagePath) {
        Uri.parse(imagePath)
    }

    var bitmap by remember {
        mutableStateOf(
            runCatching {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            }.getOrNull()
        )
    }

    var ocrText by remember { mutableStateOf("") }
    var ocrLoading by remember { mutableStateOf(true) }
    var ocrError by remember { mutableStateOf<String?>(null) }

    var aiLoading by remember { mutableStateOf(true) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }

    // Editable form states
    var storeName by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Documents") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var warrantyPeriodMonths by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    fun normalizeCategory(input: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("electronic") || lower.contains("phone") || lower.contains("gadget") || lower.contains("tech") -> "Electronics"
            lower.contains("appliance") || lower.contains("fridge") || lower.contains("washer") || lower.contains("home") -> "Home Appliance"
            lower.contains("machine") || lower.contains("tool") || lower.contains("equipment") -> "Machines & Equipment"
            lower.contains("vehicle") || lower.contains("car") || lower.contains("auto") || lower.contains("bike") -> "Vehicles"
            lower.contains("rent") || lower.contains("property") || lower.contains("lease") -> "Rental / Property"
            lower.contains("insurance") || lower.contains("policy") -> "Insurance"
            lower.contains("service") || lower.contains("repair") || lower.contains("maintenance") -> "Service & Maintenance"
            lower.contains("card") || lower.contains("id") || lower.contains("doc") || lower.contains("receipt") || lower.contains("bill") || lower.contains("invoice") -> "Documents"
            else -> input.ifBlank { "Documents" }
        }
    }

    fun inferCategoryFromText(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("phone") || lower.contains("laptop") || lower.contains("tv") ||
            lower.contains("computer") || lower.contains("headphone") || lower.contains("macbook") ||
            lower.contains("iphone") || lower.contains("samsung") || lower.contains("apple") ||
            lower.contains("electronic") -> "Electronics"

            lower.contains("refrigerator") || lower.contains("fridge") || lower.contains("washer") ||
            lower.contains("dryer") || lower.contains("oven") || lower.contains("microwave") ||
            lower.contains("appliance") -> "Home Appliance"

            lower.contains("car") || lower.contains("auto") || lower.contains("vehicle") ||
            lower.contains("bike") || lower.contains("tyre") || lower.contains("tire") -> "Vehicles"

            lower.contains("rent") || lower.contains("lease") || lower.contains("apartment") ||
            lower.contains("flat") || lower.contains("landlord") -> "Rental / Property"

            lower.contains("insurance") || lower.contains("policy") || lower.contains("claim") -> "Insurance"

            lower.contains("service") || lower.contains("repair") || lower.contains("maintenance") -> "Service & Maintenance"

            lower.contains("card") || lower.contains("id") || lower.contains("identity") ||
            lower.contains("license") || lower.contains("passport") || lower.contains("certificate") -> "Documents"

            lower.contains("bill") || lower.contains("invoice") || lower.contains("receipt") || lower.contains("tax") -> "Documents"

            else -> "Documents"
        }
    }

    // ---------------------------------------------------------
    // ML KIT OCR
    // ---------------------------------------------------------
    LaunchedEffect(uri) {
        try {
            ocrLoading = true
            ocrError = null

            val inputImage =
                com.google.mlkit.vision.common.InputImage.fromFilePath(
                    context,
                    uri
                )

            val recognizer =
                com.google.mlkit.vision.text.TextRecognition
                    .getClient(
                        com.google.mlkit.vision.text.latin.TextRecognizerOptions
                            .DEFAULT_OPTIONS
                    )

            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    ocrText = result.text
                    ocrLoading = false

                    // If form fields are empty, populate from raw OCR text
                    val ocrLines = result.text.lines().map { it.trim() }.filter { it.isNotBlank() }
                    if (productName.isBlank() && ocrLines.isNotEmpty()) {
                        productName = ocrLines.first()
                    }
                    if (notes.isBlank() && result.text.isNotBlank()) {
                        notes = result.text
                    }
                    if (category.isBlank() || category == "Documents" || category == "Electronics") {
                        category = inferCategoryFromText(result.text)
                    }
                }
                .addOnFailureListener { exception ->
                    ocrError = exception.message ?: "OCR failed."
                    ocrLoading = false
                }

        } catch (exception: Exception) {
            ocrError = exception.message ?: "Unable to process image."
            ocrLoading = false
        }
    }

    // ---------------------------------------------------------
    // GROQ AI ANALYSIS
    // ---------------------------------------------------------
    fun analyzeWithAI() {
        aiLoading = true
        aiError = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = com.example.keeptrack.BuildConfig.GROQ_API_KEY

                if (apiKey.isBlank()) {
                    throw Exception("GROQ_API_KEY is missing.")
                }

                val repository = GroqRepository(
                    context = context,
                    apiKey = apiKey
                )

                val result = repository.analyzeReceipt(uri, ocrText)

                withContext(Dispatchers.Main) {
                    result
                        .onSuccess { data ->
                            val extractedProduct = data.productName?.trim().orEmpty()
                            val extractedStore = data.storeName?.trim().orEmpty()
                            val extractedCategory = data.category?.trim().orEmpty()
                            val extractedNotes = data.notes?.trim().orEmpty()

                            val ocrLines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            val firstOcrLine = ocrLines.firstOrNull().orEmpty()

                            productName = when {
                                extractedProduct.isNotBlank() -> extractedProduct
                                extractedStore.isNotBlank() -> extractedStore
                                firstOcrLine.isNotBlank() -> firstOcrLine
                                else -> "Scanned Document"
                            }

                            storeName = when {
                                extractedStore.isNotBlank() -> extractedStore
                                ocrLines.size > 1 -> ocrLines[1]
                                else -> ""
                            }

                            category = when {
                                extractedCategory.isNotBlank() && extractedCategory != "Electronics" -> normalizeCategory(extractedCategory)
                                ocrText.isNotBlank() -> inferCategoryFromText(ocrText)
                                extractedCategory.isNotBlank() -> extractedCategory
                                else -> "Documents"
                            }

                            brand = data.brand?.trim().orEmpty()
                            model = data.model?.trim().orEmpty()
                            serialNumber = data.serialNumber?.trim().orEmpty()
                            purchaseDate = data.purchaseDate?.trim().orEmpty()
                            purchasePrice = data.purchasePrice?.toString().orEmpty()
                            warrantyPeriodMonths = data.warrantyPeriodMonths?.toString().orEmpty()

                            notes = when {
                                extractedNotes.isNotBlank() -> extractedNotes
                                ocrText.isNotBlank() -> ocrText
                                else -> ""
                            }

                            aiLoading = false
                            showForm = true
                        }
                        .onFailure { exception ->
                            val ocrLines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
                            if (productName.isBlank()) {
                                productName = ocrLines.firstOrNull() ?: "Scanned Document"
                            }
                            if (notes.isBlank() && ocrText.isNotBlank()) {
                                notes = ocrText
                            }
                            if (category.isBlank() || category == "Electronics") {
                                category = inferCategoryFromText(ocrText)
                            }
                            aiError = exception.message ?: "AI analysis failed."
                            aiLoading = false
                        }
                }

            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    val ocrLines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
                    if (productName.isBlank()) {
                        productName = ocrLines.firstOrNull() ?: "Scanned Document"
                    }
                    if (notes.isBlank() && ocrText.isNotBlank()) {
                        notes = ocrText
                    }
                    if (category.isBlank() || category == "Electronics") {
                        category = inferCategoryFromText(ocrText)
                    }
                    aiError = exception.message ?: "AI analysis failed."
                    aiLoading = false
                }
            }
        }
    }

    // Trigger AI extraction automatically on launch
    LaunchedEffect(uri) {
        analyzeWithAI()
    }

    fun confirmAndSave() {
        val titleVal = productName.ifBlank { storeName.ifBlank { ocrText.lines().firstOrNull { it.isNotBlank() } ?: "Scanned Document" } }
        val categoryVal = category.ifBlank { inferCategoryFromText(ocrText) }

        val parsedDate = runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(purchaseDate)?.time
        }.getOrNull() ?: runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(purchaseDate)?.time
        }.getOrNull()

        val parsedPrice = purchasePrice.replace("₹", "").replace("$", "").replace(",", "").trim().toDoubleOrNull()
        val parsedMonths = warrantyPeriodMonths.filter(Char::isDigit).toIntOrNull()

        val calculatedExpiry = if (parsedMonths != null && parsedDate != null) {
            java.util.Calendar.getInstance().apply {
                timeInMillis = parsedDate
                add(java.util.Calendar.MONTH, parsedMonths)
            }.timeInMillis
        } else null

        val newRecord = RecordEntity(
            title = titleVal.trim(),
            category = categoryVal.trim(),
            brand = brand.trim().ifBlank { null },
            model = model.trim().ifBlank { null },
            serialNumber = serialNumber.trim().ifBlank { null },
            purchaseDate = parsedDate,
            purchasePrice = parsedPrice,
            storeOrProvider = storeName.trim().ifBlank { null },
            warrantyPeriodMonths = parsedMonths,
            warrantyExpiryDate = calculatedExpiry,
            documentPath = uri.toString(),
            documentType = "Receipt",
            notes = notes.trim().ifBlank { null }
        )

        if (recordViewModel != null) {
            recordViewModel.addRecord(newRecord)
            Toast.makeText(context, "Record saved successfully", Toast.LENGTH_SHORT).show()
            onNavigateToRecords()
        } else {
            Toast.makeText(context, "Error: RecordViewModel not available", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = if (showForm) "Review Receipt Information" else "Processing Receipt",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // -------------------------------------------------
            // RECEIPT IMAGE PREVIEW
            // -------------------------------------------------
            item {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Receipt",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // -------------------------------------------------
            // PHASE 1: LOADING STATE (NO EDITABLE FIELDS)
            // -------------------------------------------------
            if (aiLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Analyzing receipt with AI...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Extracting store, items, purchase date, and prices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

            // -------------------------------------------------
            // PHASE 2: ERROR & RETRY STATE
            // -------------------------------------------------
            } else if (aiError != null && !showForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "AI Extraction Failure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = aiError ?: "Unable to analyze receipt.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { analyzeWithAI() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry"
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text("Retry AI")
                                }

                                OutlinedButton(
                                    onClick = { showForm = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Enter Manually")
                                }
                            }
                        }
                    }
                }

            // -------------------------------------------------
            // PHASE 3: EDITABLE REVIEW FORM
            // -------------------------------------------------
            } else if (showForm) {

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Verify and edit extracted details:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedButton(
                            onClick = { analyzeWithAI() },
                            enabled = !aiLoading
                        ) {
                            Text("Re-run AI")
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = { Text("Product Name / Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = serialNumber,
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = purchaseDate,
                        onValueChange = { purchaseDate = it },
                        label = { Text("Purchase Date (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Purchase Price") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = warrantyPeriodMonths,
                        onValueChange = { warrantyPeriodMonths = it },
                        label = { Text("Warranty Period (Months)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }

                // Confirm & Save
                item {
                    KeepTrackButton(
                        text = "Confirm & Save",
                        enabled = productName.isNotBlank() || storeName.isNotBlank(),
                        onClick = { confirmAndSave() }
                    )
                }
            }

            // -------------------------------------------------
            // DELETE DOCUMENT
            // -------------------------------------------------
            item {
                Button(
                    onClick = {
                        try {
                            val file = File(uri.path ?: "")
                            if (file.exists()) {
                                file.delete()
                            }
                        } catch (_: Exception) {
                        }
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Delete Document")
                }
            }
        }
    }
}
