public class TestCase {
    private final String input;
    private final String expectedOutput;

    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    @Override
    public String toString() {
        return "Input: \"" + input + "\"  =>  Expected: \"" + expectedOutput + "\"";
    }
}
