package com.example.keeptrack.ui.screens.addrecord

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.ui.components.KeepTrackButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val categories = listOf("Electronics", "Home Appliance", "Machines & Equipment", "Vehicles", "Rental / Property", "Insurance", "Documents", "Service & Maintenance", "Other")

@Composable
fun AddRecordScreen(onSave: (RecordEntity) -> Unit, onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var subCategory by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serial by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var warrantyMonths by remember { mutableStateOf("") }
    var warrantyExpiry by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var agreementStart by remember { mutableStateOf("") }
    var agreementEnd by remember { mutableStateOf("") }
    var monthly by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var policy by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(false) }

    fun calculateWarranty() {
        val months = warrantyMonths.toIntOrNull() ?: return
        val purchase = parseDate(purchaseDate) ?: return
        val cal = Calendar.getInstance().apply { timeInMillis = purchase; add(Calendar.MONTH, months) }
        warrantyExpiry = formatDate(cal.timeInMillis)
    }

    fun save() {
        val months = warrantyMonths.toIntOrNull()
        val purchase = parseDate(purchaseDate)
        val calculatedExpiry = if (months != null && purchase != null) {
            Calendar.getInstance().apply { timeInMillis = purchase; add(Calendar.MONTH, months) }.timeInMillis
        } else parseDate(warrantyExpiry)
        onSave(RecordEntity(
            title = title.trim(), category = category.trim(), subCategory = subCategory.trim().ifBlank { null },
            brand = brand.trim().ifBlank { null }, model = model.trim().ifBlank { null }, serialNumber = serial.trim().ifBlank { null },
            purchaseDate = purchase, purchasePrice = parseAmount(purchasePrice), storeOrProvider = provider.trim().ifBlank { null },
            warrantyPeriodMonths = months, warrantyExpiryDate = calculatedExpiry, expiryDate = parseDate(expiryDate),
            agreementStartDate = parseDate(agreementStart), agreementEndDate = parseDate(agreementEnd),
            monthlyAmount = parseAmount(monthly), securityDeposit = parseAmount(deposit), policyNumber = policy.trim().ifBlank { null },
            contactName = contactName.trim().ifBlank { null }, contactPhone = contactPhone.trim().ifBlank { null }, notes = notes.trim().ifBlank { null }
        ))
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.Top) {
        Text("Add Record", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text("Choose a category first. Only the information relevant to that category is requested.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Title *") })
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = if (category.isBlank()) "" else category, onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true, label = { Text("Category *") }, trailingIcon = { IconButton({ menu = true }) { Text("▼") } })
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) { categories.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { category = item; menu = false }) } }

        if (category.isNotBlank()) {
            Spacer(Modifier.height(18.dp))
            Text(category, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Field("Sub-category", subCategory) { subCategory = it }

            when (category) {
                "Electronics", "Home Appliance", "Machines & Equipment", "Vehicles" -> {
                    Field("Brand", brand) { brand = it }
                    Field("Model", model) { model = it }
                    Field(if (category == "Vehicles") "Registration / Serial Number" else "Serial Number", serial) { serial = it }
                    DateField("Purchase Date", purchaseDate) { purchaseDate = it; calculateWarranty() }
                    Field("Purchase Price", purchasePrice) { purchasePrice = it }
                    Field("Store / Provider", provider) { provider = it }
                    Field("Warranty Period (months)", warrantyMonths) { warrantyMonths = it.filter(Char::isDigit); calculateWarranty() }
                    DateField("Warranty Expiry (automatic)", warrantyExpiry, readOnly = true) { }
                }
                "Rental / Property" -> {
                    DateField("Agreement Start Date", agreementStart) { agreementStart = it }
                    DateField("Agreement End Date", agreementEnd) { agreementEnd = it }
                    Field("Monthly Amount", monthly) { monthly = it }
                    Field("Security Deposit", deposit) { deposit = it }
                    Field("Provider / Owner", provider) { provider = it }
                    Field("Contact Name", contactName) { contactName = it }
                    Field("Contact Phone", contactPhone) { contactPhone = it }
                }
                "Insurance" -> {
                    Field("Policy Number", policy) { policy = it }
                    Field("Provider", provider) { provider = it }
                    DateField("Policy / Expiry Date", expiryDate) { expiryDate = it }
                    Field("Contact Name", contactName) { contactName = it }
                    Field("Contact Phone", contactPhone) { contactPhone = it }
                }
                "Service & Maintenance" -> {
                    DateField("Service Date", purchaseDate) { purchaseDate = it }
                    Field("Service Provider", provider) { provider = it }
                    Field("Amount", purchasePrice) { purchasePrice = it }
                    DateField("Next Service / Expiry Date", expiryDate) { expiryDate = it }
                    Field("Contact Phone", contactPhone) { contactPhone = it }
                }
                "Documents" -> {
                    DateField("Document Date", purchaseDate) { purchaseDate = it }
                    DateField("Expiry Date", expiryDate) { expiryDate = it }
                    Field("Document Provider / Issuer", provider) { provider = it }
                }
                else -> {
                    Field("Brand / Provider", brand) { brand = it }
                    Field("Reference Number", serial) { serial = it }
                    DateField("Date", purchaseDate) { purchaseDate = it }
                    DateField("Expiry Date", expiryDate) { expiryDate = it }
                }
            }
            Spacer(Modifier.height(8.dp))
            Field("Notes", notes, singleLine = false) { notes = it }
            Spacer(Modifier.height(18.dp))
            KeepTrackButton("Save Record", enabled = title.isNotBlank() && category.isNotBlank(), onClick = ::save)
            Spacer(Modifier.height(8.dp))
            KeepTrackButton("Cancel", onClick = onBackClick)
        } else {
            Spacer(Modifier.height(24.dp))
            Text("Select a category to continue.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            KeepTrackButton("Cancel", onClick = onBackClick)
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable private fun Field(label: String, value: String, singleLine: Boolean = true, onChange: (String) -> Unit) {
    Spacer(Modifier.height(10.dp)); OutlinedTextField(value, onChange, Modifier.fillMaxWidth().then(if (singleLine) Modifier else Modifier.height(110.dp)), singleLine = singleLine, label = { Text(label) })
}

@Composable private fun DateField(label: String, value: String, readOnly: Boolean = false, onChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(value = value, onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true, singleLine = true, label = { Text(label) }, trailingIcon = {
        IconButton(enabled = !readOnly, onClick = {
            val cal = Calendar.getInstance()
            DatePickerDialog(context, { _, y, m, d -> onChange("%02d/%02d/%04d".format(Locale.US, d, m + 1, y)) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }) { Icon(Icons.Default.CalendarMonth, contentDescription = "Select date") }
    })
}

private fun parseDate(value: String): Long? = runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(value)?.time }.getOrNull()
private fun formatDate(value: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(java.util.Date(value))
private fun parseAmount(value: String): Double? = value.replace("₹", "").replace(",", "").trim().toDoubleOrNull()
