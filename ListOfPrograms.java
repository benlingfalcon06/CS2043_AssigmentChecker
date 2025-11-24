import java.util.ArrayList;
import java.util.List;

public class ListOfPrograms {
    private final List<Program> programs;

    public ListOfPrograms() {
        this.programs = new ArrayList<>();
    }

    public void add(Program p) {
        programs.add(p);
    }

    public Program get(int index) {
        return programs.get(index);
    }

    public int size() {
        return programs.size();
    }

    public List<Program> asList() {
        return programs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Program p : programs) {
            sb.append(p.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
