import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestSuite
{
    // Instance Variables
    private String name;
    private ListOfTestCase testCases = new ListOfTestCase();

    // Req FR-1.2 – create a test suite (group of test cases)
    public TestSuite(String name) 
    {
        this.name = name;
    }

    // Req FR-1.1 – add a test case to a test set/suite
    public void addTestCase(TestCase tc) 
    {
        testCases.add(tc);
    }

    // Req FR-1.1 – remove a test case from the suite
    public void removeTestCase(TestCase tc) 
    {
        testCases.remove(tc);
    }

    // Req FR-7.3 – allow other components to list/search test cases
    public ListOfTestCase getTestCases() 
    {
        return testCases;
    }
}
