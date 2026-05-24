package com.example.stepcounterbase;

import android.content.Context;
import android.content.SharedPreferences;

import static com.example.stepcounterbase.GameRules.FIGHT_COMBAT;
import static com.example.stepcounterbase.GameRules.FIGHT_HUB;
import static com.example.stepcounterbase.GameRules.MODE_DUNGEON;
import static com.example.stepcounterbase.GameRules.MODE_NONE;

final class SaveStore {
    private static final String PREFS = "lootwalkers_prefs";
    private static final String DATE_KEY = "date";
    private static final String BASELINE_KEY = "baseline";
    private static final String TODAY_STEPS_KEY = "today_steps";
    private static final String DEBUG_STEP_OFFSET_KEY = "debug_step_offset";
    private static final String LAST_SENSOR_STEPS_KEY = "last_sensor_steps";
    private static final String TODAY_GOLD_EARNED_KEY = "today_gold_earned";
    private static final String TODAY_ENEMIES_DEFEATED_KEY = "today_enemies_defeated";
    private static final String TODAY_CHESTS_OPENED_KEY = "today_chests_opened";
    private static final String DAILY_REWARD_MASK_KEY = "daily_reward_mask";
    private static final String ACTIVITY_TAB_KEY = "activity_tab";
    private static final String DAILY_HISTORY_KEY = "daily_history";
    private static final String AUTO_EAT_UNLOCKED_KEY = "auto_eat_unlocked";
    private static final String FORGOTTEN_GRAVEYARD_UNLOCKED_KEY = "forgotten_graveyard_unlocked";
    private static final String MERCHANT_TAB_KEY = "merchant_tab";
    private static final String ACTIVE_KEY = "active_run";
    private static final String SHOW_DEV_TOOLS_KEY = "show_dev_tools";
    private static final String PHASE_KEY = "phase";
    private static final String ENCOUNTER_KEY = "encounter";
    private static final String TRAVEL_LEFT_KEY = "travel_left";
    private static final String ENEMY_HP_KEY = "enemy_hp";
    private static final String ATTACK_CHARGE_KEY = "attack_charge";
    private static final String ENEMY_ATTACK_CHARGE_KEY = "enemy_attack_charge";
    private static final String PLAYER_HP_KEY = "player_hp";
    private static final String LAST_GAME_STEPS_KEY = "last_game_steps";
    private static final String CHEST_READY_KEY = "chest_ready";
    private static final String LAST_REWARD_KEY = "last_reward";
    private static final String EVENT_LOG_KEY = "event_log";
    private static final String CHEST_OPENED_AT_KEY = "chest_opened_at";
    private static final String AUTO_CHEST_CHARGE_KEY = "auto_chest_charge";
    private static final String ACTIVITY_MODE_KEY = "activity_mode";
    private static final String MAIN_TAB_KEY = "main_tab";
    private static final String FIGHT_SCREEN_KEY = "fight_screen";
    private static final String SELECTED_AREA_KEY = "selected_area";
    private static final String SELECTED_AREA_ENEMY_KEY = "selected_area_enemy";
    private static final String GOLD_KEY = "gold";
    private static final String WEAPON_LEVEL_KEY = "weapon_level";
    private static final String ARMOR_LEVEL_KEY = "armor_level";
    private static final String BOOTS_LEVEL_KEY = "boots_level";
    private static final String CHARM_LEVEL_KEY = "charm_level";
    private static final String INVENTORY_KEY = "inventory";
    private static final String NEXT_ITEM_ID_KEY = "next_item_id";
    private static final String EQUIPPED_WEAPON_KEY = "equipped_weapon";
    private static final String EQUIPPED_ARMOR_KEY = "equipped_armor";
    private static final String EQUIPPED_BOOTS_KEY = "equipped_boots";
    private static final String EQUIPPED_CHARM_KEY = "equipped_charm";

    private final SharedPreferences prefs;

    SaveStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    GameState load(String todayKey, String defaultLastReward, String defaultEventLog) {
        GameState state = new GameState();
        state.todayKey = todayKey;

        String storedDate = prefs.getString(DATE_KEY, "");
        if (!todayKey.equals(storedDate)) {
            state.baseline = -1;
            state.todaySteps = 0;
            state.debugStepOffset = 0;
            state.lastSensorSteps = -1;
        } else {
            state.baseline = prefs.getInt(BASELINE_KEY, -1);
            state.todaySteps = prefs.getInt(TODAY_STEPS_KEY, 0);
            state.debugStepOffset = prefs.getInt(DEBUG_STEP_OFFSET_KEY, 0);
            state.lastSensorSteps = prefs.getInt(LAST_SENSOR_STEPS_KEY, -1);
        }

        state.activeRun = prefs.getBoolean(ACTIVE_KEY, false);
        state.chestReady = prefs.getBoolean(CHEST_READY_KEY, false);
        state.showDevTools = prefs.getBoolean(SHOW_DEV_TOOLS_KEY, false);
        state.activityTab = prefs.getInt(ACTIVITY_TAB_KEY, 0);
        state.autoEatUnlocked = prefs.getBoolean(AUTO_EAT_UNLOCKED_KEY, false);
        state.forgottenGraveyardUnlocked = prefs.getBoolean(FORGOTTEN_GRAVEYARD_UNLOCKED_KEY, false);
        state.merchantTab = prefs.getInt(MERCHANT_TAB_KEY, 0);
        state.phase = prefs.getInt(PHASE_KEY, 0);
        state.encounterIndex = prefs.getInt(ENCOUNTER_KEY, 0);
        state.travelLeft = prefs.getInt(TRAVEL_LEFT_KEY, 0);
        state.enemyHp = prefs.getInt(ENEMY_HP_KEY, 75);
        state.attackCharge = prefs.getInt(ATTACK_CHARGE_KEY, 0);
        state.enemyAttackCharge = prefs.getInt(ENEMY_ATTACK_CHARGE_KEY, 0);
        state.playerHp = prefs.getInt(PLAYER_HP_KEY, 100);
        state.lastGameSteps = prefs.getInt(LAST_GAME_STEPS_KEY, state.todaySteps);
        state.lastReward = prefs.getString(LAST_REWARD_KEY, defaultLastReward);
        state.eventLog = prefs.getString(EVENT_LOG_KEY, defaultEventLog);
        state.chestOpenedAt = prefs.getLong(CHEST_OPENED_AT_KEY, 0L);
        state.autoChestCharge = prefs.getInt(AUTO_CHEST_CHARGE_KEY, 0);
        state.activityMode = prefs.getInt(ACTIVITY_MODE_KEY, state.activeRun || state.chestReady ? MODE_DUNGEON : MODE_NONE);
        state.mainTab = prefs.getInt(MAIN_TAB_KEY, 0);
        state.fightScreen = prefs.getInt(FIGHT_SCREEN_KEY, state.activeRun || state.chestReady ? FIGHT_COMBAT : FIGHT_HUB);
        state.selectedArea = prefs.getInt(SELECTED_AREA_KEY, 1);
        state.selectedAreaEnemy = prefs.getInt(SELECTED_AREA_ENEMY_KEY, 0);
        state.gold = prefs.getInt(GOLD_KEY, 0);
        state.weaponLevel = prefs.getInt(WEAPON_LEVEL_KEY, 1);
        state.armorLevel = prefs.getInt(ARMOR_LEVEL_KEY, 1);
        state.bootsLevel = prefs.getInt(BOOTS_LEVEL_KEY, 1);
        state.charmLevel = prefs.getInt(CHARM_LEVEL_KEY, 1);
        state.nextItemId = prefs.getInt(NEXT_ITEM_ID_KEY, 5);
        state.equippedWeaponId = prefs.getInt(EQUIPPED_WEAPON_KEY, 1);
        state.equippedArmorId = prefs.getInt(EQUIPPED_ARMOR_KEY, 2);
        state.equippedBootsId = prefs.getInt(EQUIPPED_BOOTS_KEY, 3);
        state.equippedCharmId = prefs.getInt(EQUIPPED_CHARM_KEY, 4);
        parseDailyHistory(prefs.getString(DAILY_HISTORY_KEY, ""), state);
        DailyStats todayStats = findDailyStats(state, todayKey);
        if (todayStats != null) {
            state.todayGoldEarned = todayStats.goldEarned;
            state.todayEnemiesDefeated = todayStats.enemiesDefeated;
            state.todayChestsOpened = todayStats.chestsOpened;
            state.dailyRewardMask = todayStats.rewardMask;
        } else if (todayKey.equals(storedDate)) {
            state.todayGoldEarned = prefs.getInt(TODAY_GOLD_EARNED_KEY, 0);
            state.todayEnemiesDefeated = prefs.getInt(TODAY_ENEMIES_DEFEATED_KEY, 0);
            state.todayChestsOpened = prefs.getInt(TODAY_CHESTS_OPENED_KEY, 0);
            state.dailyRewardMask = prefs.getInt(DAILY_REWARD_MASK_KEY, 0);
        }
        parseInventory(prefs.getString(INVENTORY_KEY, ""), state);
        return state;
    }

    void save(GameState state) {
        prefs.edit()
                .putString(DATE_KEY, state.todayKey)
                .putInt(BASELINE_KEY, state.baseline)
                .putInt(TODAY_STEPS_KEY, state.todaySteps)
                .putInt(DEBUG_STEP_OFFSET_KEY, state.debugStepOffset)
                .putInt(LAST_SENSOR_STEPS_KEY, state.lastSensorSteps)
                .putInt(TODAY_GOLD_EARNED_KEY, state.todayGoldEarned)
                .putInt(TODAY_ENEMIES_DEFEATED_KEY, state.todayEnemiesDefeated)
                .putInt(TODAY_CHESTS_OPENED_KEY, state.todayChestsOpened)
                .putInt(DAILY_REWARD_MASK_KEY, state.dailyRewardMask)
                .putInt(ACTIVITY_TAB_KEY, state.activityTab)
                .putBoolean(AUTO_EAT_UNLOCKED_KEY, state.autoEatUnlocked)
                .putBoolean(FORGOTTEN_GRAVEYARD_UNLOCKED_KEY, state.forgottenGraveyardUnlocked)
                .putInt(MERCHANT_TAB_KEY, state.merchantTab)
                .putBoolean(ACTIVE_KEY, state.activeRun)
                .putBoolean(CHEST_READY_KEY, state.chestReady)
                .putBoolean(SHOW_DEV_TOOLS_KEY, state.showDevTools)
                .putInt(PHASE_KEY, state.phase)
                .putInt(ENCOUNTER_KEY, state.encounterIndex)
                .putInt(TRAVEL_LEFT_KEY, state.travelLeft)
                .putInt(ENEMY_HP_KEY, state.enemyHp)
                .putInt(ATTACK_CHARGE_KEY, state.attackCharge)
                .putInt(ENEMY_ATTACK_CHARGE_KEY, state.enemyAttackCharge)
                .putInt(PLAYER_HP_KEY, state.playerHp)
                .putInt(LAST_GAME_STEPS_KEY, state.lastGameSteps)
                .putString(LAST_REWARD_KEY, state.lastReward)
                .putString(EVENT_LOG_KEY, state.eventLog)
                .putLong(CHEST_OPENED_AT_KEY, state.chestOpenedAt)
                .putInt(AUTO_CHEST_CHARGE_KEY, state.autoChestCharge)
                .putInt(ACTIVITY_MODE_KEY, state.activityMode)
                .putInt(MAIN_TAB_KEY, state.mainTab)
                .putInt(FIGHT_SCREEN_KEY, state.fightScreen)
                .putInt(SELECTED_AREA_KEY, state.selectedArea)
                .putInt(SELECTED_AREA_ENEMY_KEY, state.selectedAreaEnemy)
                .putInt(GOLD_KEY, state.gold)
                .putInt(WEAPON_LEVEL_KEY, state.weaponLevel)
                .putInt(ARMOR_LEVEL_KEY, state.armorLevel)
                .putInt(BOOTS_LEVEL_KEY, state.bootsLevel)
                .putInt(CHARM_LEVEL_KEY, state.charmLevel)
                .putInt(NEXT_ITEM_ID_KEY, state.nextItemId)
                .putInt(EQUIPPED_WEAPON_KEY, state.equippedWeaponId)
                .putInt(EQUIPPED_ARMOR_KEY, state.equippedArmorId)
                .putInt(EQUIPPED_BOOTS_KEY, state.equippedBootsId)
                .putInt(EQUIPPED_CHARM_KEY, state.equippedCharmId)
                .putString(INVENTORY_KEY, serializeInventory(state))
                .putString(DAILY_HISTORY_KEY, serializeDailyHistory(state))
                .apply();
    }

    private void parseInventory(String value, GameState state) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String[] items = value.split(";");
        for (String itemValue : items) {
            Item item = Item.fromStorage(itemValue);
            if (item != null) {
                state.inventory.add(item);
            }
        }
    }

    private String serializeInventory(GameState state) {
        StringBuilder builder = new StringBuilder();
        for (Item item : state.inventory) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(item.toStorage());
        }
        return builder.toString();
    }

    private void parseDailyHistory(String value, GameState state) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String[] days = value.split(";");
        for (String dayValue : days) {
            DailyStats stats = DailyStats.fromStorage(dayValue);
            if (stats != null) {
                state.dailyHistory.add(stats);
            }
        }
    }

    private DailyStats findDailyStats(GameState state, String dateKey) {
        for (DailyStats stats : state.dailyHistory) {
            if (dateKey.equals(stats.dateKey)) {
                return stats;
            }
        }
        return null;
    }

    private String serializeDailyHistory(GameState state) {
        StringBuilder builder = new StringBuilder();
        for (DailyStats stats : state.dailyHistory) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(stats.toStorage());
        }
        return builder.toString();
    }
}
