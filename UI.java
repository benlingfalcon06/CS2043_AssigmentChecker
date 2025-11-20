import javax.swing.*;
import java.awt.*;
/***************************************************88
 * UI Class for Test Runner Application
 * 
 * author: Atharva Naik
 * Date: November 19, 2025
 * 
 */

public class UI extends JFrame 
{

    private Coordinator coordinator;

    private JComboBox<String> programList;
    private JComboBox<String> suiteList;
    private JTextArea output;

    public UI(Coordinator coordinator) 
    {
        this.coordinator = coordinator;

        setTitle("Test Runner UI");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        ///////////////////////////////////////////
        // TOP PANEL (Buttons)
        ///////////////////////////////////////////
        JPanel topPanel = new JPanel();
        JButton btnLoadPrograms = new JButton("List Programs");
        JButton btnLoadSuites = new JButton("List Test Suites");
        JButton btnRun = new JButton("Run Tests");

        topPanel.add(btnLoadPrograms);
        topPanel.add(btnLoadSuites);
        topPanel.add(btnRun);

        add(topPanel, BorderLayout.NORTH);

        ///////////////////////////////////////////
        // MIDDLE PANEL (Dropdowns)
        ///////////////////////////////////////////
        JPanel middlePanel = new JPanel(new GridLayout(2, 2));

        middlePanel.add(new JLabel("Select Program:"));
        programList = new JComboBox<>();
        middlePanel.add(programList);

        middlePanel.add(new JLabel("Select Test Suite:"));
        suiteList = new JComboBox<>();
        middlePanel.add(suiteList);

        add(middlePanel, BorderLayout.CENTER);

        ///////////////////////////////////////////
        // OUTPUT AREA
        ///////////////////////////////////////////
        output = new JTextArea();
        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.SOUTH);

        ///////////////////////////////////////////
        // BUTTON ACTIONS
        ///////////////////////////////////////////

        // Load programs into dropdown
        btnLoadPrograms.addActionListener(e -> {
            programList.removeAllItems();
            var programs = coordinator.getPrograms().getAll();
            for (Program p : programs) {
                programList.addItem(p.getFolderPath());
            }
            output.setText("Programs loaded.");
        });

        // Load suites into dropdown
        btnLoadSuites.addActionListener(e -> {
            suiteList.removeAllItems();
            var suites = coordinator.getSuites().getAll();
            for (TestSuite ts : suites) {
                suiteList.addItem(ts.getName());
            }
            output.setText("Test suites loaded.");
        });

        // RUN button
        btnRun.addActionListener(e -> runSelected());

        setVisible(true);
    }

    private void runSelected() {

        int pIndex = programList.getSelectedIndex();
        int sIndex = suiteList.getSelectedIndex();

        if (pIndex < 0 || sIndex < 0) {
            output.setText("Please select both a Program and a Test Suite.");
            return;
        }

        Program p = coordinator.getPrograms().getAll().get(pIndex);
        TestSuite suite = coordinator.getSuites().getAll().get(sIndex);

        StringBuilder results = new StringBuilder();
        results.append("Running ").append(suite.getName()).append("...\n");

        for (TestCase tc : suite.getTestCases().getAll()) 
        {
            String actual = p.runOnInput(tc.getInput());
            boolean pass = tc.compareOutput(actual);
            results.append(tc.getName())
                    .append(": ")
                    .append(pass ? "PASS" : "FAIL")
                    .append("\n");
        }

        output.setText(results.toString());
    }
}
