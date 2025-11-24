import java.io.File;

public class Program {
    private final String name;   // program name = folder name
    private final File javaFile; // the .java file containing main

    public Program(String name, File javaFile) {
        this.name = name;
        this.javaFile = javaFile;
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

    @Override
    public String toString() {
        return "Program{" + name + ", file=" + javaFile.getAbsolutePath() + "}";
    }
}
