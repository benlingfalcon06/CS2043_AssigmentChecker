import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Coordinator {

    private Program currentProgram;
    private final ListOfPrograms programs;

    public Coordinator() {
        this.programs = new ListOfPrograms();
    }

    public void setCurrentProgram(File javaFile) {
        if (javaFile == null) {
            currentProgram = null;
        } else {
            currentProgram = new Program(javaFile);
            programs.add(currentProgram);
        }
    }

    public Program getCurrentProgram() {
        return currentProgram;
    }

    // -------------------- COMPILATION --------------------

    public boolean compileCurrentProgram(StringBuilder log) {
        if (currentProgram == null) {
            log.append("No submission selected.\n");
            return false;
        }

        String path = currentProgram.getJavaFile().getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add("javac");
        cmd.add(path);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            while ((line = r.readLine()) != null)
                log.append(line).append("\n");

            int exitCode = p.waitFor();
            if (exitCode != 0) {
                log.append("Compilation failed.\n");
                return false;
            }

            log.append("Compilation successful.\n");
            return true;

        } catch (Exception e) {
            log.append("Compilation error: ").append(e.getMessage()).append("\n");
            return false;
        }
    }

    // ----------------------- RUN -------------------------

    public String runProgram(StringBuilder log) throws Exception {
        if (currentProgram == null)
            throw new IllegalStateException("No program selected.");

        String classDir = currentProgram.getClassPathDir();
        String mainClass = currentProgram.getMainClassName();

        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("-cp");
        cmd.add(classDir);
        cmd.add(mainClass);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();

        p.getOutputStream().close();

        StringBuilder out = new StringBuilder();
        BufferedReader rOut = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)
        );
        String line;
        while ((line = rOut.readLine()) != null)
            out.append(line).append("\n");

        BufferedReader rErr = new BufferedReader(
                new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8)
        );
        while ((line = rErr.readLine()) != null)
            log.append(line).append("\n");

        p.waitFor();
        return out.toString();
    }

    // -------------------- COMPARISON ---------------------

    private String normalize(String s) {
        return s.trim().replace("\r\n", "\n");
    }

    public boolean compareOutput(String programOutput, String answerText) {
        return normalize(programOutput).equals(normalize(answerText));
    }

    public boolean compileRunCheck(String answerText, StringBuilder log) {
        if (!compileCurrentProgram(log)) return false;

        String output;
        try {
            output = runProgram(log);
        } catch (Exception e) {
            log.append("Runtime error: ").append(e.getMessage()).append("\n");
            return false;
        }

        boolean match = compareOutput(output, answerText);

        log.append("\n--- PROGRAM OUTPUT ---\n").append(output).append("\n");
        log.append("--- EXPECTED ANSWER ---\n").append(answerText).append("\n");
        log.append("--- RESULT ---\n").append(match ? "PASS\n" : "FAIL\n");

        return match;
    }
}

