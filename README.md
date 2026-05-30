# 🧊 冰箱守護精靈 (Fridge Spirit)

> 歡迎來到 **冰箱守護精靈 ‧ 小冰** 的暖心溫室！這是一款基於 **Jetpack Compose** 與 **Room 區域資料庫** 打造的離線优先（Offline-First）冰箱食材儲存管理與購物整合助理。
> 我們捨棄了冰冷無趣的傳統表格，改以療癒的插圖、精緻的統計佔比，與可愛精靈「小冰」的靈動互動，讓您的食材管理日常變得優雅又充滿溫度！🐾🌸

---

## 🌟 核心特色功能

### 1. 🗃️ 聰明食材庫存管理 (Smart Ingredient Inventory)
* **繽紛分類卡片**：內建 **蔬果🥬、肉類與生鮮🥩、蛋奶與乳製品🥛、飲料與醬料🍹、熟食剩菜🍲、其他📦** 6 大標籤，每個類別皆對應活潑專屬色。
* **智慧鮮度警示**：採用「安全、即期、已過期」三色燈號，自動在過期前發起暖心提醒。
* **快捷狀態切換**：卡片上即時勾選過期狀態，簡潔俐落的滑動載入與更新。

### 2. 📊 直觀食材種類佔比 (Visual Category Distribution)
* **視覺化分類佔比條**：內建精美的 `CategoryBreakdownCard`，動態計算當前冰箱中各類別食材的百分比與件數。
* **清爽配色語意**：利用乾淨俐落的進度條與 Emoji 符號，讓冰箱裡的儲藏分布一目了然！

### 3. 🧸 精靈暖心互動面板 (The Spirit Panel & Stats)
* **守護狀態語句**：透過冰箱精靈「小冰」，在冰箱完全充滿健康食材時，發放「食材管理模範大獎模範徽章」；當空空如也時，則是「小精靈在睡大覺」的逗趣互動。
* **暖心小常識館**：精選雙擊（Double Tap）切換常識卡片功能，為您科普如何更好地保存番茄、冷藏海鮮等實用廚房小妙招。

### 4. 🛒 整合型採買備忘單 (Unified Shopping List)
* **離線記事本**：支援購物清單之新增、份量單位備註、大類標示。
* **一鍵快速勾銷**：一目了然的勾選狀態（Strikethrough）回饋，買完食材後隨手打勾，極致流暢。

### 5. 🎨 極致感官視覺體驗 (Visual Craftsmanship)
* **湖水綠主色調 (`TealPrimary`)**：溫和眼部負擔的科技湖水綠感配色，搭配大圓角 M3 設計語彙。
* **特製小精靈 Toast 彈窗 (`CutieToast`)**：操作時觸發精美自定義 Toast 提示框，附帶超 Q 表情與圓角邊框，大大增強人機互動的愉悅感。

---

## 🛠️ 開發技術棧與架構

本專案本著高品質與高穩定性的現代 Android 開發標準：

* **UI 框架**：純 **Jetpack Compose** 聲明式 UI 編寫，確保流暢而生動的轉折動畫。
* **狀態管理**：基於 Android **MVVM 架構模式**，完美封裝 `FridgeViewModel`。利用 `MutableStateFlow` 與 `collectAsStateWithLifecycle` 監聽生命週期狀態，防範記憶體洩漏。
* **資料層持久化**：使用官方推薦的 **Room Database (SQLite)** 實現本地高效率離線儲存。食材數據、採購單與统计資料皆在本地，極致守護使用者隱私。
* **異步處理**：**Kotlin Coroutines (協程)** 與 `Flow` 進行資料庫流式查詢傳導，即便背景處理極大宗食材亦保證主線程零卡頓。
* **主體樣式**：完美相容於 Material Design 3 (M3) 標準，具備流暢的 Ripple 水波紋回饋與寬裕大方的排版間距（8dp 網格基準）。

---

## 🚀 專案目錄引導

```text
app/src/main/
├── java/com/example/
│   ├── MainActivity.kt             # 應用生命週期主入口
│   ├── data/
│   │   ├── AppDatabase.kt          # Room 資料庫配置
│   │   ├── FridgeDao.kt            # 資料庫操作 DAO 介面
│   │   ├── FridgeItem.kt           # 冰箱食材儲存實體域 (Model)
│   │   └── ShoppingItem.kt         # 採購單食材儲存實體域 (Model)
│   └── ui/
│       ├── FridgeViewModel.kt      # 中央狀態機控制器 (State Processor)
│       ├── FridgeMainScreen.kt     # Jetpack Compose 主介面 (Tab 1 ~ 3 + 互動元件)
│       └── theme/
│           ├── Color.kt            # 專屬精緻顏色宣告
│           ├── Theme.kt            # 冰箱精靈主題配置
│           └── Type.kt             # Material 3 舒適字體系統
└── res/
    └── values/
        └── strings.xml             # 語系與應用名稱 strings 定義
```

---

## 🧪 測試與驗證

為確保每個模塊的穩定健康，專案中搭配了敏捷本地 JVM 單元測試：
* 內部整合 **Robolectric** 進行本地 UI 與狀態驗證。
* 執行測試指令：
  ```bash
  gradle :app:testDebugUnitTest
  ```
  *(本地測試運行均正常，保證 100% 構建通過！)*

---

## 🌸 主廚小冰精靈的結語
「*用心保存，熱愛生活！在小冰的守護下，一起成為精明又優雅的食材整理大師吧！🐾🌸*」
