package com.shumsic.autodrinkominousbottles.hud;

import com.shumsic.autodrinkominousbottles.RaidMessageDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class RaidStatusTracker {
    private static final RaidStatusTracker INSTANCE = new RaidStatusTracker();

    private boolean inRaid = false;
    private int currentWave = 0;
    private int totalWaves = 7;
    private float raidHealthPercent = 0.0f;
    private int nearbyRaidersCount = 0;
    private int consecutiveRaidsCount = 0;

    private float lowestHealthInCurrentWave = 1.0f;
    private boolean waveCleared = false;
    private boolean hadActiveBar = false;
    private int missingBarTicks = 0;

    public static RaidStatusTracker getInstance() {
        return INSTANCE;
    }

    private RaidStatusTracker() {}

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            inRaid = false;
            return;
        }

        LerpingBossEvent raidBar = findActiveRaidBar(client);

        if (raidBar != null) {
            inRaid = true;
            missingBarTicks = 0;
            raidHealthPercent = raidBar.getProgress();

            // 總波次計算：Easy 3波, Normal 5波, Hard 7波 (依世界難度與突襲進展自適應)
            Difficulty diff = client.level.getDifficulty();
            int wavesByDiff = switch (diff) {
                case PEACEFUL -> 0;
                case EASY -> 3;
                case NORMAL -> 5;
                case HARD -> 7;
            };
            int baseTotal = wavesByDiff > 0 ? wavesByDiff : 7;
            if (this.totalWaves < baseTotal) {
                this.totalWaves = baseTotal;
            }

            // 首次偵測到突襲：初始化波次為第 1 波
            if (currentWave == 0) {
                currentWave = 1;
                lowestHealthInCurrentWave = raidHealthPercent;
                waveCleared = false;
            }

            // 追蹤當前波次戰鬥中所經歷的最低血量
            if (raidHealthPercent < lowestHealthInCurrentWave) {
                lowestHealthInCurrentWave = raidHealthPercent;
            }

            // 當血量降到低谷 (<= 25%)，標記當前波次已被擊敗清空
            if (lowestHealthInCurrentWave <= 0.25f) {
                waveCleared = true;
            }

            // 若當前波次已清空，且 BossBar 在 15 秒間歇充能完畢 (>= 95%)，判定為下一波開始！
            if (waveCleared && raidHealthPercent >= 0.95f) {
                currentWave++;
                if (currentWave > totalWaves) {
                    totalWaves = currentWave;
                }
                waveCleared = false;
                lowestHealthInCurrentWave = raidHealthPercent;
            }

            hadActiveBar = true;

            // 統計以玩家為中心 64 格內的 Raider (突襲者) 數量
            try {
                AABB searchBox = client.player.getBoundingBox().inflate(64.0);
                List<Raider> raiders = client.level.getEntitiesOfClass(Raider.class, searchBox, Raider::isAlive);
                nearbyRaidersCount = raiders.size();
            } catch (Throwable ignored) {
                nearbyRaidersCount = 0;
            }
        } else {
            // 沒有進行中的突襲 Bar：給予 30 ticks (約 1.5 秒) 緩衝防抖，避免波次轉換短暫消失時被立即重置
            if (hadActiveBar) {
                missingBarTicks++;
                if (missingBarTicks >= 30) {
                    inRaid = false;
                    currentWave = 0;
                    totalWaves = 7;
                    raidHealthPercent = 0.0f;
                    lowestHealthInCurrentWave = 1.0f;
                    waveCleared = false;
                    hadActiveBar = false;
                    nearbyRaidersCount = 0;
                    missingBarTicks = 0;
                }
            }
        }
    }

    public void onRaidVictory() {
        consecutiveRaidsCount++;
        // 勝利後清理狀態
        inRaid = false;
        currentWave = 0;
        lowestHealthInCurrentWave = 1.0f;
        waveCleared = false;
        hadActiveBar = false;
        missingBarTicks = 0;
    }

    public void resetConsecutiveCount() {
        consecutiveRaidsCount = 0;
    }

    public boolean isInRaid() {
        return inRaid;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return totalWaves;
    }

    public float getRaidHealthPercent() {
        return raidHealthPercent;
    }

    public int getNearbyRaidersCount() {
        return nearbyRaidersCount;
    }

    public int getConsecutiveRaidsCount() {
        return consecutiveRaidsCount;
    }

    private LerpingBossEvent findActiveRaidBar(Minecraft client) {
        try {
            if (client.gui == null || client.gui.hud == null) return null;
            BossHealthOverlay hud = client.gui.hud.getBossOverlay();
            if (hud == null) return null;

            for (Field f : hud.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(hud);
                if (!(v instanceof Map<?, ?> m)) continue;

                for (Object o : m.values()) {
                    if (o instanceof LerpingBossEvent bar) {
                        if (RaidMessageDetector.isRaidActive(bar.getName())) {
                            return bar;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
