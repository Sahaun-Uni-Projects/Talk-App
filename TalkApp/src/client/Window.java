package client;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import user.User;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class Window {
    private Client client;
    private Stage window;
    private FXMLController controller;
    
    public Window(Client client) {
        this.client = client;
        this.window = new Stage();
        this.controller = null;
    }
    
    public Window(Client client, String fxml) {
        this(client);
        loadFXML(fxml);
    }
    
    public Window(Client client, WINDOW winType) {
        this(client);
        load(winType);
    }
    
    public Window load(WINDOW winType) {
        String fxml = "";
        switch (winType) {
            case LOGIN   : fxml = "WinLogin.fxml";   break;
            case MAIN    : fxml = "WinMain.fxml";    break;
            case SIGNUP  : fxml = "WinSignup.fxml";  break;
            case PROFILE : fxml = "WinProfile.fxml"; break;
            case FRIENDS : fxml = "WinFriends.fxml"; break;
            default: break;
        }
        return loadFXML(fxml);
    }
    
    public Window loadFXML(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("windows/"+fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            setScene(scene);
            show();
            //center();
            
            this.window.setTitle("Talk App");
            this.window.getIcons().add(new Image(("client/res/sys_images/icon.png")));
           
            User user = this.client.getUser();
            if (user != null) this.client.setUser(user.getUsername());

            this.controller = loader.getController();
            this.controller.initialize(this.client);
        } catch (Exception ex) {
            Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }
    
    public Window center() {
        this.window.centerOnScreen();
        return this;
    }
    
    public Window close() {
        this.window.close();
        return this;
    }
    
    public Window show() {
            this.window.show();
        return this;
    }
    
    public Window setScene(Scene scene) {
        this.window.setScene(scene);
        return this;
    }
    
    public FXMLController getController() {
        return this.controller;
    }
    
    public Stage getStage() {
        return this.window;
    }
}
