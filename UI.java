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
import java.io.IOException;
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
        Label headerLabel = new Label("Assignment Checker - Test Suite Manager");
        headerLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

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

        Button importSuitesButton = new Button("Import Suites from Files");
        importSuitesButton.setOnAction(e -> onImportSuitesFromFiles(primaryStage));

        HBox testSuiteBox = new HBox(10, createTestSuiteButton, addTestCaseButton,
                                     manageTestCasesButton, viewTestSuitesButton, deleteTestSuiteButton, importSuitesButton);
        testSuiteBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Test Suite Selection and Execution -----
        Label selectSuiteLabel = new Label("Select Test Suite:");
        suiteComboBox = new ComboBox<>();
        suiteComboBox.setPrefWidth(250);

        HBox suiteSelectionBox = new HBox(10, selectSuiteLabel, suiteComboBox);
        suiteSelectionBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Root Folder selection -----
        rootFolderLabel = new Label("No root folder selected.");
        Button chooseRootButton = new Button("Choose Root Folder");
        chooseRootButton.setOnAction(e -> onChooseRootFolder(primaryStage));

        HBox rootBox = new HBox(10, new Label("Root Folder:"), rootFolderLabel, chooseRootButton);
        rootBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Execute with Test Suite button -----
        Button executeWithSuiteButton = new Button("Run Test Suite");
        executeWithSuiteButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 30px; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        executeWithSuiteButton.setOnAction(e -> onExecuteWithTestSuite());

        HBox executeBox = new HBox(executeWithSuiteButton);
        executeBox.setAlignment(Pos.CENTER);

        // ----- Execution log -----
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(25);
        logArea.setPromptText("Test suite execution log will appear here...");
        logArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");

        // Wrap in scroll pane
        ScrollPane logScroll = new ScrollPane(logArea);
        logScroll.setFitToWidth(true);
        logScroll.setFitToHeight(true);

        VBox centerBox = new VBox(10,
                headerLabel,
                new Separator(),
                new Label("Test Suite Management:"),
                testSuiteBox,
                new Separator(),
                new Label("Execute Programs with Test Suite:"),
                suiteSelectionBox,
                rootBox,
                executeBox,
                new Label("Execution Log:"),
                logScroll
        );

        mainPane.getChildren().add(centerBox);

        Scene scene = new Scene(mainPane, 1000, 750);
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Assignment Checker");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        dialog.setHeaderText("Create a new Test Suite");

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Suite Name");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Suite Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                return nameField.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.isEmpty()) {
                showAlert("Suite name cannot be empty.");
            } else {
                coordinator.createEmptyTestSuite(name);
                refreshSuiteList();
                showAlert("Test suite '" + name + "' created successfully!\nNow add test cases to it.");
                logArea.appendText("✓ Created test suite: " + name + "\n");
            }
        });
    }

    private static class TestCaseInput {
        final String suiteName;
        final String input;
        final String expectedOutput;
        final boolean addMore;

        TestCaseInput(String suiteName, String input, String expectedOutput, boolean addMore) {
            this.suiteName = suiteName;
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.addMore = addMore;
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

        ComboBox<String> suiteCombo = new ComboBox<>();
        suiteCombo.getItems().addAll(suiteNames);
        if (!suiteNames.isEmpty()) {
            suiteCombo.setValue(suiteNames.get(0));
        }

        TextArea inputArea = new TextArea();
        inputArea.setPromptText("Program input here...");
        inputArea.setPrefRowCount(3);

        TextArea expectedArea = new TextArea();
        expectedArea.setPromptText("Expected output here...");
        expectedArea.setPrefRowCount(3);

        grid.add(new Label("Test Suite:"), 0, 0);
        grid.add(suiteCombo, 1, 0);
        grid.add(new Label("Input:"), 0, 1);
        grid.add(inputArea, 1, 1);
        grid.add(new Label("Expected Output:"), 0, 2);
        grid.add(expectedArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType || dialogButton == addMoreButtonType) {
                String suiteName = suiteCombo.getValue();
                String input = inputArea.getText().trim();
                String expected = expectedArea.getText().trim();

                if (suiteName == null || suiteName.isEmpty()) {
                    showAlert("Please select a test suite.");
                    return null;
                }
                if (input.isEmpty() || expected.isEmpty()) {
                    showAlert("Input and Expected Output cannot be empty.");
                    return null;
                }
                return new TestCaseInput(suiteName, input, expected, dialogButton == addMoreButtonType);
            }
            return null;
        });

        boolean addMore = true;

        while (addMore) {
            Optional<TestCaseInput> result = dialog.showAndWait();
            if (result.isPresent()) {
                TestCaseInput input = result.get();
                if (input == null) {
                    // Invalid input (already handled by showAlert)
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

        ChoiceDialog<String> suiteDialog = new ChoiceDialog<>(suiteNames.get(0), suiteNames);
        suiteDialog.setTitle("Manage Test Cases");
        suiteDialog.setHeaderText("Select a test suite to manage");
        suiteDialog.setContentText("Test Suite:");

        Optional<String> suiteResult = suiteDialog.showAndWait();
        if (!suiteResult.isPresent()) {
            return;
        }

        String suiteName = suiteResult.get();
        TestSuit suite = coordinator.getListOfTestSuites().getSuite(suiteName);

        if (suite == null) {
            showAlert("Selected suite not found.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Test Cases - " + suiteName);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

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

        HBox buttonsBox = new HBox(10, viewButton, editButton, deleteButton);
        buttonsBox.setAlignment(Pos.CENTER);

        VBox contentBox = new VBox(10, new Label("Test Cases:"), testCaseListView, buttonsBox);
        contentBox.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contentBox);

        // View button action
        viewButton.setOnAction(e -> {
            int selected = testCaseListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                TestCase tc = suite.getTestCases().get(selected);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Test Case Details");
                alert.setHeaderText("Test Case #" + (selected + 1));

                TextArea detailArea = new TextArea(tc.toDetailedString());
                detailArea.setEditable(false);
                detailArea.setWrapText(true);
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

                Dialog<TestCase> editDialog = new Dialog<>();
                editDialog.setTitle("Edit Test Case");
                editDialog.setHeaderText("Edit Test Case #" + (selected + 1));
                editDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);

                TextArea inputArea = new TextArea(tc.getInput());
                inputArea.setPrefRowCount(3);

                TextArea expectedArea = new TextArea(tc.getExpectedOutput());
                expectedArea.setPrefRowCount(3);

                grid.add(new Label("Input:"), 0, 0);
                grid.add(inputArea, 1, 0);
                grid.add(new Label("Expected Output:"), 0, 1);
                grid.add(expectedArea, 1, 1);

                editDialog.getDialogPane().setContent(grid);

                editDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.OK) {
                        String newInput = inputArea.getText().trim();
                        String newExpected = expectedArea.getText().trim();
                        if (newInput.isEmpty() || newExpected.isEmpty()) {
                            showAlert("Input and Expected Output cannot be empty.");
                            return null;
                        }
                        return new TestCase(newInput, newExpected);
                    }
                    return null;
                });

                Optional<TestCase> editResult = editDialog.showAndWait();
                editResult.ifPresent(newTc -> {
                    suite.getTestCases().asList().set(selected, newTc);
                    logArea.appendText("✓ Edited test case #" + (selected + 1) + " in suite: " + suiteName + "\n");

                    // Update display
                    String display = String.format("%d. Input: %s => Expected: %s",
                            selected + 1,
                            newTc.getInput().replace("\n", "\\n").substring(0, Math.min(50, newTc.getInput().length())),
                            newTc.getExpectedOutput().replace("\n", "\\n").substring(0, Math.min(50, newTc.getExpectedOutput().length()))
                    );
                    testCaseListView.getItems().set(selected, display);
                });
            } else {
                showAlert("Please select a test case to edit.");
            }
        });

        // Delete button action
        deleteButton.setOnAction(e -> {
            int selected = testCaseListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Test Case");
                confirm.setHeaderText("Are you sure you want to delete this test case?");
                confirm.setContentText("This action cannot be undone.");

                Optional<ButtonType> confirmResult = confirm.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    suite.getTestCases().asList().remove(selected);
                    testCaseListView.getItems().remove(selected);
                    logArea.appendText("✗ Deleted test case #" + (selected + 1) + " from suite: " + suiteName + "\n");
                }
            } else {
                showAlert("Please select a test case to delete.");
            }
        });

        dialog.showAndWait();
    }

    private void onViewTestSuites() {
        ListOfTestSuites suites = coordinator.getListOfTestSuites();
        if (suites.getSuiteNames().isEmpty()) {
            showAlert("No test suites available.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available Test Suites:\n\n");
        for (String name : suites.getSuiteNames()) {
            TestSuit suite = suites.getSuite(name);
            sb.append("- ").append(name)
              .append(" (").append(suite.getTestCases().size()).append(" test cases)\n");
        }

        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(15);
        area.setPrefColumnCount(50);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test Suites");
        alert.setHeaderText("All Test Suites");
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    private void onDeleteTestSuite() {
        List<String> suiteNames = coordinator.getAllTestSuiteNames();
        if (suiteNames.isEmpty()) {
            showAlert("No test suites available.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(suiteNames.get(0), suiteNames);
        dialog.setTitle("Delete Test Suite");
        dialog.setHeaderText("Select a test suite to delete");
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

    /**
     * Import one or more test suite definition files and turn each into a suite.
     * Each non-empty, non-# line of the file must have the form:
     *     input ==> expectedOutput
     */
    private void onImportSuitesFromFiles(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Test Suite File(s)");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.in")
        );

        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null || files.isEmpty()) {
            return;
        }

        int imported = 0;

        for (File file : files) {
            try {
                String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                // Derive suite name from file name (without extension)
                String fileName = file.getName();
                String suiteName;
                int dot = fileName.lastIndexOf('.');
                if (dot > 0) {
                    suiteName = fileName.substring(0, dot);
                } else {
                    suiteName = fileName;
                }

                coordinator.loadTestCasesFromText(suiteName, text);
                imported++;
                if (logArea != null) {
                    logArea.appendText("✓ Loaded test suite '" + suiteName + "' from file: "
                                       + file.getAbsolutePath() + "\n");
                }
            } catch (IOException ex) {
                if (logArea != null) {
                    logArea.appendText("⚠ Failed to read file: " + file.getAbsolutePath()
                                       + " (" + ex.getMessage() + ")\n");
                }
            }
        }

        refreshSuiteList();

        if (imported > 0) {
            showAlert("Successfully imported " + imported + " test suite file(s).");
        } else {
            showAlert("No test suites were imported.");
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

    public static void main(String[] args) {
        launch(args);
    }
}
