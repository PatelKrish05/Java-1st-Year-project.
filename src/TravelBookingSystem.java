import java.io.*;
import java.sql.*;
import java.util.*;
import java.time.*;



class TravelBookingSystem extends connection{

    static Scanner sc = new Scanner(System.in);
    int user_id,admin_id;
    /*static String url = "jdbc:mysql://localhost:3306/yatraverse";
    static String dbUsername = "root";
    static String dbPassword = "";
    static Connection con;

    static {
        try {
            con = DriverManager.getConnection(url, dbUsername, dbPassword);
        }
        catch (SQLException e) {
            System.out.println("Server Down...");
            System.exit(0);
        }
    }*/
    static{
        connection co  = new connection();
    }


    static {
        try {
            BufferedReader br = new BufferedReader(new FileReader("Flight Tickets.txt"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] tickets = line.split(" ");
                Flight.f.add(tickets);
            }

            br.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }

    }




    public static void main(String[] args) throws Exception {
        TravelBookingSystem tbs = new TravelBookingSystem();


        while (true) {
            System.out.println("\n1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int choice=new Methods().readValidInt("Enter Choice : ");

            switch (choice) {
                case 1 -> new Auth().registration();
                case 2 -> new Auth().login();
                case 3 -> {
                    con.close();
                    System.exit(0);
                }
                default ->System.out.println("Invalid Choice");
            }
        }
    }
}




