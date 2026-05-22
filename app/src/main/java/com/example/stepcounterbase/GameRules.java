package com.example.stepcounterbase;

final class GameRules {
    static final String SLOT_WEAPON = "weapon";
    static final String SLOT_ARMOR = "armor";
    static final String SLOT_BOOTS = "boots";
    static final String SLOT_CHARM = "charm";
    static final String SLOT_LOOT = "loot";
    static final String SLOT_CONSUMABLE = "consumable";
    static final String SLOT_UNLOCK = "unlock";

    static final int PHASE_TRAVEL = 0;
    static final int PHASE_COMBAT = 1;
    static final int PHASE_COMPLETE = 2;
    static final int PHASE_EXHAUSTED = 3;
    static final int ENCOUNTERS = 3;

    static final int TAB_FIGHT = 0;
    static final int TAB_SKILLS = 1;
    static final int TAB_BAG = 2;
    static final int TAB_TOWN = 3;

    static final int FIGHT_HUB = 0;
    static final int FIGHT_AREAS = 1;
    static final int FIGHT_AREA_ENEMY = 2;
    static final int FIGHT_DUNGEONS = 3;
    static final int FIGHT_DUNGEON_DETAIL = 4;
    static final int FIGHT_COMBAT = 5;

    static final int MODE_NONE = 0;
    static final int MODE_AREA = 1;
    static final int MODE_DUNGEON = 2;

    private GameRules() {
    }
}
