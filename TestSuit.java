import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TestSuit {
    private final String name;
    private final ListOfTestCase testCases;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastRunDate;
    private int passedCount;
    private int failedCount;
    
    // Store results per program
    private Map<String, ProgramResult> programResults;
    
    private static final String RESULTS_DIR = "test_results";

    public TestSuit(String name, ListOfTestCase testCases) {
        this.name = name;
        this.testCases = testCases;
        this.description = "";
        this.createdDate = LocalDateTime.now();
        this.lastRunDate = null;
        this.passedCount = 0;
        this.failedCount = 0;
        this.programResults = new HashMap<>();
        
        // Ensure results directory exists
        try {
            Path resultsPath = Paths.get(RESULTS_DIR);
            if (!Files.exists(resultsPath)) {
                Files.createDirectories(resultsPath);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create results directory: " + e.getMessage());
        }
    }

    public String getName() {
        return name;
    }

    public ListOfTestCase getTestCases() {
        return testCases;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastRunDate() {
        return lastRunDate;
    }

    public void setLastRunDate(LocalDateTime lastRunDate) {
        this.lastRunDate = lastRunDate;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public int getTotalCount() {
        return testCases.size();
    }

    /**
     * Calculate and update pass/fail counts based on test case results
     */
    public void updateStatistics() {
        passedCount = 0;
        failedCount = 0;
        
        for (TestCase tc : testCases.asList()) {
            if (tc.getActualOutput() != null) {
                if (tc.isPassed()) {
                    passedCount++;
                } else {
                    failedCount++;
                }
            }
        }
    }

    /**
     * Reset all test cases in this suite
     */
    public void resetAllTestCases() {
        for (TestCase tc : testCases.asList()) {
            tc.reset();
        }
        passedCount = 0;
        failedCount = 0;
        lastRunDate = null;
        programResults.clear();
    }

    /**
     * Get pass percentage
     */
    public double getPassPercentage() {
        int total = passedCount + failedCount;
        if (total == 0) return 0.0;
        return (passedCount * 100.0) / total;
    }

    /**
     * Check if all test cases passed
     */
    public boolean allTestsPassed() {
        return failedCount == 0 && passedCount == testCases.size();
    }
    
    /**
     * Store program result
     */
    public void storeProgramResult(String programName, boolean compiled, int passed, int failed) {
        programResults.put(programName, new ProgramResult(programName, compiled, passed, failed));
    }
    
    /**
     * Get program results
     */
    public Map<String, ProgramResult> getProgramResults() {
        return programResults;
    }
    
    /**
     * Save the test suite results to a text file
     */
    public boolean saveResults() {
        if (lastRunDate == null) {
            System.err.println("Cannot save results: test suite has not been executed yet.");
            return false;
        }
        
        try {
            String timestamp = lastRunDate.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s.txt", name, timestamp);
            Path filePath = Paths.get(RESULTS_DIR, filename);
            
            String report = generateResultReport();
            Files.write(filePath, report.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a human-readable report of the test results
     */
    private String generateResultReport() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                    TEST SUITE EXECUTION REPORT                   \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        sb.append("Suite Name: ").append(name).append("\n");
        if (!description.isEmpty()) {
            sb.append("Description: ").append(description).append("\n");
        }
        sb.append("Execution Date: ").append(lastRunDate.format(formatter)).append("\n");
        sb.append("Total Test Cases: ").append(testCases.size()).append("\n");
        sb.append("Total Programs Tested: ").append(programResults.size()).append("\n\n");
        
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                        PROGRAM RESULTS                           \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        for (ProgramResult pr : programResults.values()) {
            sb.append("───────────────────────────────────────────────────────────────────\n");
            sb.append("Program: ").append(pr.programName).append("\n");
            sb.append("───────────────────────────────────────────────────────────────────\n");
            
            if (!pr.compiled) {
                sb.append("Status: ❌ COMPILATION FAILED\n\n");
                continue;
            }
            
            sb.append("Status: ✓ Compiled Successfully\n");
            sb.append("Tests Passed: ").append(pr.passed).append(" / ").append(testCases.size()).append("\n");
            sb.append("Tests Failed: ").append(pr.failed).append("\n");
            sb.append("Pass Rate: ").append(String.format("%.1f%%", pr.getPassPercentage())).append("\n");
            
            if (pr.passed == testCases.size()) {
                sb.append("★ ALL TESTS PASSED! ★\n");
            }
            sb.append("\n");
        }
        
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                    DETAILED TEST RESULTS                         \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            sb.append("Test Case #").append(i + 1).append(":\n");
            sb.append("  Input: ").append(tc.getInput().replace("\n", "\\n")).append("\n");
            sb.append("  Expected: ").append(tc.getExpectedOutput().replace("\n", "\\n")).append("\n");
            
            if (tc.getActualOutput() != null) {
                sb.append("  Actual: ").append(tc.getActualOutput().replace("\n", "\\n")).append("\n");
                sb.append("  Status: ").append(tc.isPassed() ? "✓ PASS" : "✗ FAIL").append("\n");
            }
            
            if (tc.getErrorMessage() != null) {
                sb.append("  Error: ").append(tc.getErrorMessage()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                          SUMMARY                                 \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");
        
        for (ProgramResult pr : programResults.values()) {
            sb.append(String.format("%-30s", pr.programName));
            
            if (!pr.compiled) {
                sb.append(" : Compilation Failed ❌\n");
            } else {
                sb.append(String.format(" : %d/%d passed (%.1f%%) %s\n",
                    pr.passed,
                    testCases.size(),
                    pr.getPassPercentage(),
                    pr.passed == testCases.size() ? "✓" : ""));
            }
        }
        
        sb.append("\n═══════════════════════════════════════════════════════════════════\n");
        sb.append("                       END OF REPORT                              \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return "TestSuit{" + name + ", cases=" + testCases.size() + "}";
    }

    /**
     * Detailed string with statistics
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Test Suite: ").append(name).append("\n");
        
        if (!description.isEmpty()) {
            sb.append("Description: ").append(description).append("\n");
        }
        
        sb.append("Total Test Cases: ").append(getTotalCount()).append("\n");
        
        if (lastRunDate != null) {
            sb.append("Last Run: ").append(lastRunDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            sb.append("Passed: ").append(passedCount).append("\n");
            sb.append("Failed: ").append(failedCount).append("\n");
            sb.append("Pass Rate: ").append(String.format("%.1f%%", getPassPercentage())).append("\n");
        }
        
        sb.append("Created: ").append(createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Inner class to store program results
     */
    public static class ProgramResult {
        final String programName;
        final boolean compiled;
        final int passed;
        final int failed;
        
        public ProgramResult(String programName, boolean compiled, int passed, int failed) {
            this.programName = programName;
            this.compiled = compiled;
            this.passed = passed;
            this.failed = failed;
        }
        
        public double getPassPercentage() {
            int total = passed + failed;
            if (total == 0) return 0.0;
            return (passed * 100.0) / total;
        }
    }
}
