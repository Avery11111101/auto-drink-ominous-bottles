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

    private float lastKnownHealth = 0.0f;
    private boolean hadActiveBar = false;

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
            raidHealthPercent = raidBar.getProgress();

            // 總波次計算：Easy 3波, Normal 5波, Hard 7波 (不祥突襲/高難度預設7波)
            Difficulty diff = client.level.getDifficulty();
            int wavesByDiff = switch (diff) {
                case PEACEFUL -> 0;
                case EASY -> 3;
                case NORMAL -> 5;
                case HARD -> 7;
            };
            this.totalWaves = Math.max(wavesByDiff, 7);

            // 若血量由低轉高且回滿，代表新一波次開始
            if (lastKnownHealth < 0.2f && raidHealthPercent >= 0.95f && hadActiveBar) {
                currentWave++;
                if (currentWave > totalWaves) {
                    currentWave = totalWaves;
                }
            } else if (currentWave == 0) {
                currentWave = 1;
            }

            lastKnownHealth = raidHealthPercent;
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
            // 沒有進行中的突襲 Bar
            if (hadActiveBar) {
                // 剛結束突襲波次
                inRaid = false;
                currentWave = 0;
                raidHealthPercent = 0.0f;
                lastKnownHealth = 0.0f;
                hadActiveBar = false;
                nearbyRaidersCount = 0;
            }
        }
    }

    public void onRaidVictory() {
        consecutiveRaidsCount++;
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
