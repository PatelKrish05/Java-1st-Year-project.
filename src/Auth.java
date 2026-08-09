import JDBC.connection;

import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Auth extends connection {
    static Scanner sc = new Scanner(System.in);
    int user_id,admin_id;

    void login() throws Exception{

        Statement Ast = con.createStatement();
        Statement Ust = con.createStatement();
        System.out.print("Username: ");
        String u = sc.nextLine();
        if (u.isBlank()) return;

        System.out.print("Password: ");
        String p = sc.nextLine();
        if (p.isBlank()) return;

        String adminSql = "SELECT * FROM ADMIN WHERE ADMIN_NAME = '" + u +"' AND PASSWORD = '"+p+"'";
        ResultSet adminR = Ast.executeQuery(adminSql);

        String userSql = "SELECT * FROM USER WHERE USERNAME = '" + u +"' AND PASSWORD = '"+p+"'";
        ResultSet userR = Ust.executeQuery(userSql);

        if (adminR.next()) {
            admin_id = adminR.getInt(1);
            Admin admin = new Admin(admin_id);
            FileOutputStream fos = new FileOutputStream("Log.txt",true);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            fos.write(("[" + timestamp + "] Admin "+admin_id+" Logged In\n").getBytes());

            fos.close();
            admin.adminPanel();
        }
        else if (userR.next()) {
            user_id = userR.getInt(1);
            User user= new User();
            FileOutputStream fos = new FileOutputStream("Log.txt",true);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            fos.write(("[" + timestamp + "] User "+user_id+" Logged In\n").getBytes());

            fos.close();
            user.userPanel(user_id);
        }
        else {
            System.out.println("Invalid Username or Password!");
        }
    }

    void registration() throws Exception {
        String uTry;
        String password, username="" ;
        String country , state,city, mCode = "";
        int country_id = 0,state_code=0,city_id=0;

        //Username
        Boolean uCheck = true;
        while(true) {
            System.out.print("Username: ");
            uTry = new Methods().cValidation();
            if(uTry == null || uTry.isBlank()){ return; }
            Statement st = con.createStatement();
            Statement Ast = con.createStatement();
            String sql = "SELECT * FROM USER WHERE USERNAME = '" + uTry+"'";
            String Asql = "SELECT * FROM ADMIN WHERE ADMIN_NAME = '" + uTry+"'";
            ResultSet rs = st.executeQuery(sql);
            ResultSet Ars = Ast.executeQuery(Asql);
            if (!rs.next()&&!Ars.next()) {
                username = uTry;
                break;
            }
            else {
                System.out.println("Username Already Taken , Try Different Username.");
            }
        }


        //Password
        System.out.println(
                "Password must be at least 8 characters long and contain at least one uppercase letter, one digit, and one special character (@, $, !, %, , ?, &). ");
        while (true) {
            System.out.print("Password: ");
            String p = sc.nextLine();
            if (p.isBlank()) return;
            boolean checkPass = new Methods().isValidPassword(p);
            if (checkPass) {
                password = p;
                break;
            } else {
                System.out.println("Enter Valid Password According to Instructions!!!");
            }
        }
//Mobile Number
        System.out.print("Mobile no. : "+mCode+" ");
        String mobInput = new Methods().mobileValidation();
        if (mobInput == null || mobInput.isBlank()) return;
        long mo = Long.parseLong(mobInput);

//Email
        String email;
        while (true) {
            System.out.print("Email ID : ");
            email = sc.nextLine();
            if (email.isBlank()) return;
            if(new Methods().emailValidation(email)){
                break;
            }
            System.out.println("Try Again");
        }
//country
        while (true) {
            System.out.print("Country : ");
            country = sc.nextLine();
            if (country.isBlank()) return;
            Statement st = con.createStatement();
            String sql = "SELECT * FROM COUNTRIES WHERE COUNTRY_NAME = '" + country+"'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                country_id = rs.getInt(1);
                mCode = rs.getString(4);
                break;
            }
            else {
                System.out.println("Invalid Country Name");
            }
        }

        //State
        while (true) {
            System.out.print("State : ");
            state = sc.nextLine();
            if (state.isBlank()) return;
            Statement st = con.createStatement();
            String sql = "SELECT * FROM STATES WHERE COUNTRY_ID = " + country_id+" AND STATE_NAME = '"+ state+"'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                state_code = rs.getInt(1);
                break;
            }
            else {
                System.out.println("Invalid State Name");
            }
        }

//City

        while (true) {
            System.out.print("City : ");
            city = sc.nextLine();
            if (city.isBlank()) return;
            Statement st = con.createStatement();
            String sql = "SELECT * FROM CITIES WHERE STATE_ID = " + state_code +" AND CITY_NAME ='"+ city+"'";
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                city_id = rs.getInt(1);
                break;
            }
            else {
                System.out.println("Invalid City Name");
            }
        }


        String userEntry="INSERT INTO USER (`Username`, `Password`, `MO`, `Email`, `Country`, `State`, `City`) " +
                "VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(userEntry);
        ps.setString(1,username);
        ps.setString(2,password);
        ps.setLong(3,mo);
        ps.setString(4,email);
        ps.setString(5,country);
        ps.setString(6,state);
        ps.setString(7,city);

        int r = ps.executeUpdate();
        if(r!=0){
            System.out.println("Account Created!");
            FileOutputStream fos = new FileOutputStream("Log.txt", true);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            fos.write(("[" + timestamp + "] User Registered\n").getBytes());
            fos.close();
        }
        else {
            System.out.println("Failed");

        }
    }
}