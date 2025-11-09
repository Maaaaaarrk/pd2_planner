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
    private static final String rootdir = System.getProperty("user.dir");
    private static final String dir = rootdir + "\\data\\";
    private static final String UNIQUE_ITEMS_PATH = dir + "UniqueItems.txt";
    public static final String RUNE_WORDS = dir + "Runes.txt";
    public static final String GEM_RUNE = dir + "Gems.txt";
    private static final String SET_ITEMS_PATH = dir + "SetItems.txt";
    private static final String MISC_PATH = dir + "magicrarerw.tsv";
    private static final String OUTPUT_DIR = dir;
    private static final String INPUT_DIR = rootdir + "\\src\\";
    private static final String IMAGE_DIR = rootdir + "\\images\\";
    private static final String ITEM_IMAGE_DIR = IMAGE_DIR + "\\items\\";

    // File naming: new file each run like equipment-YYYYMMDD-HHmmss.js
    private static final String OUTPUT_BASENAME = "equipment";

    private static final boolean buildProd = true;
    private static final String PROD_OUTPUT_BASENAME = "items_equipment.js";

    public static void main(String[] args) {

        try {


            Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();


            if (extracted(grouped, ItemType.UNIQUE))
                System.err.println("Error ItemType.UNIQUE");
            if (extracted(grouped, ItemType.SET))
                System.err.println("Error ItemType.SET");
            if (extracted(grouped, ItemType.MISC))
                System.err.println("Error ItemType.MISC");

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
            System.out.println("Groups: " + grouped.size());

            RunesTxtToJson.writeRunewordsJsFile();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private enum ItemType {
        UNIQUE, SET, MISC
    }

    private static boolean extracted(Map<String, List<Map<String, Object>>> grouped, ItemType itemType) throws IOException {

        final String path;
        if (itemType == ItemType.UNIQUE)
            path = UNIQUE_ITEMS_PATH;
        else if (itemType == ItemType.SET)
            path = SET_ITEMS_PATH;
        else if (itemType == ItemType.MISC)
            path = MISC_PATH;
        else {
            System.out.println("Invalid item type " + itemType);
            return true;
        }

        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            System.out.println("Input is empty: " + path);
            return true;
        }

        String[] header = splitTSV(lines.get(0));
        Map<String, Integer> h = headerIndex(header);

        // Required columns
        int idxName = h.getOrDefault("index", -1);
        final int idxType;
        switch (itemType) {
            case UNIQUE:
            case MISC:
                idxType = h.getOrDefault("*type", -1);
                break;
            case SET:
                idxType = h.getOrDefault("*item", -1);
                break;
            default:
                return true;
        }


        int idxSetGroup = h.getOrDefault("set", -1);// SET ONLY
        int idxItemRarity = h.getOrDefault("rarity", -1);// misc ONLY

        int idxEnabled = h.getOrDefault("enabled", 2);
        int idxReqLevel = h.getOrDefault("lvl req", 8); // fallback to 3rd column if header missing

        if (idxName < 0 || idxType < 0) {
            System.err.println("Missing required headers: index " + idxName + " and/or *type " + idxType);
            return true;
        }

        // Precompute prop/max indices for 1..11
        int[] idxProp = new int[13];
        int[] idxPar = new int[13];
        int[] idxMin = new int[13];
        int[] idxMax = new int[13];
        Arrays.fill(idxProp, -1);
        Arrays.fill(idxMax, -1);
        for (int i = 1; i <= 12; i++) {
            idxProp[i] = h.getOrDefault("prop" + i, -1);
            idxPar[i] = h.getOrDefault("par" + i, -1);
            idxMin[i] = h.getOrDefault("min" + i, -1);
            idxMax[i] = h.getOrDefault("max" + i, -1);
        }

        int[] idxaPropa = new int[6];
        int[] idxaPara = new int[6];
        int[] idxaMina = new int[6];
        int[] idxaMaxa = new int[6];
        int[] idxaPropb = new int[6];
        int[] idxaParb = new int[6];
        int[] idxaMinb = new int[6];
        int[] idxaMaxb = new int[6];
        for (int i = 1; i <= 5; i++) {
            idxaPropa[i] = h.getOrDefault("aprop" + i + "a", -1);
            idxaPara[i] = h.getOrDefault("apar" + i + "a", -1);
            idxaMina[i] = h.getOrDefault("amin" + i + "a", -1);
            idxaMaxa[i] = h.getOrDefault("amax" + i + "a", -1);
            idxaPropb[i] = h.getOrDefault("aprop" + i + "b", -1);
            idxaParb[i] = h.getOrDefault("apar" + i + "b", -1);
            idxaMinb[i] = h.getOrDefault("amin" + i + "b", -1);
            idxaMaxb[i] = h.getOrDefault("amax" + i + "b", -1);
        }


        int linecount = 0;
        int savedRows = 0;
        for (int i = 1; i < lines.size(); i++) {
            linecount++;
            String line = lines.get(i);
            if (line == null || line.trim().isEmpty()) continue;

            String[] cols = splitTSV(line);


            // Must have "enabled" == "1"
            // Unique only
            if (itemType == ItemType.UNIQUE) {
                String enabled = safeGet(cols, idxEnabled).trim();
                if (!"1".equals(enabled)) {
                    continue;
                }
            }

            String baseType = itemmap.checkForRename(safeGet(cols, idxType).trim());
            String name = itemmap.checkForRename(safeGet(cols, idxName).trim());
            String setGroup = "";
            if (idxSetGroup > -1) {
                setGroup = safeGet(cols, idxSetGroup).trim();
            }

            if (itemmap.skipCheck(name)) {
                continue;
            }
            String reqLevel = safeGet(cols, idxReqLevel).trim();

            if (baseType.isEmpty() || name.isEmpty()) {
                continue;
            }

            Map<String, Object> row = new LinkedHashMap<>();
            if (!baseType.equalsIgnoreCase("ring") && !baseType.equalsIgnoreCase("amulet")) {
                String baseClean = removeNumbersAndCapitalizeFirst(baseType);
                row.put("base", baseClean);
                if (WeaponGroupTypeUtil.ITEM_BASE_TO_SUBTYPE.containsKey(baseClean)) {
                    row.put("subtype", WeaponGroupTypeUtil.ITEM_BASE_TO_SUBTYPE.get(baseClean));
                }
            }
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
                case "Amulet of the Viper":
                    continue;
            }

            if (itemType == ItemType.MISC) {
                String rarity = safeGet(cols, idxItemRarity).trim();
                row.put("rarity", rarity.toLowerCase());
                if (rarity.equals("rw")) {
                    row.put("name", name.replace(" - ", " \u00AD \u00AD - \u00AD \u00AD "));
                } else {
                    row.put("name", name);
                }
            } else {
                row.put("name", name);
            }

            if (itemType == ItemType.SET) {
                row.put("rarity", "set");

                final String setpGroupCode;
                switch (setGroup) {
                    case "Immortal King":
                        setpGroupCode = "set_IK";
                        break;
                    case "M'avina's Battle Hymn":
                        setpGroupCode = "set_Mav";
                        break;
                    case "Griswold's Legacy":
                        setpGroupCode = "set_Gris";
                        break;
                    case "Trang-Oul's Avatar":
                        setpGroupCode = "set_TO";
                        break;
                    case "Tal Rasha's Wrappings":
                        setpGroupCode = "set_TR";
                        break;
                    case "Natalya's Odium":
                        setpGroupCode = "set_Nat";
                        break;
                    case "Aldur's Watchtower":
                        setpGroupCode = "set_Ald";
                        break;
                    case "Bul-Kathos' Children":
                        setpGroupCode = "set_BK";
                        break;
                    case "The Disciple":
                        setpGroupCode = "set_Disciple";
                        break;
                    case "Angelical Raiment":
                        setpGroupCode = "set_Angelic";
                        break;
                    case "Cathan's Traps":
                        setpGroupCode = "set_Cathan";
                        break;
                    case "Cow King's Leathers":
                        setpGroupCode = "set_Cow";
                        break;
                    case "Heaven's Brethren":
                        setpGroupCode = "set_Brethren";
                        break;
                    case "Hwanin's Majesty":
                        setpGroupCode = "set_Hwanin";
                        break;
                    case "Naj's Ancient Set":
                        setpGroupCode = "set_Naj";
                        break;
                    case "Orphan's Call":
                        setpGroupCode = "set_Orphan";
                        break;
                    case "McAuley's Folly":
                        setpGroupCode = "set_Sander";
                        break;
                    case "Sazabi's Grand Tribute":
                        setpGroupCode = "set_Sazabi";
                        break;
                    case "Arcanna's Tricks":
                        setpGroupCode = "set_Arcanna";
                        break;
                    case "Arctic Gear":
                        setpGroupCode = "set_Arctic";
                        break;
                    case "Berserker's Garb":
                        setpGroupCode = "set_Berserker";
                        break;
                    case "Civerb's Vestments":
                        setpGroupCode = "set_Civerb";
                        break;
                    case "Cleglaw's Brace":
                        setpGroupCode = "set_Cleglaw";
                        break;
                    case "Death's Disguise":
                        setpGroupCode = "set_Death";
                        break;
                    case "Hsarus' Defense":
                        setpGroupCode = "set_Hsarus";
                        break;
                    case "Infernal Tools":
                        setpGroupCode = "set_Infernal";
                        break;
                    case "Iratha's Finery":
                        setpGroupCode = "set_Iratha";
                        break;
                    case "Isenhart's Armory":
                        setpGroupCode = "set_Isenhart";
                        break;
                    case "Milabrega's Regalia":
                        setpGroupCode = "set_Milabrega";
                        break;
                    case "Sigon's Complete Steel":
                        setpGroupCode = "set_Sigon";
                        break;
                    case "Tancred's Battlegear":
                        setpGroupCode = "set_Tancred";
                        break;
                    case "Vidala's Rig":
                        setpGroupCode = "set_Vidala";
                        break;
                    default:
                        System.err.println("setGroup not matched " + setGroup);
                        continue;
                }

                //    System.err.println(line);
                row.put(setpGroupCode, 1);
                List<Object> setProperties = new ArrayList<>();
                setProperties.add(setpGroupCode);
/*
   int[] idxaPropa = new int[6];
        int[] idxaPara = new int[6];
        int[] idxaMina = new int[6];
        int[] idxaMaxa = new int[6];
        int[] idxaPropb = new int[6];
        int[] idxaParb = new int[6];
        int[] idxaMinb = new int[6];
        int[] idxaMaxb = new int[6];
 */

                for (int j = 1; j < idxaPropa.length; j++) {
                    int[] idxProps = new int[2];
                    idxProps[0] = idxaPropa[j];
                    idxProps[1] = idxaPropb[j];
                    int[] idxPars = new int[2];
                    idxPars[0] = idxaPara[j];
                    idxPars[1] = idxaParb[j];
                    int[] idxMins = new int[2];
                    idxMins[0] = idxaMina[j];
                    idxMins[1] = idxaMinb[j];
                    int[] idxMaxs = new int[2];
                    idxMaxs[0] = idxaMaxa[j];
                    idxMaxs[1] = idxaMaxb[j];
                    Map<String, Object> setBonusLevel = new LinkedHashMap<>();
                    extractedItemProps(idxProps, idxPars, idxMins, idxMaxs, cols, name, setBonusLevel, null);
                    setProperties.add(setBonusLevel);
                }

                row.put("set_bonuses", setProperties);
                // set_bonuses:
            }


            String groupBaseType = itemmap.getGroupBaseType(baseType);
            if (groupBaseType != null && !groupBaseType.isEmpty()) {
                if (itemmap.typeChecker(groupBaseType)) {
                    String itemGroupType = WeaponGroupTypeUtil.groupOf(baseType);
                    String itemTypeForRw = groupBaseType;
                    if (itemGroupType != null) {
                        row.put("type", itemGroupType);
                        itemTypeForRw = itemGroupType;
                    } else if (groupBaseType.equals("Offhand")) {
                        if (baseType.equals("Arrows") || baseType.equals("Bolts")) {
                            row.put("type", "quiver");
                        } else {
                            row.put("type", "shield");
                        }
                        itemTypeForRw = "shield";
                    } else {
                        //    System.err.println("itemGroupType = null for " + baseType);
                    }

                    // Add prop1..prop11 with their max values, using the prop value as the key
                    extractedItemProps(idxProp, idxPar, idxMin, idxMax, cols, name, row, itemTypeForRw);
                    if (!groupBaseType.equals("Amulet") && !groupBaseType.equals("Ring1")) {
                        if (itemType == ItemType.SET || itemType == ItemType.UNIQUE) {
                            // Set / Unique
                            row.put("img", name.replace(" ", "_"));
                        }
                    }
                    String groupBaseTypeKeyname = groupBaseType.toLowerCase();
                   /* if (!grouped.containsKey(groupBaseTypeKeyname)) {
                        Map<String, Object> rowheader = new LinkedHashMap<>();
                        rowheader.put("name", groupBaseType.replace("1", ""));
                        grouped.computeIfAbsent(groupBaseTypeKeyname, k -> new ArrayList<>()).add(rowheader);
                    }*/
                    grouped.computeIfAbsent(groupBaseTypeKeyname, k -> new ArrayList<>()).add(row);
                    savedRows++;
                } else {
                    System.err.println("No type match on: " + baseType);
                }
            } else {
                if (!(baseType.equals("charm") || baseType.equals("jewel") || baseType.startsWith("t5"))) {
                    System.err.println("No groupBaseType match on: " + baseType);
                    System.out.println(line);
                }
            }
        }
        System.out.println("Read: " + linecount + " Lines, added " + savedRows + " Items");
        return false;
    }

    private static void extractedItemProps(int[] idxProp, int[] idxPar, int[] idxMin, int[] idxMax, String[] cols, String name, Map<String, Object> row, String itemGroupType) {
        for (int p = 0; p < idxProp.length; p++) {
            int pIdx = idxProp[p];
            int parIdx = idxPar[p];
            int minIdx = idxMin[p];
            int maxIdx = idxMax[p];
            if (pIdx < 0 || maxIdx < 0) {
                continue;
            }

            String propKey = safeGet(cols, pIdx).trim();
            String parameter = safeGet(cols, parIdx).trim();
            String minValStr = safeGet(cols, minIdx).trim();
            String maxValStr = safeGet(cols, maxIdx).trim();
            Map<String, Object> resolvedStats = resolveStat(name, propKey, maxValStr, parameter, minValStr, itemGroupType);
            row = combineRows(row, resolvedStats);
        }
    }

    // Merges two maps:
    // - Upsert all keys from 'addition' into 'base'
    // - If a key exists in both and both values are Integers, add them
    // - Otherwise, overwrite with the value from 'addition'
    public static Map<String, Object> combineRows(Map<String, Object> base, Map<String, Object> addition) {
        if (addition == null || addition.isEmpty()) return base;
        if (base == null) base = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : addition.entrySet()) {
            String key = e.getKey();
            Object newVal = e.getValue();
            if (base.containsKey(key)) {
                Object oldVal = base.get(key);
                if (oldVal instanceof Integer && newVal instanceof Integer) {
                    base.put(key, (Integer) oldVal + (Integer) newVal);
                } else {
                    base.put(key, newVal);
                }
            } else {
                base.put(key, newVal);
            }
        }
        return base;
    }

    public static Map<String, Object> resolveStat(String name, String propKey, String maxValStr, String parameter, String minValStr, String itemGroupType) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (propKey.isEmpty() || (maxValStr.isEmpty() && parameter.isEmpty())) {
            return row;
        }

        Object val = (maxValStr.isEmpty() ? parseNumericOrString(parameter) : parseNumericOrString(maxValStr));
        Object minval = parseNumericOrString(minValStr);


        String plannerPropKey = itemmap.PROP_MAP.get(propKey);
        if (plannerPropKey == null) {
            plannerPropKey = itemmap.PROP_MAP.get(propKey.toLowerCase());
        }
        if (itemmap.PROP_MAP.containsValue(propKey))
            plannerPropKey = propKey;
        else if (propKey.startsWith("skills_")) {
            plannerPropKey = propKey;
        } else if (propKey.equals("only")) {
            if (val instanceof String) {
                if ((val).equals("")) {
                    return row;
                }
                val = ((String) val).toLowerCase();
            }
            plannerPropKey = propKey;
        }

        // Runeword

        if (propKey.toLowerCase().equals("runeword")) {


            Map<String, Object> rwData = RunesTxtToJson.getRuneword(parameter, itemGroupType);
            if (rwData == null) {
                System.err.println("Missing rw: " + parameter);
                return row;
            }
            Object rwStats = rwData.get("rwstats");
            if (rwStats instanceof Map) {
                row.putAll((Map<String, Object>) rwStats);
            } else {
                System.err.println("Missing rwstats: " + parameter);
            }
            return row;
        }

        //skilltab
        if (propKey.equals("skilltab")) {
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
                    return row;
            }
        }

        //skill

        if (propKey.equals("skill") || propKey.equals("oskill")) {
            String skillname = getSkillName(parameter);
            if (skillname == null) {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            plannerPropKey = propKey + "_" + (skillname.replace(" ", "_"));
        }

        //aura
        if (propKey.equals("aura")) {
            String skillname = getSkillName(parameter);
            if (skillname == null) {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            plannerPropKey = "aura";
            row.put(plannerPropKey, skillname);
            plannerPropKey = "aura_lvl";
            row.put(plannerPropKey, val);
            return row;
        }

        // Equipped Skill ""

        if (propKey.equals("equipped-skill")) {
            plannerPropKey = "equipped_skill";
            row.put(plannerPropKey, parameter.replace("SelfAura", "").trim());
            plannerPropKey = "equipped_skill_level";
            row.put(plannerPropKey, val);
            return row;
        }


        if (propKey.equals("skill-rand")) {
            //250 for druid
            if (val.equals(250)) {
                plannerPropKey = "random_skill";
                row.put(plannerPropKey, "[Random Druid Skill] (Druid Only)");
                plannerPropKey = "random_skill_level";
                row.put(plannerPropKey, parseNumericOrString(parameter));
                return row;
            }
            System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                    parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
            return row;
        }

            /*
            cast-skill	Twister	15	28
            cast_skill:{index:["cast_chance","cast_level","cast_skill"], format:["","% Chance to Cast Level "," "," on Casting"]},
            strike_skill:{index:["strike_chance","strike_level","strike_skill"], format:["","% Chance to Cast Level "," "," on Striking"]},
            hit_skill:{index:["hit_chance","hit_level","hit_skill"], format:["","% Chance to Cast Level "," "," on Hit"]},
             */
        if (propKey.equals("hit-skill") || propKey.equals("block-skill") || propKey.equals("levelup-skill") || propKey.equals("kill-skill") || propKey.equals("cast-skill") || propKey.equals("gethit-skill") || propKey.equals("death-skill")) {
            final String type;
            if (propKey.equals("death-skill"))
                type = "ondeath";
            else if (propKey.equals("gethit-skill"))
                type = "gethit";
            else if (propKey.equals("cast-skill"))
                type = "cast";
            else if (propKey.equals("kill-skill"))
                type = "onkill";
            else if (propKey.equals("levelup-skill"))
                type = "onlevel";
            else if (propKey.equals("block-skill"))
                type = "onblock";
            else if (propKey.equals("hit-skill"))
                type = "strike";
            else {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            String skillname = getSkillName(parameter);
            if (skillname == null) {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            plannerPropKey = type + "_skill";
            row.put(plannerPropKey, skillname);
            plannerPropKey = type + "_chance";
            row.put(plannerPropKey, minval);
            plannerPropKey = type + "_level";
            row.put(plannerPropKey, val);
            return row;
        }

        // skill charged
        if (propKey.equals("charged")) {
            String type = "charges";
            String skillname = getSkillName(parameter);
            if (skillname == null) {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            plannerPropKey = type + "_skill";
            row.put(plannerPropKey, skillname);
            plannerPropKey = type + "_charges";
            row.put(plannerPropKey, minval);
            plannerPropKey = type + "_level";
            row.put(plannerPropKey, val);
            return row;
        }

        // flat dmg
        if (propKey.equals("dmg-norm")) {
            plannerPropKey = "damage_min";
            row.put(plannerPropKey, val);
            plannerPropKey = "damage_max";
            row.put(plannerPropKey, val);
            return row;
        }

        //
        if (propKey.equals("dmg-mag")) {
            plannerPropKey = "mDamage_min";
            row.put(plannerPropKey, val);
            plannerPropKey = "mDamage_max";
            row.put(plannerPropKey, val);
            return row;
        }
        //dmg-elem
        if (propKey.equals("dmg-elem")) {
            plannerPropKey = "fDamage_min";
            row.put(plannerPropKey, minval);
            plannerPropKey = "fDamage_max";
            row.put(plannerPropKey, val);
            plannerPropKey = "cDamage_min";
            row.put(plannerPropKey, minval);
            plannerPropKey = "cDamage_max";
            row.put(plannerPropKey, val);
            plannerPropKey = "lDamage_min";
            row.put(plannerPropKey, minval);
            plannerPropKey = "lDamage_max";
            row.put(plannerPropKey, val);
            return row;
        }

        if (propKey.equals("res-all-max")) {
            plannerPropKey = "fRes_max";
            row.put(plannerPropKey, val);
            plannerPropKey = "lRes_max";
            row.put(plannerPropKey, val);
            plannerPropKey = "cRes_max";
            row.put(plannerPropKey, val);
            plannerPropKey = "pRes_max";
            row.put(plannerPropKey, val);
            return row;
        }

            /*
            Hit Causes Monster to Flee +
            16-> 12
            10->7
            100->100
            64->50
            14-> 10

            howl	10	5 -> 10% level 5
             */

        if (propKey.equals("howl")) {
            if (minval.equals(val)) {
                final double newRate;
                if (val.equals(100)) {
                    newRate = 100;
                } else {
                    newRate = Math.floor(((Integer) val) * 0.75);
                }
                row.put("flee_on_hit", newRate);
                return row;
            }
            String skillname = getSkillName(propKey);
            if (skillname == null) {
                System.err.println("name: " + name + " propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            String type = "hit";
            plannerPropKey = type + "_skill";
            row.put(plannerPropKey, skillname);
            plannerPropKey = type + "_chance";
            row.put(plannerPropKey, minval);
            plannerPropKey = type + "_level";
            row.put(plannerPropKey, val);
            return row;
        }

        if (propKey.equals("dmg-pois")) {
            final double factor;
            final int seconds;
            if (parseNumericOrString(parameter).equals(75)) {
                factor = 3.3;
                seconds = 3;
            } else if (parseNumericOrString(parameter).equals(50)) {
                factor = 5.1;
                seconds = 2;
            } else if (parseNumericOrString(parameter).equals(100)) {
                factor = 2.5;
                seconds = 4;
            } else if (parseNumericOrString(parameter).equals(125)) {
                factor = 2;
                seconds = 5;
            } else if (parseNumericOrString(parameter).equals(25)) {
                factor = 10.24;
                seconds = 1;
            } else {
                System.err.println("name: " + name + " add psn calc for propKey: " + propKey + " parameter: " +
                        parameter + " minValStr: " + minValStr + " maxValStr: " + maxValStr);
                return row;
            }
            final double newPsn = Math.floor(((Integer) val) / factor);
            row.put("dmg_pois", newPsn);
            row.put("dmg_pois_time", seconds);
        }

        if (propKey.equals("silence-fhr-ias")) {
            row.put("fhr", val);
            row.put("ias", val);
            return row;
        }
        if (propKey.equals("plague-fcr-pierce")) {
            row.put("fcr", val);
            row.put("enemy_pRes", ((Integer) val) * -1);
            return row;
        }
        if (propKey.equals("str-and-vit")) {
            row.put("strength", val);
            row.put("vitality", val);
            return row;
        }
        // Skip unknown properties instead of inserting a null key
        if (plannerPropKey == null || plannerPropKey.isBlank()) {

            if (propKey != null & !propKey.isEmpty()) {
                if (!propKey.startsWith("map-"))
                    System.err.println("Unknown prop: " + propKey);
            }
            return row;
        }

        if (plannerPropKey.contains("%") || plannerPropKey.contains("-")
                || plannerPropKey.contains("/")) {
            // System.err.println("Violate rule plannerPropKey: " + plannerPropKey);
            return row;
        }

        // Make negative on purpose
        if (plannerPropKey.equals("enemy_fRes") ||
                plannerPropKey.equals("enemy_cRes") ||
                plannerPropKey.equals("enemy_lRes") ||
                plannerPropKey.equals("enemy_pRes") ||
                plannerPropKey.equals("enemy_phyRes")) {
            if (val instanceof Integer && ((Integer) val) > 0) {
                val = ((Integer) val) * -1;
            }
        }

        // add stat for uld
        if (plannerPropKey.equals("mindmg_energy")) {
            row.put("mindmg_per_energy", 1);
        }

        if (row.containsKey(plannerPropKey)) {
            Object oldVal = row.get(plannerPropKey);
            if (oldVal instanceof Integer && val instanceof Integer) {
                row.put(plannerPropKey, (Integer) oldVal + (Integer) val);
            }
            row.put(plannerPropKey, val);
        } else {
            row.put(plannerPropKey, val);
        }
        return row;
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
                mapToJsObjectAppender(sb, row, removeNumbersAndCapitalizeFirst(groupKey));
                sb.append(",");
                sb.append("\n");
            }

            sb.append("  ]");
            sb.append(",");
            sb.append("\n");
        }
        sb.append(loadFooterFromFile());
        sb.append("};\n");
        return sb.toString();
    }

    private static void mapToJsObjectAppender(StringBuilder sb, Map<String, Object> row, String groupKey) {
        sb.append("    {");
        int ci = 0;
        int csize = row.size();


        if (row.containsKey("img")) {
            String subGroupKey = "";
            if (row.containsKey("type") && groupKey.toLowerCase().equals("weapon")) {
                subGroupKey = String.valueOf(row.get("type"));
            }
            imageCheck(groupKey, subGroupKey, true, String.valueOf(row.get("img")), row.get("name"));
        } else if (row.containsKey("base") && false) {
            // these dont need to be reviewed, there is a complex eltie-> normal base reduction
            String subGroupKey = "";
            if (row.containsKey("type") && groupKey.toLowerCase().equals("weapon")) {
                subGroupKey = String.valueOf(row.get("type"));
            }
            imageCheck(groupKey, subGroupKey, false, String.valueOf(row.get("base")), row.get("name"));
        }

        for (Map.Entry<String, Object> ce : row.entrySet()) {
            // sb.append("\"").append(escapeJsString(ce.getKey())).append("\": ");
            sb.append(escapeJsString(ce.getKey())).append(": ");
            Object val = ce.getValue();
            if (val instanceof Number) {
                sb.append(val.toString());
            } else if (val instanceof ArrayList<?>) {
                ArrayList<Object> al = (ArrayList<Object>) val;
                sb.append("[");
                for (int j = 0; j < al.size(); j++) {
                    if (j > 0) {
                        sb.append(", ");
                    }
                    if (al.get(j) instanceof String) {
                        sb.append("\"").append(al.get(j).toString()).append("\"");
                    } else if (al.get(j) instanceof Map<?, ?>) {
                        Map<String, Object> m = (Map<String, Object>) al.get(j);
                        mapToJsObjectAppender(sb, m, groupKey);
                    } else {
                        sb.append(al.get(j).toString());
                    }
                    if (j == 0) {
                        sb.append(", {}");
                    }
                }
                sb.append("]");
            } else {
                sb.append("\"").append(escapeJsString(String.valueOf(val))).append("\"");
            }
            if (++ci < csize) sb.append(", ");
        }
        sb.append("}");
    }

    private static void imageCheck(String groupKey, String subGroup, boolean isSpecial, String s, Object name) {
        s = s.replace(" ", "_");
        String fileName = s + ".png";
        String isSpecialStr = isSpecial ? "special\\" : "";
        if (subGroup != null && !subGroup.isEmpty()) {
            subGroup = subGroup + "\\";
        } else {
            subGroup = "";
        }
        Path imagePath = Paths.get(ITEM_IMAGE_DIR, groupKey.toLowerCase(), "\\", subGroup.toLowerCase(), isSpecialStr, fileName);

        if (!Files.exists(imagePath)) {
            System.err.println(name + ": " + imagePath.toAbsolutePath());
        }
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
            case "dopplezon":
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
            case "enchant":
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
            case "wearbear":
                return "Werebear";
            case "229":
            case "molten boulder":
                return "Molten Boulder";
            case "230":
            case "arctic blast":
                return "Arctic Blast";
            case "231":
            case "plague poppy":
            case "carrion vine":
                return "Carrion Vine";
            case "232":
            case "feral rage":
                return "Feral Rage";
            case "233":
            case "maul":
                return "Maul";
            case "234":
            case "eruption":
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
            case "fingermagebossnova":
                return "Bone Nova";
            case "442":
            case "ampdmg proc":
            case "amplify damage proc":
            case "amplify damage (proc)":
                return "Amplify Damage";
            case "443":
            case "weaken (proc)":
                return "Weaken";
            case "444":
            case "iron maiden proc":
            case "iron maiden (proc)":
                return "Iron Maiden";
            case "445":
            case "life tap proc":
            case "life tap (proc)":
                return "Life Tap";
            case "446":
            case "decrepify proc":
            case "decrepify (proc)":
                return "Decrepify";
            case "447":
            case "lowres proc":
            case "lower resist (proc)":
                return "Lower Resist";
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

    // New: load footer content from external file under data/
    private static String loadFooterFromFile() {
        Path footerPath = Paths.get(INPUT_DIR, "CharmsData.txt");
        try {
            return Files.readString(footerPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read footer file: " + footerPath.toAbsolutePath(), e);
        }
    }
}