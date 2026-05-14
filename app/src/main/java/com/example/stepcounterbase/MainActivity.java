package com.example.stepcounterbase;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.CheckBox;
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
    private static final int AREA_DEEP_FOREST = 0;
    private static final int AREA_GRASSY_FIELDS = 1;
    private static final int AREA_ENEMY_GREEN_SLIME = 0;
    private static final int AREA_ENEMY_RUNAWAY_SCARECROW = 1;
    private static final int AREA_ENEMY_RAGGED_BANDIT = 2;
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
    private TextView eventLogView;
    private TextView areaEnemyTitleView;
    private LinearLayout rewardContentView;
    private LinearLayout combatLogContentView;
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
    private ImageView preferencesButton;
    private Button combatLogToggleButton;
    private boolean dungeonLootVisible = false;
    private boolean showDevTools = false;

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
    private int selectedArea = AREA_GRASSY_FIELDS;
    private int selectedAreaEnemy = AREA_ENEMY_GREEN_SLIME;
    private int expandedAreaLootEnemy = -1;
    private int autoChestCharge = 0;
    private long chestOpenedAt = 0L;
    private String lastReward = "No loot yet. Clear Goblin Cave I to open your first chest.";
    private String lastRewardTitle = "Rewards";
    private String lastRewardNote = "No loot yet. Clear Goblin Cave I to open your first chest.";
    private Item lastRewardItem = null;
    private int lastRewardGold = 0;
    private boolean lastRewardFromChest = false;
    private final ArrayList<Item> lastRewardItems = new ArrayList<>();
    private int rewardStepsRemaining = 0;
    private String eventLog = "Ready at the cave mouth.";
    private boolean combatLogVisible = true;
    private final ArrayList<Item> inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();
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
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setBackgroundColor(Color.rgb(16, 14, 11));
        screen.setPadding(dp(8), dp(10), dp(8), dp(6));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);

        LinearLayout contentRoot = new LinearLayout(this);
        contentRoot.setOrientation(LinearLayout.VERTICAL);
        contentRoot.setPadding(0, 0, 0, dp(8));
        scrollView.addView(contentRoot);

        LinearLayout topHud = darkCard();
        topHud.setOrientation(LinearLayout.HORIZONTAL);
        topHud.setGravity(Gravity.CENTER_VERTICAL);

        preferencesButton = new ImageView(this);
        preferencesButton.setImageResource(R.drawable.preference_icon);
        preferencesButton.setAdjustViewBounds(true);
        preferencesButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preferencesButton.setPadding(dp(6), dp(6), dp(6), dp(6));
        preferencesButton.setBackground(ui.panelBackground(Color.rgb(36, 30, 22), Color.rgb(126, 82, 37)));
        preferencesButton.setClickable(true);
        preferencesButton.setOnClickListener(v -> showPreferencesDialog());
        LinearLayout.LayoutParams preferencesParams = new LinearLayout.LayoutParams(dp(44), dp(44));
        preferencesParams.setMargins(0, 0, dp(9), 0);
        topHud.addView(preferencesButton, preferencesParams);

        LinearLayout heroHud = new LinearLayout(this);
        heroHud.setOrientation(LinearLayout.VERTICAL);
        TextView nameView = text("Arin", 23, Color.rgb(245, 224, 177), true);
        heroHud.addView(nameView);
        dateView = text("Lv. 1", 15, Color.rgb(192, 157, 100), true);
        heroHud.addView(dateView);
        topHud.addView(heroHud, weightedWidth(1.0f));

        LinearLayout statHud = new LinearLayout(this);
        statHud.setOrientation(LinearLayout.VERTICAL);
        statHud.setGravity(Gravity.RIGHT);
        todayStepsView = text("", 17, Color.rgb(245, 224, 177), true);
        todayStepsView.setGravity(Gravity.RIGHT);
        statHud.addView(todayStepsView);
        topHud.addView(statHud, weightedWidth(1.25f));
        screen.addView(topHud);

        fightPanel = new LinearLayout(this);
        fightPanel.setOrientation(LinearLayout.VERTICAL);
        contentRoot.addView(fightPanel);

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
        areasPanel.addView(adventureCard(R.drawable.title_grassy_fields, "Grassy Fields", "A sunny beginner meadow. This is the first main area we will build out.", v -> showAreaEnemy(AREA_GRASSY_FIELDS)));
        areasPanel.addView(testAreaCard(R.drawable.card_areas_deep_forest, v -> showAreaEnemy(AREA_DEEP_FOREST)));
        areasPanel.addView(backButton());
        fightPanel.addView(areasPanel);

        areaEnemyPanel = darkCard();
        areaEnemyTitleView = sectionTitle("Grassy Fields");
        areaEnemyPanel.addView(areaEnemyTitleView);
        areaEnemyPanel.addView(adventureCard(selectedAreaPortrait(), selectedAreaEnemyName(), selectedAreaEnemyStats(), null));
        areaEnemyPanel.addView(detailRow("Possible drops", selectedAreaDrops()));
        Button startArea = actionButton("Start Farming", true);
        startArea.setOnClickListener(v -> startAreaFarming());
        areaEnemyPanel.addView(startArea, buttonLayoutParams());
        areaEnemyPanel.addView(backButton());
        fightPanel.addView(areaEnemyPanel);

        dungeonsPanel = darkCard();
        fightPanel.addView(dungeonsPanel);

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

        LinearLayout combatButtonRow = new LinearLayout(this);
        combatButtonRow.setOrientation(LinearLayout.HORIZONTAL);
        retreatButton = actionButton("Retreat", false);
        retreatButton.setTextColor(Color.rgb(245, 224, 177));
        retreatButton.setBackground(ui.panelBackground(Color.rgb(82, 34, 28), Color.rgb(188, 84, 58)));
        retreatButton.setOnClickListener(v -> stopActivity());
        combatButtonRow.addView(retreatButton, weightedWidth(1.0f));
        combatLogToggleButton = actionButton("Hide Log", false);
        combatLogToggleButton.setTextColor(Color.rgb(245, 224, 177));
        combatLogToggleButton.setBackground(ui.panelBackground(Color.rgb(36, 30, 22), Color.rgb(192, 125, 44)));
        combatLogToggleButton.setOnClickListener(v -> toggleCombatLog());
        combatButtonRow.addView(combatLogToggleButton, weightedWidth(1.0f));
        actionPanel.addView(combatButtonRow, buttonLayoutParams());
        fightPanel.addView(actionPanel);

        combatInfoPanel = darkCard();
        rewardContentView = new LinearLayout(this);
        rewardContentView.setOrientation(LinearLayout.VERTICAL);
        combatInfoPanel.addView(rewardContentView);
        combatLogContentView = new LinearLayout(this);
        combatLogContentView.setOrientation(LinearLayout.VERTICAL);
        combatInfoPanel.addView(combatLogContentView);
        testStepsButton = actionButton("+100 test steps", false);
        testStepsButton.setOnClickListener(v -> addTestSteps(100));
        combatInfoPanel.addView(testStepsButton, buttonLayoutParams());
        testBigStepsButton = actionButton("+1000 test steps", false);
        testBigStepsButton.setOnClickListener(v -> addTestSteps(1000));
        combatInfoPanel.addView(testBigStepsButton, buttonLayoutParams());
        fightPanel.addView(combatInfoPanel);

        skillsPanel = darkCard();
        skillsPanel.addView(text("SKILLS", 26, Color.rgb(245, 224, 177), true));
        skillsPanel.addView(skillRow(R.drawable.skill_woodcutting, "Woodcutting"));
        skillsPanel.addView(skillRow(R.drawable.skill_mining, "Mining"));
        skillsPanel.addView(skillRow(R.drawable.skill_fishing, "Fishing"));
        skillsPanel.addView(skillRow(R.drawable.skill_crafting, "Crafting"));
        skillsPanel.addView(skillRow(R.drawable.skill_cooking, "Cooking"));
        contentRoot.addView(skillsPanel);

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
        contentRoot.addView(bagPanel);

        townPanel = darkCard();
        townPanel.addView(text("TOWN", 26, Color.rgb(245, 224, 177), true));
        addLockedRow(townPanel, "Merchant");
        addLockedRow(townPanel, "Bank");
        addLockedRow(townPanel, "Trainer");
        addLockedRow(townPanel, "Activity");
        contentRoot.addView(townPanel);

        permissionButton = actionButton("Allow step tracking", true);
        permissionButton.setOnClickListener(v -> requestStepPermission());
        contentRoot.addView(permissionButton, buttonLayoutParams());

        resetButton = actionButton("Reset prototype", false);
        resetButton.setOnClickListener(v -> resetPrototype());
        contentRoot.addView(resetButton, buttonLayoutParams());

        eventLogView = text("", 14, Color.rgb(226, 205, 163), false);
        systemView = text("", 14, Color.rgb(192, 157, 100), false);
        systemView.setGravity(Gravity.CENTER);
        systemView.setPadding(0, dp(12), 0, 0);
        contentRoot.addView(systemView, fullWidthWrapContent());

        screen.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
        ));

        LinearLayout navRow = new LinearLayout(this);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        navRow.setPadding(0, dp(4), 0, 0);
        fightNavButton = mainNavButton("Fight", TAB_FIGHT, R.drawable.fight_icon);
        skillsNavButton = mainNavButton("Skills", TAB_SKILLS, R.drawable.skills_icon);
        bagNavButton = mainNavButton("Bag", TAB_BAG, R.drawable.bag_icon);
        townNavButton = mainNavButton("Town", TAB_TOWN, R.drawable.town_icon);
        navRow.addView(fightNavButton, weightedWidth(1.0f));
        navRow.addView(skillsNavButton, weightedWidth(1.0f));
        navRow.addView(bagNavButton, weightedWidth(1.0f));
        navRow.addView(townNavButton, weightedWidth(1.0f));
        screen.addView(navRow);

        return screen;
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
        lastReward = areaName() + " farming started. " + enemyName() + " will keep appearing until you retreat.";
        setRewardMessage("Possible drops", selectedAreaDrops());
        addEvent("Started farming " + enemyName() + " in " + areaName() + ".");
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
        setRewardMessage("Rewards", "Defeat the Goblin Chief to earn a chest.");
        addEvent("Entered Goblin Cave I. A Cave Goblin appears.");
        saveState();
        updateViews();
        startListeningIfReady();
    }

    private void processGameSteps(int steps) {
        lastGameSteps = todaySteps;
        ageRewardBySteps(steps);
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
                addEvent(attackName() + " hits for " + damage + ".");
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

        int rawDamage = enemyMinHit() + random.nextInt(Math.max(1, enemyMaxHit() - enemyMinHit() + 1));
        int reducedDamage = Math.max(1, rawDamage * (1000 - damageReductionTenths()) / 1000);
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
            int goldReward = areaGoldReward();
            gold += goldReward;
            ArrayList<Item> foundItems = rollAreaLoot();
            if (!foundItems.isEmpty()) {
                inventory.addAll(foundItems);
                lastReward = enemyName() + " defeated.\n" + areaLootSummary(foundItems)
                        + "\nGold gained: " + goldReward;
                setRewardItems("Last reward", foundItems, goldReward, false);
                addEvent(enemyName() + " defeated. Found " + areaLootSummary(foundItems) + ".");
            } else {
                lastReward = enemyName() + " defeated.\nGold gained: " + goldReward + "\nNo item drop this time.";
                setRewardGold("Last reward", enemyName() + " defeated. No item drop.", goldReward);
                addEvent(enemyName() + " defeated. Another appears.");
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
            setRewardMessage("Chest found", "Walk 10 steps to open the boss chest.");
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

        int oldEstimate = estimatedClearSteps();
        Item foundItem = ItemCatalog.create(nextItemId++, randomGoblinCaveChestItemKey());

        inventory.add(foundItem);
        int goldReward = 18 + random.nextInt(8) + bonusGold();
        gold += goldReward;
        chestOpenedAt = System.currentTimeMillis();
        lastReward = foundItem.rarity + " - " + foundItem.name
                + "\nAdded to inventory"
                + "\n" + itemStatLine(foundItem)
                + "\nGold gained: " + goldReward
                + "\nCurrent estimate: " + oldEstimate + " steps"
                + "\nEquip it if you want to change your build.";
        setRewardItem("Chest opened", foundItem, goldReward, true);
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
        showDevTools = state.showDevTools;
        phase = state.phase;
        encounterIndex = state.encounterIndex;
        travelLeft = state.travelLeft;
        enemyHp = state.enemyHp;
        attackCharge = state.attackCharge;
        enemyAttackCharge = state.enemyAttackCharge;
        playerHp = state.playerHp;
        lastGameSteps = state.lastGameSteps;
        lastReward = state.lastReward;
        setRewardMessage("Rewards", lastReward);
        eventLog = state.eventLog;
        chestOpenedAt = state.chestOpenedAt;
        autoChestCharge = state.autoChestCharge;
        activityMode = state.activityMode;
        selectedArea = state.selectedArea;
        selectedAreaEnemy = state.selectedAreaEnemy;
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
        } else {
            normalizeLegacyStarterItems();
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
        state.showDevTools = showDevTools;
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
        state.selectedArea = selectedArea;
        state.selectedAreaEnemy = selectedAreaEnemy;
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
        selectedArea = AREA_GRASSY_FIELDS;
        selectedAreaEnemy = AREA_ENEMY_GREEN_SLIME;
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
        setRewardMessage("Rewards", "Start a fight to earn gold and gear.");
        eventLog = "Prototype reset. Ready at the cave mouth.";
        saveState();
        updateViews();
    }

    private void showPreferencesDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(12), dp(18), dp(4));

        CheckBox devToolsCheckbox = new CheckBox(this);
        devToolsCheckbox.setText("Show dev tools");
        devToolsCheckbox.setTextSize(16);
        devToolsCheckbox.setTextColor(Color.rgb(245, 224, 177));
        devToolsCheckbox.setChecked(showDevTools);
        devToolsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showDevTools = isChecked;
            saveState();
            updateViews();
        });
        panel.addView(devToolsCheckbox);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Preferences")
                .setView(panel)
                .setPositiveButton("Done", null)
                .create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(245, 224, 177));
        });
        dialog.show();
    }

    private void updateViews() {
        if (todayStepsView == null) {
            return;
        }

        boolean hasSensor = stepCounterSensor != null;
        boolean hasPermission = hasStepPermission();
        dateView.setText("Lv. 1");
        todayStepsView.setText("Steps today: " + todaySteps + "\nGold: " + gold);

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
        resetButton.setVisibility(showDevTools ? View.VISIBLE : View.GONE);
        testStepsButton.setVisibility(showDevTools ? View.VISIBLE : View.GONE);
        testBigStepsButton.setVisibility(showDevTools ? View.VISIBLE : View.GONE);

        updateEquipmentView();
        updateInventoryView();
        updateRewardView();
        updateCombatLogView();
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
            return areaName();
        }
        if (activityMode == MODE_DUNGEON || chestReady) {
            return "Goblin Cave I";
        }
        return "Fight";
    }

    private String areaName() {
        return selectedArea == AREA_GRASSY_FIELDS ? "Grassy Fields" : "Deep Forest";
    }

    private String activityStatus() {
        if (activityMode == MODE_AREA) {
            return "Area farming - " + enemyName();
        }
        if (activityMode == MODE_DUNGEON || chestReady) {
            return stageLabel() + " - " + difficultyLabel() + " - " + estimatedClearSteps() + " estimated steps";
        }
        return "Pick an activity to begin.";
    }

    private void setRewardMessage(String title, String note) {
        lastRewardTitle = title;
        lastRewardNote = note;
        lastRewardGold = 0;
        lastRewardItem = null;
        lastRewardItems.clear();
        lastRewardFromChest = false;
        rewardStepsRemaining = isImportantRewardMessage(title) ? 50 : 0;
    }

    private void setRewardGold(String title, String note, int goldReward) {
        lastRewardTitle = title;
        lastRewardNote = note;
        lastRewardGold = goldReward;
        lastRewardItem = null;
        lastRewardItems.clear();
        lastRewardFromChest = false;
        rewardStepsRemaining = 50;
    }

    private void setRewardItem(String title, Item item, int goldReward, boolean fromChest) {
        lastRewardTitle = title;
        lastRewardNote = "Added to bag";
        lastRewardGold = goldReward;
        lastRewardItem = item;
        lastRewardItems.clear();
        lastRewardItems.add(item);
        lastRewardFromChest = fromChest;
        rewardStepsRemaining = 50;
    }

    private void setRewardItems(String title, List<Item> items, int goldReward, boolean fromChest) {
        lastRewardTitle = title;
        lastRewardNote = "Added to bag";
        lastRewardGold = goldReward;
        lastRewardItem = items.isEmpty() ? null : items.get(0);
        lastRewardItems.clear();
        lastRewardItems.addAll(items);
        lastRewardFromChest = fromChest;
        rewardStepsRemaining = 50;
    }

    private boolean isImportantRewardMessage(String title) {
        return "Chest found".equals(title) || "Chest opened".equals(title);
    }

    private void ageRewardBySteps(int steps) {
        if (rewardStepsRemaining <= 0 || steps <= 0) {
            return;
        }

        rewardStepsRemaining = Math.max(0, rewardStepsRemaining - steps);
    }

    private void updateRewardView() {
        if (rewardContentView == null) {
            return;
        }

        rewardContentView.removeAllViews();
        rewardContentView.setVisibility(rewardStepsRemaining > 0 ? View.VISIBLE : View.GONE);
        if (rewardStepsRemaining <= 0) {
            return;
        }

        TextView title = text(lastRewardTitle, 18, Color.rgb(245, 224, 177), true);
        rewardContentView.addView(title);

        if (!lastRewardItems.isEmpty()) {
            for (int index = 0; index < lastRewardItems.size(); index += 1) {
                rewardContentView.addView(rewardItemCard(lastRewardItems.get(index),
                        index == 0 ? lastRewardGold : 0,
                        lastRewardFromChest));
            }
            return;
        }

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(lastRewardGold > 0 ? R.drawable.item_gold : R.drawable.chest_open);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), Color.rgb(192, 125, 44)));
        card.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(lastRewardNote, 15, Color.rgb(245, 224, 177), true));
        String goldLine = lastRewardGold > 0 ? "Gold gained: " + lastRewardGold : "No reward claimed yet.";
        copy.addView(text(goldLine, 13, Color.rgb(226, 205, 163), false));
        card.addView(copy, weightedWidth(1.0f));

        rewardContentView.addView(card, buttonLayoutParams());
    }

    private LinearLayout rewardItemCard(Item item, int goldReward, boolean fromChest) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(itemIcon(item));
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), rarityColor(item.rarity)));
        card.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(64)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(fromChest ? "Chest reward" : "Enemy drop", 12, Color.rgb(192, 157, 100), true));
        copy.addView(text(item.rarity + " " + item.name, 17, rarityColor(item.rarity), true));
        copy.addView(text(itemStatLine(item), 13, Color.rgb(226, 205, 163), false));
        copy.addView(text("Added to bag", 12, Color.rgb(139, 229, 87), true));
        if (goldReward > 0) {
            copy.addView(text("Gold gained: " + goldReward, 12, Color.rgb(245, 224, 177), false));
        }
        card.addView(copy, weightedWidth(1.0f));
        return card;
    }

    private void toggleCombatLog() {
        combatLogVisible = !combatLogVisible;
        updateCombatLogView();
    }

    private void updateCombatLogView() {
        if (combatLogContentView == null || combatLogToggleButton == null) {
            return;
        }

        combatLogToggleButton.setText(combatLogVisible ? "Hide Log" : "Show Log");
        combatLogContentView.removeAllViews();
        combatLogContentView.setVisibility(combatLogVisible ? View.VISIBLE : View.GONE);
        if (!combatLogVisible) {
            return;
        }

        TextView title = text("Combat log", 18, Color.rgb(245, 224, 177), true);
        title.setPadding(0, dp(4), 0, dp(4));
        combatLogContentView.addView(title);

        String[] lines = eventLog.split("\\n");
        int shown = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            combatLogContentView.addView(combatLogRow(trimmed));
            shown += 1;
            if (shown >= 5) {
                break;
            }
        }

        if (shown == 0) {
            combatLogContentView.addView(text("No events yet.", 13, Color.rgb(226, 205, 163), false));
        }
    }

    private LinearLayout combatLogRow(String event) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(7), dp(6), dp(7), dp(6));
        row.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));

        String tag = combatLogTag(event);
        TextView tagView = text(tag, 10, combatLogTagColor(tag), true);
        tagView.setGravity(Gravity.CENTER);
        row.addView(tagView, new LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView eventView = text(event, 13, Color.rgb(226, 205, 163), false);
        eventView.setPadding(dp(8), 0, 0, 0);
        row.addView(eventView, weightedWidth(1.0f));

        LinearLayout.LayoutParams params = buttonLayoutParams();
        params.setMargins(0, 0, 0, dp(6));
        row.setLayoutParams(params);
        return row;
    }

    private String combatLogTag(String event) {
        String lower = event.toLowerCase(Locale.US);
        if (lower.contains("opened") || lower.contains("found") || lower.contains("drop")) {
            return "LOOT";
        }
        if (lower.contains("hits") || lower.contains("attacks") || lower.contains("dodge")) {
            return lower.startsWith("arin") || lower.contains("sword") || lower.contains("punch") ? "ATK" : "DMG";
        }
        if (lower.contains("recover") || lower.contains("exhausted")) {
            return "REC";
        }
        return "INFO";
    }

    private int combatLogTagColor(String tag) {
        if ("LOOT".equals(tag)) {
            return Color.rgb(139, 229, 87);
        }
        if ("ATK".equals(tag)) {
            return Color.rgb(78, 159, 255);
        }
        if ("DMG".equals(tag)) {
            return Color.rgb(238, 104, 80);
        }
        if ("REC".equals(tag)) {
            return Color.rgb(245, 224, 177);
        }
        return Color.rgb(192, 157, 100);
    }

    private String progressText() {
        if (chestReady || phase == PHASE_COMPLETE) {
            return "Goblin Chief defeated. Chest opens at " + Math.min(autoChestCharge, 10) + " / 10 steps.";
        }
        if (!activeRun) {
            if (fightScreen == FIGHT_AREA_ENEMY) {
                return "Farm " + selectedAreaEnemyName() + " until you retreat.";
            }
            if (fightScreen == FIGHT_DUNGEONS) {
                return "3 encounters - Boss: Goblin Chief - HP combat";
            }
            return "Select an area or dungeon.";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Exhausted - recover to " + resumeHp() + " HP to keep fighting";
        }
        if (activityMode == MODE_AREA) {
            return "Farming " + enemyName() + " - enemy attacks every " + enemyAttackInterval() + " steps";
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
            return attackName() + " attacks every " + attackInterval() + " steps.";
        }
        if (phase == PHASE_EXHAUSTED) {
            return "Recovery: " + recoveryAmount() + " HP every " + recoveryStepCost() + " steps.";
        }
        return attackName() + " " + Math.min(attackCharge, attackInterval()) + " / " + attackInterval() + " steps";
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
                attackDamageLine()));
        equipmentListView.addView(equipmentRow(R.drawable.slot_armor, "Armor", equippedArmor(),
                "+" + armorHpBonus() + " HP / " + formatPercent(armorReductionTenths()) + " reduction"));
        equipmentListView.addView(equipmentRow(R.drawable.slot_boots, "Boots", equippedBoots(),
                "+" + bootsHpBonus() + " HP / " + dodgeChance() + "% dodge"));
        equipmentListView.addView(equipmentRow(R.drawable.slot_charm, "Charm", equippedCharm(),
                itemStatLine(equippedCharm())));
    }

    private LinearLayout equipmentRow(int slotIconRes, String slot, Item item, String statLine) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(8), dp(8), dp(8));
        int borderColor = item == null ? Color.rgb(126, 82, 37) : rarityColor(item.rarity);
        row.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), borderColor));

        row.addView(equipmentSlotIcon(slotIconRes, item), new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(item == null ? "Empty " + slot : item.rarity + " " + slot, 13, borderColor, true));
        copy.addView(text(item == null ? emptySlotName(slot) : item.displayName(), 17, Color.rgb(245, 224, 177), true));
        copy.addView(text(statLine, 13, Color.rgb(226, 205, 163), false));
        row.addView(copy, weightedWidth(1.0f));

        if (item != null) {
            row.setOnClickListener(v -> showItemDetails(item));
        }
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
            equippedIcon.setImageResource(itemIcon(item));
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
        int visibleCount = 0;
        LinearLayout row = null;

        for (Item item : inventory) {
            if (isEquipped(item)) {
                continue;
            }

            if (visibleCount % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.TOP);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, dp(8));
                inventoryListView.addView(row, rowParams);
            }

            LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(0, dp(118), 1.0f);
            tileParams.setMargins(dp(3), 0, dp(3), 0);
            row.addView(inventoryItemTile(item), tileParams);
            visibleCount += 1;
        }

        if (visibleCount == 0) {
            TextView emptyView = text("No unequipped items.", 14, Color.rgb(226, 205, 163), false);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(0, dp(14), 0, dp(10));
            inventoryListView.addView(emptyView);
            return;
        }

        while (visibleCount % 3 != 0 && row != null) {
            View spacer = new View(this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, dp(118), 1.0f);
            spacerParams.setMargins(dp(3), 0, dp(3), 0);
            row.addView(spacer, spacerParams);
            visibleCount += 1;
        }
    }

    private LinearLayout inventoryItemTile(Item item) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_HORIZONTAL);
        tile.setPadding(dp(6), dp(6), dp(6), dp(6));
        tile.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(itemIcon(item));
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), rarityColor(item.rarity)));
        tile.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));

        TextView rarityView = text(item.rarity, 10, rarityColor(item.rarity), true);
        rarityView.setGravity(Gravity.CENTER);
        rarityView.setPadding(0, dp(4), 0, 0);
        tile.addView(rarityView);

        TextView nameView = text(item.name, 12, Color.rgb(245, 224, 177), true);
        nameView.setGravity(Gravity.CENTER);
        nameView.setMaxLines(2);
        tile.addView(nameView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView sellView = text("Sell " + item.sellValue + "g", 11, Color.rgb(226, 205, 163), false);
        sellView.setGravity(Gravity.CENTER);
        tile.addView(sellView);

        tile.setOnClickListener(v -> showItemDetails(item));
        return tile;
    }

    private LinearLayout skillRow(int iconRes, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(9), dp(8), dp(9), dp(8));
        row.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), Color.rgb(192, 125, 44)));
        row.addView(icon, new LinearLayout.LayoutParams(dp(64), dp(64)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(label, 17, Color.rgb(245, 224, 177), true));
        copy.addView(text("COMING SOON", 12, Color.rgb(192, 157, 100), true));
        row.addView(copy, weightedWidth(1.0f));

        TextView lockView = text("LOCKED", 11, Color.rgb(226, 205, 163), true);
        lockView.setGravity(Gravity.CENTER);
        row.addView(lockView, new LinearLayout.LayoutParams(dp(70), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.setLayoutParams(buttonLayoutParams());
        return row;
    }

    private FrameLayout testAreaCard(int drawableRes, View.OnClickListener listener) {
        FrameLayout card = new FrameLayout(this);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));
        card.setClickable(true);
        card.setOnClickListener(listener);

        ImageView image = new ImageView(this);
        image.setImageResource(drawableRes);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(Color.rgb(13, 16, 12));
        card.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(140),
                Gravity.CENTER
        ));

        TextView badge = text("TEST AREA", 12, Color.rgb(245, 224, 177), true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(8), dp(4), dp(8), dp(4));
        badge.setBackground(ui.panelBackground(Color.rgb(82, 34, 28), Color.rgb(240, 174, 55)));
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT | Gravity.TOP
        );
        badgeParams.setMargins(0, dp(12), dp(12), 0);
        card.addView(badge, badgeParams);

        LinearLayout.LayoutParams params = buttonLayoutParams();
        card.setLayoutParams(params);
        return card;
    }

    private void showItemDetails(Item item) {
        boolean equipped = isEquipped(item);
        boolean equippable = isEquippable(item);
        Item current = equippedForSlot(item.slot);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(12));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(itemIcon(item));
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(5), dp(5), dp(5), dp(5));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), rarityColor(item.rarity)));
        header.addView(icon, new LinearLayout.LayoutParams(dp(74), dp(74)));

        LinearLayout titleCopy = new LinearLayout(this);
        titleCopy.setOrientation(LinearLayout.VERTICAL);
        titleCopy.setPadding(dp(12), 0, 0, 0);
        titleCopy.addView(text(item.rarity + " " + slotLabel(item.slot), 13, rarityColor(item.rarity), true));
        titleCopy.addView(text(item.displayName(), 19, Color.rgb(245, 224, 177), true));
        String location = equippable ? (equipped ? "Currently equipped" : "In inventory") : "Merchant loot";
        titleCopy.addView(text(location, 13,
                equipped ? Color.rgb(139, 229, 87) : Color.rgb(226, 205, 163), true));
        header.addView(titleCopy, weightedWidth(1.0f));
        panel.addView(header);

        addDivider(panel);
        panel.addView(ui.detailRow("Stats", itemStatLine(item)));
        if (equippable) {
            panel.addView(ui.detailRow("Equipped in this slot", current == null ? "No item equipped" : current.displayName()));
        }
        if (equippable && current != null && current.id != item.id) {
            panel.addView(ui.detailRow("Current equipped stats", itemStatLine(current)));
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        actions.setPadding(0, dp(8), 0, 0);

        Button closeButton = ui.actionButton("Close", false);
        Button equipButton = ui.actionButton(equipped ? "Unequip" : "Equip", true);

        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(0, dp(52), 1.0f);
        actionParams.setMargins(dp(4), 0, dp(4), 0);
        actions.addView(closeButton, actionParams);
        if (equippable) {
            actions.addView(equipButton, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
        }
        panel.addView(actions);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(panel)
                .create();
        closeButton.setOnClickListener(v -> dialog.dismiss());
        equipButton.setOnClickListener(v -> {
            if (equipped) {
                unequipItem(item);
            } else {
                equipItem(item.id);
            }
            dialog.dismiss();
        });
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));
            }
        });
        dialog.show();
    }

    private void addDivider(LinearLayout panel) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(80, 58, 35));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        params.setMargins(0, dp(12), 0, dp(10));
        panel.addView(divider, params);
    }

    private int itemIcon(Item item) {
        return item == null ? R.drawable.item_gold : item.iconRes;
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

    private void showAreaEnemy(int area) {
        selectedArea = area;
        selectedAreaEnemy = AREA_ENEMY_GREEN_SLIME;
        expandedAreaLootEnemy = -1;
        showFightScreen(FIGHT_AREA_ENEMY);
    }

    private void updateAreaEnemyPanel() {
        if (areaEnemyPanel == null || areaEnemyTitleView == null) {
            return;
        }

        while (areaEnemyPanel.getChildCount() > 1) {
            areaEnemyPanel.removeViewAt(1);
        }

        areaEnemyTitleView.setText(areaName());
        if (selectedAreaIsGrassyFields()) {
            areaEnemyPanel.addView(enemyChoiceCard(AREA_ENEMY_GREEN_SLIME));
            areaEnemyPanel.addView(enemyChoiceCard(AREA_ENEMY_RUNAWAY_SCARECROW));
            areaEnemyPanel.addView(enemyChoiceCard(AREA_ENEMY_RAGGED_BANDIT));
        } else {
            areaEnemyPanel.addView(enemyChoiceCard(AREA_ENEMY_GREEN_SLIME));
        }
        areaEnemyPanel.addView(backButton());
    }

    private LinearLayout enemyChoiceCard(int enemy) {
        int previousEnemy = selectedAreaEnemy;
        selectedAreaEnemy = enemy;
        String enemyName = selectedAreaEnemyName();
        String enemyStats = selectedAreaEnemyStats();
        int portrait = selectedAreaPortrait();
        String goldRange = selectedAreaGoldRange();
        selectedAreaEnemy = previousEnemy;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView portraitView = new ImageView(this);
        portraitView.setImageResource(portrait);
        portraitView.setAdjustViewBounds(true);
        portraitView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        portraitView.setPadding(dp(5), dp(5), dp(5), dp(5));
        header.addView(portraitView, new LinearLayout.LayoutParams(dp(86), dp(86)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(enemyName, 21, Color.rgb(245, 224, 177), true));
        TextView stats = text(enemyStats, 14, Color.rgb(226, 205, 163), false);
        stats.setPadding(0, dp(5), 0, 0);
        copy.addView(stats);
        header.addView(copy, weightedWidth(1.0f));
        card.addView(header);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(8), 0, 0);
        Button fightButton = actionButton("Fight", true);
        fightButton.setOnClickListener(v -> {
            selectedAreaEnemy = enemy;
            startAreaFarming();
        });
        buttons.addView(fightButton, weightedWidth(1.0f));

        Button lootButton = actionButton(expandedAreaLootEnemy == enemy ? "Hide Loot" : "Show Loot", false);
        lootButton.setOnClickListener(v -> {
            expandedAreaLootEnemy = expandedAreaLootEnemy == enemy ? -1 : enemy;
            updateAreaEnemyPanel();
        });
        buttons.addView(lootButton, weightedWidth(1.0f));
        card.addView(buttons);

        if (expandedAreaLootEnemy == enemy) {
            card.addView(enemyLootPreview(enemy, goldRange));
        }

        card.setLayoutParams(buttonLayoutParams());
        return card;
    }

    private LinearLayout enemyLootPreview(int enemy, String goldRange) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(9), dp(10), dp(9));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));
        panel.addView(text("Possible loot", 14, Color.rgb(245, 224, 177), true));

        panel.addView(lootPreviewRow(R.drawable.item_gold, "Gold: " + goldRange, "Currency"));
        for (String itemKey : selectedAreaLootKeys(enemy)) {
            Item item = ItemCatalog.create(0, itemKey);
            if (item != null) {
                panel.addView(lootPreviewRow(item));
            }
        }

        LinearLayout.LayoutParams params = buttonLayoutParams();
        params.setMargins(0, dp(8), 0, 0);
        panel.setLayoutParams(params);
        return panel;
    }

    private LinearLayout lootPreviewRow(int iconRes, String name, String detail) {
        return lootPreviewRow(iconRes, name, detail, Color.rgb(192, 125, 44));
    }

    private LinearLayout lootPreviewRow(Item item) {
        return lootPreviewRow(item.iconRes, item.name, item.rarity + " " + slotLabel(item.slot), rarityColor(item.rarity));
    }

    private LinearLayout lootPreviewRow(int iconRes, String name, String detail, int borderColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, 0);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(3), dp(3), dp(3), dp(3));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), borderColor));
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(name, 15, Color.rgb(226, 205, 163), true));
        copy.addView(text(detail, 12, Color.rgb(192, 157, 100), false));
        row.addView(copy, weightedWidth(1.0f));
        return row;
    }

    private void updateMainScreens() {
        if (fightPanel == null) {
            return;
        }

        if (activeRun || chestReady) {
            fightScreen = FIGHT_COMBAT;
        }

        if (areaEnemyTitleView != null) {
            areaEnemyTitleView.setText(areaName());
        }
        if (fightScreen == FIGHT_AREA_ENEMY) {
            updateAreaEnemyPanel();
        }
        if (fightScreen == FIGHT_DUNGEON_DETAIL) {
            fightScreen = FIGHT_DUNGEONS;
        }
        if (fightScreen == FIGHT_DUNGEONS) {
            updateDungeonsPanel();
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
        combatHeaderPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        scenePanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        actionPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        combatInfoPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);

        styleTabButton(fightNavButton, mainTab == TAB_FIGHT);
        styleTabButton(skillsNavButton, mainTab == TAB_SKILLS);
        styleTabButton(bagNavButton, mainTab == TAB_BAG);
        styleTabButton(townNavButton, mainTab == TAB_TOWN);
    }

    private void updateDungeonsPanel() {
        if (dungeonsPanel == null) {
            return;
        }

        dungeonsPanel.removeAllViews();
        dungeonsPanel.addView(sectionTitle("DUNGEONS"));
        dungeonsPanel.addView(adventureCard(R.drawable.title_goblin_cave, "Goblin Cave I",
                "Goblin Chief boss, chest rewards.", null));

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button startDungeon = actionButton("Start Dungeon", true);
        startDungeon.setOnClickListener(v -> startDungeonRun());
        buttons.addView(startDungeon, weightedWidth(1.0f));

        Button lootButton = actionButton(dungeonLootVisible ? "Hide Info" : "Show Info", false);
        lootButton.setOnClickListener(v -> {
            dungeonLootVisible = !dungeonLootVisible;
            updateDungeonsPanel();
        });
        buttons.addView(lootButton, weightedWidth(1.0f));
        dungeonsPanel.addView(buttons, buttonLayoutParams());

        if (dungeonLootVisible) {
            dungeonsPanel.addView(dungeonInfoPreview());
        }
        dungeonsPanel.addView(backButton());
    }

    private LinearLayout dungeonInfoPreview() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(9), dp(10), dp(9));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));
        panel.addView(text("Dungeon info", 14, Color.rgb(245, 224, 177), true));
        panel.addView(detailRow("Enemies", "3 Goblins | Boss: Goblin Chief"));
        panel.addView(detailRow("Estimated steps", estimatedClearSteps() + " with current gear"));
        panel.addView(detailRow("Chest", "Opens automatically 10 steps after the boss"));
        panel.addView(text("Possible chest rewards", 14, Color.rgb(245, 224, 177), true));
        panel.addView(lootPreviewRow(R.drawable.item_gold, "Gold: 18-25", "Currency"));
        for (String key : dungeonChestLootKeys()) {
            Item item = ItemCatalog.create(0, key);
            if (item != null) {
                panel.addView(lootPreviewRow(item));
            }
        }
        LinearLayout.LayoutParams params = buttonLayoutParams();
        params.setMargins(0, 0, 0, dp(10));
        panel.setLayoutParams(params);
        return panel;
    }

    private List<String> dungeonChestLootKeys() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add(ItemCatalog.IRON_SWORD);
        keys.add(ItemCatalog.CHIPPED_GOBLIN_AXE);
        keys.add(ItemCatalog.GOBLIN_TOOTH_CHARM);
        keys.add(ItemCatalog.GOBLIN_SCOUT_BOOTS);
        keys.add(ItemCatalog.DEEP_CAVE_ARMOR);
        return keys;
    }

    private String randomGoblinCaveChestItemKey() {
        int roll = random.nextInt(5);
        if (roll == 0) {
            return ItemCatalog.IRON_SWORD;
        }
        if (roll == 1) {
            return ItemCatalog.CHIPPED_GOBLIN_AXE;
        }
        if (roll == 2) {
            return ItemCatalog.GOBLIN_TOOTH_CHARM;
        }
        if (roll == 3) {
            return ItemCatalog.GOBLIN_SCOUT_BOOTS;
        }
        return ItemCatalog.DEEP_CAVE_ARMOR;
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
        setRewardMessage("Rewards", "Choose an activity to earn gold and gear.");
        addEvent("Returned to the Fight hub.");
        saveState();
        updateViews();
    }

    private String itemStatLine(Item item) {
        if (item == null) {
            return "No item equipped";
        }
        if (SLOT_WEAPON.equals(item.slot)) {
            return "+" + item.minDamage + "-" + item.maxDamage + " damage / " + item.attackSteps + " steps";
        }
        if (SLOT_ARMOR.equals(item.slot)) {
            return "+" + item.hpBonus + " HP" + optionalPercent(item.damageReduction, " reduction");
        }
        if (SLOT_BOOTS.equals(item.slot)) {
            String line = "+" + item.hpBonus + " HP";
            if (item.dodge > 0) {
                line += ", +" + item.dodge + "% dodge";
            }
            return line + optionalPercent(item.damageReduction, " reduction");
        }
        if (SLOT_LOOT.equals(item.slot)) {
            return "Sell to merchant for " + item.sellValue + "g";
        }
        String line = item.damageBonus > 0 ? "+" + item.damageBonus + " damage" : "";
        if (item.recoveryBonus > 0) {
            line += (line.isEmpty() ? "" : ", ") + "+" + item.recoveryBonus + " recovery";
        }
        if (item.bonusGold > 0) {
            line += (line.isEmpty() ? "" : ", ") + "+" + item.bonusGold + " bonus gold";
        }
        return line.isEmpty() ? "No stat bonus" : line;
    }

    private String optionalPercent(int value, String label) {
        return value <= 0 ? "" : ", " + formatPercent(value) + label;
    }

    private String formatPercent(int tenths) {
        if (tenths % 10 == 0) {
            return (tenths / 10) + "%";
        }
        return (tenths / 10) + "." + Math.abs(tenths % 10) + "%";
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
        if (SLOT_LOOT.equals(slot)) {
            return "Loot";
        }
        return "Charm";
    }

    private String emptySlotName(String slot) {
        if ("Weapon".equals(slot)) {
            return "Punch";
        }
        return "No " + slot + " equipped";
    }

    private void equipItem(int itemId) {
        Item item = findItem(itemId);
        if (item == null || !isEquippable(item)) {
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

    private void configureSystemBars() {
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    private void unequipItem(Item item) {
        if (item == null || !isEquipped(item)) {
            return;
        }

        if (item.id == equippedWeaponId) {
            equippedWeaponId = 0;
        } else if (item.id == equippedArmorId) {
            equippedArmorId = 0;
        } else if (item.id == equippedBootsId) {
            equippedBootsId = 0;
        } else if (item.id == equippedCharmId) {
            equippedCharmId = 0;
        }

        updateEquippedStats();
        playerHp = Math.min(maxPlayerHp(), playerHp);
        addEvent("Unequipped " + item.name + ".");
        saveState();
        updateViews();
    }

    private boolean isEquipped(Item item) {
        return item.id == equippedWeaponId
                || item.id == equippedArmorId
                || item.id == equippedBootsId
                || item.id == equippedCharmId;
    }

    private boolean isEquippable(Item item) {
        return item != null && !SLOT_LOOT.equals(item.slot);
    }

    private Item equippedWeapon() {
        return findItem(equippedWeaponId);
    }

    private Item equippedArmor() {
        return findItem(equippedArmorId);
    }

    private Item equippedBoots() {
        return findItem(equippedBootsId);
    }

    private Item equippedCharm() {
        return findItem(equippedCharmId);
    }

    private Item equippedForSlot(String slot) {
        if (SLOT_WEAPON.equals(slot)) {
            return equippedWeapon();
        }
        if (SLOT_ARMOR.equals(slot)) {
            return equippedArmor();
        }
        if (SLOT_BOOTS.equals(slot)) {
            return equippedBoots();
        }
        return equippedCharm();
    }

    private String attackName() {
        Item weapon = equippedWeapon();
        return weapon == null ? "Punch" : weapon.name;
    }

    private void updateEquippedStats() {
        weaponLevel = equippedWeapon() == null ? 0 : 1;
        armorLevel = equippedArmor() == null ? 0 : 1;
        bootsLevel = equippedBoots() == null ? 0 : 1;
        charmLevel = equippedCharm() == null ? 0 : 1;
    }

    private Item findItem(int id) {
        for (Item item : inventory) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }

    private void normalizeLegacyStarterItems() {
        if (findItem(1) == null) {
            inventory.add(ItemCatalog.create(1, ItemCatalog.NOVICE_SWORD));
        }
        if (findItem(2) == null) {
            inventory.add(ItemCatalog.create(2, ItemCatalog.NOVICE_TUNIC));
        }
        if (findItem(3) == null) {
            inventory.add(ItemCatalog.create(3, ItemCatalog.NOVICE_BOOTS));
        }
        if (findItem(4) == null) {
            inventory.add(ItemCatalog.create(4, ItemCatalog.NOVICE_CHARM));
        }
    }

    private void createStarterInventory(int weapon, int armor, int boots, int charm) {
        inventory.clear();
        inventory.add(ItemCatalog.create(1, ItemCatalog.NOVICE_SWORD));
        inventory.add(ItemCatalog.create(2, ItemCatalog.NOVICE_TUNIC));
        inventory.add(ItemCatalog.create(3, ItemCatalog.NOVICE_BOOTS));
        inventory.add(ItemCatalog.create(4, ItemCatalog.NOVICE_CHARM));
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
        Item weapon = equippedWeapon();
        return weapon == null ? CombatSystem.BASE_ATTACK_INTERVAL : weapon.attackSteps;
    }

    private int attackDamage() {
        return attackMaxDamage();
    }

    private int armorReductionTenths() {
        return Math.min(450, armorReductionBonus());
    }

    private int enemyMaxHp(boolean boss) {
        if (currentEnemyIsGreenSlime() && !boss) {
            return CombatSystem.greenSlimeMaxHp();
        }
        if (currentEnemyIsRunawayScarecrow() && !boss) {
            return CombatSystem.runawayScarecrowMaxHp();
        }
        if (currentEnemyIsRaggedBandit() && !boss) {
            return CombatSystem.raggedBanditMaxHp();
        }
        return CombatSystem.enemyMaxHp(boss);
    }

    private String enemyName() {
        if (currentEnemyIsGreenSlime()) {
            return CombatSystem.greenSlimeName();
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return CombatSystem.runawayScarecrowName();
        }
        if (currentEnemyIsRaggedBandit()) {
            return CombatSystem.raggedBanditName();
        }
        return CombatSystem.enemyName(isBossFight());
    }

    private int enemyMaxHit() {
        if (currentEnemyIsGreenSlime()) {
            return CombatSystem.greenSlimeMaxHit();
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return CombatSystem.runawayScarecrowMaxHit();
        }
        if (currentEnemyIsRaggedBandit()) {
            return CombatSystem.raggedBanditMaxHit();
        }
        return CombatSystem.enemyMaxHit(isBossFight());
    }

    private int enemyMinHit() {
        if (currentEnemyIsRunawayScarecrow()) {
            return 7;
        }
        if (currentEnemyIsRaggedBandit()) {
            return 4;
        }
        return 1;
    }

    private int enemyAttackInterval() {
        if (currentEnemyIsGreenSlime()) {
            return CombatSystem.greenSlimeAttackInterval();
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return CombatSystem.runawayScarecrowAttackInterval();
        }
        if (currentEnemyIsRaggedBandit()) {
            return CombatSystem.raggedBanditAttackInterval();
        }
        return CombatSystem.enemyAttackInterval(isBossFight());
    }

    private boolean currentEnemyIsGreenSlime() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_GRASSY_FIELDS
                && selectedAreaEnemy == AREA_ENEMY_GREEN_SLIME
                && !isBossFight();
    }

    private boolean currentEnemyIsRunawayScarecrow() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_GRASSY_FIELDS
                && selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW
                && !isBossFight();
    }

    private boolean currentEnemyIsRaggedBandit() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_GRASSY_FIELDS
                && selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT
                && !isBossFight();
    }

    private boolean selectedAreaIsGrassyFields() {
        return selectedArea == AREA_GRASSY_FIELDS;
    }

    private String selectedAreaEnemyName() {
        if (!selectedAreaIsGrassyFields()) {
            return "Cave Goblin";
        }
        if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            return CombatSystem.raggedBanditName();
        }
        if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            return CombatSystem.runawayScarecrowName();
        }
        return CombatSystem.greenSlimeName();
    }

    private String selectedAreaEnemyStats() {
        if (!selectedAreaIsGrassyFields()) {
            return "HP 75 | Max Hit 10 | Attack 115 steps";
        }
        if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            return "HP 80 | Damage 7-9 | Attack 110 steps | Gold 2-5";
        }
        if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            return "HP 100 | Damage 4-6 | Attack 55 steps | Gold 4-8";
        }
        return "HP 40 | Damage 1-6 | Attack 100 steps | Gold 1-3";
    }

    private int selectedAreaPortrait() {
        if (!selectedAreaIsGrassyFields()) {
            return R.drawable.portrait_cave_goblin;
        }
        if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            return R.drawable.ragged_bandit_enemy_portrait;
        }
        if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            return R.drawable.runaway_scarecrow_enemy_portrait;
        }
        return R.drawable.green_slime_enemy_portrait;
    }

    private String selectedAreaDrops() {
        return selectedAreaIsGrassyFields() ? "Gold only for now" : "Gold only";
    }

    private String selectedAreaGoldRange() {
        if (!selectedAreaIsGrassyFields()) {
            return (2 + bonusGold()) + "-" + (5 + bonusGold());
        }
        if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            return "4-8";
        }
        if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            return "2-5";
        }
        return "1-3";
    }

    private List<String> selectedAreaLootKeys(int enemy) {
        ArrayList<String> keys = new ArrayList<>();
        if (!selectedAreaIsGrassyFields()) {
            return keys;
        }
        if (enemy == AREA_ENEMY_RAGGED_BANDIT) {
            keys.add(ItemCatalog.STOLEN_TRINKET);
            keys.add(ItemCatalog.BRONZE_ARMOR);
            keys.add(ItemCatalog.IRON_ARMOR);
            keys.add(ItemCatalog.IRON_CHARM);
            return keys;
        }
        if (enemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            keys.add(ItemCatalog.NAILS);
            keys.add(ItemCatalog.BRONZE_SWORD);
            keys.add(ItemCatalog.IRON_BOOTS);
            keys.add(ItemCatalog.IRON_SWORD);
            return keys;
        }
        keys.add(ItemCatalog.GREEN_GOO);
        keys.add(ItemCatalog.BRONZE_BOOTS);
        keys.add(ItemCatalog.BRONZE_CHARM);
        keys.add(ItemCatalog.IRON_CHARM);
        return keys;
    }

    private int areaGoldReward() {
        if (selectedAreaIsGrassyFields() && selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            return 2 + random.nextInt(4);
        }
        if (selectedAreaIsGrassyFields() && selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            return 4 + random.nextInt(5);
        }
        if (selectedAreaIsGrassyFields()) {
            return 1 + random.nextInt(3);
        }
        return 2 + random.nextInt(4) + bonusGold();
    }

    private ArrayList<Item> rollAreaLoot() {
        ArrayList<Item> drops = new ArrayList<>();
        if (!selectedAreaIsGrassyFields()) {
            return drops;
        }

        String sellDropKey = null;
        String equipmentDropKey = null;
        if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
            if (random.nextInt(100) < 60) {
                sellDropKey = ItemCatalog.STOLEN_TRINKET;
            }
            int roll = random.nextInt(100);
            if (roll < 20) {
                equipmentDropKey = ItemCatalog.BRONZE_ARMOR;
            } else if (roll < 27) {
                equipmentDropKey = ItemCatalog.IRON_ARMOR;
            } else if (roll < 32) {
                equipmentDropKey = ItemCatalog.IRON_CHARM;
            }
        } else if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
            if (random.nextInt(100) < 60) {
                sellDropKey = ItemCatalog.NAILS;
            }
            int roll = random.nextInt(100);
            if (roll < 14) {
                equipmentDropKey = ItemCatalog.BRONZE_SWORD;
            } else if (roll < 24) {
                equipmentDropKey = ItemCatalog.IRON_BOOTS;
            } else if (roll < 29) {
                equipmentDropKey = ItemCatalog.IRON_SWORD;
            }
        } else {
            if (random.nextInt(100) < 60) {
                sellDropKey = ItemCatalog.GREEN_GOO;
            }
            int roll = random.nextInt(100);
            if (roll < 15) {
                equipmentDropKey = ItemCatalog.BRONZE_BOOTS;
            } else if (roll < 25) {
                equipmentDropKey = ItemCatalog.BRONZE_CHARM;
            } else if (roll < 32) {
                equipmentDropKey = ItemCatalog.IRON_CHARM;
            }
        }

        if (sellDropKey != null) {
            drops.add(ItemCatalog.create(nextItemId++, sellDropKey));
        }
        if (equipmentDropKey != null) {
            drops.add(ItemCatalog.create(nextItemId++, equipmentDropKey));
        }
        return drops;
    }

    private String areaLootSummary(List<Item> items) {
        StringBuilder builder = new StringBuilder();
        for (Item item : items) {
            if (item == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(item.name);
        }
        return builder.length() == 0 ? "no item drop" : builder.toString();
    }

    private String attackDamageLine() {
        return attackMinDamage() + "-" + attackMaxDamage() + " damage / " + attackInterval() + " steps";
    }

    private int attackMinDamage() {
        Item weapon = equippedWeapon();
        return CombatSystem.BASE_UNARMED_MIN_DAMAGE + (weapon == null ? 0 : weapon.minDamage) + damageBonus();
    }

    private int attackMaxDamage() {
        Item weapon = equippedWeapon();
        return CombatSystem.BASE_UNARMED_MAX_DAMAGE + (weapon == null ? 0 : weapon.maxDamage) + damageBonus();
    }

    private int damageBonus() {
        Item charm = equippedCharm();
        return charm == null ? 0 : charm.damageBonus;
    }

    private int armorReductionBonus() {
        int total = 0;
        Item armor = equippedArmor();
        Item boots = equippedBoots();
        if (armor != null) {
            total += armor.damageReduction;
        }
        if (boots != null) {
            total += boots.damageReduction;
        }
        return total;
    }

    private int dodgeBonus() {
        int total = 0;
        Item boots = equippedBoots();
        Item charm = equippedCharm();
        if (boots != null) {
            total += boots.dodge;
        }
        if (charm != null) {
            total += charm.dodge;
        }
        return total;
    }

    private int recoveryBonus() {
        Item charm = equippedCharm();
        return charm == null ? 0 : charm.recoveryBonus;
    }

    private int bonusGold() {
        Item charm = equippedCharm();
        return charm == null ? 0 : charm.bonusGold;
    }

    private int maxPlayerHp() {
        return CombatSystem.BASE_PLAYER_HP + armorHpBonus() + bootsHpBonus();
    }

    private int armorHpBonus() {
        Item armor = equippedArmor();
        return armor == null ? 0 : armor.hpBonus;
    }

    private int bootsHpBonus() {
        Item boots = equippedBoots();
        return boots == null ? 0 : boots.hpBonus;
    }

    private int dodgeChance() {
        return Math.min(35, dodgeBonus());
    }

    private int damageReductionTenths() {
        return armorReductionTenths();
    }

    private int playerHitDamage() {
        int min = attackMinDamage();
        int max = attackMaxDamage();
        return min + random.nextInt(Math.max(1, max - min + 1));
    }

    private int recoveryStepCost() {
        return CombatSystem.recoveryStepCost();
    }

    private int recoveryAmount() {
        return CombatSystem.BASE_RECOVERY_AMOUNT + recoveryBonus();
    }

    private int resumeHp() {
        return CombatSystem.resumeHp(maxPlayerHp());
    }

    private boolean isBossFight() {
        return encounterIndex >= ENCOUNTERS;
    }

    private int estimatedClearSteps() {
        return CombatSystem.estimatedClearSteps(attackInterval(), Math.max(1, (attackMinDamage() + attackMaxDamage()) / 2));
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
    public boolean sceneEnemyIsGreenSlime() {
        return currentEnemyIsGreenSlime();
    }

    @Override
    public boolean sceneEnemyIsRunawayScarecrow() {
        return currentEnemyIsRunawayScarecrow();
    }

    @Override
    public boolean sceneEnemyIsRaggedBandit() {
        return currentEnemyIsRaggedBandit();
    }

    @Override
    public boolean sceneChestReady() {
        return chestReady;
    }

    @Override
    public long sceneChestOpenedAt() {
        return chestOpenedAt;
    }

    @Override
    public boolean sceneUseGrassyFields() {
        return activityMode == MODE_AREA && selectedArea == AREA_GRASSY_FIELDS;
    }
}
