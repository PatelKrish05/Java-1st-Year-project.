package JDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class connection {
    protected static Connection con;
    public connection(){
        String url = "jdbc:mysql://localhost:3306/yatraverse";
        String username = "root";
        String password = "";



        try {
             con = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            System.out.println("Server Down...");
            System.exit(0);
        }
    }
}

