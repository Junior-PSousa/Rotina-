package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ServiceRecord
import com.example.data.network.GeminiClient
import com.example.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ActiveOutputState(
    val serviceRecord: ServiceRecord,
    val title: String, // e.g., "Serviço Cadastrado com Sucesso!" or "Detalhes do Serviço"
    val agendaJson: String,
    val sheetsJson: String,
    val contactsJson: String
)

class ServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ServiceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ServiceRepository(database.serviceDao())
    }

    // Raw unfiltered services
    val allServices: StateFlow<List<ServiceRecord>> = repository.allServices
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search and filter inputs
    var searchClientQuery by mutableStateOf("")
    var searchDateQuery by mutableStateOf("")

    private val filterTrigger = MutableStateFlow(0)

    // Filtered services reactive stream
    val filteredServices = combine(
        allServices,
        filterTrigger
    ) { services, _ ->
        services.filter { item ->
            val matchesClient = searchClientQuery.isBlank() || 
                    item.clientName.contains(searchClientQuery, ignoreCase = true)
            
            val matchesDate = searchDateQuery.isBlank() || 
                    item.formattedDateTime.contains(searchDateQuery, ignoreCase = true) ||
                    // Allow simple translation of months (e.g. "Maio", "05")
                    monthsContains(item.formattedDateTime, searchDateQuery)

            matchesClient && matchesDate
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun triggerFilterUpdate() {
        filterTrigger.value++
    }

    // Input form states
    var clientNameInput by mutableStateOf("")
    var serviceInput by mutableStateOf("")
    var amountInput by mutableStateOf("")
    var phoneInput by mutableStateOf("")
    var addressInput by mutableStateOf("")
    var dateTimeInput by mutableStateOf("")
    var saveAsContactInput by mutableStateOf(false)

    // AI Natural Language input section
    var naturalLanguageInput by mutableStateOf("")
    var isAiLoading by mutableStateOf(false)
    var aiError by mutableStateOf<String?>(null)
    var aiSuccessState by mutableStateOf<String?>(null)

    // Selected payload / output container for displaying visual summary & copyable JSONs
    var activeOutput by mutableStateOf<ActiveOutputState?>(null)

    init {
        // Preset form date to today
        resetForm()
    }

    fun resetForm() {
        clientNameInput = ""
        serviceInput = ""
        amountInput = ""
        phoneInput = ""
        addressInput = ""
        saveAsContactInput = false
        val today = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        dateTimeInput = today
        aiSuccessState = null
        aiError = null
    }

    // Call Gemini to parse raw Portuguese voice or typing instruction
    fun processTextWithAI() {
        if (naturalLanguageInput.isBlank()) {
            aiError = "Por favor, digite um relato de serviço!"
            return
        }

        viewModelScope.launch {
            isAiLoading = true
            aiError = null
            aiSuccessState = null
            
            val todayDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val result = GeminiClient.parseServiceDescription(naturalLanguageInput, todayDateStr)

            if (result != null) {
                // Populate form fields with extracted data
                if (result.clientName.isNotBlank()) clientNameInput = result.clientName
                if (result.serviceExecuted.isNotBlank()) serviceInput = result.serviceExecuted
                if (result.amountCharged > 0.0) amountInput = "%.2f".format(Locale.US, result.amountCharged)
                if (result.phone.isNotBlank()) phoneInput = result.phone
                if (result.address.isNotBlank()) addressInput = result.address
                if (result.date.isNotBlank()) dateTimeInput = result.date
                saveAsContactInput = result.saveAsContact

                aiSuccessState = "Dados identificados e preenchidos no formulário abaixo!"
            } else {
                aiError = "Não foi possível extrair os dados. Talvez a chave da API do Gemini esteja ausente. Preencha o formulário manualmente!"
            }
            isAiLoading = false
        }
    }

    // Add service from the state form
    fun submitService() {
        if (clientNameInput.isBlank()) {
            aiError = "O nome do cliente é obrigatório!"
            return
        }
        if (serviceInput.isBlank()) {
            aiError = "O serviço executado é obrigatório!"
            return
        }
        val parsedAmount = amountInput.replace(",", ".").toDoubleOrNull() ?: 0.0

        val parsedTimestamp = try {
            val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            parser.parse(dateTimeInput)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        val record = ServiceRecord(
            clientName = clientNameInput,
            serviceExecuted = serviceInput,
            amountCharged = parsedAmount,
            timestamp = parsedTimestamp,
            formattedDateTime = dateTimeInput,
            phone = phoneInput,
            address = addressInput,
            saveAsContact = saveAsContactInput
        )

        viewModelScope.launch {
            val id = repository.insert(record)
            val savedRecord = record.copy(id = id.toInt())

            // Select it immediately to show output (Confirmação Visual / Payloads)
            selectRecordForOutput(savedRecord, isNew = true)
            
            // Clear inputs for next entry
            resetForm()
            naturalLanguageInput = ""
        }
    }

    // Select standard service record and generate custom payloads for it
    fun selectRecordForOutput(record: ServiceRecord, isNew: Boolean) {
        val title = if (isNew) "Serviço Cadastrado com Sucesso! 🎉" else "Detalhes do Serviço Cadastrado"
        
        activeOutput = ActiveOutputState(
            serviceRecord = record,
            title = title,
            agendaJson = generateAgendaJson(record),
            sheetsJson = generateSheetsJson(record),
            contactsJson = generateContactsJson(record)
        )
    }

    fun deleteRecord(record: ServiceRecord) {
        viewModelScope.launch {
            repository.delete(record)
            if (activeOutput?.serviceRecord?.id == record.id) {
                activeOutput = null
            }
        }
    }

    // Simple month checking utility for PT-BR
    private fun monthsContains(formattedDate: String, search: String): Boolean {
        // formattedDate: dd/MM/yyyy HH:mm
        val cleanSearch = search.lowercase(Locale.getDefault())
        val parts = formattedDate.split("/")
        if (parts.size < 2) return false
        val monthCode = parts[1] // e.g. "05" or "12"
        
        return when (monthCode) {
            "01" -> "janeiro"
            "02" -> "fevereiro"
            "03" -> "março"
            "04" -> "abril"
            "05" -> "maio"
            "06" -> "junho"
            "07" -> "julho"
            "08" -> "agosto"
            "09" -> "setembro"
            "10" -> "outubro"
            "11" -> "novembro"
            "12" -> "dezembro"
            else -> ""
        }.contains(cleanSearch) || monthCode == cleanSearch
    }

    private fun parseDateTimeToIso8601(dateTimeStr: String): Pair<String, String>? {
        return try {
            val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = parser.parse(dateTimeStr) ?: return null
            
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val formattedStart = isoFormatter.format(date) + "-03:00"
            
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val formattedEnd = isoFormatter.format(calendar.time) + "-03:00"
            
            Pair(formattedStart, formattedEnd)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateAgendaJson(record: ServiceRecord): String {
        val times = parseDateTimeToIso8601(record.formattedDateTime)
        val startIso = times?.first ?: "${record.formattedDateTime}:00-03:00"
        val endIso = times?.second ?: "${record.formattedDateTime}:00-03:00"
        
        val recordAmount = "%.2f".format(Locale.US, record.amountCharged)
        return """{
  "summary": "[Rotina+] Serviço: ${record.serviceExecuted} - ${record.clientName}",
  "description": "Valor Cobrado: R$ ${recordAmount}\nTelefone: ${record.phone}\nEndereço: ${record.address}",
  "start": {
    "dateTime": "$startIso",
    "timeZone": "America/Sao_Paulo"
  },
  "end": {
    "dateTime": "$endIso",
    "timeZone": "America/Sao_Paulo"
  }
}"""
    }

    private fun generateSheetsJson(record: ServiceRecord): String {
        val recordAmount = "%.2f".format(Locale.US, record.amountCharged)
        return """{
  "values": [
    [
      "${record.formattedDateTime}",
      "${record.clientName}",
      "${record.serviceExecuted}",
      "R$ ${recordAmount}",
      "${record.phone}",
      "${record.address}"
    ]
  ]
}"""
    }

    private fun generateContactsJson(record: ServiceRecord): String {
        return """{
  "names": [
    {
      "givenName": "${record.clientName}"
    }
  ],
  "phoneNumbers": [
    {
      "value": "${record.phone}",
      "type": "mobile"
    }
  ],
  "addresses": [
    {
      "streetAddress": "${record.address}",
      "type": "home"
    }
  ],
  "biographies": [
    {
      "value": "Cliente cadastrado via Rotina+. Serviço: ${record.serviceExecuted}"
    }
  ]
}"""
    }
}
