package Admin;

import JDBC.connection;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Hotel extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, hID;
    String hName, hCity, hType, address, contactNo, email, description, n = "";
    double rating;
    long cID;


    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM hotels");

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tHotel Name\t\tCity ID\tType\t\tRating\tCheck-In\tCheck-Out\tContact\t\tEmail\t\t\tAddress");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("hotel_id") + "\t" +
                            rs.getString("hotel_name") + "\t\t" +
                            rs.getLong("city_id") + "\t" +
                            rs.getString("hotel_type") + "\t\t" +
                            rs.getBigDecimal("rating") + "\t" +
                            rs.getTime("check_in") + "\t" +
                            rs.getTime("check_out") + "\t" +
                            rs.getString("contact_no") + "\t" +
                            rs.getString("email") + "\t" +
                            rs.getString("address")
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.print("City : ");
            hCity = sc.nextLine();
            String fSql = "SELECT * FROM cities WHERE city_name = ?";
            PreparedStatement pst = con.prepareStatement(fSql);
            pst.setString(1, hCity);
            ResultSet Frs = pst.executeQuery();
            if (Frs.next()) {
                cID = Frs.getLong(1);
                break;
            }
            else {
                System.out.println("City Not Available");
                System.out.println("1. Add City to Database");
                System.out.println("2. Try Another");
                choice = 0;
                try {
                    choice = sc.nextInt();
                    sc.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid Choice");
                    sc.nextLine();
                }
                switch (choice) {
                    case 1 -> new City().add();
                    case 2 -> {
                        continue;
                    }
                    default -> System.out.println("Invalid Choice");
                }
            }
        }

        System.out.print("Hotel Name : ");
        hName = sc.nextLine();

        System.out.print("Address : ");
        address = sc.nextLine();

        hType = readHotelType();

        while (true) {
            System.out.print("Rating (0.0 - 5.0) : ");
            try {
                rating = Double.parseDouble(sc.nextLine());
                if (rating < 0.0 || rating > 5.0) {
                    System.out.println("Rating must be between 0.0 and 5.0");
                    continue;
                }
                break;
            }
            catch (Exception e) {
                System.out.println("Invalid Rating");
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime checkIn;

        while (true) {
            System.out.print("Check-In Time (HH:mm): ");
            String input = sc.nextLine();

            try {
                checkIn = LocalTime.parse(input, formatter);
                break;
            } catch (Exception e) {
                System.out.println("Invalid Time");
            }
        }

        LocalTime checkOut;

        while (true) {
            System.out.print("Check-Out Time (HH:mm): ");
            String input = sc.nextLine();

            try {
                checkOut = LocalTime.parse(input, formatter);
                break;
            } catch (Exception e) {
                System.out.println("Invalid Time");
            }
        }

        contactNo = readContactNo();

        email = readEmail();

        System.out.print("Description : ");
        description = sc.nextLine();

        String insert = "INSERT INTO `hotels`(`hotel_name`, `city_id`, `address`, `hotel_type`, `rating`, `check_in`, `check_out`, `contact_no`, `email`, `description`)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insert);
        pst.setString(1, hName);
        pst.setLong(2, cID);
        pst.setString(3, address);
        pst.setString(4, hType);
        pst.setDouble(5, rating);
        pst.setObject(6, checkIn);
        pst.setObject(7, checkOut);
        pst.setString(8, contactNo);
        pst.setString(9, email);
        pst.setString(10, description);
        int rs = pst.executeUpdate();
        System.out.println(rs>0?"Hotel Added":"Failed to add Hotel");
    }

    @Override
    public void edit() throws Exception {

        view();
        while (true) {
            System.out.println("Enter Hotel ID : ");
            hID = sc.nextInt();
            sc.nextLine();
            String sql = "SELECT `hotel_id`,`address`,`hotel_type`,`rating`,`check_in`,`check_out`,`contact_no`,`email`,`description` FROM `hotels` WHERE hotel_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, hID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            if (rs.next()) {
                System.out.println("ID : "+rs.getInt(1));
                System.out.println("1."+rsm.getColumnName(2)+" = "+rs.getObject(2));
                System.out.println("2."+rsm.getColumnName(3)+" = "+rs.getObject(3));
                System.out.println("3."+rsm.getColumnName(4)+" = "+rs.getObject(4));
                System.out.println("4."+rsm.getColumnName(5)+" = "+rs.getObject(5));
                System.out.println("5."+rsm.getColumnName(6)+" = "+rs.getObject(6));
                System.out.println("6."+rsm.getColumnName(7)+" = "+rs.getObject(7));
                System.out.println("7."+rsm.getColumnName(8)+" = "+rs.getObject(8));
                System.out.println("8."+rsm.getColumnName(9)+" = "+rs.getObject(9));
                System.out.println("Enter Column  number to edit : ");
                choice = 0;
                while (true) {

                    try {
                        choice = sc.nextInt();
                        sc.nextLine();
                    }
                    catch (Exception e) {
                        System.out.println("Invalid Choice");
                        sc.nextLine();
                        continue;
                    }

                    int col = 0;
                    switch (choice) {
                        case 1 -> {
                            System.out.print("New Address : ");
                            n = sc.nextLine();
                            col = 2;
                        }
                        case 2 -> {
                            n = readHotelType();
                            col = 3;
                        }
                        case 3 -> {
                            while (true) {
                                System.out.print("New Rating (0.0 - 5.0) : ");
                                try {
                                    double r = Double.parseDouble(sc.nextLine());
                                    if (r < 0.0 || r > 5.0) {
                                        System.out.println("Rating must be between 0.0 and 5.0");
                                        continue;
                                    }
                                    n = "" + r;
                                    col = 4;
                                    break;
                                }
                                catch (Exception e) {
                                    System.out.println("Invalid Rating");
                                }
                            }
                        }
                        case 4 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                            LocalTime checkIn;

                            while (true) {
                                System.out.print("Check-In Time (HH:mm): ");
                                String input = sc.nextLine();

                                try {
                                    checkIn = LocalTime.parse(input, formatter);
                                    n= checkIn.toString();
                                    col = 5;
                                    break;
                                }
                                catch (Exception e) {
                                    System.out.println("Invalid Time");
                                }
                            }
                        }
                        case 5 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                            LocalTime checkOut;

                            while (true) {
                                System.out.print("Check-Out Time (HH:mm): ");
                                String input = sc.nextLine();

                                try {
                                    checkOut = LocalTime.parse(input, formatter);
                                    n= checkOut.toString();
                                    col = 6;
                                    break;
                                }
                                catch (Exception e) {
                                    System.out.println("Invalid Time");
                                }
                            }
                        }
                        case 6 -> {
                            n = readContactNo();
                            col = 7;
                        }
                        case 7 -> {
                            n = readEmail();
                            col = 8;
                        }
                        case 8 -> {
                            System.out.print("New Description : ");
                            n = sc.nextLine();
                            col = 9;
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }
                    String fSql = "UPDATE `hotels` SET `" + rsm.getColumnName(col) + "` = ? WHERE hotel_id = ?";
                    PreparedStatement upst = con.prepareStatement(fSql);
                    upst.setString(1, n);
                    upst.setInt(2, hID);
                    int r = upst.executeUpdate();
                    System.out.println(r>0?"Hotel Updated Successfully":"Failed to Update Hotel");
                    break;
                }
                break;
            }
            else {
                System.out.println("Invalid Hotel Id");
            }
        }

    }

    @Override
    public void delete() throws Exception {
        view();
        System.out.println("Enter Hotel Id : ");
        int hID = sc.nextInt();
        String csql = "DELETE FROM `hotels` WHERE `hotel_id` = ?";
        PreparedStatement pst = con.prepareStatement(csql);
        pst.setInt(1, hID);
        int r = pst.executeUpdate();
        System.out.println(r>0?"Hotel Deleted Successfully":"Failed to Delete Hotel");
    }


    private String readHotelType() {
        while (true) {
            System.out.println("Hotel type :-");
            System.out.println("1. Budget");
            System.out.println("2. Standard");
            System.out.println("3. Deluxe");
            System.out.println("4. Resort");
            System.out.println("5. Suite");
            choice = 0;
            try {
                choice = sc.nextInt();
                sc.nextLine();
            }
            catch (Exception e) {
                System.out.println("Invalid Choice");
                sc.nextLine();
                continue;
            }
            switch (choice) {
                case 1 -> { return "Budget"; }
                case 2 -> { return "Standard"; }
                case 3 -> { return "Deluxe"; }
                case 4 -> { return "Resort"; }
                case 5 -> { return "Suite"; }
                default -> System.out.println("invalid Choice");
            }
        }
    }

    private String readContactNo() {
        String mNumber;
        while (true) {
            System.out.print("Contact no : ");
            boolean vCheck = true;
            String thing = sc.nextLine();
            char[] thi = thing.toCharArray();
            if(thi.length==10) {
                for (char c : thi) {
                    if (!Character.isDigit(c)) {
                        vCheck = false;
                    }
                }
                if (vCheck) {
                    if (thi[0]=='6'||thi[0]=='7'||thi[0]=='8'||thi[0]=='9') {
                        mNumber=thing;
                       break;
                    }
                    else {
                        System.out.println("Invalid Input, Indian Number Format.");
                        System.out.println("Try again : ");
                    }
                }
                else {
                    System.out.println("Invalid Input, Only Digits Allowed.");
                    System.out.println("Try again : ");
                }
            }
            else {
                System.out.println("Invalid Input, Must contain 10 Digits Only.");
                System.out.println("Try again : ");
            }
        }

        return mNumber;
    }

    private String readEmail() {
        while (true) {
            System.out.print("Email : ");
            String input = sc.nextLine();

            int at = input.indexOf('@');
            int dot = input.lastIndexOf('.');

            if (at <= 0) {
                System.out.println("Invalid Email!!!");
                continue;   // '@' not found or is the first character
            }

            if (dot <= at + 1) {
                System.out.println("Invalid Email!!!");
                continue;   // '.' must come after '@'
            }

            if (dot == input.length() - 1) {
                System.out.println("Invalid Email!!!");
                continue;   // '.' cannot be the last character
            }
            return input;
        }
    }

}