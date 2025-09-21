import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

public class BulkRenameWebpToPng {

    private static final String dir = System.getProperty("user.dir") + "\\images\\";

    public static void main(String[] args) {


        Path folder = Paths.get(dir);
        boolean recursive = true;

        if (!Files.isDirectory(folder)) {
            System.err.println("Error: The provided path is not a directory: " + folder);
            System.exit(2);
        }

        Result result = process(folder, recursive);

        System.out.printf(
                "Done. Renamed: %d, Skipped (no change): %d, Skipped (errors): %d%n",
                result.renamed, result.skippedNoChange, result.skippedErrors
        );
    }

    private static Result process(Path folder, boolean recursive) {
        Result result = new Result();

        if (recursive) {
            try {
                Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (attrs.isRegularFile()) {
                            renameFile(file, result);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        System.err.println("Failed to access: " + file + " -> " + exc.getMessage());
                        result.skippedErrors++;
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                System.err.println("Fatal error while walking directory: " + e.getMessage());
            }
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        renameFile(entry, result);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading directory: " + e.getMessage());
            }
        }

        return result;
    }

    private static void renameFile(Path file, Result result) {
        String originalName = file.getFileName().toString();

        // Step 1: Replace spaces with underscores
        String newName = originalName.replace(' ', '_');

        // Step 2: If extension is .webp (case-insensitive), change to .png
        if (newName.toLowerCase(Locale.ROOT).endsWith(".webp")) {
            newName = newName.substring(0, newName.length() - ".webp".length()) + ".png";
        }

        if (newName.equals(originalName)) {
            result.skippedNoChange++;
            return;
        }

        Path parent = file.getParent();
        Path target = parent.resolve(newName);

        // If target exists, create a unique name with a numeric suffix
        if (Files.exists(target)) {
            String base;
            String ext = "";
            int dot = newName.lastIndexOf('.');
            if (dot >= 0) {
                base = newName.substring(0, dot);
                ext = newName.substring(dot); // includes the dot
            } else {
                base = newName;
            }

            int counter = 1;
            do {
                String candidate = base + "_" + counter + ext;
                target = parent.resolve(candidate);
                counter++;
            } while (Files.exists(target));
        }

        try {
            Files.move(file, target);
            System.out.println("Renamed: " + originalName + " -> " + target.getFileName());
            result.renamed++;
        } catch (IOException e) {
            System.err.println("Failed to rename: " + file + " -> " + target + " | " + e.getMessage());
            result.skippedErrors++;
        }
    }

    private static class Result {
        int renamed = 0;
        int skippedNoChange = 0;
        int skippedErrors = 0;
    }
}
