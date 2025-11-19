
/***************************************************
 * This class is the list of test suites
 * 
 * 
  * author: Atharva Naik
 * Version: 1.0
 * insitally created:  Novemeber 18, 2025
 *******************************************************/

public class ListOfTestSuites 
{
    private ArrayList<TestSuite> suites = new ArrayList<>();

    public void add(TestSuite suite) 
    {

        suites.add(suite);
    }

    public TestSuite find(String name) 
    {
        for (TestSuite suite : suites) 
        {
            if (suite.getName().equals(name)) 
            {
                return suite;
            }
        }
        
        return "Not Found";
    }
}
