import java.util.ArrayList;
import java.util.List;

public class ListOfTestSuites {
    private final List<TestSuit> suites;

    public ListOfTestSuites() {
        this.suites = new ArrayList<>();
    }

    public void setSuite(TestSuit suite) {
        // Add or replace suite with same name
        for (int i = 0; i < suites.size(); i++) {
            if (suites.get(i).getName().equals(suite.getName())) {
                suites.set(i, suite);
                return;
            }
        }
        suites.add(suite);
    }

    public void addSuite(TestSuit suite) {
        suites.add(suite);
    }

    public TestSuit getSuite(String name) {
        for (TestSuit suite : suites) {
            if (suite.getName().equals(name)) {
                return suite;
            }
        }
        return null;
    }

    public List<TestSuit> getAllSuites() {
        return suites;
    }

    public int size() {
        return suites.size();
    }

    public boolean isEmpty() {
        return suites.isEmpty();
    }

    public List<String> getSuiteNames() {
        List<String> names = new ArrayList<>();
        for (TestSuit suite : suites) {
            names.add(suite.getName());
        }
        return names;
    }

    public void deleteSuite(String name) {
        suites.removeIf(suite -> suite.getName().equals(name));
    }
}
