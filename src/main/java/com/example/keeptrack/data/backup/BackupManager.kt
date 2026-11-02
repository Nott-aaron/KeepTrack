package com.example.keeptrack.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.keeptrack.data.local.UserPreferences
import com.example.keeptrack.data.model.RecordEntity
import com.example.keeptrack.data.storage.FileStorage
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object BackupManager {
    private const val RECORDS = "records.json"
    private const val PROFILE = "profile.json"
    private const val IMAGES_DIR = "images/"

    fun createBackup(context: Context, records: List<RecordEntity>): File {
        val out = File(context.cacheDir, "KeepTrack_Backup_${System.currentTimeMillis()}.zip")
        ZipOutputStream(BufferedOutputStream(FileOutputStream(out))).use { zip ->
            zip.putNextEntry(ZipEntry(RECORDS))
            zip.write(recordsJson(context, records).toString(2).toByteArray())
            zip.closeEntry()

            val prefs = UserPreferences(context)
            val profile = JSONObject().apply {
                put("name", prefs.getName())
                put("email", prefs.getEmail())
                put("phone", prefs.getPhone())
                put("gender", prefs.getGender())
            }
            zip.putNextEntry(ZipEntry(PROFILE))
            zip.write(profile.toString(2).toByteArray())
            zip.closeEntry()

            FileStorage.listSavedImages(context).forEachIndexed { index, uri ->
                val name = "${IMAGES_DIR}${index}_${FileStorage.fileName(context, uri)}"
                context.contentResolver.openInputStream(uri)?.use { input ->
                    zip.putNextEntry(ZipEntry(name))
                    input.copyTo(zip)
                    zip.closeEntry()
                }
            }
        }
        return out
    }

    private fun recordsJson(context: Context, records: List<RecordEntity>): JSONArray {
        val array = JSONArray()
        records.forEach { r ->
            val imageName = r.documentPath?.let { path ->
                val uri = Uri.parse(path)
                "${FileStorage.fileName(context, uri)}"
            }
            array.put(JSONObject().apply {
                put("id", r.id); put("title", r.title); put("category", r.category); putNullable("subCategory", r.subCategory)
                putNullable("brand", r.brand); putNullable("model", r.model); putNullable("serialNumber", r.serialNumber)
                putNullable("purchaseDate", r.purchaseDate); putNullable("purchasePrice", r.purchasePrice); putNullable("storeOrProvider", r.storeOrProvider)
                putNullable("warrantyPeriodMonths", r.warrantyPeriodMonths); putNullable("warrantyExpiryDate", r.warrantyExpiryDate); putNullable("expiryDate", r.expiryDate)
                putNullable("agreementStartDate", r.agreementStartDate); putNullable("agreementEndDate", r.agreementEndDate); putNullable("monthlyAmount", r.monthlyAmount); putNullable("securityDeposit", r.securityDeposit)
                putNullable("policyNumber", r.policyNumber); putNullable("contactName", r.contactName); putNullable("contactPhone", r.contactPhone)
                putNullable("documentType", r.documentType); putNullable("notes", r.notes); put("isFavorite", r.isFavorite); put("createdAt", r.createdAt); put("updatedAt", r.updatedAt)
                putNullable("documentFileName", imageName)
            })
        }
        return array
    }

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    suspend fun restore(context: Context, backupUri: Uri, replace: suspend (List<RecordEntity>) -> Unit) {
        val bytes = context.contentResolver.openInputStream(backupUri)?.use { it.readBytes() }
            ?: error("Could not open backup file")
        val text = bytes.toString(Charsets.UTF_8).trimStart()
        if (text.startsWith("{")) {
            val root = JSONObject(text)
            val array = root.optJSONArray("records") ?: error("JSON backup does not contain records")
            replace(parseRecords(array, emptyMap()))
            return
        }
        val temp = File(context.cacheDir, "restore_${System.currentTimeMillis()}.zip")
        FileOutputStream(temp).use { it.write(bytes) }
        ZipFile(temp).use { zip ->
            val recordsEntry = zip.getEntry(RECORDS) ?: error("Invalid KeepTrack backup: records.json missing")
            val recordsArray = JSONArray(zip.getInputStream(recordsEntry).bufferedReader().use { it.readText() })
            val imageMap = mutableMapOf<String, String>()
            zip.entries().asSequence().filter { !it.isDirectory && it.name.startsWith(IMAGES_DIR) }.forEach { entry ->
                val originalName = entry.name.removePrefix(IMAGES_DIR).substringAfter('_')
                val saved = saveBackupImage(context, zip.getInputStream(entry), originalName)
                if (saved != null) imageMap[originalName] = saved
            }
            replace(parseRecords(recordsArray, imageMap))
            zip.getEntry(PROFILE)?.let { entry ->
                val p = JSONObject(zip.getInputStream(entry).bufferedReader().use { it.readText() })
                UserPreferences(context).saveProfile(p.optString("name"), p.optString("email"), p.optString("phone"), p.optString("gender"))
            }
        }
        temp.delete()
    }

    private fun parseRecords(array: JSONArray, imageMap: Map<String, String>): List<RecordEntity> = buildList {
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            val oldName = o.optNullableString("documentFileName")
            add(RecordEntity(
                id = o.optLong("id"), title = o.optString("title"), category = o.optString("category"),
                subCategory = o.optNullableString("subCategory"), brand = o.optNullableString("brand"), model = o.optNullableString("model"), serialNumber = o.optNullableString("serialNumber"),
                purchaseDate = o.optNullableLong("purchaseDate"), purchasePrice = o.optNullableDouble("purchasePrice"), storeOrProvider = o.optNullableString("storeOrProvider"),
                warrantyPeriodMonths = o.optNullableInt("warrantyPeriodMonths"), warrantyExpiryDate = o.optNullableLong("warrantyExpiryDate"), expiryDate = o.optNullableLong("expiryDate"),
                agreementStartDate = o.optNullableLong("agreementStartDate"), agreementEndDate = o.optNullableLong("agreementEndDate"), monthlyAmount = o.optNullableDouble("monthlyAmount"), securityDeposit = o.optNullableDouble("securityDeposit"),
                policyNumber = o.optNullableString("policyNumber"), contactName = o.optNullableString("contactName"), contactPhone = o.optNullableString("contactPhone"),
                documentPath = oldName?.let { imageMap[it] }, documentType = o.optNullableString("documentType"), notes = o.optNullableString("notes"),
                isFavorite = o.optBoolean("isFavorite"), createdAt = o.optLong("createdAt", System.currentTimeMillis()), updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            ))
        }
    }

    private fun saveBackupImage(context: Context, input: java.io.InputStream, fileName: String): String? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "KeepTrack_Restored_${System.currentTimeMillis()}_$fileName")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KeepTrack")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        return try {
            context.contentResolver.openOutputStream(uri)?.use { input.copyTo(it) } ?: error("Cannot write image")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) context.contentResolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri.toString()
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null); null
        }
    }

    private fun JSONObject.optNullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    private fun JSONObject.optNullableLong(key: String): Long? = if (isNull(key)) null else optLong(key)
    private fun JSONObject.optNullableDouble(key: String): Double? = if (isNull(key)) null else optDouble(key)
    private fun JSONObject.optNullableInt(key: String): Int? = if (isNull(key)) null else optInt(key)
}
