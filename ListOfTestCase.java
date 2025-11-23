import java.util.ArrayList;
import java.util.List;

public class ListOfTestSuites {
    private final List<TestSuit> suites = new ArrayList<>();

    public void add(TestSuit ts) 
    {
        suites.add(ts); 
    }
    public TestSuit get(int i) 
    { 
        return suites.get(i); 
    }
    public int size() 
    { return suites.size(); 
    }
}
