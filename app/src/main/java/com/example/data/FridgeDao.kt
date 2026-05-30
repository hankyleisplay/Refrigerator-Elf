package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeDao {

    // --- Fridge Items Queries ---
    @Query("SELECT * FROM fridge_items ORDER BY expiryDate ASC")
    fun getAllFridgeItems(): Flow<List<FridgeItem>>

    @Query("SELECT * FROM fridge_items WHERE id = :id")
    suspend fun getFridgeItemById(id: Int): FridgeItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFridgeItem(item: FridgeItem)

    @Update
    suspend fun updateFridgeItem(item: FridgeItem)

    @Delete
    suspend fun deleteFridgeItem(item: FridgeItem)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteFridgeItemById(id: Int)

    // --- Shopping Items Queries ---
    @Query("SELECT * FROM shopping_items ORDER BY addedDate DESC")
    fun getAllShoppingItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItemById(id: Int)

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    suspend fun deleteCheckedShoppingItems()
}
