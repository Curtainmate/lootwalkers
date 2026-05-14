package com.example.stepcounterbase;

final class Item {
    final int id;
    final String key;
    final String slot;
    final String name;
    final String rarity;
    final int minDamage;
    final int maxDamage;
    final int attackSteps;
    final int hpBonus;
    final int damageBonus;
    final int damageReduction;
    final int dodge;
    final int recoveryBonus;
    final int bonusGold;
    final int sellValue;
    final int iconRes;

    Item(int id, String key, String slot, String name, String rarity, int minDamage, int maxDamage,
            int attackSteps, int hpBonus, int damageBonus, int damageReduction, int dodge,
            int recoveryBonus, int bonusGold, int sellValue, int iconRes) {
        this.id = id;
        this.key = key;
        this.slot = slot;
        this.name = name;
        this.rarity = rarity;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.attackSteps = attackSteps;
        this.hpBonus = hpBonus;
        this.damageBonus = damageBonus;
        this.damageReduction = damageReduction;
        this.dodge = dodge;
        this.recoveryBonus = recoveryBonus;
        this.bonusGold = bonusGold;
        this.sellValue = sellValue;
        this.iconRes = iconRes;
    }

    String displayName() {
        return name;
    }

    String toStorage() {
        return id + "," + key;
    }

    static Item fromStorage(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length == 2) {
            try {
                return ItemCatalog.create(Integer.parseInt(parts[0]), parts[1]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (parts.length == 5) {
            try {
                return ItemCatalog.fromLegacy(
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
        return null;
    }
}
