public class TestSuit {
    private final String name;
    private final ListOfTestCase testCases;

    public TestSuit(String name, ListOfTestCase testCases) {
        this.name = name;
        this.testCases = testCases;
    }

    public String getName() {
        return name;
    }

    public ListOfTestCase getTestCases() {
        return testCases;
    }

    @Override
    public String toString() {
        return "TestSuit{" + name + ", cases=" + testCases.size() + "}";
    }
}
