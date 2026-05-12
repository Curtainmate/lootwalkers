package com.example.stepcounterbase;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

import static com.example.stepcounterbase.GameRules.*;

public class MainActivity extends Activity implements SensorEventListener, SceneView.Model {
    private static final int REQUEST_ACTIVITY_RECOGNITION = 40;
    private final Random random = new Random();

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SaveStore saveStore;
    private UiFactory ui;

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
    private LinearLayout equipmentListView;
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
    private ImageView attackMeterIconView;
    private ImageView enemyMeterIconView;
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

        saveStore = new SaveStore(this);
        ui = new UiFactory(this);
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
        fightHubPanel.addView(categoryCard(R.drawable.category_areas, "AREAS", "Farm individual enemies for specific drops.", v -> showFightScreen(FIGHT_AREAS)));
        fightHubPanel.addView(categoryCard(R.drawable.category_dungeons, "DUNGEONS", "Clear encounters, defeat bosses, and open chests.", v -> showFightScreen(FIGHT_DUNGEONS)));
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
        sceneView = new SceneView(this, this);
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
        attackMeterIconView = ui.meterIcon(R.drawable.attack_icon);
        meterRow.addView(meterCard("Your Attack", attackMeterIconView, attackProgressBar, nextAttackView), weightedWidth(1.0f));
        enemyProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        enemyProgressBar.setMax(1000);
        enemyView = text("", 15, Color.rgb(245, 224, 177), true);
        enemyMeterIconView = ui.meterIcon(R.drawable.enemy_attack_icon);
        meterRow.addView(meterCard("Enemy Attack", enemyMeterIconView, enemyProgressBar, enemyView), weightedWidth(1.0f));
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
        bagPanel.addView(sectionTitle("EQUIPMENT"));
        equipmentListView = new LinearLayout(this);
        equipmentListView.setOrientation(LinearLayout.VERTICAL);
        bagPanel.addView(equipmentListView);
        bagPanel.addView(sectionTitle("INVENTORY"));
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
        GameState state = saveStore.load(todayKey, lastReward, eventLog);
        baseline = state.baseline;
        todaySteps = state.todaySteps;
        debugStepOffset = state.debugStepOffset;
        activeRun = state.activeRun;
        chestReady = state.chestReady;
        phase = state.phase;
        encounterIndex = state.encounterIndex;
        travelLeft = state.travelLeft;
        enemyHp = state.enemyHp;
        attackCharge = state.attackCharge;
        enemyAttackCharge = state.enemyAttackCharge;
        playerHp = state.playerHp;
        lastGameSteps = state.lastGameSteps;
        lastReward = state.lastReward;
        eventLog = state.eventLog;
        chestOpenedAt = state.chestOpenedAt;
        autoChestCharge = state.autoChestCharge;
        activityMode = state.activityMode;
        mainTab = state.mainTab;
        fightScreen = state.fightScreen;
        gold = state.gold;
        weaponLevel = state.weaponLevel;
        armorLevel = state.armorLevel;
        bootsLevel = state.bootsLevel;
        charmLevel = state.charmLevel;
        nextItemId = state.nextItemId;
        equippedWeaponId = state.equippedWeaponId;
        equippedArmorId = state.equippedArmorId;
        equippedBootsId = state.equippedBootsId;
        equippedCharmId = state.equippedCharmId;
        inventory.clear();
        inventory.addAll(state.inventory);
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
        GameState state = new GameState();
        state.todayKey = todayKey;
        state.baseline = baseline;
        state.todaySteps = todaySteps;
        state.debugStepOffset = debugStepOffset;
        state.activeRun = activeRun;
        state.chestReady = chestReady;
        state.phase = phase;
        state.encounterIndex = encounterIndex;
        state.travelLeft = travelLeft;
        state.enemyHp = enemyHp;
        state.attackCharge = attackCharge;
        state.enemyAttackCharge = enemyAttackCharge;
        state.playerHp = playerHp;
        state.lastGameSteps = lastGameSteps;
        state.lastReward = lastReward;
        state.eventLog = eventLog;
        state.chestOpenedAt = chestOpenedAt;
        state.autoChestCharge = autoChestCharge;
        state.activityMode = activityMode;
        state.mainTab = mainTab;
        state.fightScreen = fightScreen;
        state.gold = gold;
        state.weaponLevel = weaponLevel;
        state.armorLevel = armorLevel;
        state.bootsLevel = bootsLevel;
        state.charmLevel = charmLevel;
        state.nextItemId = nextItemId;
        state.equippedWeaponId = equippedWeaponId;
        state.equippedArmorId = equippedArmorId;
        state.equippedBootsId = equippedBootsId;
        state.equippedCharmId = equippedCharmId;
        state.inventory.addAll(inventory);
        saveStore.save(state);
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
        updateMeterIcons();
        actionMeterView.setText(actionMeterText());
        sceneView.invalidate();

        permissionButton.setVisibility(hasPermission || !hasSensor ? View.GONE : View.VISIBLE);
        resetButton.setVisibility(View.VISIBLE);

        updateEquipmentView();
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

    private void updateMeterIcons() {
        if (attackMeterIconView == null || enemyMeterIconView == null) {
            return;
        }
        if (chestReady) {
            attackMeterIconView.setImageResource(R.drawable.chest_open);
            enemyMeterIconView.setImageResource(R.drawable.chest_open);
            return;
        }
        if (phase == PHASE_EXHAUSTED) {
            attackMeterIconView.setImageResource(R.drawable.recovery_icon);
            enemyMeterIconView.setImageResource(R.drawable.enemy_attack_icon);
            return;
        }
        attackMeterIconView.setImageResource(R.drawable.attack_icon);
        enemyMeterIconView.setImageResource(R.drawable.enemy_attack_icon);
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

    private void updateEquipmentView() {
        if (equipmentListView == null) {
            return;
        }

        equipmentListView.removeAllViews();
        equipmentListView.addView(equipmentRow(R.drawable.slot_weapon, "Weapon", equippedWeapon(),
                attackDamage() + " max hit / " + attackInterval() + " steps"));
        equipmentListView.addView(equipmentRow(R.drawable.slot_armor, "Armor", equippedArmor(),
                "+" + armorHpBonus() + " HP / " + armorReduction() + "% reduction"));
        equipmentListView.addView(equipmentRow(R.drawable.slot_boots, "Boots", equippedBoots(),
                "+" + bootsHpBonus() + " HP / " + dodgeChance() + "% dodge"));
        equipmentListView.addView(equipmentRow(R.drawable.slot_charm, "Charm", equippedCharm(),
                recoveryAmount() + " recovery / " + (charmLevel * 2) + " bonus gold"));
    }

    private LinearLayout equipmentRow(int slotIconRes, String slot, Item item, String statLine) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));

        row.addView(equipmentSlotIcon(slotIconRes, item), new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(slot, 13, Color.rgb(192, 157, 100), true));
        copy.addView(text(item.displayName(), 17, Color.rgb(245, 224, 177), true));
        copy.addView(text(statLine, 13, Color.rgb(226, 205, 163), false));
        row.addView(copy, weightedWidth(1.0f));

        row.setOnClickListener(v -> equipItem(item.id));
        row.setLayoutParams(buttonLayoutParams());
        return row;
    }

    private FrameLayout equipmentSlotIcon(int slotIconRes, Item item) {
        FrameLayout frame = new FrameLayout(this);

        ImageView slotFrame = new ImageView(this);
        slotFrame.setImageResource(slotIconRes);
        slotFrame.setAdjustViewBounds(true);
        slotFrame.setScaleType(ImageView.ScaleType.FIT_CENTER);
        frame.addView(slotFrame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.CENTER
        ));

        if (item != null) {
            ImageView equippedIcon = new ImageView(this);
            equippedIcon.setImageResource(itemIcon(item.slot));
            equippedIcon.setAdjustViewBounds(true);
            equippedIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            equippedIcon.setPadding(dp(9), dp(9), dp(9), dp(9));
            frame.addView(equippedIcon, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            ));
        }

        return frame;
    }

    private void updateInventoryView() {
        if (inventoryListView == null) {
            return;
        }

        inventoryListView.removeAllViews();
        for (Item item : inventory) {
            inventoryListView.addView(inventoryItemCard(item));
        }
    }

    private LinearLayout inventoryItemCard(Item item) {
        boolean equipped = isEquipped(item);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(itemIcon(item.slot));
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(3), dp(3), dp(3), dp(3));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), rarityColor(item.rarity)));
        row.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(item.rarity + " " + slotLabel(item.slot), 12, rarityColor(item.rarity), true));
        copy.addView(text(item.name + " - Level " + item.level, 16, Color.rgb(245, 224, 177), true));
        copy.addView(text(itemStatLine(item), 12, Color.rgb(226, 205, 163), false));
        row.addView(copy, weightedWidth(1.0f));

        TextView equippedBadge = text(equipped ? "EQUIPPED" : "EQUIP", 11,
                equipped ? Color.rgb(139, 229, 87) : Color.rgb(192, 157, 100), true);
        equippedBadge.setGravity(Gravity.CENTER);
        row.addView(equippedBadge, new LinearLayout.LayoutParams(dp(70), LinearLayout.LayoutParams.WRAP_CONTENT));

        row.setOnClickListener(v -> equipItem(item.id));
        row.setLayoutParams(buttonLayoutParams());
        return row;
    }

    private int itemIcon(String slot) {
        if (SLOT_WEAPON.equals(slot)) {
            return R.drawable.item_weapon;
        }
        if (SLOT_ARMOR.equals(slot)) {
            return R.drawable.item_armor;
        }
        if (SLOT_BOOTS.equals(slot)) {
            return R.drawable.item_boots;
        }
        if (SLOT_CHARM.equals(slot)) {
            return R.drawable.item_charm;
        }
        return R.drawable.item_gold;
    }

    private int rarityColor(String rarity) {
        if ("Rare".equals(rarity)) {
            return Color.rgb(78, 159, 255);
        }
        if ("Epic".equals(rarity)) {
            return Color.rgb(181, 96, 255);
        }
        if ("Uncommon".equals(rarity)) {
            return Color.rgb(139, 229, 87);
        }
        return Color.rgb(192, 157, 100);
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
        return CombatSystem.attackInterval(weaponLevel);
    }

    private int attackDamage() {
        return CombatSystem.attackDamage(weaponLevel);
    }

    private int armorReduction() {
        return CombatSystem.armorReduction(armorLevel, bootsLevel);
    }

    private int enemyMaxHp(boolean boss) {
        return CombatSystem.enemyMaxHp(boss);
    }

    private String enemyName() {
        return CombatSystem.enemyName(isBossFight());
    }

    private int enemyMaxHit() {
        return CombatSystem.enemyMaxHit(isBossFight());
    }

    private int enemyAttackInterval() {
        return CombatSystem.enemyAttackInterval(isBossFight());
    }

    private int maxPlayerHp() {
        return CombatSystem.maxPlayerHp(armorLevel, bootsLevel);
    }

    private int armorHpBonus() {
        return CombatSystem.armorHpBonus(armorLevel);
    }

    private int bootsHpBonus() {
        return CombatSystem.bootsHpBonus(bootsLevel);
    }

    private int dodgeChance() {
        return CombatSystem.dodgeChance(bootsLevel, charmLevel);
    }

    private int damageReductionPercent() {
        return armorReduction();
    }

    private int playerHitDamage() {
        return 1 + random.nextInt(attackDamage());
    }

    private int recoveryStepCost() {
        return CombatSystem.recoveryStepCost();
    }

    private int recoveryAmount() {
        return CombatSystem.recoveryAmount(charmLevel);
    }

    private int resumeHp() {
        return CombatSystem.resumeHp(maxPlayerHp());
    }

    private boolean isBossFight() {
        return encounterIndex >= ENCOUNTERS;
    }

    private int estimatedClearSteps() {
        return CombatSystem.estimatedClearSteps(weaponLevel);
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
        return LootSystem.randomAreaDropSlot(random);
    }

    private String areaDropName(String slot) {
        return LootSystem.areaDropName(slot);
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
        return ui.text(value, size, color, bold);
    }

    private LinearLayout darkCard() {
        return ui.darkCard();
    }

    private Button actionButton(String label, boolean primary) {
        return ui.actionButton(label, primary);
    }

    private TextView sectionTitle(String label) {
        return ui.sectionTitle(label);
    }

    private LinearLayout adventureCard(String tag, String title, String body, View.OnClickListener listener) {
        return ui.adventureCard(tag, title, body, listener);
    }

    private LinearLayout adventureCard(int drawableRes, String title, String body, View.OnClickListener listener) {
        return ui.adventureCard(drawableRes, title, body, listener);
    }

    private LinearLayout categoryCard(int drawableRes, String title, String body, View.OnClickListener listener) {
        return ui.iconAdventureCard(drawableRes, title, body, listener);
    }

    private LinearLayout detailRow(String label, String value) {
        return ui.detailRow(label, value);
    }

    private LinearLayout meterCard(String title, String tag, ProgressBar bar, TextView valueView) {
        return ui.meterCard(title, tag, bar, valueView);
    }

    private LinearLayout meterCard(String title, ImageView icon, ProgressBar bar, TextView valueView) {
        return ui.meterCard(title, icon, bar, valueView);
    }

    private Button menuButton(String label) {
        return ui.menuButton(label);
    }

    private Button backButton() {
        return ui.backButton(v -> showFightScreen(FIGHT_HUB));
    }

    private void addLockedRow(LinearLayout parent, String label) {
        ui.addLockedRow(parent, label);
    }

    private Button mainNavButton(String label, int tab, int drawableRes) {
        return ui.mainNavButton(label, drawableRes, v -> showMainTab(tab));
    }

    private void styleTabButton(Button button, boolean selected) {
        ui.styleTabButton(button, selected);
    }

    private LinearLayout.LayoutParams fullWidthWrapContent() {
        return ui.fullWidthWrapContent();
    }

    private LinearLayout.LayoutParams weightedWidth(float weight) {
        return ui.weightedWidth(weight);
    }

    private LinearLayout.LayoutParams progressLayoutParams() {
        return ui.progressLayoutParams();
    }

    private LinearLayout.LayoutParams sceneLayoutParams() {
        return ui.sceneLayoutParams();
    }

    private LinearLayout.LayoutParams buttonLayoutParams() {
        return ui.buttonLayoutParams();
    }

    private int dp(int value) {
        return ui.dp(value);
    }

    @Override
    public boolean sceneHeroWalking() {
        return activeRun && !chestReady;
    }

    @Override
    public int scenePlayerHp() {
        return playerHp;
    }

    @Override
    public int scenePlayerMaxHp() {
        return maxPlayerHp();
    }

    @Override
    public boolean sceneShouldShowEnemy() {
        return activeRun && (phase == PHASE_COMBAT || phase == PHASE_EXHAUSTED);
    }

    @Override
    public boolean sceneIsBossFight() {
        return isBossFight();
    }

    @Override
    public int sceneEnemyHp() {
        return enemyHp;
    }

    @Override
    public int sceneEnemyMaxHp() {
        return enemyMaxHp(isBossFight());
    }

    @Override
    public String sceneEnemyName() {
        return enemyName();
    }

    @Override
    public boolean sceneChestReady() {
        return chestReady;
    }

    @Override
    public long sceneChestOpenedAt() {
        return chestOpenedAt;
    }
}
