package com.example.stepcounterbase;

import java.util.Random;

final class LootSystem {
    private LootSystem() {
    }

    static String randomAreaDropSlot(Random random) {
        int roll = random.nextInt(4);
        if (roll == 0) {
            return GameRules.SLOT_WEAPON;
        }
        if (roll == 1) {
            return GameRules.SLOT_ARMOR;
        }
        if (roll == 2) {
            return GameRules.SLOT_BOOTS;
        }
        return GameRules.SLOT_CHARM;
    }

    static String areaDropName(String slot) {
        if (GameRules.SLOT_WEAPON.equals(slot)) {
            return "Goblin Sticker";
        }
        if (GameRules.SLOT_ARMOR.equals(slot)) {
            return "Patched Jerkin";
        }
        if (GameRules.SLOT_BOOTS.equals(slot)) {
            return "Mud Boots";
        }
        return "Green Charm";
    }
}
