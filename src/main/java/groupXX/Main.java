package groupXX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The Main class is the entry point for launching the application.
 * It extends the JavaFX Application class and sets up the primary stage and scene.
 */
public class Main extends Application {

    /**
     * Initializes and shows the primary stage with the loaded FXML UI.
     * 
     * @param primaryStage The main window (stage) for the application.
     * @throws Exception If there is an error loading the FXML file.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUIMill.fxml"));
        Parent root = loader.load();

        // Get controller and set the primary stage reference
        Controller controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("Project 2-1");
        Scene scene = new Scene(root, 1920, 1080); // Adjust the dimensions if needed
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Main method that launches the JavaFX application.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}