# Auto-Drink Ominous Bottles (自動喝不祥之瓶)

[English](#english) | [繁體中文](#繁體中文)

---

<a name="english"></a>
## English

Client-side Fabric mod for Minecraft 26.2 / 26.3+ (by Avery) that automatically drinks an **Ominous Bottle** when a raid ends, provides a triggerbot/auto-attack system, supports rich in-game configuration via Mod Menu with dynamic field visibility, configurable bottle level filtering, and weapon slot restoring.

### Features
1. **Raid Victory Trigger**: Watches the raid boss bar and triggers automatically when victory is achieved.
2. **Auto Attack (TriggerBot)**:
   - Automatically attacks targets under your crosshair (living entities, hostile mobs, players, etc.).
   - Supports 3 modes: `Full Cooldown (1.0 Attack Strength)`, `Custom Tick Interval`, and `Once Per Aim`.
   - Independent keybind to toggle on/off on the fly.
3. **Mod Menu & Cloth Config Support (with Dynamic UI)**:
   - Configurable directly from the Mod Menu.
   - Dynamic conditional fields: Only displays `Exact Level` when in exact mode, `Fixed Weapon Slot` when in fixed slot mode, and `Attack Interval` when in custom interval mode.
   - Built-in vanilla fallback config screen if Cloth Config is not present.
4. **Bottle Selection Strategy**:
   - `Highest Level First` (Default): Drinks the highest level bottle in inventory/hotbar.
   - `Lowest Level First`: Drinks the lowest level bottle.
   - `Exact Level Only`: Drinks only a specific level (1 to 5).
5. **Post-Drink Hotbar Restoration**:
   - `Restore Previous Held Slot` (Default): Returns to whatever slot you held before drinking.
   - `Switch to Fixed Weapon Slot`: Switches to a designated hotbar slot (1~9) so you can immediately resume fighting.
6. **Inventory Pulling**: Option to pull bottles from main inventory if none are found in the hotbar.

### Keybinds & Usage
- **Toggle Mod Keybind**: Options → Controls → Key Binds → Toggle Mod (Default: `R`).
- **Toggle Auto Attack Keybind**: Options → Controls → Key Binds → Toggle Auto Attack (Default: `G`).
- Configuration is saved in `.minecraft/config/auto_drink_ominous_bottles.json`.

---

<a name="繁體中文"></a>
## 繁體中文

適用於 Minecraft 26.2 / 26.3+ 的 Fabric 純客戶端模組（由 Avery 維護製作）。在襲擊（Raid）結束取得勝利時，自動飲用背包或快捷列中的**不祥之瓶 (Ominous Bottle)**；並提供瞄準時自動攻擊功能、模組選單動態設定介面、藥水等級偏好挑選與喝完自動切換回武器快捷欄位。

### 主要功能
1. **襲擊勝利自動觸發**：即時監聽畫面頂部的襲擊 Boss 血條，當跳出「襲擊 - 勝利」時自動飲用不祥之瓶。
2. **準心自動攻擊 (Auto Attack)**：
   - 準心瞄準可攻擊實體時自動揮刀，不限制手持物品。
   - 支援 3 種攻擊模式：
     - `滿冷卻攻擊 (Full Cooldown)`：蓄力達 1.0 最大傷害與橫掃時揮擊（預設）。
     - `自訂攻擊間隔 (Custom Interval)`：每隔指定 Tick 數固定頻率攻擊。
     - `瞄準時僅攻擊一次 (Once on Aim)`：瞄準目標時僅攻擊一次，移開重瞄或切換目標才再次揮擊。
   - 具備獨立熱鍵可於戰鬥中隨時切換開關，並於 ActionBar 即時提示。
3. **模組選單 (Mod Menu) 動態設定介面**：
   - 支援 Cloth Config 與原版備用畫面，於模組清單點擊「設定」齒輪即可進入。
   - **動態欄位聯動**：僅在「指定藥水等級」模式時才顯示等級滑桿、僅在「固定武器欄位」時才顯示欄位滑桿、僅在「自訂攻擊間隔」時才顯示 Tick 數滑桿，介面清爽直覺。
4. **藥水等級挑選偏好**：
   - `最高等級優先`（預設）：優先飲用身上等級最高的不祥之瓶（如 V 級）。
   - `最低等級優先`：優先飲用等級最低的不祥之瓶（如 I 級）。
   - `指定等級`：僅飲用指定等級（1~5 級）。
5. **喝完切換快捷欄位**：
   - `切回原本手持欄位`（預設）：喝完自動切換回觸發前的快捷欄位。
   - `切換至固定武器欄位`：喝完後自動切換至指定武器快捷欄位（1~9 欄），無縫切回武器繼續戰鬥！
6. **背包自動抽取**：快捷列沒有藥水時，可自動從主背包交換至快捷列飲用。

### 快捷鍵與設定
- **模組總開關快捷鍵**：選項 → 控制 → 按鍵綁定 → 自動喝不祥之瓶開關（預設為 `R` 鍵）。
- **自動攻擊開關快捷鍵**：選項 → 控制 → 按鍵綁定 → 自動攻擊開關（預設為 `G` 鍵）。
- 按下快捷鍵時，Actionbar 會即時提示啟用/停用狀態。
- 設定檔自動儲存於 `.minecraft/config/auto_drink_ominous_bottles.json`。