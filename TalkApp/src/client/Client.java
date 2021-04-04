package client;

import client.windows.WinMainController;
import database.Database;
import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import listeners.UserStatusListener;
import message.Message;
import user.User;
import utils.WINDOW;

/**
 *
 * @author Sahaun
 */
public class Client {
    private String serverName;
    private int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader reader;
    private Window window;
    private User user;
    private Database db;
    private HashMap<String, Window> windows = new HashMap<>();
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    
    public Client(String serverName, int serverPort) {
        addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String username) {
                System.out.println("ONLINE:: " + username);
            }
            
            @Override
            public void offline(String username) {
                System.out.println("OFFLINE:: " + username);
            }
        });
        
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.db = new Database();
        
        connect();
    }
    
    // ----- Global
    public Timestamp getTimestamp(LocalDate date) {
        String str = "";
        str += date.getYear();
        str += "-";
        str += date.getMonthValue();
        str += "-";
        str += date.getDayOfMonth();
        str += " 00:00:00.0";
        return Timestamp.valueOf(str);
    }
    
    public User getUserByName(String name) {
        return this.db.getUser(name);
    }
    
    public ArrayList<String> getConversationMessageIds(String conversationId) {
        return this.db.getConversationMessageIds(conversationId);
    }
    
    public ArrayList<Message> getConversationMessages(String conversationId) {
        ArrayList<String> ids = this.db.getConversationMessageIds(conversationId);
        ArrayList<Message> list = new ArrayList<>();
        
        for (String id : ids) {
            Message msg = this.db.getMessage(id);
            //msg.fixNewLines();
            list.add(msg);
        }
        
        return list;
    }
    
    public Message getMessageById(String id) {
        //message.fixNewLines();
        //System.out.println("Recieved: " + message);
        return this.db.getMessage(id);
    }
    
    public void onConversationOpen(String id) {
        this.db.conversationOpen(this.user.getUsername(), id);
        //setUser(this.user.getUsername());
    }
    
    public boolean conversationIsUnread(String id) {
        System.out.println("Check:: " + this.user.getUsername() + " :: " + id);
        return this.db.conversationIsUnread(this.user.getUsername(), id);
    }
    
    public boolean registerUser(User user) {
        return db.registerUser(user);
    }
    
    public void updateUser(User user) {
        db.updateUser(user);
        setUser(this.user.getUsername());
    }
    
    public boolean addUser(String username) {
        if (db.userExists(username)) {
            db.addPendingRequest(this.user.getUsername(), username);
            try {
                send("add " + this.user.getUsername() + " " + username);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }
    
    public void requestAccept(String username) {
        db.addFriend(username, this.user.getUsername());
        try {
            send("add " + this.user.getUsername() + " " + username);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void requestDecline(String username) {
        db.removePendingRequest(username, this.user.getUsername());
        setUser(this.user.getUsername());
    }
    
    public void unfriendUser(String username) {
        db.removeFriend(username, this.user.getUsername());
        try {
            send("remove " + this.user.getUsername() + " " + username);
            //setUser(this.user.getUsername());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeConversation(String conversation) {
        this.db.conversationClose(this.user.getUsername(), conversation);
        setUser(this.user.getUsername());
        WinMainController controller = (WinMainController) this.window.getController();
        controller.init();
    }
   
    // ----- Self
    private Window registerWindow(Window win) {
        String id;
        while (true) {
            id = UUID.randomUUID().toString();
            if (!this.windows.containsKey(id)) break;
        }
        this.windows.put(id, win);
        return win;
    }
    
    private boolean unregisterWindow(Window win) {
        if (!this.windows.containsValue(win)) return false;
        String id = "";
        for (Entry<String,Window> entry : this.windows.entrySet()) {
            if (entry.getValue().equals(win)) {
                id = entry.getKey();
                break;
            }
        }
        this.windows.remove(id);
        return true;
    }

    public Window getWindow() {
        return this.window;
    }

    public User getUser() {
        return this.user;
    }
    
    public Client setUser(String name) {
        this.user = this.db.getUser(name);
        return this;
    }
    
    public void openConversation(String username) {
        String id = Message.getConversationId(this.user.getUsername(), username);
        onConversationOpen(id);
        setUser(this.user.getUsername());
        
        this.window.load(WINDOW.MAIN);
        WinMainController controller = (WinMainController) this.window.getController();
        controller.openConversation(id);
    }
    
    // ----- Server
    public void send(String cmd) throws IOException {
        cmd += System.lineSeparator();
        this.serverOut.write(cmd.getBytes());
    }
    
    public String execute(String cmd) throws IOException {
        cmd += System.lineSeparator();
        
        System.out.print("Client command from components:: " + cmd);
        this.serverOut.write(cmd.getBytes());
        
        String res = this.reader.readLine();
        System.out.println("Return value:: " + res);
        return res;
    }
    
    public boolean connect() {
        try {
            System.out.print("Client connecting... ");
            this.socket = new Socket(this.serverName, this.serverPort);
            this.serverIn = this.socket.getInputStream();
            this.serverOut = this.socket.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(this.serverIn));
            this.window = registerWindow(new Window(this, WINDOW.LOGIN));
            this.window.center();
            System.out.println("success");
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("failed");
        return false;
    }
    
    public boolean login(String user, String pass) {
        try {
            String res = execute("login " + user.toLowerCase() + " " + pass);
            if (res.equals("success")) {
                startMessageReader();
                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void logout() {
        try {
            if (this.user != null) send("logout");
            reset();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMessage(String conversation, String body) {
        String id = this.db.registerMessage(this.user.getUsername(), conversation, body);
        try {
            send("msg " + id);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addUserStatusListener(UserStatusListener listener) {
        this.userStatusListeners.add(listener);
    }
    
    public void removeUserStatusListener(UserStatusListener listener) {
        this.userStatusListeners.remove(listener);
    }
    
    private void startMessageReader() {
        Thread t;
        t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }
    
    private void readMessageLoop() {
        try {
            String line;
            while ((line = this.reader.readLine()) != null) {
                System.out.println("Client command from server:: " + line);

                String[] tokens = line.split(" ");
                if (tokens.length == 0) continue;

                String cmd = tokens[0];
                String[] args = new String[tokens.length-1];
                System.arraycopy(tokens, 1, args, 0, args.length);
                
                if (cmd.equalsIgnoreCase("online")) {
                    handleOnline(args);
                } else if (cmd.equalsIgnoreCase("offline")) {
                    handleOffline(args);
                } else if (cmd.equalsIgnoreCase("msg")) {
                    handleMessage(args);
                } else if (cmd.equalsIgnoreCase("updateUser")) {
                    handleUpdateUser(args);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleOnline(String[] args) {
        String username = args[0];
        for (UserStatusListener listener : this.userStatusListeners) {
            listener.online(username);
            this.window.getController().send("online " + username);
        }
    }
    
    private void handleOffline(String[] args) {
        String username = args[0];
        for (UserStatusListener listener : this.userStatusListeners) {
            listener.offline(username);
            this.window.getController().send("offline " + username);
        }
    }
    
    private void handleMessage(String[] args) {
        String id = args[0];
        Message message = this.db.getMessage(id);
        
        System.out.println("Received:: " + message.getBody());
        
        this.window.getController().send("msg " + id);
    }
    
    private void handleUpdateUser(String[] args) {
        setUser(this.user.getUsername());
    }
    
    private void reset() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.window.close();
        Client client = new Client(this.serverName, this.serverPort);
    }
}
