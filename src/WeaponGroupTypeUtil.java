public final class WeaponGroupTypeUtil {

    private WeaponGroupTypeUtil() {
    }

    // Normalize to lower-case for exact, case-insensitive matching
    private static String norm(String s) {
        return s.trim().toLowerCase(java.util.Locale.ROOT);
    }

    // -------------------------
    // Group sets (readable case)
    // -------------------------

    // Javelins (non-class and Amazon-only thrown javelins)
    private static final java.util.Set<String> JAVELIN = java.util.Set.of(
            // Normal
            "Javelin", "Pilum", "Short Spear", "Glaive", "Throwing Spear",
            // Exceptional
            "War Javelin", "Great Pilum", "Harpoon", "Simbilan", "Spiculum",
            // Elite
            "Hyperion Javelin", "Stygian Pilum", "Winged Harpoon", "Ghost Glaive",
            // Amazon-only
            "Maiden Javelin", "Ceremonial Javelin", "Matriarchal Javelin"
    );

    // Spears (2H melee spears, including Amazon-only)
    private static final java.util.Set<String> SPEAR = java.util.Set.of(
            // Normal
            "Spear", "Trident", "Brandistock", "Spetum", "Pike",
            // Exceptional
            "War Spear", "Fuscina", "War Fork", "Yari", "Lance",
            // Elite
            "Hyperion Spear", "Stygian Pike", "Mancatcher", "Ghost Spear", "War Pike",
            // Amazon-only
            "Maiden Spear", "Maiden Pike", "Ceremonial Spear", "Ceremonial Pike", "Matriarchal Spear", "Matriarchal Pike"
    );

    // Assassin claws
    private static final java.util.Set<String> CLAW = java.util.Set.of(
            // Normal
            "Katar", "Wrist Blade", "Cestus", "Claws", "Blade Talons", "Scissors Katar",
            // Exceptional
            "Hatchet Hands", "Wrist Spike", "Fascia", "Hand Scythe", "Greater Claws", "Greater Talons", "Quhab", "Scissors Quhab",
            // Elite
            "Suwayyah", "Wrist Sword", "War Fist", "Battle Cestus", "Feral Claws", "Runic Talons", "Scissors Suwayyah"
    );

    // Orbs (Sorceress)
    private static final java.util.Set<String> ORB = java.util.Set.of(
            // Normal
            "Eagle Orb", "Sacred Globe", "Smoked Sphere", "Clasped Orb", "Jared's Stone",
            // Exceptional
            "Glowing Orb", "Crystalline Globe", "Cloudy Sphere", "Sparkling Ball", "Swirling Crystal",
            // Elite
            "Heavenly Stone", "Eldritch Orb", "Demon Heart", "Vortex Orb", "Dimensional Shard"
    );

    // Scepters
    private static final java.util.Set<String> SCEPTER = java.util.Set.of(
            // Normal
            "Scepter", "Grand Scepter", "War Scepter",
            // Exceptional
            "Rune Scepter", "Holy Water Sprinkler", "Divine Scepter",
            // Elite
            "Mighty Scepter", "Seraph Rod", "Caduceus"
    );

    // Swords (1H and 2H)
    private static final java.util.Set<String> SWORD = java.util.Set.of(
            // Normal 1H
            "Short Sword", "Scimitar", "Falchion", "Crystal Sword", "Broad Sword", "Long Sword", "War Sword",
            // Normal 2H
            "Two-Handed Sword", "Claymore", "Giant Sword", "Bastard Sword", "Flamberge", "Great Sword",
            // Exceptional 1H
            "Gladius", "Cutlass", "Tulwar", "Dimensional Blade", "Battle Sword", "Rune Sword", "Ancient Sword",
            // Exceptional 2H
            "Espandon", "Dacian Falx", "Tusk Sword", "Gothic Sword", "Zweihander", "Executioner Sword",
            // Elite 1H
            "Falcata", "Ataghan", "Elegant Blade", "Phase Blade", "Conquest Sword", "Cryptic Sword", "Mythical Sword",
            // Elite 2H
            "Highland Blade", "Balrog Blade", "Champion Sword", "Colossus Sword", "Colossus Blade", "Legend Sword"
    );

    // Staves
    private static final java.util.Set<String> STAFF = java.util.Set.of(
            // Normal
            "Short Staff", "Long Staff", "Gnarled Staff", "Battle Staff", "War Staff",
            // Exceptional
            "Jo Staff", "Quarterstaff", "Cedar Staff", "Gothic Staff", "Rune Staff",
            // Elite
            "Walking Stick", "Stalagmite", "Elder Staff", "Shillelagh", "Archon Staff"
    );

    // Wands
    private static final java.util.Set<String> WAND = java.util.Set.of(
            // Normal
            "Wand", "Yew Wand", "Bone Wand", "Grim Wand",
            // Exceptional
            "Burnt Wand", "Petrified Wand", "Tomb Wand", "Grave Wand",
            // Elite
            "Polished Wand", "Ghost Wand", "Lich Wand", "Unearthed Wand"
    );

    // Clubs / maces / hammers (grouped under "club" per your list)
    private static final java.util.Set<String> CLUB = java.util.Set.of(
            // Normal
            "Club", "Spiked Club", "Mace", "Morning Star", "Flail", "War Hammer",
            // Exceptional
            "Cudgel", "Barbed Club", "Flanged Mace", "Jagged Star", "Knout", "Battle Hammer",
            // Elite
            "Truncheon", "Tyrant Club", "Reinforced Mace", "Devil Star", "Scourge", "Legendary Mallet"
    );

    // Axes
    private static final java.util.Set<String> AXE = java.util.Set.of(
            // Normal 1H
            "Hand Axe", "Axe", "Double Axe", "Military Pick", "War Axe",
            // Normal 2H
            "Large Axe", "Broad Axe", "Battle Axe", "Great Axe", "Giant Axe",
            // Exceptional 1H
            "Hatchet", "Cleaver", "Twin Axe", "Crowbill", "Naga",
            // Exceptional 2H
            "Military Axe", "Bearded Axe", "Tabar", "Gothic Axe", "Ancient Axe",
            // Elite 1H
            "Tomahawk", "Small Crescent", "Ettin Axe", "War Spike", "Berserker Axe",
            // Elite 2H
            "Feral Axe", "Silver-Edged Axe", "Decapitator", "Champion Axe", "Glorious Axe"
    );

    // Daggers
    private static final java.util.Set<String> DAGGER = java.util.Set.of(
            // Normal
            "Dagger", "Dirk", "Kris", "Blade",
            // Exceptional
            "Poignard", "Rondel", "Cinquedeas", "Stiletto",
            // Elite
            "Bone Knife", "Mithril Point", "Fanged Knife", "Legend Spike"
    );

    // Polearms
    private static final java.util.Set<String> POLEARM = java.util.Set.of(
            // Normal
            "Bardiche", "Voulge", "Scythe", "Poleaxe", "Halberd", "War Scythe",
            // Exceptional
            "Lochaber Axe", "Bill", "Battle Scythe", "Partizan", "Bec-de-Corbin", "Grim Scythe",
            // Elite
            "Ogre Axe", "Colossus Voulge", "Thresher", "Cryptic Axe", "Great Poleaxe", "Giant Thresher"
    );

    // Thrown (knives/axes only; javelins are in JAVELIN)
    private static final java.util.Set<String> THROWN = java.util.Set.of(
            // Knives (normal/exceptional/elite)
            "Throwing Knife", "Balanced Knife", "Battle Dart", "War Dart", "Winged Knife", "Flying Knife",
            // Axes (normal/exceptional/elite)
            "Throwing Axe", "Balanced Axe", "Francisca", "Hurlbat", "Winged Axe", "Flying Axe"
    );

    // Crossbows
    private static final java.util.Set<String> CROSSBOW = java.util.Set.of(
            // Normal
            "Light Crossbow", "Crossbow", "Heavy Crossbow", "Repeating Crossbow",
            // Exceptional
            "Arbalest", "Siege Crossbow", "Ballista", "Chu-Ko-Nu",
            // Elite
            "Great Crossbow", "Colossus Crossbow", "Demon Crossbow", "Gorgon Crossbow"
    );

    // -------------------------
    // Lookup map (exact match)
    // -------------------------
    private static final java.util.Map<String, String> NAME_TO_GROUP;

    static {
        java.util.Map<String, String> m = new java.util.HashMap<>(256);

        putAll(m, JAVELIN, "javelin");
        putAll(m, SPEAR, "spear");
        putAll(m, CLAW, "claw");
        putAll(m, ORB, "orb");
        putAll(m, SCEPTER, "scepter");
        putAll(m, SWORD, "sword");
        putAll(m, STAFF, "staff");
        putAll(m, WAND, "wand");
        putAll(m, CLUB, "club");
        putAll(m, AXE, "axe");
        putAll(m, DAGGER, "dagger");
        putAll(m, POLEARM, "polearm");
        putAll(m, THROWN, "thrown");
        putAll(m, CROSSBOW, "crossbow");

        NAME_TO_GROUP = java.util.Collections.unmodifiableMap(m);
    }

    private static void putAll(java.util.Map<String, String> m, java.util.Set<String> names, String group) {
        for (String raw : names) {
            String key = norm(raw);
            String existing = m.putIfAbsent(key, group);
            if (existing != null && !existing.equals(group)) {
                throw new IllegalStateException("Base name present in multiple groups: " + raw + " -> " + existing + " and " + group);
            }
        }
    }

    // -------------------------
    // API
    // -------------------------

    // Returns the group for the exact base name, or empty if unknown.
    public static String groupOf(String baseName) {
        if (baseName == null) return null;
        String normBaseName = norm(baseName.toLowerCase());
        return NAME_TO_GROUP.get(norm(normBaseName));
    }

    // For validation or introspection (e.g., unit tests)
    public static java.util.Set<String> allKnownBases() {
        return java.util.Collections.unmodifiableSet(NAME_TO_GROUP.keySet());
    }

    // If you need the raw sets, expose them as unmodifiable views
    public static java.util.Map<String, java.util.Set<String>> groupsView() {
        java.util.Map<String, java.util.Set<String>> v = new java.util.LinkedHashMap<>();
        v.put("javelin", java.util.Collections.unmodifiableSet(JAVELIN));
        v.put("spear", java.util.Collections.unmodifiableSet(SPEAR));
        v.put("claw", java.util.Collections.unmodifiableSet(CLAW));
        v.put("orb", java.util.Collections.unmodifiableSet(ORB));
        v.put("scepter", java.util.Collections.unmodifiableSet(SCEPTER));
        v.put("sword", java.util.Collections.unmodifiableSet(SWORD));
        v.put("staff", java.util.Collections.unmodifiableSet(STAFF));
        v.put("wand", java.util.Collections.unmodifiableSet(WAND));
        v.put("club", java.util.Collections.unmodifiableSet(CLUB));
        v.put("axe", java.util.Collections.unmodifiableSet(AXE));
        v.put("dagger", java.util.Collections.unmodifiableSet(DAGGER));
        v.put("polearm", java.util.Collections.unmodifiableSet(POLEARM));
        v.put("thrown", java.util.Collections.unmodifiableSet(THROWN));
        v.put("crossbow", java.util.Collections.unmodifiableSet(CROSSBOW));
        return java.util.Collections.unmodifiableMap(v);
    }
}
