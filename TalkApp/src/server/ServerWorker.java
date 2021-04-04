package server;

import database.Database;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import user.User;

/**
 *
 * @author Sahaun
 */
public class ServerWorker extends Thread {
    private Socket clientSocket;
    private Server server;
    private OutputStream clientOut;
    private Database db;
    private User user;
    
    public ServerWorker(Server server, Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientOut = clientSocket.getOutputStream();
        this.db = new Database();
        this.user = null;
        System.out.println("Created worker for client socket: " + clientSocket + " (server: " + server + ")");
    }
    
    @Override
    public void run() {
        try {
            handleClient();
        } catch (IOException ex) {
            System.out.println("Socket closed.");
            try {
                handleLogout();
            } catch (IOException ex1) {
                System.out.println("Can not handle logout.");
            }
        }
    }
    
    private void handleClient() throws IOException {
        InputStream in = this.clientSocket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            if (tokens.length == 0) continue;
            
            String cmd = tokens[0];
            String[] args = new String[tokens.length-1];
            System.arraycopy(tokens, 1, args, 0, args.length);
            
            if (cmd.equalsIgnoreCase("login")) {
                handleLogin(args);
            } else if (cmd.equalsIgnoreCase("logout")) {
                handleLogout();
            } else if (cmd.equalsIgnoreCase("msg")) {
                System.out.println(line);
                tokens = line.split(" ", 4);
                cmd = tokens[0];
                args = new String[tokens.length-1];
                System.arraycopy(tokens, 1, args, 0, args.length);
                
                handleMessage(args);
            } else if (cmd.equals("add")) {
                handleAdd(args);
            } else if (cmd.equals("remove")) {
                handleRemove(args);
            } else if (cmd.equalsIgnoreCase("getOnlineUsers")) {
                handleGetOnlineUsers();
            }
        }
        
        handleLogout();
    }
    
    private void handleLogin(String[] args) throws IOException {
        if (args.length != 2) {
            send("failed");
            return;
        }
        String name = args[0];
        String pass = args[1];
        
        User user = db.getUser(name);
        if (user == null) {
            send("failed");
            return;
        }
        
        if (user.getPassword().equals(pass)) {
            this.user = user;
            send("success");
            broadcast("online " + name, true);
        } else {
            send("failed");
        }
    }
    
    private void handleLogout() throws IOException {
        this.server.removeWorker(this);
        
        broadcast("offline " + this.user.getUsername(), true);
        this.user = null;
        
        this.clientSocket.close();
    }
    
    private void handleAdd(String[] args) throws IOException {
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (String username : args) {
            for (ServerWorker worker : workers) {
                if (worker.getUser().getUsername().equals(username)) {
                    worker.send("updateUser");
                }
            }
        }
    }
    
    private void handleRemove(String[] args) throws IOException {
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (String username : args) {
            for (ServerWorker worker : workers) {
                if (worker.getUser().getUsername().equals(username)) {
                    worker.send("updateUser");
                }
            }
        }
    }

    private void handleGetOnlineUsers() throws IOException {
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (ServerWorker worker : workers) {
            if (worker == this) continue;
            User otherUser = worker.getUser();
            if (otherUser == null) continue;
            send("online " + otherUser.getUsername());
        }
    }
    
    private void handleMessage(String[] args) throws IOException {
        String id = args[0];
        Message message = db.getMessage(id);
        
        ArrayList<String> participants = Message.getConversationUsers(message.getConversation());
        
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (ServerWorker worker : workers) {
            User user = worker.getUser();
            if (user == null) continue;
            for (String participant : participants) {
                if (participant.equals(user.getUsername())) {
                    worker.send("msg " + id);
                    break;
                }
            }
        }
    }
    
    public User getUser() {
        return this.user;
    }
    
    public void send(String str) throws IOException {
        String s = "";
        if (this.user != null) s = user.getUsername();
        System.out.println("From " + s + ":: " + str);
        str += System.lineSeparator();
        this.clientOut.write(str.getBytes());
    }
    
    public void broadcast(String str, boolean ignoreSelf) throws IOException {
        if (this.user == null) return;
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (ServerWorker worker : workers) {
            if (ignoreSelf && (worker == this)) continue;
            User user = worker.getUser();
            if (user == null) continue;
            worker.send(str);
        }
    }
    
    public void broadcast(String str) throws IOException {
        if (this.user == null) return;
        broadcast(str, false);
    }
    
    public void broadcastToFriends(String str) throws IOException {
        if (this.user == null) return;
        ArrayList<ServerWorker> workers = this.server.getWorkersList();
        for (ServerWorker worker : workers) {
            if (worker.getUser() == null) continue;
            if (worker.getUser().getFriendsList().contains(this.user.getUsername())) {
                worker.send(str);
            }
        }
    }
}
