package com.example.stepcounterbase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int REQUEST_ACTIVITY_RECOGNITION = 40;
    private static final String PREFS = "lootwalkers_prefs";
    private static final String DATE_KEY = "date";
    private static final String BASELINE_KEY = "baseline";
    private static final String TODAY_STEPS_KEY = "today_steps";
    private static final String DEBUG_STEP_OFFSET_KEY = "debug_step_offset";
    private static final String ACTIVE_KEY = "active_run";
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

    private static final String SLOT_WEAPON = "weapon";
    private static final String SLOT_ARMOR = "armor";
    private static final String SLOT_BOOTS = "boots";
    private static final String SLOT_CHARM = "charm";

    private static final int PHASE_TRAVEL = 0;
    private static final int PHASE_COMBAT = 1;
    private static final int PHASE_COMPLETE = 2;
    private static final int PHASE_EXHAUSTED = 3;
    private static final int ENCOUNTERS = 3;
    private static final int TAB_FIGHT = 0;
    private static final int TAB_SKILLS = 1;
    private static final int TAB_BAG = 2;
    private static final int TAB_TOWN = 3;
    private static final int FIGHT_HUB = 0;
    private static final int FIGHT_AREAS = 1;
    private static final int FIGHT_AREA_ENEMY = 2;
    private static final int FIGHT_DUNGEONS = 3;
    private static final int FIGHT_DUNGEON_DETAIL = 4;
    private static final int FIGHT_COMBAT = 5;
    private static final int MODE_NONE = 0;
    private static final int MODE_AREA = 1;
    private static final int MODE_DUNGEON = 2;

    private final Random random = new Random();

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SharedPreferences prefs;

    private TextView dateView;
    private TextView todayStepsView;
    private TextView dungeonTitleView;
    private TextView dungeonStatusView;
    private TextView progressDetailView;
    private TextView enemyView;
    private TextView nextAttackView;
    private TextView actionMeterView;
    private TextView lootView;
    private TextView eventLogView;
    private TextView gearView;
    private LinearLayout inventoryListView;
    private LinearLayout dungeonPanel;
    private LinearLayout inventoryPanel;
    private LinearLayout gearPanel;
    private LinearLayout logPanel;
    private LinearLayout combatHeaderPanel;
    private LinearLayout scenePanel;
    private LinearLayout actionPanel;
    private LinearLayout fightPanel;
    private LinearLayout skillsPanel;
    private LinearLayout bagPanel;
    private LinearLayout townPanel;
    private LinearLayout fightHubPanel;
    private LinearLayout areasPanel;
    private LinearLayout areaEnemyPanel;
    private LinearLayout dungeonsPanel;
    private LinearLayout dungeonDetailPanel;
    private LinearLayout combatInfoPanel;
    private Button dungeonTabButton;
    private Button inventoryTabButton;
    private Button gearTabButton;
    private Button logTabButton;
    private Button fightNavButton;
    private Button skillsNavButton;
    private Button bagNavButton;
    private Button townNavButton;
    private Button retreatButton;
    private TextView systemView;
    private SceneView sceneView;
    private ProgressBar dungeonProgressBar;
    private ProgressBar enemyProgressBar;
    private ProgressBar attackProgressBar;
    private Button primaryButton;
    private Button chestButton;
    private Button permissionButton;
    private Button resetButton;
    private Button testStepsButton;
    private Button testBigStepsButton;

    private int baseline = -1;
    private int todaySteps = 0;
    private int debugStepOffset = 0;
    private String todayKey;

    private boolean activeRun = false;
    private boolean chestReady = false;
    private int phase = PHASE_TRAVEL;
    private int encounterIndex = 0;
    private int travelLeft = 0;
    private int enemyHp = 0;
    private int attackCharge = 0;
    private int enemyAttackCharge = 0;
    private int playerHp = 100;
    private int lastGameSteps = 0;
    private int gold = 0;
    private int weaponLevel = 1;
    private int armorLevel = 1;
    private int bootsLevel = 1;
    private int charmLevel = 1;
    private int nextItemId = 5;
    private int equippedWeaponId = 1;
    private int equippedArmorId = 2;
    private int equippedBootsId = 3;
    private int equippedCharmId = 4;
    private int activeTab = 0;
    private int mainTab = TAB_FIGHT;
    private int fightScreen = FIGHT_HUB;
    private int activityMode = MODE_NONE;
    private int autoChestCharge = 0;
    private long chestOpenedAt = 0L;
    private String lastReward = "No loot yet. Clear Goblin Cave I to open your first chest.";
    private String eventLog = "Ready at the cave mouth.";
    private final ArrayList<Item> inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        todayKey = currentDateKey();

        loadState();
        setContentView(buildLayout());
        updateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startListeningIfReady();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }

        ensureCurrentDay();

        int totalSinceBoot = Math.round(event.values[0]);
        if (baseline < 0 || totalSinceBoot < baseline) {
            baseline = totalSinceBoot;
        }

        int newTodaySteps = Math.max(0, totalSinceBoot - baseline + debugStepOffset);
        int delta = Math.max(0, newTodaySteps - todaySteps);
        todaySteps = newTodaySteps;

        if ((activeRun || chestReady) && delta > 0) {
            processGameSteps(delta);
        }

        saveState();
        updateViews();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            updateViews();
            startListeningIfReady();
        }
    }

    private View buildLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(16, 14, 11));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(8), dp(10), dp(8), dp(10));
        scrollView.addView(root);

        LinearLayout topHud = darkCard();
        topHud.setOrientation(LinearLayout.HORIZONTAL);
        topHud.setGravity(Gravity.CENTER_VERTICAL);
        TextView portrait = text("Arin\nLv. 1", 22, Color.rgb(245, 224, 177), true);
        topHud.addView(portrait, weightedWidth(0.9f));
        dateView = text("Lootwalkers", 14, Color.rgb(192, 157, 100), false);
        topHud.addView(dateView, weightedWidth(0.9f));
        todayStepsView = text("", 24, Color.rgb(245, 224, 177), true);
        todayStepsView.setGravity(Gravity.RIGHT);
        topHud.addView(todayStepsView, weightedWidth(1.2f));
        root.addView(topHud);

        fightPanel = new LinearLayout(this);
        fightPanel.setOrientation(LinearLayout.VERTICAL);
        root.addView(fightPanel);

        fightHubPanel = darkCard();
        TextView fightTitle = text("FIGHT", 26, Color.rgb(245, 224, 177), true);
        fightTitle.setGravity(Gravity.CENTER);
        fightHubPanel.addView(fightTitle);
        TextView fightPrompt = text("What do you want to do today?", 15, Color.rgb(226, 205, 163), false);
        fightPrompt.setGravity(Gravity.CENTER);
        fightPrompt.setPadding(0, dp(6), 0, dp(12));
        fightHubPanel.addView(fightPrompt);
        fightHubPanel.addView(adventureCard(R.drawable.card_areas_deep_forest, "AREAS", "Farm enemies for specific drops.", v -> showFightScreen(FIGHT_AREAS)));
        fightHubPanel.addView(adventureCard(R.drawable.card_dungeon_goblin_cave, "DUNGEONS", "Clear encounters, defeat bosses, claim rewards.", v -> showFightScreen(FIGHT_DUNGEONS)));
        fightPanel.addView(fightHubPanel);

        areasPanel = darkCard();
        areasPanel.addView(sectionTitle("AREAS"));
        areasPanel.addView(adventureCard(R.drawable.card_areas_deep_forest, "Deep Forest", "A beginner forest path. Farm Cave Goblins for gold and early gear.", v -> showFightScreen(FIGHT_AREA_ENEMY)));
        areasPanel.addView(backButton());
        fightPanel.addView(areasPanel);

        areaEnemyPanel = darkCard();
        areaEnemyPanel.addView(sectionTitle("Deep Forest"));
        areaEnemyPanel.addView(adventureCard(R.drawable.portrait_cave_goblin, "Cave Goblin", "HP 75 | Max Hit 10 | Attack 115 steps", null));
        areaEnemyPanel.addView(detailRow("Possible drops", "Gold, sword, armor, boots, charm"));
        Button startArea = actionButton("Farm Cave Goblin", true);
        startArea.setOnClickListener(v -> startAreaFarming());
        areaEnemyPanel.addView(startArea, buttonLayoutParams());
        areaEnemyPanel.addView(backButton());
        fightPanel.addView(areaEnemyPanel);

        dungeonsPanel = darkCard();
        dungeonsPanel.addView(sectionTitle("DUNGEONS"));
        dungeonsPanel.addView(adventureCard(R.drawable.card_dungeon_goblin_cave, "Goblin Cave I", "3 encounters, Goblin Chief boss, common and uncommon gear.", v -> showFightScreen(FIGHT_DUNGEON_DETAIL)));
        dungeonsPanel.addView(backButton());
        fightPanel.addView(dungeonsPanel);

        dungeonDetailPanel = darkCard();
        dungeonDetailPanel.addView(sectionTitle("Goblin Cave I"));
        dungeonDetailPanel.addView(adventureCard(R.drawable.portrait_goblin_chief, "Goblin Chief", "Clear 3 encounters, defeat the boss, then walk 10 steps to open the chest.", null));
        dungeonDetailPanel.addView(detailRow("Drops", "Common and uncommon gear"));
        dungeonDetailPanel.addView(detailRow("Estimated clear", estimatedClearSteps() + " steps with current gear"));
        Button startDungeon = actionButton("Start Dungeon", true);
        startDungeon.setOnClickListener(v -> startDungeonRun());
        dungeonDetailPanel.addView(startDungeon, buttonLayoutParams());
        dungeonDetailPanel.addView(backButton());
        fightPanel.addView(dungeonDetailPanel);

        combatHeaderPanel = darkCard();
        dungeonTitleView = text("", 22, Color.rgb(245, 224, 177), true);
        combatHeaderPanel.addView(dungeonTitleView);
        dungeonStatusView = text("", 14, Color.rgb(192, 157, 100), false);
        dungeonStatusView.setPadding(0, dp(4), 0, dp(8));
        combatHeaderPanel.addView(dungeonStatusView);
        dungeonProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        dungeonProgressBar.setMax(1000);
        combatHeaderPanel.addView(dungeonProgressBar, progressLayoutParams());
        progressDetailView = text("", 14, Color.rgb(245, 224, 177), false);
        progressDetailView.setPadding(0, dp(6), 0, 0);
        combatHeaderPanel.addView(progressDetailView);
        fightPanel.addView(combatHeaderPanel);

        scenePanel = darkCard();
        scenePanel.setPadding(dp(4), dp(4), dp(4), dp(4));
        sceneView = new SceneView(this);
        scenePanel.addView(sceneView, sceneLayoutParams());
        fightPanel.addView(scenePanel);

        actionPanel = darkCard();
        TextView actionTitle = text("WALK TO ACT", 18, Color.rgb(245, 224, 177), true);
        actionTitle.setGravity(Gravity.CENTER);
        actionPanel.addView(actionTitle);
        actionMeterView = text("", 22, Color.rgb(139, 229, 87), true);
        actionMeterView.setGravity(Gravity.CENTER);
        actionMeterView.setPadding(0, dp(2), 0, dp(8));
        actionPanel.addView(actionMeterView);

        LinearLayout meterRow = new LinearLayout(this);
        meterRow.setOrientation(LinearLayout.HORIZONTAL);
        attackProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        attackProgressBar.setMax(1000);
        nextAttackView = text("", 14, Color.rgb(192, 157, 100), false);
        meterRow.addView(meterCard("Your Attack", "ATK", attackProgressBar, nextAttackView), weightedWidth(1.0f));
        enemyProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        enemyProgressBar.setMax(1000);
        enemyView = text("", 15, Color.rgb(245, 224, 177), true);
        meterRow.addView(meterCard("Enemy Attack", "FOE", enemyProgressBar, enemyView), weightedWidth(1.0f));
        actionPanel.addView(meterRow);
        retreatButton = actionButton("Retreat", false);
        retreatButton.setOnClickListener(v -> stopActivity());
        actionPanel.addView(retreatButton, buttonLayoutParams());
        fightPanel.addView(actionPanel);

        combatInfoPanel = darkCard();
        lootView = text("", 15, Color.rgb(226, 205, 163), false);
        combatInfoPanel.addView(lootView);
        testStepsButton = actionButton("+100 test steps", false);
        testStepsButton.setOnClickListener(v -> addTestSteps(100));
        combatInfoPanel.addView(testStepsButton, buttonLayoutParams());
        testBigStepsButton = actionButton("+1000 test steps", false);
        testBigStepsButton.setOnClickListener(v -> addTestSteps(1000));
        combatInfoPanel.addView(testBigStepsButton, buttonLayoutParams());
        fightPanel.addView(combatInfoPanel);

        skillsPanel = darkCard();
        skillsPanel.addView(text("SKILLS", 26, Color.rgb(245, 224, 177), true));
        addLockedRow(skillsPanel, "Woodcutting");
        addLockedRow(skillsPanel, "Mining");
        addLockedRow(skillsPanel, "Fishing");
        addLockedRow(skillsPanel, "Crafting");
        addLockedRow(skillsPanel, "Cooking");
        root.addView(skillsPanel);

        bagPanel = darkCard();
        bagPanel.addView(text("BAG", 26, Color.rgb(245, 224, 177), true));
        gearView = text("", 15, Color.rgb(226, 205, 163), false);
        gearView.setPadding(0, dp(8), 0, dp(8));
        bagPanel.addView(gearView);
        inventoryListView = new LinearLayout(this);
        inventoryListView.setOrientation(LinearLayout.VERTICAL);
        bagPanel.addView(inventoryListView);
        root.addView(bagPanel);

        townPanel = darkCard();
        townPanel.addView(text("TOWN", 26, Color.rgb(245, 224, 177), true));
        addLockedRow(townPanel, "Merchant");
        addLockedRow(townPanel, "Bank");
        addLockedRow(townPanel, "Trainer");
        addLockedRow(townPanel, "Activity");
        root.addView(townPanel);

        permissionButton = actionButton("Allow step tracking", true);
        permissionButton.setOnClickListener(v -> requestStepPermission());
        root.addView(permissionButton, buttonLayoutParams());

        resetButton = actionButton("Reset prototype", false);
        resetButton.setOnClickListener(v -> resetPrototype());
        root.addView(resetButton, buttonLayoutParams());

        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        fightNavButton = mainNavButton("Fight", TAB_FIGHT, R.drawable.fight_icon);
        skillsNavButton = mainNavButton("Skills", TAB_SKILLS, R.drawable.skills_icon);
        bagNavButton = mainNavButton("Bag", TAB_BAG, R.drawable.bag_icon);
        townNavButton = mainNavButton("Town", TAB_TOWN, R.drawable.town_icon);
        navRow.addView(fightNavButton, weightedWidth(1.0f));
        navRow.addView(skillsNavButton, weightedWidth(1.0f));
        navRow.addView(bagNavButton, weightedWidth(1.0f));
        navRow.addView(townNavButton, weightedWidth(1.0f));
        root.addView(navRow);

        eventLogView = text("", 14, Color.rgb(226, 205, 163), false);
        systemView = text("", 14, Color.rgb(192, 157, 100), false);
        systemView.setGravity(Gravity.CENTER);
        systemView.setPadding(0, dp(12), 0, 0);
        root.addView(systemView, fullWidthWrapContent());

        return scrollView;
    }

    private void startListeningIfReady() {
        if (sensorManager == null || stepCounterSensor == null || !hasStepPermission()) {
            updateViews();
            return;
        }

        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void startOrContinueRun() {
        startDungeonRun();
    }

    private void startAreaFarming() {
        if (!hasStepPermission()) {
            requestStepPermission();
            return;
        }

        activityMode = MODE_AREA;
        activeRun = true;
        chestReady = false;
        phase = PHASE_COMBAT;
        encounterIndex = 0;
        enemyHp = enemyMaxHp(false);
        attackCharge = 0;
        enemyAttackCharge = 0;
        autoChestCharge = 0;
        playerHp = maxPlayerHp();
        lastGameSteps = todaySteps;
        fightScreen = FIGHT_COMBAT;
        lastReward = "Deep Forest farming started. Cave Goblins will keep appearing until you retreat.";
        addEvent("Started farming Cave Goblins in Deep Forest.");
        saveState();
        updateViews();
        startListeningIfReady();
    }

    private void startDungeonRun() {
        if (!hasStepPermission()) {
            requestStepPermission();
            return;
        }

        activityMode = MODE_DUNGEON;
        activeRun = true;
        chestReady = false;
        phase = PHASE_COMBAT;
        encounterIndex = 0;
        enemyHp = enemyMaxHp(false);
        attackCharge = 0;
        enemyAttackCharge = 0;
        autoChestCharge = 0;
        playerHp = maxPlayerHp();
        lastGameSteps = todaySteps;
        fightScreen = FIGHT_COMBAT;
        lastReward = "Goblin Cave I started. Steps now power combat.";
        addEvent("Entered Goblin Cave I. A Cave Goblin appears.");
        saveState();
        updateViews();
        startListeningIfReady();
    }

    private void processGameSteps(int steps) {
        lastGameSteps = todaySteps;
        if (chestReady && activityMode == MODE_DUNGEON) {
            autoChestCharge += steps;
            if (autoChestCharge >= 10) {
                openChest();
            }
            return;
        }

        if (!activeRun || phase == PHASE_COMPLETE) {
            return;
        }

        if (phase == PHASE_EXHAUSTED) {
            recoverFromExhaustion(steps);
            return;
        }

        attackCharge += steps;
        enemyAttackCharge += steps;

        while (activeRun && phase == PHASE_COMBAT
                && (attackCharge >= attackInterval() || enemyAttackCharge >= enemyAttackInterval())) {
            if (attackCharge >= attackInterval()) {
                attackCharge -= attackInterval();
                int damage = playerHitDamage();
                enemyHp -= damage;
                addEvent(equippedWeapon().name + " hits for " + damage + ".");
                if (enemyHp <= 0) {
                    finishEnemy();
                    continue;
                }
            }

            if (enemyAttackCharge >= enemyAttackInterval()) {
                enemyAttackCharge -= enemyAttackInterval();
                resolveEnemyAttack();
            }
        }
    }

    private void resolveEnemyAttack() {
        if (random.nextInt(100) < dodgeChance()) {
            addEvent(enemyName() + " attacks, but you dodge.");
            return;
        }

        int rawDamage = 1 + random.nextInt(enemyMaxHit());
        int reducedDamage = Math.max(1, rawDamage * (100 - damageReductionPercent()) / 100);
        playerHp = Math.max(0, playerHp - reducedDamage);
        addEvent(enemyName() + " hits for " + reducedDamage + ".");
        if (playerHp <= 0) {
            phase = PHASE_EXHAUSTED;
            attackCharge = 0;
            enemyAttackCharge = 0;
            addEvent("Arin is Exhausted. Walk to recover.");
        }
    }

    private void recoverFromExhaustion(int steps) {
        attackCharge += steps;
        while (attackCharge >= recoveryStepCost() && phase == PHASE_EXHAUSTED) {
            attackCharge -= recoveryStepCost();
            playerHp = Math.min(maxPlayerHp(), playerHp + recoveryAmount());
            addEvent("Recovered " + recoveryAmount() + " HP.");
            if (playerHp >= resumeHp()) {
                phase = PHASE_COMBAT;
                enemyAttackCharge = 0;
                addEvent("Recovered enough to keep fighting.");
            }
        }
    }

    private void finishEnemy() {
        if (activityMode == MODE_AREA) {
            int goldReward = 2 + random.nextInt(4) + charmLevel;
            gold += goldReward;
            if (random.nextInt(100) < 12) {
                String dropSlot = randomAreaDropSlot();
                Item foundItem = new Item(nextItemId++, dropSlot, areaDropName(dropSlot), "Common", lootLevelForSlot(dropSlot));
                inventory.add(foundItem);
                lastReward = "Cave Goblin dropped " + foundItem.rarity + " " + foundItem.name
                        + "\n" + itemStatLine(foundItem)
                        + "\nGold gained: " + goldReward;
                addEvent("Cave Goblin defeated. Found " + foundItem.name + ".");
            } else {
                lastReward = "Cave Goblin defeated.\nGold gained: " + goldReward + "\nNo item drop this time.";
                addEvent("Cave Goblin defeated. Another appears.");
            }
            phase = PHASE_COMBAT;
            encounterIndex = 0;
            enemyHp = enemyMaxHp(false);
            attackCharge = 0;
            enemyAttackCharge = 0;
            return;
        }

        if (isBossFight()) {
            activeRun = false;
            chestReady = true;
            phase = PHASE_COMPLETE;
            autoChestCharge = 0;
            lastReward = "The Goblin Chief is defeated. The chest opens automatically after 10 more steps.";
            addEvent("Goblin Chief defeated. Chest found.");
            return;
        }

        encounterIndex += 1;
        phase = PHASE_COMBAT;
        enemyHp = enemyMaxHp(isBossFight());
        attackCharge = 0;
        enemyAttackCharge = 0;
        addEvent(isBossFight() ? "The Goblin Chief enters the fight." : "Cave Goblin defeated. Another appears.");
    }

    private void openChest() {
        if (!chestReady) {
            return;
        }

        int roll = random.nextInt(100);
        int oldEstimate = estimatedClearSteps();
        Item foundItem;
        if (roll < 30) {
            foundItem = new Item(nextItemId++, SLOT_WEAPON, "Ironwood Sword", "Uncommon", lootLevelForSlot(SLOT_WEAPON));
        } else if (roll < 55) {
            foundItem = new Item(nextItemId++, SLOT_BOOTS, "Trail Boots", "Uncommon", lootLevelForSlot(SLOT_BOOTS));
        } else if (roll < 75) {
            foundItem = new Item(nextItemId++, SLOT_ARMOR, "Padded Tunic", "Common", lootLevelForSlot(SLOT_ARMOR));
        } else if (roll < 90) {
            foundItem = new Item(nextItemId++, SLOT_CHARM, "Lucky Pebble", "Uncommon", lootLevelForSlot(SLOT_CHARM));
        } else {
            foundItem = new Item(nextItemId++, SLOT_WEAPON, "Sunlit Blade", "Rare", lootLevelForSlot(SLOT_WEAPON) + 1);
        }

        inventory.add(foundItem);
        int goldReward = 18 + random.nextInt(17) + charmLevel * 2;
        gold += goldReward;
        chestOpenedAt = System.currentTimeMillis();
        lastReward = foundItem.rarity + " - " + foundItem.name
                + "\nAdded to inventory"
                + "\n" + itemStatLine(foundItem)
                + "\nGold gained: " + goldReward
                + "\nCurrent estimate: " + oldEstimate + " steps"
                + "\nEquip it if you want to change your build.";
        addEvent("Chest opened: " + foundItem.rarity + " " + foundItem.name + ".");
        chestReady = false;
        activeRun = activityMode == MODE_DUNGEON;
        phase = activeRun ? PHASE_COMBAT : PHASE_TRAVEL;
        encounterIndex = 0;
        enemyHp = enemyMaxHp(false);
        attackCharge = 0;
        enemyAttackCharge = 0;
        autoChestCharge = 0;
        playerHp = maxPlayerHp();
        if (activeRun) {
            fightScreen = FIGHT_COMBAT;
            addEvent("Goblin Cave I starts again.");
        } else {
            activityMode = MODE_NONE;
            fightScreen = FIGHT_HUB;
        }
        saveState();
        updateViews();
    }

    private void loadState() {
        String storedDate = prefs.getString(DATE_KEY, "");
        if (!todayKey.equals(storedDate)) {
            baseline = -1;
            todaySteps = 0;
            debugStepOffset = 0;
        } else {
            baseline = prefs.getInt(BASELINE_KEY, -1);
            todaySteps = prefs.getInt(TODAY_STEPS_KEY, 0);
            debugStepOffset = prefs.getInt(DEBUG_STEP_OFFSET_KEY, 0);
        }

        activeRun = prefs.getBoolean(ACTIVE_KEY, false);
        chestReady = prefs.getBoolean(CHEST_READY_KEY, false);
        phase = prefs.getInt(PHASE_KEY, PHASE_TRAVEL);
        encounterIndex = prefs.getInt(ENCOUNTER_KEY, 0);
        enemyHp = prefs.getInt(ENEMY_HP_KEY, enemyMaxHp(false));
        attackCharge = prefs.getInt(ATTACK_CHARGE_KEY, 0);
        enemyAttackCharge = prefs.getInt(ENEMY_ATTACK_CHARGE_KEY, 0);
        playerHp = prefs.getInt(PLAYER_HP_KEY, maxPlayerHp());
        lastGameSteps = prefs.getInt(LAST_GAME_STEPS_KEY, todaySteps);
        lastReward = prefs.getString(LAST_REWARD_KEY, lastReward);
        eventLog = prefs.getString(EVENT_LOG_KEY, eventLog);
        chestOpenedAt = prefs.getLong(CHEST_OPENED_AT_KEY, 0L);
        autoChestCharge = prefs.getInt(AUTO_CHEST_CHARGE_KEY, 0);
        activityMode = prefs.getInt(ACTIVITY_MODE_KEY, activeRun || chestReady ? MODE_DUNGEON : MODE_NONE);
        mainTab = prefs.getInt(MAIN_TAB_KEY, TAB_FIGHT);
        fightScreen = prefs.getInt(FIGHT_SCREEN_KEY, activeRun || chestReady ? FIGHT_COMBAT : FIGHT_HUB);
        gold = prefs.getInt(GOLD_KEY, 0);
        weaponLevel = prefs.getInt(WEAPON_LEVEL_KEY, 1);
        armorLevel = prefs.getInt(ARMOR_LEVEL_KEY, 1);
        bootsLevel = prefs.getInt(BOOTS_LEVEL_KEY, 1);
        charmLevel = prefs.getInt(CHARM_LEVEL_KEY, 1);
        nextItemId = prefs.getInt(NEXT_ITEM_ID_KEY, 5);
        equippedWeaponId = prefs.getInt(EQUIPPED_WEAPON_KEY, 1);
        equippedArmorId = prefs.getInt(EQUIPPED_ARMOR_KEY, 2);
        equippedBootsId = prefs.getInt(EQUIPPED_BOOTS_KEY, 3);
        equippedCharmId = prefs.getInt(EQUIPPED_CHARM_KEY, 4);
        parseInventory(prefs.getString(INVENTORY_KEY, ""));
        if (inventory.isEmpty()) {
            createStarterInventory(weaponLevel, armorLevel, bootsLevel, charmLevel);
        }
        updateEquippedStats();
        playerHp = Math.min(playerHp, maxPlayerHp());
        if (activeRun && phase == PHASE_TRAVEL) {
            phase = PHASE_COMBAT;
        }
        if (activeRun && phase != PHASE_COMPLETE) {
            enemyHp = Math.min(enemyHp, enemyMaxHp(isBossFight()));
            if (enemyHp <= 0) {
                enemyHp = enemyMaxHp(isBossFight());
            }
        }
    }

    private void saveState() {
        prefs.edit()
                .putString(DATE_KEY, todayKey)
                .putInt(BASELINE_KEY, baseline)
                .putInt(TODAY_STEPS_KEY, todaySteps)
                .putInt(DEBUG_STEP_OFFSET_KEY, debugStepOffset)
                .putBoolean(ACTIVE_KEY, activeRun)
                .putBoolean(CHEST_READY_KEY, chestReady)
                .putInt(PHASE_KEY, phase)
                .putInt(ENCOUNTER_KEY, encounterIndex)
                .putInt(TRAVEL_LEFT_KEY, travelLeft)
                .putInt(ENEMY_HP_KEY, enemyHp)
                .putInt(ATTACK_CHARGE_KEY, attackCharge)
                .putInt(ENEMY_ATTACK_CHARGE_KEY, enemyAttackCharge)
                .putInt(PLAYER_HP_KEY, playerHp)
                .putInt(LAST_GAME_STEPS_KEY, lastGameSteps)
                .putString(LAST_REWARD_KEY, lastReward)
                .putString(EVENT_LOG_KEY, eventLog)
                .putLong(CHEST_OPENED_AT_KEY, chestOpenedAt)
                .putInt(AUTO_CHEST_CHARGE_KEY, autoChestCharge)
                .putInt(ACTIVITY_MODE_KEY, activityMode)
                .putInt(MAIN_TAB_KEY, mainTab)
                .putInt(FIGHT_SCREEN_KEY, fightScreen)
                .putInt(GOLD_KEY, gold)
                .putInt(WEAPON_LEVEL_KEY, weaponLevel)
                .putInt(ARMOR_LEVEL_KEY, armorLevel)
                .putInt(BOOTS_LEVEL_KEY, bootsLevel)
                .putInt(CHARM_LEVEL_KEY, charmLevel)
                .putInt(NEXT_ITEM_ID_KEY, nextItemId)
                .putInt(EQUIPPED_WEAPON_KEY, equippedWeaponId)
                .putInt(EQUIPPED_ARMOR_KEY, equippedArmorId)
                .putInt(EQUIPPED_BOOTS_KEY, equippedBootsId)
                .putInt(EQUIPPED_CHARM_KEY, equippedCharmId)
                .putString(INVENTORY_KEY, serializeInventory())
                .apply();
    }

    private void ensureCurrentDay() {
        String currentDate = currentDateKey();
        if (!currentDate.equals(todayKey)) {
            todayKey = currentDate;
            baseline = -1;
            todaySteps = 0;
            debugStepOffset = 0;
            lastGameSteps = 0;
        }
    }

    private void addTestSteps(int steps) {
        if (!activeRun && !chestReady) {
            startDungeonRun();
        }

        debugStepOffset += steps;
        todaySteps += steps;
        if (activeRun || chestReady) {
            processGameSteps(steps);
        }
        saveState();
        updateViews();
    }

    private void resetPrototype() {
        baseline = -1;
        todaySteps = 0;
        debugStepOffset = 0;
        activeRun = false;
        chestReady = false;
        activityMode = MODE_NONE;
        mainTab = TAB_FIGHT;
        fightScreen = FIGHT_HUB;
        phase = PHASE_TRAVEL;
        encounterIndex = 0;
        enemyHp = enemyMaxHp(false);
        attackCharge = 0;
        enemyAttackCharge = 0;
        autoChestCharge = 0;
        lastGameSteps = 0;
        gold = 0;
        weaponLevel = 1;
        armorLevel = 1;
        bootsLevel = 1;
        charmLevel = 1;
        inventory.clear();
        createStarterInventory(1, 1, 1, 1);
        updateEquippedStats();
        playerHp = maxPlayerHp();
        chestOpenedAt = 0L;
        lastReward = "Prototype reset. Start Goblin Cave I when ready.";
        eventLog = "Prototype reset. Ready at the cave mouth.";
        saveState();
        updateViews();
    }

    private void updateViews() {
        if (todayStepsView == null) {
            return;
        }

        boolean hasSensor = stepCounterSensor != null;
        boolean hasPermission = hasStepPermission();
        dateView.setText("Gold " + gold);
        todayStepsView.setText(todaySteps + " STEPS");

        dungeonTitleView.setText(activityTitle());
        dungeonStatusView.setText(activityStatus());
        progressDetailView.setText(progressText());
        enemyView.setText(enemyText());
        nextAttackView.setText(nextAttackText());
        dungeonProgressBar.setProgress(dungeonProgress());
        enemyProgressBar.setProgress(enemyAttackProgress());
        attackProgressBar.setProgress(attackProgress());
        actionMeterView.setText(actionMeterText());
        sceneView.invalidate();

        permissionButton.setVisibility(hasPermission || !hasSensor ? View.GONE : View.VISIBLE);
        resetButton.setVisibility(View.VISIBLE);

        gearView.setText(gearText());
        updateInventoryView();
        lootView.setText(combatInfoText());
        eventLogView.setText(eventLog);
        updateMainScreens();

        if (!hasSensor) {
            systemView.setText("This phone does not expose Android's step counter sensor.");
        } else if (!hasPermission) {
            systemView.setText("Allow activity permission so steps can power the dungeon.");
        } else if (phase == PHASE_EXHAUSTED) {
            systemView.setText("Exhausted. Keep walking to recover HP.");
        } else if (activeRun) {
            systemView.setText("Walk with your phone. Steps power attacks and enemy turns.");
        } else if (chestReady) {
            systemView.setText("Dungeon clear. Walk 10 steps to open the chest.");
        } else {
            systemView.setText("Choose Fight, Skills, Bag, or Town from the main menu.");
        }
    }

    private String activityTitle() {
        if (activityMode == MODE_AREA) {
            return "Deep Forest";
        }
        if (activityMode == MODE_DUNGEON || chestReady) {
            return "Goblin Cave I";
        }
        return "Fight";
    }

    private String activityStatus() {
        if (activityMode == MODE_AREA) {
            return "Area farming - Cave Goblin";
        }
        if (activityMode == MODE_DUNGEON || chestReady) {
            return stageLabel() + " - " + difficultyLabel() + " - " + estimatedClearSteps() + " estimated steps";
        }
        return "Pick an activity to begin.";
    }

    private String combatInfoText() {
        String label = activityMode == MODE_AREA ? "Possible drops" : "Rewards";
        return label + "\n" + lastReward + "\n\nEvent log\n" + eventLog;
    }

    private String progressText() {
        if (chestReady || phase == PHASE_COMPLETE) {
            return "Goblin Chief defeated. Chest opens at " + Math.min(autoChestCharge, 10) + " / 10 steps.";
        }
        if (!activeRun) {
            if (fightScreen == FIGHT_AREA_ENEMY) {
                return "Farm Cave Goblins until you retreat.";
            }
            if (fightScreen == FIGHT_DUNGEON_DETAIL) {
                return "3 encounters - Boss: Goblin Chief - HP combat";
            }
            return "Select an area or dungeon.";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Exhausted - recover to " + resumeHp() + " HP to keep fighting";
        }
        if (activityMode == MODE_AREA) {
            return "Farming Cave Goblins - enemy attacks every " + enemyAttackInterval() + " steps";
        }
        return (isBossFight() ? "Boss fight" : "Encounter " + (encounterIndex + 1) + " of " + ENCOUNTERS)
                + " - enemy attacks every " + enemyAttackInterval() + " steps";
    }

    private String enemyText() {
        if (chestReady) {
            return "Chest opens after " + Math.max(0, 10 - autoChestCharge) + " more steps";
        }
        if (!activeRun) {
            return "No active fight";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Paused while exhausted";
        }
        return enemyName() + " " + Math.min(enemyAttackCharge, enemyAttackInterval()) + " / " + enemyAttackInterval() + " steps";
    }

    private String nextAttackText() {
        if (chestReady) {
            return "Auto-opening chest: " + Math.min(autoChestCharge, 10) + " / 10 steps.";
        }
        if (!activeRun) {
            return equippedWeapon().name + " attacks every " + attackInterval() + " steps.";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Recovery: " + recoveryAmount() + " HP every " + recoveryStepCost() + " steps.";
        }
        return equippedWeapon().name + " " + Math.min(attackCharge, attackInterval()) + " / " + attackInterval() + " steps";
    }

    private String actionMeterText() {
        if (chestReady) {
            return "Opening chest";
        }
        if (!activeRun) {
            return "Ready";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Recovering";
        }
        return "Keep walking";
    }

    private int attackProgress() {
        if (chestReady) {
            return Math.max(0, Math.min(1000, autoChestCharge * 1000 / 10));
        }
        if (!activeRun) {
            return 0;
        }
        if (phase == PHASE_EXHAUSTED) {
            return Math.max(0, Math.min(1000, attackCharge * 1000 / recoveryStepCost()));
        }
        return Math.max(0, Math.min(1000, attackCharge * 1000 / attackInterval()));
    }

    private int enemyAttackProgress() {
        if (!activeRun || phase == PHASE_EXHAUSTED || chestReady) {
            return 0;
        }
        return Math.max(0, Math.min(1000, enemyAttackCharge * 1000 / enemyAttackInterval()));
    }

    private String gearText() {
        return "Weapon: " + equippedWeapon().displayName() + " - " + attackDamage() + " damage / "
                + attackInterval() + " steps\n"
                + "Armor: " + equippedArmor().displayName() + " - +" + armorHpBonus() + " HP, " + armorReduction() + "% reduction\n"
                + "Boots: " + equippedBoots().displayName() + " - +" + bootsHpBonus() + " HP, " + dodgeChance() + "% dodge\n"
                + "Charm: " + equippedCharm().displayName() + " - " + recoveryAmount() + " recovery, " + (charmLevel * 2) + " bonus gold";
    }

    private void updateInventoryView() {
        if (inventoryListView == null) {
            return;
        }

        inventoryListView.removeAllViews();
        for (Item item : inventory) {
            Button button = actionButton(itemButtonText(item), isEquipped(item));
            button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            button.setOnClickListener(v -> equipItem(item.id));
            inventoryListView.addView(button, buttonLayoutParams());
        }
    }

    private void setActiveTab(int tab) {
        showMainTab(tab);
    }

    private void updateTabs() {
        updateMainScreens();
    }

    private void showMainTab(int tab) {
        mainTab = tab;
        updateMainScreens();
        saveState();
    }

    private void showFightScreen(int screen) {
        mainTab = TAB_FIGHT;
        fightScreen = screen;
        updateMainScreens();
        saveState();
    }

    private void updateMainScreens() {
        if (fightPanel == null) {
            return;
        }

        if (activeRun || chestReady) {
            fightScreen = FIGHT_COMBAT;
        }

        fightPanel.setVisibility(mainTab == TAB_FIGHT ? View.VISIBLE : View.GONE);
        skillsPanel.setVisibility(mainTab == TAB_SKILLS ? View.VISIBLE : View.GONE);
        bagPanel.setVisibility(mainTab == TAB_BAG ? View.VISIBLE : View.GONE);
        townPanel.setVisibility(mainTab == TAB_TOWN ? View.VISIBLE : View.GONE);

        boolean showCombat = fightScreen == FIGHT_COMBAT;
        fightHubPanel.setVisibility(fightScreen == FIGHT_HUB ? View.VISIBLE : View.GONE);
        areasPanel.setVisibility(fightScreen == FIGHT_AREAS ? View.VISIBLE : View.GONE);
        areaEnemyPanel.setVisibility(fightScreen == FIGHT_AREA_ENEMY ? View.VISIBLE : View.GONE);
        dungeonsPanel.setVisibility(fightScreen == FIGHT_DUNGEONS ? View.VISIBLE : View.GONE);
        dungeonDetailPanel.setVisibility(fightScreen == FIGHT_DUNGEON_DETAIL ? View.VISIBLE : View.GONE);
        combatHeaderPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        scenePanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        actionPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        combatInfoPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);

        styleTabButton(fightNavButton, mainTab == TAB_FIGHT);
        styleTabButton(skillsNavButton, mainTab == TAB_SKILLS);
        styleTabButton(bagNavButton, mainTab == TAB_BAG);
        styleTabButton(townNavButton, mainTab == TAB_TOWN);
    }

    private void stopActivity() {
        activeRun = false;
        chestReady = false;
        activityMode = MODE_NONE;
        phase = PHASE_TRAVEL;
        attackCharge = 0;
        enemyAttackCharge = 0;
        autoChestCharge = 0;
        fightScreen = FIGHT_HUB;
        addEvent("Returned to the Fight hub.");
        saveState();
        updateViews();
    }

    private String itemButtonText(Item item) {
        String equipped = isEquipped(item) ? "Equipped - " : "";
        return equipped + item.rarity + " " + item.name
                + "\n" + slotLabel(item.slot) + " - Level " + item.level
                + " - " + itemStatLine(item);
    }

    private String itemStatLine(Item item) {
        if (SLOT_WEAPON.equals(item.slot)) {
            int damage = 10 + item.level * 6;
            int interval = Math.max(35, 70 - item.level * 4);
            return "Max hit " + damage + " / " + interval + " steps";
        }
        if (SLOT_ARMOR.equals(item.slot)) {
            return "+" + (item.level * 12) + " HP, " + (item.level * 3) + "% reduction";
        }
        if (SLOT_BOOTS.equals(item.slot)) {
            return "+" + (item.level * 6) + " HP, " + Math.min(25, item.level * 3) + "% dodge";
        }
        return (4 + item.level * 2) + " recovery, " + (item.level * 2) + " bonus gold";
    }

    private String slotLabel(String slot) {
        if (SLOT_WEAPON.equals(slot)) {
            return "Weapon";
        }
        if (SLOT_ARMOR.equals(slot)) {
            return "Armor";
        }
        if (SLOT_BOOTS.equals(slot)) {
            return "Boots";
        }
        return "Charm";
    }

    private void equipItem(int itemId) {
        Item item = findItem(itemId);
        if (item == null) {
            return;
        }

        int oldMaxHp = maxPlayerHp();
        if (SLOT_WEAPON.equals(item.slot)) {
            equippedWeaponId = item.id;
        } else if (SLOT_ARMOR.equals(item.slot)) {
            equippedArmorId = item.id;
        } else if (SLOT_BOOTS.equals(item.slot)) {
            equippedBootsId = item.id;
        } else if (SLOT_CHARM.equals(item.slot)) {
            equippedCharmId = item.id;
        }

        updateEquippedStats();
        playerHp = Math.min(maxPlayerHp(), playerHp + Math.max(0, maxPlayerHp() - oldMaxHp));
        addEvent("Equipped " + item.name + ".");
        saveState();
        updateViews();
    }

    private boolean isEquipped(Item item) {
        return item.id == equippedWeaponId
                || item.id == equippedArmorId
                || item.id == equippedBootsId
                || item.id == equippedCharmId;
    }

    private Item equippedWeapon() {
        return equippedOrFallback(equippedWeaponId, SLOT_WEAPON, "Rusty Sword");
    }

    private Item equippedArmor() {
        return equippedOrFallback(equippedArmorId, SLOT_ARMOR, "Cloth Tunic");
    }

    private Item equippedBoots() {
        return equippedOrFallback(equippedBootsId, SLOT_BOOTS, "Worn Boots");
    }

    private Item equippedCharm() {
        return equippedOrFallback(equippedCharmId, SLOT_CHARM, "Lucky Pebble");
    }

    private Item equippedOrFallback(int id, String slot, String name) {
        Item item = findItem(id);
        if (item != null) {
            return item;
        }
        return new Item(id, slot, name, "Common", 1);
    }

    private void updateEquippedStats() {
        weaponLevel = equippedWeapon().level;
        armorLevel = equippedArmor().level;
        bootsLevel = equippedBoots().level;
        charmLevel = equippedCharm().level;
    }

    private int lootLevelForSlot(String slot) {
        int current;
        if (SLOT_WEAPON.equals(slot)) {
            current = equippedWeapon().level;
        } else if (SLOT_ARMOR.equals(slot)) {
            current = equippedArmor().level;
        } else if (SLOT_BOOTS.equals(slot)) {
            current = equippedBoots().level;
        } else {
            current = equippedCharm().level;
        }
        return current + 1 + (random.nextInt(100) < 18 ? 1 : 0);
    }

    private Item findItem(int id) {
        for (Item item : inventory) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }

    private void createStarterInventory(int weapon, int armor, int boots, int charm) {
        inventory.clear();
        inventory.add(new Item(1, SLOT_WEAPON, "Rusty Sword", "Common", Math.max(1, weapon)));
        inventory.add(new Item(2, SLOT_ARMOR, "Cloth Tunic", "Common", Math.max(1, armor)));
        inventory.add(new Item(3, SLOT_BOOTS, "Worn Boots", "Common", Math.max(1, boots)));
        inventory.add(new Item(4, SLOT_CHARM, "Lucky Pebble", "Common", Math.max(1, charm)));
        equippedWeaponId = 1;
        equippedArmorId = 2;
        equippedBootsId = 3;
        equippedCharmId = 4;
        nextItemId = 5;
    }

    private void parseInventory(String value) {
        inventory.clear();
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String[] items = value.split(";");
        for (String itemValue : items) {
            Item item = Item.fromStorage(itemValue);
            if (item != null) {
                inventory.add(item);
            }
        }
    }

    private String serializeInventory() {
        StringBuilder builder = new StringBuilder();
        for (Item item : inventory) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(item.toStorage());
        }
        return builder.toString();
    }

    private String stageLabel() {
        if (chestReady || phase == PHASE_COMPLETE) {
            return "Chest ready";
        }
        if (!activeRun) {
            return "Ready";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Exhausted";
        }
        return isBossFight() ? "Boss fight" : "Combat";
    }

    private void addEvent(String event) {
        String[] lines = eventLog.split("\\n");
        StringBuilder builder = new StringBuilder(event);
        int kept = 0;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }
            if (kept >= 5) {
                break;
            }
            builder.append('\n').append(line);
            kept += 1;
        }
        eventLog = builder.toString();
    }

    private int dungeonProgress() {
        if (chestReady || phase == PHASE_COMPLETE) {
            return 1000;
        }
        int totalFights = ENCOUNTERS + 1;
        return Math.min(1000, encounterIndex * 1000 / totalFights);
    }

    private int enemyProgress() {
        if (!activeRun || phase == PHASE_EXHAUSTED) {
            return 0;
        }
        int maxHp = enemyMaxHp(isBossFight());
        return Math.max(0, Math.min(1000, (maxHp - enemyHp) * 1000 / maxHp));
    }

    private int roomDistance() {
        return 0;
    }

    private int attackInterval() {
        return Math.max(35, 70 - weaponLevel * 4);
    }

    private int attackDamage() {
        return 10 + weaponLevel * 6;
    }

    private int armorReduction() {
        return Math.min(45, armorLevel * 3 + bootsLevel);
    }

    private int enemyMaxHp(boolean boss) {
        return boss ? 220 : 75;
    }

    private String enemyName() {
        return isBossFight() ? "Goblin Chief" : "Cave Goblin";
    }

    private int enemyMaxHit() {
        return isBossFight() ? 18 : 10;
    }

    private int enemyAttackInterval() {
        return isBossFight() ? 90 : 115;
    }

    private int maxPlayerHp() {
        return 100 + armorHpBonus() + bootsHpBonus();
    }

    private int armorHpBonus() {
        return armorLevel * 12;
    }

    private int bootsHpBonus() {
        return bootsLevel * 6;
    }

    private int dodgeChance() {
        return Math.min(35, bootsLevel * 3 + charmLevel);
    }

    private int damageReductionPercent() {
        return armorReduction();
    }

    private int playerHitDamage() {
        return 1 + random.nextInt(attackDamage());
    }

    private int recoveryStepCost() {
        return 25;
    }

    private int recoveryAmount() {
        return 4 + charmLevel * 2;
    }

    private int resumeHp() {
        return Math.max(1, maxPlayerHp() * 40 / 100);
    }

    private boolean isBossFight() {
        return encounterIndex >= ENCOUNTERS;
    }

    private int estimatedClearSteps() {
        int combat = ENCOUNTERS * rounds(enemyMaxHp(false), Math.max(1, attackDamage() / 2)) * attackInterval();
        int boss = rounds(enemyMaxHp(true), Math.max(1, attackDamage() / 2)) * attackInterval();
        return combat + boss;
    }

    private int rounds(int hp, int damage) {
        return (hp + damage - 1) / damage;
    }

    private String difficultyLabel() {
        int estimate = estimatedClearSteps();
        if (estimate < 3000) {
            return "Comfortable";
        }
        if (estimate < 6500) {
            return "Challenging";
        }
        return "Long";
    }

    private String randomAreaDropSlot() {
        int roll = random.nextInt(4);
        if (roll == 0) {
            return SLOT_WEAPON;
        }
        if (roll == 1) {
            return SLOT_ARMOR;
        }
        if (roll == 2) {
            return SLOT_BOOTS;
        }
        return SLOT_CHARM;
    }

    private String areaDropName(String slot) {
        if (SLOT_WEAPON.equals(slot)) {
            return "Goblin Sticker";
        }
        if (SLOT_ARMOR.equals(slot)) {
            return "Patched Jerkin";
        }
        if (SLOT_BOOTS.equals(slot)) {
            return "Mud Boots";
        }
        return "Green Charm";
    }

    private boolean hasStepPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }
        return checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStepPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_ACTIVITY_RECOGNITION);
        }
    }

    private String currentDateKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(dp(2), 1.0f);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(15), dp(16), dp(15));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(255, 251, 239));
        background.setCornerRadius(dp(8));
        background.setStroke(dp(1), Color.rgb(220, 205, 176));
        layout.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(12));
        layout.setLayoutParams(params);
        return layout;
    }

    private LinearLayout darkCard() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(30, 25, 18));
        background.setCornerRadius(dp(4));
        background.setStroke(dp(2), Color.rgb(126, 82, 37));
        layout.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(8));
        layout.setLayoutParams(params);
        return layout;
    }

    private Button actionButton(String label, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTextColor(primary ? Color.WHITE : Color.rgb(46, 34, 28));
        button.setBackgroundResource(primary ? R.drawable.button_primary : R.drawable.button_secondary);
        return button;
    }

    private TextView sectionTitle(String label) {
        TextView title = text(label, 24, Color.rgb(245, 224, 177), true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(10));
        return title;
    }

    private LinearLayout adventureCard(String tag, String title, String body, View.OnClickListener listener) {
        return adventureCard(0, tag, title, body, listener);
    }

    private LinearLayout adventureCard(int drawableRes, String title, String body, View.OnClickListener listener) {
        return adventureCard(drawableRes, "", title, body, listener);
    }

    private LinearLayout adventureCard(int drawableRes, String tag, String title, String body, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(drawableRes == 0 ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        card.setGravity(drawableRes == 0 ? Gravity.CENTER_VERTICAL : Gravity.CENTER);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setMinimumHeight(drawableRes == 0 ? dp(116) : dp(188));
        card.setBackground(cardBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));
        if (listener != null) {
            card.setClickable(true);
            card.setOnClickListener(listener);
        }

        if (drawableRes == 0) {
            card.addView(placeholderIcon(tag, dp(78)), new LinearLayout.LayoutParams(dp(82), dp(82)));
        } else {
            ImageView image = new ImageView(this);
            image.setImageResource(drawableRes);
            image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setBackgroundColor(Color.rgb(13, 16, 12));
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(126)
            );
            imageParams.setMargins(0, 0, 0, dp(8));
            card.addView(image, imageParams);
        }

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(drawableRes == 0 ? dp(12) : dp(4), 0, 0, 0);
        TextView titleView = text(title, 21, Color.rgb(245, 224, 177), true);
        titleView.setGravity(drawableRes == 0 ? Gravity.LEFT : Gravity.CENTER);
        TextView bodyView = text(body, 14, Color.rgb(226, 205, 163), false);
        bodyView.setGravity(drawableRes == 0 ? Gravity.LEFT : Gravity.CENTER);
        bodyView.setPadding(0, dp(5), 0, 0);
        copy.addView(titleView);
        copy.addView(bodyView);
        card.addView(copy, weightedWidth(1.0f));

        LinearLayout.LayoutParams params = buttonLayoutParams();
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout detailRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(cardBackground(Color.rgb(36, 30, 22), Color.rgb(80, 58, 35)));
        TextView labelView = text(label, 13, Color.rgb(192, 157, 100), true);
        TextView valueView = text(value, 15, Color.rgb(245, 224, 177), false);
        valueView.setPadding(0, dp(3), 0, 0);
        row.addView(labelView);
        row.addView(valueView);
        row.setLayoutParams(buttonLayoutParams());
        return row;
    }

    private LinearLayout meterCard(String title, String tag, ProgressBar bar, TextView valueView) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(cardBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(placeholderIcon(tag, dp(34)), new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView titleView = text(title, 13, Color.rgb(245, 224, 177), true);
        titleView.setPadding(dp(8), 0, 0, 0);
        header.addView(titleView, weightedWidth(1.0f));
        card.addView(header);

        card.addView(bar, progressLayoutParams());
        valueView.setTextColor(Color.rgb(192, 157, 100));
        valueView.setTextSize(12);
        valueView.setGravity(Gravity.CENTER);
        valueView.setPadding(0, dp(4), 0, 0);
        card.addView(valueView);
        return card;
    }

    private TextView placeholderIcon(String label, int size) {
        TextView icon = text(label, 13, Color.rgb(245, 224, 177), true);
        icon.setGravity(Gravity.CENTER);
        icon.setSingleLine(false);
        icon.setBackground(cardBackground(Color.rgb(52, 42, 28), Color.rgb(192, 125, 44)));
        icon.setMinWidth(size);
        icon.setMinHeight(size);
        return icon;
    }

    private GradientDrawable cardBackground(int fill, int stroke) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(fill);
        background.setCornerRadius(dp(4));
        background.setStroke(dp(2), stroke);
        return background;
    }

    private Button menuButton(String label) {
        Button button = actionButton(label, false);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        button.setMinHeight(dp(92));
        button.setPadding(dp(14), dp(10), dp(14), dp(10));
        return button;
    }

    private Button backButton() {
        Button button = actionButton("Back", false);
        button.setOnClickListener(v -> showFightScreen(FIGHT_HUB));
        return button;
    }

    private void addLockedRow(LinearLayout parent, String label) {
        Button button = menuButton(label + "\nComing later");
        button.setEnabled(false);
        parent.addView(button, buttonLayoutParams());
    }

    private Button mainNavButton(String label, int tab, int drawableRes) {
        Button button = actionButton(label, false);
        button.setTextSize(12);
        button.setSingleLine(false);
        button.setPadding(dp(2), 0, dp(2), 0);
        Drawable icon = getResources().getDrawable(drawableRes, getTheme());
        int size = dp(28);
        icon.setBounds(0, 0, size, size);
        button.setCompoundDrawables(null, icon, null, null);
        button.setCompoundDrawablePadding(dp(2));
        button.setOnClickListener(v -> showMainTab(tab));
        return button;
    }

    private Button tabButton(String label, int tab) {
        Button button = actionButton(label, false);
        button.setTextSize(11);
        button.setSingleLine(true);
        button.setPadding(dp(2), 0, dp(2), 0);
        button.setOnClickListener(v -> setActiveTab(tab));
        return button;
    }

    private void styleTabButton(Button button, boolean selected) {
        if (button == null) {
            return;
        }
        button.setTextColor(selected ? Color.WHITE : Color.rgb(245, 224, 177));
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? Color.rgb(129, 83, 31) : Color.rgb(36, 30, 22));
        background.setCornerRadius(dp(4));
        background.setStroke(dp(2), selected ? Color.rgb(240, 174, 55) : Color.rgb(126, 82, 37));
        button.setBackground(background);
    }

    private LinearLayout.LayoutParams fullWidthWrapContent() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams weightedWidth(float weight) {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
    }

    private LinearLayout.LayoutParams progressLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(14)
        );
        params.setMargins(0, dp(2), 0, dp(4));
        return params;
    }

    private LinearLayout.LayoutParams sceneLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(260)
        );
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private LinearLayout.LayoutParams buttonLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dp(10));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Item {
        final int id;
        final String slot;
        final String name;
        final String rarity;
        final int level;

        Item(int id, String slot, String name, String rarity, int level) {
            this.id = id;
            this.slot = slot;
            this.name = name;
            this.rarity = rarity;
            this.level = level;
        }

        String displayName() {
            return name + " (Level " + level + ")";
        }

        String toStorage() {
            return id + "," + slot + "," + name + "," + rarity + "," + level;
        }

        static Item fromStorage(String value) {
            String[] parts = value.split(",", -1);
            if (parts.length != 5) {
                return null;
            }
            try {
                return new Item(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        parts[2],
                        parts[3],
                        Integer.parseInt(parts[4])
                );
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private class SceneView extends View {
        private static final int FRAME_COUNT = 4;
        private final Bitmap background;
        private final Bitmap heroIdle;
        private final Bitmap heroWalk;
        private final Bitmap goblin;
        private final Bitmap goblinBoss;
        private final Bitmap chestOpening;
        private final Paint paint = new Paint();
        private final Paint shadePaint = new Paint();
        private final Rect src = new Rect();
        private final Rect dst = new Rect();

        SceneView(Context context) {
            super(context);
            background = BitmapFactory.decodeResource(getResources(), R.drawable.goblin_dungeon);
            heroIdle = BitmapFactory.decodeResource(getResources(), R.drawable.hero_idle);
            heroWalk = BitmapFactory.decodeResource(getResources(), R.drawable.hero_walk);
            goblin = BitmapFactory.decodeResource(getResources(), R.drawable.goblin);
            goblinBoss = BitmapFactory.decodeResource(getResources(), R.drawable.goblin_boss);
            chestOpening = BitmapFactory.decodeResource(getResources(), R.drawable.chest_opening);
            shadePaint.setColor(Color.argb(70, 0, 0, 0));
            paint.setAntiAlias(false);
            paint.setFilterBitmap(false);
            paint.setDither(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawBackground(canvas);

            int frame = (int) ((System.currentTimeMillis() / 220) % FRAME_COUNT);
            boolean walking = activeRun && !chestReady;
            Bitmap heroSheet = walking ? heroWalk : heroIdle;
            int ground = (int) (getHeight() * 0.88f);
            int heroSize = Math.min(dp(112), (int) (getHeight() * 0.78f));
            int heroX = dp(24);
            int heroY = ground - heroSize;
            drawFrame(canvas, heroSheet, frame, heroX, heroY, heroSize, heroSize);
            drawHpBar(canvas, heroX, heroY - dp(18), heroSize, playerHp, maxPlayerHp(), "Arin");

            if (activeRun && (phase == PHASE_COMBAT || phase == PHASE_EXHAUSTED)) {
                Bitmap enemySheet = isBossFight() ? goblinBoss : goblin;
                int enemySize = isBossFight()
                        ? Math.min(dp(145), (int) (getHeight() * 0.88f))
                        : Math.min(dp(112), (int) (getHeight() * 0.75f));
                int enemyX = getWidth() - enemySize - dp(22);
                int enemyY = ground - enemySize;
                drawFrame(canvas, enemySheet, frame, enemyX, enemyY, enemySize, enemySize);
                drawHpBar(canvas, enemyX, enemyY - dp(18), enemySize, Math.max(0, enemyHp), enemyMaxHp(isBossFight()), enemyName());
            }

            if (chestReady || recentlyOpenedChest()) {
                int chestSize = Math.min(dp(92), (int) (getHeight() * 0.52f));
                int chestX = getWidth() - chestSize - dp(42);
                int chestFrame = chestReady ? 0 : Math.min(3, (int) ((System.currentTimeMillis() - chestOpenedAt) / 180));
                drawFrame(canvas, chestOpening, chestFrame, chestX, ground - chestSize - dp(12), chestSize, chestSize);
            }

            canvas.drawRect(0, 0, getWidth(), getHeight(), shadePaint);
            postInvalidateDelayed(180);
        }

        private void drawBackground(Canvas canvas) {
            if (background == null) {
                canvas.drawColor(Color.rgb(35, 45, 39));
                return;
            }

            float viewRatio = getWidth() / (float) getHeight();
            float bitmapRatio = background.getWidth() / (float) background.getHeight();
            if (bitmapRatio > viewRatio) {
                int cropWidth = Math.round(background.getHeight() * viewRatio);
                int left = (background.getWidth() - cropWidth) / 2;
                src.set(left, 0, left + cropWidth, background.getHeight());
            } else {
                int cropHeight = Math.round(background.getWidth() / viewRatio);
                int top = (background.getHeight() - cropHeight) / 2;
                src.set(0, top, background.getWidth(), top + cropHeight);
            }
            dst.set(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(background, src, dst, paint);
        }

        private void drawFrame(Canvas canvas, Bitmap sheet, int frame, int x, int y, int width, int height) {
            if (sheet == null) {
                return;
            }

            int frameWidth = sheet.getWidth() / FRAME_COUNT;
            src.set(frame * frameWidth, 0, (frame + 1) * frameWidth, sheet.getHeight());
            dst.set(x, y, x + width, y + height);
            canvas.drawBitmap(sheet, src, dst, paint);
        }

        private void drawHpBar(Canvas canvas, int x, int y, int width, int current, int max, String label) {
            if (max <= 0) {
                return;
            }

            int barWidth = Math.max(dp(72), Math.min(width, dp(126)));
            int barHeight = dp(9);
            int barX = x + (width - barWidth) / 2;
            int barY = Math.max(dp(8), y);
            int fillWidth = Math.max(0, Math.min(barWidth, current * barWidth / max));

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(210, 20, 12, 10));
            canvas.drawRect(barX - dp(2), barY - dp(2), barX + barWidth + dp(2), barY + barHeight + dp(2), paint);
            paint.setColor(Color.rgb(85, 17, 20));
            canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
            paint.setColor(Color.rgb(214, 54, 48));
            canvas.drawRect(barX, barY, barX + fillWidth, barY + barHeight, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(245, 224, 177));
            canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(10));
            paint.setColor(Color.rgb(245, 224, 177));
            canvas.drawText(label + " " + current + "/" + max, barX + barWidth / 2f, barY - dp(4), paint);
        }

        private boolean recentlyOpenedChest() {
            return chestOpenedAt > 0L && System.currentTimeMillis() - chestOpenedAt < 1400L;
        }
    }
}
