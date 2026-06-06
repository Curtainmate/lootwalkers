package com.example.stepcounterbase;

import static com.example.stepcounterbase.GameRules.SLOT_ARMOR;
import static com.example.stepcounterbase.GameRules.SLOT_BOOTS;
import static com.example.stepcounterbase.GameRules.SLOT_CHARM;
import static com.example.stepcounterbase.GameRules.SLOT_CONSUMABLE;
import static com.example.stepcounterbase.GameRules.SLOT_LOOT;
import static com.example.stepcounterbase.GameRules.SLOT_UNLOCK;
import static com.example.stepcounterbase.GameRules.SLOT_WEAPON;

final class ItemCatalog {
    static final String NOVICE_SWORD = "novice_sword";
    static final String NOVICE_TUNIC = "novice_tunic";
    static final String NOVICE_BOOTS = "novice_boots";
    static final String NOVICE_CHARM = "novice_charm";
    static final String BRONZE_SWORD = "bronze_sword";
    static final String BRONZE_ARMOR = "bronze_armor";
    static final String BRONZE_BOOTS = "bronze_boots";
    static final String BRONZE_CHARM = "bronze_charm";
    static final String IRON_SWORD = "iron_sword";
    static final String IRON_ARMOR = "iron_armor";
    static final String IRON_BOOTS = "iron_boots";
    static final String IRON_CHARM = "iron_charm";
    static final String CHIPPED_GOBLIN_AXE = "chipped_goblin_axe";
    static final String DEEP_CAVE_ARMOR = "deep_cave_armor";
    static final String GOBLIN_SCOUT_BOOTS = "goblin_scout_boots";
    static final String GOBLIN_TOOTH_CHARM = "goblin_tooth_charm";
    static final String GRAVEKEEPER_BLADE = "gravekeeper_blade";
    static final String GRAVEKEEPER_VEST = "gravekeeper_vest";
    static final String GRAVEKEEPER_BOOTS = "gravekeeper_boots";
    static final String GRAVEKEEPER_TOKEN = "gravekeeper_token";
    static final String MOONLIT_WARDEN_SABER = "moonlit_warden_saber";
    static final String MOONLIT_WARDEN_MAIL = "moonlit_warden_mail";
    static final String MOONLIT_WARDEN_BOOTS = "moonlit_warden_boots";
    static final String MOONLIT_WARDEN_LOCKET = "moonlit_warden_locket";
    static final String CHAPEL_BELL_BOOTS = "chapel_bell_boots";
    static final String FALLEN_PRIOR_RELIC = "fallen_prior_relic";
    static final String GREEN_GOO = "green_goo";
    static final String NAILS = "nails";
    static final String STOLEN_TRINKET = "stolen_trinket";
    static final String BONE_CHIPS = "bone_chips";
    static final String RAT_TAIL = "rat_tail";
    static final String FADED_ECTOPLASM = "faded_ectoplasm";
    static final String BREAD = "bread";
    static final String AUTO_EAT_MANUAL = "auto_eat_manual";
    static final String FORGOTTEN_GRAVEYARD_MAP = "forgotten_graveyard_map";

    private ItemCatalog() {
    }

    static Item create(int id, String key) {
        if (NOVICE_SWORD.equals(key)) {
            return weapon(id, key, "Novice Sword", "Common", 5, 6, 70, 0, 1, R.drawable.novice_sword);
        }
        if (NOVICE_TUNIC.equals(key)) {
            return armor(id, key, "Novice Tunic", "Common", 10, 0, 1, R.drawable.novice_shirt);
        }
        if (NOVICE_BOOTS.equals(key)) {
            return boots(id, key, "Novice Boots", "Common", 5, 0, 0, 1, R.drawable.novice_boots);
        }
        if (NOVICE_CHARM.equals(key)) {
            return charm(id, key, "Novice Charm", "Common", 2, 0, 0, 0, 1, R.drawable.novice_amulet);
        }
        if (BRONZE_SWORD.equals(key)) {
            return weapon(id, key, "Bronze Sword", "Common", 7, 9, 65, 0, 6, R.drawable.bronze_sword);
        }
        if (BRONZE_ARMOR.equals(key)) {
            return armor(id, key, "Bronze Armor", "Common", 18, 10, 7, R.drawable.bronze_armor);
        }
        if (BRONZE_BOOTS.equals(key)) {
            return boots(id, key, "Bronze Boots", "Common", 8, 0, 1, 5, R.drawable.bronze_boots);
        }
        if (BRONZE_CHARM.equals(key)) {
            return charm(id, key, "Bronze Charm", "Common", 3, 1, 0, 0, 5, R.drawable.bronze_charm);
        }
        if (IRON_SWORD.equals(key)) {
            return weapon(id, key, "Iron Sword", "Common", 10, 13, 60, 0, 14, R.drawable.iron_sword);
        }
        if (IRON_ARMOR.equals(key)) {
            return armor(id, key, "Iron Armor", "Common", 30, 25, 16, R.drawable.iron_armor);
        }
        if (IRON_BOOTS.equals(key)) {
            return boots(id, key, "Iron Boots", "Common", 14, 10, 2, 12, R.drawable.iron_boots);
        }
        if (IRON_CHARM.equals(key)) {
            return charm(id, key, "Iron Charm", "Common", 5, 2, 0, 0, 12, R.drawable.iron_charm);
        }
        if (CHIPPED_GOBLIN_AXE.equals(key)) {
            return weapon(id, key, "Chipped Goblin Axe", "Uncommon", 17, 22, 85, 0, 24, R.drawable.chipped_goblin_axe);
        }
        if (DEEP_CAVE_ARMOR.equals(key)) {
            return armor(id, key, "Deep Cave Armor", "Rare", 48, 45, 45, R.drawable.deep_cave_armor);
        }
        if (GOBLIN_SCOUT_BOOTS.equals(key)) {
            return boots(id, key, "Goblin Scout Boots", "Uncommon", 15, 10, 4, 22, R.drawable.goblin_scout_boots);
        }
        if (GOBLIN_TOOTH_CHARM.equals(key)) {
            return charm(id, key, "Goblin Tooth Charm", "Uncommon", 7, 0, 2, 0, 23, R.drawable.goblin_tooth_charm);
        }
        if (GRAVEKEEPER_BLADE.equals(key)) {
            return weapon(id, key, "Gravekeeper Blade", "Uncommon", 14, 18, 58, 0, 26, R.drawable.gravekeeper_blade);
        }
        if (GRAVEKEEPER_VEST.equals(key)) {
            return armor(id, key, "Gravekeeper Vest", "Uncommon", 38, 35, 28, R.drawable.gravekeeper_vest);
        }
        if (GRAVEKEEPER_BOOTS.equals(key)) {
            return boots(id, key, "Gravekeeper Boots", "Uncommon", 18, 20, 3, 24, R.drawable.gravekeeper_boots);
        }
        if (GRAVEKEEPER_TOKEN.equals(key)) {
            return charm(id, key, "Gravekeeper Token", "Uncommon", 6, 3, 1, 0, 25, R.drawable.gravekeeper_token);
        }
        if (MOONLIT_WARDEN_SABER.equals(key)) {
            return weapon(id, key, "Moonlit Warden Saber", "Rare", 19, 25, 62, 0, 55, R.drawable.moonlit_warden_saber);
        }
        if (MOONLIT_WARDEN_MAIL.equals(key)) {
            return armor(id, key, "Moonlit Warden Mail", "Rare", 58, 55, 62, R.drawable.moonlit_warden_mail);
        }
        if (MOONLIT_WARDEN_BOOTS.equals(key)) {
            return boots(id, key, "Moonlit Warden Boots", "Rare", 26, 30, 5, 52, R.drawable.moonlit_warden_boots);
        }
        if (MOONLIT_WARDEN_LOCKET.equals(key)) {
            return charm(id, key, "Moonlit Warden Locket", "Rare", 9, 4, 2, 0, 54, R.drawable.moonlit_warden_locket);
        }
        if (CHAPEL_BELL_BOOTS.equals(key)) {
            return boots(id, key, "Chapel Bell Boots", "Rare", 34, 40, 6, 75, R.drawable.chapel_bell_boots);
        }
        if (FALLEN_PRIOR_RELIC.equals(key)) {
            return new Item(id, key, SLOT_CHARM, "Fallen Prior's Relic", "Epic",
                    0, 0, 0, 0, 12, 10, 3, 6, 0, 120, R.drawable.fallen_prior_relic);
        }
        if (GREEN_GOO.equals(key)) {
            return loot(id, key, "Green Goo", "Common", 3, R.drawable.item_green_goo);
        }
        if (NAILS.equals(key)) {
            return loot(id, key, "Nails", "Common", 5, R.drawable.item_nails);
        }
        if (STOLEN_TRINKET.equals(key)) {
            return loot(id, key, "Stolen Trinket", "Common", 7, R.drawable.item_stolen_trinket);
        }
        if (BONE_CHIPS.equals(key)) {
            return loot(id, key, "Bone Chips", "Common", 9, R.drawable.item_bone_chips);
        }
        if (RAT_TAIL.equals(key)) {
            return loot(id, key, "Rat Tail", "Common", 9, R.drawable.item_rat_tail);
        }
        if (FADED_ECTOPLASM.equals(key)) {
            return loot(id, key, "Faded Ectoplasm", "Common", 12, R.drawable.item_faded_ectoplasm);
        }
        if (BREAD.equals(key)) {
            return consumable(id, key, "Bread", "Common", 2, R.drawable.item_bread);
        }
        if (AUTO_EAT_MANUAL.equals(key)) {
            return unlock(id, key, "Auto-eat Manual", "Uncommon", 0, R.drawable.item_auto_eat_manual);
        }
        if (FORGOTTEN_GRAVEYARD_MAP.equals(key)) {
            return unlock(id, key, "Forgotten Graveyard Map", "Uncommon", 0, R.drawable.map_forgotten_graveyard);
        }
        return null;
    }

    static Item fromLegacy(int id, String slot, String name, String rarity, int level) {
        if ("Novice Sword".equals(name) || ("Rusty Sword".equals(name) && SLOT_WEAPON.equals(slot))) {
            return create(id, NOVICE_SWORD);
        }
        if ("Novice Tunic".equals(name) || ("Cloth Tunic".equals(name) && SLOT_ARMOR.equals(slot))) {
            return create(id, NOVICE_TUNIC);
        }
        if ("Novice Boots".equals(name) || ("Worn Boots".equals(name) && SLOT_BOOTS.equals(slot))) {
            return create(id, NOVICE_BOOTS);
        }
        if ("Novice Charm".equals(name)) {
            return create(id, NOVICE_CHARM);
        }
        if ("Chipped Goblin Axe".equals(name)) {
            return create(id, CHIPPED_GOBLIN_AXE);
        }
        if ("Deep Cave Armor".equals(name)) {
            return create(id, DEEP_CAVE_ARMOR);
        }
        if ("Goblin Scout Boots".equals(name)) {
            return create(id, GOBLIN_SCOUT_BOOTS);
        }
        if ("Goblin Tooth Charm".equals(name)) {
            return create(id, GOBLIN_TOOTH_CHARM);
        }
        if ("Gravekeeper Blade".equals(name)) {
            return create(id, GRAVEKEEPER_BLADE);
        }
        if ("Gravekeeper Vest".equals(name)) {
            return create(id, GRAVEKEEPER_VEST);
        }
        if ("Gravekeeper Boots".equals(name)) {
            return create(id, GRAVEKEEPER_BOOTS);
        }
        if ("Gravekeeper Token".equals(name)) {
            return create(id, GRAVEKEEPER_TOKEN);
        }
        if ("Moonlit Warden Saber".equals(name)) {
            return create(id, MOONLIT_WARDEN_SABER);
        }
        if ("Moonlit Warden Mail".equals(name)) {
            return create(id, MOONLIT_WARDEN_MAIL);
        }
        if ("Moonlit Warden Boots".equals(name)) {
            return create(id, MOONLIT_WARDEN_BOOTS);
        }
        if ("Moonlit Warden Locket".equals(name)) {
            return create(id, MOONLIT_WARDEN_LOCKET);
        }
        if ("Chapel Bell Boots".equals(name)) {
            return create(id, CHAPEL_BELL_BOOTS);
        }
        if ("Fallen Prior's Relic".equals(name)) {
            return create(id, FALLEN_PRIOR_RELIC);
        }
        if ("Green Goo".equals(name)) {
            return create(id, GREEN_GOO);
        }
        if ("Nails".equals(name)) {
            return create(id, NAILS);
        }
        if ("Stolen Trinket".equals(name)) {
            return create(id, STOLEN_TRINKET);
        }
        if ("Bone Chips".equals(name)) {
            return create(id, BONE_CHIPS);
        }
        if ("Rat Tail".equals(name)) {
            return create(id, RAT_TAIL);
        }
        if ("Faded Ectoplasm".equals(name)) {
            return create(id, FADED_ECTOPLASM);
        }
        if ("Bread".equals(name)) {
            return create(id, BREAD);
        }
        if ("Auto-eat Manual".equals(name)) {
            return create(id, AUTO_EAT_MANUAL);
        }
        if ("Forgotten Graveyard Map".equals(name)) {
            return create(id, FORGOTTEN_GRAVEYARD_MAP);
        }
        return null;
    }

    private static Item weapon(int id, String key, String name, String rarity, int minDamage, int maxDamage,
            int attackSteps, int damageBonus, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_WEAPON, name, rarity, minDamage, maxDamage, attackSteps, 0,
                damageBonus, 0, 0, 0, 0, sellValue, iconRes);
    }

    private static Item armor(int id, String key, String name, String rarity, int hpBonus, int damageReduction,
            int sellValue, int iconRes) {
        return new Item(id, key, SLOT_ARMOR, name, rarity, 0, 0, 0, hpBonus, 0,
                damageReduction, 0, 0, 0, sellValue, iconRes);
    }

    private static Item boots(int id, String key, String name, String rarity, int hpBonus, int damageReduction,
            int dodge, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_BOOTS, name, rarity, 0, 0, 0, hpBonus, 0,
                damageReduction, dodge, 0, 0, sellValue, iconRes);
    }

    private static Item charm(int id, String key, String name, String rarity, int damageBonus, int recoveryBonus,
            int dodge, int bonusGold, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_CHARM, name, rarity, 0, 0, 0, 0, damageBonus,
                0, dodge, recoveryBonus, bonusGold, sellValue, iconRes);
    }

    private static Item loot(int id, String key, String name, String rarity, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_LOOT, name, rarity, 0, 0, 0, 0, 0,
                0, 0, 0, 0, sellValue, iconRes);
    }

    private static Item consumable(int id, String key, String name, String rarity, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_CONSUMABLE, name, rarity, 0, 0, 0, 0, 0,
                0, 0, 0, 0, sellValue, iconRes);
    }

    private static Item unlock(int id, String key, String name, String rarity, int sellValue, int iconRes) {
        return new Item(id, key, SLOT_UNLOCK, name, rarity, 0, 0, 0, 0, 0,
                0, 0, 0, 0, sellValue, iconRes);
    }
}
