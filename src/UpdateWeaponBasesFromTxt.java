import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UpdateWeaponBasesFromTxt {
    private static final String rootdir = System.getProperty("user.dir");
    private static final String dir = rootdir + "\\data\\";
    private static final Path ARMOR_TXT = Paths.get(dir, "Weapons.txt");
    private static final Path ITEM_METADATA_JS = Paths.get(dir, "item_metadata.js");
    private static final Map<String, List<String>> MAP = new LinkedHashMap<String, List<String>>() {{
        put("minac", Arrays.asList("def_low"));
        put("maxac", Arrays.asList("def_high", "base_defense"));
        put("block", Arrays.asList("block"));
        put("mindam", Arrays.asList("base_damage_min"));
        put("maxdam", Arrays.asList("base_damage_max"));
        put("reqstr", Arrays.asList("req_strength"));
    }};

    public static void main(String[] args) throws IOException {
        // 1) Read Armor.txt
        Map<String, Map<String, Integer>> armor = readArmorRows();
        // 2) Read JS
        List<String> js = Files.readAllLines(ITEM_METADATA_JS, StandardCharsets.UTF_8);
        // 3) Update
        UpdateReport report = new UpdateReport();
        List<String> updated = updateBases(js, armor, report);
        // 4) Write back
        //  if (true)
        //       throw new RuntimeException("Aborted");
        Files.write(ITEM_METADATA_JS, updated, StandardCharsets.UTF_8);
        // 5) Print summary
        report.print();
        System.out.println("Updated: " + ITEM_METADATA_JS.toAbsolutePath());
    }

    // -------- TSV reader (simple, robust like elsewhere in project) --------
    private static Map<String, Map<String, Integer>> readArmorRows() throws IOException {
        Map<String, Map<String, Integer>> byName = new LinkedHashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(ARMOR_TXT.toFile(), StandardCharsets.UTF_8))) {
            String line;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                line = stripBom(line);
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t", -1);

                if (headers == null) {
                    headers = parts;
                    for (int i = 0; i < headers.length; i++) headers[i] = headers[i] == null ? "" : headers[i].trim();
                    continue;
                }

                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String key = headers[i].isEmpty() ? ("col_" + i) : headers[i];
                    row.put(key, i < parts.length ? parts[i] : "");
                }

                String rawName = safe(row.get("name")).trim();
                if (rawName.isEmpty()) continue;

                Map<String, Integer> vals = new HashMap<>();
                putIntIfPositive(vals, "minac", row.get("minac"));
                putIntIfPositive(vals, "maxac", row.get("maxac"));
                putIntIfPositive(vals, "block", row.get("block"));
                // use first occurrence of mindam/maxdam columns
                putIntIfPositive(vals, "mindam", row.get("mindam"));
                putIntIfPositive(vals, "maxdam", row.get("maxdam"));
                putIntIfPositive(vals, "reqstr", row.get("reqstr"));

                // Store under multiple keys to improve matching: raw, normalized spaces->underscores, underscores->spaces
                byName.put(rawName, vals);
                byName.put(normalizeToUnderscores(rawName), vals);
                byName.put(normalizeToSpaces(rawName), vals);
            }
        }
        return byName;
    }

    private static void putIntIfPositive(Map<String, Integer> map, String key, String raw) {
        Integer v = parseInt(raw);
        if (v != null && v > 0) map.put(key, v);
    }

    private static Integer parseInt(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        // accept integer only
        if (!s.matches("-?\\d+")) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // -------- JS updater limited to "var bases = { ... }" --------

    private static List<String> updateBases(List<String> lines,
                                            Map<String, Map<String, Integer>> armorValues,
                                            UpdateReport report) {

        List<String> out = new ArrayList<>(lines.size());
        boolean inBases = false;
        int braceDepth = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (!inBases && trimmed.startsWith("var bases = {")) {
                inBases = true;
                braceDepth = count(trimmed, '{') - count(trimmed, '}');
                out.add(line);
                continue;
            }

            if (inBases) {
                braceDepth += count(trimmed, '{') - count(trimmed, '}');

                // End of bases block
                if (braceDepth <= 0) {
                    inBases = false;
                    out.add(line);
                    continue;
                }

                // Detect entry start: key: {   (key can be quoted or not, may include spaces)
                BaseKey key = parseBaseKeyStart(trimmed);
                if (key != null) {
                    // collect object lines until its closing brace at same nesting
                    List<String> block = new ArrayList<>();
                    block.add(line);
/*
                    int innerDepth = count(trimmed, '{') - count(trimmed, '}');
                    int j = i + 1;
                    for (; j < lines.size(); j++) {
                        String l2 = lines.get(j);
                        block.add(l2);
                        String t2 = l2.trim();
                        innerDepth += count(t2, '{') - count(t2, '}');
                        if (innerDepth <= 0) break;
                    }
*/
                    // Update and append
                    block = splitEntry(line);
                    List<String> newBlock = applyToBlock(block, key.name, armorValues, report);
                    String newline = newBlock.toString();
                    // System.out.println("Updated " + concatWithCommaExceptAfterFirst(newBlock));
                    out.add(concatWithCommaExceptAfterFirst(newBlock));
                    //out.addAll(newBlock);
                    //   i = j; // advance
                    continue;
                }
            }

            out.add(line);
        }

        return out;
    }

    public static String concatWithCommaExceptAfterFirst(List<String> parts) {
        if (parts == null || parts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        boolean isSecond = true;
        for (String part : parts) {
            if (part == null) continue; // skip nulls; remove if you want "null"
            if (isFirst) {
                sb.append(part);
                isFirst = false;
            } else if (isSecond) {
                sb.append(part);
                isSecond = false;
            } else {
                sb.append(',').append(part);
            }
        }
        return sb.toString();
    }

    // ... existing code ...
    public static List<String> splitEntry(String input) {
        List<String> parts = new ArrayList<>();
        if (input == null || input.isEmpty()) return parts;

        int open = input.indexOf(":{");
        if (open < 0) {
            parts.add(input);
            return parts;
        }

        // Include :{ with the name to avoid stripping it
        String nameWithColonBrace = input.substring(0, open + 2); // up to and including :{
        parts.add(nameWithColonBrace);

        // Body starts right after :{
        int start = open + 2;

        // Find matching } on the same line, preserve everything verbatim
        int end = findMatchingBraceOnLine(input, start - 1);
        if (end < 0) end = input.length();

        String body = input.substring(start, Math.min(end, input.length()));

        for (String field : splitTopLevel(body)) {
            if (!field.isEmpty()) parts.add(field);
        }

        // Keep tail starting at the matching }, including the brace itself
        if (end >= 0 && end < input.length()) {
            String tail = input.substring(end);
            if (!tail.isEmpty()) parts.add(tail);
        }

        return parts;
    }

    // Find the matching '}' for a '{' at pos openIdx, scanning only this line
    private static int findMatchingBraceOnLine(String s, int openIdx) {
        int depth = 0;
        boolean inS = false, inD = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            // stop at newline if present
            if (c == '\n' || c == '\r') break;

            if (!inS && !inD) {
                if (c == '\'') inS = true;
                else if (c == '"') inD = true;
                else if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return i;
                }
            } else {
                if (inS && c == '\'' && (i == 0 || s.charAt(i - 1) != '\\')) inS = false;
                if (inD && c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inD = false;
            }
        }
        return -1;
    }

    // Split by commas that are not inside quotes or nested braces
    private static List<String> splitTopLevel(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        boolean inS = false, inD = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (!inS && !inD) {
                if (c == '\'') {
                    inS = true;
                    cur.append(c);
                } else if (c == '"') {
                    inD = true;
                    cur.append(c);
                } else if (c == '{') {
                    depth++;
                    cur.append(c);
                } else if (c == '}') {
                    if (depth > 0) depth--;
                    cur.append(c);
                } else if (c == ',' && depth == 0) {
                    out.add(cur.toString()); // keep exact text, no trim
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            } else {
                // inside quotes
                cur.append(c);
                if (inS && c == '\'' && (i == 0 || s.charAt(i - 1) != '\\')) inS = false;
                if (inD && c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) inD = false;
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
    // ... existing code ...

    private static int count(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }

    private static BaseKey parseBaseKeyStart(String trimmed) {
        // Allow: Key: {   OR   "Key Name": {   OR   'Key Name': {
        int colon = trimmed.indexOf(':');
        int brace = trimmed.indexOf('{');
        if (colon <= 0 || brace < 0 || brace < colon) return null;

        String left = trimmed.substring(0, colon).trim();
        if (left.isEmpty()) return null;

        String name = unquote(left);
        if (name.isEmpty()) return null;

        return new BaseKey(name);
    }

    private static String unquote(String s) {
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static List<String> applyToBlock(List<String> block,
                                             String baseName,
                                             Map<String, Map<String, Integer>> armorValues,
                                             UpdateReport report) {
        // Try several matching variants (raw, underscores, spaces)
        Map<String, Integer> row = lookupArmorRow(armorValues, baseName);
        if (row == null || row.isEmpty()) {
            report.skipNoRow.add(baseName);
            return block;
        }

        // Find existing target keys in block
        Map<String, Integer> keyLineIdx = new HashMap<>();
        for (int i = 0; i < block.size(); i++) {
            String t = block.get(i).trim();
            if (i == 0) {
                //   System.out.println("First line in block: " + t);
                continue;
            }
            int colon = t.indexOf(':');
            if (colon <= 0) {
                //   System.out.println("Unexpected line in block: " + t);
                continue;
            }
            String key = unquote(t.substring(0, colon).trim());
            if (MAP.values().stream().anyMatch(list -> list.contains(key))) {
                //  System.out.println("Found existing key " + key + " in block");
                keyLineIdx.put(key, i);
            } else {

                keyLineIdx.put(key, i);
            }
        }

        // System.err.println("Found " + keyLineIdx.size() + " keys in block");
        Map<String, Integer> updates = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> src : row.entrySet()) {
            //   System.out.println("Try " + baseName + " " + src.getKey() + " to " + src.getValue());
            List<String> targets = MAP.get(src.getKey());
            if (targets == null) continue;
            Integer v = src.getValue();
            if (v == null || v <= 0) continue;
            for (String tgt : targets) {
                if (keyLineIdx.containsKey(tgt)) {
                    //       System.out.println("  to " + tgt);
                    updates.put(tgt, v);
                } else {
                    //     System.out.println("  no matching key found in block for " + tgt);
                }
            }
        }

        if (updates.isEmpty()) {
            //   System.out.println("No matching keys for " + baseName);
            report.skipNoKeys.add(baseName);
            return block;
        }

        List<String> out = new ArrayList<>(block.size());
        boolean changed = false;

        for (int i = 0; i < block.size(); i++) {
            String line = block.get(i);
            String trimmed = line.trim();
            int colon = trimmed.indexOf(':');

            if (colon > 0) {
                String key = unquote(trimmed.substring(0, colon).trim());
                Integer newVal = updates.get(key);
                if (newVal != null) {
                    String indent = line.substring(0, line.indexOf(trimmed));
                    String beforeComment = line;
                    int cIdx = line.indexOf("//");
                    if (cIdx >= 0) beforeComment = line.substring(0, cIdx);
                    String after = beforeComment.trim().endsWith(",") ? "," : "";
                    String comment = cIdx >= 0 ? line.substring(cIdx) : "";
                    String newLine = indent + key + ": " + newVal + after + (comment.isEmpty() ? "" : " " + comment.trim());
                    out.add(newLine);
                    changed = true;
                    continue;
                }
            }
            out.add(line);
        }

        if (changed) {
            report.updated.add(baseName);
        } else {
            report.skipNoKeys.add(baseName);
        }

        return out;
    }

    // -------- helpers --------
    private static String normalizeToUnderscores(String s) {
        if (s == null) return null;
        return s.trim().replace(' ', '_');
    }

    private static String normalizeToSpaces(String s) {
        if (s == null) return null;
        return s.trim().replace('_', ' ');
    }

    private static Map<String, Integer> lookupArmorRow(Map<String, Map<String, Integer>> armorValues, String baseName) {
        if (armorValues.containsKey(baseName)) return armorValues.get(baseName);
        String u = normalizeToUnderscores(baseName);
        if (armorValues.containsKey(u)) return armorValues.get(u);
        String sp = normalizeToSpaces(baseName);
        if (armorValues.containsKey(sp)) return armorValues.get(sp);
        // last resort: case-insensitive match on these three variants
        String[] tries = new String[]{baseName, u, sp};
        for (String key : armorValues.keySet()) {
            for (String q : tries) {
                if (q != null && key.equalsIgnoreCase(q)) return armorValues.get(key);
            }
        }
        return null;
    }

    private static class BaseKey {
        final String name;

        BaseKey(String name) {
            this.name = name;
        }
    }

    private static class UpdateReport {
        final Set<String> updated = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        final Set<String> skipNoRow = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        final Set<String> skipNoKeys = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        void print() {
            System.out.println("Bases updated: " + updated.size());
            if (!updated.isEmpty()) System.out.println("  " + updated);
            if (!skipNoRow.isEmpty()) System.out.println("No Armor.txt row for: " + skipNoRow);
            if (!skipNoKeys.isEmpty()) System.out.println("No matching existing keys in JS for: " + skipNoKeys);
        }
    }
}