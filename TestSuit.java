import java.lang.reflect.Array;
import java.util.ArrayList;

/*********************************************************************************
 * This class defines test suites for grouping multiple test cases and addding them
 * 
 * author: Atharva Naik
 * Version: 1.0
 * insitally created:  Novemeber 18, 2025
 ******************************************************************************/

public class TestSuit
{
    // Instance Variables
    private String name;
    private ListOfTestCases testCases = new ListOfTestCases();
    
    // Constructor
    public TestSuite(String name) 
    {
        this.name = name;
    }

    // Method to add test case to the test suite
    public void addTestCase(TestCase tc) 
    {
        testCases.add(tc);
    }

    public void removeTestCase(TestCase tc) 
    {
        testCases.remove(tc);
    }


    // Getter for test cases
    public ListOfTestCases getTestCases() 
    {
        return testCases;
    }




}
