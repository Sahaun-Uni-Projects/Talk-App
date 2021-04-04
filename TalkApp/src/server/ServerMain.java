package server;

import utils.MACRO;

/**
 *
 * @author Sahaun
 */
public class ServerMain {
    public static void main(String[] args) {
        int port = MACRO.SERVER_PORT;
        Server server = new Server(port);
        server.start();
    }
}
