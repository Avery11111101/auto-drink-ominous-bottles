package com.shumsic.autodrinkominousbottles;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

final class RaidMessageDetector {
    private static final String RAID_VICTORY_TRANSLATION_KEY = "event.minecraft.raid.victory.full";
    private static final String RAID_ACTIVE_TRANSLATION_KEY = "event.minecraft.raid.full";

    private RaidMessageDetector() {
    }

    static boolean isRaidVictory(Component message) {
        if (message == null) return false;
        if (message.getContents() instanceof TranslatableContents translatable) {
            if (RAID_VICTORY_TRANSLATION_KEY.equals(translatable.getKey())) {
                return true;
            }
        }
        String str = message.getString().toLowerCase();
        return (str.contains("raid") && str.contains("victory"))
            || (str.contains("襲擊") && str.contains("勝利"))
            || (str.contains("袭击") && str.contains("胜利"));
    }

    static boolean isRaidActive(Component message) {
        if (message == null) return false;
        if (message.getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (RAID_ACTIVE_TRANSLATION_KEY.equals(key) || key.startsWith("event.minecraft.raid")) {
                if (!RAID_VICTORY_TRANSLATION_KEY.equals(key)) {
                    return true;
                }
            }
        }
        String str = message.getString().toLowerCase();
        if (isRaidVictory(message)) return false;
        return str.contains("raid") || str.contains("襲擊") || str.contains("袭击");
    }
}
