public class ListOfTestCase 
{
    private final List<TestCase> cases = new ArrayList<>();

    // Req FR-1.1 – add a new test case to the test set
    public void add(TestCase tc) 
    {
        if (tc == null) throw new IllegalArgumentException("TestCase cannot be null");
        cases.add(tc);
    }

    // Req FR-1.1 – modify an existing test case
    public void set(int index, TestCase tc) 
    {
        if (tc == null) throw new IllegalArgumentException("TestCase cannot be null");
        checkIndex(index);
        cases.set(index, tc);
    }

    // Req FR-7.3 – allow listing test cases
    public int size() 
    {
        return cases.size();
    }

    // Req FR-7.3 – access a specific test case
    public TestCase get(int index) 
    {
        checkIndex(index);
        return cases.get(index);
    }

    // Req NFR-MAINT-1 – provide safe read-only access to all test cases
    public List<TestCase> asList() 
    {
        return Collections.unmodifiableList(cases);
    }

    // Req FR-1.1 – delete a specific test case
    public boolean remove(TestCase tc) 
    {
        return cases.remove(tc);
    }

    // Req FR-1.1 – clear all test cases
    public void clear() 
    {
        cases.clear();
    }

    // Req FR-7.3 – search for a test case by title
    public TestCase findByTitle(String title) 
    {
        if (title == null) return null;
        for (TestCase tc : cases)
        {
            if (title.equals(tc.getTitle()))
                return tc;
        }
        return null;
    }

    // Req FR-1.1 + FR-7.3 – delete by test case name
    public boolean removeByTitle(String title) 
    {
        if (title == null) return false;
        return cases.removeIf(tc -> Objects.equals(title, tc.getTitle()));
    }

    // Req NFR-ROBUST-1 – internal safety check for valid indexes
    private void checkIndex(int index) 
    {
        if (index < 0 || index >= cases.size()) 
        {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }

    // (No requirement — utility for debugging)
    public String toString() 
    {
        return "ListOfTestCase{" + "cases=" + cases + '}';
    }
}
