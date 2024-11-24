package group15;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUIMill.fxml"));
        Parent root = loader.load();

        // Get controller and set the primary stage reference
        Controller controller = loader.getController();
        controller.setPrimaryStage(primaryStage);

        primaryStage.setTitle("KEN15 Project 2-1");
        Scene scene = new Scene(root, 1920, 1080); // Adjust the dimensions if needed
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}