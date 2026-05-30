package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FridgeRepository(db.fridgeDao())

    // --- Filter & Sorting state ---
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("全部")
    val selectedLocationFilter = MutableStateFlow("全部") // 全部, 冷藏, 冷凍, 常溫
    val selectedSortOrder = MutableStateFlow(SortOrder.EXPIRY_ASC)

    // --- Main Lists from Room ---
    val rawFridgeItems: StateFlow<List<FridgeItem>> = repository.allFridgeItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.allShoppingItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Filtered Fridge Items Flow ---
    val fridgeItems: StateFlow<List<FridgeItem>> = combine(
        rawFridgeItems,
        searchQuery,
        selectedCategoryFilter,
        selectedLocationFilter,
        selectedSortOrder
    ) { items, query, catFilter, locFilter, sortOrder ->
        var filteredList = items

        // 1. Filter by Search Query
        if (query.isNotBlank()) {
            filteredList = filteredList.filter { it.name.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true) }
        }

        // 2. Filter by Category
        if (catFilter != "全部") {
            filteredList = filteredList.filter { it.category == catFilter }
        }

        // 3. Filter by Storage Location
        if (locFilter != "全部") {
            filteredList = filteredList.filter { it.location == locFilter }
        }

        // 4. Sort
        when (sortOrder) {
            SortOrder.NAME_ASC -> filteredList.sortedBy { it.name }
            SortOrder.EXPIRY_ASC -> filteredList.sortedBy { it.expiryDate }
            SortOrder.EXPIRY_DESC -> filteredList.sortedByDescending { it.expiryDate }
            SortOrder.ADDED_DESC -> filteredList.sortedByDescending { it.addedDate }
            SortOrder.QUANTITY_DESC -> filteredList.sortedByDescending { it.quantity }
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Statistics Flow ---
    val statistics = rawFridgeItems.map { items ->
        val currentTime = System.currentTimeMillis()
        var totalCount = 0
        var freshCount = 0
        var expiringSoonCount = 0
        var expiredCount = 0

        items.forEach { item ->
            totalCount++
            when (item.getFreshnessStatus(currentTime)) {
                FreshnessStatus.FRESH -> freshCount++
                FreshnessStatus.EXPIRING_SOON -> expiringSoonCount++
                FreshnessStatus.EXPIRED -> expiredCount++
            }
        }

        FridgeStats(
            total = totalCount,
            fresh = freshCount,
            expiringSoon = expiringSoonCount,
            expired = expiredCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FridgeStats()
    )

    // --- Actions for Fridge Items ---
    fun addFridgeItem(
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        expiryDate: Long,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            val item = FridgeItem(
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                expiryDate = expiryDate,
                location = location,
                notes = notes
            )
            repository.insertFridgeItem(item)
        }
    }

    fun updateFridgeItem(item: FridgeItem) {
        viewModelScope.launch {
            repository.updateFridgeItem(item)
        }
    }

    fun deleteFridgeItem(item: FridgeItem, autoAddToShoppingList: Boolean = false) {
        viewModelScope.launch {
            repository.deleteFridgeItem(item)
            if (autoAddToShoppingList) {
                // Auto add to shopping list when food item is deleted/finished
                addShoppingItem(item.name, item.quantity, item.unit, item.category)
            }
        }
    }

    fun quickAdjustQuantity(item: FridgeItem, change: Double) {
        viewModelScope.launch {
            val newQty = (item.quantity + change).coerceAtLeast(0.0)
            if (newQty <= 0.0) {
                // If quantity reaches 0, delete and optionally suggest adding to shopping
                repository.deleteFridgeItem(item)
                addShoppingItem(item.name, 1.0, item.unit, item.category)
            } else {
                repository.updateFridgeItem(item.copy(quantity = newQty))
            }
        }
    }

    // --- Actions for Shopping Items ---
    fun addShoppingItem(name: String, quantity: Double, unit: String, category: String) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val item = ShoppingItem(
                name = name,
                quantity = quantity,
                unit = unit,
                category = category
            )
            repository.insertShoppingItem(item)
        }
    }

    fun toggleShoppingItemChecked(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun clearPurchasedShoppingItems() {
        viewModelScope.launch {
            repository.deleteCheckedShoppingItems()
        }
    }

    // Move checked shopping items to fridge automatically!
    // This is super smart and helpful for users.
    fun transferBoughtItemsToFridge(defaultExpiryOffsetDays: Int = 7) {
        viewModelScope.launch {
            val bought = shoppingItems.value.filter { it.isChecked }
            val expiryTime = System.currentTimeMillis() + (defaultExpiryOffsetDays * 24L * 60L * 60L * 1000L)
            
            bought.forEach { shoppingItem ->
                // Insert into Fridge
                val fridgeItem = FridgeItem(
                    name = shoppingItem.name,
                    category = shoppingItem.category,
                    quantity = shoppingItem.quantity,
                    unit = shoppingItem.unit,
                    expiryDate = expiryTime,
                    location = "冷藏",
                    notes = "從購物清單匯入"
                )
                repository.insertFridgeItem(fridgeItem)
                
                // Delete from Shopping List
                repository.deleteShoppingItem(shoppingItem)
            }
        }
    }


}

// --- ViewModel Factory ---
class FridgeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FridgeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FridgeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// --- Companion Support Classes ---

enum class SortOrder {
    NAME_ASC,       // 名稱遞增
    EXPIRY_ASC,     // 到期日最近 (優先即期)
    EXPIRY_DESC,    // 到期日最遠
    ADDED_DESC,     // 最新加入
    QUANTITY_DESC   // 數量遞減
}

data class FridgeStats(
    val total: Int = 0,
    val fresh: Int = 0,
    val expiringSoon: Int = 0,
    val expired: Int = 0
)
