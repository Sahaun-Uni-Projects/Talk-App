package client;

import javafx.application.Platform;

/**
 *
 * @author Sahaun
 */
public class FXMLController {
    protected Client client;
    
    public void init() {}
    
    public void send(String str) {}
    
    public void setClient(Client client) {
        this.client = client;
    }

    public void initialize(Client client) {
        setClient(client);
        init();
    }
}
