package app;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Crc extends Application {
    
    @Override
    public void start(Stage primaryStage) throws IOException {
        
        Parent root = FXMLLoader.load(getClass().getResource("app.fxml"));
        
        Scene scene = new Scene(root, 640, 480);
        
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setTitle("CRC i Hamming");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
