import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class UI extends Application {

    private final Coordinator coordinator = new Coordinator();

    // UI Components
    private Label rootFolderLabel;
    private File rootFolder;
    private TextArea logArea;
    private ComboBox<String> suiteComboBox;
    
    // Comparison Vars
    private File comparisonFile1;
    private File comparisonFile2;
    private Label file1Label;
    private Label file2Label;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Assignment Checker - Version 2");

        VBox mainPane = new VBox(15);
        mainPane.setPadding(new Insets(15));

        // ----- Header -----
        Label headerLabel = new Label("Assignment Checker Tool");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // ----- 1. Test Suite Section (Restored Buttons) -----
        TitledPane suitePane = createTestSuiteSection(primaryStage);
        
        // ----- 2. Execution Section -----
        TitledPane execPane = createExecutionSection(primaryStage);
        
        // ----- 3. Comparison Section (New Version 2) -----
        TitledPane comparePane = createComparisonSection(primaryStage);

        // ----- Log Area -----
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPromptText("Execution and Comparison logs will appear here...");
        logArea.setStyle("-fx-font-family: 'Courier New', monospace;");
        
        // Layout
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(suitePane, execPane, comparePane);
        accordion.setExpandedPane(execPane); // Default open

        mainPane.getChildren().addAll(headerLabel, accordion, new Label("Output Log:"), logArea);

        Scene scene = new Scene(mainPane, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Init
        coordinator.createEmptyTestSuite("DefaultSuite");
        refreshSuiteList();
    }

    // --- Section Creation Helpers ---

    private TitledPane createTestSuiteSection(Stage stage) {
        Button createBtn = new Button("Create Suite");
        createBtn.setOnAction(e -> onCreateTestSuite());
        
        Button addCaseBtn = new Button("Add Test Case");
        addCaseBtn.setOnAction(e -> onAddTestCase());
        
        // --- RESTORED BUTTONS ---
        Button manageBtn = new Button("Manage Test Cases");
        manageBtn.setOnAction(e -> onManageTestCases(stage));

        Button deleteSuiteBtn = new Button("Delete Suite");
        deleteSuiteBtn.setStyle("-fx-text-fill: red;");
        deleteSuiteBtn.setOnAction(e -> onDeleteTestSuite());
        // ------------------------

        HBox box = new HBox(10, createBtn, addCaseBtn, manageBtn, deleteSuiteBtn);
        box.setPadding(new Insets(10));
        return new TitledPane("Test Suite Management", box);
    }

    private TitledPane createExecutionSection(Stage stage) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        // Suite Selection
        HBox suiteBox = new HBox(10, new Label("Select Suite:"), suiteComboBox = new ComboBox<>());
        suiteComboBox.setPrefWidth(200);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshSuiteList());
        suiteBox.getChildren().add(refreshBtn);

        // Root Folder Selection
        rootFolderLabel = new Label("No folder selected");
        Button rootBtn = new Button("Select Root Folder (containing Student Subfolders)");
        rootBtn.setOnAction(e -> onChooseRootFolder(stage));
        HBox rootBox = new HBox(10, rootBtn, rootFolderLabel);

        // Run Button
        Button runBtn = new Button("RUN TEST SUITE");
        runBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        runBtn.setOnAction(e -> onExecute());
        
        // View Results
        Button viewResultsBtn = new Button("View/Reload Specific Result File");
        viewResultsBtn.setOnAction(e -> onViewResultFile(stage));

        box.getChildren().addAll(suiteBox, rootBox, new Separator(), runBtn, viewResultsBtn);
        return new TitledPane("Execution & Results", box);
    }

    private TitledPane createComparisonSection(Stage stage) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        
        Label desc = new Label("Select two result files to compare success rates side-by-side.");
        
        // File 1
        Button btn1 = new Button("Select Result File 1");
        file1Label = new Label("None");
        btn1.setOnAction(e -> {
            File f = chooseResultFile(stage);
            if (f != null) {
                comparisonFile1 = f;
                file1Label.setText(f.getName());
            }
        });
        
        // File 2
        Button btn2 = new Button("Select Result File 2");
        file2Label = new Label("None");
        btn2.setOnAction(e -> {
            File f = chooseResultFile(stage);
            if (f != null) {
                comparisonFile2 = f;
                file2Label.setText(f.getName());
            }
        });

        Button compareBtn = new Button("COMPARE RESULTS");
        compareBtn.setOnAction(e -> onCompare());

        HBox r1 = new HBox(10, btn1, file1Label);
        HBox r2 = new HBox(10, btn2, file2Label);
        
        box.getChildren().addAll(desc, r1, r2, new Separator(), compareBtn);
        return new TitledPane("Result Comparison (Version 2)", box);
    }

    // --- Logic & Handlers ---

    private void refreshSuiteList() {
        String selected = suiteComboBox.getValue();
        suiteComboBox.getItems().clear();
        suiteComboBox.getItems().addAll(coordinator.getAllTestSuiteNames());
        if (selected != null && suiteComboBox.getItems().contains(selected)) {
            suiteComboBox.setValue(selected);
        } else if (!suiteComboBox.getItems().isEmpty()) {
            suiteComboBox.setValue(suiteComboBox.getItems().get(0));
        }
    }

    private void onCreateTestSuite() {
        TextInputDialog dialog = new TextInputDialog("NewSuite");
        dialog.setTitle("Create Test Suite");
        dialog.setHeaderText("Enter name for new test suite:");
        dialog.showAndWait().ifPresent(name -> {
            if (coordinator.getAllTestSuiteNames().contains(name)) {
                showAlert("Suite already exists!");
                return;
            }
            coordinator.createEmptyTestSuite(name);
            refreshSuiteList();
            suiteComboBox.setValue(name);
            logArea.appendText("Created suite: " + name + "\n");
        });
    }

    private void onAddTestCase() {
        if (suiteComboBox.getValue() == null) {
            showAlert("Create or select a test suite first.");
            return;
        }
        
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Test Case");
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextArea input = new TextArea(); input.setPrefRowCount(3);
        TextArea expect = new TextArea(); expect.setPrefRowCount(3);
        
        grid.add(new Label("Input:"), 0, 0); grid.add(input, 1, 0);
        grid.add(new Label("Expected:"), 0, 1); grid.add(expect, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(btn -> btn == ButtonType.OK ? new String[]{input.getText(), expect.getText()} : null);
        
        dialog.showAndWait().ifPresent(res -> {
            if (res[0].trim().isEmpty() || res[1].trim().isEmpty()) {
                showAlert("Input and Expected Output cannot be empty.");
                return;
            }
            coordinator.addTestCaseToSuite(suiteComboBox.getValue(), res[0], res[1]);
            logArea.appendText("Added test case to " + suiteComboBox.getValue() + "\n");
        });
    }

    // --- RESTORED: Manage Test Cases (View/Edit/Delete) ---
    private void onManageTestCases(Stage stage) {
        String selectedSuite = suiteComboBox.getValue();
        if (selectedSuite == null) {
            showAlert("Select a test suite first.");
            return;
        }

        TestSuit suite = coordinator.getListOfTestSuites().getSuite(selectedSuite);
        if (suite == null || suite.getTestCases().size() == 0) {
            showAlert("Suite is empty or not found.");
            return;
        }

        Dialog<Void> manageDialog = new Dialog<>();
        manageDialog.setTitle("Manage Test Cases: " + selectedSuite);
        manageDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        ListView<String> listView = new ListView<>();
        updateListView(listView, suite);

        Button editBtn = new Button("Edit Selected");
        Button deleteBtn = new Button("Delete Selected");
        Button deleteAllBtn = new Button("Delete All");
        deleteAllBtn.setStyle("-fx-text-fill: red;");

        HBox btns = new HBox(10, editBtn, deleteBtn, deleteAllBtn);
        content.getChildren().addAll(listView, btns);
        manageDialog.getDialogPane().setContent(content);

        // Edit Action
        editBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                TestCase tc = suite.getTestCases().get(idx);
                // Show Edit Dialog
                Dialog<TestCaseEdit> editDialog = new Dialog<>();
                editDialog.setTitle("Edit Test Case");
                
                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);
                TextArea input = new TextArea(tc.getInput());
                TextArea expect = new TextArea(tc.getExpectedOutput());
                
                grid.add(new Label("Input:"), 0, 0); grid.add(input, 1, 0);
                grid.add(new Label("Expected:"), 0, 1); grid.add(expect, 1, 1);
                
                editDialog.getDialogPane().setContent(grid);
                editDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                
                editDialog.setResultConverter(b -> b == ButtonType.OK ? new TestCaseEdit(input.getText(), expect.getText()) : null);
                
                editDialog.showAndWait().ifPresent(edited -> {
                    suite.getTestCases().asList().set(idx, new TestCase(edited.input, edited.expectedOutput));
                    updateListView(listView, suite);
                    logArea.appendText("Edited test case " + (idx+1) + " in " + selectedSuite + "\n");
                });
            }
        });

        // Delete Action
        deleteBtn.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                suite.getTestCases().asList().remove(idx);
                updateListView(listView, suite);
                logArea.appendText("Deleted test case " + (idx+1) + " from " + selectedSuite + "\n");
            }
        });
        
        // Delete All
        deleteAllBtn.setOnAction(e -> {
            suite.getTestCases().asList().clear();
            updateListView(listView, suite);
            logArea.appendText("Deleted all test cases from " + selectedSuite + "\n");
        });

        manageDialog.showAndWait();
    }

    private void updateListView(ListView<String> lv, TestSuit suite) {
        lv.getItems().clear();
        for (int i = 0; i < suite.getTestCases().size(); i++) {
            TestCase tc = suite.getTestCases().get(i);
            String display = (i + 1) + ". Input: " + tc.getInput().replace("\n", " ") 
                           + " => Expected: " + tc.getExpectedOutput().replace("\n", " ");
            lv.getItems().add(display);
        }
    }

    // --- RESTORED: Delete Suite ---
    private void onDeleteTestSuite() {
        String selected = suiteComboBox.getValue();
        if (selected == null) {
            showAlert("Select a test suite to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete suite '" + selected + "'?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                coordinator.getListOfTestSuites().deleteSuite(selected);
                refreshSuiteList();
                logArea.appendText("Deleted suite: " + selected + "\n");
            }
        });
    }

    // --- Execution Logic ---

    private void onChooseRootFolder(Stage stage) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Root Folder (Submission Folder)");
        File f = dc.showDialog(stage);
        if (f != null) {
            rootFolder = f;
            rootFolderLabel.setText(f.getAbsolutePath());
        }
    }

    private void onExecute() {
        if (rootFolder == null || suiteComboBox.getValue() == null) {
            showAlert("Please select a Root Folder and a Test Suite.");
            return;
        }
        logArea.setText("Executing...\n");
        new Thread(() -> {
            String res = coordinator.executeWithTestSuite(rootFolder, suiteComboBox.getValue());
            javafx.application.Platform.runLater(() -> logArea.setText(res));
        }).start();
    }
    
    private File chooseResultFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Result File");
        fc.setInitialDirectory(new File("test_results"));
        if (!fc.getInitialDirectory().exists()) fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        return fc.showOpenDialog(stage);
    }

    private void onViewResultFile(Stage stage) {
        File f = chooseResultFile(stage);
        if (f != null) {
            logArea.setText(coordinator.reloadResults(f.toPath()));
        }
    }

    private void onCompare() {
        if (comparisonFile1 == null || comparisonFile2 == null) {
            showAlert("Please select both files to compare.");
            return;
        }
        String res = coordinator.compareResultFiles(comparisonFile1.toPath(), comparisonFile2.toPath());
        logArea.setText(res);
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
    
    // Helper class for Editing
    private static class TestCaseEdit {
        String input, expectedOutput;
        TestCaseEdit(String i, String e) { input = i; expectedOutput = e; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}