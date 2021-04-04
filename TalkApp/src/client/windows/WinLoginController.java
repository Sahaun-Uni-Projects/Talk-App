package client.windows;

import client.FXMLController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class WinLoginController extends FXMLController {
    @FXML private Button btnLogin;
    @FXML private Button btnSignup;
    @FXML private TextField fieldUser;
    @FXML private PasswordField fieldPass;
    @FXML private Text txtInvalid;
    
    @FXML private void btnLoginAction(ActionEvent event) {
        String user = fieldUser.getText();
        String pass = fieldPass.getText();
        if (this.client.login(user, pass)) {
            this.client.setUser(user);
            this.client.getWindow().load(WINDOW.MAIN);
            txtInvalid.setVisible(false);
        } else {
            txtInvalid.setVisible(true);
        }
    }
    
    @FXML private void btnSignupAction(ActionEvent event) {
        this.client.getWindow().load(WINDOW.SIGNUP);
    }
    
    @Override
    public void init() {
        fieldUser.textProperty().addListener((observable, oldValue, newValue) -> {
            txtInvalid.setVisible(false);
        });
        fieldPass.textProperty().addListener((observable, oldValue, newValue) -> {
            txtInvalid.setVisible(false);
        });
    }
}
