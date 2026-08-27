package ir.ghiyas.alimaa.data

import ir.ghiyas.alimaa.domain.models.CalculationHistoryRecord
import ir.ghiyas.alimaa.domain.models.ResultItem
import ir.ghiyas.alimaa.domain.models.WalnutUnit
import kotlinx.browser.window
import kotlin.js.json

object LocalStorageRepository {
    private const val STORAGE_KEY = "ghiyas_history_records"

    fun saveRecord(record: CalculationHistoryRecord) {
        val records = getAllRecords().toMutableList()
        val existingIndex = records.indexOfFirst { it.id == record.id }
        
        if (existingIndex >= 0) {
            // آپدیت رکورد موجود
            records[existingIndex] = record
        } else {
            // رکورد جدید است، پس منطق نامگذاری خودکار (Auto-increment) را اعمال می‌کنیم
            val baseName = record.calculationName
            var finalName = baseName
            var counter = 1
            
            // تا زمانی که در همان سال رکوردی با این نام دقیق وجود دارد، عدد را بالا ببر
            while (records.any { it.calculationName == finalName && it.persianYear == record.persianYear }) {
                finalName = "$baseName ($counter)"
                counter++
            }
            
            val recordToSave = record.copy(calculationName = finalName)
            records.add(0, recordToSave)
        }
        
        saveAll(records)
    }

    fun deleteRecord(id: String) {
        val records = getAllRecords().filter { it.id != id }
        saveAll(records)
    }

    // متد اختصاصی برای ویرایش نام رکورد به طور مستقیم
    fun updateRecordName(id: String, newName: String) {
        val records = getAllRecords().toMutableList()
        val existingIndex = records.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            val record = records[existingIndex]
            records[existingIndex] = record.copy(calculationName = newName)
            saveAll(records)
        }
    }

    fun getAllRecords(): List<CalculationHistoryRecord> {
        val jsonString = window.localStorage.getItem(STORAGE_KEY)
        if (jsonString.isNullOrEmpty()) return emptyList()
        return try {
            val jsArray = js("JSON.parse(jsonString)") as Array<dynamic>
            jsArray.map { jsToRecord(it) }.sortedByDescending { it.timestamp }
        } catch (e: Exception) {
            console.error("Error parsing history records", e)
            emptyList()
        }
    }

    private fun saveAll(records: List<CalculationHistoryRecord>) {
        val jsArray = records.map { recordToJs(it) }.toTypedArray()
        window.localStorage.setItem(STORAGE_KEY, js("JSON.stringify(jsArray)") as String)
    }

    private fun recordToJs(record: CalculationHistoryRecord): dynamic {
        return json(
            "id" to record.id,
            "timestamp" to record.timestamp.toDouble(),
            "calculationName" to record.calculationName,
            "persianYear" to record.persianYear,
            "baseUnit" to record.baseUnit,
            "inputAmount" to record.inputAmount.value,
            "expensesResults" to record.expensesResults.map { json("label" to it.label, "value" to it.value.value) }.toTypedArray(),
            "agricultureResults" to record.agricultureResults.map { json("label" to it.label, "value" to it.value.value) }.toTypedArray(),
            "nimehkariResults" to record.nimehkariResults.map { json("label" to it.label, "value" to it.value.value) }.toTypedArray(),
            "finalSharesResults" to record.finalSharesResults.map { json("label" to it.label, "value" to it.value.value) }.toTypedArray()
        )
    }

    private fun jsToRecord(jsObj: dynamic): CalculationHistoryRecord {
        val expensesJs: Array<dynamic> = if (jsObj.expensesResults != undefined) jsObj.expensesResults else emptyArray()
        val agricultureJs: Array<dynamic> = if (jsObj.agricultureResults != undefined) jsObj.agricultureResults else emptyArray()
        val nimehkariJs: Array<dynamic> = if (jsObj.nimehkariResults != undefined) jsObj.nimehkariResults else emptyArray()
        val finalSharesJs: Array<dynamic> = if (jsObj.finalSharesResults != undefined) jsObj.finalSharesResults else emptyArray()

        return CalculationHistoryRecord(
            id = jsObj.id as String,
            timestamp = (jsObj.timestamp as Number).toLong(),
            calculationName = jsObj.calculationName as String,
            persianYear = jsObj.persianYear as String,
            baseUnit = jsObj.baseUnit as String,
            inputAmount = WalnutUnit((jsObj.inputAmount as Number).toDouble()),
            expensesResults = expensesJs.map { ResultItem(it.label as String, WalnutUnit((it.value as Number).toDouble())) },
            agricultureResults = agricultureJs.map { ResultItem(it.label as String, WalnutUnit((it.value as Number).toDouble())) },
            nimehkariResults = nimehkariJs.map { ResultItem(it.label as String, WalnutUnit((it.value as Number).toDouble())) },
            finalSharesResults = finalSharesJs.map { ResultItem(it.label as String, WalnutUnit((it.value as Number).toDouble())) }
        )
    }
}
