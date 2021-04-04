package client.windows;

import client.FXMLController;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import user.User;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class WinProfileController extends FXMLController {
    @FXML ImageView imgPicture;
    @FXML Label labelPicturePath;
    @FXML Label labelUsername;
    @FXML Label labelFullName;
    @FXML Button btnChoose;
    @FXML Button btnBack;
    @FXML Button btnConfirm;
    @FXML PasswordField fieldPass;
    @FXML Text txtWarningPass;
    @FXML DatePicker dpBirthday;
    
    @FXML private void btnChooseAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.client.getWindow().getStage());
        if (file != null) {
            String name = file.getName();
            if (loadImage(name)) labelPicturePath.setText(name);
        }
    }
    
    @FXML private void btnBackAction(ActionEvent event) {
        this.client.getWindow().load(WINDOW.MAIN);
    }
    
    @FXML private void btnConfirmAction(ActionEvent event) {
        User user = this.client.getUser();
        user.setPicture(labelPicturePath.getText());
        user.setPassword(fieldPass.getText());
        user.setBirthday(this.client.getTimestamp(dpBirthday.getValue()));
        this.client.updateUser(user);
        this.client.getWindow().load(WINDOW.MAIN);      // Go back
    }
    
    @Override
    public void init() {
        // User
        User user = this.client.getUser();
        
        // Titles
        labelUsername.setText(user.getUsername());
        labelFullName.setText(user.getName());
        
        // Image
        labelPicturePath.setText(user.getPicture());
        loadImage(user.getPicture());
        
        // Password
        fieldPass.setText(user.getPassword());
        
        // Birthday
        Timestamp date = user.getBirthday();
        dpBirthday.setValue(LocalDate.of(date.getYear()+1900, date.getMonth()+1, date.getDate()));
        
        // Disable warnings
        txtWarningPass.setVisible(false);
        
        // Add listerners
        fieldPass.textProperty().addListener((observable, oldValue, newValue) -> {
            txtWarningPass.setVisible(newValue.isEmpty());
        });
        
        // Confirm button activation thread
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    boolean disable = false;
                    if (fieldPass.getText().isEmpty()) disable = true;
                    if (dpBirthday.getValue() == null) disable = true;
                    btnConfirm.setDisable(disable);
                }
            }
        };
        t.start();
    }
    
    private boolean loadImage(String name) {
        try {
            Image img = new Image("client/res/user_images/" + name);
            imgPicture.setImage(img);
            return true;
        } catch (Exception e) {
            System.out.println("Image not found");
        }
        return false;
    }
}