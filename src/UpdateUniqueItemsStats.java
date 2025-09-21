import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UpdateUniqueItemsStats {
    // Configuration: replace with your actual paths
    private static final String dir = System.getProperty("user.dir") + "\\data\\";
    private static final String UNIQUE_ITEMS_PATH = dir + "UniqueItems.txt";
    private static final String OUTPUT_DIR = dir;

    // File naming: new file each run like equipment-YYYYMMDD-HHmmss.js
    private static final String OUTPUT_BASENAME = "equipment";

    private static final boolean buildProd = false;
    private static final String PROD_OUTPUT_BASENAME = "items_equipment.js";

    public static void main(String[] args) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(UNIQUE_ITEMS_PATH), StandardCharsets.UTF_8);
            if (lines.isEmpty()) {
                System.out.println("Input is empty: " + UNIQUE_ITEMS_PATH);
                return;
            }

            String[] header = splitTSV(lines.get(0));
            Map<String, Integer> h = headerIndex(header);

            // Required columns
            int idxName = h.getOrDefault("index", -1);
            int idxType = h.getOrDefault("*type", -1);
            int idxEnabled = h.getOrDefault("enabled", 2);
            int idxReqLevel = h.getOrDefault("lvl req", 8); // fallback to 3rd column if header missing

            if (idxName < 0 || idxType < 0) {
                System.err.println("Missing required headers: index and/or *type");
                return;
            }

            // Precompute prop/max indices for 1..11
            int[] idxProp = new int[12]; // 1..11 used
            int[] idxMax = new int[12];
            Arrays.fill(idxProp, -1);
            Arrays.fill(idxMax, -1);
            for (int i = 1; i <= 11; i++) {
                idxProp[i] = h.getOrDefault("prop" + i, -1);
                idxMax[i] = h.getOrDefault("max" + i, -1);
            }

            Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
            int totalRows = 0;
            int keptRows = 0;

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.trim().isEmpty()) continue;

                String[] cols = splitTSV(line);
                totalRows++;

                // Must have "enabled" == "1"
                String enabled = safeGet(cols, idxEnabled).trim();
                if (!"1".equals(enabled)) continue;

                String baseType = safeGet(cols, idxType).trim();
                String name = safeGet(cols, idxName).trim();
                String reqLevel = safeGet(cols, idxReqLevel).trim();

                if (baseType.isEmpty() || name.isEmpty()) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", name);
                if (!baseType.equalsIgnoreCase("ring") && !baseType.equalsIgnoreCase("amulet"))
                    row.put("base", baseType);
                row.put("req_level", parseNumericOrString(reqLevel));
                String onlyClass = itemmap.classForBaseOrNull(baseType);
                if (onlyClass != null) {
                    row.put("only", onlyClass);
                }
                if (TwoHandedWeaponUtil.isTwoHandedBase(baseType)) {
                    row.put("twoHanded", 1);
                }

                // Add prop1..prop11 with their max values, using the prop value as the key
                for (int p = 1; p <= 11; p++) {
                    int pIdx = idxProp[p];
                    int mIdx = idxMax[p];
                    if (pIdx < 0 || mIdx < 0) continue;

                    String propKey = safeGet(cols, pIdx).trim();
                    String maxValStr = safeGet(cols, mIdx).trim();
                    if (propKey.isEmpty() || maxValStr.isEmpty()) continue;

                    Object val = parseNumericOrString(maxValStr);
                    String plannerPropKey = itemmap.PROP_MAP.get(propKey);

                    // Skip unknown properties instead of inserting a null key
                    if (plannerPropKey == null || plannerPropKey.isBlank()) {
                        continue;
                    }
                    if (propKey.contains("%"))
                        continue;// TODO remove
                    if (propKey.contains("-"))
                        continue;// TODO remove
                    if (propKey.contains("/"))
                        continue;// TODO remove
                    row.put(plannerPropKey, val);
                }

                String groupBaseType = itemmap.TYPE_MAP.get(baseType);
                if (groupBaseType != null && !groupBaseType.isEmpty()) {
                    if (itemmap.TYPES.contains(groupBaseType)) {
                        String itemGroupType = WeaponGroupTypeUtil.groupOf(baseType);
                        if (itemGroupType != null) {
                            row.put("type", itemGroupType);
                        } else if (groupBaseType.equals("Offhand")) {
                            row.put("type", "Shield");
                        }
                        if (!groupBaseType.equals("Amulet") && !groupBaseType.equals("Ring1"))
                            row.put("img", name.replace(" ", "_"));
                        String groupBaseTypeKeyname = groupBaseType.toLowerCase();
                       /* if (!grouped.containsKey(groupBaseTypeKeyname)) {
                            Map<String, Object> rowheader = new LinkedHashMap<>();
                            rowheader.put("name", groupBaseType.replace("1", ""));
                            grouped.computeIfAbsent(groupBaseTypeKeyname, k -> new ArrayList<>()).add(rowheader);
                        }*/
                        grouped.computeIfAbsent(groupBaseTypeKeyname, k -> new ArrayList<>()).add(row);
                        keptRows++;
                    } else {
                        System.err.println("No type match on: " + baseType);
                    }
                } else {
                    // System.err.println("No match on: " + baseType);
                }
            }

            String js = buildEquipmentJs(grouped);

            Path outDir = Paths.get(OUTPUT_DIR);
            Files.createDirectories(outDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            final Path outFile;
            if (buildProd) {
                outFile = outDir.resolve(PROD_OUTPUT_BASENAME);
            } else {
                outFile = outDir.resolve(OUTPUT_BASENAME + "-" + timestamp + ".js");
            }
            Files.write(outFile, js.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

            System.out.println("Wrote: " + outFile.toAbsolutePath());
            System.out.println("Total rows read: " + totalRows);
            System.out.println("Rows kept (enabled == 1): " + keptRows);
            System.out.println("Groups: " + grouped.size());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String[] splitTSV(String line) {
        return line.split("\t", -1); // keep empty trailing columns
    }

    private static Map<String, Integer> headerIndex(String[] header) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i].trim(), i);
        }
        return map;
    }

    private static String safeGet(String[] arr, int idx) {
        return (idx >= 0 && idx < arr.length) ? arr[idx] : "";
    }

    private static Object parseNumericOrString(String s) {
        try {
            if (s.contains(".")) {
                return Double.parseDouble(s);
            }
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return s;
        }
    }

    private static String buildEquipmentJs(Map<String, List<Map<String, Object>>> grouped) {
        StringBuilder sb = new StringBuilder();
        sb.append("var equipment = ");
        sb.append("{\n");

        int gi = 0;
        int gsize = grouped.size();
        for (Map.Entry<String, List<Map<String, Object>>> ge : grouped.entrySet()) {
            String groupKey = ge.getKey();
            List<Map<String, Object>> rows = ge.getValue();
            // Sort Alphabetically
            rows.sort(Comparator.comparing(
                    (Map<String, Object> m) -> m.get("name") == null ? null : m.get("name").toString(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            ));

            //sb.append("  \"").append(escapeJsString(groupKey)).append("\": [\n");
            sb.append(groupKey).append(": [\n{name:\"" + groupKey + "\"},\n");
            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);
                sb.append("    {");
                int ci = 0;
                int csize = row.size();
                for (Map.Entry<String, Object> ce : row.entrySet()) {
                    // sb.append("\"").append(escapeJsString(ce.getKey())).append("\": ");
                    sb.append(escapeJsString(ce.getKey())).append(": ");
                    Object val = ce.getValue();
                    if (val instanceof Number) {
                        sb.append(val.toString());
                    } else {
                        sb.append("\"").append(escapeJsString(String.valueOf(val))).append("\"");
                    }
                    if (++ci < csize) sb.append(", ");
                }
                sb.append("}");
                sb.append(",");
                sb.append("\n");
            }

            sb.append("  ]");
            sb.append(",");
            sb.append("\n");
        }
        sb.append(footer);
        sb.append("};\n");
        return sb.toString();
    }

    private static String escapeJsString(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
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
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        return out.toString();
    }


    private static final String footer = "\n ring2: [],\n\n    charms: [\n" +
            "{name:\"Charms\"},\n" +
            "{name:\"Annihilus\", size:\"small\", req_level:80, all_skills:1, all_attributes:20, all_res:20, experience:10},\n" +
            "{name:\"Hellfire Torch\", size:\"large\", req_level:75, skills_class:2, vitality:60, energy:20, all_res:20, light_radius:8},\n" +
            "{name:\"Gheed's Fortune\", size:\"grand\", req_level:62, gf:160, mf:40, discount:15, pod:1},\n" +
            "{only:\"amazon\", rarity:\"magic\", name:\"+1 Harpoonist's Grand Charm\", size:\"grand\", req_level:42, skills_javelins:1},\n" +
            "{only:\"amazon\", rarity:\"magic\", name:\"+1 Acrobat's Grand Charm\", size:\"grand\", req_level:42, skills_passives:1},\n" +
            "{only:\"amazon\", rarity:\"magic\", name:\"+1 Fletcher's Grand Charm\", size:\"grand\", req_level:42, skills_bows:1},\n" +
            "{only:\"assassin\", rarity:\"magic\", name:\"+1 Shogukusha's Grand Charm\", size:\"grand\", req_level:42, skills_martial:1},\n" +
            "{only:\"assassin\", rarity:\"magic\", name:\"+1 Mentalist's Grand Charm\", size:\"grand\", req_level:42, skills_shadow:1},\n" +
            "{only:\"assassin\", rarity:\"magic\", name:\"+1 Entrapping Grand Charm\", size:\"grand\", req_level:42, skills_traps:1},\n" +
            "{only:\"barbarian\", rarity:\"magic\", name:\"+1 Sounding Grand Charm\", size:\"grand\", req_level:42, skills_warcries:1},\n" +
            "{only:\"barbarian\", rarity:\"magic\", name:\"+1 Fanatic Grand Charm\", size:\"grand\", req_level:42, skills_masteries:1},\n" +
            "{only:\"barbarian\", rarity:\"magic\", name:\"+1 Expert's Grand Charm\", size:\"grand\", req_level:42, skills_combat_barbarian:1},\n" +
            "{only:\"druid\", rarity:\"magic\", name:\"+1 Nature's Grand Charm\", size:\"grand\", req_level:42, skills_elemental:1},\n" +
            "{only:\"druid\", rarity:\"magic\", name:\"+1 Spiritual Grand Charm\", size:\"grand\", req_level:42, skills_shapeshifting:1},\n" +
            "{only:\"druid\", rarity:\"magic\", name:\"+1 Trainer's Grand Charm\", size:\"grand\", req_level:42, skills_summoning_druid:1},\n" +
            "{only:\"necromancer\", rarity:\"magic\", name:\"+1 Graverobber's Grand Charm\", size:\"grand\", req_level:42, skills_summoning_necromancer:1},\n" +
            "{only:\"necromancer\", rarity:\"magic\", name:\"+1 Fungal Grand Charm\", size:\"grand\", req_level:42, skills_poisonBone:1},\n" +
            "{only:\"necromancer\", rarity:\"magic\", name:\"+1 Hexing Grand Charm\", size:\"grand\", req_level:42, skills_curses:1},\n" +
            "{only:\"paladin\", rarity:\"magic\", name:\"+1 Preserver's Grand Charm\", size:\"grand\", req_level:42, skills_defensive:1},\n" +
            "{only:\"paladin\", rarity:\"magic\", name:\"+1 Captain's Grand Charm\", size:\"grand\", req_level:42, skills_offensive:1},\n" +
            "{only:\"paladin\", rarity:\"magic\", name:\"+1 Lion Branded Grand Charm\", size:\"grand\", req_level:42, skills_combat_paladin:1},\n" +
            "{only:\"sorceress\", rarity:\"magic\", name:\"+1 Chilling Grand Charm\", size:\"grand\", req_level:42, skills_cold:1},\n" +
            "{only:\"sorceress\", rarity:\"magic\", name:\"+1 Sparking Grand Charm\", size:\"grand\", req_level:42, skills_lightning:1},\n" +
            "{only:\"sorceress\", rarity:\"magic\", name:\"+1 Burning Grand Charm\", size:\"grand\", req_level:42, skills_fire:1},\n" +
            "{rarity:\"magic\", name:\"Serpent's Small Charm of Vita\", size:\"small\", req_level:40, mana:17, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Shimmering Small Charm of Inertia\", size:\"small\", req_level:36, all_res:5, frw:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Shimmering Small Charm of Good Luck\", size:\"small\", req_level:33, all_res:5, mf:7, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Shimmering Small Charm of Vita\", size:\"small\", req_level:39, all_res:5, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Ruby Small Charm of Vita\", size:\"small\", req_level:39, fRes:11, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Sapphire Small Charm of Vita\", size:\"small\", req_level:39, cRes:11, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Amber Small Charm of Vita\", size:\"small\", req_level:39, lRes:11, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Emerald Small Charm of Vita\", size:\"small\", req_level:39, pRes:11, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Fine Small Charm of Balance\", size:\"small\", req_level:29, damage_max:3, ar:20, fhr:5, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Fine Small Charm of Inertia\", size:\"small\", req_level:36, damage_max:3, ar:20, frw:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Pestilent Small Charm of Anthrax\", size:\"small\", req_level:80, pDamage_all:451, pDamage_duration:12, pod:1},\n" +
            "{rarity:\"magic\", name:\"Fine Small Charm of Vita\", size:\"small\", req_level:39, damage_max:3, ar:20, life:20, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Sharp Large Charm of Vita\", size:\"large\", req_level:66, damage_max:6, ar:48, life:35, pd2:1},\n" +
            "{rarity:\"magic\", name:\"Sharp Grand Charm of Vita\", size:\"grand\", req_level:83, damage_max:10, ar:76, life:45, pd2:1},\n" +
            "{rarity:\"magic\", name:\"+3% Inferno Large Charm\", size:\"large\", req_level:42, fDamage:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"+3% Numbing Large Charm\", size:\"large\", req_level:42, cDamage:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"+3% Conduit Large Charm\", size:\"large\", req_level:42, lDamage:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"+3% Infectious Large Charm\", size:\"large\", req_level:42, pDamage:3, pd2:1},\n" +
            "{rarity:\"magic\", name:\"+3% Scintillating Large Charm\", size:\"large\", req_level:42, mDamage:3, pd2:1}," +
            "]";
}