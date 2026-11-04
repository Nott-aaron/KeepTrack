package com.example.keeptrack.ui.screens.editrecord

import android.app.DatePickerDialog
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
import java.util.Date
import java.util.Locale

@Composable
fun EditRecordScreen(record: RecordEntity, onSave: (RecordEntity) -> Unit, onCancel: () -> Unit = {}) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(record.title) }
    var subCategory by remember { mutableStateOf(record.subCategory ?: "") }
    var brand by remember { mutableStateOf(record.brand ?: "") }
    var model by remember { mutableStateOf(record.model ?: "") }
    var serial by remember { mutableStateOf(record.serialNumber ?: "") }
    var purchaseDate by remember { mutableStateOf(formatDate(record.purchaseDate)) }
    var price by remember { mutableStateOf(record.purchasePrice?.toString() ?: "") }
    var provider by remember { mutableStateOf(record.storeOrProvider ?: "") }
    var warrantyMonths by remember { mutableStateOf(record.warrantyPeriodMonths?.toString() ?: "") }
    var warrantyExpiry by remember { mutableStateOf(formatDate(record.warrantyExpiryDate)) }
    var expiryDate by remember { mutableStateOf(formatDate(record.expiryDate)) }
    var agreementStart by remember { mutableStateOf(formatDate(record.agreementStartDate)) }
    var agreementEnd by remember { mutableStateOf(formatDate(record.agreementEndDate)) }
    var monthly by remember { mutableStateOf(record.monthlyAmount?.toString() ?: "") }
    var deposit by remember { mutableStateOf(record.securityDeposit?.toString() ?: "") }
    var policy by remember { mutableStateOf(record.policyNumber ?: "") }
    var contactName by remember { mutableStateOf(record.contactName ?: "") }
    var contactPhone by remember { mutableStateOf(record.contactPhone ?: "") }
    var notes by remember { mutableStateOf(record.notes ?: "") }

    fun calculateWarranty() {
        val months = warrantyMonths.toIntOrNull() ?: return
        val date = parseDate(purchaseDate) ?: return
        warrantyExpiry = formatDate(Calendar.getInstance().apply { timeInMillis = date; add(Calendar.MONTH, months) }.timeInMillis)
    }

    fun save() {
        val months = warrantyMonths.toIntOrNull()
        val purchase = parseDate(purchaseDate)
        val calculated = if (months != null && purchase != null) Calendar.getInstance().apply { timeInMillis = purchase; add(Calendar.MONTH, months) }.timeInMillis else parseDate(warrantyExpiry)
        onSave(record.copy(
            title = title.trim(), subCategory = subCategory.trim().ifBlank { null }, brand = brand.trim().ifBlank { null }, model = model.trim().ifBlank { null }, serialNumber = serial.trim().ifBlank { null },
            purchaseDate = purchase, purchasePrice = parseAmount(price), storeOrProvider = provider.trim().ifBlank { null }, warrantyPeriodMonths = months, warrantyExpiryDate = calculated,
            expiryDate = parseDate(expiryDate), agreementStartDate = parseDate(agreementStart), agreementEndDate = parseDate(agreementEnd), monthlyAmount = parseAmount(monthly), securityDeposit = parseAmount(deposit),
            policyNumber = policy.trim().ifBlank { null }, contactName = contactName.trim().ifBlank { null }, contactPhone = contactPhone.trim().ifBlank { null }, notes = notes.trim().ifBlank { null }, updatedAt = System.currentTimeMillis()
        ))
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp)) {
        Text("Edit Record", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(18.dp))
        Field("Title", title) { title = it }
        Field("Category", record.category, readOnly = true) {}
        Field("Sub-category", subCategory) { subCategory = it }
        Field("Brand", brand) { brand = it }
        Field("Model", model) { model = it }
        Field("Serial / Registration Number", serial) { serial = it }
        DateField("Purchase Date", purchaseDate) { purchaseDate = it; calculateWarranty() }
        Field("Purchase Price", price) { price = it }
        Field("Store / Provider", provider) { provider = it }
        Field("Warranty Period (months)", warrantyMonths) { warrantyMonths = it.filter(Char::isDigit); calculateWarranty() }
        DateField("Warranty Expiry (automatic)", warrantyExpiry, true) {}
        DateField("Expiry Date", expiryDate) { expiryDate = it }
        DateField("Agreement Start", agreementStart) { agreementStart = it }
        DateField("Agreement End", agreementEnd) { agreementEnd = it }
        Field("Monthly Amount", monthly) { monthly = it }
        Field("Security Deposit", deposit) { deposit = it }
        Field("Policy Number", policy) { policy = it }
        Field("Contact Name", contactName) { contactName = it }
        Field("Contact Phone", contactPhone) { contactPhone = it }
        Field("Notes", notes, false) { notes = it }
        Spacer(Modifier.height(18.dp))
        KeepTrackButton("Save Changes", enabled = title.isNotBlank(), onClick = ::save)
        Spacer(Modifier.height(8.dp))
        KeepTrackButton("Cancel", onClick = onCancel)
        Spacer(Modifier.height(30.dp))
    }
}

@Composable private fun Field(label: String, value: String, singleLine: Boolean = true, readOnly: Boolean = false, onChange: (String) -> Unit) {
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().then(if (singleLine) Modifier else Modifier.height(110.dp)), singleLine = singleLine, readOnly = readOnly, label = { Text(label) })
}

@Composable private fun DateField(label: String, value: String, readOnly: Boolean = false, onChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedTextField(value = value, onValueChange = {}, modifier = Modifier.fillMaxWidth(), readOnly = true, singleLine = true, label = { Text(label) }, trailingIcon = {
        IconButton(enabled = !readOnly, onClick = {
            val c = Calendar.getInstance()
            DatePickerDialog(context, { _, y, m, d -> onChange("%02d/%02d/%04d".format(Locale.US, d, m + 1, y)) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }) { Icon(Icons.Default.CalendarMonth, contentDescription = "Select date") }
    })
}

private fun parseDate(value: String): Long? = runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(value)?.time }.getOrNull()
private fun formatDate(value: Long?): String = value?.let { SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(it)) } ?: ""
private fun parseAmount(value: String): Double? = value.replace("₹", "").replace(",", "").trim().toDoubleOrNull()
