# Auto-Drink Ominous Bottles (自動喝不祥之瓶)

[English](#english) | [繁體中文](#繁體中文)

---

<a name="english"></a>
## English

Client-side Fabric mod for Minecraft 26.1 / 26.2 / 26.3+ that automatically drinks an **Ominous Bottle** when a raid ends, supports rich in-game configuration via Mod Menu, configurable bottle level filtering, and weapon slot restoring.

### Features
1. **Raid Victory Trigger**: Watches the raid boss bar and triggers automatically when victory is achieved.
2. **Mod Menu & Cloth Config Support**: Open the in-game configuration screen directly from the Mods list.
3. **Bottle Selection Strategy**:
   - `Highest Level First` (Default): Drinks the highest level bottle in inventory/hotbar.
   - `Lowest Level First`: Drinks the lowest level bottle.
   - `Exact Level Only`: Drinks only a specific level (1 to 5).
4. **Post-Drink Hotbar Restoration**:
   - `Restore Previous Held Slot` (Default): Returns to whatever slot you held before drinking.
   - `Switch to Fixed Weapon Slot`: Switches to a designated hotbar slot (1~9) so you can immediately resume fighting.
5. **Inventory Pulling**: Option to pull bottles from main inventory if none are found in the hotbar.

### Keybind & Usage
- **Keybind**: Options → Controls → Key Binds → Auto-Drink Ominous Bottles (Default: `R`).
- Toggling displays an on-screen status message.
- Configuration is saved in `.minecraft/config/auto_drink_ominous_bottles.json`.

---

<a name="繁體中文"></a>
## 繁體中文

適用於 Minecraft 26.1 / 26.2 / 26.3+ 的 Fabric 純客戶端模組。在襲擊（Raid）結束取得勝利時，自動飲用背包或快捷列中的**不祥之瓶 (Ominous Bottle)**，並支援模組選單圖形化設定、藥水等級偏好挑選與喝完自動切換回武器快捷欄位。

### 主要功能
1. **襲擊勝利自動觸發**：即時監聽畫面頂部的襲擊 Boss 血條，當跳出「襲擊 - 勝利」時自動飲用不祥之瓶。
2. **模組選單 (Mod Menu) 設定介面**：於模組清單點擊「設定」齒輪即可進入圖形化設定選單（支援 Cloth Config，並內建原版備用畫面）。
3. **藥水等級挑選偏好**：
   - `最高等級優先`（預設）：優先飲用身上等級最高的不祥之瓶（如 V 級）。
   - `最低等級優先`：優先飲用等級最低的不祥之瓶（如 I 級）。
   - `只喝指定等級`：僅飲用指定等級（1~5 級）。
4. **喝完切換快捷欄位**：
   - `切回原本手持欄位`（預設）：喝完自動切換回觸發前的快捷欄位。
   - `切換至固定武器欄位`：喝完後自動切換至指定武器快捷欄位（1~9 欄），無縫切回武器繼續戰鬥！
5. **背包自動抽取**：快捷列沒有藥水時，可自動從主背包交換至快捷列飲用。

### 快捷鍵與設定
- **功能開關快捷鍵**：選項 → 控制 → 按鍵綁定 → 自動喝不祥之瓶開關（預設為 `R` 鍵）。
- 按下快捷鍵時，Actionbar 會即時提示啟用/停用狀態。
- 設定檔自動儲存於 `.minecraft/config/auto_drink_ominous_bottles.json`。