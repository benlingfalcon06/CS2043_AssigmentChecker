import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Coordinator {

    private final ListOfPrograms programs;
    private final ListOfTestSuites listOfTestSuites;
    private TestSuit currentSuite;

    public Coordinator() {
        this.programs = new ListOfPrograms();
        this.listOfTestSuites = new ListOfTestSuites();
    }

    // ---------------------- Test Suite / Test Cases (kept for design) ----------------------

    public void createEmptyTestSuite(String name) {
        currentSuite = new TestSuit(name, new ListOfTestCase());
        listOfTestSuites.setSuite(currentSuite);
    }

    public TestSuit getCurrentSuite() {
        return currentSuite;
    }

    public void addTestCase(String input, String expectedOutput) {
        if (currentSuite == null) {
            createEmptyTestSuite("DefaultSuite");
        }
        currentSuite.getTestCases().add(new TestCase(input, expectedOutput));
    }

    public void loadTestCasesFromText(String suiteName, String text) {
        ListOfTestCase list = new ListOfTestCase();
        String[] lines = text.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            String[] parts = trimmed.split("==>", 2);
            if (parts.length != 2) {
                continue;
            }
            String input = parts[0].trim();
            String expected = parts[1].trim();
            list.add(new TestCase(input, expected));
        }

        currentSuite = new TestSuit(suiteName, list);
        listOfTestSuites.setSuite(currentSuite);
    }

    // ---------------------- Program discovery (NO more name == .java requirement) ----------------------

    /**
     * Discover Programs under a root folder:
     *  - each subfolder is treated as one program container
     *  - we pick the first .java file inside each subfolder
     */
    public void buildProgramsFromRoot(File rootFolder, StringBuilder log) {
        programs.asList().clear();

        if (rootFolder == null || !rootFolder.isDirectory()) {
            log.append("Root folder is invalid.\n");
            return;
        }

        File[] children = rootFolder.listFiles();
        if (children == null) {
            log.append("Root folder is empty.\n");
            return;
        }

        for (File sub : children) {
            if (!sub.isDirectory()) {
                continue;
            }

            // find first .java file inside this subfolder
            File[] javaFiles = sub.listFiles((dir, name) -> name.endsWith(".java"));
            if (javaFiles != null && javaFiles.length > 0) {
                File javaFile = javaFiles[0];
                String className = javaFile.getName();
                if (className.endsWith(".java")) {
                    className = className.substring(0, className.length() - 5);
                }
                Program p = new Program(className, javaFile);
                programs.add(p);
                log.append("Found program: ").append(p.toString()).append("\n");
            } else {
                log.append("Skipping folder '").append(sub.getName())
                   .append("' (no .java file found).\n");
            }
        }
    }

    // ---------------------- Compilation & execution helpers ----------------------

    private boolean compileProgram(Program program, StringBuilder log) {
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add(program.getJavaFile().getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    log.append(line).append("\n");
                }
            }

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                log.append("Compilation failed for program ")
                   .append(program.getName()).append(" (exit code ")
                   .append(exitCode).append(")\n");
                return false;
            }

            log.append("Compilation successful for program ")
               .append(program.getName()).append(".\n");
            return true;

        } catch (IOException | InterruptedException e) {
            log.append("Compilation error for program ")
               .append(program.getName()).append(": ")
               .append(e.getMessage()).append("\n");
            return false;
        }
    }

    /**
     * Run a program with given stdin content (input.txt).
     */
    private String runProgram(Program program, String stdin) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add(program.getClassPathDir());
        cmd.add(program.getMainClassName());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();

        // send stdin
        try (OutputStream os = p.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
            os.flush();
        }

        // read stdout
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        // read stderr (optional)
        StringBuilder err = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                err.append(line).append("\n");
            }
        }

        int exitCode = p.waitFor();
        if (exitCode != 0 && err.length() > 0) {
            out.append("[Errors:]\n").append(err);
        }

        return normalize(out.toString());
    }

    private String normalize(String s) {
        return s.trim().replace("\r\n", "\n");
    }

    // ---------------------- MAIN EXECUTION: input.txt + expected_output.txt ----------------------

    /**
     * Execute all programs under rootFolder using:
     *  - inputText as stdin
     *  - expectedText as full expected stdout
     * and compare.
     */
    public String executeInputExpected(File rootFolder, String inputText, String expectedText) {
        StringBuilder log = new StringBuilder();

        if (rootFolder == null) {
            log.append("Root folder is null.\n");
            return log.toString();
        }
        if (expectedText == null) {
            log.append("Expected output text is null.\n");
            return log.toString();
        }

        buildProgramsFromRoot(rootFolder, log);

        if (programs.asList().isEmpty()) {
            log.append("No programs found under root.\n");
            return log.toString();
        }

        String normalizedExpected = normalize(expectedText);

        for (Program p : programs.asList()) {
            log.append("\n=== Program: ").append(p.getName()).append(" ===\n");

            if (!compileProgram(p, log)) {
                log.append("Skipping execution due to compilation failure.\n");
                continue;
            }

            try {
                String output = runProgram(p, inputText);
                String normalizedOutput = normalize(output);

                log.append("--- PROGRAM OUTPUT ---\n")
                   .append(normalizedOutput).append("\n");
                log.append("--- EXPECTED OUTPUT ---\n")
                   .append(normalizedExpected).append("\n");

                if (normalizedOutput.equals(normalizedExpected)) {
                    log.append("RESULT: PASS\n");
                } else {
                    log.append("RESULT: FAIL\n");
                }

            } catch (Exception e) {
                log.append("Runtime error for program ")
                   .append(p.getName()).append(": ")
                   .append(e.getMessage()).append("\n");
            }
        }

        return log.toString();
    }
}
