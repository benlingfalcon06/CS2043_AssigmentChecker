public class ListOfTestSuites {
    private TestSuit singleSuite;

    public void setSuite(TestSuit suite) {
        this.singleSuite = suite;
    }

    public TestSuit getSuite() {
        return singleSuite;
    }

    public int size() {
        return (singleSuite == null) ? 0 : 1;
    }
}
