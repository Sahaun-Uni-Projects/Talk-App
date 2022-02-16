package user;

import java.util.ArrayList;
import client.Client;
import java.sql.Timestamp;
import java.util.Collections;
import org.json.simple.JSONObject;

/**
 *
 * @author Sahaun
 */
public class User {
    private Client client;    
    private String name, username, password, picture;
    private Timestamp birthday;
    private ArrayList<String> friendsList = new ArrayList<String>();
    private ArrayList<String> requestsList = new ArrayList<String>();
    
    public User(String username, String name, String password, String picture, Timestamp birthday) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.picture = picture;
        this.birthday = birthday;
    }
    
    /**public User(String username, String name, String password, String picture, Timestamp birthday, ArrayList<String> friendsList, ArrayList<String> requestsList, JSONObject lastSeen) {
        this(username, name, password, picture, birthday);
        this.friendsList = friendsList;
        this.requestsList = requestsList;
        this.lastSeen = lastSeen;
    }*/
    
    public User(String username, String name, String password, String picture, Timestamp birthday, ArrayList<String> friendsList, ArrayList<String> requestsList) {
        this(username, name, password, picture, birthday);
        this.friendsList = friendsList;
        this.requestsList = requestsList;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return this.password;
    }
    
    public void setPicture(String picture) {
        this.picture = picture;
    }
    
    public String getPicture() {
        return this.picture;
    }
    
    public void setBirthday(Timestamp birthday) {
        this.birthday = birthday;
    }
    
    public Timestamp getBirthday() {
        return this.birthday;
    }
    
    public void setFriendsList(ArrayList<String> friendsList) {
        this.friendsList = friendsList;
    }
    
    public ArrayList<String> getFriendsList() {
        return this.friendsList;
    }
    
    public void setFriends(String friends) {
        String[] arr = friends.split(",");
        this.friendsList.clear();
        if (!friends.isEmpty()) Collections.addAll(this.friendsList, arr);
    }
    
    public String getFriends() {
        ArrayList<String> list = this.friendsList;
        if (list.isEmpty()) return "";
        
        Collections.sort(list);
        String friends = list.get(0);
        for (int i = 1; i < list.size(); ++i) friends += "," + list.get(i);
        return friends;
    }
    
    public void setRequestsList(ArrayList<String> requestsList) {
        this.requestsList = requestsList;
    }
    
    public ArrayList<String> getRequestsList() {
        return this.requestsList;
    }
    
    public void setRequests(String requests) {
        String[] arr = requests.split(",");
        this.requestsList.clear();
        if (!requests.isEmpty()) Collections.addAll(this.requestsList, arr);
    }
    
    public String getRequests() {
        ArrayList<String> list = this.requestsList;
        if (list.isEmpty()) return "";
        
        Collections.sort(list);
        String requests = list.get(0);
        for (int i = 1; i < list.size(); ++i) requests += "," + list.get(i);
        return requests;
    }
}
