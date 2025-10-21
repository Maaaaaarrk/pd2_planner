import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Converts data/Runes.txt (tab-separated) into a JSON array and writes it to out/Runes.json.
 * Assumptions:
 * - First non-empty line is a header with tab-separated column names.
 * - Subsequent non-empty lines are data rows with the same number or fewer columns (missing cells -> null/empty).
 * - File is UTF-8 compatible.
 */
public class RunesTxtToJson {

    private static Map<String, Map<String, Object>> runeWordsParsed = null;
    private static Map<String, List<Map<String, Object>>> runeWordsItemTypeBuckets = new LinkedHashMap<>();

    private static Map<String, Map<String, Object>> runeStats = null;


    public static void main(String[] args) throws IOException {

        System.out.println(getRunewordsParsed());
        // System.err.println(prettyPrintRuneStats());
        // System.out.println(getRuneword("Beast", "Staff"));
        //  writeRunewordsJsFile();

    }


    // Pretty printer for `runeStats`
    public static String prettyPrintRuneStats() {
        Map<String, Map<String, Object>> stats = runeStats();
        StringBuilder sb = new StringBuilder(4096);

        // Sort runes by name for stable output
        List<String> runeNames = new ArrayList<>(stats.keySet());
        runeNames.sort(String.CASE_INSENSITIVE_ORDER);

        for (String runeName : runeNames) {
            Map<String, Object> rune = stats.get(runeName);
            sb.append(runeName).append("\n");

            @SuppressWarnings("unchecked")
            Map<String, Object> weap = (Map<String, Object>) rune.get("rwstatsWeap");
            @SuppressWarnings("unchecked")
            Map<String, Object> armor = (Map<String, Object>) rune.get("rwstatsArmor");
            @SuppressWarnings("unchecked")
            Map<String, Object> shield = (Map<String, Object>) rune.get("rwstatsShield");

            // Weapon
            sb.append("  Weapon:\n");
            appendStatBlock(sb, weap);

            // Armor/Helm
            sb.append("  Armor/Helm:\n");
            appendStatBlock(sb, armor);

            // Shield
            sb.append("  Shield:\n");
            appendStatBlock(sb, shield);

            sb.append("\n");
        }
        return sb.toString();
    }

    // Helper to print one stats map in a consistent, readable way
    private static void appendStatBlock(StringBuilder sb, Map<String, Object> stats) {
        if (stats == null || stats.isEmpty()) {
            sb.append("    (none)\n");
            return;
        }
        // Stable key order
        List<String> keys = new ArrayList<>(stats.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);

        for (String key : keys) {
            Object val = stats.get(key);
            sb.append("    - ").append(key).append(": ").append(stringifyValue(val)).append("\n");
        }
    }

    // Formats nested values simply (maps/lists) without JSON noise
    private static String stringifyValue(Object val) {
        if (val == null) return "null";
        if (val instanceof Map) {
            // Print compact map a:b pairs
            List<String> parts = new ArrayList<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) val).entrySet()) {
                parts.add(e.getKey() + "=" + stringifyValue(e.getValue()));
            }
            parts.sort(String.CASE_INSENSITIVE_ORDER);
            return "{" + String.join(", ", parts) + "}";
        }
        if (val instanceof Collection<?>) {
            List<String> parts = new ArrayList<>();
            for (Object o : (Collection<?>) val) parts.add(String.valueOf(o));
            return "[" + String.join(", ", parts) + "]";
        }
        return String.valueOf(val);
    }

    public static Map<String, List<String>> buildRunewordsMapLikeItemMetadata() {
        if (runeWordsParsed == null) { // ensure loaded
            getRunewordsParsed();
        }
        Map<String, List<String>> out = new LinkedHashMap<>();

        // Single-bucket runewords
        for (Map.Entry<String, Map<String, Object>> e : runeWordsParsed.entrySet()) {
            String name = e.getKey();
            @SuppressWarnings("unchecked")
            List<String> runes = (List<String>) e.getValue().get("runes");
            if (runes != null) out.put(name, new ArrayList<>(runes));
        }

        // Multi-bucket runewords (same name for different itypes)
        for (Map.Entry<String, List<Map<String, Object>>> e : runeWordsItemTypeBuckets.entrySet()) {
            String name = e.getKey();
            if (out.containsKey(name)) continue; // prefer first found; all variants share the same rune list
            List<Map<String, Object>> variants = e.getValue();
            if (variants == null || variants.isEmpty()) continue;
            @SuppressWarnings("unchecked")
            List<String> runes = (List<String>) variants.get(0).get("runes");
            if (runes != null) out.put(name, new ArrayList<>(runes));
        }

        return out;
    }

    // Simple name sanitizer: replace spaces with underscores and remove doubled single quotes.
    private static String sanitizeName(String name) {
        if (name == null) return null;
        // Replace " " with "_"
        String s = name.replace(' ', '_');
        // Replace "''" with ""
        while (s.contains("'")) {
            s = s.replace("'", "");
        }
        return s;
    }

    /**
     * Writes a JS file with: var Runewords = { Name:["Rune","Rune",...] , ... };
     * Output goes to the data/ folder next to other game data files.
     */
    public static void writeRunewordsJsFile() throws IOException {
        Map<String, List<String>> map = buildRunewordsMapLikeItemMetadata();

        StringBuilder sb = new StringBuilder(8_192);
        sb.append("var runewords = {\n");
        int i = 0, n = map.size();

        // Sort by key for stable output
        List<String> keys = new ArrayList<>(map.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);

        for (String name : keys) {
            List<String> runes = map.get(name);
            String key = sanitizeName(name);
            sb.append("\t").append(key).append(":[");

            for (int r = 0; r < runes.size(); r++) {
                if (r > 0) sb.append(",");
                sb.append("\"").append(runes.get(r)).append("\"");
            }
            sb.append("]");
            if (++i < n) sb.append(",");
            sb.append("\n");
        }
        sb.append("};\n");

        String outDir = System.getProperty("user.dir") + "\\data\\";
        java.nio.file.Path path = java.nio.file.Paths.get(outDir, "item_runewords.js");
        java.nio.file.Files.write(path, sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Wrote Runewords to: " + path.toAbsolutePath());
    }

    private static Map<String, Map<String, Object>> runeStats() {
        if (runeStats != null)
            return runeStats; //lazy load

        List<Map<String, String>> rows = null;
        try {
            rows = parseTsv(UpdateUniqueItemsStats.GEM_RUNE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        runeStats = new HashMap<>();
        for (Map<String, String> row : rows) {
            if (row.get("letter") != null && !row.get("letter").isEmpty()) {
                String keyName = row.get("letter");
                Map<String, Object> rune = new LinkedHashMap<>();
                rune.put("name", keyName);
                {
                    Map<String, Object> rowStats = new LinkedHashMap<>();
                    for (int i = 1; i <= 3; i++) {
                        String type = "weapon";
                        Map<String, Object> resolvedStats = UpdateUniqueItemsStats.resolveStat(keyName, row.get(type + "Mod" + i + "Code"),
                                row.get(type + "Mod" + i + "Max"), row.get(type + "Mod" + i + "Param"), row.get(type + "Mod" + i + "Min"), null);
                        rowStats = UpdateUniqueItemsStats.combineRows(rowStats, resolvedStats);
                    }
                    rune.put("rwstatsWeap", rowStats);
                }
                {
                    Map<String, Object> rowStats = new LinkedHashMap<>();
                    for (int i = 1; i <= 3; i++) {
                        String type = "helm";
                        Map<String, Object> resolvedStats = UpdateUniqueItemsStats.resolveStat(keyName, row.get(type + "Mod" + i + "Code"),
                                row.get(type + "Mod" + i + "Max"), row.get(type + "Mod" + i + "Param"), row.get(type + "Mod" + i + "Min"), null);
                        rowStats = UpdateUniqueItemsStats.combineRows(rowStats, resolvedStats);
                    }
                    rune.put("rwstatsArmor", rowStats);
                }
                {
                    Map<String, Object> rowStats = new LinkedHashMap<>();
                    for (int i = 1; i <= 3; i++) {
                        String type = "shield";
                        Map<String, Object> resolvedStats = UpdateUniqueItemsStats.resolveStat(keyName, row.get(type + "Mod" + i + "Code"),
                                row.get(type + "Mod" + i + "Max"), row.get(type + "Mod" + i + "Param"), row.get(type + "Mod" + i + "Min"), null);
                        rowStats = UpdateUniqueItemsStats.combineRows(rowStats, resolvedStats);
                    }
                    rune.put("rwstatsShield", rowStats);
                }
                runeStats.put(keyName, rune);
            }
        }
        return runeStats;
    }


    public static Map<String, Map<String, Object>> getRunewordsParsed() {
        if (runeWordsParsed != null)
            return runeWordsParsed;
        List<Map<String, String>> rows = null;
        try {
            rows = parseTsv(UpdateUniqueItemsStats.RUNE_WORDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //     System.out.println(rows);
        runeWordsParsed = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            if (row.get("complete") != null && row.get("complete").equals("1")) {
                String keyName = row.get("Rune Name");
                keyName = keyName.trim();
                if (runeWordRename.containsKey(keyName))
                    keyName = runeWordRename.get(keyName);
                if (!keyName.isEmpty()) {
                    Map<String, Object> rune = new LinkedHashMap<>();
                    rune.put("name", keyName);
                    rune.put("Rune1", runeIdToName.get(row.get("Rune1")));
                    rune.put("Rune2", runeIdToName.get(row.get("Rune2")));
                    rune.put("Rune3", runeIdToName.get(row.get("Rune3")));
                    rune.put("Rune4", runeIdToName.get(row.get("Rune4")));
                    rune.put("Rune5", runeIdToName.get(row.get("Rune5")));
                    rune.put("Rune6", runeIdToName.get(row.get("Rune6")));
                    List<String> Rune = new ArrayList<>();
                    if (row.get("Rune1") != null && !row.get("Rune1").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune1")));
                    if (row.get("Rune2") != null && !row.get("Rune2").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune2")));
                    if (row.get("Rune3") != null && !row.get("Rune3").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune3")));
                    if (row.get("Rune4") != null && !row.get("Rune4").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune4")));
                    if (row.get("Rune5") != null && !row.get("Rune5").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune5")));
                    if (row.get("Rune6") != null && !row.get("Rune6").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune6")));
                    if (row.get("Rune7") != null && !row.get("Rune7").isEmpty())
                        Rune.add(runeIdToName.get(row.get("Rune7")));
                    rune.put("runes", Rune);
                    List<String> itype = new ArrayList<>();
                    if (row.get("itype1") != null && !row.get("itype1").isEmpty())
                        itype.add(row.get("itype1"));
                    if (row.get("itype2") != null && !row.get("itype2").isEmpty())
                        itype.add(row.get("itype2"));
                    if (row.get("itype3") != null && !row.get("itype3").isEmpty())
                        itype.add(row.get("itype3"));
                    if (row.get("itype4") != null && !row.get("itype4").isEmpty())
                        itype.add(row.get("itype4"));
                    if (row.get("itype5") != null && !row.get("itype5").isEmpty())
                        itype.add(row.get("itype5"));
                    if (row.get("itype6") != null && !row.get("itype6").isEmpty())
                        itype.add(row.get("itype6"));
                    if (row.get("itype7") != null && !row.get("itype7").isEmpty())
                        itype.add(row.get("itype7"));

                    rune.put("itype", itype);
                    Map<String, Object> rowStats = new LinkedHashMap<>();
                    for (int i = 1; i <= 7; i++) {
                        Map<String, Object> resolvedStats = UpdateUniqueItemsStats.resolveStat(keyName, row.get("T1Code" + i),
                                row.get("T1Max" + i), row.get("T1Param" + i), row.get("T1Min" + i), null);
                        rowStats = UpdateUniqueItemsStats.combineRows(rowStats, resolvedStats);
                    }
                    rune.put("rwstats", rowStats);
                    if (runeWordsParsed.containsKey(keyName)) {
                        List<Map<String, Object>> runeWordsItemTypeBucketsList = new ArrayList<>();
                        runeWordsItemTypeBucketsList.add(rune);
                        runeWordsItemTypeBucketsList.add(runeWordsParsed.get(keyName));
                        runeWordsItemTypeBuckets.put(keyName, runeWordsItemTypeBucketsList);
                        runeWordsParsed.remove(keyName);
                    } else if (runeWordsItemTypeBuckets.containsKey(keyName)) {
                        List<Map<String, Object>> runeWordsItemTypeBucketsList = runeWordsItemTypeBuckets.get(keyName);
                        runeWordsItemTypeBucketsList.add(rune);
                        runeWordsItemTypeBuckets.put(keyName, runeWordsItemTypeBucketsList);

                    } else {
                        runeWordsParsed.put(keyName, rune);
                    }
                }
            } else {
                //  System.err.println("Incomplete runeword: " + row);
            }
        }

        return runeWordsParsed;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getRuneword(String keyName, String itemGroupType) {
        final Map<String, Object> runeWordStats = getRunewordOnlyStats(keyName, itemGroupType);
        if (runeWordStats == null)
            return null;

        if (itemGroupType != null && itemGroupType.length() > 0) {
            List<String> runes = (List<String>) runeWordStats.get("runes");
            Map<String, Object> runesStats = new HashMap<>();
            for (String rune : runes) {
                if (rune == null || rune.isEmpty()) continue;
                Map<String, Object> resolvedStats = new HashMap<>();
                if (WeaponGroupTypeUtil.isWeapon(itemGroupType.toLowerCase())) {
                    resolvedStats = (Map<String, Object>) runeStats().get(rune).get("rwstatsWeap");
                } else if (itemGroupType.equalsIgnoreCase("armor") || itemGroupType.equalsIgnoreCase("Helm")) {
                    resolvedStats = (Map<String, Object>) runeStats().get(rune).get("rwstatsArmor");
                } else if (itemGroupType.equalsIgnoreCase("shield")) {
                    resolvedStats = (Map<String, Object>) runeStats().get(rune).get("rwstatsShield");
                } else {
                    System.err.println("Unkown Type: " + itemGroupType);
                }
                runesStats = UpdateUniqueItemsStats.combineRows(runesStats, resolvedStats);
            }
            runesStats = UpdateUniqueItemsStats.combineRows((Map<String, Object>) runeWordStats.get("rwstats"), runesStats);
            runeWordStats.put("rwstats", runesStats);
        }
        return runeWordStats;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getRunewordOnlyStats(String keyName, String itemGroupType) {
        if (runeWordsParsed == null) // lazy load
            getRunewordsParsed();
        if (runeWordsParsed.containsKey(keyName))
            return getRunewordsParsed().get(keyName);
        if (runeWordsItemTypeBuckets.containsKey(keyName)) {
            String weaponGroupType = WeaponGroupTypeUtil.groupOf(itemGroupType);
            if (weaponGroupType == null) {
                if (ITYPE_MAP.containsKey(itemGroupType)) {
                    weaponGroupType = ITYPE_MAP.get(itemGroupType);
                } else {
                    System.err.println("Group of " + itemGroupType + " is null");
                }
            }
            if (ITYPE_MAP.containsKey(weaponGroupType))
                weaponGroupType = ITYPE_MAP.get(weaponGroupType);
            Map<String, Object> rune = getStringObjectMap(keyName, weaponGroupType);
            if (rune != null) return rune;


            if (weaponGroupType != null && weaponGroupType.equals("swor"))
                rune = getStringObjectMap(keyName, "2hsw");
            if (rune != null) return rune;

            System.err.println(weaponGroupType + " " + itemGroupType);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getStringObjectMap(String keyName, String weaponGroupType) {
        List<Map<String, Object>> runeWordsItemTypeBucketsList = runeWordsItemTypeBuckets.get(keyName);
        for (Map<String, Object> rune : runeWordsItemTypeBucketsList) {
            //  System.err.println(rune);
            if (((List<String>) rune.get("itype")).contains(weaponGroupType)) {
                return rune;
            }
        }
        return null;
    }

    private static final Map<String, String> ITYPE_MAP = new LinkedHashMap<String, String>() {{
        put("weap", "weap");
        put("swor", "swor");
        put("sword", "swor");
        put("axe", "axe");
        put("mace", "mace");
        put("hamm", "hamm");
        put("scep", "scep");
        put("pole", "pole");
        put("spea", "spea");
        put("spear", "spea");
        put("staf", "staf");
        put("staff", "staf");
        put("wand", "wand");
        put("club", "club");
        put("miss", "miss");
        put("crossbow", "miss");
        put("jave", "jave");
        put("aspe", "aspe");
        put("thro", "thro");
        put("tkni", "tkni");
        put("h2h", "h2h");
        put("h2h2", "h2h2");
        put("shld", "shld");
        put("shield", "shld");
        put("Offhand", "shld");
        put("offhand", "shld");
        put("helm", "helm");
        put("tors", "tors");
        put("Armor", "tors");
        put("2hsw", "2hsw");
        put("pala", "pala");
        put("sc9", "sc9");
        put("sorc", "sorc");
        put("mele", "mele");
    }};

    private static Map<String, String> runeWordRename = new LinkedHashMap<String, String>() {{
        put("Doomsayer", "Doom");
        put("Widowmaker", "Grief");
        put("Exile's Path", "Exile");
        put("Bound by Duty", "Chains of Honor");
        put("The Beast", "Beast");
        //Widowmaker
    }};

    private static Map<String, String> runeIdToName = new LinkedHashMap<String, String>() {{
        put(null, null);
        put("", "");
        put("r01", "El");
        put("r02", "Eld");
        put("r03", "Tir");
        put("r04", "Nef");
        put("r05", "Eth");
        put("r06", "Ith");
        put("r07", "Tal");
        put("r08", "Ral");
        put("r09", "Ort");
        put("r10", "Thul");
        put("r11", "Amn");
        put("r12", "Sol");
        put("r13", "Shael");
        put("r14", "Dol");
        put("r15", "Hel");
        put("r16", "Io");
        put("r17", "Lum");
        put("r18", "Ko");
        put("r19", "Fal");
        put("r20", "Lem");
        put("r21", "Pul");
        put("r22", "Um");
        put("r23", "Mal");
        put("r24", "Ist");
        put("r25", "Gul");
        put("r26", "Vex");
        put("r27", "Ohm");
        put("r28", "Lo");
        put("r29", "Sur");
        put("r30", "Ber");
        put("r31", "Jah");
        put("r32", "Cham");
        put("r33", "Zod");
    }};

    private static List<Map<String, String>> parseTsv(String path) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            String line;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                // Normalize BOM and trim right-side spaces
                line = stripBom(line).trim();
                if (line.isEmpty()) continue;

                // Split by tab preserving empty cells
                String[] parts = line.split("\t", -1);

                if (headers == null) {
                    headers = parts;
                    // Normalize header keys (trim)
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = headers[i] == null ? "" : headers[i].trim();
                    }
                    continue;
                }

                Map<String, String> obj = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].isEmpty() ? ("col_" + i) : headers[i];
                    String value = i < parts.length ? parts[i] : "";
                    // Keep raw value; consumer may parse numbers if needed
                    obj.put(key, value);
                }
                result.add(obj);
            }
        }
        return result;
    }

    private static String toJson(List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            sb.append("  {");
            int j = 0;
            for (Map.Entry<String, String> e : row.entrySet()) {
                if (j++ > 0) sb.append(", ");
                sb.append("\"").append(escapeJson(e.getKey())).append("\": ");
                String v = e.getValue();
                if (v == null || v.isEmpty()) {
                    sb.append("null");
                } else if (isNumeric(v)) {
                    sb.append(v);
                } else if (isBooleanLike(v)) {
                    sb.append(v.equalsIgnoreCase("true") ? "true" : "false");
                } else {
                    sb.append("\"").append(escapeJson(v)).append("\"");
                }
            }
            sb.append("}");
            if (i < rows.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]\n");
        return sb.toString();
    }

    private static boolean isNumeric(String s) {
        // Accept integers or decimals, optionally negative
        return s.matches("-?\\d+(\\.\\d+)?");
    }

    private static boolean isBooleanLike(String s) {
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
    }

    private static String escapeJson(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\"':
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
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
}
