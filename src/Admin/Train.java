package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Train extends connection implements Manageable {
    String from = "", to = "", t_Type = "";
    int choice, tID;
    Scanner sc = new Scanner(System.in);

    // Helper method to resolve City Name or Pincode
    private String resolveCityInput(String prompt) throws Exception {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return null;

            // Pincode Lookup
            if (input.matches("\\d+")) {
                if (!input.matches("\\d{6}")) {
                    System.out.println("Invalid pincode length! Pincodes must be 6 digits. Try again.\n");
                    continue;
                }
                String pinSql = "SELECT city_name FROM view_pincode_location WHERE pincode = ? LIMIT 1";
                try (PreparedStatement pst = con.prepareStatement(pinSql)) {
                    pst.setString(1, input);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        String cityName = rs.getString("city_name");
                        System.out.println("-> Detected City: " + cityName);
                        return cityName;
                    } else {
                        System.out.println("Pincode not found in database. Please enter a valid Pincode or City Name.\n");
                        continue;
                    }
                }
            }

            // City Name Check
            String citySql = "SELECT city_name FROM cities WHERE LOWER(city_name) = LOWER(?) LIMIT 1";
            try (PreparedStatement pst = con.prepareStatement(citySql)) {
                pst.setString(1, input);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return rs.getString("city_name");
                } else {
                    String vSql = "SELECT city_name FROM view_pincode_location WHERE LOWER(city_name) = LOWER(?) LIMIT 1";
                    try (PreparedStatement vPst = con.prepareStatement(vSql)) {
                        vPst.setString(1, input);
                        ResultSet vRs = vPst.executeQuery();
                        if (vRs.next()) {
                            return vRs.getString("city_name");
                        }
                    }

                    System.out.println("City '" + input + "' not found in database.");
                    System.out.println("1. Use typed name anyway");
                    System.out.println("2. Try another City");
                    int opt = new Methods().readValidInt("Choice: ");
                    if (opt == 1) return input;
                }
            }
        }
    }

    @Override
    public void view() throws Exception {
        String sql = "SELECT * FROM trains";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\t\tTo\t\tType\t\tPrice\tDeparture\t\tJourney Time(in hours)\tStation\t\tPlatform\tTickets");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t\t" +
                            rs.getString(3) + "\t\t" +
                            rs.getString(4) + "\t\t" +
                            rs.getInt(5) + "\t" +
                            rs.getTimestamp(6) + "\t" +
                            rs.getFloat(7) + "\t\t" +
                            rs.getString(8) + "\t\t" +
                            rs.getInt(9) + "\t\t" +
                            rs.getInt(10));
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        rs.close();
        st.close();
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.println("\nTrain type :-");
            System.out.println("1. Intercity");
            System.out.println("2. Interstate");
            System.out.println("3. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 3) {
                break;
            } else {
                System.out.println("Invalid Choice");
            }
        }

        if (choice == 3) return;

        // 1. Intercity
        if (choice == 1) {
            t_Type = "Intercity";
            while (true) {
                from = resolveCityInput("From (City / Pincode) : ");
                if (from == null) return;

                to = resolveCityInput("To (City / Pincode) : ");
                if (to == null) return;

                if (from.equalsIgnoreCase(to)) {
                    System.out.println("From and To cities cannot be the same!");
                    continue;
                }
                break;
            }
        }

        // 2. Interstate
        else if (choice == 2) {
            t_Type = "Interstate";
            String t_CFrom, t_SFrom, t_CTo, t_STo;

            while (true) {
                System.out.println("\nFrom :-");
                System.out.print("State : ");
                t_SFrom = sc.nextLine().trim();
                System.out.print("City / Pincode : ");
                t_CFrom = resolveCityInput("City : ");
                if (t_CFrom == null) return;

                if (!t_SFrom.isBlank()) {
                    from = t_SFrom + ", " + t_CFrom;
                    break;
                }
                System.out.println("State cannot be empty.");
            }

            while (true) {
                System.out.println("\nTo :-");
                System.out.print("State : ");
                t_STo = sc.nextLine().trim();
                System.out.print("City / Pincode : ");
                t_CTo = resolveCityInput("City : ");
                if (t_CTo == null) return;

                if (!t_STo.isBlank()) {
                    to = t_STo + ", " + t_CTo;
                    if (from.equalsIgnoreCase(to)) {
                        System.out.println("From and To locations cannot be the same!");
                        continue;
                    }
                    break;
                }
                System.out.println("State cannot be empty.");
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime departure;

        while (true) {
            System.out.print("Date of Train Departure (dd-MM-yyyy): ");
            String date = sc.nextLine().trim();

            System.out.print("Time of Train Departure (HH:mm): ");
            String time = sc.nextLine().trim();

            try {
                departure = LocalDateTime.parse(date + " " + time, formatter);
                if (departure.isAfter(LocalDateTime.now())) {
                    break;
                } else {
                    System.out.println("Boarding date and time must be after the current date and time.");
                }
            } catch (Exception e) {
                System.out.println("Invalid Input Format! Use dd-MM-yyyy HH:mm.");
            }
        }

        int price = new Methods().readValidInt("Price : ");

        System.out.print("Journey_Time(in hours) : ");
        float jTime = sc.nextFloat();
        sc.nextLine();

        System.out.print("Station Name :");
        String sName = sc.nextLine().trim();

        int pn = new Methods().readValidInt("Platform Number : ");

        int tickets = new Methods().readValidInt("Available Tickets : ");

        String sql = "INSERT INTO TRAINS (`T_From`, `T_To`, `T_Type`, `price`, `Departure_Time`, " +
                "`Journey_Time(in hours)`, `Station_Name`, `Platform_Number`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setString(1, from);
        pt.setString(2, to);
        pt.setString(3, t_Type);
        pt.setInt(4, price);
        pt.setObject(5, departure);
        pt.setFloat(6, jTime);
        pt.setString(7, sName);
        pt.setInt(8, pn);
        pt.setInt(9, tickets);

        int r = pt.executeUpdate();
        System.out.println(r != 0 ? "Train Added Successfully!" : "Failed to Add Train");
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            tID = new Methods().readValidInt("Enter Train ID : ");

            String sql = "SELECT `train_id`, `price`, `Departure_Time`, `Station_Name`, `Platform_Number`, `Available_Tickets` FROM TRAINS WHERE TRAIN_ID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, tID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();

            if (rs.next()) {
                System.out.println("ID : " + rs.getInt(1));
                System.out.println("1. " + rsm.getColumnName(2) + " = " + rs.getInt(2));
                System.out.println("2. " + rsm.getColumnName(3) + " = " + rs.getTimestamp(3));
                System.out.println("3. " + rsm.getColumnName(4) + " = " + rs.getString(4));
                System.out.println("4. " + rsm.getColumnName(5) + " = " + rs.getInt(5));
                System.out.println("5. " + rsm.getColumnName(6) + " = " + rs.getInt(6));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    String n = "";
                    int col = 0;

                    switch (choice) {
                        case 1 -> {
                            int p = new Methods().readValidInt("New Price : ");
                            n = "" + p;
                            col = 2;
                        }
                        case 2 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            LocalDateTime departure;

                            while (true) {
                                System.out.print("Date of Train Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine().trim();

                                System.out.print("Time of Train Departure (HH:mm): ");
                                String time = sc.nextLine().trim();

                                try {
                                    departure = LocalDateTime.parse(date + " " + time, formatter);
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Input Format");
                                }
                            }

                            n = departure.toString();
                            col = 3;
                        }
                        case 3 -> {
                            System.out.print("New Station Name : ");
                            n = sc.nextLine().trim();
                            col = 4;
                        }
                        case 4 -> {
                            int pf = new Methods().readValidInt("New Platform No. : ");
                            n = "" + pf;
                            col = 5;
                        }
                        case 5 -> {
                            int t = new Methods().readValidInt("New Ticket Availability : ");
                            n = "" + t;
                            col = 6;
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }

                    String fSql = "UPDATE `TRAINS` SET `" + rsm.getColumnName(col) + "` = ? WHERE TRAIN_ID = ?";
                    PreparedStatement uSt = con.prepareStatement(fSql);
                    uSt.setString(1, n);
                    uSt.setInt(2, tID);

                    try {
                        uSt.executeUpdate();
                        System.out.println("Train Details Updated.");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            } else {
                System.out.println("Invalid Train Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int tId = new Methods().readValidInt("Enter Train Id to Delete (or 0 to cancel): ");
            if (tId == 0) return;

            String checkSql = "SELECT * FROM `trains` WHERE train_id = ?";
            PreparedStatement checkSt = con.prepareStatement(checkSql);
            checkSt.setInt(1, tId);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE train_booking SET status = 'CANCELED', train_id = NULL WHERE train_id = ?"
                    );
                    cancelBookings.setInt(1, tId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE T_ID = ?");
                    delCabs.setInt(1, tId);
                    delCabs.executeUpdate();

                    PreparedStatement delTrain = con.prepareStatement("DELETE FROM `trains` WHERE train_id = ?");
                    delTrain.setInt(1, tId);
                    int r = delTrain.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Train Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Train.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting train: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Train Id");
            }
        }
    }
}