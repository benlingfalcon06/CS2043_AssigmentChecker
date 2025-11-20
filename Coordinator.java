/********************************************
 * Coordinator class connects the UI to the
 * backened logic of the program.
 * Everyone needs connect their respectivly
 * assigned classes to this class.
 * 
 * 
 * Verison 1.0
 * @author Group 8
 */


public class Coordinator 
{
    private ListOfPrograms programs;
    private ListOfTestSuites suites;

    public Coordinator(ListOfPrograms programs, ListOfTestSuites suites) 
    {
        this.programs = programs;
        this.suites = suites;
    }

    public void runSuiteOnProgram(String suiteName, Program p) 
    {
        TestSuite suite = suites.find(suiteName);

        for (TestCase tc : suite.getTestCases().getAll()) 
        {
            String actual = p.runOnInput(tc.getInput());
            boolean passed = tc.compareOutput(actual);
            System.out.println(tc.getName() + ": " + (passed ? "PASS" : "FAIL"));
        }
    }
}
