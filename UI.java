/*********************************************************************************
 * UI
 * 
 * author: Farbod Mosalaei
 * Version: 2.0
 * initially created:  November 20, 2025
 * updated: December 4, 2025 - Test Suite Management and Execution
 ******************************************************************************/
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class UI extends Application {

    private final Coordinator coordinator = new Coordinator();

    // Root folder + log
    private Label rootFolderLabel;
    private File rootFolder;
    private TextArea logArea;
    private ComboBox<String> suiteComboBox;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Assignment Checker - Test Suite Manager");

        VBox mainPane = new VBox(15);
        mainPane.setPadding(new Insets(15));

        // ----- Header -----
        Label headerLabel = new Label("Assignment Checker - Test Suite Mode");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ----- Test Suite Management Section -----
        Button createTestSuiteButton = new Button("Create Test Suite");
        createTestSuiteButton.setOnAction(e -> onCreateTestSuite(primaryStage));

        Button addTestCaseButton = new Button("Add Test Case to Suite");
        addTestCaseButton.setOnAction(e -> onAddTestCaseToSuite(primaryStage));

        Button manageTestCasesButton = new Button("Manage Test Cases");
        manageTestCasesButton.setOnAction(e -> onManageTestCases(primaryStage));

        Button viewTestSuitesButton = new Button("View All Suites");
        viewTestSuitesButton.setOnAction(e -> onViewTestSuites());

        Button deleteTestSuiteButton = new Button("Delete Suite");
        deleteTestSuiteButton.setOnAction(e -> onDeleteTestSuite());

        HBox testSuiteBox = new HBox(10, createTestSuiteButton, addTestCaseButton, 
                                      manageTestCasesButton, viewTestSuitesButton, deleteTestSuiteButton);
        testSuiteBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Test Suite Selection and Execution -----
        Label selectSuiteLabel = new Label("Select Test Suite:");
        suiteComboBox = new ComboBox<>();
        suiteComboBox.setPrefWidth(300);

        Button refreshSuitesButton = new Button("Refresh");
        refreshSuitesButton.setOnAction(e -> refreshSuiteList());

        // Initialize combo box
        refreshSuiteList();

        HBox suiteSelectionBox = new HBox(10, selectSuiteLabel, suiteComboBox, refreshSuitesButton);
        suiteSelectionBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Root folder selection -----
        rootFolderLabel = new Label("No root folder selected.");
        Button chooseRootButton = new Button("Choose Root Folder");
        chooseRootButton.setOnAction(e -> onChooseRootFolder(primaryStage));

        HBox rootBox = new HBox(10, new Label("Root Folder:"), rootFolderLabel, chooseRootButton);
        rootBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Execute with Test Suite button -----
        Button executeWithSuiteButton = new Button("Run Test Suite");
        executeWithSuiteButton.setStyle("-fx-font-size: 16px; -fx-padding: 12px 30px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        executeWithSuiteButton.setOnAction(e -> onExecuteWithTestSuite());

        HBox executeBox = new HBox(executeWithSuiteButton);
        executeBox.setAlignment(Pos.CENTER);

        // ----- Execution log -----
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(25);
        logArea.setPromptText("Test suite execution log will appear here...");
        logArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");

        // ----- Layout -----
        mainPane.getChildren().addAll(
                headerLabel,
                new Separator(),
                new Label("Test Suite Management:"),
                testSuiteBox,
                new Separator(),
                new Label("Execute Programs with Test Suite:"),
                suiteSelectionBox,
                rootBox,
                executeBox,
                new Separator(),
                new Label("Execution Log:"),
                logArea
        );

        ScrollPane scrollPane = new ScrollPane(mainPane);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 1000, 750);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize with default suite
        coordinator.createEmptyTestSuite("DefaultSuite");
        refreshSuiteList();
    }

    // -------------------- Helper Methods --------------------

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

    // -------------------- Event Handlers --------------------

    private void onChooseRootFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Root Folder (program subfolders)");
        File folder = chooser.showDialog(stage);
        if (folder != null) {
            rootFolder = folder;
            rootFolderLabel.setText(folder.getAbsolutePath());
        }
    }

    private void onCreateTestSuite(Stage stage) {
        // Dialog to get test suite name
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create Test Suite");
        dialog.setHeaderText("Create a new test suite");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Assignment1Tests");

        grid.add(new Label("Test Suite Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Focus on name field
        javafx.application.Platform.runLater(() -> nameField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return nameField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String suiteName = result.get().trim();
            
            // Check if suite already exists
            if (coordinator.getAllTestSuiteNames().contains(suiteName)) {
                showAlert("A test suite with name '" + suiteName + "' already exists.");
                return;
            }

            coordinator.createEmptyTestSuite(suiteName);
            refreshSuiteList();
            suiteComboBox.setValue(suiteName);
            showAlert("Test suite '" + suiteName + "' created successfully!\nNow add test cases to it.");
            logArea.appendText("✓ Created test suite: " + suiteName + "\n");
        }
    }

    private void onAddTestCaseToSuite(Stage stage) {
        List<String> suiteNames = coordinator.getAllTestSuiteNames();

        if (suiteNames.isEmpty()) {
            showAlert("No test suites available. Please create a test suite first.");
            return;
        }

        // Dialog to add test case
        Dialog<TestCaseInput> dialog = new Dialog<>();
        dialog.setTitle("Add Test Case");
        dialog.setHeaderText("Add a new test case to a test suite");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType addMoreButtonType = new ButtonType("Add & Add Another", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, addMoreButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        ComboBox<String> suiteChoice = new ComboBox<>();
        suiteChoice.getItems().addAll(suiteNames);
        suiteChoice.setValue(suiteComboBox.getValue() != null ? suiteComboBox.getValue() : suiteNames.get(0));
        suiteChoice.setPrefWidth(300);

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Enter the input for the program\ne.g., 1 2 3\nor multiple lines:\n1\n2\n3");
        inputArea.setPrefRowCount(5);
        inputArea.setPrefColumnCount(40);

        TextArea expectedArea = new TextArea();
        expectedArea.setPromptText("Enter the expected output\ne.g., 6");
        expectedArea.setPrefRowCount(5);
        expectedArea.setPrefColumnCount(40);

        grid.add(new Label("Test Suite:"), 0, 0);
        grid.add(suiteChoice, 1, 0);
        grid.add(new Label("Input:"), 0, 1);
        grid.add(inputArea, 1, 1);
        grid.add(new Label("Expected Output:"), 0, 2);
        grid.add(expectedArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType || dialogButton == addMoreButtonType) {
                return new TestCaseInput(
                    suiteChoice.getValue(),
                    inputArea.getText(),
                    expectedArea.getText(),
                    dialogButton == addMoreButtonType
                );
            }
            return null;
        });

        boolean addMore = true;
        while (addMore) {
            Optional<TestCaseInput> result = dialog.showAndWait();
            
            if (result.isPresent()) {
                TestCaseInput input = result.get();
                
                if (input.input.trim().isEmpty() || input.expectedOutput.trim().isEmpty()) {
                    showAlert("Both input and expected output must be provided.");
                    if (!input.addMore) break;
                    continue;
                }

                coordinator.addTestCaseToSuite(input.suiteName, input.input, input.expectedOutput);
                logArea.appendText("✓ Added test case to suite: " + input.suiteName + "\n");
                
                if (input.addMore) {
                    // Clear fields for next test case
                    inputArea.clear();
                    expectedArea.clear();
                    showAlert("Test case added! Add another one.");
                } else {
                    showAlert("Test case added successfully to suite '" + input.suiteName + "'!");
                    addMore = false;
                }
            } else {
                addMore = false;
            }
        }
    }

    private void onManageTestCases(Stage stage) {
        List<String> suiteNames = coordinator.getAllTestSuiteNames();

        if (suiteNames.isEmpty()) {
            showAlert("No test suites available. Please create a test suite first.");
            return;
        }

        // Dialog to select test suite
        ChoiceDialog<String> suiteDialog = new ChoiceDialog<>(
            suiteComboBox.getValue() != null ? suiteComboBox.getValue() : suiteNames.get(0), 
            suiteNames
        );
        suiteDialog.setTitle("Manage Test Cases");
        suiteDialog.setHeaderText("Select a test suite to manage:");
        suiteDialog.setContentText("Test Suite:");

        Optional<String> suiteResult = suiteDialog.showAndWait();
        if (!suiteResult.isPresent()) {
            return;
        }

        String selectedSuite = suiteResult.get();
        TestSuit suite = coordinator.getListOfTestSuites().getSuite(selectedSuite);

        if (suite == null) {
            showAlert("Test suite not found.");
            return;
        }

        if (suite.getTestCases().size() == 0) {
            showAlert("This test suite has no test cases yet.\nUse 'Add Test Case to Suite' to add some.");
            return;
        }

        // Create test case management dialog
        Dialog<Void> manageDialog = new Dialog<>();
        manageDialog.setTitle("Manage Test Cases - " + selectedSuite);
        manageDialog.setHeaderText("Select test cases to view, edit, or delete");

        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        manageDialog.getDialogPane().getButtonTypes().add(closeButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // List view with test cases
        ListView<String> testCaseListView = new ListView<>();
        testCaseListView.setPrefHeight(300);
        testCaseListView.setPrefWidth(600);

        // Populate list view
        for (int i = 0; i < suite.getTestCases().size(); i++) {
            TestCase tc = suite.getTestCases().get(i);
            String display = String.format("%d. Input: %s => Expected: %s", 
                i + 1, 
                tc.getInput().replace("\n", "\\n").substring(0, Math.min(50, tc.getInput().length())),
                tc.getExpectedOutput().replace("\n", "\\n").substring(0, Math.min(50, tc.getExpectedOutput().length()))
            );
            testCaseListView.getItems().add(display);
        }

        // Buttons for actions
        Button viewButton = new Button("View Details");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button deleteAllButton = new Button("Delete All");
        deleteAllButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, viewButton, editButton, deleteButton, deleteAllButton);
        buttonBox.setAlignment(Pos.CENTER);

        content.getChildren().addAll(
            new Label("Test Cases in suite '" + selectedSuite + "':"),
            testCaseListView,
            buttonBox
        );

        // View button action
        viewButton.setOnAction(e -> {
            int selected = testCaseListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TestCase tc = suite.getTestCases().get(selected);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Test Case Details");
                alert.setHeaderText("Test Case #" + (selected + 1));
                
                TextArea detailArea = new TextArea();
                detailArea.setEditable(false);
                detailArea.setWrapText(true);
                detailArea.setText("Input:\n" + tc.getInput() + "\n\nExpected Output:\n" + tc.getExpectedOutput());
                detailArea.setPrefRowCount(10);
                detailArea.setPrefColumnCount(50);
                
                alert.getDialogPane().setContent(detailArea);
                alert.showAndWait();
            } else {
                showAlert("Please select a test case to view.");
            }
        });

        // Edit button action
        editButton.setOnAction(e -> {
            int selected = testCaseListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TestCase tc = suite.getTestCases().get(selected);
                
                // Edit dialog
                Dialog<TestCaseEdit> editDialog = new Dialog<>();
                editDialog.setTitle("Edit Test Case");
                editDialog.setHeaderText("Edit Test Case #" + (selected + 1));

                ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                editDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 10, 10, 10));

                TextArea inputArea = new TextArea(tc.getInput());
                inputArea.setPrefRowCount(5);
                inputArea.setPrefColumnCount(40);

                TextArea expectedArea = new TextArea(tc.getExpectedOutput());
                expectedArea.setPrefRowCount(5);
                expectedArea.setPrefColumnCount(40);

                grid.add(new Label("Input:"), 0, 0);
                grid.add(inputArea, 1, 0);
                grid.add(new Label("Expected Output:"), 0, 1);
                grid.add(expectedArea, 1, 1);

                editDialog.getDialogPane().setContent(grid);

                editDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        return new TestCaseEdit(inputArea.getText(), expectedArea.getText());
                    }
                    return null;
                });

                Optional<TestCaseEdit> editResult = editDialog.showAndWait();
                if (editResult.isPresent()) {
                    TestCaseEdit edited = editResult.get();
                    
                    // Remove old and add new (since TestCase fields are final)
                    suite.getTestCases().asList().remove(selected);
                    suite.getTestCases().asList().add(selected, new TestCase(edited.input, edited.expectedOutput));
                    
                    // Update list view
                    String display = String.format("%d. Input: %s => Expected: %s", 
                        selected + 1, 
                        edited.input.replace("\n", "\\n").substring(0, Math.min(50, edited.input.length())),
                        edited.expectedOutput.replace("\n", "\\n").substring(0, Math.min(50, edited.expectedOutput.length()))
                    );
                    testCaseListView.getItems().set(selected, display);
                    
                    showAlert("Test case updated successfully!");
                    logArea.appendText("✓ Edited test case #" + (selected + 1) + " in suite: " + selectedSuite + "\n");
                }
            } else {
                showAlert("Please select a test case to edit.");
            }
        });

        // Delete button action
        deleteButton.setOnAction(e -> {
            int selected = testCaseListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Delete");
                confirm.setHeaderText("Delete Test Case #" + (selected + 1) + "?");
                confirm.setContentText("This action cannot be undone.");

                Optional<ButtonType> confirmResult = confirm.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    suite.getTestCases().asList().remove(selected);
                    testCaseListView.getItems().remove(selected);
                    
                    // Update numbering
                    for (int i = 0; i < suite.getTestCases().size(); i++) {
                        TestCase tc = suite.getTestCases().get(i);
                        String display = String.format("%d. Input: %s => Expected: %s", 
                            i + 1, 
                            tc.getInput().replace("\n", "\\n").substring(0, Math.min(50, tc.getInput().length())),
                            tc.getExpectedOutput().replace("\n", "\\n").substring(0, Math.min(50, tc.getExpectedOutput().length()))
                        );
                        testCaseListView.getItems().set(i, display);
                    }
                    
                    showAlert("Test case deleted successfully!");
                    logArea.appendText("✗ Deleted test case from suite: " + selectedSuite + "\n");
                }
            } else {
                showAlert("Please select a test case to delete.");
            }
        });

        // Delete all button action
        deleteAllButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete All");
            confirm.setHeaderText("Delete ALL test cases from '" + selectedSuite + "'?");
            confirm.setContentText("This will remove all " + suite.getTestCases().size() + " test cases. This action cannot be undone.");

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                suite.getTestCases().asList().clear();
                testCaseListView.getItems().clear();
                showAlert("All test cases deleted from suite '" + selectedSuite + "'!");
                logArea.appendText("✗ Deleted all test cases from suite: " + selectedSuite + "\n");
            }
        });

        manageDialog.getDialogPane().setContent(content);
        manageDialog.getDialogPane().setPrefSize(700, 500);
        manageDialog.showAndWait();
    }

    private void onViewTestSuites() {
        List<String> suiteNames = coordinator.getAllTestSuiteNames();

        if (suiteNames.isEmpty()) {
            showAlert("No test suites available.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════════════\n");
        sb.append("                        ALL TEST SUITES                           \n");
        sb.append("═══════════════════════════════════════════════════════════════════\n\n");

        for (String suiteName : suiteNames) {
            TestSuit suite = coordinator.getListOfTestSuites().getSuite(suiteName);
            if (suite != null) {
                sb.append(suite.toDetailedString());
                sb.append("\nTest Cases:\n");
                sb.append("───────────────────────────────────────────────────────────────────\n");

                if (suite.getTestCases().size() == 0) {
                    sb.append("  (No test cases yet)\n");
                } else {
                    for (int i = 0; i < suite.getTestCases().size(); i++) {
                        TestCase tc = suite.getTestCases().get(i);
                        sb.append(String.format("%3d. ", i + 1));
                        sb.append(tc.toString()).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Suites");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefRowCount(28);
        textArea.setPrefColumnCount(80);
        textArea.setStyle("-fx-font-family: 'Courier New', monospace;");

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(900, 650);
        alert.showAndWait();
    }

    private void onDeleteTestSuite() {
        List<String> suiteNames = coordinator.getAllTestSuiteNames();

        if (suiteNames.isEmpty()) {
            showAlert("No test suites available to delete.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(suiteNames.get(0), suiteNames);
        dialog.setTitle("Delete Test Suite");
        dialog.setHeaderText("Select a test suite to delete:");
        dialog.setContentText("Test Suite:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Are you sure you want to delete test suite:");
            confirm.setContentText(result.get());

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                coordinator.getListOfTestSuites().deleteSuite(result.get());
                refreshSuiteList();
                showAlert("Test suite '" + result.get() + "' deleted successfully.");
                logArea.appendText("✗ Deleted test suite: " + result.get() + "\n");
            }
        }
    }

    private void onExecuteWithTestSuite() {
        logArea.clear();

        String selectedSuite = suiteComboBox.getValue();
        if (selectedSuite == null || selectedSuite.isEmpty()) {
            showAlert("Please select a test suite.");
            return;
        }

        if (rootFolder == null) {
            showAlert("Please select a root folder containing program submissions.");
            return;
        }

        logArea.appendText("Starting execution...\n\n");
        String result = coordinator.executeWithTestSuite(rootFolder, selectedSuite);
        logArea.setText(result);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Helper class to store test case input
    private static class TestCaseInput {
        String suiteName;
        String input;
        String expectedOutput;
        boolean addMore;

        TestCaseInput(String suiteName, String input, String expectedOutput, boolean addMore) {
            this.suiteName = suiteName;
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.addMore = addMore;
        }
    }

    // Helper class for editing test cases
    private static class TestCaseEdit {
        String input;
        String expectedOutput;

        TestCaseEdit(String input, String expectedOutput) {
            this.input = input;
            this.expectedOutput = expectedOutput;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}