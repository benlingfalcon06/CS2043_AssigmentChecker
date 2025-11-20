import java.util.ArrayList;
import java.util.List;

public class ListOfPrograms {
    private final List<Program> programs = new ArrayList<>();

    public void add(Program p) {
        programs.add(p);
    }

    public int size() {
        return programs.size();
    }

    public Program get(int index) {
        return programs.get(index);
    }

    public List<Program> asList() {
        return programs;
    }
}

