
import controller.Controller;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ui/mainui.fxml"));
        Parent root = loader.load();
        //
        Controller controller = loader.getController();
        //
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("ViBlockPDF");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            controller.saveOnClose();
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
