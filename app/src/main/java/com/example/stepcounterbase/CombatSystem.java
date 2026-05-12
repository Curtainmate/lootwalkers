package com.example.stepcounterbase;

final class CombatSystem {
    private CombatSystem() {
    }

    static int attackInterval(int weaponLevel) {
        return Math.max(35, 70 - weaponLevel * 4);
    }

    static int attackDamage(int weaponLevel) {
        return 10 + weaponLevel * 6;
    }

    static int armorReduction(int armorLevel, int bootsLevel) {
        return Math.min(45, armorLevel * 3 + bootsLevel);
    }

    static int enemyMaxHp(boolean boss) {
        return boss ? 220 : 75;
    }

    static String enemyName(boolean boss) {
        return boss ? "Goblin Chief" : "Cave Goblin";
    }

    static int enemyMaxHit(boolean boss) {
        return boss ? 18 : 10;
    }

    static int enemyAttackInterval(boolean boss) {
        return boss ? 90 : 115;
    }

    static int maxPlayerHp(int armorLevel, int bootsLevel) {
        return 100 + armorHpBonus(armorLevel) + bootsHpBonus(bootsLevel);
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
        return 4 + charmLevel * 2;
    }

    static int resumeHp(int maxPlayerHp) {
        return Math.max(1, maxPlayerHp * 40 / 100);
    }

    static int estimatedClearSteps(int weaponLevel) {
        int attackDamage = attackDamage(weaponLevel);
        int attackInterval = attackInterval(weaponLevel);
        int combat = GameRules.ENCOUNTERS * rounds(enemyMaxHp(false), Math.max(1, attackDamage / 2)) * attackInterval;
        int boss = rounds(enemyMaxHp(true), Math.max(1, attackDamage / 2)) * attackInterval;
        return combat + boss;
    }

    private static int rounds(int hp, int damage) {
        return (hp + damage - 1) / damage;
    }
}
