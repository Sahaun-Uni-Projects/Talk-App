package database;

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
    private DBConnector con;
    
    public Database() {
        this.con = new DBConnector("root", "", "talk_db", "localhost:3306");
        System.out.print("Database connecting through DBConnector... ");
        if (this.con.connect()) System.out.println("success");
            else System.out.println("failed");
    }
    
    // ----- Checkers
    public boolean userExists(String username) {
        try {
            ResultSet res = this.con.query("SELECT * FROM `users` WHERE (username=\""+username+"\")");
            if (res.next()) return true;
        } catch (SQLException ex) {
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
        JSONObject lastSeen = null;
        Timestamp birthday = null;
        
        try {
            ResultSet res = this.con.query("SELECT * FROM `users` WHERE (username=\""+name+"\")");
            if (!res.next()) return null;
            username = res.getString("username");
            fullName = res.getString("name");
            password = res.getString("password");
            image = res.getString("image_name");
            birthday = Timestamp.valueOf(res.getString("birthday"));
            
            str = res.getString("friends_list");
            if (!str.isEmpty()) Collections.addAll(friendsList, str.split(",[ ]*"));
            
            str = res.getString("friends_pending");
            if (!str.isEmpty()) Collections.addAll(requestsList, str.split(",[ ]*"));
            
            lastSeen = (JSONObject) new JSONParser().parse(res.getString("conversation_last_opened"));
        } catch (SQLException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new User(username, fullName, password, image, birthday, friendsList, requestsList, lastSeen);
    }
    
    public Message getMessage(String id) {
        String from = "",
               conversation = "",
               body = "";
        Timestamp time = new Timestamp(System.currentTimeMillis());
        
        try {
            ResultSet res = this.con.query("SELECT * FROM `messages` WHERE ("+pair("id",id)+")");
            if (!res.next()) return null;
            from = res.getString("from_user");
            conversation = res.getString("to_conversation");
            time = Timestamp.valueOf(res.getString("time"));
            body = res.getString("body");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new Message(from, conversation, time, body);
    }
    
    public ArrayList<String> getConversationMessageIds(String id) {
        ArrayList<String> list = new ArrayList<>();
        
        try {
            ResultSet res = this.con.query("SELECT * FROM `conversations` WHERE ("+pair("id",id)+")");
            if (!res.next()) return list;
            Collections.addAll(list, res.getString("messages").split(",[ ]*"));
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return list;
    }
    
    public void conversationOpen(String username, String conversation) {
        JSONObject lastSeen = getUser(username).getLastSeen();
        String time = (new Timestamp(System.currentTimeMillis())).toString();
        lastSeen.put(conversation, time);
        try {
            String json = lastSeen.toString().replace("\"", "\\\"");
            this.con.update("UPDATE `users` SET `conversation_last_opened`="+stringify(json)+" WHERE `username`="+stringify(username));
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void conversationClose(String username, String conversation) {
        JSONObject lastSeen = getUser(username).getLastSeen();
        if (lastSeen.containsKey(conversation)) lastSeen.remove(conversation);
        try {
            String json = lastSeen.toString().replace("\"", "\\\"");
            this.con.update("UPDATE `users` SET `conversation_last_opened`="+stringify(json)+" WHERE `username`="+stringify(username));
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean conversationIsUnread(String username, String conversation) {
        Timestamp lastMessage = Timestamp.valueOf("1980-01-01 10:00:00"),
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
        return lastSeen.before(lastMessage);
    }
    
    // ----- Modifiers
    public boolean registerUser(User user) {
        try {
            String vals = stringify(user.getUsername());
            vals += ","; vals += stringify(user.getName());
            vals += ","; vals += stringify(user.getPassword());
            vals += ","; vals += stringify(user.getPicture());
            vals += ","; vals += stringify(user.getBirthday().toString());
            this.con.update("INSERT INTO `users`(`username`, `name`, `password`, `image_name`, `birthday`) VALUES (" + vals + ")");
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public void updateUser(User user) {
        String username = user.getUsername();
        
        try {
            String vals = "`name`=" + stringify(user.getName());
            vals += ","; vals += "`password`=" + stringify(user.getPassword());
            vals += ","; vals += "`image_name`=" + stringify(user.getPicture());
            vals += ","; vals += "`birthday`=" + stringify(user.getBirthday().toString());
            vals += ","; vals += "`friends_list`=" + stringify(user.getFriends());
            vals += ","; vals += "`friends_pending`=" + stringify(user.getRequests());
            vals += ","; vals += "`conversation_last_opened`=" + stringify(user.getLastSeen().toString().replace("\"", "\\\""));
            System.out.println("CMD :: " + "UPDATE `users` SET " + vals + " WHERE `username`=" + stringify(user.getUsername()));
            this.con.update("UPDATE `users` SET " + vals + " WHERE `username`=" + stringify(user.getUsername()));
        } catch (SQLException ex) {
            Logger.getLogger(ServerWorker.class.getName()).log(Level.SEVERE, null, ex);
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
        String conversation = Message.getConversationId(from, to);
                
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
    }
    
    public void registerConversation(String conversationId) {
        try {
            String vals = stringify(conversationId);
            vals += ","; vals += stringify("");
            this.con.update("INSERT INTO `conversations`(`id`, `messages`) VALUES ("+vals+")");
        } catch (SQLException ex) {
            System.out.println("Conversation \"" + conversationId + "\" is already registered.");
        }
    }
    
    private void addToConversation(String conversation_id, String message_id) {
        try {
            ResultSet res = this.con.query("SELECT * FROM `conversations` WHERE ("+pair("id",conversation_id)+")");
            if (!res.next()) return;
            
            String msg = res.getString("messages");
            if (!msg.isEmpty()) msg += ",";
            msg += message_id;
            msg = stringify(msg);
            
            this.con.update("UPDATE `conversations` SET `messages`=" + msg + "WHERE ("+pair("id",conversation_id)+")");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String registerMessage(String from, String conversation, String body) {
        String id = "";
        ResultSet res;
        
        try {
            while (true) {
                id = UUID.randomUUID().toString();
                res = this.con.query("SELECT * FROM `messages` WHERE ("+pair("id",id)+")");
                if (!res.next()) break;
            }
            
            String time = (new Timestamp(System.currentTimeMillis())).toString();
            String vals = stringify(id);
            vals += ","; vals += stringify(from);
            vals += ","; vals += stringify(conversation);
            vals += ","; vals += stringify(time);
            vals += ","; vals += stringify(body);
            
            this.con.update("INSERT INTO `messages`(`id`, `from_user`, `to_conversation`, `time`, `body`) VALUES ("+vals+")");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        registerConversation(conversation);
        addToConversation(conversation, id);
        
        return id;
    }
    
    private String stringify(String str) {
        return ("\"" + str + "\"");
    }
    
    private String pair(String key, String value) {
        return (key + "=" + stringify(value));
    }
}
