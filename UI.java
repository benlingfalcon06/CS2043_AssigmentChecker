/*********************************************************************************
 * UI
 * 
 * author: Farbod Mosalaei
 * Version: 1.0
 * insitally created:  Novemeber 20, 2025
 ******************************************************************************/
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class UI extends JFrame {

    private final Coordinator coordinator = new Coordinator();

    private JLabel submissionLabel;
    private JLabel answerLabel;
    private JTextArea answerArea;
    private JTextArea resultArea;
    private String answerText = null;

    public UI() {
        super("Assignment Checker");

        initComponents();
        layoutComponents();
        attachListeners();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        submissionLabel = new JLabel("No submission selected.");
        answerLabel = new JLabel("No answer file selected.");

        answerArea = new JTextArea();
        answerArea.setEditable(false);
        answerArea.setBorder(new TitledBorder("Answer File Preview"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(new TitledBorder("Results"));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel filePanel = new JPanel(new GridLayout(2, 1));

        JPanel subPanel = new JPanel(new BorderLayout());
        JButton uploadSub = new JButton("Upload Submission (.java)");
        subPanel.add(uploadSub, BorderLayout.EAST);
        subPanel.add(submissionLabel, BorderLayout.CENTER);

        JPanel ansPanel = new JPanel(new BorderLayout());
        JButton uploadAns = new JButton("Upload Answer (.txt)");
        ansPanel.add(uploadAns, BorderLayout.EAST);
        ansPanel.add(answerLabel, BorderLayout.CENTER);

        filePanel.add(subPanel);
        filePanel.add(ansPanel);

        JPanel center = new JPanel(new GridLayout(1, 2));
        center.add(new JScrollPane(answerArea));
        center.add(new JScrollPane(resultArea));

        JButton execute = new JButton("Execute");
        add(filePanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(execute, BorderLayout.SOUTH);

        uploadSub.addActionListener(this::uploadSubmission);
        uploadAns.addActionListener(this::uploadAnswer);
        execute.addActionListener(this::executeTests);
    }

    private void attachListeners() {}

    private void uploadSubmission(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (!f.getName().endsWith(".java")) {
                JOptionPane.showMessageDialog(this, "Must select .java file");
                return;
            }
            coordinator.setCurrentProgram(f);
            submissionLabel.setText(f.getAbsolutePath());
        }
    }

    private void uploadAnswer(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                answerText = Files.readString(f.toPath(), StandardCharsets.UTF_8);
                answerArea.setText(answerText);
                answerLabel.setText(f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot read file");
            }
        }
    }

    private void executeTests(ActionEvent e) {
        resultArea.setText("");

        if (coordinator.getCurrentProgram() == null) {
            resultArea.setText("No submission selected.");
            return;
        }
        if (answerText == null) {
            resultArea.setText("No answer file selected.");
            return;
        }

        StringBuilder log = new StringBuilder();
        boolean pass = coordinator.compileRunCheck(answerText, log);

        resultArea.setText(log.toString() + "\nFinal Result: " + (pass ? "PASS" : "FAIL"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UI().setVisible(true));
    }
}
