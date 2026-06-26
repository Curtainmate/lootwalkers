package com.curtainmate.lootwalkers;

final class DailyStats {
    String dateKey;
    int steps;
    int goldEarned;
    int enemiesDefeated;
    int chestsOpened;
    int rewardMask;

    DailyStats(String dateKey) {
        this.dateKey = dateKey;
    }

    String toStorage() {
        return dateKey + ","
                + steps + ","
                + goldEarned + ","
                + enemiesDefeated + ","
                + chestsOpened + ","
                + rewardMask;
    }

    static DailyStats fromStorage(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] parts = value.split(",");
        if (parts.length < 6) {
            return null;
        }

        DailyStats stats = new DailyStats(parts[0]);
        try {
            stats.steps = Integer.parseInt(parts[1]);
            stats.goldEarned = Integer.parseInt(parts[2]);
            stats.enemiesDefeated = Integer.parseInt(parts[3]);
            stats.chestsOpened = Integer.parseInt(parts[4]);
            stats.rewardMask = Integer.parseInt(parts[5]);
        } catch (NumberFormatException ignored) {
            return null;
        }
        return stats;
    }
}


