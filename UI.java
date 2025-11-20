import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class UI extends JFrame {

    private final Coordinator coordinator = new Coordinator();

    // GUI components
    private JLabel selectedFileLabel;
    private JTextArea testSuiteArea;
    private JTextArea resultArea;
    private JButton uploadButton;
    private JButton deleteButton;
    private JButton executeButton;

    public UI() {
        super("Assignment Checker");

        initComponents();
        layoutComponents();
        attachListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // center window
    }

    private void initComponents() {
        selectedFileLabel = new JLabel("No submission selected.");
        selectedFileLabel.setForeground(Color.DARK_GRAY);

        testSuiteArea = new JTextArea(10, 40);
        testSuiteArea.setLineWrap(true);
        testSuiteArea.setWrapStyleWord(true);
        testSuiteArea.setText(
                "# Test suite format:\n" +
                "# one test per line:\n" +
                "# input ==> expected output\n" +
                "# Example:\n" +
                "2 3 ==> 5\n" +
                "10 -4 ==> 6\n"
        );

        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        uploadButton = new JButton("Upload");
        deleteButton = new JButton("Delete");
        executeButton = new JButton("Execute");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));

        // top panel
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.add(new JLabel("Submission: "), BorderLayout.WEST);
        filePanel.add(selectedFileLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(uploadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(executeButton);

        topPanel.add(filePanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // center panel
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane testScroll = new JScrollPane(testSuiteArea);
        testScroll.setBorder(new TitledBorder("Test Suite (input ==> expected output)"));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(new TitledBorder("Results"));

        centerPanel.add(testScroll);
        centerPanel.add(resultScroll);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void attachListeners() {
        uploadButton.addActionListener(this::onUpload);
        deleteButton.addActionListener(this::onDelete);
        executeButton.addActionListener(this::onExecute);
    }

    // ===== button actions =====

    private void onUpload(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select student's .java file");
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (!f.getName().endsWith(".java")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a .java file.",
                        "Invalid file",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            coordinator.setCurrentProgram(f);
            selectedFileLabel.setText(f.getAbsolutePath());
            resultArea.setText("");
        }
    }

    private void onDelete(ActionEvent e) {
        coordinator.setCurrentProgram(null);
        selectedFileLabel.setText("No submission selected.");
        resultArea.setText("");
    }

    private void onExecute(ActionEvent e) {
        resultArea.setText("");

        if (coordinator.getCurrentProgram() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "No submission selected. Please upload a .java file.",
                    "No file",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // 1. Compile
        StringBuilder compileLog = new StringBuilder();
        boolean ok = coordinator.compileCurrentProgram(compileLog);
        appendResult(compileLog.toString());

        if (!ok) {
            appendResult("\nCompilation failed. Fix errors and try again.\n");
            return;
        }

        // 2. Parse test suite from text area
        TestSuit suite = coordinator.parseTestSuite("GUI Suite", testSuiteArea.getText());

        if (suite.getTestCases().size() == 0) {
            appendResult("No valid tests found in test suite.\n");
            return;
        }

        // 3. Run tests
        String runLog = coordinator.runTests(suite);
        appendResult("\n" + runLog);
    }

    private void appendResult(String s) {
        resultArea.append(s);
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    // ===== main =====

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UI().setVisible(true);
        });
    }
}

