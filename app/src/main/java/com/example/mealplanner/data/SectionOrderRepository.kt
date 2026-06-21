package com.example.mealplanner.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SectionOrderRepository(private val sectionOrderDao: SectionOrderDao) {
    private val _sectionOrders = MutableStateFlow<List<SectionOrder>>(emptyList())
    val sectionOrders: StateFlow<List<SectionOrder>> = _sectionOrders.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            sectionOrderDao.getAllSectionOrders().collect {
                _sectionOrders.value = it
            }
        }
    }

    fun updateSectionOrders(orders: List<SectionOrder>) {
        CoroutineScope(Dispatchers.IO).launch {
            sectionOrderDao.updateAll(orders)
        }
    }
}
