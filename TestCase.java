import java.io.FileWriter;
import java.io.Exception;
import java.util.ArrayList;
import java.util.Scanner;

/*****************************************************
 * TestCase for test running the code
 * 
 * Author: Emmanuel Adeyemi-Kings
 * Date: 19th Novemeber 2025
 * Title: Software Engineering project submission 3
 ***************************************************/
public class TestCase
{
    private String expout;
    private String title;
    private ArrayList<String> input;
    
    public TestCase(String title1, File input1, File expout1)
    {
        //use teams message to refine the code before the end of friday
        //input is a txt file
        //expout is a txt file
        //i wanna read the files into this object and save its name as title
        input = new ArrayList<>();
        Scanner fileData = new Scanner(input1);

        while(fileData.hasNextLine())
        {
            input.add(fileData.nextLine());
        }

        //This is for the expected oup put
        Scanner fileout = new Scanner(expout1)
        while(fileout.hasNextLine())
        {
            expout = fileout.nextLine()
        }
    }
    
    //For test
    public static void main(String[] args)
    {
        
    }
}
public class TestCase {
    private final String input;
    private final String expectedOutput;
    private String actualOutput;
    private boolean passed;
    private String errorMessage;

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.actualOutput = null;
        this.passed = false;
        this.errorMessage = null;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Evaluate if the actual output matches expected output
     */
    public void evaluate(String actualOutput) {
        this.actualOutput = actualOutput;
        String normalizedActual = normalize(actualOutput);
        String normalizedExpected = normalize(expectedOutput);
        this.passed = normalizedActual.equals(normalizedExpected);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replace("\r\n", "\n");
    }

    /**
     * Reset the test case result for re-execution
     */
    public void reset() {
        this.actualOutput = null;
        this.passed = false;
        this.errorMessage = null;
    }

    @Override
    public String toString() {
        return "Input: \"" + input + "\"  =>  Expected: \"" + expectedOutput + "\"";
    }

    /**
     * Detailed string with result information
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input: \"").append(input).append("\"\n");
        sb.append("Expected: \"").append(expectedOutput).append("\"\n");
        if (actualOutput != null) {
            sb.append("Actual: \"").append(actualOutput).append("\"\n");
            sb.append("Status: ").append(passed ? "PASS" : "FAIL").append("\n");
        }
        if (errorMessage != null) {
            sb.append("Error: ").append(errorMessage).append("\n");
        }
        return sb.toString();
    }
}
