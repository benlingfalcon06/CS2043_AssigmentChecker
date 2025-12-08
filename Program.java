import java.io.File;

public class Program {
    private final String name;   // program name = folder name
    private final File javaFile; // the .java file containing main
    private boolean compiled;    // compilation status
    private int passedTests;     // number of passed tests
    private int failedTests;     // number of failed tests

    public Program(String name, File javaFile) {
        this.name = name;
        this.javaFile = javaFile;
        this.compiled = false;
        this.passedTests = 0;
        this.failedTests = 0;
    }

    public String getName() {
        return name;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public String getMainClassName() {
        // class name assumed to be file name without .java
        String fileName = javaFile.getName();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }

    public String getClassPathDir() {
        return javaFile.getParentFile().getAbsolutePath();
    }

    public boolean isCompiled() {
        return compiled;
    }

    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public int getFailedTests() {
        return failedTests;
    }

    public void setTestResults(int passed, int failed) {
        this.passedTests = passed;
        this.failedTests = failed;
    }

    public int getTotalTests() {
        return passedTests + failedTests;
    }

    public double getPassPercentage() {
        int total = getTotalTests();
        if (total == 0) return 0.0;
        return (passedTests * 100.0) / total;
    }

    public void resetTestResults() {
        this.passedTests = 0;
        this.failedTests = 0;
        this.compiled = false;
    }

    @Override
    public String toString() {
        return "Program{" + name + ", file=" + javaFile.getAbsolutePath() + "}";
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Program: ").append(name).append("\n");
        sb.append("File: ").append(javaFile.getAbsolutePath()).append("\n");
        sb.append("Compiled: ").append(compiled ? "Yes" : "No").append("\n");
        
        if (getTotalTests() > 0) {
            sb.append("Tests Passed: ").append(passedTests).append("\n");
            sb.append("Tests Failed: ").append(failedTests).append("\n");
            sb.append("Pass Rate: ").append(String.format("%.1f%%", getPassPercentage())).append("\n");
        }
        
        return sb.toString();
    }
}