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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.example.stepcounterbase.GameRules.*;

public class MainActivity extends Activity implements SensorEventListener, SceneView.Model {
    private static final int REQUEST_ACTIVITY_RECOGNITION = 40;
    private static final int AREA_DEEP_FOREST = 0;
    private static final int AREA_GRASSY_FIELDS = 1;
    private static final int AREA_FORGOTTEN_GRAVEYARD = 2;
    private static final int AREA_ENEMY_GREEN_SLIME = 0;
    private static final int AREA_ENEMY_RUNAWAY_SCARECROW = 1;
    private static final int AREA_ENEMY_RAGGED_BANDIT = 2;
    private static final int DUNGEON_GOBLIN_CAVE = 0;
    private static final int DUNGEON_FORGOTTEN_CHAPEL = 1;
    private static final int TOWN_HOME = 0;
    private static final int TOWN_MERCHANT = 1;
    private static final int TOWN_ACTIVITY = 2;
    private static final int ACTIVITY_TODAY = 0;
    private static final int ACTIVITY_WEEK = 1;
    private static final int ACTIVITY_MONTH = 2;
    private static final int DAILY_REWARD_3000 = 1;
    private static final int DAILY_REWARD_6000 = 2;
    private static final int DAILY_REWARD_10000 = 4;
    private static final int MERCHANT_SELL = 0;
    private static final int MERCHANT_BUY = 1;
    private static final int BREAD_HEAL_AMOUNT = 25;
    private static final int BREAD_BUY_PRICE = 8;
    private static final int AUTO_EAT_MANUAL_PRICE = 75;
    private static final int FORGOTTEN_GRAVEYARD_MAP_PRICE = 120;
    private final Random random = new Random();

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SaveStore saveStore;
    private UiFactory ui;

    private TextView dateView;
    private TextView todayStepsView;
    private TextView goldView;
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
    private LinearLayout combatConsolePanel;
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
    private Button testNextDayButton;
    private ImageView preferencesButton;
    private Button combatLogToggleButton;
    private boolean dungeonLootVisible = false;
    private boolean showDevTools = false;
    private int townScreen = TOWN_HOME;

    private int baseline = -1;
    private int todaySteps = 0;
    private int debugDayOffset = 0;
    private int debugStepOffset = 0;
    private int lastSensorSteps = -1;
    private int todayGoldEarned = 0;
    private int todayEnemiesDefeated = 0;
    private int todayChestsOpened = 0;
    private int dailyRewardMask = 0;
    private int activityTab = ACTIVITY_TODAY;
    private boolean autoEatUnlocked = false;
    private boolean forgottenGraveyardUnlocked = false;
    private int merchantTab = MERCHANT_SELL;
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
    private int selectedDungeon = DUNGEON_GOBLIN_CAVE;
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
    private final ArrayList<DailyStats> dailyHistory = new ArrayList<>();

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
        ensureCurrentDay();
        saveState();
        updateViews();
        startListeningIfReady();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER) {
            return;
        }

        int totalSinceBoot = Math.round(event.values[0]);
        syncStepCounter(totalSinceBoot);
    }

    private void syncStepCounter(int totalSinceBoot) {
        ensureCurrentDay();

        if (baseline < 0 || totalSinceBoot < baseline) {
            baseline = totalSinceBoot;
        }

        int newTodaySteps = Math.max(0, totalSinceBoot - baseline + debugStepOffset);
        int delta = Math.max(0, newTodaySteps - todaySteps);
        todaySteps = newTodaySteps;
        lastSensorSteps = totalSinceBoot;

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

        FrameLayout portraitFrame = new FrameLayout(this);
        ImageView portraitView = new ImageView(this);
        portraitView.setImageResource(R.drawable.portrait_arin);
        portraitView.setAdjustViewBounds(true);
        portraitView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        portraitView.setPadding(dp(2), dp(2), dp(2), dp(2));
        portraitView.setBackground(ui.panelBackground(Color.rgb(36, 30, 22), Color.rgb(126, 82, 37)));
        portraitFrame.addView(portraitView, new FrameLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout.LayoutParams portraitParams = new LinearLayout.LayoutParams(dp(58), dp(58));
        portraitParams.setMargins(0, 0, dp(8), 0);
        topHud.addView(portraitFrame, portraitParams);

        LinearLayout heroHud = new LinearLayout(this);
        heroHud.setOrientation(LinearLayout.VERTICAL);
        TextView nameView = text("Arin", 23, Color.rgb(245, 224, 177), true);
        heroHud.addView(nameView);
        dateView = text("Lv. 1", 15, Color.rgb(192, 157, 100), true);
        heroHud.addView(dateView);
        topHud.addView(heroHud, weightedWidth(0.9f));

        LinearLayout statHud = new LinearLayout(this);
        statHud.setOrientation(LinearLayout.VERTICAL);
        statHud.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        todayStepsView = text("", 18, Color.rgb(245, 224, 177), true);
        goldView = text("", 18, Color.rgb(245, 224, 177), true);
        statHud.addView(topHudStatRow(R.drawable.steps_icon, todayStepsView));
        statHud.addView(topHudStatRow(R.drawable.coin_icon, goldView));
        topHud.addView(statHud, weightedWidth(1.35f));

        preferencesButton = new ImageView(this);
        preferencesButton.setImageResource(R.drawable.preference_icon);
        preferencesButton.setAdjustViewBounds(true);
        preferencesButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preferencesButton.setPadding(dp(3), dp(3), dp(3), dp(3));
        preferencesButton.setBackground(ui.panelBackground(Color.rgb(36, 30, 22), Color.rgb(126, 82, 37)));
        preferencesButton.setClickable(true);
        preferencesButton.setOnClickListener(v -> showPreferencesDialog());
        LinearLayout.LayoutParams preferencesParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        preferencesParams.gravity = Gravity.TOP;
        preferencesParams.setMargins(dp(8), dp(2), 0, 0);
        topHud.addView(preferencesButton, preferencesParams);
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
        updateAreasPanel();
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

        combatConsolePanel = darkCard();

        combatHeaderPanel = new LinearLayout(this);
        combatHeaderPanel.setOrientation(LinearLayout.VERTICAL);
        combatHeaderPanel.setPadding(0, 0, 0, dp(8));
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
        combatConsolePanel.addView(combatHeaderPanel);

        scenePanel = new LinearLayout(this);
        scenePanel.setOrientation(LinearLayout.VERTICAL);
        scenePanel.setPadding(0, 0, 0, 0);
        sceneView = new SceneView(this, this);
        scenePanel.addView(sceneView, sceneLayoutParams());
        combatConsolePanel.addView(scenePanel);

        actionPanel = new LinearLayout(this);
        actionPanel.setOrientation(LinearLayout.VERTICAL);
        actionPanel.setPadding(0, dp(8), 0, 0);
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
        combatConsolePanel.addView(actionPanel);
        fightPanel.addView(combatConsolePanel);

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
        contentRoot.addView(townPanel);

        permissionButton = actionButton("Allow step tracking", true);
        permissionButton.setOnClickListener(v -> requestStepPermission());
        contentRoot.addView(permissionButton, buttonLayoutParams());

        resetButton = actionButton("Reset prototype", false);
        resetButton.setOnClickListener(v -> resetPrototype());
        contentRoot.addView(resetButton, buttonLayoutParams());
        testNextDayButton = actionButton("Simulate next day", false);
        testNextDayButton.setOnClickListener(v -> simulateNextDay());
        contentRoot.addView(testNextDayButton, buttonLayoutParams());

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

        sensorManager.unregisterListener(this);
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
        startDungeonRun(selectedDungeon);
    }

    private void startDungeonRun(int dungeon) {
        if (!hasStepPermission()) {
            requestStepPermission();
            return;
        }

        selectedDungeon = dungeon;
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
        lastReward = dungeonName() + " started. Steps now power combat.";
        setRewardMessage("Rewards", "Defeat " + dungeonBossName() + " to earn a chest.");
        addEvent("Entered " + dungeonName() + ". " + enemyName() + " appears.");
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
        tryAutoEat();
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

    private void tryAutoEat() {
        if (!autoEatUnlocked || playerHp <= 0 || playerHp > autoEatThresholdHp()) {
            return;
        }

        Item bread = firstItemByKey(ItemCatalog.BREAD);
        if (bread == null) {
            return;
        }

        inventory.remove(bread);
        int healed = Math.min(BREAD_HEAL_AMOUNT, maxPlayerHp() - playerHp);
        if (healed <= 0) {
            return;
        }
        playerHp += healed;
        addEvent("Auto-eat used Bread and restored " + healed + " HP.");
        resumeFromFoodIfReady();
    }

    private void finishEnemy() {
        recordEnemyDefeated();
        if (activityMode == MODE_AREA) {
            int goldReward = areaGoldReward();
            gold += goldReward;
            recordGoldEarned(goldReward);
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
            lastReward = "The " + dungeonBossName() + " is defeated. The chest opens automatically after 10 more steps.";
            setRewardMessage("Chest found", "Walk 10 steps to open the boss chest.");
            addEvent(dungeonBossName() + " defeated. Chest found.");
            return;
        }

        String defeatedName = enemyName();
        encounterIndex += 1;
        phase = PHASE_COMBAT;
        enemyHp = enemyMaxHp(isBossFight());
        attackCharge = 0;
        enemyAttackCharge = 0;
        addEvent(isBossFight() ? "The " + dungeonBossName() + " enters the fight." : defeatedName + " defeated. Another appears.");
    }

    private void openChest() {
        if (!chestReady) {
            return;
        }

        int oldEstimate = estimatedClearSteps();
        Item foundItem = ItemCatalog.create(nextItemId++, randomDungeonChestItemKey());
        int goldReward = dungeonGoldReward() + bonusGold();
        gold += goldReward;
        recordGoldEarned(goldReward);
        recordChestOpened();
        chestOpenedAt = System.currentTimeMillis();
        inventory.add(foundItem);
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
            addEvent(dungeonName() + " starts again.");
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
        lastSensorSteps = state.lastSensorSteps;
        todayGoldEarned = state.todayGoldEarned;
        todayEnemiesDefeated = state.todayEnemiesDefeated;
        todayChestsOpened = state.todayChestsOpened;
        dailyRewardMask = state.dailyRewardMask;
        activityTab = state.activityTab;
        autoEatUnlocked = state.autoEatUnlocked;
        forgottenGraveyardUnlocked = state.forgottenGraveyardUnlocked;
        merchantTab = state.merchantTab;
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
        selectedDungeon = state.selectedDungeon;
        selectedArea = state.selectedArea;
        selectedAreaEnemy = state.selectedAreaEnemy;
        if (selectedArea == AREA_DEEP_FOREST || (selectedArea == AREA_FORGOTTEN_GRAVEYARD && !forgottenGraveyardUnlocked)) {
            selectedArea = AREA_GRASSY_FIELDS;
            selectedAreaEnemy = AREA_ENEMY_GREEN_SLIME;
        }
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
        dailyHistory.clear();
        dailyHistory.addAll(state.dailyHistory);
        updateTodayHistory();
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
        ensureCurrentDay();
        GameState state = new GameState();
        state.todayKey = todayKey;
        state.baseline = baseline;
        state.todaySteps = todaySteps;
        state.debugStepOffset = debugStepOffset;
        state.lastSensorSteps = lastSensorSteps;
        state.todayGoldEarned = todayGoldEarned;
        state.todayEnemiesDefeated = todayEnemiesDefeated;
        state.todayChestsOpened = todayChestsOpened;
        state.dailyRewardMask = dailyRewardMask;
        state.activityTab = activityTab;
        state.autoEatUnlocked = autoEatUnlocked;
        state.forgottenGraveyardUnlocked = forgottenGraveyardUnlocked;
        state.merchantTab = merchantTab;
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
        state.selectedDungeon = selectedDungeon;
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
        updateTodayHistory();
        state.dailyHistory.addAll(dailyHistory);
        saveStore.save(state);
    }

    private void ensureCurrentDay() {
        String currentDate = currentDateKey();
        if (!currentDate.equals(todayKey)) {
            updateTodayHistory();
            todayKey = currentDate;
            baseline = -1;
            todaySteps = 0;
            debugStepOffset = 0;
            lastSensorSteps = -1;
            lastGameSteps = 0;
            todayGoldEarned = 0;
            todayEnemiesDefeated = 0;
            todayChestsOpened = 0;
            dailyRewardMask = 0;
            updateTodayHistory();
        }
    }

    private void simulateNextDay() {
        updateTodayHistory();
        debugDayOffset += 1;
        ensureCurrentDay();
        addEvent("Dev tools: moved Activity history to " + todayKey + ".");
        saveState();
        updateViews();
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
        debugDayOffset = 0;
        debugStepOffset = 0;
        lastSensorSteps = -1;
        todayGoldEarned = 0;
        todayEnemiesDefeated = 0;
        todayChestsOpened = 0;
        dailyRewardMask = 0;
        activityTab = ACTIVITY_TODAY;
        autoEatUnlocked = false;
        forgottenGraveyardUnlocked = false;
        merchantTab = MERCHANT_SELL;
        activeRun = false;
        chestReady = false;
        activityMode = MODE_NONE;
        selectedDungeon = DUNGEON_GOBLIN_CAVE;
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
        dailyHistory.clear();
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

    private LinearLayout topHudStatRow(int iconRes, TextView valueView) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(1), dp(1), dp(1), dp(1));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
        iconParams.setMargins(0, 0, dp(7), 0);
        row.addView(icon, iconParams);

        valueView.setGravity(Gravity.RIGHT);
        valueView.setSingleLine(true);
        row.addView(valueView);
        return row;
    }

    private void updateViews() {
        if (todayStepsView == null) {
            return;
        }
        ensureCurrentDay();

        boolean hasSensor = stepCounterSensor != null;
        boolean hasPermission = hasStepPermission();
        dateView.setText("Lv. 1");
        todayStepsView.setText(String.valueOf(todaySteps));
        goldView.setText(String.valueOf(gold));

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
        testNextDayButton.setVisibility(showDevTools ? View.VISIBLE : View.GONE);
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
            systemView.setText("");
        }
    }

    private String activityTitle() {
        if (activityMode == MODE_AREA) {
            return areaName();
        }
        if (activityMode == MODE_DUNGEON || chestReady) {
            return dungeonName();
        }
        return "Fight";
    }

    private String areaName() {
        if (selectedArea == AREA_GRASSY_FIELDS) {
            return "Grassy Fields";
        }
        if (selectedArea == AREA_FORGOTTEN_GRAVEYARD) {
            return "Forgotten Graveyard";
        }
        return "Grassy Fields";
    }

    private String dungeonName() {
        return selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL ? "Forgotten Chapel" : "Goblin Cave I";
    }

    private String dungeonEnemyName() {
        return selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL ? CombatSystem.chapelAcolyteName() : CombatSystem.enemyName(false);
    }

    private String dungeonBossName() {
        return selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL ? CombatSystem.fallenPriorName() : CombatSystem.enemyName(true);
    }

    private int dungeonGoldReward() {
        if (selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return 35 + random.nextInt(14);
        }
        return 18 + random.nextInt(8);
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
            return dungeonBossName() + " defeated. Chest opens at " + Math.min(autoChestCharge, 10) + " / 10 steps.";
        }
        if (!activeRun) {
            if (fightScreen == FIGHT_AREA_ENEMY) {
                return "Farm " + selectedAreaEnemyName() + " until you retreat.";
            }
            if (fightScreen == FIGHT_DUNGEONS) {
                return "Choose a dungeon. Steps power encounters, bosses, and chests.";
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

    private FrameLayout areaBannerCard(int drawableRes, String title, String detail, String badgeText,
                                       View.OnClickListener listener) {
        FrameLayout card = new FrameLayout(this);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));
        if (listener != null) {
            card.setClickable(true);
            card.setOnClickListener(listener);
        }

        FrameLayout imageFrame = new FrameLayout(this);
        ImageView image = new ImageView(this);
        image.setImageResource(drawableRes);
        image.setAdjustViewBounds(false);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageFrame.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout labelPanel = new LinearLayout(this);
        labelPanel.setOrientation(LinearLayout.VERTICAL);
        labelPanel.setGravity(Gravity.CENTER_VERTICAL);
        labelPanel.setPadding(dp(14), 0, dp(8), 0);
        labelPanel.setBackgroundColor(Color.argb(145, 12, 9, 6));
        TextView titleView = text(title, 22, Color.rgb(245, 224, 177), true);
        TextView detailView = text(detail, 13, Color.rgb(226, 205, 163), false);
        detailView.setPadding(0, dp(3), 0, 0);
        labelPanel.addView(titleView);
        labelPanel.addView(detailView);
        imageFrame.addView(labelPanel, new FrameLayout.LayoutParams(
                dp(205),
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL
        ));

        if (badgeText != null && !badgeText.isEmpty()) {
            TextView badge = text(badgeText, 11, Color.rgb(245, 224, 177), true);
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(8), dp(4), dp(8), dp(4));
            badge.setBackground(ui.panelBackground(Color.rgb(82, 34, 28), Color.rgb(240, 174, 55)));
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT | Gravity.TOP
            );
            badgeParams.setMargins(0, dp(10), dp(10), 0);
            imageFrame.addView(badge, badgeParams);
        }

        card.addView(imageFrame, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(140),
                Gravity.CENTER
        ));
        card.setLayoutParams(buttonLayoutParams());
        return card;
    }

    private void showItemDetails(Item item) {
        boolean equipped = isEquipped(item);
        boolean equippable = isEquippable(item);
        boolean edible = isBread(item);
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
        String location = equippable ? (equipped ? "Currently equipped" : "In inventory") : itemLocationLabel(item);
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
        Button eatButton = ui.actionButton(playerHp >= maxPlayerHp() ? "Full HP" : "Eat", true);

        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(0, dp(52), 1.0f);
        actionParams.setMargins(dp(4), 0, dp(4), 0);
        actions.addView(closeButton, actionParams);
        if (equippable) {
            actions.addView(equipButton, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
        } else if (edible) {
            actions.addView(eatButton, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
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
        eatButton.setOnClickListener(v -> {
            if (eatBread(item)) {
                dialog.dismiss();
            }
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

    private String itemLocationLabel(Item item) {
        if (SLOT_CONSUMABLE.equals(item.slot)) {
            return "Food";
        }
        if (SLOT_UNLOCK.equals(item.slot)) {
            return "Merchant unlock";
        }
        return "Merchant loot";
    }

    private boolean isBread(Item item) {
        return item != null && ItemCatalog.BREAD.equals(item.key);
    }

    private boolean eatBread(Item item) {
        if (!isBread(item) || playerHp >= maxPlayerHp()) {
            return false;
        }
        int healed = Math.min(BREAD_HEAL_AMOUNT, maxPlayerHp() - playerHp);
        inventory.remove(item);
        playerHp += healed;
        addEvent("Ate Bread and restored " + healed + " HP.");
        resumeFromFoodIfReady();
        saveState();
        updateViews();
        return true;
    }

    private void resumeFromFoodIfReady() {
        if (phase == PHASE_EXHAUSTED && playerHp >= resumeHp()) {
            phase = PHASE_COMBAT;
            attackCharge = 0;
            enemyAttackCharge = 0;
            addEvent("Recovered enough to keep fighting.");
        }
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

    private void openMerchantBuy() {
        mainTab = TAB_TOWN;
        townScreen = TOWN_MERCHANT;
        merchantTab = MERCHANT_BUY;
        updateMainScreens();
        saveState();
    }

    private void showLockedGraveyardDialog() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(14), dp(18), dp(12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.map_forgotten_graveyard);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(86), dp(86));
        iconParams.gravity = Gravity.CENTER_HORIZONTAL;
        panel.addView(icon, iconParams);

        TextView message = text("You need the Forgotten Graveyard Map to enter this area.\n\nThe Merchant sells it for "
                + FORGOTTEN_GRAVEYARD_MAP_PRICE + "g.", 15, Color.rgb(226, 205, 163), false);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, dp(10), 0, dp(12));
        panel.addView(message);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button back = actionButton("Back", false);
        Button merchant = actionButton("Merchant", true);
        actions.addView(back, weightedWidth(1.0f));
        actions.addView(merchant, weightedWidth(1.0f));
        panel.addView(actions);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Area Locked")
                .setView(panel)
                .create();
        back.setOnClickListener(v -> dialog.dismiss());
        merchant.setOnClickListener(v -> {
            dialog.dismiss();
            openMerchantBuy();
        });
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));
            }
        });
        dialog.show();
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
        } else if (selectedAreaIsForgottenGraveyard()) {
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
        if (fightScreen == FIGHT_AREAS) {
            updateAreasPanel();
        }
        if (fightScreen == FIGHT_DUNGEON_DETAIL) {
            fightScreen = FIGHT_DUNGEONS;
        }
        if (fightScreen == FIGHT_DUNGEONS) {
            updateDungeonsPanel();
        }
        if (mainTab == TAB_TOWN) {
            updateTownPanel();
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
        combatConsolePanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);
        combatInfoPanel.setVisibility(showCombat ? View.VISIBLE : View.GONE);

        styleTabButton(fightNavButton, mainTab == TAB_FIGHT);
        styleTabButton(skillsNavButton, mainTab == TAB_SKILLS);
        styleTabButton(bagNavButton, mainTab == TAB_BAG);
        styleTabButton(townNavButton, mainTab == TAB_TOWN);
    }

    private void updateAreasPanel() {
        if (areasPanel == null) {
            return;
        }

        areasPanel.removeAllViews();
        areasPanel.addView(sectionTitle("AREAS"));
        areasPanel.addView(areaBannerCard(R.drawable.title_grassy_fields, "Grassy Fields",
                "Beginner fields with slimes, scarecrows, and bandits.", null,
                v -> showAreaEnemy(AREA_GRASSY_FIELDS)));
        areasPanel.addView(areaBannerCard(R.drawable.title_forgotten_graveyard, "Forgotten Graveyard",
                forgottenGraveyardUnlocked ? "Undead enemies and Moonlit Warden gear." : "Buy the map from the Merchant.",
                forgottenGraveyardUnlocked ? null : "LOCKED",
                v -> {
                    if (forgottenGraveyardUnlocked) {
                        showAreaEnemy(AREA_FORGOTTEN_GRAVEYARD);
                    } else {
                        showLockedGraveyardDialog();
                    }
                }));
        areasPanel.addView(backButton());
    }

    private void updateDungeonsPanel() {
        if (dungeonsPanel == null) {
            return;
        }

        dungeonsPanel.removeAllViews();
        dungeonsPanel.addView(sectionTitle("DUNGEONS"));
        dungeonsPanel.addView(areaBannerCard(R.drawable.title_goblin_cave, "Goblin Cave I",
                "Goblin Chief boss, chest rewards.", null, null));
        dungeonsPanel.addView(dungeonActionRow(DUNGEON_GOBLIN_CAVE));

        if (dungeonLootVisible && selectedDungeon == DUNGEON_GOBLIN_CAVE) {
            dungeonsPanel.addView(dungeonInfoPreview());
        }
        dungeonsPanel.addView(areaBannerCard(R.drawable.title_forgotten_chapel, "Forgotten Chapel",
                "3 Chapel Acolytes and the Fallen Prior.", null, null));
        dungeonsPanel.addView(dungeonActionRow(DUNGEON_FORGOTTEN_CHAPEL));
        if (dungeonLootVisible && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            dungeonsPanel.addView(dungeonInfoPreview());
        }
        dungeonsPanel.addView(backButton());
    }

    private LinearLayout dungeonActionRow(int dungeon) {
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button startDungeon = actionButton("Start Dungeon", true);
        startDungeon.setOnClickListener(v -> startDungeonRun(dungeon));
        buttons.addView(startDungeon, weightedWidth(1.0f));

        boolean showingThisDungeon = dungeonLootVisible && selectedDungeon == dungeon;
        Button lootButton = actionButton(showingThisDungeon ? "Hide Info" : "Show Info", false);
        lootButton.setOnClickListener(v -> {
            if (showingThisDungeon) {
                dungeonLootVisible = false;
            } else {
                selectedDungeon = dungeon;
                dungeonLootVisible = true;
            }
            updateDungeonsPanel();
        });
        buttons.addView(lootButton, weightedWidth(1.0f));
        buttons.setLayoutParams(buttonLayoutParams());
        return buttons;
    }

    private LinearLayout dungeonInfoPreview() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(9), dp(10), dp(9));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(80, 58, 35)));
        panel.addView(text("Dungeon info", 14, Color.rgb(245, 224, 177), true));
        panel.addView(detailRow("Enemies", selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL
                ? "3 Chapel Acolytes | Boss: Fallen Prior"
                : "3 Goblins | Boss: Goblin Chief"));
        panel.addView(detailRow("Estimated steps", estimatedClearSteps() + " with current gear"));
        panel.addView(detailRow("Chest", "Opens automatically 10 steps after the boss"));
        if (selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            panel.addView(text("Possible chest rewards", 14, Color.rgb(245, 224, 177), true));
            panel.addView(lootPreviewRow(R.drawable.item_gold, "Gold: 35-48", "Currency"));
        } else {
            panel.addView(text("Possible chest rewards", 14, Color.rgb(245, 224, 177), true));
            panel.addView(lootPreviewRow(R.drawable.item_gold, "Gold: 18-25", "Currency"));
        }
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
        if (selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            keys.add(ItemCatalog.GRAVEKEEPER_BLADE);
            keys.add(ItemCatalog.GRAVEKEEPER_VEST);
            keys.add(ItemCatalog.GRAVEKEEPER_TOKEN);
            keys.add(ItemCatalog.MOONLIT_WARDEN_SABER);
            keys.add(ItemCatalog.MOONLIT_WARDEN_MAIL);
            keys.add(ItemCatalog.MOONLIT_WARDEN_LOCKET);
            keys.add(ItemCatalog.CHAPEL_BELL_BOOTS);
            keys.add(ItemCatalog.FALLEN_PRIOR_RELIC);
            return keys;
        }
        keys.add(ItemCatalog.IRON_SWORD);
        keys.add(ItemCatalog.CHIPPED_GOBLIN_AXE);
        keys.add(ItemCatalog.GOBLIN_TOOTH_CHARM);
        keys.add(ItemCatalog.GOBLIN_SCOUT_BOOTS);
        keys.add(ItemCatalog.DEEP_CAVE_ARMOR);
        return keys;
    }

    private String randomDungeonChestItemKey() {
        if (selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return randomForgottenChapelChestItemKey();
        }
        return randomGoblinCaveChestItemKey();
    }

    private String randomForgottenChapelChestItemKey() {
        int roll = random.nextInt(100);
        if (roll < 15) {
            return ItemCatalog.GRAVEKEEPER_BLADE;
        }
        if (roll < 30) {
            return ItemCatalog.GRAVEKEEPER_VEST;
        }
        if (roll < 45) {
            return ItemCatalog.GRAVEKEEPER_TOKEN;
        }
        if (roll < 57) {
            return ItemCatalog.MOONLIT_WARDEN_SABER;
        }
        if (roll < 69) {
            return ItemCatalog.MOONLIT_WARDEN_MAIL;
        }
        if (roll < 79) {
            return ItemCatalog.MOONLIT_WARDEN_LOCKET;
        }
        if (roll < 90) {
            return ItemCatalog.CHAPEL_BELL_BOOTS;
        }
        return ItemCatalog.FALLEN_PRIOR_RELIC;
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

    private void updateTownPanel() {
        if (townPanel == null) {
            return;
        }

        townPanel.removeAllViews();
        if (townScreen == TOWN_MERCHANT) {
            updateMerchantPanel();
            return;
        }
        if (townScreen == TOWN_ACTIVITY) {
            updateActivityPanel();
            return;
        }

        townPanel.addView(text("TOWN", 26, Color.rgb(245, 224, 177), true));
        townPanel.addView(townCard(R.drawable.town_merchant_card, "Merchant", "Sell loot and spare gear.", v -> {
            townScreen = TOWN_MERCHANT;
            updateTownPanel();
        }));
        townPanel.addView(townCard(R.drawable.town_activity_card, "Activity", "Daily steps, history, and rewards.", v -> {
            townScreen = TOWN_ACTIVITY;
            updateTownPanel();
        }));
        addLockedRow(townPanel, "Bank");
        addLockedRow(townPanel, "Trainer");
    }

    private LinearLayout townCard(int drawableRes, String title, String detail, View.OnClickListener listener) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(ui.panelBackground(Color.rgb(25, 42, 24), Color.rgb(126, 82, 37)));
        card.setClickable(true);
        card.setOnClickListener(listener);

        FrameLayout imageFrame = new FrameLayout(this);
        ImageView image = new ImageView(this);
        image.setImageResource(drawableRes);
        image.setAdjustViewBounds(false);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageFrame.addView(image, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout labelPanel = new LinearLayout(this);
        labelPanel.setOrientation(LinearLayout.VERTICAL);
        labelPanel.setGravity(Gravity.CENTER_VERTICAL);
        labelPanel.setPadding(dp(14), 0, dp(8), 0);
        labelPanel.setBackgroundColor(Color.argb(150, 12, 9, 6));
        TextView titleView = text(title, 23, Color.rgb(245, 224, 177), true);
        TextView detailView = text(detail, 13, Color.rgb(226, 205, 163), false);
        detailView.setPadding(0, dp(3), 0, 0);
        labelPanel.addView(titleView);
        labelPanel.addView(detailView);
        FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(dp(190), FrameLayout.LayoutParams.MATCH_PARENT);
        labelParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        imageFrame.addView(labelPanel, labelParams);

        card.addView(imageFrame, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(160)
        ));
        card.setLayoutParams(buttonLayoutParams());
        return card;
    }

    private LinearLayout townActionRow(String title, String detail, View.OnClickListener listener) {
        Button button = actionButton(title + "\n" + detail, false);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        button.setMinHeight(dp(84));
        button.setPadding(dp(14), dp(8), dp(14), dp(8));
        button.setOnClickListener(listener);
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.addView(button, fullWidthWrapContent());
        wrapper.setLayoutParams(buttonLayoutParams());
        return wrapper;
    }

    private void updateMerchantPanel() {
        townPanel.addView(sectionTitle("MERCHANT"));
        townPanel.addView(merchantHeroPanel());
        townPanel.addView(merchantTab == MERCHANT_BUY ? merchantBuyList() : merchantSellList());
        Button backButton = actionButton("Back", false);
        backButton.setOnClickListener(v -> {
            townScreen = TOWN_HOME;
            updateTownPanel();
        });
        townPanel.addView(backButton, buttonLayoutParams());
    }

    private void updateActivityPanel() {
        updateTodayHistory();
        townPanel.addView(sectionTitle("ACTIVITY"));
        townPanel.addView(activityTabs());
        if (activityTab == ACTIVITY_WEEK) {
            townPanel.addView(activityHistoryPanel(7, "This Week"));
        } else if (activityTab == ACTIVITY_MONTH) {
            townPanel.addView(activityHistoryPanel(30, "This Month"));
        } else {
            townPanel.addView(activityTodayPanel());
        }

        Button backButton = actionButton("Back", false);
        backButton.setOnClickListener(v -> {
            townScreen = TOWN_HOME;
            updateTownPanel();
        });
        townPanel.addView(backButton, buttonLayoutParams());
    }

    private LinearLayout activityTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, 0, 0, dp(4));
        tabs.addView(activityTabButton("Today", ACTIVITY_TODAY), activityTabParams(0));
        tabs.addView(activityTabButton("Week", ACTIVITY_WEEK), activityTabParams(1));
        tabs.addView(activityTabButton("Month", ACTIVITY_MONTH), activityTabParams(2));
        tabs.setLayoutParams(buttonLayoutParams());
        return tabs;
    }

    private Button activityTabButton(String label, int tab) {
        Button button = actionButton(label, activityTab == tab);
        button.setMinHeight(dp(42));
        button.setTextSize(14);
        button.setOnClickListener(v -> {
            activityTab = tab;
            saveState();
            updateTownPanel();
        });
        return button;
    }

    private LinearLayout.LayoutParams activityTabParams(int index) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(index == 0 ? 0 : dp(4), 0, index == 2 ? 0 : dp(4), 0);
        return params;
    }

    private LinearLayout activityTodayPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(10), dp(10), dp(10), dp(10));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));

        panel.addView(activityStatRow(R.drawable.steps_icon, "Steps today", String.valueOf(todaySteps)));
        panel.addView(activityStatRow(R.drawable.recovery_icon, "Estimated calories", String.valueOf(estimatedCalories(todaySteps))));
        panel.addView(activityStatRow(R.drawable.coin_icon, "Gold earned today", todayGoldEarned + "g"));
        panel.addView(activityStatRow(R.drawable.fight_icon, "Enemies defeated", String.valueOf(todayEnemiesDefeated)));
        panel.addView(activityStatRow(R.drawable.chest_open, "Chests opened", String.valueOf(todayChestsOpened)));
        panel.addView(activityDivider());
        panel.addView(activityRewardTrack());
        panel.addView(activityDivider());
        panel.addView(activityMiniGraph(7, "This Week"));
        panel.setLayoutParams(buttonLayoutParams());
        return panel;
    }

    private LinearLayout activityHistoryPanel(int days, String title) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(126, 82, 37)));

        List<DailyStats> stats = recentDailyStats(days);
        int totalSteps = 0;
        int totalGold = 0;
        int totalEnemies = 0;
        int totalChests = 0;
        DailyStats bestDay = null;
        for (DailyStats day : stats) {
            totalSteps += day.steps;
            totalGold += day.goldEarned;
            totalEnemies += day.enemiesDefeated;
            totalChests += day.chestsOpened;
            if (bestDay == null || day.steps > bestDay.steps) {
                bestDay = day;
            }
        }

        panel.addView(activityMiniGraph(days, title));
        panel.addView(activityStatRow(R.drawable.steps_icon, "Total steps", String.valueOf(totalSteps)));
        panel.addView(activityStatRow(R.drawable.recovery_icon, "Estimated calories", String.valueOf(estimatedCalories(totalSteps))));
        panel.addView(activityStatRow(R.drawable.coin_icon, "Gold earned", totalGold + "g"));
        panel.addView(activityStatRow(R.drawable.fight_icon, "Enemies defeated", String.valueOf(totalEnemies)));
        panel.addView(activityStatRow(R.drawable.chest_open, "Chests opened", String.valueOf(totalChests)));
        panel.addView(activityStatRow(R.drawable.steps_icon, "Best step day",
                bestDay == null ? "0" : shortDateLabel(bestDay.dateKey) + " - " + bestDay.steps));
        panel.addView(activityDivider());
        TextView rowsTitle = text(days == 7 ? "Daily rows" : "Recent days", 15, Color.rgb(245, 224, 177), true);
        rowsTitle.setGravity(Gravity.CENTER);
        rowsTitle.setPadding(0, 0, 0, dp(4));
        panel.addView(rowsTitle);
        int rows = days == 7 ? 7 : Math.min(10, stats.size());
        for (int index = 0; index < rows && index < stats.size(); index += 1) {
            DailyStats day = stats.get(stats.size() - 1 - index);
            panel.addView(activityDayRow(day));
        }
        panel.setLayoutParams(buttonLayoutParams());
        return panel;
    }

    private LinearLayout activityStatRow(int iconRes, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(7), dp(5), dp(7), dp(5));

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(2), dp(2), dp(2), dp(2));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), Color.rgb(80, 58, 35)));
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.setMargins(0, 0, dp(10), 0);
        row.addView(icon, iconParams);

        row.addView(text(label, 15, Color.rgb(226, 205, 163), false), weightedWidth(1.0f));
        TextView valueView = text(value, 17, Color.rgb(245, 224, 177), true);
        valueView.setGravity(Gravity.RIGHT);
        row.addView(valueView, new LinearLayout.LayoutParams(dp(116), LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private LinearLayout activityRewardTrack() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackground(ui.panelBackground(Color.rgb(18, 16, 12), Color.rgb(80, 58, 35)));
        TextView title = text("Step Rewards", 16, Color.rgb(245, 224, 177), true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(5));
        panel.addView(title);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(10000);
        progress.setProgress(Math.min(todaySteps, 10000));
        panel.addView(progress, progressLayoutParams());

        LinearLayout rewards = new LinearLayout(this);
        rewards.setOrientation(LinearLayout.HORIZONTAL);
        rewards.addView(activityRewardButton(3000, 10, DAILY_REWARD_3000), activityRewardParams(0));
        rewards.addView(activityRewardButton(6000, 25, DAILY_REWARD_6000), activityRewardParams(1));
        rewards.addView(activityRewardButton(10000, 50, DAILY_REWARD_10000), activityRewardParams(2));
        panel.addView(rewards);
        return panel;
    }

    private Button activityRewardButton(int threshold, int rewardGold, int mask) {
        boolean claimed = (dailyRewardMask & mask) != 0;
        boolean available = todaySteps >= threshold && !claimed;
        String label;
        if (claimed) {
            label = thresholdLabel(threshold) + "\nClaimed";
        } else if (available) {
            label = thresholdLabel(threshold) + "\nClaim " + rewardGold + "g";
        } else {
            label = thresholdLabel(threshold) + "\n" + rewardGold + "g";
        }

        Button button = actionButton(label, available);
        button.setTextSize(12);
        button.setMinHeight(dp(62));
        int fill = claimed ? Color.rgb(28, 55, 26) : (available ? Color.rgb(129, 83, 31) : Color.rgb(36, 30, 22));
        int stroke = claimed ? Color.rgb(100, 157, 73) : (available ? Color.rgb(240, 174, 55) : Color.rgb(80, 58, 35));
        button.setBackground(ui.panelBackground(fill, stroke));
        button.setOnClickListener(v -> {
            if (available) {
                claimDailyReward(mask, rewardGold);
            }
        });
        return button;
    }

    private LinearLayout.LayoutParams activityRewardParams(int index) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(index == 0 ? 0 : dp(4), dp(4), index == 2 ? 0 : dp(4), 0);
        return params;
    }

    private View activityDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(55, 43, 31));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
        );
        params.setMargins(0, dp(9), 0, dp(9));
        divider.setLayoutParams(params);
        return divider;
    }

    private String thresholdLabel(int threshold) {
        return threshold >= 1000 ? (threshold / 1000) + "k steps" : threshold + " steps";
    }

    private LinearLayout activityMiniGraph(int days, String title) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));
        panel.setBackground(ui.panelBackground(Color.rgb(18, 16, 12), Color.rgb(80, 58, 35)));
        TextView titleView = text(title, 16, Color.rgb(245, 224, 177), true);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(0, 0, 0, dp(5));
        panel.addView(titleView);

        LinearLayout bars = new LinearLayout(this);
        bars.setOrientation(LinearLayout.HORIZONTAL);
        bars.setGravity(Gravity.BOTTOM);
        List<DailyStats> stats = recentDailyStats(days);
        int maxSteps = 1;
        for (DailyStats day : stats) {
            maxSteps = Math.max(maxSteps, day.steps);
        }
        int shown = days == 7 ? 7 : Math.min(10, stats.size());
        int start = days == 7 ? 0 : Math.max(0, stats.size() - shown);
        for (int index = start; index < stats.size(); index += 1) {
            bars.addView(activityBar(stats.get(index), maxSteps), weightedWidth(1.0f));
        }
        panel.addView(bars, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(days == 7 ? 98 : 88)
        ));
        return panel;
    }

    private LinearLayout activityBar(DailyStats day, int maxSteps) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        TextView value = text(shortNumber(day.steps), 10, Color.rgb(192, 157, 100), false);
        value.setGravity(Gravity.CENTER);
        column.addView(value);
        View bar = new View(this);
        bar.setBackground(ui.panelBackground(
                day.dateKey.equals(todayKey) ? Color.rgb(173, 141, 47) : Color.rgb(92, 139, 54),
                day.dateKey.equals(todayKey) ? Color.rgb(240, 174, 55) : Color.rgb(126, 168, 55)
        ));
        int height = day.steps <= 0 ? dp(4) : Math.max(dp(8), day.steps * dp(52) / Math.max(1, maxSteps));
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dp(20), height);
        barParams.setMargins(0, dp(2), 0, dp(3));
        column.addView(bar, barParams);
        TextView label = text(dayOfWeekLabel(day.dateKey), 10, Color.rgb(226, 205, 163), false);
        label.setGravity(Gravity.CENTER);
        column.addView(label);
        return column;
    }

    private LinearLayout activityDayRow(DailyStats day) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(6), dp(8), dp(6));
        row.setBackground(ui.panelBackground(Color.rgb(30, 25, 18), Color.rgb(55, 43, 31)));
        row.addView(text(shortDateLabel(day.dateKey), 13, Color.rgb(226, 205, 163), true), weightedWidth(0.8f));
        row.addView(text(day.steps + " steps", 13, Color.rgb(245, 224, 177), false), weightedWidth(1.0f));
        TextView goldText = text(day.goldEarned + "g", 13, Color.rgb(245, 224, 177), true);
        goldText.setGravity(Gravity.RIGHT);
        row.addView(goldText, weightedWidth(0.6f));
        LinearLayout.LayoutParams params = buttonLayoutParams();
        params.setMargins(0, 0, 0, dp(5));
        row.setLayoutParams(params);
        return row;
    }

    private void claimDailyReward(int mask, int rewardGold) {
        if ((dailyRewardMask & mask) != 0) {
            return;
        }
        dailyRewardMask |= mask;
        gold += rewardGold;
        recordGoldEarned(rewardGold);
        addEvent("Claimed daily activity reward: " + rewardGold + "g.");
        saveState();
        updateViews();
    }

    private LinearLayout merchantHeroPanel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(0, 0, 0, dp(8));

        ImageView merchantImage = new ImageView(this);
        merchantImage.setImageResource(R.drawable.merchant_shopkeeper);
        merchantImage.setAdjustViewBounds(true);
        merchantImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        panel.addView(merchantImage, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)
        ));

        panel.addView(merchantGoldLine());
        panel.addView(merchantTabs());
        panel.setLayoutParams(buttonLayoutParams());
        return panel;
    }

    private LinearLayout merchantGoldLine() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER_VERTICAL);
        panel.setPadding(dp(8), dp(8), dp(8), dp(6));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.coin_icon);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(26), dp(26));
        iconParams.setMargins(0, 0, dp(8), 0);
        panel.addView(icon, iconParams);
        panel.addView(text("Your Gold: " + gold + "g", 16, Color.rgb(245, 224, 177), true), weightedWidth(1.0f));
        panel.addView(text("Sell items for gold.", 13, Color.rgb(192, 157, 100), false));
        return panel;
    }

    private LinearLayout merchantTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(dp(8), 0, dp(8), 0);
        TextView sell = merchantTabLabel("Sell", merchantTab == MERCHANT_SELL);
        sell.setOnClickListener(v -> {
            merchantTab = MERCHANT_SELL;
            saveState();
            updateTownPanel();
        });
        tabs.addView(sell, weightedWidth(1.0f));
        TextView buy = merchantTabLabel("Buy", merchantTab == MERCHANT_BUY);
        buy.setOnClickListener(v -> {
            merchantTab = MERCHANT_BUY;
            saveState();
            updateTownPanel();
        });
        tabs.addView(buy, weightedWidth(1.0f));
        return tabs;
    }

    private TextView merchantTabLabel(String label, boolean selected) {
        TextView tab = text(label, 15, selected ? Color.WHITE : Color.rgb(192, 157, 100), true);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, dp(10), 0, dp(10));
        tab.setBackground(ui.panelBackground(
                selected ? Color.rgb(129, 83, 31) : Color.rgb(30, 25, 18),
                selected ? Color.rgb(240, 174, 55) : Color.rgb(80, 58, 35)
        ));
        tab.setClickable(true);
        return tab;
    }

    private LinearLayout merchantBuyList() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(4), dp(8), 0);
        list.addView(merchantSectionLabel("Consumables"));
        list.addView(merchantBuyRow(
                ItemCatalog.create(0, ItemCatalog.BREAD),
                BREAD_BUY_PRICE,
                "Restores " + BREAD_HEAL_AMOUNT + " HP",
                "Owned: " + countItemsByKey(ItemCatalog.BREAD),
                v -> buyBread()
        ));
        list.addView(merchantSectionLabel("Unlocks"));
        list.addView(merchantBuyRow(
                ItemCatalog.create(0, ItemCatalog.AUTO_EAT_MANUAL),
                AUTO_EAT_MANUAL_PRICE,
                "Use Bread below 30% HP",
                autoEatUnlocked ? "Unlocked" : "Permanent unlock",
                v -> buyAutoEatManual()
        ));
        list.addView(merchantBuyRow(
                ItemCatalog.create(0, ItemCatalog.FORGOTTEN_GRAVEYARD_MAP),
                FORGOTTEN_GRAVEYARD_MAP_PRICE,
                "Unlocks Forgotten Graveyard",
                forgottenGraveyardUnlocked ? "Unlocked" : "Area map",
                v -> buyForgottenGraveyardMap()
        ));
        return list;
    }

    private LinearLayout merchantBuyRow(Item item, int price, String detail, String note, View.OnClickListener listener) {
        LinearLayout row = merchantItemRowBase(item, item.name, detail, note + " | Price: " + price + "g");
        Button buy = actionButton(autoEatBuyButtonLabel(item, price), canBuy(item, price));
        buy.setOnClickListener(listener);
        row.addView(buy, new LinearLayout.LayoutParams(dp(84), dp(48)));
        return row;
    }

    private String autoEatBuyButtonLabel(Item item, int price) {
        if (ItemCatalog.AUTO_EAT_MANUAL.equals(item.key) && autoEatUnlocked) {
            return "Owned";
        }
        if (ItemCatalog.FORGOTTEN_GRAVEYARD_MAP.equals(item.key) && forgottenGraveyardUnlocked) {
            return "Owned";
        }
        return gold >= price ? "Buy" : price + "g";
    }

    private boolean canBuy(Item item, int price) {
        if (item == null) {
            return false;
        }
        if (ItemCatalog.AUTO_EAT_MANUAL.equals(item.key) && autoEatUnlocked) {
            return false;
        }
        if (ItemCatalog.FORGOTTEN_GRAVEYARD_MAP.equals(item.key) && forgottenGraveyardUnlocked) {
            return false;
        }
        return gold >= price;
    }

    private LinearLayout merchantSellList() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(8), dp(4), dp(8), 0);

        int rows = 0;
        boolean hasLoot = hasMerchantLoot();
        boolean hasGear = hasSellableGear();
        if (hasLoot) {
            list.addView(merchantSectionLabel("Loot"));
        }
        rows += addMerchantLootStack(list, ItemCatalog.GREEN_GOO);
        rows += addMerchantLootStack(list, ItemCatalog.NAILS);
        rows += addMerchantLootStack(list, ItemCatalog.STOLEN_TRINKET);
        rows += addMerchantLootStack(list, ItemCatalog.BONE_CHIPS);
        rows += addMerchantLootStack(list, ItemCatalog.RAT_TAIL);
        rows += addMerchantLootStack(list, ItemCatalog.FADED_ECTOPLASM);
        if (hasGear) {
            list.addView(merchantSectionLabel("Spare Gear"));
        }
        for (Item item : inventory) {
            if (isSellableGear(item)) {
                list.addView(merchantGearRow(item));
                rows++;
            }
        }

        if (rows == 0) {
            TextView empty = text("No sellable loot or spare gear yet.", 15, Color.rgb(192, 157, 100), false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(10), 0, dp(12));
            list.addView(empty);
        }
        return list;
    }

    private TextView merchantSectionLabel(String label) {
        TextView view = text(label, 13, Color.rgb(240, 174, 55), true);
        view.setGravity(Gravity.LEFT);
        view.setPadding(dp(2), dp(10), 0, dp(5));
        return view;
    }

    private boolean hasMerchantLoot() {
        return countItemsByKey(ItemCatalog.GREEN_GOO) > 0
                || countItemsByKey(ItemCatalog.NAILS) > 0
                || countItemsByKey(ItemCatalog.STOLEN_TRINKET) > 0
                || countItemsByKey(ItemCatalog.BONE_CHIPS) > 0
                || countItemsByKey(ItemCatalog.RAT_TAIL) > 0
                || countItemsByKey(ItemCatalog.FADED_ECTOPLASM) > 0;
    }

    private boolean hasSellableGear() {
        for (Item item : inventory) {
            if (isSellableGear(item)) {
                return true;
            }
        }
        return false;
    }

    private int addMerchantLootStack(LinearLayout list, String itemKey) {
        int count = countItemsByKey(itemKey);
        if (count <= 0) {
            return 0;
        }
        Item item = ItemCatalog.create(0, itemKey);
        if (item == null) {
            return 0;
        }
        list.addView(merchantLootRow(item, count));
        return 1;
    }

    private LinearLayout merchantLootRow(Item item, int count) {
        LinearLayout row = merchantItemRowBase(item, item.name, "Owned: " + count, item.sellValue + "g each");
        Button sellOne = actionButton("Sell 1", false);
        sellOne.setOnClickListener(v -> sellOneItemByKey(item.key));
        row.addView(sellOne, new LinearLayout.LayoutParams(dp(76), dp(46)));
        Button sellAll = actionButton("Sell All", true);
        sellAll.setOnClickListener(v -> sellAllItemsByKey(item.key));
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(dp(84), dp(46));
        allParams.setMargins(dp(6), 0, 0, 0);
        row.addView(sellAll, allParams);
        return row;
    }

    private LinearLayout merchantGearRow(Item item) {
        LinearLayout row = merchantItemRowBase(item, item.displayName(), itemStatLine(item), "Sell value: " + item.sellValue + "g");
        Button sell = actionButton("Sell", false);
        sell.setOnClickListener(v -> confirmSellGear(item));
        row.addView(sell, new LinearLayout.LayoutParams(dp(84), dp(46)));
        return row;
    }

    private LinearLayout merchantItemRowBase(Item item, String title, String detail, String note) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(7), dp(6), dp(7));
        row.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), Color.rgb(55, 43, 31)));
        LinearLayout.LayoutParams rowParams = buttonLayoutParams();
        rowParams.setMargins(0, 0, 0, dp(5));
        row.setLayoutParams(rowParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(item.iconRes);
        icon.setAdjustViewBounds(true);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setPadding(dp(3), dp(3), dp(3), dp(3));
        icon.setBackground(ui.panelBackground(Color.rgb(52, 42, 28), rarityColor(item.rarity)));
        row.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, dp(8), 0);
        copy.addView(text(title, 15, Color.rgb(245, 224, 177), true));
        copy.addView(text(detail, 12, Color.rgb(192, 157, 100), false));
        if (note != null && !note.isEmpty()) {
            copy.addView(text(note, 12, Color.rgb(226, 205, 163), false));
        }
        row.addView(copy, weightedWidth(1.0f));
        return row;
    }

    private int countItemsByKey(String itemKey) {
        int count = 0;
        for (Item item : inventory) {
            if (itemKey.equals(item.key)) {
                count++;
            }
        }
        return count;
    }

    private Item firstItemByKey(String itemKey) {
        for (Item item : inventory) {
            if (itemKey.equals(item.key)) {
                return item;
            }
        }
        return null;
    }

    private void buyBread() {
        if (gold < BREAD_BUY_PRICE) {
            return;
        }
        gold -= BREAD_BUY_PRICE;
        inventory.add(ItemCatalog.create(nextItemId++, ItemCatalog.BREAD));
        addEvent("Bought Bread.");
        saveState();
        updateViews();
    }

    private void buyAutoEatManual() {
        if (autoEatUnlocked || gold < AUTO_EAT_MANUAL_PRICE) {
            return;
        }
        gold -= AUTO_EAT_MANUAL_PRICE;
        autoEatUnlocked = true;
        addEvent("Auto-eat unlocked.");
        saveState();
        updateViews();
    }

    private void buyForgottenGraveyardMap() {
        if (forgottenGraveyardUnlocked || gold < FORGOTTEN_GRAVEYARD_MAP_PRICE) {
            return;
        }
        gold -= FORGOTTEN_GRAVEYARD_MAP_PRICE;
        forgottenGraveyardUnlocked = true;
        addEvent("Forgotten Graveyard unlocked.");
        saveState();
        updateViews();
    }

    private void sellOneItemByKey(String itemKey) {
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (itemKey.equals(item.key)) {
                inventory.remove(i);
                gold += item.sellValue;
                saveState();
                updateViews();
                return;
            }
        }
    }

    private void sellAllItemsByKey(String itemKey) {
        int gained = 0;
        for (int i = inventory.size() - 1; i >= 0; i--) {
            Item item = inventory.get(i);
            if (itemKey.equals(item.key)) {
                gained += item.sellValue;
                inventory.remove(i);
            }
        }
        if (gained > 0) {
            gold += gained;
            saveState();
            updateViews();
        }
    }

    private void confirmSellGear(Item item) {
        new AlertDialog.Builder(this)
                .setTitle("Sell " + item.name + "?")
                .setMessage("Sell for " + item.sellValue + "g. This cannot be undone.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Sell", (dialog, which) -> sellGear(item))
                .show();
    }

    private void sellGear(Item item) {
        if (!isSellableGear(item)) {
            return;
        }
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).id == item.id) {
                inventory.remove(i);
                gold += item.sellValue;
                saveState();
                updateViews();
                return;
            }
        }
    }

    private boolean isSellableGear(Item item) {
        return item != null
                && isEquippable(item)
                && !isEquipped(item)
                && !isNoviceItem(item)
                && item.sellValue > 0;
    }

    private boolean isNoviceItem(Item item) {
        return ItemCatalog.NOVICE_SWORD.equals(item.key)
                || ItemCatalog.NOVICE_TUNIC.equals(item.key)
                || ItemCatalog.NOVICE_BOOTS.equals(item.key)
                || ItemCatalog.NOVICE_CHARM.equals(item.key);
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
        if (SLOT_CONSUMABLE.equals(item.slot)) {
            return "Restores " + BREAD_HEAL_AMOUNT + " HP";
        }
        if (SLOT_UNLOCK.equals(item.slot)) {
            return "Unlocks automatic Bread use below 30% HP";
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
        if (SLOT_CONSUMABLE.equals(slot)) {
            return "Food";
        }
        if (SLOT_UNLOCK.equals(slot)) {
            return "Unlock";
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
        return item != null && !SLOT_LOOT.equals(item.slot)
                && !SLOT_CONSUMABLE.equals(item.slot)
                && !SLOT_UNLOCK.equals(item.slot);
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
        if (activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return boss ? CombatSystem.fallenPriorMaxHp() : CombatSystem.chapelAcolyteMaxHp();
        }
        if (currentEnemyIsGreenSlime() && !boss) {
            return CombatSystem.greenSlimeMaxHp();
        }
        if (currentEnemyIsRunawayScarecrow() && !boss) {
            return CombatSystem.runawayScarecrowMaxHp();
        }
        if (currentEnemyIsRaggedBandit() && !boss) {
            return CombatSystem.raggedBanditMaxHp();
        }
        if (currentEnemyIsRestlessBones() && !boss) {
            return CombatSystem.restlessBonesMaxHp();
        }
        if (currentEnemyIsGraveRat() && !boss) {
            return CombatSystem.graveRatMaxHp();
        }
        if (currentEnemyIsLostSpirit() && !boss) {
            return CombatSystem.lostSpiritMaxHp();
        }
        return CombatSystem.enemyMaxHp(boss);
    }

    private String enemyName() {
        if (activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return isBossFight() ? CombatSystem.fallenPriorName() : CombatSystem.chapelAcolyteName();
        }
        if (currentEnemyIsLostSpirit()) {
            return CombatSystem.lostSpiritName();
        }
        if (currentEnemyIsGraveRat()) {
            return CombatSystem.graveRatName();
        }
        if (currentEnemyIsRestlessBones()) {
            return CombatSystem.restlessBonesName();
        }
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
        if (activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return isBossFight() ? CombatSystem.fallenPriorMaxHit() : CombatSystem.chapelAcolyteMaxHit();
        }
        if (currentEnemyIsGreenSlime()) {
            return CombatSystem.greenSlimeMaxHit();
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return CombatSystem.runawayScarecrowMaxHit();
        }
        if (currentEnemyIsRaggedBandit()) {
            return CombatSystem.raggedBanditMaxHit();
        }
        if (currentEnemyIsRestlessBones()) {
            return CombatSystem.restlessBonesMaxHit();
        }
        if (currentEnemyIsGraveRat()) {
            return CombatSystem.graveRatMaxHit();
        }
        if (currentEnemyIsLostSpirit()) {
            return CombatSystem.lostSpiritMaxHit();
        }
        return CombatSystem.enemyMaxHit(isBossFight());
    }

    private int enemyMinHit() {
        if (activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return isBossFight() ? 11 : 7;
        }
        if (currentEnemyIsLostSpirit()) {
            return 8;
        }
        if (currentEnemyIsGraveRat()) {
            return 3;
        }
        if (currentEnemyIsRestlessBones()) {
            return 6;
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return 7;
        }
        if (currentEnemyIsRaggedBandit()) {
            return 4;
        }
        return 1;
    }

    private int enemyAttackInterval() {
        if (activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL) {
            return isBossFight() ? CombatSystem.fallenPriorAttackInterval() : CombatSystem.chapelAcolyteAttackInterval();
        }
        if (currentEnemyIsGreenSlime()) {
            return CombatSystem.greenSlimeAttackInterval();
        }
        if (currentEnemyIsRunawayScarecrow()) {
            return CombatSystem.runawayScarecrowAttackInterval();
        }
        if (currentEnemyIsRaggedBandit()) {
            return CombatSystem.raggedBanditAttackInterval();
        }
        if (currentEnemyIsRestlessBones()) {
            return CombatSystem.restlessBonesAttackInterval();
        }
        if (currentEnemyIsGraveRat()) {
            return CombatSystem.graveRatAttackInterval();
        }
        if (currentEnemyIsLostSpirit()) {
            return CombatSystem.lostSpiritAttackInterval();
        }
        return CombatSystem.enemyAttackInterval(isBossFight());
    }

    private boolean currentEnemyIsChapelAcolyte() {
        return activityMode == MODE_DUNGEON
                && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL
                && !isBossFight();
    }

    private boolean currentEnemyIsFallenPrior() {
        return activityMode == MODE_DUNGEON
                && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL
                && isBossFight();
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

    private boolean currentEnemyIsRestlessBones() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_FORGOTTEN_GRAVEYARD
                && selectedAreaEnemy == AREA_ENEMY_GREEN_SLIME
                && !isBossFight();
    }

    private boolean currentEnemyIsGraveRat() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_FORGOTTEN_GRAVEYARD
                && selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW
                && !isBossFight();
    }

    private boolean currentEnemyIsLostSpirit() {
        return activityMode == MODE_AREA
                && selectedArea == AREA_FORGOTTEN_GRAVEYARD
                && selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT
                && !isBossFight();
    }

    private boolean selectedAreaIsGrassyFields() {
        return selectedArea == AREA_GRASSY_FIELDS;
    }

    private boolean selectedAreaIsForgottenGraveyard() {
        return selectedArea == AREA_FORGOTTEN_GRAVEYARD;
    }

    private String selectedAreaEnemyName() {
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                return CombatSystem.lostSpiritName();
            }
            if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                return CombatSystem.graveRatName();
            }
            return CombatSystem.restlessBonesName();
        }
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
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                return "HP 170 | Damage 8-12 | Attack 105 steps | Gold 7-12";
            }
            if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                return "HP 95 | Damage 3-5 | Attack 45 steps | Gold 5-8";
            }
            return "HP 130 | Damage 6-9 | Attack 90 steps | Gold 5-9";
        }
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
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                return R.drawable.lost_spirit_enemy_portrait;
            }
            if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                return R.drawable.grave_rat_enemy_portrait;
            }
            return R.drawable.restless_bones_enemy_portrait;
        }
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
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                return "7-12";
            }
            if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                return "5-8";
            }
            return "5-9";
        }
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
        if (selectedAreaIsForgottenGraveyard()) {
            if (enemy == AREA_ENEMY_RAGGED_BANDIT) {
                keys.add(ItemCatalog.FADED_ECTOPLASM);
                keys.add(ItemCatalog.IRON_SWORD);
                keys.add(ItemCatalog.IRON_CHARM);
                keys.add(ItemCatalog.GRAVEKEEPER_BLADE);
                keys.add(ItemCatalog.GRAVEKEEPER_TOKEN);
                keys.add(ItemCatalog.MOONLIT_WARDEN_SABER);
                keys.add(ItemCatalog.MOONLIT_WARDEN_LOCKET);
                keys.add(ItemCatalog.MOONLIT_WARDEN_MAIL);
                return keys;
            }
            if (enemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                keys.add(ItemCatalog.RAT_TAIL);
                keys.add(ItemCatalog.IRON_BOOTS);
                keys.add(ItemCatalog.IRON_CHARM);
                keys.add(ItemCatalog.GRAVEKEEPER_BOOTS);
                keys.add(ItemCatalog.GRAVEKEEPER_TOKEN);
                keys.add(ItemCatalog.MOONLIT_WARDEN_BOOTS);
                return keys;
            }
            keys.add(ItemCatalog.BONE_CHIPS);
            keys.add(ItemCatalog.IRON_ARMOR);
            keys.add(ItemCatalog.IRON_BOOTS);
            keys.add(ItemCatalog.GRAVEKEEPER_VEST);
            keys.add(ItemCatalog.GRAVEKEEPER_BOOTS);
            keys.add(ItemCatalog.MOONLIT_WARDEN_MAIL);
            return keys;
        }
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
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                return 7 + random.nextInt(6);
            }
            if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                return 5 + random.nextInt(4);
            }
            return 5 + random.nextInt(5);
        }
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
        if (!selectedAreaIsGrassyFields() && !selectedAreaIsForgottenGraveyard()) {
            return drops;
        }

        String sellDropKey = null;
        String equipmentDropKey = null;
        if (selectedAreaIsForgottenGraveyard()) {
            if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
                if (random.nextInt(100) < 60) {
                    sellDropKey = ItemCatalog.FADED_ECTOPLASM;
                }
                int roll = random.nextInt(100);
                if (roll < 10) {
                    equipmentDropKey = ItemCatalog.IRON_SWORD;
                } else if (roll < 20) {
                    equipmentDropKey = ItemCatalog.IRON_CHARM;
                } else if (roll < 33) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_BLADE;
                } else if (roll < 45) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_TOKEN;
                } else if (roll < 50) {
                    equipmentDropKey = ItemCatalog.MOONLIT_WARDEN_SABER;
                } else if (roll < 55) {
                    equipmentDropKey = ItemCatalog.MOONLIT_WARDEN_LOCKET;
                } else if (roll < 58) {
                    equipmentDropKey = ItemCatalog.MOONLIT_WARDEN_MAIL;
                }
            } else if (selectedAreaEnemy == AREA_ENEMY_RUNAWAY_SCARECROW) {
                if (random.nextInt(100) < 60) {
                    sellDropKey = ItemCatalog.RAT_TAIL;
                }
                int roll = random.nextInt(100);
                if (roll < 12) {
                    equipmentDropKey = ItemCatalog.IRON_BOOTS;
                } else if (roll < 22) {
                    equipmentDropKey = ItemCatalog.IRON_CHARM;
                } else if (roll < 35) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_BOOTS;
                } else if (roll < 45) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_TOKEN;
                } else if (roll < 48) {
                    equipmentDropKey = ItemCatalog.MOONLIT_WARDEN_BOOTS;
                }
            } else {
                if (random.nextInt(100) < 60) {
                    sellDropKey = ItemCatalog.BONE_CHIPS;
                }
                int roll = random.nextInt(100);
                if (roll < 12) {
                    equipmentDropKey = ItemCatalog.IRON_ARMOR;
                } else if (roll < 22) {
                    equipmentDropKey = ItemCatalog.IRON_BOOTS;
                } else if (roll < 34) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_VEST;
                } else if (roll < 44) {
                    equipmentDropKey = ItemCatalog.GRAVEKEEPER_BOOTS;
                } else if (roll < 47) {
                    equipmentDropKey = ItemCatalog.MOONLIT_WARDEN_MAIL;
                }
            }
        } else if (selectedAreaEnemy == AREA_ENEMY_RAGGED_BANDIT) {
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

    private int autoEatThresholdHp() {
        return Math.max(1, maxPlayerHp() * 30 / 100);
    }

    private boolean isBossFight() {
        return encounterIndex >= ENCOUNTERS;
    }

    private int estimatedClearSteps() {
        int averageDamage = Math.max(1, (attackMinDamage() + attackMaxDamage()) / 2);
        int regularHp = selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL
                ? CombatSystem.chapelAcolyteMaxHp()
                : CombatSystem.enemyMaxHp(false);
        int bossHp = selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL
                ? CombatSystem.fallenPriorMaxHp()
                : CombatSystem.enemyMaxHp(true);
        return (ENCOUNTERS * roundsForEstimate(regularHp, averageDamage)
                + roundsForEstimate(bossHp, averageDamage)) * attackInterval();
    }

    private int roundsForEstimate(int hp, int averageDamage) {
        return (hp + averageDamage - 1) / averageDamage;
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

    private void recordGoldEarned(int amount) {
        if (amount <= 0) {
            return;
        }
        todayGoldEarned += amount;
        updateTodayHistory();
    }

    private void recordEnemyDefeated() {
        todayEnemiesDefeated += 1;
        updateTodayHistory();
    }

    private void recordChestOpened() {
        todayChestsOpened += 1;
        updateTodayHistory();
    }

    private void updateTodayHistory() {
        DailyStats stats = dailyStatsFor(todayKey);
        stats.steps = todaySteps;
        stats.goldEarned = todayGoldEarned;
        stats.enemiesDefeated = todayEnemiesDefeated;
        stats.chestsOpened = todayChestsOpened;
        stats.rewardMask = dailyRewardMask;
        trimDailyHistory();
    }

    private DailyStats dailyStatsFor(String dateKey) {
        for (DailyStats stats : dailyHistory) {
            if (dateKey.equals(stats.dateKey)) {
                return stats;
            }
        }
        DailyStats stats = new DailyStats(dateKey);
        dailyHistory.add(stats);
        return stats;
    }

    private void trimDailyHistory() {
        while (dailyHistory.size() > 45) {
            dailyHistory.remove(0);
        }
    }

    private List<DailyStats> recentDailyStats(int days) {
        ArrayList<DailyStats> stats = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar calendar = effectiveCalendar();
        for (int index = days - 1; index >= 0; index -= 1) {
            Calendar day = (Calendar) calendar.clone();
            day.add(Calendar.DAY_OF_YEAR, -index);
            String dateKey = format.format(day.getTime());
            DailyStats existing = findDailyStats(dateKey);
            if (existing != null) {
                stats.add(existing);
            } else {
                stats.add(new DailyStats(dateKey));
            }
        }
        return stats;
    }

    private DailyStats findDailyStats(String dateKey) {
        for (DailyStats stats : dailyHistory) {
            if (dateKey.equals(stats.dateKey)) {
                return stats;
            }
        }
        return null;
    }

    private int estimatedCalories(int steps) {
        return Math.max(0, steps / 20);
    }

    private String shortNumber(int value) {
        if (value >= 1000) {
            return (value / 1000) + "k";
        }
        return String.valueOf(value);
    }

    private String dayOfWeekLabel(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey);
            return new SimpleDateFormat("EEE", Locale.US).format(date);
        } catch (Exception ignored) {
            return "";
        }
    }

    private String shortDateLabel(String dateKey) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey);
            return new SimpleDateFormat("MMM d", Locale.US).format(date);
        } catch (Exception ignored) {
            return dateKey;
        }
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
        Calendar calendar = effectiveCalendar();
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private Calendar effectiveCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, debugDayOffset);
        return calendar;
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
    public boolean sceneEnemyIsRestlessBones() {
        return currentEnemyIsRestlessBones();
    }

    @Override
    public boolean sceneEnemyIsGraveRat() {
        return currentEnemyIsGraveRat();
    }

    @Override
    public boolean sceneEnemyIsLostSpirit() {
        return currentEnemyIsLostSpirit();
    }

    @Override
    public boolean sceneEnemyIsChapelAcolyte() {
        return currentEnemyIsChapelAcolyte();
    }

    @Override
    public boolean sceneEnemyIsFallenPrior() {
        return currentEnemyIsFallenPrior();
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

    @Override
    public boolean sceneUseForgottenGraveyard() {
        return activityMode == MODE_AREA && selectedArea == AREA_FORGOTTEN_GRAVEYARD;
    }

    @Override
    public boolean sceneUseForgottenChapel() {
        return activityMode == MODE_DUNGEON && selectedDungeon == DUNGEON_FORGOTTEN_CHAPEL;
    }
}
