package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "fridge_items")
data class FridgeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // e.g. "蔬果", "肉類與生鮮", "蛋奶與乳製品", "飲料與醬料", "熟食剩菜", "其他"
    val quantity: Double,
    val unit: String,      // e.g. "個", "包", "瓶", "克", "台斤"
    val addedDate: Long = System.currentTimeMillis(),
    val expiryDate: Long, // timestamp
    val location: String,  // e.g. "冷藏", "冷凍", "常溫"
    val notes: String = ""
) : Serializable {
    
    fun getDaysRemaining(currentTimeMillis: Long = System.currentTimeMillis()): Int {
        val diff = expiryDate - currentTimeMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getFreshnessStatus(currentTimeMillis: Long = System.currentTimeMillis()): FreshnessStatus {
        val days = getDaysRemaining(currentTimeMillis)
        return when {
            days < 0 -> FreshnessStatus.EXPIRED
            days <= 3 -> FreshnessStatus.EXPIRING_SOON
            else -> FreshnessStatus.FRESH
        }
    }
}

enum class FreshnessStatus {
    FRESH,          // 安全 (剩餘3天以上)
    EXPIRING_SOON,  // 即期 (剩餘 0-3 天)
    EXPIRED         // 過期 (< 0天)
}
