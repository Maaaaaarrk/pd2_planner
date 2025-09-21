// Java

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TwoHandedWeaponUtil {

    private TwoHandedWeaponUtil() {
    }

    // Raw names for readability. We'll normalize them into the lookup set below.
    private static final String[] TWO_HANDED_BASES_RAW = new String[]{
            // Polearms
            "Bardiche", "Voulge", "Scythe", "Poleaxe", "Halberd", "War Scythe",
            "Lochaber Axe", "Bill", "Battle Scythe", "Partizan", "Bec-de-Corbin", "Grim Scythe",
            "Ogre Axe", "Colossus Voulge", "Thresher", "Cryptic Axe", "Great Poleaxe", "Giant Thresher",

            // Spears
            "Spear", "Trident", "Brandistock", "Spetum", "Pike",
            "War Spear", "Fuscina", "War Fork", "Yari", "Lance",
            "Hyperion Spear", "Stygian Pike", "Mancatcher", "Ghost Spear", "War Pike",

            // Amazon Spears (2H)
            "Maiden Spear", "Maiden Pike", "Ceremonial Spear", "Ceremonial Pike", "Matriarchal Spear", "Matriarchal Pike",

            // Staves
            "Short Staff", "Long Staff", "Gnarled Staff", "Battle Staff", "War Staff",
            "Jo Staff", "Quarterstaff", "Cedar Staff", "Gothic Staff", "Rune Staff",
            "Elder Staff", "Shillelagh", "Archon Staff",

            // Bows
            "Short Bow", "Hunter's Bow", "Long Bow", "Composite Bow", "Short Battle Bow", "Long Battle Bow", "Short War Bow", "Long War Bow",
            "Edge Bow", "Razor Bow", "Cedar Bow", "Double Bow", "Short Siege Bow", "Long Siege Bow", "Rune Bow", "Gothic Bow",
            "Spider Bow", "Blade Bow", "Shadow Bow", "Great Bow", "Diamond Bow", "Crusader Bow", "Ward Bow", "Hydra Bow",

            // Amazon Bows (2H)
            "Stag Bow", "Reflex Bow", "Ashwood Bow", "Matriarchal Bow", "Grand Matron Bow",

            // Crossbows
            "Light Crossbow", "Crossbow", "Heavy Crossbow",
            "Arbalest", "Siege Crossbow", "Ballista",
            "Chu-Ko-Nu", "Demon Crossbow", "Colossus Crossbow",

            // Two-handed Axes
            "Large Axe", "Broad Axe", "Battle Axe", "Great Axe", "Giant Axe",
            "Military Axe", "Bearded Axe", "Tabar", "Gothic Axe", "Ancient Axe",
            "Feral Axe", "Silver-Edged Axe", "Decapitator", "Champion Axe", "Glorious Axe",

            // Mauls (two-handed maces)
            "Maul", "Great Maul", "War Club", "Martel de Fer", "Ogre Maul", "Thunder Maul",

            // Swords that are 2H or can be 1H/2H (count these as two-handed)
            "Two-Handed Sword", "Claymore", "Giant Sword", "Bastard Sword", "Flamberge", "Great Sword",
            "Espandon", "Dacian Falx", "Tusk Sword", "Gothic Sword", "Zweihander", "Executioner Sword",
            "Legend Sword", "Highland Blade", "Balrog Blade", "Champion Sword", "Colossus Sword", "Colossus Blade"
    };

    // Build a normalized, unmodifiable lookup set
    private static final Set<String> TWO_HANDED_BASES = Arrays.stream(TWO_HANDED_BASES_RAW)
            .map(TwoHandedWeaponUtil::normKey)
            .map(String::toLowerCase)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * Returns true if the given base name is considered two-handed.
     * Notes:
     * - All polearms, staves, bows, crossbows, spears (including Amazon spears), 2H axes, and mauls are two-handed.
     * - Swords that can be used 1H or 2H are counted as two-handed by design.
     */
    public static boolean isTwoHandedBase(String baseName) {
        if (baseName == null || baseName.isBlank()) return false;
        return TWO_HANDED_BASES.contains(normKey(baseName));
    }

    /**
     * Normalize to be robust against case, punctuation, and spacing.
     * Examples:
     * - "Thresher" -> "thresher"
     * - "Bec-de-Corbin" / "Bec de Corbin" -> "bec de corbin"
     * - "Hunter’s Bow" / "Hunters Bow" / "Hunter's Bow" -> "hunters bow"
     */
    private static String normKey(String s) {
        String lower = s.toLowerCase(Locale.ROOT).trim();
        // unify apostrophes and hyphens, then collapse non-alnum to single space
        lower = lower.replace('’', '\'').replace('–', '-').replace('—', '-');
        lower = lower.replaceAll("[^a-z0-9]+", " ").trim();
        return lower.replaceAll("\\s+", " ");
    }

    // Optional helper if you prefer to get "number of hands" directly
    public static int handsRequired(String baseName) {
        return isTwoHandedBase(baseName) ? 2 : 1;
    }
}
