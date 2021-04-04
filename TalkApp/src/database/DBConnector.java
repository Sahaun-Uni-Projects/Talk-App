package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sahaun
 */
public class DBConnector {
    private String user, pass, dbName, serverAddress;
    public Connection con;

    public DBConnector(String user, String pass, String dbName, String serverAddress) {
        this.user = user;
        this.pass = pass;
        this.dbName = dbName;
        this.serverAddress = serverAddress;
    }
    
    public boolean connect() {
        String url = "jdbc:mysql://" + serverAddress + "/" + dbName;
        try {
            this.con = (Connection) DriverManager.getConnection(url, user, pass);
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DBConnector.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public ResultSet query(String q) throws SQLException {
        return this.con.createStatement().executeQuery(q);
    }
    
    public void update(String q) throws SQLException {
        this.con.createStatement().executeUpdate(q);
    }
}
