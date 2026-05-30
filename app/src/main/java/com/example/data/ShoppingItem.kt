package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isChecked: Boolean = false,
    val category: String = "其他",
    val addedDate: Long = System.currentTimeMillis()
)
