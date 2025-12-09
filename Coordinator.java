import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public ListOfTestSuites getListOfTestSuites() {
        return listOfTestSuites;
    }

    public List<String> getAllTestSuiteNames() {
        return listOfTestSuites.getSuiteNames();
    }

    public void addTestCaseToSuite(String suiteName, String input, String expectedOutput) {
        TestSuit suite = listOfTestSuites.getSuite(suiteName);
        if (suite == null) {
            createEmptyTestSuite(suiteName);
            suite = currentSuite;
        }
        suite.getTestCases().add(new TestCase(input, expectedOutput));
    }

    // ---------------------- Program discovery (Updated for Version 2) ----------------------

    public void buildProgramsFromRoot(File rootFolder, StringBuilder log) {
        programs.asList().clear();

        if (rootFolder == null || !rootFolder.isDirectory()) {
            log.append("Root folder is invalid.\n");
            return;
        }

        // Look for directories (Student Folders)
        File[] studentFolders = rootFolder.listFiles(File::isDirectory);
        if (studentFolders == null || studentFolders.length == 0) {
            log.append("No student subfolders found in root.\n");
            return;
        }

        log.append("Scanning ").append(studentFolders.length).append(" student folders...\n");

        for (File studentDir : studentFolders) {
            File mainFile = findMainFile(studentDir);
            
            if (mainFile != null) {
                // Program name is the folder name (Student Name)
                Program p = new Program(studentDir.getName(), studentDir, mainFile);
                programs.add(p);
                log.append("  Found: ").append(p.getName()).append(" (Main: ").append(mainFile.getName()).append(")\n");
            } else {
                log.append("  ⚠ WARNING: No file with 'public static void main' found in ").append(studentDir.getName()).append("\n");
            }
        }
    }

    /**
     * Recursive search for a file containing "public static void main"
     */
    private File findMainFile(File directory) {
        try {
            return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toFile)
                .filter(this::containsMainMethod)
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            System.err.println("Error scanning directory " + directory + ": " + e.getMessage());
            return null;
        }
    }

    private boolean containsMainMethod(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("public static void main")) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    // ---------------------- Compilation & Execution ----------------------

    private boolean compileProgram(Program program, StringBuilder log) {
        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add("-sourcepath"); // Ensure it looks in the student's dir for other files
        cmd.add(program.getProjectDirectory().getAbsolutePath()); 
        cmd.add(program.getJavaFile().getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    log.append("    [javac] ").append(line).append("\n");
                }
            }

            int exitCode = p.waitFor();
            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            log.append("Error invoking javac: ").append(e.getMessage()).append("\n");
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

        // Write to STDIN
        try (OutputStream os = p.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            os.flush();
        }

        // Read STDOUT
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                out.append(line).append("\n");
            }
        }

        // Read STDERR
        StringBuilder err = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                err.append(line).append("\n");
            }
        }

        if (err.length() > 0) {
            out.append("[Stderr]:\n").append(err);
        }

        return normalize(out.toString());
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replace("\r\n", "\n");
    }

    // ---------------------- Execution Logic ----------------------

    public String executeWithTestSuite(File rootFolder, String suiteName) {
        StringBuilder log = new StringBuilder();

        TestSuit suite = listOfTestSuites.getSuite(suiteName);
        if (suite == null) return "Test suite not found.";
        
        // FIX: Changed from .isEmpty() to .size() == 0
        if (suite.getTestCases().size() == 0) return "Test suite is empty.";

        buildProgramsFromRoot(rootFolder, log);

        if (programs.asList().isEmpty()) {
            log.append("No valid programs found to run.\n");
            return log.toString();
        }

        suite.resetAllTestCases();
        log.append("\nStarting Execution of Suite: ").append(suiteName).append("\n");
        log.append("--------------------------------------------------\n");

        for (Program program : programs.asList()) {
            log.append("\nTesting Program: ").append(program.getName()).append("\n");

            if (!compileProgram(program, log)) {
                log.append("  ❌ Compilation Failed.\n");
                program.setCompiled(false);
                suite.storeProgramResult(program.getName(), false, 0, 0);
                continue;
            }

            program.setCompiled(true);
            int passed = 0;
            int failed = 0;

            for (TestCase tc : suite.getTestCases().asList()) {
                try {
                    String actual = runProgram(program, tc.getInput());
                    tc.evaluate(actual);
                    if (tc.isPassed()) passed++;
                    else failed++;
                } catch (Exception e) {
                    failed++;
                }
            }

            program.setTestResults(passed, failed);
            suite.storeProgramResult(program.getName(), true, passed, failed);
            
            log.append("  Result: ").append(passed).append("/").append(suite.getTestCases().size())
               .append(" passed (").append(String.format("%.1f%%", program.getPassPercentage())).append(")\n");
        }

        suite.updateStatistics();
        suite.setLastRunDate(LocalDateTime.now());
        suite.saveResults(); // Save to text file

        log.append("\nExecution Complete. Results saved to 'test_results' folder.\n");
        return log.toString();
    }

    // ---------------------- Result Management (Version 2) ----------------------

    public String reloadResults(Path resultFile) {
        try {
            return Files.readString(resultFile);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    /**
     * Parses two result text files and creates a side-by-side comparison table.
     */
    public String compareResultFiles(Path file1, Path file2) {
        Map<String, String> results1 = parseResultFile(file1);
        Map<String, String> results2 = parseResultFile(file2);

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                       COMPARISON REPORT                           \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("File 1: ").append(file1.getFileName()).append("\n");
        sb.append("File 2: ").append(file2.getFileName()).append("\n\n");

        sb.append(String.format("%-25s | %-20s | %-20s | %s\n", "Student (Program)", "Run 1 Rate", "Run 2 Rate", "Change"));
        sb.append("──────────────────────────┼──────────────────────┼──────────────────────┼──────────\n");

        Set<String> allStudents = new TreeSet<>();
        allStudents.addAll(results1.keySet());
        allStudents.addAll(results2.keySet());

        for (String student : allStudents) {
            String r1 = results1.getOrDefault(student, "N/A");
            String r2 = results2.getOrDefault(student, "N/A");
            
            String change = "";
            if (!r1.equals("N/A") && !r2.equals("N/A") && !r1.contains("Fail") && !r2.contains("Fail")) {
                try {
                    double d1 = Double.parseDouble(r1.replace("%", ""));
                    double d2 = Double.parseDouble(r2.replace("%", ""));
                    double diff = d2 - d1;
                    if (diff > 0) change = String.format("+%.1f%%", diff);
                    else if (diff < 0) change = String.format("%.1f%%", diff);
                    else change = "=";
                } catch (NumberFormatException ignored) {}
            }

            sb.append(String.format("%-25s | %-20s | %-20s | %s\n", student, r1, r2, change));
        }
        
        return sb.toString();
    }

    /**
     * Helper to parse the text report format.
     */
    private Map<String, String> parseResultFile(Path path) {
        Map<String, String> map = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(path);
            String currentProgram = null;
            
            Pattern programPattern = Pattern.compile("^Program:\\s+(.+)$");
            Pattern ratePattern = Pattern.compile("^Pass Rate:\\s+([0-9.]+%?)$");

            for (String line : lines) {
                line = line.trim();
                Matcher pm = programPattern.matcher(line);
                if (pm.matches()) {
                    currentProgram = pm.group(1).trim();
                } else if (currentProgram != null) {
                    if (line.contains("COMPILATION FAILED")) {
                        map.put(currentProgram, "Comp. Failed");
                    } else {
                        Matcher rm = ratePattern.matcher(line);
                        if (rm.matches()) {
                            map.put(currentProgram, rm.group(1));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing file " + path + ": " + e.getMessage());
        }
        return map;
    }
}