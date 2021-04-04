package client.windows;

import client.FXMLController;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import user.User;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class WinFriendsController extends FXMLController {
    @FXML VBox vbFriends;
    @FXML VBox vbRequests;
    @FXML TextField fieldAdd;
    @FXML Label labelAddMessage;
    @FXML Button btnAdd;
    @FXML Button btnBack;
    
    @FXML private void btnAddAction(ActionEvent event) {
        boolean green = true;
        
        User user = this.client.getUser();
        String friendName = fieldAdd.getText();
        
        if (friendName.isEmpty()) return;
        
        if (friendName.equals(user.getUsername())) {
            labelAddMessage.setText("You are your friend irl!");
        } else if (user.getFriendsList().contains(friendName)) {
            labelAddMessage.setText("User is your friend already!");
        } else if (this.client.addUser(friendName)) {
            labelAddMessage.setText("User added successfully!");
        } else {
            green = false;
            labelAddMessage.setText("User does not exist!");
        }
        
        labelAddMessage.setTextFill(Color.web(green ? "#77ff99" : "#ff3434"));
        labelAddMessage.setVisible(true);
    }
    
    @FXML private void btnBackAction(ActionEvent event) {
        this.client.getWindow().load(WINDOW.MAIN);
    }
    
    @Override
    public void init() {
        User user = this.client.getUser();
        ArrayList<String> list;
        
        // Hide
        labelAddMessage.setVisible(false);
        
        // Add listerners
        fieldAdd.textProperty().addListener((observable, oldValue, newValue) -> {
            labelAddMessage.setVisible(false);
        });
        
        // Load Friends
        list = user.getFriendsList();
        for (String username : list) {
            loadFriend(this.client.getUserByName(username));
        }
        
        // Load Requests
        list = user.getRequestsList();
        for (String username : list) {
            loadRequest(this.client.getUserByName(username));
        }
    }
   
    private void loadFriend(User user) {
        // Picture
        ImageView pfp = new ImageView(); 
        pfp.setImage(getImage(user.getPicture()));
        pfp.setPreserveRatio(true);
        pfp.setFitWidth(65);
        pfp.setFitHeight(65);
        
        // Username
        Label name = new Label();
        name.setLineSpacing(0);
        name.setText(user.getUsername() + " (" + user.getName() + ")");
        name.setFont(new Font("System", 15));
        name.setPrefSize(270, 20);
        name.setTextFill(Color.web("#eeeeee"));
        
        // Button
        Button unfriend = new Button("Unfriend");
        unfriend.setPrefSize(85, 35);
        
        // Container
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setBackground(Background.EMPTY);
        container.setCursor(Cursor.HAND);
        
        container.getChildren().add(pfp); container.setMargin(pfp, new Insets(10,0,10,10));
        container.getChildren().add(name); container.setMargin(name, new Insets(0,0,0,10));
        container.getChildren().add(unfriend); container.setMargin(unfriend, new Insets(0,0,0,0));
        
        // Mouse events
        container.setOnMouseClicked((e)->{
            this.client.openConversation(user.getUsername());
        });
        
        container.setOnMouseEntered((e)->{
            container.setBackground(new Background(new BackgroundFill(Color.web("#2f3136"), CornerRadii.EMPTY, new Insets(0,-25,0,-25))));
        });
        
        container.setOnMouseExited((e)->{
            container.setBackground(Background.EMPTY);
        });
        
        // Separator
        Separator sep = new Separator();
        
        // Finally, add em to the VBox
        vbFriends.getChildren().add(container);
        vbFriends.getChildren().add(sep);
        
        // Button actions
        unfriend.setOnAction((e)->{
            this.client.unfriendUser(user.getUsername());
            this.client.getWindow().load(WINDOW.FRIENDS);
        });
    }
    
    private void loadRequest(User user) {
        // Picture
        ImageView pfp = new ImageView(); 
        pfp.setImage(getImage(user.getPicture()));
        pfp.setPreserveRatio(true);
        pfp.setFitWidth(65);
        pfp.setFitHeight(65);
        
        // Username
        Label name = new Label();
        name.setLineSpacing(0);
        name.setText(user.getUsername() + " (" + user.getName() + ")");
        name.setFont(new Font("System", 15));
        name.setPrefSize(235, 20);
        name.setTextFill(Color.web("#eeeeee"));
        
        // Buttons
        Button accept = new Button("Accept");
        accept.setPrefSize(60, 35);
        
        Button decline = new Button("Decline");
        decline.setPrefSize(60, 35);
        
        // Container
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setCursor(Cursor.HAND);
        
        container.getChildren().add(pfp); container.setMargin(pfp, new Insets(10,0,10,10));
        container.getChildren().add(name); container.setMargin(name, new Insets(0,0,0,10));
        container.getChildren().add(accept); container.setMargin(accept, new Insets(0,0,0,0));
        container.getChildren().add(decline); container.setMargin(decline, new Insets(0,0,0,0));
        
        // Separator
        Separator sep = new Separator();
        
        // Finally, add em to the VBox
        vbRequests.getChildren().add(container);
        vbRequests.getChildren().add(sep);
        
        // Button actions
        accept.setOnAction((e)->{
            this.client.requestAccept(user.getUsername());
            this.client.getWindow().load(WINDOW.FRIENDS);
        });
        decline.setOnAction((e)->{
            this.client.requestDecline(user.getUsername());
            this.client.getWindow().load(WINDOW.FRIENDS);
        });
    }
    
    private Image getImage(String name) {
        try {
            Image img = new Image("client/res/user_images/" + name);
            return img;
        } catch (Exception e) {
            System.out.println("Image not found");
        }
        return null;
    }
}