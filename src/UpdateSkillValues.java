import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateSkillValues {
    private static final String dir = System.getProperty("user.dir") + "\\data\\";
    private static final String SKILLS_TEXT = dir + "Skills.txt";
    private static final boolean skipEmptyValues = true;
    private static int skillLevelMax = 70;

    public static void main(String[] args) throws IOException {

        Path input = Path.of(SKILLS_TEXT);
        List<String> lines = readAllLinesUTF8(input);
        if (lines.isEmpty()) {
            System.out.println("[]");
            return;
        }

        // Split header by tabs
        String headerLine = lines.get(0);
        String[] headers = headerLine.split("\t", -1);

        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            // Skip completely empty lines
            if (line.isEmpty()) continue;

            String[] cols = line.split("\t", -1);
            Map<String, String> obj = new LinkedHashMap<>();

            int max = Math.max(headers.length, cols.length);
            for (int c = 0; c < max; c++) {
                String key = c < headers.length ? headers[c] : ("col_" + c);
                String val = c < cols.length ? cols[c] : "";
                if (!skipEmptyValues || !val.isEmpty())
                    obj.put(key, val);
            }
            rows.add(obj);
        }

        // String json = toSkilljs(rows);
        //System.out.println(json);
        updatePossibles(rows, amazon_skill_map, Path.of(ClassJSUpdater.amazon));
        updatePossibles(rows, assassin_skill_map, Path.of(ClassJSUpdater.assassin));
        updatePossibles(rows, barbarian_skill_map, Path.of(ClassJSUpdater.barbarian));
        updatePossibles(rows, druid_skill_map, Path.of(ClassJSUpdater.druid));
        updatePossibles(rows, necromancer_skill_map, Path.of(ClassJSUpdater.necromancer));
        updatePossibles(rows, paladin_skill_map, Path.of(ClassJSUpdater.paladin));
        updatePossibles(rows, sorceress_skill_map, Path.of(ClassJSUpdater.sorceress));

    }

    private static String toSkilljs(List<Map<String, String>> rows) {

        for (Map<String, String> row : rows) {
            String skillName = row.get("skill");
            if (amazon_skill_map.containsKey(skillName)) {
                StringBuilder sb = new StringBuilder();
                sb.append("\n/* ").append(skillName).append(" */ var ").append(amazon_skill_map.get(skillName)).append(" = {values:[");
                addSkillData(row, sb, "ToHit", "LevToHit", "attack rating bonus");
                String damageType = row.get("EType");
                if (damageType != null) {
                    if (etype_map.containsKey(damageType)) {
                        damageType = etype_map.get(damageType);
                    } else {
                        System.err.println("Unknown damage type: " + damageType);
                        continue;
                    }
                }
                if (damageType != null) {
                    // Calc min damage
                    String dmgEnd = "Min";
                    sb.append("\n\t\t[\"").append(damageType).append(" Damage (").append(dmgEnd.toLowerCase()).append(")\"");
                    sb.append(
                            buildBucketedLevelsCSV(
                                    tryParseInt(row.get("E" + dmgEnd), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev1"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev2"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev3"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev4"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev5"), 0)));
                    sb.append("],");
                }
                if (damageType != null) {
                    // Calc min damage
                    String dmgEnd = "Max";
                    sb.append("\n\t\t[\"").append(damageType).append(" Damage (").append(dmgEnd.toLowerCase()).append(")\"");
                    sb.append(
                            buildBucketedLevelsCSV(
                                    tryParseInt(row.get("E" + dmgEnd), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev1"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev2"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev3"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev4"), 0),
                                    tryParseInt(row.get("E" + dmgEnd + "Lev5"), 0)));
                    sb.append("],");
                }

                {
                    // Calc mana
                    int mana = tryParseInt(row.get("mana"), 0);
                    int lvlmana = tryParseInt(row.get("lvlmana"), 0);
                    int manashift = tryParseInt(row.get("manashift"), 0);
                    int minmana = tryParseInt(row.get("minmana"), 0);
                    if (manashift < 0) manashift = 0;
                    if (manashift > 30) manashift = 30;
                    // Multiplier should be (2^manashift) / 256.0 to match the chart
                    double effectiveshift = (1L << manashift) / 256.0;
                    if (Math.max((mana + lvlmana * skillLevelMax) * effectiveshift, minmana) > 0 ||
                            Math.max(mana * effectiveshift, minmana) > 0) {
                        sb.append("\n\t\t[\"Mana Cost\"");
                        for (int i = 0; i < skillLevelMax; i++) {
                            double manacost = (mana + lvlmana * i) * effectiveshift;
                            manacost = Math.max(manacost, minmana);
                            sb.append(",").append(formatNumber(manacost));
                        }
                        sb.append("],");
                    }
                }
                sb.append("\n]};");
                amazon_skill_map.replace(skillName, sb.toString());
            }
        }
        // Puts in Order for git compares vs old
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : amazon_skill_map.entrySet()) {
            sb.append(entry.getValue());
        }
        return sb.toString();
    }

    private static void updatePossibles(List<Map<String, String>> rows, Map<String, String> skillMap, Path path) throws IOException {

        for (Map<String, String> row : rows) {
            String skillName = row.get("skill");
            if (skillMap.containsKey(skillName)) {
                String dcode = skillMap.get(skillName);
                /*
        String varKey = "122"; // e.g., Power Strike => var d122
        String label = "attack rating bonus";
        String numbers = "20,32,44,56,68,80,92,104,116,128"; // your replacement list
        updateNumberList(path, varKey, label, numbers);
                 */
                // "attack rating bonus"
                String arBonus = getDataListFromRow(row, "ToHit", "LevToHit");
                if (arBonus != null && !arBonus.isEmpty()) {
                    ClassJSUpdater.updateNumberList(path, dcode, "attack rating bonus", arBonus);
                }

                String damageType = row.get("EType");
                if (damageType != null) {
                    if (etype_map.containsKey(damageType)) {
                        damageType = etype_map.get(damageType);
                    } else {
                        System.err.println("Unknown damage type: " + damageType);
                        continue;
                    }
                }
                if (damageType != null) {
                    // Calc min damage
                    String dmgEnd = "Min";
                    String damage = buildBucketedLevelsCSV(
                            tryParseInt(row.get("E" + dmgEnd), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev1"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev2"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev3"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev4"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev5"), 0));
                    if (damage != null && !damage.isEmpty())
                        ClassJSUpdater.updateNumberList(path, dcode, damageType + " Damage (" + dmgEnd.toLowerCase() + ")", damage);
                }
                if (damageType != null) {
                    // Calc min damage
                    String dmgEnd = "Max";
                    String damage = buildBucketedLevelsCSV(
                            tryParseInt(row.get("E" + dmgEnd), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev1"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev2"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev3"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev4"), 0),
                            tryParseInt(row.get("E" + dmgEnd + "Lev5"), 0));
                    if (damage != null && !damage.isEmpty())
                        ClassJSUpdater.updateNumberList(path, dcode, damageType + " Damage (" + dmgEnd.toLowerCase() + ")", damage);
                }

                {
                    // Calc mana
                    int mana = tryParseInt(row.get("mana"), 0);
                    int lvlmana = tryParseInt(row.get("lvlmana"), 0);
                    int manashift = tryParseInt(row.get("manashift"), 0);
                    int minmana = tryParseInt(row.get("minmana"), 0);
                    if (manashift < 0) manashift = 0;
                    if (manashift > 30) manashift = 30;
                    // Multiplier should be (2^manashift) / 256.0 to match the chart
                    double effectiveshift = (1L << manashift) / 256.0;
                    if (Math.max((mana + lvlmana * skillLevelMax) * effectiveshift, minmana) > 0 ||
                            Math.max(mana * effectiveshift, minmana) > 0) {
                        //  sb.append("\n\t\t[\"Mana Cost\"");
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < skillLevelMax; i++) {
                            double manacost = (mana + lvlmana * i) * effectiveshift;
                            manacost = Math.max(manacost, minmana);
                            sb.append(",").append(formatNumber(manacost));
                        }
                        ClassJSUpdater.updateNumberList(path, dcode, "Mana Cost", sb.toString());
                    }
                }
            }
        }
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) {
            // It's effectively an integer (handles -0.0 and floating rounding)
            long asLong = (long) Math.rint(value);
            return Long.toString(asLong);
        }
        return Double.toString(value);
    }

    private static void addSkillData(Map<String, String> row, StringBuilder sb, String textFileKey, String textFileKeyPerLvl, String jsFileKey) {
        if (row.containsKey(textFileKey)) {
            sb.append("\n\t\t[\"" + jsFileKey + "\"");
            sb.append(getDataListFromRow(row, textFileKey, textFileKeyPerLvl));
            sb.append("],");
        }
    }

    private static String getDataListFromRow(Map<String, String> row, String textFileKey, String textFileKeyPerLvl) {
        StringBuilder sb = new StringBuilder();
        int baseVal = tryParseInt(row.get(textFileKey), 0);
        int perlevelValue = tryParseInt(row.get(textFileKeyPerLvl), 0);
        if (baseVal == 0 && perlevelValue == 0) return null;
        for (int i = 0; i < skillLevelMax; i++) {
            int j = (baseVal + perlevelValue * i);
            sb.append(",").append(j);
        }
        return sb.toString();
    }

    static Map<String, String> etype_map = Map.ofEntries(
            Map.entry("mag", "Magic"),
            Map.entry("cold", "Cold"),
            Map.entry("fire", "Fire"),
            Map.entry("ltng", "Lighting"),
            Map.entry("pois", "Poison")
    );

    /**
     * Builds a comma-delimited list of 70 values starting from level 1.
     * Each subsequent level adds a bucketed increment to the previous value:
     * - Levels 2-8 use levDam1
     * - Levels 9-16 use levDam2
     * - Levels 17-22 use levDam3
     * - Levels 23-28 use levDam4
     * - Levels 29-70 use levDam5
     *
     * @param level1Start starting value at level 1
     * @param levDam1     increment for levels 2-8
     * @param levDam2     increment for levels 9-16
     * @param levDam3     increment for levels 17-22
     * @param levDam4     increment for levels 23-28
     * @param levDam5     increment for levels 29-70
     * @return comma-delimited string of 70 values
     */
    public static String buildBucketedLevelsCSV(
            int level1Start,
            int levDam1,
            int levDam2,
            int levDam3,
            int levDam4,
            int levDam5
    ) {
        if (level1Start == 0 && levDam1 == 0 && levDam2 == 0 && levDam3 == 0 && levDam4 == 0 && levDam5 == 0)
            return null;
        final int totalLevels = skillLevelMax;
        StringBuilder out = new StringBuilder();
        int current = level1Start;

        // Level 1
        out.append(current);

        for (int level = 2; level <= totalLevels; level++) {
            int inc;
            if (level <= 8) {
                inc = levDam1;
            } else if (level <= 16) {
                inc = levDam2;
            } else if (level <= 22) {
                inc = levDam3;
            } else if (level <= 28) {
                inc = levDam4;
            } else {
                inc = levDam5;
            }
            current += inc;
            out.append(',').append(current);
        }
        return out.toString();
    }

    private static int tryParseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static List<String> readAllLinesUTF8(Path path) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Normalize Windows CRLF if present (BufferedReader drops it anyway)
                result.add(line);
            }
        }
        return result;
    }

    private static String toJsonArray(List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(toJsonObject(rows.get(i)) + "\n");
        }
        sb.append(']');
        return sb.toString();
    }

    /*
    {"skill":"Magic Arrow","Id":6,"charclass":"ama","skilldesc":"magic arrow","srvstfunc":4,"srvdofunc":8,"srvmissilea":"magicarrow","cltstfunc":11,"cltdofunc":17,"cltmissilea":"magicarrow","enhanceable":1,"attackrank":0,"noammo":1,"range":"rng","itypea1":"miss","anim":"A1","seqtrans":"A1","monanim":"xx","UseAttackRate":1,"reqlevel":1,"maxlvl":20,"leftskill":1,"minmana":0,"manashift":5,"mana":16,"lvlmana":1,"interrupt":1,"calc1":"1 + (blvl / 5)","Param1":75,"*Param1 Description":"mag dmg% conversion","Param2":10,"*Param2 Description":"extra arrows level","Param8":20,"*Param8 Description":"damage synergy","InGame":1,"ToHit":10,"LevToHit":9,"HitShift":8,"SrcDam":96,"EType":"mag","EMin":3,"EMinLev1":2,"EMinLev2":5,"EMinLev3":15,"EMinLev4":30,"EMinLev5":44,"EMax":5,"EMaxLev1":3,"EMaxLev2":7,"EMaxLev3":17,"EMaxLev4":32,"EMaxLev5":47,"EDmgSymPerCalc":"(skill('Inner Sight'.blvl)+skill('Slow Movement'.blvl)+skill('Guided Arrow'.blvl))*par8","cost mult":256,"cost add":5000}

     */

    private static String toJsonObject(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escapeJson(e.getKey())).append('"').append(':');
            // attempt numeric detection, otherwise emit as string
            String v = e.getValue();
            if (looksNumeric(v)) {
                sb.append(v);
            } else if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
                sb.append(v.toLowerCase());
            } else {
                sb.append('"').append(escapeJson(v)).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private static boolean looksNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        // Allow integers and simple decimals, optional leading sign
        return s.matches("[+-]?\\d+(\\.\\d+)?");
    }

    private static String escapeJson(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        out.append(String.format("\\u%04x", (int) ch));
                    } else {
                        out.append(ch);
                    }
            }
        }
        return out.toString();
    }


    static LinkedHashMap<String, String> amazon_skill_map = new LinkedHashMap<>();

    static {
        amazon_skill_map.put("Jab", "d111");
        amazon_skill_map.put("Power Strike", "d122");
        amazon_skill_map.put("Poison Javelin", "d113");
        amazon_skill_map.put("Javelin and Spear Mastery", "d121");
        amazon_skill_map.put("Lightning Bolt", "d133");
        amazon_skill_map.put("Charged Strike", "d142");
        amazon_skill_map.put("Plague Javelin", "d143");
        amazon_skill_map.put("Fend", "d141");
        amazon_skill_map.put("Lightning Strike", "d162");
        amazon_skill_map.put("Lightning Fury", "d163");
        amazon_skill_map.put("Inner Sight", "d211");
        amazon_skill_map.put("Critical Strike", "d213");
        amazon_skill_map.put("Slow Movement", "d231");
        amazon_skill_map.put("Dodge", "d242");
        amazon_skill_map.put("None", "d253");
        amazon_skill_map.put("Penetrate", "d253");
        amazon_skill_map.put("Evade", "d222");
        amazon_skill_map.put("Decoy", "d241");
        amazon_skill_map.put("Valkyrie", "d261");
        amazon_skill_map.put("Pierce", "d233");
        amazon_skill_map.put("Cold Arrow", "d321");
        amazon_skill_map.put("Magic Arrow", "d312");
        amazon_skill_map.put("Multiple Shot", "d322");
        amazon_skill_map.put("Fire Arrow", "d323");
        amazon_skill_map.put("Ice Arrow", "d331");
        amazon_skill_map.put("Guided Arrow", "d342");
        amazon_skill_map.put("Exploding Arrow", "d343");
        amazon_skill_map.put("Strafe", "d352");
        amazon_skill_map.put("Immolation Arrow", "d363");
        amazon_skill_map.put("Freezing Arrow", "d361");
    }

    static LinkedHashMap<String, String> assassin_skill_map = new LinkedHashMap<>();

    static {
        assassin_skill_map.put("Tiger Strike", "d112");
        assassin_skill_map.put("Dragon Talon", "d113");
        assassin_skill_map.put("Fists of Fire", "d121");
        assassin_skill_map.put("Dragon Claw", "d123");
        assassin_skill_map.put("Cobra Strike", "d132");
        assassin_skill_map.put("Claws of Thunder", "d141");
        assassin_skill_map.put("Dragon Tail", "d143");
        assassin_skill_map.put("Blades of Ice", "d151");
        assassin_skill_map.put("Dragon Flight", "d153");
        assassin_skill_map.put("Phoenix Strike", "d162");
        assassin_skill_map.put("Claw and Dagger Mastery", "d212");
        assassin_skill_map.put("Psychic Hammer", "d213");
        assassin_skill_map.put("Burst of Speed", "d221");
        assassin_skill_map.put("Weapon Block", "d232");
        assassin_skill_map.put("Cloak of Shadows", "d233");
        assassin_skill_map.put("Fade", "d241");
        assassin_skill_map.put("Shadow Warrior", "d242");
        assassin_skill_map.put("Mind Blast", "d243");
        assassin_skill_map.put("Venom", "d261");
        assassin_skill_map.put("Shadow Master", "d262");
        assassin_skill_map.put("Fire Blast", "d312");
        assassin_skill_map.put("Shock Web", "d321");
        assassin_skill_map.put("Blade Sentinel", "d323");
        assassin_skill_map.put("Charged Bolt Sentry", "d331");
        assassin_skill_map.put("Wake of Fire", "d332");
        assassin_skill_map.put("Blade Fury", "d343");
        assassin_skill_map.put("Lightning Sentry", "d351");
        assassin_skill_map.put("Wake of Inferno", "d352");
        assassin_skill_map.put("Blade Shield", "d353");
        assassin_skill_map.put("Chain Lightning Sentry", "d361");
        assassin_skill_map.put("Death Sentry", "d362");
    }

    static LinkedHashMap<String, String> barbarian_skill_map = new LinkedHashMap<>();

    static {
        barbarian_skill_map.put("Howl", "d111");
        barbarian_skill_map.put("Find Potion", "d113");
        barbarian_skill_map.put("Taunt", "d142");
        barbarian_skill_map.put("Shout", "d121");
        barbarian_skill_map.put("Find Item", "d133");
        barbarian_skill_map.put("Battle Cry", "d162");
        barbarian_skill_map.put("Battle Orders", "d151");
        barbarian_skill_map.put("Grim Ward", "d153");
        barbarian_skill_map.put("War Cry", "d122");
        barbarian_skill_map.put("Battle Command", "d161");

        barbarian_skill_map.put("General Mastery", "d211");
        barbarian_skill_map.put("Polearm and Spear Mastery", "d222");
        barbarian_skill_map.put("Deep Wounds", "d262");
        barbarian_skill_map.put("Throwing Mastery", "d213");
        barbarian_skill_map.put("Combat Reflexes", "d231");
        barbarian_skill_map.put("Iron Skin", "d243");
        barbarian_skill_map.put("Increased Speed", "d251");
        barbarian_skill_map.put("Natural Resistance", "d263");

        barbarian_skill_map.put("Frenzy", "d312");
        barbarian_skill_map.put("Concentrate", "d332");
        barbarian_skill_map.put("Berserk", "d352");
        barbarian_skill_map.put("Stun", "d321");
        barbarian_skill_map.put("Leap", "d331");
        barbarian_skill_map.put("Double Throw", "d333");
        barbarian_skill_map.put("Bash", "d311");
        barbarian_skill_map.put("Leap Attack", "d351");
        barbarian_skill_map.put("Whirlwind", "d362");
        barbarian_skill_map.put("Double Swing", "d323");
    }

    static Map<String, String> druid_skill_map = Map.ofEntries(
            Map.entry("Firestorm", "d111"),
            Map.entry("Molten Boulder", "d121"),
            Map.entry("Gust", "d143"),
            Map.entry("Arctic Blast", "d113"),
            Map.entry("Fissure", "d131"),
            Map.entry("Cyclone Armor", "d123"),
            Map.entry("Twister", "d132"),
            Map.entry("Volcano", "d151"),
            Map.entry("Tornado", "d152"),
            Map.entry("Armageddon", "d161"),
            Map.entry("Hurricane", "d162"),
            Map.entry("Werewolf", "d211"),
            Map.entry("Lycanthropy", "d212"),
            Map.entry("Werebear", "d213"),
            Map.entry("Feral Rage", "d221"),
            Map.entry("Maul", "d223"),
            Map.entry("Rabies", "d241"),
            Map.entry("Fire Claws", "d252"),
            Map.entry("Hunger", "d232"),
            Map.entry("Shock Wave", "d243"),
            Map.entry("Fury", "d261"),
            Map.entry("Raven", "d312"),
            Map.entry("Poison Creeper", "d313"),
            Map.entry("Heart of Wolverine", "d321"),
            Map.entry("Summon Spirit Wolf", "d332"),
            Map.entry("Carrion Vine", "d333"),
            Map.entry("Oak Sage", "d361"),
            Map.entry("Summon Dire Wolf", "d352"),
            Map.entry("Solar Creeper", "d353"),
            Map.entry("Spirit of Barbs", "d341"),
            Map.entry("Summon Grizzly", "d362")
    );

    static Map<String, String> necromancer_skill_map = Map.ofEntries(
            Map.entry("Skeleton Mastery", "d111"),
            Map.entry("Skeleton Warrior", "d113"),
            Map.entry("Skeleton Archer", "d153"),
            Map.entry("Clay Golem", "d122"),
            Map.entry("Golem Mastery", "d131"),
            Map.entry("Skeletal Mage", "d133"),
            Map.entry("Blood Golem", "d142"),
            Map.entry("Blood Warp", "d151"),
            Map.entry("Iron Golem", "d152"),
            Map.entry("Fire Golem", "d162"),
            Map.entry("Revive", "d163"),
            Map.entry("Poison Strike", "d211"),
            Map.entry("Teeth", "d212"),
            Map.entry("Bone Armor", "d223"),
            Map.entry("Corpse Explosion", "d221"),
            Map.entry("Desecrate", "d231"),
            Map.entry("Bone Spear", "d242"),
            Map.entry("Bone Wall", "d233"),
            Map.entry("Bone Spirit", "d262"),
            Map.entry("Poison Nova", "d261"),
            Map.entry("Bone Prison", "d253"),
            Map.entry("Amplify Damage", "d312")
    );

    static Map<String, String> paladin_skill_map = new LinkedHashMap<>();

    static {
        paladin_skill_map.put("Prayer", "d111");
        paladin_skill_map.put("Resist Fire", "d113");
        paladin_skill_map.put("Defiance", "d122");
        paladin_skill_map.put("Resist Cold", "d123");
        paladin_skill_map.put("Cleansing", "d131");
        paladin_skill_map.put("Resist Lightning", "d133");
        paladin_skill_map.put("Vigor", "d142");
        paladin_skill_map.put("Meditation", "d151");
        paladin_skill_map.put("Redemption", "d162");
        paladin_skill_map.put("Salvation", "d163");
        paladin_skill_map.put("Might", "d211");
        paladin_skill_map.put("Holy Fire", "d222");
        paladin_skill_map.put("Thorns", "d223");
        paladin_skill_map.put("Blessed Aim", "d231");
        paladin_skill_map.put("Concentration", "d241");
        paladin_skill_map.put("Holy Freeze", "d242");
        paladin_skill_map.put("Holy Shock", "d252");
        paladin_skill_map.put("Sanctuary", "d253");
        paladin_skill_map.put("Fanaticism", "d261");
        paladin_skill_map.put("Conviction", "d263");
        paladin_skill_map.put("Sacrifice", "d321");
        paladin_skill_map.put("Smite", "d313");
        paladin_skill_map.put("Holy Bolt", "d312");
        paladin_skill_map.put("Zeal", "d311");
        paladin_skill_map.put("Charge", "d333");
        paladin_skill_map.put("Vengeance", "d341");
        paladin_skill_map.put("Blessed Hammer", "d342");
        paladin_skill_map.put("Holy Sword", "d343");
        paladin_skill_map.put("Holy Shield", "d353");
        paladin_skill_map.put("Fist of the Heavens", "d352");
        paladin_skill_map.put("Joust", "d351");
        paladin_skill_map.put("Holy Light", "d332");
        paladin_skill_map.put("Holy Nova", "d362");
    }

    static Map<String, String> sorceress_skill_map = new LinkedHashMap<>();

    static {
        sorceress_skill_map.put("Ice Bolt", "d112");
        sorceress_skill_map.put("Cold Enchant", "d113");
        sorceress_skill_map.put("Frost Nova", "d121");
        sorceress_skill_map.put("Ice Blast", "d122");
        sorceress_skill_map.put("Shiver Armor", "d133");
        sorceress_skill_map.put("Glacial Spike", "d142");
        sorceress_skill_map.put("Blizzard", "d151");
        sorceress_skill_map.put("Ice Barrage", "d152");
        sorceress_skill_map.put("Chilling Armor", "d153");
        sorceress_skill_map.put("Frozen Orb", "d161");
        sorceress_skill_map.put("Cold Mastery", "d162");

        sorceress_skill_map.put("Charged Bolt", "d212");
        sorceress_skill_map.put("Static Field", "d211");
        sorceress_skill_map.put("Telekinesis", "d213");
        sorceress_skill_map.put("Nova", "d231");
        sorceress_skill_map.put("Lightning", "d232");
        sorceress_skill_map.put("Chain Lightning", "d252");
        sorceress_skill_map.put("Teleport", "d243");
        sorceress_skill_map.put("Energy Shield", "d263");
        sorceress_skill_map.put("Lightning Mastery", "d262");
        sorceress_skill_map.put("Thunder Storm", "d251");

        sorceress_skill_map.put("Fire Bolt", "d312");
        sorceress_skill_map.put("Warmth", "d313");
        sorceress_skill_map.put("Blaze", "d321");
        sorceress_skill_map.put("Inferno", "d311");
        sorceress_skill_map.put("Fire Ball", "d332");
        sorceress_skill_map.put("Fire Wall", "d331");
        sorceress_skill_map.put("Enchant Fire", "d343");
        sorceress_skill_map.put("Meteor", "d351");
        sorceress_skill_map.put("Fire Mastery", "d362");
    }

}
