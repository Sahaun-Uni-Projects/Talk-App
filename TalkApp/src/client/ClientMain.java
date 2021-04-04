package client;

import javafx.application.Application;
import javafx.stage.Stage;
import utils.MACRO;

/**
 *
 * @author Sahaun
 */
public class ClientMain extends Application {
    private Client client = new Client(MACRO.SERVER_NAME, MACRO.SERVER_PORT);
    
    @Override
    public void start(Stage stage) throws Exception {}

    public static void main(String[] args) {
        launch(args);
    }
}
