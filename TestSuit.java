import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestSuit {
    private final String name;
    private final ListOfTestCase testCases;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastRunDate;
    private int passedCount;
    private int failedCount;

    public TestSuit(String name, ListOfTestCase testCases) {
        this.name = name;
        this.testCases = testCases;
        this.description = "";
        this.createdDate = LocalDateTime.now();
        this.lastRunDate = null;
        this.passedCount = 0;
        this.failedCount = 0;
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
}