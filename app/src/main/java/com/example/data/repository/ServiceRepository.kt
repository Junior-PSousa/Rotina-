package com.example.data.repository

import com.example.data.local.ServiceDao
import com.example.data.model.ServiceRecord
import kotlinx.coroutines.flow.Flow

class ServiceRepository(private val serviceDao: ServiceDao) {
    val allServices: Flow<List<ServiceRecord>> = serviceDao.getAllServices()

    suspend fun insert(service: ServiceRecord): Long {
        return serviceDao.insertService(service)
    }

    suspend fun delete(service: ServiceRecord) {
        serviceDao.deleteService(service)
    }

    suspend fun deleteById(id: Int) {
        serviceDao.deleteServiceById(id)
    }
}
