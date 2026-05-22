package com.example.data.local

import androidx.room.*
import com.example.data.model.ServiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_records ORDER BY timestamp DESC")
    fun getAllServices(): Flow<List<ServiceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceRecord): Long

    @Delete
    suspend fun deleteService(service: ServiceRecord)

    @Query("DELETE FROM service_records WHERE id = :id")
    suspend fun deleteServiceById(id: Int)
}
