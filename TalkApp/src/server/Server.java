package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sahaun
 */
public class Server extends Thread {
    private final int port;
    private ArrayList<ServerWorker> workersList = new ArrayList<>();
    
    public Server(int port) {
        this.port = port;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);
            System.out.println("Server started at port: " + this.port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerWorker worker = new ServerWorker(this, clientSocket);
                this.workersList.add(worker);
                
                System.out.println("Accepted connection from from " + clientSocket);
                System.out.println("Worker count: " + this.workersList.size());
                
                worker.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<ServerWorker> getWorkersList() {
        return this.workersList;
    }
    
    public void removeWorker(ServerWorker worker) {
        this.workersList.remove(worker);
    }
}
