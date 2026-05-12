package com.example.stepcounterbase;

final class Item {
    final int id;
    final String slot;
    final String name;
    final String rarity;
    final int level;

    Item(int id, String slot, String name, String rarity, int level) {
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.rarity = rarity;
        this.level = level;
    }

    String displayName() {
        return name + " (Level " + level + ")";
    }

    String toStorage() {
        return id + "," + slot + "," + name + "," + rarity + "," + level;
    }

    static Item fromStorage(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length != 5) {
            return null;
        }
        try {
            return new Item(
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
}
