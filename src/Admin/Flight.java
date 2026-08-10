package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;
import Data_Structure.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Flight extends connection implements Manageable {
    public static ArrayList<String[]> f = new ArrayList<>();
    static Stack st;
    String from = "", to = "", f_Type = "";
    int choice, fID;
    Scanner sc = new Scanner(System.in);

    // Helper method to resolve City or Pincode to a valid City Name
    private String resolveCityInput(String prompt) throws Exception {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return null;

            // Pincode Resolution
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

            // City Name Resolution
            String citySql = "SELECT city_name FROM cities WHERE LOWER(city_name) = LOWER(?) LIMIT 1";
            try (PreparedStatement pst = con.prepareStatement(citySql)) {
                pst.setString(1, input);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return rs.getString("city_name");
                } else {
                    // Check view_pincode_location fallback
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

    public void view() throws Exception {
        String sql = "SELECT * FROM flights";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\t\tTo\t\tType\t\tPrice\tBoarding\t\tJourney Time\tDeparture\tAvailable_Tickets\n");
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t\t" +
                            rs.getString(3) + "\t\t" +
                            rs.getString(4) + "\t\t" +
                            rs.getInt(5) + "\t" +
                            rs.getTimestamp(6) + "\t" +
                            rs.getFloat(7) + "\t\t" +
                            rs.getTimestamp(8) + "\t" +
                            rs.getInt(9));
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------");
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.println("\nFlight type :-");
            System.out.println("1. Domestic");
            System.out.println("2. International");
            System.out.println("3. Private");
            System.out.println("4. Back");

            choice = new Methods().readValidInt("Choice: ");
            if (choice >= 1 && choice <= 4) break;
            System.out.println("Invalid Choice");
        }

        if (choice == 4) return;

        // 1. Domestic Flights (City / Pincode Input)
        if (choice == 1) {
            f_Type = "Domestic";
            while (true) {
                from = resolveCityInput("From (City / Pincode): ");
                if (from == null) return;

                to = resolveCityInput("To (City / Pincode): ");
                if (to == null) return;

                if (from.equalsIgnoreCase(to)) {
                    System.out.println("Departure and Destination cities cannot be the same!");
                    continue;
                }
                break;
            }
        }

        // 2. International Flights
        else if (choice == 2 || choice == 3) {
            f_Type = (choice == 2) ? "International" : "Private";
            String f_CFrom, f_SFrom, f_CTo, f_STo;

            while (true) {
                System.out.println("\nFrom :-");
                System.out.print("Country : ");
                f_CFrom = sc.nextLine().trim();
                System.out.print("State / City : ");
                f_SFrom = sc.nextLine().trim();

                if (!f_CFrom.isBlank() && !f_SFrom.isBlank()) {
                    from = f_CFrom + ", " + f_SFrom;
                    break;
                }
                System.out.println("Country and State/City cannot be empty.");
            }

            while (true) {
                System.out.println("\nTo :-");
                System.out.print("Country : ");
                f_CTo = sc.nextLine().trim();
                System.out.print("State / City : ");
                f_STo = sc.nextLine().trim();

                if (!f_CTo.isBlank() && !f_STo.isBlank()) {
                    to = f_CTo + ", " + f_STo;
                    if (from.equalsIgnoreCase(to)) {
                        System.out.println("Departure and Destination cannot be the same!");
                        continue;
                    }
                    break;
                }
                System.out.println("Country and State/City cannot be empty.");
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime boarding;

        while (true) {
            System.out.print("Date of Flight Boarding (dd-MM-yyyy): ");
            String date = sc.nextLine().trim();
            System.out.print("Time of Flight Boarding (HH:mm): ");
            String time = sc.nextLine().trim();

            try {
                boarding = LocalDateTime.parse(date + " " + time, formatter);
                if (boarding.isAfter(LocalDateTime.now())) break;
                System.out.println("Boarding date and time must be after the current date and time.");
            } catch (Exception e) {
                System.out.println("Invalid Date/Time Format! Please use dd-MM-yyyy HH:mm.");
            }
        }

        LocalDateTime departure;

        while (true) {
            System.out.print("Departure Date (dd-MM-yyyy): ");
            String date = sc.nextLine().trim();
            System.out.print("Departure Time (HH:mm): ");
            String time = sc.nextLine().trim();

            try {
                departure = LocalDateTime.parse(date + " " + time, formatter);
                if (departure.isAfter(boarding.plusMinutes(30))) break;
                System.out.println("Departure must be at least 30 minutes after boarding.");
            } catch (Exception e) {
                System.out.println("Invalid Date/Time Format! Please use dd-MM-yyyy HH:mm.");
            }
        }

        System.out.print("Time of Journey (in hours): ");
        float jTime = sc.nextFloat();
        sc.nextLine();

        int tickets = new Methods().readValidInt("Available Tickets : ");
        int price = new Methods().readValidInt("Price : ");

        String sql = "INSERT INTO flights (`F_From`, `F_To`, `F_Type`, `price`, " +
                "`Boarding_Time`, `Journey_Time(in hours)`, `Departure_Time`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setString(1, from);
        pt.setString(2, to);
        pt.setString(3, f_Type);
        pt.setInt(4, price);
        pt.setObject(5, boarding);
        pt.setFloat(6, jTime);
        pt.setObject(7, departure);
        pt.setInt(8, tickets);

        int r = pt.executeUpdate();
        if (r > 0) {
            String idSql = "SELECT MAX(FLIGHT_ID) FROM FLIGHTS";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(idSql);
            if (rs.next()) {
                f.add(fGen(rs.getInt(1), tickets));
            }
            System.out.println("Flight Added Successfully!");
        } else {
            System.out.println("Failed to Add Flight.");
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            fID = new Methods().readValidInt("Enter Flight ID : ");

            String sql = "SELECT `FLIGHT_ID`, `price`,`Boarding_Time`,`Departure_Time`,`Available_Tickets` FROM FLIGHTS WHERE FLIGHT_ID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, fID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();

            if (rs.next()) {
                System.out.println("ID : " + rs.getInt(1));
                System.out.println("1. " + rsm.getColumnName(2) + " = " + rs.getInt(2));
                System.out.println("2. " + rsm.getColumnName(3) + " = " + rs.getTimestamp(3));
                System.out.println("3. " + rsm.getColumnName(4) + " = " + rs.getTimestamp(4));
                System.out.println("4. " + rsm.getColumnName(5) + " = " + rs.getInt(5));

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
                            LocalDateTime boarding;
                            while (true) {
                                System.out.print("Date of Flight Boarding (dd-MM-yyyy): ");
                                String date = sc.nextLine();
                                System.out.print("Time of Flight Boarding (HH:mm): ");
                                String time = sc.nextLine();

                                try {
                                    boarding = LocalDateTime.parse(date + " " + time, formatter);
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Input");
                                }
                            }
                            n = boarding.toString();
                            col = 3;
                        }
                        case 3 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            LocalDateTime departure;
                            while (true) {
                                System.out.print("Date of Flight Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine();
                                System.out.print("Time of Flight Departure (HH:mm): ");
                                String time = sc.nextLine();

                                try {
                                    departure = LocalDateTime.parse(date + " " + time, formatter);
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Input");
                                }
                            }
                            n = departure.toString();
                            col = 4;
                        }
                        case 4 -> {
                            int t = new Methods().readValidInt("New Ticket Availability : ");
                            n = "" + t;
                            col = 5;
                            if (fID - 1 < f.size()) {
                                String[] New = ticketEdit(f.get(fID - 1), t);
                                f.set(fID - 1, New);
                            }
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }

                    String fSql = "UPDATE `flights` SET `" + rsm.getColumnName(col) + "` = ? WHERE FLIGHT_ID = ?";
                    PreparedStatement uSt = con.prepareStatement(fSql);
                    uSt.setString(1, n);
                    uSt.setInt(2, fID);

                    try {
                        uSt.executeUpdate();
                        System.out.println("Flight Details Updated.");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            } else {
                System.out.println("Invalid Flight Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int fId = new Methods().readValidInt("Enter Flight Id to Delete (or 0 to cancel): ");
            if (fId == 0) return;

            String fc = "SELECT * FROM `flights` WHERE FLIGHT_ID = ?";
            PreparedStatement fst = con.prepareStatement(fc);
            fst.setInt(1, fId);
            ResultSet fcr = fst.executeQuery();

            if (fcr.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE flight_booking SET status = 'CANCELED', flight_id = NULL WHERE flight_id = ?"
                    );
                    cancelBookings.setInt(1, fId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE F_ID = ?");
                    delCabs.setInt(1, fId);
                    delCabs.executeUpdate();

                    PreparedStatement delPkgTrans = con.prepareStatement("DELETE FROM packages_transports WHERE flight_id = ?");
                    delPkgTrans.setInt(1, fId);
                    delPkgTrans.executeUpdate();

                    PreparedStatement delFlight = con.prepareStatement("DELETE FROM flights WHERE FLIGHT_ID = ?");
                    delFlight.setInt(1, fId);
                    int r = delFlight.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Flight Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                        if (fId - 1 < f.size()) {
                            f.remove(fId - 1);
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Flight.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting flight: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Flight Id");
            }
        }
    }

    String[] fGen(int id, int max) throws Exception {
        String[] tickets = new String[max];
        st = new Stack(max);

        String sql = "SELECT `Flight_id`, `F_From`, `F_To`, `Boarding_Time`, `Available_Tickets` FROM `flights` WHERE Flight_id = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Timestamp ts = rs.getTimestamp(4);
            int date = ts.toLocalDateTime().getDayOfMonth();
            String fromStr = rs.getString(2).trim();
            String toStr = rs.getString(3).trim();

            char fromChar = !fromStr.isEmpty() ? fromStr.toUpperCase().charAt(0) : 'F';
            char toChar = !toStr.isEmpty() ? toStr.toUpperCase().charAt(0) : 'T';

            String tic = "" + fromChar + toChar + id + date;
            for (int i = 1; i <= max; i++) {
                st.push(tic + "/" + i, tickets);
            }
        }

        try (FileOutputStream fout = new FileOutputStream("Flight Tickets.txt", true)) {
            for (String c : tickets) {
                if (c != null) fout.write((c + " ").getBytes());
            }
            fout.write("\n".getBytes());
        } catch (IOException e) {
            System.out.println("Error writing flight tickets log: " + e.getMessage());
        }

        return tickets;
    }

    String[] ticketEdit(String[] og, int max) {
        String[] temp = new String[max];
        if (og == null || og.length == 0 || og[0] == null) return temp;

        String tic = og[0].substring(0, og[0].indexOf('/'));
        if (og.length >= max) {
            for (int i = 0; i < max; i++) {
                temp[i] = og[i];
            }
        } else {
            System.arraycopy(og, 0, temp, 0, og.length);
            for (int i = og.length; i < max; i++) {
                temp[i] = (tic + "/" + (i + 1));
            }
        }
        return temp;
    }
}