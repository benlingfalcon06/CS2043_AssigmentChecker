import java.util.ArrayList;
import java.util.List;

public class ListOfTestCase {
    private final List<TestCase> cases;

    public ListOfTestCase() {
        this.cases = new ArrayList<>();
    }

    public void add(TestCase t) {
        cases.add(t);
    }

    public int size() {
        return cases.size();
    }

    public TestCase get(int index) {
        return cases.get(index);
    }

    public List<TestCase> asList() {
        return cases;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TestCase tc : cases) {
            sb.append(tc.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
