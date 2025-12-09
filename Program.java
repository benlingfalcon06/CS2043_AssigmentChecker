import java.io.File;

public class Program {
    private final String name;           // Student name (folder name)
    private final File projectDirectory; // The folder containing the student's code
    private final File javaFile;         // The specific .java file containing main()
    private boolean compiled;
    private int passedTests;
    private int failedTests;

    public Program(String name, File projectDirectory, File javaFile) {
        this.name = name;
        this.projectDirectory = projectDirectory;
        this.javaFile = javaFile;
        this.compiled = false;
        this.passedTests = 0;
        this.failedTests = 0;
    }

    public String getName() {
        return name;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public String getMainClassName() {
        // Class name is the file name without .java
        String fileName = javaFile.getName();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }

    public String getClassPathDir() {
        // Execution classpath is the directory containing the file
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

    @Override
    public String toString() {
        return "Program{" + name + ", dir=" + projectDirectory.getName() + ", main=" + javaFile.getName() + "}";
    }
}