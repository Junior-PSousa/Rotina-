package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val serviceExecuted: String,
    val amountCharged: Double,
    val timestamp: Long,
    val formattedDateTime: String, // format: dd/MM/yyyy HH:mm
    val phone: String = "",
    val address: String = "",
    val saveAsContact: Boolean = false
)
