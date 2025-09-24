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

    private static final boolean buildProd = true;
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
            int[] idxProp = new int[12];
            int[] idxPar = new int[12];
            int[] idxMin = new int[12];
            int[] idxMax = new int[12];
            Arrays.fill(idxProp, -1);
            Arrays.fill(idxMax, -1);
            for (int i = 1; i <= 11; i++) {
                idxProp[i] = h.getOrDefault("prop" + i, -1);
                idxPar[i] = h.getOrDefault("par" + i, -1);
                idxMin[i] = h.getOrDefault("min" + i, -1);
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

                String baseType = itemmap.checkForRename(safeGet(cols, idxType).trim());
                String name = itemmap.checkForRename(safeGet(cols, idxName).trim());

                if (itemmap.skipCheck(name)) {
                    continue;
                }
                String reqLevel = safeGet(cols, idxReqLevel).trim();

                if (baseType.isEmpty() || name.isEmpty()) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("name", name);
                if (!baseType.equalsIgnoreCase("ring") && !baseType.equalsIgnoreCase("amulet"))
                    row.put("base", removeNumbersAndCapitalizeFirst(baseType));
                row.put("req_level", parseNumericOrString(reqLevel));
                String onlyClass = itemmap.classForBaseOrNull(baseType);
                if (onlyClass != null) {
                    row.put("only", onlyClass);
                }
                if (TwoHandedWeaponUtil.isTwoHandedBase(baseType)) {
                    row.put("twoHanded", 1);
                }

                switch (name) {
                    case "Overlord's Helm":
                    case "Dark Abyss":
                    case "Itherael's Path":
                    case "Hadriel's Hand":
                    case "Aidan's Scar":
                        row.put("boss_item", "Uber Diablo");
                        break;
                    case "The Third Eye":
                    case "Band of Skulls":
                    case "Cage of the Unsullied":
                        row.put("boss_item", "Rathma");
                        break;
                    default:
                }

                // Add prop1..prop11 with their max values, using the prop value as the key
                for (int p = 1; p <= 11; p++) {
                    int pIdx = idxProp[p];
                    int parIdx = idxPar[p];
                    int minIdx = idxMin[p];
                    int maxIdx = idxMax[p];
                    if (pIdx < 0 || maxIdx < 0) continue;

                    String propKey = safeGet(cols, pIdx).trim();
                    String parameter = safeGet(cols, parIdx).trim();
                    String minValStr = safeGet(cols, minIdx).trim();
                    String maxValStr = safeGet(cols, maxIdx).trim();
                    if (propKey.isEmpty() || maxValStr.isEmpty()) continue;

                    Object val = parseNumericOrString(maxValStr);


                    String plannerPropKey = itemmap.PROP_MAP.get(propKey);

                    //skilltab
                    if (propKey.equals("skilltab")) {
                        /*
                        {only:"amazon", rarity:"magic", name:"+1 Harpoonist's Grand Charm", size:"grand", req_level:42, skills_javelins:1},
{only:"amazon", rarity:"magic", name:"+1 Acrobat's Grand Charm", size:"grand", req_level:42, skills_passives:1},
skills_bows
{only:"assassin", rarity:"magic", name:"+1 Shogukusha's Grand Charm", size:"grand", req_level:42, skills_martial:1},
{only:"assassin", rarity:"magic", name:"+1 Mentalist's Grand Charm", size:"grand", req_level:42, skills_shadow:1},
{only:"assassin", rarity:"magic", name:"+1 Entrapping Grand Charm", size:"grand", req_level:42, skills_traps:1},
{only:"barbarian", rarity:"magic", name:"+1 Sounding Grand Charm", size:"grand", req_level:42, skills_warcries:1},
{only:"barbarian", rarity:"magic", name:"+1 Fanatic Grand Charm", size:"grand", req_level:42, skills_masteries:1},
{only:"barbarian", rarity:"magic", name:"+1 Expert's Grand Charm", size:"grand", req_level:42, skills_combat_barbarian:1},
{only:"druid", rarity:"magic", name:"+1 Nature's Grand Charm", size:"grand", req_level:42, skills_elemental:1},
{only:"druid", rarity:"magic", name:"+1 Spiritual Grand Charm", size:"grand", req_level:42, skills_shapeshifting:1},
{only:"druid", rarity:"magic", name:"+1 Trainer's Grand Charm", size:"grand", req_level:42, skills_summoning_druid:1},
{only:"necromancer", rarity:"magic", name:"+1 Graverobber's Grand Charm", size:"grand", req_level:42, skills_summoning_necromancer:1},
{only:"necromancer", rarity:"magic", name:"+1 Fungal Grand Charm", size:"grand", req_level:42, skills_poisonBone:1},
{only:"necromancer", rarity:"magic", name:"+1 Hexing Grand Charm", size:"grand", req_level:42, skills_curses:1},
{only:"paladin", rarity:"magic", name:"+1 Preserver's Grand Charm", size:"grand", req_level:42, skills_defensive:1},
{only:"paladin", rarity:"magic", name:"+1 Captain's Grand Charm", size:"grand", req_level:42, skills_offensive:1},
{only:"paladin", rarity:"magic", name:"+1 Lion Branded Grand Charm", size:"grand", req_level:42, skills_combat_paladin:1},
{only:"sorceress", rarity:"magic", name:"+1 Chilling Grand Charm", size:"grand", req_level:42, skills_cold:1},
{only:"sorceress", rarity:"magic", name:"+1 Sparking Grand Charm", size:"grand", req_level:42, skills_lightning:1},
{only:"sorceress", rarity:"magic", name:"+1 Burning Grand Charm", size:"grand", req_level:42, skills_fire:1},
TABSK0	Amazon	Bow and Crossbow Skills
TABSK1	Amazon	Passive and Magic Skills
TABSK2	Amazon	Javelin and Spear Skills
TABSK8	Sorceress	Fire Spells
TABSK9	Sorceress	Lightning Spells
TABSK10	Sorceress	Cold Spells
TABSK16	Necromancer	Curses
TABSK17	Necromancer	Poison & Bone Spells
TABSK18	Necromancer	Summoning Spells
TABSK24	Paladin	Combat Skills
TABSK25	Paladin	Offensive Auras
TABSK26	Paladin	Defensive Auras
TABSK32	Barbarian	Combat Skills
TABSK33	Barbarian	Combat Masteries
TABSK34	Barbarian	Warcries
TABSK40	Druid	Summoning
TABSK41	Druid	Shape Shifting
TABSK42	Druid	Elemental
TABSK48	Assassin	Traps
TABSK49	Assassin	Shadow Disciplines
TABSK50	Assassin	Martial Arts
                         */
                        switch (parameter.trim()) {
                            case "0":
                                plannerPropKey = "skills_bows";
                                break;
                            case "1":
                                plannerPropKey = "skills_passives";
                                break;
                            case "2":
                                plannerPropKey = "skills_javelins";
                                break;
                            case "3":
                                plannerPropKey = "skills_fire";
                                break;
                            case "4":
                                plannerPropKey = "skills_lightning";
                                break;
                            case "5":
                                plannerPropKey = "skills_cold";
                                break;
                            case "6":
                                plannerPropKey = "skills_curses";
                                break;
                            case "7":
                                plannerPropKey = "skills_poisonBone";
                                break;
                            case "8":
                                plannerPropKey = "skills_summoning_necromancer";
                                break;
                            case "9":
                                plannerPropKey = "skills_combat_paladin";
                                break;
                            case "10":
                                plannerPropKey = "skills_offensive";
                                break;
                            case "11":
                                plannerPropKey = "skills_defensive";
                                break;
                            case "12":
                                plannerPropKey = "skills_combat_barbarian";
                                break;
                            case "13":
                                plannerPropKey = "skills_masteries";
                                break;
                            case "14":
                                plannerPropKey = "skills_warcries";
                                break;
                            case "15":
                                plannerPropKey = "skills_summoning_druid";
                                break;
                            case "16":
                                plannerPropKey = "skills_shapeshifting";
                                break;
                            case "17":
                                plannerPropKey = "skills_elemental";
                                break;
                            case "18":
                                plannerPropKey = "skills_traps";
                                break;
                            case "19":
                                plannerPropKey = "skills_shadow";
                                break;
                            case "20":
                                plannerPropKey = "skills_martial";
                                break;
                            default:
                                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                                continue;
                        }
                    }

                    //skill

                    if (propKey.equals("skill") || propKey.equals("oskill")) {
                        String skillname = getSkillName(parameter);
                        if (skillname == null) {
                            System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                                    parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                            continue;
                        }
                        plannerPropKey = propKey + "_" + (skillname.replace(" ", "_"));
                    }

                    /*
                        System.err.println("propKey: " + propKey + " parameter: " +
                                parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                        continue;
                     */

                    // Skip unknown properties instead of inserting a null key
                    if (plannerPropKey == null || plannerPropKey.isBlank()) {
                        if (propKey != null & !propKey.isEmpty()) {
                            if (!propKey.startsWith("map-"))
                                System.err.println("Unknown prop: " + propKey);
                        }
                        continue;
                    }
                    if (plannerPropKey.contains("%"))
                        continue;// TODO remove
                    if (plannerPropKey.contains("-"))
                        continue;// TODO remove
                    if (plannerPropKey.contains("/"))
                        continue;// TODO remove
                    row.put(plannerPropKey, val);
                }

                String groupBaseType = itemmap.getGroupBaseType(baseType);
                if (groupBaseType != null && !groupBaseType.isEmpty()) {
                    if (itemmap.typeChecker(groupBaseType)) {
                        String itemGroupType = WeaponGroupTypeUtil.groupOf(baseType);
                        if (itemGroupType != null) {
                            row.put("type", itemGroupType);
                        } else if (groupBaseType.equals("Offhand")) {
                            row.put("type", "shield");
                        } else {
                            //    System.err.println("itemGroupType = null for " + baseType);
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
                    if (!(baseType.equals("charm") || baseType.equals("jewel") || baseType.startsWith("t5")))
                        System.err.println("No match on: " + baseType);
                }
            }

            String js = buildEquipmentJs(grouped);

            Path outDir = Paths.get(OUTPUT_DIR);
            Files.createDirectories(outDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            final Path outFile;
            if (buildProd) {
                outFile = outDir.resolve(PROD_OUTPUT_BASENAME);
                Files.write(outFile, js.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                outFile = outDir.resolve(OUTPUT_BASENAME + "-" + timestamp + ".js");
                Files.write(outFile, js.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);

            }
            System.out.println("Wrote: " + outFile.toAbsolutePath());
            System.out.println("Total rows read: " + totalRows);
            System.out.println("Rows kept (enabled == 1): " + keptRows);
            System.out.println("Groups: " + grouped.size());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String removeNumbersAndCapitalizeFirst(String input) {
        if (input == null) {
            return null;
        }
        // Remove all digits
        String cleaned = input.replaceAll("\\d+", "");
        if (cleaned.isEmpty()) {
            return cleaned;
        }

        // Capitalize the first letter of every word (separated by whitespace)
        StringBuilder sb = new StringBuilder(cleaned.length());
        boolean capitalizeNext = true;

        for (int i = 0; i < cleaned.length(); ) {
            int cp = cleaned.codePointAt(i);
            int count = Character.charCount(cp);

            if (capitalizeNext && Character.isLetter(cp)) {
                String upper = new String(Character.toChars(cp)).toUpperCase(java.util.Locale.ROOT);
                sb.append(upper);
                capitalizeNext = false;
            } else {
                sb.appendCodePoint(cp);
            }

            if (Character.isWhitespace(cp)) {
                capitalizeNext = true;
            }

            i += count;
        }

        return sb.toString();
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
            sb.append(groupKey).append(": [\n{name:\"" + removeNumbersAndCapitalizeFirst(groupKey) + "\"},\n");
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


    public static String getSkillName(String skNumber) {
        switch (skNumber.toLowerCase()) {
            // Amazon SK6-SK35
            case "6":
            case "magic arrow":
                return "Magic Arrow";
            case "7":
            case "fire arrow":
                return "Fire Arrow";
            case "8":
            case "inner sight":
                return "Inner Sight";
            case "9":
            case "critical strike":
                return "Critical Strike";
            case "10":
            case "jab":
                return "Jab";
            case "11":
            case "cold arrow":
                return "Cold Arrow";
            case "12":
            case "multiple shot":
                return "Multiple Shot";
            case "13":
            case "dodge":
                return "Dodge";
            case "14":
            case "power strike":
                return "Power Strike";
            case "15":
            case "poison javelin":
                return "Poison Javelin";
            case "16":
            case "exploding arrow":
                return "Exploding Arrow";
            case "17":
            case "slow movement":
                return "Slow Movement";
            case "18":
            case "avoid":
                return "Avoid";
            case "19":
            case "javelin and spear mastery":
                return "Javelin and Spear Mastery";
            case "20":
            case "lightning bolt":
                return "Lightning Bolt";
            case "21":
            case "ice arrow":
                return "Ice Arrow";
            case "22":
            case "guided arrow":
                return "Guided Arrow";
            case "23":
            case "penetrate":
                return "Penetrate";
            case "24":
            case "charged strike":
                return "Charged Strike";
            case "25":
            case "plague javelin":
                return "Plague Javelin";
            case "26":
            case "strafe":
                return "Strafe";
            case "27":
            case "immolation arrow":
                return "Immolation Arrow";
            case "28":
            case "decoy":
                return "Decoy";
            case "29":
            case "evade":
                return "Evade";
            case "30":
            case "fend":
                return "Fend";
            case "31":
            case "freezing arrow":
                return "Freezing Arrow";
            case "32":
            case "valkyrie":
                return "Valkyrie";
            case "33":
            case "pierce":
                return "Pierce";
            case "34":
            case "lightning strike":
                return "Lightning Strike";
            case "35":
            case "lightning fury":
                return "Lightning Fury";

            // Sorceress SK36-SK65, SK369, SK376, SK383
            case "36":
            case "fire bolt":
                return "Fire Bolt";
            case "37":
            case "warmth":
                return "Warmth";
            case "38":
            case "charged bolt":
                return "Charged Bolt";
            case "39":
            case "ice bolt":
                return "Ice Bolt";
            case "40":
            case "cold enchant":
                return "Cold Enchant";
            case "41":
            case "inferno":
                return "Inferno";
            case "42":
            case "static field":
                return "Static Field";
            case "43":
            case "telekinesis":
                return "Telekinesis";
            case "44":
            case "frost nova":
                return "Frost Nova";
            case "45":
            case "ice blast":
                return "Ice Blast";
            case "46":
            case "blaze":
                return "Blaze";
            case "47":
            case "fire ball":
                return "Fire Ball";
            case "48":
            case "nova":
                return "Nova";
            case "49":
            case "lightning":
                return "Lightning";
            case "50":
            case "shiver armor":
                return "Shiver Armor";
            case "51":
            case "fire wall":
                return "Fire Wall";
            case "52":
            case "enchant fire":
                return "Enchant Fire";
            case "53":
            case "chain lightning":
                return "Chain Lightning";
            case "54":
            case "teleport":
                return "Teleport";
            case "55":
            case "glacial spike":
                return "Glacial Spike";
            case "56":
            case "meteor":
                return "Meteor";
            case "57":
            case "thunder storm":
                return "Thunder Storm";
            case "58":
            case "energy shield":
                return "Energy Shield";
            case "59":
            case "blizzard":
                return "Blizzard";
            case "60":
            case "chilling armor":
                return "Chilling Armor";
            case "61":
            case "fire mastery":
                return "Fire Mastery";
            case "62":
            case "hydra":
                return "Hydra";
            case "63":
            case "lightning mastery":
                return "Lightning Mastery";
            case "64":
            case "frozen orb":
                return "Frozen Orb";
            case "65":
            case "cold mastery":
                return "Cold Mastery";
            case "369":
            case "ice barrage":
                return "Ice Barrage";
            case "376":
            case "combustion":
                return "Combustion";
            case "383":
            case "lesser hydra":
                return "Lesser Hydra";

            // Necromancer SK66-SK95, SK367, SK374, SK381
            case "66":
            case "ampdmg":
            case "amplify damage":
                return "Amplify Damage";
            case "67":
            case "teeth":
                return "Teeth";
            case "68":
            case "bone armor":
                return "Bone Armor";
            case "69":
            case "skeleton mastery":
                return "Skeleton Mastery";
            case "70":
            case "raise skeleton warrior":
                return "Raise Skeleton Warrior";
            case "71":
            case "dim vision":
                return "Dim Vision";
            case "72":
            case "weaken":
                return "Weaken";
            case "73":
            case "poison strike":
                return "Poison Strike";
            case "74":
            case "corpse explosion":
                return "Corpse Explosion";
            case "75":
            case "clay golem":
                return "Clay Golem";
            case "76":
            case "iron maiden":
                return "Iron Maiden";
            case "77":
            case "terror":
                return "Terror";
            case "78":
            case "bone wall":
                return "Bone Wall";
            case "79":
            case "golem mastery":
                return "Golem Mastery";
            case "80":
            case "raise skeletal mage":
                return "Raise Skeletal Mage";
            case "81":
            case "confuse":
                return "Confuse";
            case "82":
            case "life tap":
                return "Life Tap";
            case "83":
            case "desecrate":
                return "Desecrate";
            case "84":
            case "bone spear":
                return "Bone Spear";
            case "85":
            case "blood golem":
            case "bloodgolem":
                return "Blood Golem";
            case "86":
            case "attract":
                return "Attract";
            case "87":
            case "decrepify":
                return "Decrepify";
            case "88":
            case "bone prison":
                return "Bone Prison";
            case "89":
            case "raise skeleton archer":
                return "Raise Skeleton Archer";
            case "90":
            case "iron golem":
            case "irongolem":
                return "Iron Golem";
            case "91":
            case "lowres":
            case "lower resist":
                return "Lower Resist";
            case "92":
            case "poison nova":
                return "Poison Nova";
            case "93":
            case "bone spirit":
                return "Bone Spirit";
            case "94":
            case "fire golem":
                return "Fire Golem";
            case "95":
            case "revive":
                return "Revive";
            case "367":
            case "blood warp":
                return "Blood Warp";
            case "374":
            case "curse mastery":
                return "Curse Mastery";
            case "381":
            case "dark pact":
                return "Dark Pact";

            // Paladin SK96-SK125, SK364, SK371, SK378
            case "96":
            case "sacrifice":
                return "Sacrifice";
            case "97":
            case "smite":
                return "Smite";
            case "98":
            case "might":
                return "Might";
            case "99":
            case "prayer":
                return "Prayer";
            case "100":
            case "resist fire":
                return "Resist Fire";
            case "101":
            case "holy bolt":
                return "Holy Bolt";
            case "102":
            case "holy fire":
                return "Holy Fire";
            case "103":
            case "thorns":
                return "Thorns";
            case "104":
            case "defiance":
                return "Defiance";
            case "105":
            case "resist cold":
                return "Resist Cold";
            case "106":
            case "zeal":
                return "Zeal";
            case "107":
            case "charge":
                return "Charge";
            case "108":
            case "blessed aim":
                return "Blessed Aim";
            case "109":
            case "cleansing":
                return "Cleansing";
            case "110":
            case "resist lightning":
                return "Resist Lightning";
            case "111":
            case "vengeance":
                return "Vengeance";
            case "112":
            case "blessed hammer":
                return "Blessed Hammer";
            case "113":
            case "concentration":
                return "Concentration";
            case "114":
            case "holy freeze":
                return "Holy Freeze";
            case "115":
            case "vigor":
                return "Vigor";
            case "116":
            case "holy sword":
                return "Holy Sword";
            case "117":
            case "holy shield":
                return "Holy Shield";
            case "118":
            case "holy shock":
                return "Holy Shock";
            case "119":
            case "sanctuary":
                return "Sanctuary";
            case "120":
            case "meditation":
                return "Meditation";
            case "121":
            case "fist of the heavens":
                return "Fist of the Heavens";
            case "122":
            case "fanaticism":
                return "Fanaticism";
            case "123":
            case "conviction":
                return "Conviction";
            case "124":
            case "redemption":
                return "Redemption";
            case "125":
            case "salvation":
                return "Salvation";
            case "364":
            case "holy nova":
                return "Holy Nova";
            case "371":
            case "holy light":
                return "Holy Light";
            case "378":
            case "joust":
                return "Joust";

            // Barbarian SK126-SK155, SK368
            case "126":
            case "bash":
                return "Bash";
            case "127":
            case "sword mastery":
                return "Sword Mastery";
            case "128":
            case "general mastery":
                return "General Mastery";
            case "129":
            case "mace mastery":
                return "Mace Mastery";
            case "130":
            case "howl":
                return "Howl";
            case "131":
            case "find potion":
                return "Find Potion";
            case "132":
            case "leap":
                return "Leap";
            case "133":
            case "double swing":
                return "Double Swing";
            case "134":
            case "polearm and spear mastery":
                return "Polearm and Spear Mastery";
            case "135":
            case "throwing mastery":
                return "Throwing Mastery";
            case "136":
            case "spear mastery":
                return "Spear Mastery";
            case "137":
            case "taunt":
                return "Taunt";
            case "138":
            case "shout":
                return "Shout";
            case "139":
            case "stun":
                return "Stun";
            case "140":
            case "double throw":
                return "Double Throw";
            case "141":
            case "combat reflexes":
                return "Combat Reflexes";
            case "142":
            case "find item":
                return "Find Item";
            case "143":
            case "leap attack":
                return "Leap Attack";
            case "144":
            case "concentrate":
                return "Concentrate";
            case "145":
            case "iron skin":
                return "Iron Skin";
            case "146":
            case "battle cry":
                return "Battle Cry";
            case "147":
            case "frenzy":
                return "Frenzy";
            case "148":
            case "increased speed":
                return "Increased Speed";
            case "149":
            case "battle orders":
                return "Battle Orders";
            case "150":
            case "grim ward":
                return "Grim Ward";
            case "151":
            case "whirlwind":
                return "Whirlwind";
            case "152":
            case "berserk":
                return "Berserk";
            case "153":
            case "natural resistance":
                return "Natural Resistance";
            case "154":
            case "war cry":
                return "War Cry";
            case "155":
            case "battle command":
                return "Battle Command";
            case "368":
            case "deep wounds":
                return "Deep Wounds";

            // Druid SK221-SK250, SK370
            case "221":
            case "raven":
                return "Raven";
            case "222":
            case "poison creeper":
                return "Poison Creeper";
            case "223":
            case "werewolf":
                return "Werewolf";
            case "224":
            case "lycanthropy":
                return "Lycanthropy";
            case "225":
            case "firestorm":
                return "Firestorm";
            case "226":
            case "oak sage":
                return "Oak Sage";
            case "227":
            case "summon spirit wolf":
                return "Summon Spirit Wolf";
            case "228":
            case "werebear":
                return "Werebear";
            case "229":
            case "molten boulder":
                return "Molten Boulder";
            case "230":
            case "arctic blast":
                return "Arctic Blast";
            case "231":
            case "carrion vine":
                return "Carrion Vine";
            case "232":
            case "feral rage":
                return "Feral Rage";
            case "233":
            case "maul":
                return "Maul";
            case "234":
            case "fissure":
                return "Fissure";
            case "235":
            case "cyclone armor":
                return "Cyclone Armor";
            case "236":
            case "heart of wolverine":
                return "Heart of Wolverine";
            case "237":
            case "summon dire wolf":
                return "Summon Dire Wolf";
            case "238":
            case "rabies":
                return "Rabies";
            case "239":
            case "fire claws":
                return "Fire Claws";
            case "240":
            case "twister":
                return "Twister";
            case "241":
            case "solar creeper":
                return "Solar Creeper";
            case "242":
            case "hunger":
                return "Hunger";
            case "243":
            case "shock wave":
                return "Shock Wave";
            case "244":
            case "volcano":
                return "Volcano";
            case "245":
            case "tornado":
                return "Tornado";
            case "246":
            case "spirit of barbs":
                return "Spirit of Barbs";
            case "247":
            case "summon grizzly":
                return "Summon Grizzly";
            case "248":
            case "fury":
                return "Fury";
            case "249":
            case "armageddon":
                return "Armageddon";
            case "250":
            case "hurricane":
                return "Hurricane";
            case "370":
            case "gust":
                return "Gust";

            // Assassin SK251-SK280, SK366
            case "251":
            case "fire blast":
                return "Fire Blast";
            case "252":
            case "claw and dagger mastery":
                return "Claw and Dagger Mastery";
            case "253":
            case "psychic hammer":
                return "Psychic Hammer";
            case "254":
            case "tiger strike":
                return "Tiger Strike";
            case "255":
            case "dragon talon":
                return "Dragon Talon";
            case "256":
            case "shock web":
                return "Shock Web";
            case "257":
            case "blade sentinel":
                return "Blade Sentinel";
            case "258":
            case "burst of speed":
                return "Burst of Speed";
            case "259":
            case "fists of fire":
                return "Fists of Fire";
            case "260":
            case "dragon claw":
                return "Dragon Claw";
            case "261":
            case "charged bolt sentry":
                return "Charged Bolt Sentry";
            case "262":
            case "wake of fire":
                return "Wake of Fire";
            case "263":
            case "weapon block":
                return "Weapon Block";
            case "264":
            case "cloak of shadows":
                return "Cloak of Shadows";
            case "265":
            case "cobra strike":
                return "Cobra Strike";
            case "266":
            case "blade fury":
                return "Blade Fury";
            case "267":
            case "fade":
                return "Fade";
            case "268":
            case "shadow warrior":
                return "Shadow Warrior";
            case "269":
            case "claws of thunder":
                return "Claws of Thunder";
            case "270":
            case "dragon tail":
                return "Dragon Tail";
            case "271":
            case "chain lightning sentry":
                return "Chain Lightning Sentry";
            case "272":
            case "wake of inferno":
                return "Wake of Inferno";
            case "273":
            case "mind blast":
                return "Mind Blast";
            case "274":
            case "blades of ice":
                return "Blades of Ice";
            case "275":
            case "dragon flight":
                return "Dragon Flight";
            case "276":
            case "death sentry":
                return "Death Sentry";
            case "277":
            case "blade shield":
                return "Blade Shield";
            case "278":
            case "venom":
                return "Venom";
            case "279":
            case "shadow master":
                return "Shadow Master";
            case "280":
            case "phoenix strike":
                return "Phoenix Strike";
            case "366":
            case "lightning sentry":
                return "Lightning Sentry";

            // Other Skills
            case "357":
            case "blink":
                return "Blink";
            case "380":
            case "blade dance":
                return "Blade Dance";
            case "391":
            case "lesser fade":
                return "Lesser Fade";
            case "400":
            case "bone nova":
                return "Bone Nova";
            case "442":
            case "amplify damage (proc)":
                return "Amplify Damage (Proc)";
            case "443":
            case "weaken (proc)":
                return "Weaken (Proc)";
            case "444":
            case "iron maiden (proc)":
                return "Iron Maiden (Proc)";
            case "445":
            case "life tap (proc)":
                return "Life Tap (Proc)";
            case "446":
            case "decrepify (proc)":
                return "Decrepify (Proc)";
            case "447":
            case "lower resist (proc)":
                return "Lower Resist (Proc)";
            case "554":
            case "energy shield (item supplied)":
                return "Energy Shield (Item Supplied)";
            case "555":
            case "chilling armor (item supplied)":
                return "Chilling Armor (Item Supplied)";
            case "556":
            case "burst of speed (item supplied)":
                return "Burst of Speed (Item Supplied)";
            case "557":
            case "blade shield (item supplied)":
                return "Blade Shield (Item Supplied)";
            default:
                return null;
        }
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