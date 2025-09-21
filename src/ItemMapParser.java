import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The ItemMapParser class provides functionality to read and parse a tab-separated
 * input file containing item data, extract specific properties and types, and then
 * generate a Java source file defining maps with these extracted values.
 * <p>
 * The class processes an input file where the first line is expected to be a header,
 * and subsequent lines contain data entries. Specific columns, such as "*type" and
 * "prop1" to "prop11", are extracted. The parsed values are written into a generated
 * source file with immutable maps.
 * <p>
 * Main features:
 * - Parses a file to extract property and type data.
 * - Generates a Java class file containing immutable maps for the extracted data.
 * - Handles errors like missing columns or empty inputs.
 * <p>
 * Methods:
 * - main: Entry point for the program; allows specifying input and output file paths.
 * - parseUniqueItems: Reads and processes the input file, extracts values, and stores
 * them in sorted sets.
 * - indexHeader: Creates a mapping of column names to their indices from the header row.
 * - firstPresentIndex: Finds the first matching header index from a list of possible names.
 * - writeItemMapJava: Writes a Java file containing the extracted property and type data
 * in immutable maps.
 * - escapeJava: Escapes Java special characters in strings for safe inclusion in source code.
 * <p>
 * Error Handling:
 * - Throws IllegalArgumentException if the input file is empty or required columns are missing.
 * - Prints error messages to standard error for other exceptions and exits with a non-zero status.
 */
public class ItemMapParser {

    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");
        String inputPath = args.length > 0 ? args[0] : dir + "\\data\\" + "UniqueItems.txt";
        String outputPath = args.length > 1 ? args[1] : dir + "\\src\\" + "itemmapNEW.java";


        try {
            Result result = parseUniqueItems(Paths.get(inputPath));
            writeItemMapJava(Paths.get(outputPath), result.propValues, result.typeValues);
            System.out.println("Wrote " + outputPath + " with " +
                    result.propValues.size() + " prop values and " +
                    result.typeValues.size() + " type values.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Result parseUniqueItems(Path input) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Input file is empty.");
            }

            Map<String, Integer> headerIndex = indexHeader(headerLine);
            int typeIdx = firstPresentIndex(headerIndex, "*type", "type");
            if (typeIdx < 0) {
                throw new IllegalArgumentException("Column '*type' not found in header.");
            }

            int[] propIdx = new int[11];
            for (int i = 1; i <= 11; i++) {
                String key = ("prop" + i).toLowerCase();
                propIdx[i - 1] = headerIndex.getOrDefault(key, -1);
            }

            Set<String> propValues = new TreeSet<>();
            Set<String> typeValues = new TreeSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                String[] cells = line.split("\t", -1);

                // *type
                if (typeIdx >= 0 && typeIdx < cells.length) {
                    String v = cells[typeIdx].trim();
                    if (!v.isEmpty()) typeValues.add(v);
                }

                // prop1..prop11
                for (int idx : propIdx) {
                    if (idx >= 0 && idx < cells.length) {
                        String v = cells[idx].trim();
                        if (!v.isEmpty()) propValues.add(v);
                    }
                }
            }

            return new Result(propValues, typeValues);
        }
    }

    private static Map<String, Integer> indexHeader(String headerLine) {
        String[] headers = headerLine.split("\t", -1);
        Map<String, Integer> index = new HashMap<>(headers.length * 2);
        for (int i = 0; i < headers.length; i++) {
            String key = headers[i] == null ? "" : headers[i].trim().toLowerCase();
            if (!key.isEmpty() && !index.containsKey(key)) {
                index.put(key, i);
            }
        }
        return index;
    }

    private static int firstPresentIndex(Map<String, Integer> headerIndex, String... names) {
        return Arrays.stream(names)
                .map(n -> n == null ? null : n.trim().toLowerCase())
                .filter(n -> n != null && headerIndex.containsKey(n))
                .map(headerIndex::get)
                .findFirst()
                .orElse(-1);
    }

    private static void writeItemMapJava(Path output, Set<String> propValues, Set<String> typeValues) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            w.write("// Auto-generated by GenerateItemMap. Do not edit manually.\n");
            w.write("import java.util.Collections;\n");
            w.write("import java.util.LinkedHashMap;\n");
            w.write("import java.util.Map;\n\n");

            w.write("public final class itemmap {\n\n");
            w.write("    public static final Map<String,String> PROP_MAP;\n");
            w.write("    public static final Map<String,String> TYPE_MAP;\n\n");

            // PROP_MAP
            w.write("    static {\n");
            w.write("        LinkedHashMap<String,String> p = new LinkedHashMap<>();\n");
            for (String v : propValues) {
                w.write("        p.put(\"" + escapeJava(v) + "\", \"" + escapeJava(v) + "\");\n");
            }
            w.write("        PROP_MAP = Collections.unmodifiableMap(p);\n\n");

            // TYPE_MAP
            w.write("        LinkedHashMap<String,String> t = new LinkedHashMap<>();\n");
            for (String v : typeValues) {
                w.write("        t.put(\"" + escapeJava(v) + "\", \"" + escapeJava(v) + "\");\n");
            }
            w.write("        TYPE_MAP = Collections.unmodifiableMap(t);\n");
            w.write("    }\n\n");

            w.write("    private itemmap() {}\n");
            w.write("}\n");
        }
    }

    private static String escapeJava(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static final class Result {
        final Set<String> propValues;
        final Set<String> typeValues;

        Result(Set<String> propValues, Set<String> typeValues) {
            this.propValues = propValues;
            this.typeValues = typeValues;
        }
    }
}