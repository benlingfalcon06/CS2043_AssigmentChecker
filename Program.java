import java.io.*;
/******************************
 * The program class
 * 
 * 
 * @author Atharva Naik
 * @version 1.0
 * Initiailly Created on Nov 20th 2025
 */

public class Program 
{
    private String folderPath;
    private String compileCommand = "javac Main.java";
    private String runCommand = "java Main";

    public Program(String folderPath) 
    {
        this.folderPath = folderPath;
    }

    public boolean compile() 
    {
        return runSystemCommand(compileCommand);
    }

    public String runOnInput(String input) 
    {
        try 
        {
            Process process = Runtime.getRuntime().exec(runCommand);
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));

            writer.write(input);
            writer.flush();
            writer.close();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean runSystemCommand(String cmd) 
    {
        try 
        {
            Process p = Runtime.getRuntime().exec(cmd);
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
