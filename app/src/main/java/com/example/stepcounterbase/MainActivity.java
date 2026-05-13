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
    private TextView eventLogView;
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
    private Button combatLogToggleButton;

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
    private String lastRewardTitle = "Rewards";
    private String lastRewardNote = "No loot yet. Clear Goblin Cave I to open your first chest.";
    private Item lastRewardItem = null;
    private int lastRewardGold = 0;
    private boolean lastRewardFromChest = false;
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
        lastReward = "Deep Forest farming started. Cave Goblins will keep appearing until you retreat.";
        setRewardMessage("Possible drops", "Farm Cave Goblins for gold and early gear.");
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
                setRewardItem("Last reward", foundItem, goldReward, false);
                addEvent("Cave Goblin defeated. Found " + foundItem.name + ".");
            } else {
                lastReward = "Cave Goblin defeated.\nGold gained: " + goldReward + "\nNo item drop this time.";
                setRewardGold("Last reward", "Cave Goblin defeated. No item drop.", goldReward);
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
        setRewardMessage("Rewards", "Start a fight to earn gold and gear.");
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
        resetButton.setVisibility(View.VISIBLE);

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

    private void setRewardMessage(String title, String note) {
        lastRewardTitle = title;
        lastRewardNote = note;
        lastRewardGold = 0;
        lastRewardItem = null;
        lastRewardFromChest = false;
        rewardStepsRemaining = isImportantRewardMessage(title) ? 50 : 0;
    }

    private void setRewardGold(String title, String note, int goldReward) {
        lastRewardTitle = title;
        lastRewardNote = note;
        lastRewardGold = goldReward;
        lastRewardItem = null;
        lastRewardFromChest = false;
        rewardStepsRemaining = 50;
    }

    private void setRewardItem(String title, Item item, int goldReward, boolean fromChest) {
        lastRewardTitle = title;
        lastRewardNote = "Added to bag";
        lastRewardGold = goldReward;
        lastRewardItem = item;
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

        if (lastRewardItem != null) {
            rewardContentView.addView(rewardItemCard(lastRewardItem, lastRewardGold, lastRewardFromChest));
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
        icon.setImageResource(itemIcon(item.slot));
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
        icon.setImageResource(itemIcon(item.slot));
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

        TextView levelView = text("Lv. " + item.level, 11, Color.rgb(226, 205, 163), false);
        levelView.setGravity(Gravity.CENTER);
        tile.addView(levelView);

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

    private void showItemDetails(Item item) {
        boolean equipped = isEquipped(item);
        Item current = equippedForSlot(item.slot);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(14), dp(16), dp(12));
        panel.setBackground(ui.panelBackground(Color.rgb(24, 21, 17), rarityColor(item.rarity)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageResource(itemIcon(item.slot));
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
        titleCopy.addView(text(equipped ? "Currently equipped" : "In inventory", 13,
                equipped ? Color.rgb(139, 229, 87) : Color.rgb(226, 205, 163), true));
        header.addView(titleCopy, weightedWidth(1.0f));
        panel.addView(header);

        addDivider(panel);
        panel.addView(ui.detailRow("Stats", itemStatLine(item)));
        panel.addView(ui.detailRow("Equipped in this slot", current == null ? "No item equipped" : current.displayName()));
        if (current != null && current.id != item.id) {
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
        actions.addView(equipButton, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
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
        setRewardMessage("Rewards", "Choose an activity to earn gold and gear.");
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

    private String emptySlotName(String slot) {
        if ("Weapon".equals(slot)) {
            return "Punch";
        }
        return "No " + slot + " equipped";
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
        Item weapon = equippedWeapon();
        Item armor = equippedArmor();
        Item boots = equippedBoots();
        Item charm = equippedCharm();
        weaponLevel = weapon == null ? 0 : weapon.level;
        armorLevel = armor == null ? 0 : armor.level;
        bootsLevel = boots == null ? 0 : boots.level;
        charmLevel = charm == null ? 0 : charm.level;
    }

    private int lootLevelForSlot(String slot) {
        int current;
        if (SLOT_WEAPON.equals(slot)) {
            Item item = equippedWeapon();
            current = item == null ? 0 : item.level;
        } else if (SLOT_ARMOR.equals(slot)) {
            Item item = equippedArmor();
            current = item == null ? 0 : item.level;
        } else if (SLOT_BOOTS.equals(slot)) {
            Item item = equippedBoots();
            current = item == null ? 0 : item.level;
        } else {
            Item item = equippedCharm();
            current = item == null ? 0 : item.level;
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

    private void normalizeLegacyStarterItems() {
        replaceStarterItem(1, SLOT_WEAPON, "Novice Sword");
        replaceStarterItem(2, SLOT_ARMOR, "Novice Tunic");
        replaceStarterItem(3, SLOT_BOOTS, "Novice Boots");
        replaceStarterItem(4, SLOT_CHARM, "Novice Charm");
    }

    private void replaceStarterItem(int id, String slot, String name) {
        for (int index = 0; index < inventory.size(); index += 1) {
            Item item = inventory.get(index);
            if (item.id == id && slot.equals(item.slot) && !name.equals(item.name)) {
                inventory.set(index, new Item(item.id, item.slot, name, item.rarity, item.level));
                return;
            }
        }
    }

    private void createStarterInventory(int weapon, int armor, int boots, int charm) {
        inventory.clear();
        inventory.add(new Item(1, SLOT_WEAPON, "Novice Sword", "Common", Math.max(1, weapon)));
        inventory.add(new Item(2, SLOT_ARMOR, "Novice Tunic", "Common", Math.max(1, armor)));
        inventory.add(new Item(3, SLOT_BOOTS, "Novice Boots", "Common", Math.max(1, boots)));
        inventory.add(new Item(4, SLOT_CHARM, "Novice Charm", "Common", Math.max(1, charm)));
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
