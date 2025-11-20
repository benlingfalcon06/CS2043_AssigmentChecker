import java.util.ArrayList;
import java.util.List;

public class ListOfTestCase {
    private final List<TestCase> cases = new ArrayList<>();

    public void add(TestCase tc) {
        cases.add(tc);
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
}

