import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassJSUpdater {

    /**
     * Updates the number list in amazon.js for a given var code block and label.
     *
     * @param amazonJsPath  Path to the amazon.js file.
     * @param varCodeKey    The code key, e.g., "122" for var d122 = {values:[ ... ]};
     * @param label         The label of the array to update, e.g., "attack rating bonus".
     *                      This must match the string inside the inner array: ["attack rating bonus", ...]
     * @param newNumbersCsv New numbers as "n1,n2,n3,...". Do not include brackets.
     * @throws IOException              If file read/write fails
     * @throws IllegalArgumentException If the key or label cannot be found
     */
    public static void updateNumberList(Path amazonJsPath,
                                        String varCodeKey,
                                        String label,
                                        String newNumbersCsv) throws IOException {
        if (varCodeKey.startsWith("d"))
            varCodeKey = varCodeKey.substring(1);
        if (newNumbersCsv.startsWith(","))
            newNumbersCsv = newNumbersCsv.substring(1);
        String content = Files.readString(amazonJsPath, StandardCharsets.UTF_8);

        // 1) Find the var block for the given key: var d<key> = {values:[ ... ]};
        //    We capture everything from var d<key> through the matching ]}; of values.
        String varPattern = "(?s)(/\\*\\[[^\\]]*\\]\\s*.*?\\*/\\s*)?var\\s+d" + Pattern.quote(varCodeKey) +
                "\\s*=\\s*\\{\\s*values\\s*:\\s*\\[(.*?)\\]\\s*\\};";
        Pattern pVar = Pattern.compile(varPattern);
        Matcher mVar = pVar.matcher(content);
        if (!mVar.find()) {
            throw new IllegalArgumentException(amazonJsPath.getFileName().toFile() + " Could not find var d" + varCodeKey + " block.");
        }

        String valuesBlock = mVar.group(1) == null ? mVar.group(0) : mVar.group(0); // full matched block
        String innerArrays = mVar.group(2); // content inside values: [ ... ] (inner arrays)

        // 2) Inside inner arrays, find the one with the given label: ["label", ...numbers...]
        //    We capture the list after the first comma up to the closing bracket of this inner array.
        String arrayPattern = "(?s)\\[\\s*\"" + Pattern.quote(label) + "\"\\s*,\\s*(.*?)\\s*\\]";
        Pattern pArr = Pattern.compile(arrayPattern);
        Matcher mArr = pArr.matcher(innerArrays);
        if (!mArr.find()) {
            System.err.println(amazonJsPath.getFileName().toString() + " Label \"" + label + "\" not found under d" + varCodeKey + ".");
            return;
        }

        String oldNumbers = mArr.group(1);

        // 3) Replace old numbers with the new list
        String newInnerArrays = new StringBuilder(innerArrays)
                .replace(mArr.start(1), mArr.end(1), newNumbersCsv)
                .toString();

        // 4) Rebuild the whole content: replace only the inner arrays part of this var block
        // Reconstruct the specific var block with updated inner arrays
        String updatedVarBlock = mVar.group(0).replace(innerArrays, newInnerArrays);

        // 5) Replace in the full file
        String updatedContent = new StringBuilder(content)
                .replace(mVar.start(), mVar.end(), updatedVarBlock)
                .toString();

        Files.writeString(amazonJsPath, updatedContent, StandardCharsets.UTF_8);
        System.out.println("Updated " + amazonJsPath.getFileName() + ".");
    }

    public static final String dir = System.getProperty("user.dir") + "\\data\\skills\\PD2\\";
    public static final String amazon = dir + "amazon.js";
    public static final String assassin = dir + "assassin.js";
    public static final String barbarian = dir + "barbarian.js";
    public static final String druid = dir + "druid.js";
    public static final String necromancer = dir + "necromancer.js";
    public static final String paladin = dir + "paladin.js";
    public static final String sorceress = dir + "sorceress.js";

    // Example usage:
    public static void main(String[] args) throws Exception {
        Path path = Path.of(amazon);
        String varKey = "122"; // e.g., Power Strike => var d122
        String label = "attack rating bonus";
        String numbers = "20,32,44,56,68,80,92,104,116,128"; // your replacement list
        updateNumberList(path, varKey, label, numbers);
        System.out.println("Update completed.");
    }
}
