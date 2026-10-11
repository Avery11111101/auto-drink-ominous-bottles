# Auto-Drink Ominous Bottles (自動喝不祥之瓶)

[English](#english) | [繁體中文](#繁體中文)

---

<a name="english"></a>
## English

A lightweight, purely client-side Fabric utility mod for Minecraft 26.3+ (maintained by Avery). Designed to streamline raid farming and quality-of-life gameplay: automatically drinks an **Ominous Bottle** upon raid victory, provides smart **Auto-Eat** assistance, and supports full customization via Mod Menu and Cloth Config.

Fully compliant with Modrinth Content Policies (No combat hacks, no TriggerBot).

### Features
1. **Continuous Raid Maintaining & Auto-Drink**:
   - Actively maintains the raid state whenever the mod is enabled.
   - Automatically drinks an Ominous Bottle when toggled on or when a raid concludes, keeping raid farms constantly running.
2. **Spawn Point Failure Detection & Auto-Disable**:
   - Accurately tracks **Raid Omen** expiration. Waits up to 5 seconds for the server to initialize the raid boss bar.
   - If no raid spawns (e.g., no valid spawn points), automatically attempts to retry drinking.
   - If spawn fails consecutively (default 2 times), automatically disables the mod with high-visibility dual warnings (Chat + Action Bar) and an alert sound.
3. **Not-in-Village Safeguard**:
   - If **Bad Omen** fails to convert into Raid Omen within configurable seconds (default 10s), the mod recognizes that the player is not in a village and automatically turns off, preventing bottle depletion.
4. **Real-Time Raid Status HUD with Visual Drag-and-Drop Positioning**:
   - Displays real-time wave progress (e.g. `Wave: 3 / 7`), boss health percentage, nearby raiders within 64 blocks, and AFK cleared streak count.
   - **Interactive Drag-and-Drop Screen**: Customize the HUD position directly on screen via Mod Menu with screen boundary clamping.
   - **Customizable Visual Styles**: Switch between translucent dark background box or minimal text-only display.
   - **Auto-Hide Protection**: Automatically displays exclusively during active raids.
5. **Smart Auto-Eat (Quality-of-Life)**:
   - Automatically eats food when player hunger drops below a configurable threshold (Default: 14 / 7 drumsticks).
   - **Smart Filtering**: Automatically ignores harmful items as well as precious Golden Apples and teleporting Chorus Fruits.
   - **Inventory Swapping**: Pulls suitable food from main inventory if hotbar runs out.
   - **Priority Protection**: Raid bottle drinking always takes priority over auto-eating.
6. **Real-time Action Feedback & Dual-Column Config**:
   - Displays real-time on-screen notifications when drinking, eating, or experiencing timeouts.
   - Mod Menu & Cloth Config integration with dynamic sliders and a clean two-column fallback GUI.
7. **Bottle Selection Strategy**:
   - `Highest Level First` (Default): Drinks the highest level bottle in inventory/hotbar.
   - `Lowest Level First`: Drinks the lowest level bottle.
   - `Exact Level Only`: Drinks only a specific level (1 to 5).
8. **Post-Drink Hotbar Restoration**:
   - `Restore Previous Held Slot` (Default): Seamlessly returns to whatever slot was held before drinking/eating.
   - `Switch to Fixed Weapon Slot`: Switches to a designated hotbar slot (1~9) so you can immediately resume combat or tool usage.
9. **Main Inventory Pulling**: Option to pull bottles from main inventory if none are found in the hotbar.

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
1. **常態維持突襲與自動飲用**：
   - 模組啟用期間主動維持突襲狀態。剛按下熱鍵開啟或前一輪突襲勝利結束時，只要偵測到「目前無突襲狀態」且無凶兆，立即自動飲用不祥之瓶開啟突襲！
2. **生怪失敗偵測與自動停用防耗**：
   - 精確追蹤「突襲凶兆 (Raid Omen)」倒數結束，給予 5 秒等待伺服器生成突襲血條。
   - 若 5 秒內未出現突襲（代表找不到生成點），自動重新嘗試飲用；若連續 2 次找不到生成點，**自動關閉模組**並於聊天欄與 ActionBar 同時發送醒目紅字警告與警示音效！
3. **不在村莊防呆保護機制**：
   - 喝下藥水後若超過 10 秒（可自訂）普通不祥之兆仍未轉化為突襲凶兆，判定玩家不在村莊範圍內，自動停用模組並發送雙重提示與音效，防止無謂耗損藥水。
4. **突襲即時狀態 HUD 與視覺化自訂拖曳介面**：
   - 即時顯示目前波次進度（如 `第 3 / 7 波`）、血量百分比、玩家周圍 64 格內存活突襲怪數量、以及掛機期間**連續完成場次**。
   - **視覺化可拖曳設定畫面**：於 Mod Menu 設定選單點擊「調整 HUD 顯示位置...」，可使用滑鼠左鍵自由拖拉 HUD 至螢幕任意位置，內建邊界防呆防出界限制。
   - **風格自由切換**：支援半透明質感背景框與極簡純文字模式。
   - **顯示時機優化**：僅於處於突襲期間才顯示，平時自動隱藏不干擾原版畫面。
5. **智能自動進食 (Auto-Eat)**：
   - 當飢餓值低於指定閾值時（預設 14，即 7 格雞腿）自動進食。
   - **智能避雷過濾**：自動避開有害負面食物（腐肉、河豚、毒馬鈴薯、蜘蛛眼），並自動排除高價值金蘋果與具隨機傳送風險的紫頌果。
   - **跨背包調取**：快捷列缺少食物時，可自動從主背包交換至快捷列食用。
   - **優先權防護**：襲擊喝瓶動作優先級高於自動進食，避免衝突。
6. **模組選單 (Mod Menu) 雙欄動態設定介面**：
   - 支援 Cloth Config 與升級版原版雙欄風格備用畫面，可於遊戲內隨時調整各項超時與上限參數。
7. **藥水等級挑選偏好**：
   - `最高等級優先`（預設）：優先飲用等級最高的不祥之瓶（如 V 級）。
   - `最低等級優先`：優先飲用等級最低的不祥之瓶（如 I 級）。
   - `指定等級`：僅飲用指定等級（1~5 級）。
8. **動作完畢欄位自動切換**：
   - `切回原本手持欄位`（預設）：喝瓶或進食結束後，自動切回動作前的手持欄位。
   - `切換至固定武器欄位`：動作結束後自動切換至指定欄位（1~9 欄），無縫切回武器或工具！
9. **背包自動抽取**：快捷列缺少藥水時，可自動從主背包交換至快捷列飲用。

### 快捷鍵與設定
- **按鍵獨立分類**：於 Minecraft「選項」→「控制」→「按鍵綁定」中擁有專屬分類 **「自動喝不祥之瓶」**。
- **模組總開關快捷鍵**：選項 → 控制 → 按鍵綁定 → 自動喝不祥之瓶 → 開關（預設為 `R` 鍵）。
- **自動進食設定**：可於 Mod Menu 設定選單中自由開啟/關閉及調整飢餓閾值。
- 設定檔自動儲存於 `.minecraft/config/auto_drink_ominous_bottles.json`。

### 伺服器規範提醒 (Disclaimer)
> 本模組為純客戶端輔助工具，不修改伺服器通訊協定亦不含任何戰鬥外掛。但在具備嚴格客戶端巨集／自動化限制的多人伺服器遊玩前，請務必先確認該伺服器規則以確保合規。