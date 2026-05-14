package com.example.stepcounterbase;

final class CombatSystem {
    static final int BASE_PLAYER_HP = 100;
    static final int BASE_UNARMED_MIN_DAMAGE = 1;
    static final int BASE_UNARMED_MAX_DAMAGE = 2;
    static final int BASE_ATTACK_DAMAGE = 10;
    static final int BASE_ATTACK_INTERVAL = 70;
    static final int BASE_RECOVERY_AMOUNT = 4;

    private CombatSystem() {
    }

    static int attackInterval(int weaponLevel) {
        return Math.max(35, BASE_ATTACK_INTERVAL - weaponLevel * 4);
    }

    static int attackDamage(int weaponLevel) {
        return BASE_ATTACK_DAMAGE + weaponLevel * 6;
    }

    static int armorReduction(int armorLevel, int bootsLevel) {
        return Math.min(45, armorLevel * 3 + bootsLevel);
    }

    static int enemyMaxHp(boolean boss) {
        return boss ? 220 : 75;
    }

    static int greenSlimeMaxHp() {
        return 40;
    }

    static int runawayScarecrowMaxHp() {
        return 80;
    }

    static int raggedBanditMaxHp() {
        return 100;
    }

    static String enemyName(boolean boss) {
        return boss ? "Goblin Chief" : "Cave Goblin";
    }

    static String greenSlimeName() {
        return "Green Slime";
    }

    static String runawayScarecrowName() {
        return "Runaway Scarecrow";
    }

    static String raggedBanditName() {
        return "Ragged Bandit";
    }

    static int enemyMaxHit(boolean boss) {
        return boss ? 18 : 10;
    }

    static int greenSlimeMaxHit() {
        return 6;
    }

    static int runawayScarecrowMaxHit() {
        return 9;
    }

    static int raggedBanditMaxHit() {
        return 6;
    }

    static int enemyAttackInterval(boolean boss) {
        return boss ? 90 : 115;
    }

    static int greenSlimeAttackInterval() {
        return 100;
    }

    static int runawayScarecrowAttackInterval() {
        return 110;
    }

    static int raggedBanditAttackInterval() {
        return 55;
    }

    static int maxPlayerHp(int armorLevel, int bootsLevel) {
        return BASE_PLAYER_HP + armorHpBonus(armorLevel) + bootsHpBonus(bootsLevel);
    }

    static int armorHpBonus(int armorLevel) {
        return armorLevel * 12;
    }

    static int bootsHpBonus(int bootsLevel) {
        return bootsLevel * 6;
    }

    static int dodgeChance(int bootsLevel, int charmLevel) {
        return Math.min(35, bootsLevel * 3 + charmLevel);
    }

    static int recoveryStepCost() {
        return 25;
    }

    static int recoveryAmount(int charmLevel) {
        return BASE_RECOVERY_AMOUNT + charmLevel * 2;
    }

    static int resumeHp(int maxPlayerHp) {
        return Math.max(1, maxPlayerHp * 40 / 100);
    }

    static int estimatedClearSteps(int weaponLevel) {
        int attackDamage = attackDamage(weaponLevel);
        int attackInterval = attackInterval(weaponLevel);
        return estimatedClearSteps(attackInterval, Math.max(1, attackDamage / 2));
    }

    static int estimatedClearSteps(int attackInterval, int averageDamage) {
        int combat = GameRules.ENCOUNTERS * rounds(enemyMaxHp(false), Math.max(1, averageDamage)) * attackInterval;
        int boss = rounds(enemyMaxHp(true), Math.max(1, averageDamage)) * attackInterval;
        return combat + boss;
    }

    private static int rounds(int hp, int damage) {
        return (hp + damage - 1) / damage;
    }
}
