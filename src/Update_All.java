public class Update_All {
    public static void main(String[] args) {
        run("UpdateArmorBasesFromTxt", () -> {
            try {
                UpdateArmorBasesFromTxt.main(new String[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        run("UpdateSkillValues", () -> {
            try {
                UpdateSkillValues.main(new String[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        run("UpdateUniqueItemsStats", () -> {
            try {
                UpdateUniqueItemsStats.main(new String[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        run("UpdateWeaponBasesFromTxt", () -> {
            try {
                UpdateWeaponBasesFromTxt.main(new String[0]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("All updaters finished.");
    }

    private static void run(String name, Runnable r) {
        System.out.println("=== Running " + name + " ===");
        long t0 = System.currentTimeMillis();
        try {
            r.run();
            System.out.println("=== " + name + " OK (" + (System.currentTimeMillis() - t0) + " ms) ===");
        } catch (RuntimeException ex) {
            System.err.println("=== " + name + " FAILED (" + (System.currentTimeMillis() - t0) + " ms) ===");
            ex.printStackTrace();
        }
    }
}
