package com.example.data

import kotlinx.coroutines.flow.Flow

class FridgeRepository(private val fridgeDao: FridgeDao) {

    // --- Fridge Items ---
    val allFridgeItems: Flow<List<FridgeItem>> = fridgeDao.getAllFridgeItems()

    suspend fun getFridgeItemById(id: Int): FridgeItem? {
        return fridgeDao.getFridgeItemById(id)
    }

    suspend fun insertFridgeItem(item: FridgeItem) {
        fridgeDao.insertFridgeItem(item)
    }

    suspend fun updateFridgeItem(item: FridgeItem) {
        fridgeDao.updateFridgeItem(item)
    }

    suspend fun deleteFridgeItem(item: FridgeItem) {
        fridgeDao.deleteFridgeItem(item)
    }

    suspend fun deleteFridgeItemById(id: Int) {
        fridgeDao.deleteFridgeItemById(id)
    }

    // --- Shopping Items ---
    val allShoppingItems: Flow<List<ShoppingItem>> = fridgeDao.getAllShoppingItems()

    suspend fun insertShoppingItem(item: ShoppingItem) {
        fridgeDao.insertShoppingItem(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        fridgeDao.updateShoppingItem(item)
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        fridgeDao.deleteShoppingItem(item)
    }

    suspend fun deleteShoppingItemById(id: Int) {
        fridgeDao.deleteShoppingItemById(id)
    }

    suspend fun deleteCheckedShoppingItems() {
        fridgeDao.deleteCheckedShoppingItems()
    }
}
