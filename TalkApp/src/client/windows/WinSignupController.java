package client.windows;

import client.FXMLController;
import client.Window;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDate;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
public class WinSignupController extends FXMLController {
    @FXML ImageView imgPicture;
    @FXML Label labelPicturePath;
    @FXML Button btnChoose;
    @FXML Button btnBack;
    @FXML Button btnConfirm;
    @FXML TextField fieldName;
    @FXML TextField fieldUser;
    @FXML PasswordField fieldPass;
    @FXML Text txtWarningName;
    @FXML Text txtWarningUser;
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
        this.client.logout();
    }
    
    @FXML private void btnConfirmAction(ActionEvent event) {
        User user = new User(
            fieldUser.getText(),
            fieldName.getText(),
            fieldPass.getText(),
            labelPicturePath.getText(),
            this.client.getTimestamp(dpBirthday.getValue())
        );
        if (this.client.registerUser(user)) {
            this.client.logout();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Username is taken! Please use a different username.");
            alert.showAndWait();
        }
    }
    
    @Override
    public void init() {
        // Disable warnings
        txtWarningName.setVisible(false);
        txtWarningUser.setVisible(false);
        txtWarningPass.setVisible(false);
        
        // Add listerners
        fieldName.textProperty().addListener((observable, oldValue, newValue) -> {
            txtWarningName.setVisible(!isBetween(newValue.length(), 3, 50));
        });
        fieldUser.textProperty().addListener((observable, oldValue, newValue) -> {
            txtWarningUser.setVisible(!isBetween(newValue.length(), 3, 50));
        });
        fieldPass.textProperty().addListener((observable, oldValue, newValue) -> {
            txtWarningPass.setVisible(newValue.isEmpty());
        });
        
        // Confirm button activation thread
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    boolean disable = false;
                    if (!isBetween(fieldName.getText().length(), 3, 50)) disable = true;
                    if (!isBetween(fieldUser.getText().length(), 3, 50)) disable = true;
                    if (fieldPass.getText().isEmpty()) disable = true;
                    if (dpBirthday.getValue() == null) disable = true;
                    btnConfirm.setDisable(disable);
                }
            }
        };
        t.start();
    }
    
    private boolean isBetween(int val, int a, int b) {
        int mn = Math.min(a, b);
        int mx = Math.max(a, b);
        return ((mn <= val) && (val <= mx ));
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