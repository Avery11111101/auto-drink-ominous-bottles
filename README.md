# Auto-Drink Ominous Bottles (自動喝不祥之瓶)

[English](#english) | [繁體中文](#繁體中文)

---

<a name="english"></a>
## English

A lightweight, purely client-side Fabric utility mod for Minecraft 26.3+ (maintained by Avery). Designed to streamline raid farming and quality-of-life gameplay: automatically drinks an **Ominous Bottle** upon raid victory, provides smart **Auto-Eat** assistance, and supports full customization via Mod Menu and Cloth Config.

Fully compliant with Modrinth Content Policies (No combat hacks, no TriggerBot).

### Features
1. **Raid Victory Auto-Drink**:
   - Monitors the raid boss bar in real-time.
   - Automatically drinks an Ominous Bottle the moment victory is achieved so you can seamlessly continue bad omen raid loops.
2. **Smart Auto-Eat (Quality-of-Life)**:
   - Automatically eats food when player hunger drops below a configurable threshold (Default: 14 / 7 drumsticks).
   - **Smart Filtering**: Automatically ignores harmful items (Rotten Flesh, Pufferfish, Poisonous Potato, Spider Eye) as well as precious Golden Apples and teleporting Chorus Fruits.
   - **Inventory Swapping**: If hotbar runs out of food, automatically pulls suitable food from the main inventory.
   - **Priority Protection**: Raid bottle drinking always takes priority over auto-eating.
3. **No-Spawn Retry Mechanism (AFK Friendly)**:
   - If an Ominous Bottle is consumed but no raid boss bar activates (e.g. no valid raid spawn location or interrupted), the mod automatically attempts to drink again up to a configurable retry limit (Default: 2 retries) before temporarily pausing to avoid wasting bottles.
4. **Real-time Action Feedback**:
   - Displays real-time on-screen notifications when drinking bottles, eating food, or performing retries.
   - Configurable modes: `Action Bar` (default), `Chat`, or `Disabled`.
5. **Mod Menu & Cloth Config Support**:
   - In-game GUI configuration via Cloth Config with dynamic conditional fields.
   - Built-in vanilla fallback screen if Cloth Config is not installed.
6. **Bottle Selection Strategy**:
   - `Highest Level First` (Default): Drinks the highest level bottle in inventory/hotbar.
   - `Lowest Level First`: Drinks the lowest level bottle.
   - `Exact Level Only`: Drinks only a specific level (1 to 5).
7. **Post-Drink Hotbar Restoration**:
   - `Restore Previous Held Slot` (Default): Seamlessly returns to whatever slot was held before drinking/eating.
   - `Switch to Fixed Weapon Slot`: Switches to a designated hotbar slot (1~9) so you can immediately resume combat or tool usage.
8. **Main Inventory Pulling**: Option to pull bottles from main inventory if none are found in the hotbar.

### Keybinds & Settings
- **Keybind Category**: Dedicated category **"Auto-Drink Ominous Bottles"** under Options → Controls → Key Binds.
- **Toggle Mod Keybind**: Default: `R` (Toggles the mod on/off with an on-screen notification).
- **Auto-Eat Settings**: Easily configured and toggled within the Mod Menu configuration screen.
- Configuration file saved at `.minecraft/config/auto_drink_ominous_bottles.json`.

### Disclaimer
> **Server Rule Notice**: This mod is purely client-side and does not alter server mechanics or provide combat automation. However, certain multiplayer servers may have strict policies regarding automatic actions or hotbar macros. Please verify your server's rules before enabling automatic features.

---

<a name="繁體中文"></a>
## 繁體中文

適用於 Minecraft 26.3+ 的純客戶端 Fabric 生活品質（QoL）輔助模組（由 Avery 維護製作）。專為襲擊（Raid）農場與日常冒險設計：在襲擊勝利時自動飲用**不祥之瓶 (Ominous Bottle)**，並具備智能**自動進食 (Auto-Eat)** 與完整的 Mod Menu / Cloth Config 設定介面。

全面符合 Modrinth 內容政策規範（無任何戰鬥外掛、無 TriggerBot）。

### 主要功能
1. **襲擊勝利自動觸發**：
   - 即時監聽襲擊 Boss 勝利狀態，戰勝瞬間自動飲用不祥之瓶，無縫重啟不祥之兆循環。
2. **智能自動進食 (Auto-Eat)**：
   - 當飢餓值低於指定閾值時（預設 14，即 7 格雞腿）自動進食。
   - **智能避雷過濾**：自動避開有害負面食物（腐肉、河豚、毒馬鈴薯、蜘蛛眼），並自動排除高價值金蘋果與具隨機傳送風險的紫頌果。
   - **跨背包調取**：快捷列缺少食物時，可自動從主背包交換至快捷列食用。
   - **優先權防護**：襲擊勝利喝瓶事件優先級高於自動進食，避免衝突。
3. **襲擊未生成重試飲用機制（長時間掛機救星）**：
   - 當飲用不祥之瓶後，若因伺服器判定吐血、無有效生怪點或網路波動導致未產生襲擊 BossBar，模組會在等待一段時間後**自動嘗試重新飲用**（預設最多重試 2 次），避免掛機中斷；達上限後自動暫停以避免無謂浪費。
4. **即時動作提示反饋 (Action Feedback)**：
   - 飲用不祥之瓶、自動進食、以及觸發重試動作時，可即時在螢幕上反饋當前動作（支援快捷欄上方 ActionBar 或聊天欄 Chat 訊息，亦可關閉）。
5. **模組選單 (Mod Menu) 動態設定介面**：
   - 支援 Cloth Config 與原版備用畫面，可於遊戲內隨時調整參數。
   - **動態條件聯動**：僅在需要時顯示對應滑桿，介面清爽直覺。
6. **藥水等級挑選偏好**：
   - `最高等級優先`（預設）：優先飲用等級最高的不祥之瓶（如 V 級）。
   - `最低等級優先`：優先飲用等級最低的不祥之瓶（如 I 級）。
   - `指定等級`：僅飲用指定等級（1~5 級）。
7. **動作完畢欄位自動切換**：
   - `切回原本手持欄位`（預設）：喝瓶或進食結束後，自動切回動作前的手持欄位。
   - `切換至固定武器欄位`：動作結束後自動切換至指定欄位（1~9 欄），無縫切回武器或工具！
8. **背包自動抽取**：快捷列缺少藥水時，可自動從主背包交換至快捷列飲用。

### 快捷鍵與設定
- **按鍵獨立分類**：於 Minecraft「選項」→「控制」→「按鍵綁定」中擁有專屬分類 **「自動喝不祥之瓶」**。
- **模組總開關快捷鍵**：選項 → 控制 → 按鍵綁定 → 自動喝不祥之瓶 → 開關（預設為 `R` 鍵）。
- **自動進食設定**：可於 Mod Menu 設定選單中自由開啟/關閉及調整飢餓閾值。
- 設定檔自動儲存於 `.minecraft/config/auto_drink_ominous_bottles.json`。

### 伺服器規範提醒 (Disclaimer)
> 本模組為純客戶端輔助工具，不修改伺服器通訊協定亦不含任何戰鬥外掛。但在具備嚴格客戶端巨集／自動化限制的多人伺服器遊玩前，請務必先確認該伺服器規則以確保合規。