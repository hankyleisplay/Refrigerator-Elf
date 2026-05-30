package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class CutieToast(
    val message: String,
    val icon: String = "🌟"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeMainScreen(
    viewModel: FridgeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 冰箱食材, 1: 溫馨叮嚀, 2: 備忘清單
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<FridgeItem?>(null) }
    var activeToast by remember { mutableStateOf<CutieToast?>(null) }

    val context = LocalContext.current
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()

    LaunchedEffect(activeToast) {
        if (activeToast != null) {
            kotlinx.coroutines.delay(3500)
            activeToast = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Kitchen,
                                contentDescription = "App Icon",
                                tint = TealLight,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "冰箱精靈 ‧ 食物管家",
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                fontSize = 22.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SoftTealContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Kitchen, contentDescription = "冰箱") },
                        label = { Text("冰箱食材") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = SoftTealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "精靈") },
                        label = { Text("溫馨叮嚀") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = SoftTealContainer
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "採購") },
                        label = { Text("備忘清單") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            indicatorColor = SoftTealContainer
                        )
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = TealPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_item_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "新增食材")
                    }
                } else if (selectedTab == 2) {
                    var showAddShoppingDialog by remember { mutableStateOf(false) }
                    FloatingActionButton(
                        onClick = { showAddShoppingDialog = true },
                        containerColor = TealLight,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("add_shopping_fab")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = "新增採購項目")
                    }

                    if (showAddShoppingDialog) {
                        QuickAddShoppingItemDialog(
                            onDismiss = { showAddShoppingDialog = false },
                            onConfirm = { name, q, unit, cat ->
                                viewModel.addShoppingItem(name, q, unit, cat)
                                activeToast = CutieToast("收到！已將 [${name}] 登記在採購清單上囉～🛒🧸", "📝")
                                showAddShoppingDialog = false
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Render content depending on selected tab
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_navigation"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> FridgeTabContent(
                                viewModel = viewModel,
                                onEditItem = { item -> editingItem = item },
                                statistics = statistics,
                                showToast = { msg, icon -> activeToast = CutieToast(msg, icon) }
                            )
                            1 -> FridgeSpiritTabContent(
                                viewModel = viewModel,
                                showToast = { msg, icon -> activeToast = CutieToast(msg, icon) }
                            )
                            2 -> ShoppingTabContent(
                                viewModel = viewModel,
                                showToast = { msg, icon -> activeToast = CutieToast(msg, icon) }
                            )
                        }
                    }
                }
            }
        }

        // Beautiful custom animated Toast overlay at the TOP of the screen (z-indexed 99)
        AnimatedVisibility(
            visible = activeToast != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
                .padding(horizontal = 20.dp)
                .zIndex(99f) // Always on top
        ) {
            activeToast?.let { toast ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F2)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFDA4AF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeToast = null }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = toast.icon, fontSize = 24.sp)
                        Text(
                            text = toast.message,
                            color = Color(0xFFBE123C),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "💖", fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Dialogue for Add Material
    if (showAddDialog) {
        FridgeItemAddEditDialog(
            item = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, quantity, unit, expiry, location, notes ->
                viewModel.addFridgeItem(name, category, quantity, unit, expiry, location, notes)
                activeToast = CutieToast("啵！可愛的 [${name}] 已經安心放進冷藏庫囉～🏡🥦", "✨")
                showAddDialog = false
            }
        )
    }

    // Dialogue for Edit Material
    if (editingItem != null) {
        FridgeItemAddEditDialog(
            item = editingItem,
            onDismiss = { editingItem = null },
            onConfirm = { name, category, quantity, unit, expiry, location, notes ->
                viewModel.updateFridgeItem(
                    editingItem!!.copy(
                        name = name,
                        category = category,
                        quantity = quantity,
                        unit = unit,
                        expiryDate = expiry,
                        location = location,
                        notes = notes
                    )
                )
                activeToast = CutieToast("叮咚！[${name}] 的檔案已重新整理完畢喵～🐾💓", "🎀")
                editingItem = null
            }
        )
    }
}

// ==========================================
// SCREEN CONTENTS: FRIDGE TAB
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FridgeTabContent(
    viewModel: FridgeViewModel,
    onEditItem: (FridgeItem) -> Unit,
    statistics: FridgeStats,
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val items by viewModel.fridgeItems.collectAsStateWithLifecycle()
    val rawItems by viewModel.rawFridgeItems.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val activeLocation by viewModel.selectedLocationFilter.collectAsStateWithLifecycle()
    val activeSort by viewModel.selectedSortOrder.collectAsStateWithLifecycle()

    var showSortMenu by remember { mutableStateOf(false) }

    val categories = listOf("全部", "蔬果", "肉類與生鮮", "蛋奶與乳製品", "飲料與醬料", "熟食剩菜", "其他")
    val locations = listOf("全部", "冷藏", "冷凍", "常溫")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Stats Banner Box
        StatsBanner(stats = statistics)

        Spacer(modifier = Modifier.height(10.dp))

        CategoryBreakdownCard(items = rawItems)

        Spacer(modifier = Modifier.height(12.dp))

        // Search & Filter Sort Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = 56.dp)
                    .testTag("search_input"),
                placeholder = { Text("搜尋食材或備註...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜尋", tint = Color.Gray) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .background(SoftTealContainer, RoundedCornerShape(12.dp))
                        .size(48.dp)
                        .testTag("sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "排序",
                        tint = TealPrimary
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("即期優先 ⏳") },
                        onClick = {
                            viewModel.selectedSortOrder.value = SortOrder.EXPIRY_ASC
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("最久過期 ✨") },
                        onClick = {
                            viewModel.selectedSortOrder.value = SortOrder.EXPIRY_DESC
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("名稱 (A-Z) 🔠") },
                        onClick = {
                            viewModel.selectedSortOrder.value = SortOrder.NAME_ASC
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("最新增添 🆕") },
                        onClick = {
                            viewModel.selectedSortOrder.value = SortOrder.ADDED_DESC
                            showSortMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("數量多至少 📦") },
                        onClick = {
                            viewModel.selectedSortOrder.value = SortOrder.QUANTITY_DESC
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Filter Carousel
        Text(
            text = "食材類別",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == activeCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCategoryFilter.value = cat },
                    label = { Text(cat, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_$cat")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Location Filter Chips
        Text(
            text = "存放空間",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(locations) { loc ->
                val isSelected = loc == activeLocation
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedLocationFilter.value = loc },
                    label = { Text(loc, fontSize = 13.sp) },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = TealLight,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main List & Empty State Check
        if (rawItems.isEmpty()) {
            FridgeEmptyState(hint = "冰箱空空的！\n點擊右下角 + 按鈕開始加入食材 🥬")
        } else if (items.isEmpty()) {
            FridgeEmptyState(hint = "找不到符合當前篩選條件的食材 🔍")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    FridgeItemCard(
                        item = item,
                        onCardClick = { onEditItem(item) },
                        onIncreaseQty = {
                            viewModel.quickAdjustQuantity(item, 0.5)
                            showToast("放了更多 [${item.name}] 到冷藏室囉～🥄✨", "🍰")
                        },
                        onDecreaseQty = {
                            val nextQty = item.quantity - 0.5
                            if (nextQty <= 0) {
                                showToast("[${item.name}] 吃光光囉！已經自動為你記在採買單中囉～🍽️❤️", "🌈")
                            } else {
                                val displayQty = if (nextQty % 1.0 == 0.0) nextQty.toInt() else nextQty
                                showToast("消滅了一些 [${item.name}]！剩餘約 $displayQty ${item.unit}～😋🍴", "🥄")
                            }
                            viewModel.quickAdjustQuantity(item, -0.5)
                        },
                        onDeleteClick = {
                            viewModel.deleteFridgeItem(item, autoAddToShoppingList = true)
                            showToast("已從冰箱移除了 [${item.name}] 唷！🍃✨", "🌟")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsBanner(stats: FridgeStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SoftTealContainer)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Stats",
                    tint = TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "冰箱盤點摘要",
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatSubBox("食材總量", stats.total.toString(), Color.DarkGray)
                StatSubBox("新鮮安全", stats.fresh.toString(), FreshGreen)
                StatSubBox("即期食材", stats.expiringSoon.toString(), ExpiringAmber)
                StatSubBox("已過期", stats.expired.toString(), ExpiredRed)
            }
        }
    }
}

@Composable
fun RowScope.StatSubBox(label: String, valStr: String, valueColor: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = valStr,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

data class CategoryMeta(val name: String, val emoji: String, val color: Color)

@Composable
fun CategoryBreakdownCard(items: List<FridgeItem>) {
    if (items.isEmpty()) return
    
    val totalCount = items.size.toDouble()
    val categoryCounts = items.groupBy { it.category }
        .mapValues { it.value.size }
    
    val categoryDetails = listOf(
        CategoryMeta("蔬果", "🥬", FreshGreen),
        CategoryMeta("肉類與生鮮", "🥩", Color(0xFFF43F5E)),
        CategoryMeta("蛋奶與乳製品", "🥛", Color(0xFF3B82F6)),
        CategoryMeta("飲料與醬料", "🍹", Color(0xFFF59E0B)),
        CategoryMeta("熟食剩菜", "🍲", Color(0xFF8B5CF6)),
        CategoryMeta("其他", "📦", Color(0xFF6B7280))
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📊 食材種類佔比",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            categoryDetails.forEach { meta ->
                val count = categoryCounts[meta.name] ?: 0
                if (count > 0) {
                    val percent = count / totalCount
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(meta.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = meta.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            modifier = Modifier.width(80.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(percent.toFloat())
                                    .background(meta.color, RoundedCornerShape(3.dp))
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${count}項 (${(percent * 100).toInt()}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.width(64.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FridgeItemCard(
    item: FridgeItem,
    onCardClick: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val daysLeft = item.getDaysRemaining(currentTime)
    val status = item.getFreshnessStatus(currentTime)

    val (badgeBg, badgeText, statusLabel) = when (status) {
        FreshnessStatus.EXPIRED -> Triple(
            ExpiredRed.copy(alpha = 0.15f),
            ExpiredRed,
            if (daysLeft == -1) "已過期 1 天" else "已過期 ${-daysLeft} 天"
        )
        FreshnessStatus.EXPIRING_SOON -> Triple(
            ExpiringAmber.copy(alpha = 0.15f),
            ExpiringAmber,
            if (daysLeft == 0) "今天到期！" else "即期 剩餘 $daysLeft 天"
        )
        FreshnessStatus.FRESH -> Triple(
            FreshGreen.copy(alpha = 0.15f),
            FreshGreen,
            "剩餘 $daysLeft 天"
        )
    }

    val locationColor = when (item.location) {
        "冷凍" -> Color(0xFF3B82F6)  // blue
        "常溫" -> Color(0xFF8B5CF6)  // purple
        else -> TealPrimary         // teal/greenish
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("fridge_item_card_${item.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge icon selector
                    val icon = when (item.category) {
                        "蔬果" -> "🥦"
                        "肉類與生鮮" -> "🥩"
                        "蛋奶與乳製品" -> "🥛"
                        "飲料與醬料" -> "🍹"
                        "熟食剩菜" -> "🍲"
                        else -> "🍱"
                    }
                    Text(text = icon, fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                    
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Storage Location Tag
                    Box(
                        modifier = Modifier
                            .background(locationColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.location,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = locationColor
                        )
                    }

                    // Freshness Badge
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                }

                // Display Note if present
                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📝：${item.notes}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right side: Quantity editor
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_item_${item.name}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "吃完了/刪除",
                        tint = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecreaseQty,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "減少",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    val formattedQty = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
                    Text(
                        text = "$formattedQty${item.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        color = Color.DarkGray
                    )

                    IconButton(
                        onClick = onIncreaseQty,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "增加",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FridgeEmptyState(hint: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Kitchen,
                contentDescription = "Empty Refrigerator",
                modifier = Modifier.size(80.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = hint,
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

enum class MascotExpression {
    HAPPY,
    WORRIED,
    SAD
}

@Composable
fun CuteMascot(
    expression: MascotExpression,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Body color depends on expression
        val bodyColor = when (expression) {
            MascotExpression.HAPPY -> Color(0xFFFAF8F6) // Creamy solid warm white
            MascotExpression.WORRIED -> Color(0xFFFEF3C7) // Pastel warm honey yellow
            MascotExpression.SAD -> Color(0xFFE2E8F0) // Slated soft gray-blue
        }
        
        val strokeColor = Color(0xFF334155) // Dark slate contour
        val strokeWidth = 5f
        
        // 1. Draw head/body box representing a cute retro round refrigerator
        val bodyRect = androidx.compose.ui.geometry.Rect(w * 0.18f, h * 0.20f, w * 0.82f, h * 0.90f)
        val cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.12f)
        
        // Draw standard shadow
        drawRoundRect(
            color = Color(0x0C000000),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.20f, h * 0.23f),
            size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.70f),
            cornerRadius = cornerRadius
        )
        
        // Draw solid body
        drawRoundRect(
            color = bodyColor,
            topLeft = bodyRect.topLeft,
            size = bodyRect.size,
            cornerRadius = cornerRadius
        )
        
        // Draw outer thick brush stroke outline
        drawRoundRect(
            color = strokeColor,
            topLeft = bodyRect.topLeft,
            size = bodyRect.size,
            cornerRadius = cornerRadius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // 2. Fridge Door center horizontal division line
        drawLine(
            color = strokeColor,
            start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.58f),
            end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.58f),
            strokeWidth = strokeWidth
        )
        
        // 3. Refrigerator Doors handles (round knobs)
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.44f),
            size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.10f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
        )
        drawRoundRect(
            color = Color(0xFF64748B),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.64f),
            size = androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.10f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
        )
        
        // 4. Rosy pink blushing cheeks
        drawCircle(
            color = Color(0xFFFDA4AF),
            radius = w * 0.06f,
            center = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.40f)
        )
        drawCircle(
            color = Color(0xFFFDA4AF),
            radius = w * 0.06f,
            center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.40f)
        )
        
        // 5. Draw Eye elements and mouths based on spirit's emotion state
        when (expression) {
            MascotExpression.HAPPY -> {
                // Curved smiling eyes arching up ^^
                val pathLeft = androidx.compose.ui.graphics.Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.36f, h * 0.26f, w * 0.44f, h * 0.34f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                val pathRight = androidx.compose.ui.graphics.Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.56f, h * 0.26f, w * 0.64f, h * 0.34f),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                }
                drawPath(path = pathLeft, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth + 1f))
                drawPath(path = pathRight, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth + 1f))
                
                // Cute open happy smile tongue mouth
                val smilePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.46f, h * 0.42f)
                    quadraticTo(w * 0.50f, h * 0.50f, w * 0.54f, h * 0.42f)
                }
                drawPath(path = smilePath, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                drawCircle(color = strokeColor, radius = 5f, center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.45f))
            }
            MascotExpression.WORRIED -> {
                // Small worried pill circles for eyes
                drawCircle(color = strokeColor, radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.31f))
                drawCircle(color = strokeColor, radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.31f))
                
                // Straight tiny line mouth representing "O_O"
                drawLine(
                    color = strokeColor,
                    start = androidx.compose.ui.geometry.Offset(w * 0.46f, h * 0.42f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.54f, h * 0.42f),
                    strokeWidth = strokeWidth
                )
                
                // Little blue anxiety sweat drop on forehead
                drawCircle(color = Color(0xFF60A5FA), radius = 9f, center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.25f))
                val sweatPeak = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.72f, h * 0.25f - 9f)
                    lineTo(w * 0.72f - 7f, h * 0.25f)
                    lineTo(w * 0.72f + 7f, h * 0.25f)
                    close()
                }
                drawPath(path = sweatPeak, color = Color(0xFF60A5FA))
            }
            MascotExpression.SAD -> {
                // Sad arching down crying eyes
                val pathLeft = androidx.compose.ui.graphics.Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.36f, h * 0.28f, w * 0.44f, h * 0.36f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                }
                val pathRight = androidx.compose.ui.graphics.Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.56f, h * 0.28f, w * 0.64f, h * 0.36f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                }
                drawPath(path = pathLeft, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                drawPath(path = pathRight, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                
                // Small sad inverted mouth
                val sadMouth = androidx.compose.ui.graphics.Path().apply {
                    arcTo(
                        rect = androidx.compose.ui.geometry.Rect(w * 0.47f, h * 0.42f, w * 0.53f, h * 0.48f),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                }
                drawPath(path = sadMouth, color = strokeColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth))
                
                // Bright light blue crying tear circles dripping off face!
                drawCircle(color = Color(0xFF38BDF8), radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.46f))
                drawCircle(color = Color(0xFF38BDF8), radius = 7f, center = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.46f))
            }
        }
        
        // 6. ADORABLE red antenna heart on top of head!
        val antX = w * 0.5f
        val antY = h * 0.20f
        drawLine(
            color = strokeColor,
            start = androidx.compose.ui.geometry.Offset(antX, antY),
            end = androidx.compose.ui.geometry.Offset(antX, antY - w * 0.07f),
            strokeWidth = strokeWidth
        )
        drawCircle(
            color = Color(0xFFF43F5E), // Sparkling warm berry rose
            radius = w * 0.045f,
            center = androidx.compose.ui.geometry.Offset(antX, antY - w * 0.07f)
        )
    }
}

@Composable
fun FridgeSpiritTabContent(
    viewModel: FridgeViewModel,
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val items by viewModel.rawFridgeItems.collectAsStateWithLifecycle()
    
    // Classify the system environment to determine mascot moods
    val expiredItems = items.filter { it.getDaysRemaining() < 0 }
    val expiringItems = items.filter { it.getDaysRemaining() in 0..3 }
    val totalCount = items.size
    
    val currentExpression = when {
        expiredItems.isNotEmpty() -> MascotExpression.SAD
        expiringItems.isNotEmpty() -> MascotExpression.WORRIED
        else -> MascotExpression.HAPPY
    }
    
    // Bouncing Animation trigger
    val infiniteTransition = rememberInfiniteTransition(label = "bouncing_spirit")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floater"
    )
    
    // Cute tips rotating state when clicked
    val kitchenTips = listOf(
        "🍳 雞蛋尖端朝下放、圓端朝上放，保鮮時間能延長一倍唷！因為圓端有氣孔呼吸～🥚🌸",
        "🥬 櫻桃番茄、蘋果等會釋放熟成乙烯氣體，蔬菜和牠們放在一起容易爛，請盡量隔開密封冷藏喔！🍅",
        "🥩 肉類如果這兩天內沒有馬上要吃，記得直接放入「冷凍室」封裝，避免冷藏細菌滋生影響風味唷！❄️",
        "🥦 蔬菜冷藏前用噴微濕的乾淨廚房紙巾包好放塑料袋，直立擺放可以保持水嫩多汁、脆綠如新！🥬💅",
        "🍹 開封後的鮮乳或飲料不要常放在「冰箱門邊」，那裡開關門溫差最大，容易變質，放中間內側最安全！🥛",
        "🍥 剩菜熟食一定要加蓋或包上保鮮膜，並且在兩天之內徹底加熱完食用完畢，安心又健康！🍛✨"
    )
    var currentTipIdx by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Cute Guardian Spirit Card Panel
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFDE047)), // Beautiful cartoon yellow outline
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ 冰箱守護精靈 ‧ 小冰 ✨",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBE123C),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Canvas Mascot drawing animated bounces up and down
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .offset(y = floatOffset.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CuteMascot(
                            expression = currentExpression,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Bubble Dialog speech container
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val adviceText = when (currentExpression) {
                                MascotExpression.HAPPY -> {
                                    if (totalCount == 0) {
                                        "嗨！主人～冰箱現在空空的耶！小冰正在乖乖地打掃冷藏室～出門散步別忘了買些美味營養的新鮮食材回來入住唷！主人辛苦囉～🐾🎈"
                                    } else {
                                        "哇～冰箱裡整理得真棒！每一個好料都住得舒舒服服！小冰給你一個大大的擁抱～🥰 所有的食材都非常安全新鮮，主人有好好做飯、精緻生活唷！💖✨"
                                    }
                                }
                                MascotExpression.WORRIED -> {
                                    "主人主人！小冰聞到有 ${expiringItems.size} 種食材快要到期了喔！🥺🥕 大家都在探頭等著上餐桌呢。今晚要不要挑選 [${expiringItems.firstOrNull()?.name ?: ""}] 做個快炒或美味湯品呢？小梅和小冰陪主人一起消滅食材！🍽️❤️"
                                }
                                MascotExpression.SAD -> {
                                    "大反省大作戰！嗚嗚嗚...冰箱有 ${expiredItems.size} 樣食材寂寞地過期了，小冰流下了悲傷的眼淚💔 為了健康，建議早點清空牠們唷！今晚我們振作精神、重新出發好嗎？主人加油！💪😭🌟"
                                }
                            }
                            Text(
                                text = adviceText,
                                fontSize = 13.sp,
                                color = Color(0xFF9F1239),
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        // 2. Warm Notice Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Notices",
                    tint = TealPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "冰箱健康檢測報告",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }
        }
        
        // 3. Conditional Alert lists
        if (totalCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftTealContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧸 冰箱空空，精靈睡著囉", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "點擊下方菜單右側的「備忘清單」把想買的水果、牛奶寫下，出門時小冰就會隨身提醒你唷！",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            // Overdue food listing
            if (expiredItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💔 幽靈過期警報！(${expiredItems.size}項)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ExpiredRed)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("👻", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            expiredItems.forEach { item ->
                                val daysAgo = -item.getDaysRemaining()
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("❌ ", fontSize = 12.sp)
                                    Column {
                                        Text(
                                            text = "${item.name} (${item.quantity} ${item.unit})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "位於 [${item.location}] ‧ 在 $daysAgo 天前就過期囉喵",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7F1D1D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Expiring priority listing
            if (expiringItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳ 急速搶救！即期名單 (${expiringItems.size}項)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ExpiringAmber)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("⏰", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            expiringItems.forEach { item ->
                                val rem = item.getDaysRemaining()
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("⚠️ ", fontSize = 12.sp)
                                    Column {
                                        Text(
                                            text = "${item.name} 剩下 $rem 天過期",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Text(
                                            text = "快拿出來煮一煮！建議烹飪提示：與簡單時蔬快速清炒，方便又溫馨",
                                            fontSize = 11.sp,
                                            color = Color(0xFF78350F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Healthy Green State
        if (expiredItems.isEmpty() && expiringItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("💖", fontSize = 28.sp)
                        Column {
                            Text("冰箱綠色安全狀態中！", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF065F46))
                            Text("所有食材保鮮度完美～小冰給主人頒發食材管理模範大獎！繼續維持美味健康的習慣唷🐾💐", fontSize = 12.sp, color = Color(0xFF047857), lineHeight = 16.sp)
                        }
                    }
                }
            }
        }


        // 5. Double tap Refreshable Secret Tip Panel
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD1D5DB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentTipIdx = (currentTipIdx + 1) % kitchenTips.size
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧸", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "小冰精靈的暖心常識館",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "按我換一個 🔄",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = kitchenTips[currentTipIdx],
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}


// Border utility shorthand
object RowDefaults {
    val CardBorder = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
}

// ==========================================
// SCREEN CONTENTS: SHOPPING LIST TAB
// ==========================================

@Composable
fun ShoppingTabContent(
    viewModel: FridgeViewModel,
    showToast: (String, String) -> Unit = { _, _ -> }
) {
    val items by viewModel.shoppingItems.collectAsStateWithLifecycle()
    val checkedCount = items.count { it.isChecked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Quick Action banner to clear or migrate
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftTealContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "採購",
                        tint = TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "採買智慧助理",
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "當冰箱物件被歸零或刪除時，會自動記錄在採買單中喔！買齊後按一鍵就能把它們匯入冷藏保存囉。",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                if (checkedCount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.transferBoughtItemsToFridge()
                                showToast("超級棒！買齊的 $checkedCount 項食材都順利入住冰箱吹冷氣了～🍇🍦", "🎉")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = "歸庫", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("把買齊的 $checkedCount 項放回冰箱", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearPurchasedShoppingItems()
                                showToast("已順利把所有買完的紀錄通通打掃乾淨囉～🧹🌟", "🧹")
                            },
                            modifier = Modifier.height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpiredRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExpiredRed.copy(0.3f)),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("清除已買", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main List & Empty State Check
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentPaste,
                        contentDescription = "List",
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "沒什麼需要採買的，冰箱滿滿好料 ✨",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "目前待辦採買單",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "共計 ${items.size} 個缺漏食材",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onCheckedChange = {
                            val isCheckedNew = !item.isChecked
                            if (isCheckedNew) {
                                showToast("買齊 [${item.name}] 囉！乖乖打勾記下～🛒🥳", "❤️")
                            } else {
                                showToast("哎呀，[${item.name}] 原來還沒放進購物車呀～🛍️🌱", "👀")
                            }
                            viewModel.toggleShoppingItemChecked(item)
                        },
                        onDeleteClick = {
                            viewModel.deleteShoppingItem(item)
                            showToast("已將 [${item.name}] 從備忘單中悄悄撕掉囉～🗑️🐾", "🍃")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onCheckedChange: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .testTag("shopping_item_${item.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) Color(0xFFF8FAFC) else Color.White
        ),
        border = RowDefaults.CardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (item.isChecked) Color.Gray else Color(0xFF334155),
                    textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    text = "分組：${item.category}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString()
            Text(
                text = "$qtyStr ${item.unit}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (item.isChecked) Color.Gray else TealPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "刪除",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


// ==========================================
// DIALOGUES & FORM VIEWS
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FridgeItemAddEditDialog(
    item: FridgeItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Long, String, String) -> Unit
) {
    val isEdit = item != null

    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "蔬果") }
    var quantityStr by remember { mutableStateOf(item?.quantity?.toString() ?: "1.0") }
    var unit by remember { mutableStateOf(item?.unit ?: "個") }
    var location by remember { mutableStateOf(item?.location ?: "冷藏") }
    var notes by remember { mutableStateOf(item?.notes ?: "") }

    // Handled Calendar Date
    val calendar = Calendar.getInstance()
    if (isEdit) {
        calendar.timeInMillis = item!!.expiryDate
    } else {
        // Default to +7 days for adding convenience
        calendar.add(Calendar.DAY_OF_YEAR, 7)
    }
    var expiryTimeMillis by remember { mutableStateOf(if (isEdit) item!!.expiryDate else calendar.timeInMillis) }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.TAIWAN)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(10.dp)
                .testTag("add_item_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = RowDefaults.CardBorder
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (isEdit) "編輯內部食材" else "增添冰箱新食材",
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    // Ingredient input
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth().testTag("add_item_dialog_name"),
                        label = { Text("食材名稱 (如：高麗菜)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                    )
                }

                item {
                    // Category Selection Chips
                    Text(
                        text = "食材類別：",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    val cats = listOf("蔬果", "肉類與生鮮", "蛋奶與乳製品", "飲料與醬料", "熟食剩菜", "其他")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cats.forEach { cat ->
                            val selected = category == cat
                            FilterChip(
                                selected = selected,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealLight,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    // Row for Quantity & Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("食材數量") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                        )

                        // Shorthand Unit Chips / Selector
                        var showUnitDrop by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showUnitDrop = true }
                                .padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("單位 (可點擊選)") },
                                singleLine = true,
                                readOnly = false, // Allow typed or drop selected
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary),
                                trailingIcon = {
                                    IconButton(onClick = { showUnitDrop = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "選擇")
                                    }
                                }
                            )

                            val units = listOf("個", "包", "瓶", "克", "台斤", "盒", "條", "碗", "根")
                            DropdownMenu(
                                expanded = showUnitDrop,
                                onDismissRequest = { showUnitDrop = false }
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            showUnitDrop = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Expiry Date choosing
                    Column {
                        Text(
                            text = "安全保存期限：",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    val currentPickCal = Calendar.getInstance().apply {
                                        timeInMillis = expiryTimeMillis
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, y, m, d ->
                                            val newCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, y)
                                                set(Calendar.MONTH, m)
                                                set(Calendar.DAY_OF_MONTH, d)
                                                set(Calendar.HOUR_OF_DAY, 23)
                                                set(Calendar.MINUTE, 59)
                                                set(Calendar.SECOND, 59)
                                            }
                                            expiryTimeMillis = newCal.timeInMillis
                                        },
                                        currentPickCal.get(Calendar.YEAR),
                                        currentPickCal.get(Calendar.MONTH),
                                        currentPickCal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = TealPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = sdf.format(Date(expiryTimeMillis)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "編輯保存日 📅",
                                fontSize = 12.sp,
                                color = TealPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Shortcuts to expand expiry date easily
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExpiryShortcutButton("+3天", 3) { expiryTimeMillis = calculateShortcutTime(3) }
                            ExpiryShortcutButton("+7天 (1周)", 7) { expiryTimeMillis = calculateShortcutTime(7) }
                            ExpiryShortcutButton("+14天 (2周)", 14) { expiryTimeMillis = calculateShortcutTime(14) }
                            ExpiryShortcutButton("+30天 (1月)", 30) { expiryTimeMillis = calculateShortcutTime(30) }
                        }
                    }
                }

                item {
                    // Storage Location
                    Text(
                        text = "存放冰箱空間：",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val spaceNames = listOf("冷藏", "冷凍", "常溫")
                        spaceNames.forEach { sp ->
                            val active = location == sp
                            val spColor = when (sp) {
                                "冷凍" -> Color(0xFF3B82F6)
                                "常溫" -> Color(0xFF8B5CF6)
                                else -> TealPrimary
                            }
                            ElevatedFilterChip(
                                selected = active,
                                onClick = { location = sp },
                                label = { Text(sp) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = spColor,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    // Additional commentary notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("備註與料理方式 (選填)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealPrimary)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Actions Dialog Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Text("取消", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val calculatedQty = quantityStr.toDoubleOrNull() ?: 1.0
                                    onConfirm(
                                        name.trim(),
                                        category,
                                        calculatedQty,
                                        unit.trim(),
                                        expiryTimeMillis,
                                        location,
                                        notes.trim()
                                    )
                                }
                            },
                            enabled = name.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("add_item_dialog_confirm"),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("儲存", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ExpiryShortcutButton(label: String, daysToAdd: Int, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE2E8F0),
            contentColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// Clear shortcut time math
fun calculateShortcutTime(daysToAdd: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    return cal.timeInMillis
}

@Composable
fun QuickAddShoppingItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1.0") }
    var unit by remember { mutableStateOf("個") }
    var category by remember { mutableStateOf("其他") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = RowDefaults.CardBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "增添採買購物目標",
                    fontWeight = FontWeight.Bold,
                    color = TealLight,
                    fontSize = 17.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("採購食材（如：雞蛋）") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealLight),
                    modifier = Modifier.testTag("add_shopping_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("數量") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealLight)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("單位") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = TealLight)
                    )
                }

                // Drop-down quick group
                var showGroupDrop by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("類別分組") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showGroupDrop = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "選擇")
                            }
                        }
                    )

                    val itemGroups = listOf("蔬果", "肉類與生鮮", "蛋奶與乳製品", "飲料與醬料", "熟食剩菜", "其他")
                    DropdownMenu(
                        expanded = showGroupDrop,
                        onDismissRequest = { showGroupDrop = false }
                    ) {
                        itemGroups.forEach { grp ->
                            DropdownMenuItem(
                                text = { Text(grp) },
                                onClick = {
                                    category = grp
                                    showGroupDrop = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("放棄", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name.trim(),
                                    quantityStr.toDoubleOrNull() ?: 1.0,
                                    unit.trim(),
                                    category
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1f).testTag("add_shopping_confirm"),
                        colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("確定加入", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
