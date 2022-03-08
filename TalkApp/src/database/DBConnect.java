package database;

/**
 *
 * @author ssaha
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnect {
    private Statement statement = null;
    private Connection connection;
    
    public void connect() throws ClassNotFoundException, SQLException {        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        String connectionUrl = "jdbc:sqlserver://localhost:1433;user=sa;password=p@ssword13;databaseName=TalkDB";
        connection = DriverManager.getConnection(connectionUrl);
        statement = connection.createStatement();
        statement.executeUpdate("USE TalkDB");
    }
    
    public boolean disconnect() {
        try {
            if(statement != null) {
                statement.close();
            }
            if(connection != null) {
                connection.close();
            }
            return true;
        } catch (SQLException ex){
            return false;
        }
    }
    
    public boolean update(String query) {
        System.out.println("UPDATE :: " + query);
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            return true;
        } catch (SQLException ex){
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }        
     }
    
    public ResultSet query(String query) {
        System.out.println("QUERY :: " + query);
        try {
            statement = connection.createStatement();
            ResultSet resultset = statement.executeQuery(query);
            return resultset;            
        } catch (SQLException ex){
            Logger.getLogger(DBConnect.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }       
    }
}

