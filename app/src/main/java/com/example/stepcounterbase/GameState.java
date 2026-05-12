package com.example.stepcounterbase;

import java.util.ArrayList;

final class GameState {
    String todayKey = "";
    int baseline = -1;
    int todaySteps = 0;
    int debugStepOffset = 0;

    boolean activeRun = false;
    boolean chestReady = false;
    int phase = 0;
    int encounterIndex = 0;
    int travelLeft = 0;
    int enemyHp = 75;
    int attackCharge = 0;
    int enemyAttackCharge = 0;
    int playerHp = 100;
    int lastGameSteps = 0;
    int gold = 0;
    int weaponLevel = 1;
    int armorLevel = 1;
    int bootsLevel = 1;
    int charmLevel = 1;
    int nextItemId = 5;
    int equippedWeaponId = 1;
    int equippedArmorId = 2;
    int equippedBootsId = 3;
    int equippedCharmId = 4;
    int mainTab = 0;
    int fightScreen = 0;
    int activityMode = 0;
    int autoChestCharge = 0;
    long chestOpenedAt = 0L;
    String lastReward = "";
    String eventLog = "";
    final ArrayList<Item> inventory = new ArrayList<>();
}
