import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    // ---------------------- Test Suite / Test Cases ----------------------

    public void createEmptyTestSuite(String name) {
        currentSuite = new TestSuit(name, new ListOfTestCase());
        listOfTestSuites.setSuite(currentSuite);
    }

    public TestSuit getCurrentSuite() {
        return currentSuite;
    }

    public void setCurrentSuite(String suiteName) {
        currentSuite = listOfTestSuites.getSuite(suiteName);
    }

    public ListOfTestSuites getListOfTestSuites() {
        return listOfTestSuites;
    }

    public List<String> getAllTestSuiteNames() {
        return listOfTestSuites.getSuiteNames();
    }

    public void addTestCase(String input, String expectedOutput) {
        if (currentSuite == null) {
            createEmptyTestSuite("DefaultSuite");
        }
        currentSuite.getTestCases().add(new TestCase(input, expectedOutput));
    }

    public void addTestCaseToSuite(String suiteName, String input, String expectedOutput) {
        TestSuit suite = listOfTestSuites.getSuite(suiteName);
        if (suite == null) {
            createEmptyTestSuite(suiteName);
            suite = currentSuite;
        }
        suite.getTestCases().add(new TestCase(input, expectedOutput));
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

    public void uploadTestCasesToSuite(String suiteName, String text) {
        TestSuit suite = listOfTestSuites.getSuite(suiteName);
        if (suite == null) {
            loadTestCasesFromText(suiteName, text);
            return;
        }

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
            suite.getTestCases().add(new TestCase(input, expected));
        }
    }

    // ---------------------- Program discovery ----------------------

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
                    new InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
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

    private String runProgram(Program program, String stdin) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add(program.getClassPathDir());
        cmd.add(program.getMainClassName());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();

        try (OutputStream os = p.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            os.flush();
        }

        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        StringBuilder err = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
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
        if (s == null) return "";
        return s.trim().replace("\r\n", "\n");
    }

    // ---------------------- EXECUTION WITH TEST SUITES ----------------------

    /**
     * Execute all programs using a specific test suite
     */
    public String executeWithTestSuite(File rootFolder, String suiteName) {
        StringBuilder log = new StringBuilder();

        if (rootFolder == null) {
            log.append("Root folder is null.\n");
            return log.toString();
        }

        TestSuit suite = listOfTestSuites.getSuite(suiteName);
        if (suite == null) {
            log.append("Test suite '").append(suiteName).append("' not found.\n");
            return log.toString();
        }

        if (suite.getTestCases().size() == 0) {
            log.append("Test suite '").append(suiteName).append("' has no test cases.\n");
            return log.toString();
        }

        buildProgramsFromRoot(rootFolder, log);

        if (programs.asList().isEmpty()) {
            log.append("No programs found under root.\n");
            return log.toString();
        }

        log.append("\n╔═══════════════════════════════════════════════════════════╗\n");
        log.append("  Testing with Suite: ").append(suiteName).append("\n");
        log.append("  Total Test Cases: ").append(suite.getTestCases().size()).append("\n");
        log.append("╚═══════════════════════════════════════════════════════════╝\n\n");

        // Reset all test cases before execution
        suite.resetAllTestCases();

        for (Program program : programs.asList()) {
            log.append("\n════════════════════════════════════════════════════════════\n");
            log.append("  Program: ").append(program.getName()).append("\n");
            log.append("════════════════════════════════════════════════════════════\n");

            if (!compileProgram(program, log)) {
                log.append("❌ Skipping execution due to compilation failure.\n");
                program.setCompiled(false);
                
                // Store compilation failure result
                suite.storeProgramResult(program.getName(), false, 0, 0);
                continue;
            }

            program.setCompiled(true);
            int passed = 0;
            int failed = 0;

            for (int i = 0; i < suite.getTestCases().size(); i++) {
                TestCase testCase = suite.getTestCases().get(i);
                log.append("\n--- Test Case ").append(i + 1).append(" ---\n");
                log.append("Input: ").append(testCase.getInput()).append("\n");
                log.append("Expected: ").append(testCase.getExpectedOutput()).append("\n");

                try {
                    String output = runProgram(program, testCase.getInput());
                    testCase.evaluate(output);

                    log.append("Actual: ").append(output).append("\n");

                    if (testCase.isPassed()) {
                        log.append("✓ PASS\n");
                        passed++;
                    } else {
                        log.append("✗ FAIL\n");
                        failed++;
                    }

                } catch (Exception e) {
                    testCase.setErrorMessage(e.getMessage());
                    log.append("❌ Runtime Error: ").append(e.getMessage()).append("\n");
                    failed++;
                }
            }

            log.append("\n────────────────────────────────────────────────────────────\n");
            log.append("  Results for ").append(program.getName()).append(":\n");
            log.append("  Passed: ").append(passed).append(" / ").append(suite.getTestCases().size()).append("\n");
            log.append("  Failed: ").append(failed).append("\n");
            double percentage = (passed * 100.0) / suite.getTestCases().size();
            log.append("  Pass Rate: ").append(String.format("%.1f%%", percentage)).append("\n");
            log.append("────────────────────────────────────────────────────────────\n");

            // Store results in program
            program.setTestResults(passed, failed);
            
            // Store results in suite
            suite.storeProgramResult(program.getName(), true, passed, failed);
        }

        // Update suite statistics
        suite.updateStatistics();
        suite.setLastRunDate(LocalDateTime.now());

        // Overall summary
        log.append("\n╔═══════════════════════════════════════════════════════════╗\n");
        log.append("  OVERALL SUMMARY\n");
        log.append("╚═══════════════════════════════════════════════════════════╝\n");
        
        for (Program program : programs.asList()) {
            if (program.isCompiled()) {
                log.append("\n").append(program.getName()).append(": ");
                log.append(program.getPassedTests()).append(" passed, ");
                log.append(program.getFailedTests()).append(" failed ");
                log.append("(").append(String.format("%.1f%%", program.getPassPercentage())).append(")");
                
                if (program.getPassedTests() == suite.getTestCases().size()) {
                    log.append(" ✓ ALL TESTS PASSED");
                }
                log.append("\n");
            } else {
                log.append("\n").append(program.getName()).append(": Compilation Failed ❌\n");
            }
        }
        
        // Save the result to disk
        if (suite.saveResults()) {
            log.append("\n✓ Results saved successfully to test_results/").append(suiteName).append("_[timestamp].txt\n");
        } else {
            log.append("\n⚠ Warning: Could not save results to disk.\n");
        }

        return log.toString();
    }

    public ListOfPrograms getPrograms() {
        return programs;
    }
    
     // ----------------------------------------------------------
    // Reload a single result file
    // ----------------------------------------------------------
    public String reloadResults(Path resultFile) {
        StringBuilder log = new StringBuilder();

        if (resultFile == null) {
            return "No result file selected.\n";
        }

        log.append("Reloading results from: ")
           .append(resultFile.toAbsolutePath())
           .append(System.lineSeparator())
           .append(System.lineSeparator());

        try {
            java.util.List<String> lines =
                    Files.readAllLines(resultFile, java.nio.charset.StandardCharsets.UTF_8);
            for (String line : lines) {
                log.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.append("Error reading result file: ")
               .append(e.getMessage())
               .append(System.lineSeparator());
        }

        return log.toString();
    }

    // ----------------------------------------------------------
    // Compare two result files line by line
    // ----------------------------------------------------------
    public String compareResultFiles(Path file1, Path file2) {
        StringBuilder log = new StringBuilder();

        if (file1 == null || file2 == null) {
            return "Both result files must be selected.\n";
        }

        log.append("Comparing result files:")
           .append(System.lineSeparator());
        log.append("  File 1: ").append(file1.toAbsolutePath())
           .append(System.lineSeparator());
        log.append("  File 2: ").append(file2.toAbsolutePath())
           .append(System.lineSeparator())
           .append(System.lineSeparator());

        try {
            java.util.List<String> lines1 =
                    Files.readAllLines(file1, java.nio.charset.StandardCharsets.UTF_8);
            java.util.List<String> lines2 =
                    Files.readAllLines(file2, java.nio.charset.StandardCharsets.UTF_8);

            int maxLines = Math.max(lines1.size(), lines2.size());
            boolean anyDifference = false;

            for (int i = 0; i < maxLines; i++) {
                String l1 = (i < lines1.size()) ? lines1.get(i) : "";
                String l2 = (i < lines2.size()) ? lines2.get(i) : "";

                if (!l1.equals(l2)) {
                    anyDifference = true;
                    log.append("Line ").append(i + 1).append(" differs:")
                       .append(System.lineSeparator());
                    log.append("  < ").append(l1).append(System.lineSeparator());
                    log.append("  > ").append(l2).append(System.lineSeparator())
                       .append(System.lineSeparator());
                }
            }

            if (!anyDifference) {
                log.append("The two result files are identical.")
                   .append(System.lineSeparator());
            }
        } catch (IOException e) {
            log.append("Error reading result files: ")
               .append(e.getMessage())
               .append(System.lineSeparator());
        }

        return log.toString();
    }
}
