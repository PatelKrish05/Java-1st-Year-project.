package Travel_Booking_System;

import JDBC.connection;
import Admin.*;
import Costumer.*;

import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Auth extends connection {
    static Scanner sc = new Scanner(System.in);
    int user_id, admin_id;

    void login() throws Exception {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        if (u.isBlank()) return;

        System.out.print("Password: ");
        String p = sc.nextLine().trim();
        if (p.isBlank()) return;

        String adminSql = "SELECT * FROM ADMIN WHERE ADMIN_NAME = ? AND PASSWORD = ?";
        PreparedStatement Ast = con.prepareStatement(adminSql);
        Ast.setString(1, u);
        Ast.setString(2, p);
        ResultSet adminR = Ast.executeQuery();

        String userSql = "SELECT * FROM USER WHERE USER_NAME = ? AND PASSWORD = ?";
        PreparedStatement Ust = con.prepareStatement(userSql);
        Ust.setString(1, u);
        Ust.setString(2, p);
        ResultSet userR = Ust.executeQuery();

        if (adminR.next()) {
            admin_id = adminR.getInt(1);
            Admin admin = new Admin(admin_id);
            try (FileOutputStream fos = new FileOutputStream("Log.txt", true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                fos.write(("[" + timestamp + "] Admin " + admin_id + " Logged In\n").getBytes());
            }
            admin.adminPanel();
        } else if (userR.next()) {
            user_id = userR.getInt(1);
            User user = new User();
            try (FileOutputStream fos = new FileOutputStream("Log.txt", true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                fos.write(("[" + timestamp + "] User " + user_id + " Logged In\n").getBytes());
            }
            user.userPanel(user_id);
        } else {
            System.out.println("Invalid Username or Password!");
        }
    }

    void registration() throws Exception {
        String uTry;
        String password = "", username = "";
        String country = "", state = "", city = "", pincode = "";

        // 1. Username Validation
        while (true) {
            System.out.print("Username: ");
            uTry = new Methods().cValidation();
            if (uTry == null || uTry.isBlank()) { return; }

            String userCheckSql = "SELECT 1 FROM USER WHERE USER_NAME = ?";
            String adminCheckSql = "SELECT 1 FROM ADMIN WHERE ADMIN_NAME = ?";

            PreparedStatement st = con.prepareStatement(userCheckSql);
            st.setString(1, uTry);
            ResultSet rs = st.executeQuery();

            PreparedStatement Ast = con.prepareStatement(adminCheckSql);
            Ast.setString(1, uTry);
            ResultSet Ars = Ast.executeQuery();

            if (!rs.next() && !Ars.next()) {
                username = uTry;
                break;
            } else {
                System.out.println("Username Already Taken, Try Different Username.");
            }
        }

        // 2. Password Validation
        System.out.println("Password must be at least 8 characters long and contain at least one uppercase letter, one digit, and one special character (@, $, !, %, ?, &).");
        while (true) {
            System.out.print("Password: ");
            String p = sc.nextLine().trim();
            if (p.isBlank()) return;
            boolean checkPass = new Methods().isValidPassword(p);
            if (checkPass) {
                password = p;
                break;
            } else {
                System.out.println("Enter Valid Password According to Instructions!!!");
            }
        }

        // 3. Mobile Number Validation
        System.out.print("Mobile no. : ");
        String mobInput = new Methods().mobileValidation();
        if (mobInput == null || mobInput.isBlank()) return;
        long mo = Long.parseLong(mobInput);

        // 4. Email Validation
        String email;
        while (true) {
            System.out.print("Email ID : ");
            email = sc.nextLine().trim();
            if (email.isBlank()) return;
            if (new Methods().emailValidation(email)) {
                break;
            }
            System.out.println("Try Again");
        }

        // 5. Location Handling (Auto-Patch via Pincode with Manual Option)
        while (true) {
            System.out.print("Pincode / Postal Code: ");
            pincode = sc.nextLine().trim();
            if (pincode.isBlank()) return;

            String pinSql = "SELECT city_name, state_name, country_name FROM view_pincode_location WHERE pincode = ? LIMIT 1";
            PreparedStatement pinPst = con.prepareStatement(pinSql);
            pinPst.setString(1, pincode);
            ResultSet pinRs = pinPst.executeQuery();

            if (pinRs.next()) {
                city = pinRs.getString("city_name");
                state = pinRs.getString("state_name");
                country = pinRs.getString("country_name");

                System.out.println("\n--- Location Auto-Patched ---");
                System.out.println("City    : " + city);
                System.out.println("State   : " + state);
                System.out.println("Country : " + country);
                System.out.println("-----------------------------\n");
                break;
            } else {
                System.out.println("\nPincode not found in database.");
                System.out.println("1. Enter details manually");
                System.out.println("2. Try another Pincode");
                System.out.print("Select an option (1 or 2): ");

                String opt = sc.nextLine().trim();
                if (opt.equals("1")) {
                    while (true) {
                        System.out.print("Enter City: ");
                        city = sc.nextLine().trim();
                        System.out.print("Enter State: ");
                        state = sc.nextLine().trim();
                        System.out.print("Enter Country: ");
                        country = sc.nextLine().trim();

                        if (!city.isBlank() && !state.isBlank() && !country.isBlank()) {
                            break;
                        }
                        System.out.println("City, State, and Country fields cannot be empty! Please enter all details.\n");
                    }
                    break; // Exit location loop after manual entry
                } else {
                    System.out.println("Please enter a valid pincode.\n");
                }
            }
        }

        // 6. Insert User Record into Database
        String userEntry = "INSERT INTO USER (`User_name`, `Password`, `MO`, `Email`, `Country`, `State`, `City`, `Pincode`) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(userEntry);
        ps.setString(1, username);
        ps.setString(2, password);
        ps.setLong(3, mo);
        ps.setString(4, email);
        ps.setString(5, country);
        ps.setString(6, state);
        ps.setString(7, city);
        ps.setString(8, pincode);

        int r = ps.executeUpdate();
        if (r != 0) {
            System.out.println("Account Created Successfully!");
            try (FileOutputStream fos = new FileOutputStream("Log.txt", true)) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                fos.write(("[" + timestamp + "] User Registered: " + username + "\n").getBytes());
            }
        } else {
            System.out.println("Registration Failed. Please try again.");
        }
    }
}