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
    private static final String dir = "";
    private static final String UNIQUE_ITEMS_PATH = dir + "UniqueItems.txt";
    private static final String OUTPUT_DIR = dir;

    // File naming: new file each run like equipment-YYYYMMDD-HHmmss.js
    private static final String OUTPUT_BASENAME = "equipment";

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
                row.put("base", baseType);
                row.put("req_level", parseNumericOrString(reqLevel));

                // Add prop1..prop11 with their max values, using the prop value as the key
                for (int p = 1; p <= 11; p++) {
                    int pIdx = idxProp[p];
                    int mIdx = idxMax[p];
                    if (pIdx < 0 || mIdx < 0) continue;

                    String propKey = safeGet(cols, pIdx).trim();
                    String maxValStr = safeGet(cols, mIdx).trim();
                    if (propKey.isEmpty() || maxValStr.isEmpty()) continue;

                    // Convert numeric where possible (integers preferred)
                    Object val = parseNumericOrString(maxValStr);
                    String plannerPropKey = itemmap.PROP_MAP.get(propKey);
                    row.put(plannerPropKey, val);
                }

                row.put("img", name.replace(" ", "_"));
                String groupBaseType = itemmap.TYPE_MAP.get(baseType);
                if (groupBaseType != null && !groupBaseType.isEmpty()) {
                    if (itemmap.TYPES.contains(groupBaseType)) {
                        String groupBaseTypeKeyname = groupBaseType.toLowerCase();
                        if (!grouped.containsKey(groupBaseTypeKeyname)) {
                            Map<String, Object> rowheader = new LinkedHashMap<>();
                            rowheader.put("name", groupBaseType.replace("1", ""));
                            grouped.computeIfAbsent(groupBaseTypeKeyname, k -> new ArrayList<>()).add(rowheader);
                        }
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
            Path outFile = outDir.resolve(OUTPUT_BASENAME + "-" + timestamp + ".js");
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

            sb.append("  \"").append(escapeJsString(groupKey)).append("\": [\n");

            for (int i = 0; i < rows.size(); i++) {
                Map<String, Object> row = rows.get(i);
                sb.append("    {");
                int ci = 0;
                int csize = row.size();
                for (Map.Entry<String, Object> ce : row.entrySet()) {
                    sb.append("\"").append(escapeJsString(ce.getKey())).append("\": ");
                    Object val = ce.getValue();
                    if (val instanceof Number) {
                        sb.append(val.toString());
                    } else {
                        sb.append("\"").append(escapeJsString(String.valueOf(val))).append("\"");
                    }
                    if (++ci < csize) sb.append(", ");
                }
                sb.append("}");
                if (i + 1 < rows.size()) sb.append(",");
                sb.append("\n");
            }

            sb.append("  ]");
            if (++gi < gsize) sb.append(",");
            sb.append("\n");
        }

        sb.append("};\n");
        return sb.toString();
    }

    private static String escapeJsString(String s) {
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
}