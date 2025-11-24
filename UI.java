/*********************************************************************************
 * UI
 * 
 * author: Farbod Mosalaei
 * Version: 1.0
 * insitally created:  Novemeber 20, 2025
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

public class UI extends Application {

    private final Coordinator coordinator = new Coordinator();

    // Input / expected files
    private Label inputFileLabel;
    private Label expectedFileLabel;
    private String inputText = null;
    private String expectedOutputText = null;

    // Root folder + log
    private Label rootFolderLabel;
    private File rootFolder;
    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Assignment Checker");

        // ----- Input file selection -----
        inputFileLabel = new Label("No input file selected.");
        Button loadInputButton = new Button("Load Input File");
        loadInputButton.setOnAction(e -> onLoadInputFile(primaryStage));

        HBox inputBox = new HBox(10, new Label("Input File:"), inputFileLabel, loadInputButton);
        inputBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Expected output file -----
        expectedFileLabel = new Label("No expected output file selected.");
        Button loadExpectedButton = new Button("Load Expected Output File");
        loadExpectedButton.setOnAction(e -> onLoadExpectedFile(primaryStage));

        HBox expectedBox = new HBox(10, new Label("Expected Output File:"), expectedFileLabel, loadExpectedButton);
        expectedBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Root folder selection -----
        rootFolderLabel = new Label("No root folder selected.");
        Button chooseRootButton = new Button("Choose Root Folder");
        chooseRootButton.setOnAction(e -> onChooseRootFolder(primaryStage));

        HBox rootBox = new HBox(10, new Label("Root Folder:"), rootFolderLabel, chooseRootButton);
        rootBox.setAlignment(Pos.CENTER_LEFT);

        // ----- Execution log -----
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(20);
        logArea.setPromptText("Execution log will appear here.");

        // ----- Execute button -----
        Button executeButton = new Button("Execute");
        executeButton.setOnAction(e -> onExecute());

        // ----- Layout -----
        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        root.getChildren().addAll(
                new Label("Load Files:"),
                inputBox,
                expectedBox,
                new Separator(),

                new Label("Select Root Folder:"),
                rootBox,
                executeButton,
                new Separator(),

                new Label("Execution Log:"),
                logArea
        );

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        coordinator.createEmptyTestSuite("DefaultSuite");
    }

    // -------------------- Event Handlers --------------------

    private void onLoadInputFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select input.txt");
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            inputText = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            inputFileLabel.setText(file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Error reading input file: " + e.getMessage());
        }
    }

    private void onLoadExpectedFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select expected_output.txt");
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            expectedOutputText = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            expectedFileLabel.setText(file.getAbsolutePath());
        } catch (Exception e) {
            showAlert("Error reading expected output file: " + e.getMessage());
        }
    }

    private void onChooseRootFolder(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Root Folder (program subfolders)");
        File folder = chooser.showDialog(stage);
        if (folder != null) {
            rootFolder = folder;
            rootFolderLabel.setText(folder.getAbsolutePath());
        }
    }

    private void onExecute() {
        logArea.clear();

        if (rootFolder == null) {
            showAlert("Please choose a root folder.");
            return;
        }
        if (inputText == null) {
            showAlert("Please load an input file first.");
            return;
        }
        if (expectedOutputText == null) {
            showAlert("Please load the expected output file first.");
            return;
        }

        String result = coordinator.executeInputExpected(rootFolder, inputText, expectedOutputText);
        logArea.setText(result);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
