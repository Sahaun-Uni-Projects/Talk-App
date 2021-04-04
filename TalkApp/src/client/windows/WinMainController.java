package client.windows;

import client.FXMLController;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import message.Message;
import org.json.simple.JSONObject;
import user.User;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class WinMainController extends FXMLController {
    private String currentConversation = "";
    
    @FXML private Text txtToUserFullName;
    @FXML private Text txtToUsername;
    @FXML private Text txtUsername;
    @FXML private ImageView imgUserDP;
    @FXML private VBox vbConversation;
    @FXML private ScrollPane spConversation;
    @FXML private VBox vbUserList;
    @FXML private VBox vbChat;
    @FXML private HBox hbToUser;
    @FXML private TextArea taMessage;
    @FXML private Button btnProfile;
    @FXML private Button btnMessage;
    @FXML private Button btnFriends;
    @FXML private Button btnLogout;
    @FXML private Button btnSend;
    
    @FXML private void btnProfileAction(ActionEvent event) {
        this.client.getWindow().load(WINDOW.PROFILE);
    }
    
    @FXML private void btnMessageAction(ActionEvent event) {
        this.client.logout();
    }
    
    @FXML private void btnFriendsAction(ActionEvent event) {
        this.client.getWindow().load(WINDOW.FRIENDS);
    }
    
    @FXML private void btnLogoutAction(ActionEvent event) {
        this.client.logout();
    }
    
    @FXML private void btnSendAction(ActionEvent event) {
        String body = taMessage.getText();
        taMessage.clear();
        if (body.isEmpty()) return;
        this.client.sendMessage(this.currentConversation, body);
        init();
    }
    
    @Override
    public void init() {
        vbChat.setVisible(false);
        
        this.client.updateUser(this.client.getUser());
        vbUserList.getChildren().clear();
        
        User user = this.client.getUser();
        imgUserDP.setImage(getImage(user.getPicture()));
        txtUsername.setText(user.getUsername());
        
        ArrayList<String> friendsList = new ArrayList<>();
        Set keys = user.getLastSeen().keySet();
        for (Object key : keys) {
            String conversation = (String) key;
            ArrayList<String> list = Message.getConversationUsers(conversation);
            String username = "";
            for (String str : list) {
                if (!str.equals(user.getUsername())) username = str;
            }
            friendsList.add(username);
        }
        
        Collections.sort(friendsList);
        
        for (String name : friendsList) {
            User friend = this.client.getUserByName(name);
            if (friend != null) {
                String conversation = Message.getConversationId(user.getUsername(), friend.getUsername());
                boolean unread = this.client.conversationIsUnread(conversation);
                if (this.currentConversation.equals(conversation)) unread = false;
                loadUserPreview(friend, unread);
            }
        }
        
        // Auto scroll down scroll pane when vbox height changes
        vbConversation.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                spConversation.setVvalue((Double)newValue);  
            }
        });
        
        try {
            this.client.send("getOnlineUsers");
        } catch (IOException ex) {
            Logger.getLogger(WinMainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Open current conversation
        if (this.currentConversation != "") openConversation(this.currentConversation);
    }
    
    private void loadUserPreview(User user, boolean unread) {
        ArrayList<String> list = Message.getConversationUsers(this.currentConversation);
        boolean current = false;
        for (String str : list) {
            if (str.equals(user.getUsername())) current = true;
        }
        
        // Picture
        ImageView pfp = new ImageView(); 
        pfp.setImage(getImage(user.getPicture()));
        pfp.setPreserveRatio(true);
        pfp.setFitWidth(65);
        pfp.setFitHeight(65);
        
        // Username
        Text name = new Text();
        name.setLineSpacing(0);
        //name.setText(user.getName() + (unread ? "*" : ""));
        name.setText(user.getUsername());
        name.setFont(new Font("System", 15));
        name.setWrappingWidth(170);
        name.setFill(Color.web(unread ? "#22ff22" : "#eeeeee"));
        
        // Active status
        Circle status = new Circle();
        status.setRadius(4);
        status.setFill(Color.web("#ff0000"));
        
        // Container
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setCursor(Cursor.HAND);
        container.setPrefSize(250, 70);
        container.setBackground(new Background(new BackgroundFill(Color.web(current ? "#36393f" : "#2f3136"), CornerRadii.EMPTY, new Insets(0,-20,0,-20))));
        
        container.getChildren().add(pfp);
        container.getChildren().add(name); container.setMargin(name, new Insets(0,0,0,10));
        container.getChildren().add(status); container.setMargin(status, new Insets(0,0,0,5));
        
        // Mouse events
        container.setOnMouseClicked((e)->{
            if (e.getButton() == MouseButton.SECONDARY) {
                String conversation = Message.getConversationId(this.client.getUser().getUsername(), user.getUsername());
                if (this.currentConversation.equals(conversation)) {
                    this.currentConversation = "";
                    vbChat.setVisible(false);
                }
                this.client.removeConversation(conversation);
            } else {
                this.currentConversation = Message.getConversationId(this.client.getUser().getUsername(), user.getUsername());
                this.client.onConversationOpen(currentConversation);
                init();
            }
        });
        
        container.setOnMouseEntered((e)->{
            container.setBackground(new Background(new BackgroundFill(Color.web("#292b2f"), CornerRadii.EMPTY, new Insets(0,-20,0,-20))));
        });
        
        container.setOnMouseExited((e)->{
            ArrayList<String> _list = Message.getConversationUsers(this.currentConversation);
            boolean _current = false;
            for (String str : _list) {
                if (str.equals(user.getUsername())) _current = true;
            }
            container.setBackground(new Background(new BackgroundFill(Color.web(_current ? "#36393f" : "#2f3136"), CornerRadii.EMPTY, new Insets(0,-20,0,-20))));
        });
        
        // Separator
        Separator sep = new Separator();
        
        // Finally, add em to the VBox
        vbUserList.getChildren().add(container);
        vbUserList.getChildren().add(sep);
    }
    
    public void loadConversation(String conversationId) {
        vbConversation.getChildren().clear();
        
        ArrayList<Message> messages = this.client.getConversationMessages(conversationId);
        for (Message message : messages) {
            loadMessage(message);
        }
    }
    
    private void loadMessage(Message message) {
            if (!message.getConversation().equals(this.currentConversation)) return;
        
            // User
            String username = message.getFrom();
            User user = this.client.getUserByName(username);
            
            // Picture
            ImageView pfp = new ImageView(); 
            pfp.setImage(getImage(user.getPicture()));
            pfp.setPreserveRatio(true);
            pfp.setFitWidth(50);
            pfp.setFitHeight(50);
            
            // Username and time
            Text name = new Text();
            name.setText(user.getUsername());
            name.setFont(new Font("System", 15));
            name.setFill(Color.web("#ffffff"));
            
            Text time = new Text();
            time.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(message.getTime()));
            time.setFont(new Font("System", 12));
            time.setFill(Color.web("#888888"));
            
            HBox ntContainer = new HBox();
            ntContainer.setAlignment(Pos.CENTER_LEFT);
            ntContainer.getChildren().add(name);
            ntContainer.getChildren().add(time); ntContainer.setMargin(time, new Insets(0,0,0,15));

            // Text Container
            VBox textContainer = new VBox();
            textContainer.getChildren().add(ntContainer);

            // Message
            TextFlow wrapper = new TextFlow();
            Text body = new Text(message.getBody());
            body.setFont(new Font("System", 17));
            body.setFill(Color.web("#dddddd"));
            wrapper.getChildren().add(body);
            textContainer.getChildren().add(wrapper);

            // Message Container
            HBox container = new HBox();
            container.setAlignment(Pos.TOP_LEFT);
            container.getChildren().add(pfp);
            container.getChildren().add(textContainer); container.setMargin(textContainer, new Insets(0,0,0,5));
            
            // Add to the Main Container
            vbConversation.getChildren().add(container); vbConversation.setMargin(container, new Insets(5,20,5,5));
    }
    
    private Image getImage(String str) {
        return new Image("client/res/user_images/" + str);
    }
    
    public void openConversation(String conversation) {
        this.currentConversation = conversation;
        
        ArrayList<String> list = Message.getConversationUsers(conversation);
        String name = "";
        for (String username : list) {
            if (!this.client.getUser().getUsername().equals(username)) {
                name = username;
                break;
            }
        }
        
        User user = this.client.getUserByName(name);
        vbChat.setVisible(true);
        txtToUsername.setText(user.getUsername());
        txtToUserFullName.setText("("+user.getName()+")");
        
        this.client.onConversationOpen(this.currentConversation);
        loadConversation(this.currentConversation);
    }
    
    @Override
    public void send(String str) {
        System.out.println("Client to Controller:: " + str);
        
        String[] tokens = str.split(" ");
        if (tokens.length == 0) return;

        String cmd = tokens[0];
        String[] args = new String[tokens.length-1];
        System.arraycopy(tokens, 1, args, 0, args.length);
            
        if (cmd.equalsIgnoreCase("msg")) handleMessage(args[0]);
            else if (cmd.equalsIgnoreCase("online")) handleOnline(args[0]);
            else if (cmd.equalsIgnoreCase("offline")) handleOffline(args[0]);
    }
    
    private void handleMessage(String id) {
        Message msg = this.client.getMessageById(id);
        System.out.println("Message: " + msg.getBody());
        
        Platform.runLater(new Runnable() {
            @Override public void run() {
                init();
            }
        });
    }
    
    private void handleOnline(String username) {
        Circle status = null;
        boolean flag = false;
        for (Iterator<Node> it = vbUserList.getChildren().iterator(); it.hasNext();) {
            Node node = it.next();
            if (!flag) status = null;
            if (node instanceof HBox) {
                for (Iterator<Node> it2 = ((HBox) node).getChildren().iterator(); it2.hasNext();) {
                    Node node2 = it2.next();
                    if (node2 instanceof Circle) {
                        status = (Circle) node2;
                    } else if (node2 instanceof Text) {
                        String name = ((Text) node2).getText().replace("*", "");
                        name = name.replace("*", "");
                        if (name.equalsIgnoreCase(username)) {
                            flag = true;
                            if (status != null) break;
                        }
                    }
                }
            }
            if (flag) break;
        }
        if (flag && (status != null)) status.setFill(Color.web("#00FF00"));
    }
    
    private void handleOffline(String username) {
        Circle status = null;
        boolean flag = false;
        for (Iterator<Node> it = vbUserList.getChildren().iterator(); it.hasNext();) {
            Node node = it.next();
            if (!flag) status = null;
            if (node instanceof HBox) {
                for (Iterator<Node> it2 = ((HBox) node).getChildren().iterator(); it2.hasNext();) {
                    Node node2 = it2.next();
                    if (node2 instanceof Circle) {
                        status = (Circle) node2;
                    } else if (node2 instanceof Text) {
                        String name = ((Text) node2).getText().replace("*", "");
                        if (name.equalsIgnoreCase(username)) {
                            flag = true;
                            if (status != null) break;
                        }
                    }
                }
            }
            if (flag) break;
        }
        if (flag) status.setFill(Color.web("#FF0000"));
    }
}
