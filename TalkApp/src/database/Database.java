package database;

import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.ServerWorker;
import user.User;

/**
 *
 * @author Sahaun
 */
public class Database {
    private DBConnect con;
    
    public Database() {
        this.con = new DBConnect();
        
        /*this.con = new DBConnector("root", "", "talk_db", "localhost:3306");
        System.out.print("Database connecting through DBConnector... ");
        if (this.con.connect()) System.out.println("success");
            else System.out.println("failed");*/
    }
    
    // ----- Checkers
    public boolean userExists(String username) {
        try {
            this.con.connect();
            ResultSet res = this.con.query("SELECT * FROM users WHERE (username=\""+username+"\")");
            if (res.next()) return true;
            this.con.connect();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    // ----- Getters
    public User getUser(String name) {
        String username = "",
               fullName = "",
               password = "",
               image = "",
               str;
        ArrayList<String> friendsList = new ArrayList<>();
        ArrayList<String> requestsList = new ArrayList<>();
        Timestamp birthday = null;
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * FROM users WHERE (username='"+name+"')");
            if (!res.next()) return null;
            username = res.getString("username");
            fullName = res.getString("name");
            password = res.getString("pass");
            image = res.getString("image_name");
            birthday = Timestamp.valueOf(res.getString("birthday"));
            friendsList = getFriends(username);
            friendsList.forEach(value -> System.out.print(value));

            
            /*str = res.getString("friends_list");
            if (!str.isEmpty()) Collections.addAll(friendsList, str.split(",[ ]*"));
            
            str = res.getString("friends_pending");
            if (!str.isEmpty()) Collections.addAll(requestsList, str.split(",[ ]*"));
            
            lastSeen = (JSONObject) new JSONParser().parse(res.getString("conversation_last_opened"));*/
            
            this.con.disconnect();
        } catch (SQLException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new User(username, fullName, password, image, birthday, friendsList, requestsList);
    }
    
    public Timestamp getUserLastSeen(String name, BigInteger conversation) {
        Timestamp lastSeen = null;
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * FROM conversation_viewer WHERE ((" + pair("username",name) + ") or (" + pair("channel",conversation.toString()) + "))");
            if (res.next()) {
                lastSeen = Timestamp.valueOf(res.getString("last_seen"));
            }
            
            this.con.disconnect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lastSeen;
    }
    
    public ArrayList<String> getFriends(String name) {
        ArrayList<String> friendsList = new ArrayList<>();
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * FROM friendships WHERE ((" + pair("user1",name) + ") or (" + pair("user2",name) + "))");
            while (res.next()) {
                String u1 = res.getString("user1");
                String u2 = res.getString("user2");
                friendsList.add(u1.equals(name) ? u2 : u1);
                System.out.println(res.getString("user1") + " " + res.getString("user2"));
            }
            
            this.con.disconnect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return friendsList;
    }
    
    
    public int getConversationId(String user1, String user2) {
        int id = -1;
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * FROM channels WHERE (((" + pair("user1",user1) + ") and (" + pair("user2",user2) + ")) or ((" + pair("user1",user2) + ") and (" + pair("user2",user1) + ")))");
            if (res.next()) {
                id = res.getInt("id");
            }
            
            this.con.disconnect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return id;
    }
    
    public Message getMessage(int id) {
        String from = "",
               body = "";
        int conversation = -1;
        Timestamp time = new Timestamp(System.currentTimeMillis());
        
        try {
            this.con.connect();
            ResultSet res = this.con.query("SELECT * FROM messages_ WHERE ("+pair("id",String.valueOf(id))+")");
            if (!res.next()) return null;
            from = res.getString("username");
            conversation = Integer.valueOf(res.getString("channel"));
            time = Timestamp.valueOf(res.getString("date_"));
            body = res.getString("msg");
            this.con.disconnect();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new Message(from, conversation, time, body);
    }
    
    public ArrayList<Integer> getConversationMessageIds(int id) {
        ArrayList<Integer> list = new ArrayList<>();
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * FROM messages_ WHERE ("+pair("channel",String.valueOf(id))+")");
            while (res.next()) {
                list.add(Integer.valueOf(res.getString("id")));
            }
            
            this.con.disconnect();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return list;
    }
    
    public void conversationOpen(String username, int conversation) {
        try {
            this.con.connect();
            
            User user = getUser(username);
            String vals = "date_=" + getCurrentTimestamp().toString();
            System.out.println("QQQQ :: " + "UPDATE conversation_viewer SET " + vals + " WHERE ((" + pair("username",username) + ") and (" + pair("channel",String.valueOf(conversation)) + "))");
            //this.con.update("UPDATE conversation_viewer SET " + vals + " WHERE ((" + pair("username",username) + ") and (" + pair("channel",String.valueOf(conversation)) + "))");
            
            this.con.disconnect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return;
    }
    
    public void conversationClose(String username, int conversation) {
        return;
        
        /*JSONObject lastSeen = getUser(username).getLastSeen();
        if (lastSeen.containsKey(conversation)) lastSeen.remove(conversation);
        String json = lastSeen.toString().replace("\"", "\\\"");
        this.con.update("UPDATE `users` SET `conversation_last_opened`="+stringify(json)+" WHERE `username`="+stringify(username));
        */
    }
    
    public boolean conversationIsUnread(String username, String conversation) {
        return false;
        
        /*Timestamp lastMessage = Timestamp.valueOf("1980-01-01 10:00:00"),
                  lastSeen    = Timestamp.valueOf("1980-01-02 10:00:00");
        
        ArrayList<String> list = getConversationMessageIds(conversation);
        if (!list.isEmpty()) {
            Message msg = getMessage(list.get(list.size()-1));
            if (!username.equals(msg.getFrom())) lastMessage = msg.getTime();
        }
        
        try {
            ResultSet res = this.con.query("SELECT `conversation_last_opened` FROM `users` WHERE `username`="+stringify(username));
            if (res.next()) {
                JSONObject json = (JSONObject) new JSONParser().parse(res.getString("conversation_last_opened"));
                if (json.containsKey(conversation)) {
                    lastSeen = Timestamp.valueOf((String) json.get(conversation));
                }
            }
            
        } catch (SQLException | ParseException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(username + " " + conversation + " " + lastSeen + " " + lastMessage);
        return lastSeen.before(lastMessage);*/
    }
    
    // ----- Modifiers
    public boolean registerUser(User user) {
        try {
            this.con.connect();
            
            String vals = stringify(user.getUsername());
            vals += ","; vals += stringify(user.getName());
            vals += ","; vals += stringify(user.getPassword());
            vals += ","; vals += stringify(user.getPicture());
            vals += ","; vals += stringify(user.getBirthday().toString());
            vals += ","; vals += "null";
            this.con.update("INSERT INTO users(username, name, pass, image_name, birthday, last_seen) VALUES (" + vals + ")");
            
            this.con.disconnect();
            
            return true;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }
    
    public void updateUser(User user) {
        String username = user.getUsername();
        
        try {
            this.con.connect();
            
            String vals = "name=" + stringify(user.getName());
            vals += ","; vals += "pass=" + stringify(user.getPassword());
            vals += ","; vals += "image_name=" + stringify(user.getPicture());
            vals += ","; vals += "birthday=" + stringify(user.getBirthday().toString());
            System.out.println("CMD :: " + "UPDATE users SET " + vals + " WHERE username=" + stringify(user.getUsername()));
            this.con.update("UPDATE users SET " + vals + " WHERE username=" + stringify(user.getUsername()));
            
            this.con.disconnect();
        } catch (Exception ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addPendingRequest(String from, String to) {
        User user = getUser(to);
        if (!user.getRequestsList().contains(from)) user.getRequestsList().add(from);
        updateUser(user);
    }
    
    public void removePendingRequest(String from, String to) {
        User user = getUser(to);
        if (user.getRequestsList().contains(from)) user.getRequestsList().remove(from);
        updateUser(user);
    }
    
    public void addFriend(String from, String to) {
        User user;
        ArrayList<String> list;
        
        user = getUser(from);
        list = user.getFriendsList();
        if (!list.contains(to)) list.add(to);
        updateUser(user);
        
        user = getUser(to);
        list = user.getFriendsList();
        if (!list.contains(from)) list.add(from);
        updateUser(user);
        
        removePendingRequest(from, to);
    }
    
    public void removeFriend(String from, String to) {
        return;
        
        /*String conversation = Message.getConversationId(from, to);
                
        User user;
        ArrayList<String> list;
        JSONObject lastSeen;
        
        user = getUser(from);
        list = user.getFriendsList();
        lastSeen = user.getLastSeen();
        if (list.contains(to)) list.remove(to);
        if (lastSeen.containsKey(conversation)) lastSeen.remove(conversation);
        updateUser(user);
        
        user = getUser(to);
        list = user.getFriendsList();
        lastSeen = user.getLastSeen();
        if (list.contains(from)) list.remove(from);
        if (lastSeen.containsKey(conversation)) lastSeen.remove(conversation);
        updateUser(user);
*/
    }
    
    public void registerConversation(String user1, String user2) {
        String vals = user1;
        vals += ","; vals += user2;
        vals += ","; vals += getCurrentTimestamp().toString();
        this.con.update("INSERT INTO channels VALUES ("+vals+")");
    }
    
    public int registerMessage(String from, int conversation, String body) {
        int msg_id = -1;
        ResultSet res;
        
        try {
            this.con.connect();
            
            for (int i = 0; i < 1000; ++i) {
                res = this.con.query("SELECT * from messages_ where(" + pair("id",String.valueOf(i)) + ")");
                if (!res.next()) {
                    msg_id = i;
                    break;
                }
            }
            
            String vals = stringify(from);
            vals += ","; vals += stringify(String.valueOf(conversation));
            vals += ","; vals += stringify(body);
            vals += ","; vals += stringify(getCurrentTimestamp().toString());
            
            this.con.update("INSERT INTO messages_ VALUES ("+vals+")");
            this.con.disconnect();
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //registerConversation(conversation);
        //addToConversation(conversation, id);
        
        return msg_id;
    }
    
    private String stringify(String str) {
        return ("'" + str + "'");
    }
    
    private String pair(String key, String value) {
        return (key + "=" + stringify(value));
    }
    
    private Timestamp getCurrentTimestamp() {
        return (new Timestamp(System.currentTimeMillis()));
    }

    public ArrayList<String> getConversationUsers(int conversation) {
        ArrayList<String> users = new ArrayList<>();
        
        try {
            this.con.connect();
            
            ResultSet res = this.con.query("SELECT * from channels where(" + pair("id", String.valueOf(conversation)) + ")");
            if (res.next()) {
                users.add(res.getString("user1"));
                users.add(res.getString("user2"));
            }
            
            this.con.disconnect();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return users;
    }
}
