package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;
import Data_Structure.*;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
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

    // ================= STRICT STATE, CITY & PINCODE RESOLUTION =================
    private String resolveLocation(String message) throws Exception {
        while (true) {
            System.out.println("\n--- " + message.toUpperCase() + " LOCATION ---");
            System.out.print("Enter State Name or 6-Digit Pincode: ");
            String input = sc.nextLine().trim();

            if (input.isBlank()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            // 1. PINCODE AUTO-FETCH (Extracts verified State and City)
            if (input.matches("\\d+")) {
                if (!input.matches("\\d{6}")) {
                    System.out.println("Invalid pincode length! Pincodes must be exactly 6 digits. Try again.");
                    continue;
                }
                String pinSql = "SELECT city_name, state_name FROM view_pincode_location WHERE pincode = ? LIMIT 1";
                try (PreparedStatement pst = con.prepareStatement(pinSql)) {
                    pst.setString(1, input);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String detectedCity = rs.getString("city_name");
                            String detectedState = rs.getString("state_name");
                            System.out.println("-> Detected Location: " + detectedState + ", " + detectedCity);
                            return detectedState + ", " + detectedCity;
                        } else {
                            System.out.println("Pincode '" + input + "' not found in database. Try a valid Pincode or State Name.\n");
                            continue;
                        }
                    }
                }
            }

            // 2. STRICT STATE NAME VALIDATION
            String stateName = null;
            int stateId = 0;
            String stateSql = "SELECT state_id, state_name FROM states WHERE LOWER(state_name) = LOWER(?) LIMIT 1";
            try (PreparedStatement pst = con.prepareStatement(stateSql)) {
                pst.setString(1, input);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        stateId = rs.getInt("state_id");
                        stateName = rs.getString("state_name");
                    }
                }
            }

            if (stateName == null) {
                System.out.println("Invalid State: '" + input + "' not found in database. Try again.");
                continue;
            }

            // 3. STRICT CITY NAME VALIDATION UNDER STATE
            while (true) {
                System.out.print("Enter City Name for " + stateName + " (or 0 to change State): ");
                String cityInput = sc.nextLine().trim();

                if (cityInput.equals("0")) break;

                if (cityInput.isBlank()) {
                    System.out.println("City name cannot be blank. Try again.");
                    continue;
                }

                String cityName = null;
                String citySql = "SELECT city_name FROM cities WHERE LOWER(city_name) = LOWER(?) AND state_id = ? LIMIT 1";

                try (PreparedStatement pst = con.prepareStatement(citySql)) {
                    pst.setString(1, cityInput);
                    pst.setInt(2, stateId);

                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            cityName = rs.getString("city_name");
                        }
                    }
                }

                if (cityName != null) {
                    System.out.println("-> Selected Location: " + stateName + ", " + cityName);
                    return stateName + ", " + cityName;
                } else {
                    System.out.println("Invalid City: '" + cityInput + "' is not a registered city under " + stateName + ". Try again.");
                }
            }
        }
    }

    public void view() throws Exception {
        String sql = "SELECT * FROM flights ORDER BY Flight_id";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("ID\tFrom\t\t\tTo\t\t\tType\t\tPrice\tBoarding\t\tJourney Time\tDeparture\tAvailable_Tickets");
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
                                rs.getTimestamp(8) + "\t" +
                                rs.getInt(9));
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        }
    }

    @Override
    public void add() throws Exception {
        System.out.println("\n--- ADD FLIGHT ---");
        System.out.println("1. Single Manual Entry");
        System.out.println("2. Bulk Upload via CSV File");
        System.out.println("3. Back");

        int mode = new Methods().readValidInt("Choice: ");
        if (mode == 2) {
            uploadFlightFile();
            return;
        } else if (mode == 3 || mode == 0) {
            return;
        } else if (mode != 1) {
            System.out.println("Invalid Choice.");
            return;
        }

        // Manual Insertion Flow
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

        // 1. Domestic Flights (State -> City / Pincode Input)
        if (choice == 1) {
            f_Type = "Domestic";
            while (true) {
                from = resolveLocation("Departure (From)");
                if (from == null) return;

                to = resolveLocation("Destination (To)");
                if (to == null) return;

                if (from.equalsIgnoreCase(to)) {
                    System.out.println("Departure and Destination locations cannot be the same!");
                    continue;
                }
                break;
            }
        }

        // 2. International & Private Flights
        else if (choice == 2 || choice == 3) {
            f_Type = (choice == 2) ? "International" : "Private";
            String f_CFrom, f_SFrom, f_CTo, f_STo;

            while (true) {
                System.out.println("\nFrom :-");
                System.out.print("Country : ");
                f_CFrom = sc.nextLine().trim();
                f_SFrom = resolveLocation("Departure State/City");
                if (f_SFrom == null) return;

                if (!f_CFrom.isBlank()) {
                    from = f_CFrom + ", " + f_SFrom;
                    break;
                }
                System.out.println("Country cannot be empty.");
            }

            while (true) {
                System.out.println("\nTo :-");
                System.out.print("Country : ");
                f_CTo = sc.nextLine().trim();
                f_STo = resolveLocation("Destination State/City");
                if (f_STo == null) return;

                if (!f_CTo.isBlank()) {
                    to = f_CTo + ", " + f_STo;
                    if (from.equalsIgnoreCase(to)) {
                        System.out.println("Departure and Destination cannot be the same!");
                        continue;
                    }
                    break;
                }
                System.out.println("Country cannot be empty.");
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

        float jTime = new Methods().readValidFloat("Time of Journey (in hours): ");
        int tickets = new Methods().readValidInt("Available Tickets : ");
        int price = new Methods().readValidInt("Price : ");

        boolean autoCommitState = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            String sql = "INSERT INTO flights (`F_From`, `F_To`, `F_Type`, `price`, " +
                    "`Boarding_Time`, `Journey_Time(in hours)`, `Departure_Time`, `Available_Tickets`) " +
                    "VALUES (?,?,?,?,?,?,?,?)";

            try (PreparedStatement pt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                    try (ResultSet rs = pt.getGeneratedKeys()) {
                        if (rs.next()) {
                            f.add(fGen(rs.getInt(1), tickets));
                        }
                    }
                    con.commit();
                    System.out.println("Flight Added Successfully!");
                } else {
                    if (!con.getAutoCommit()) con.rollback();
                    System.out.println("Failed to Add Flight.");
                }
            }
        } catch (Exception e) {
            if (con != null && !con.getAutoCommit()) {
                con.rollback();
            }
            System.out.println("Error adding flight: " + e.getMessage());
        } finally {
            if (con != null) {
                con.setAutoCommit(autoCommitState);
            }
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            fID = new Methods().readValidInt("Enter Flight ID : ");

            String sql = "SELECT `FLIGHT_ID`, `price`,`Boarding_Time`,`Departure_Time`,`Available_Tickets` FROM FLIGHTS WHERE FLIGHT_ID = ?";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, fID);
                try (ResultSet rs = pst.executeQuery()) {
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
                            try (PreparedStatement uSt = con.prepareStatement(fSql)) {
                                uSt.setString(1, n);
                                uSt.setInt(2, fID);
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
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int fId = new Methods().readValidInt("Enter Flight Id to Delete (or 0 to cancel): ");
            if (fId == 0) return;

            String fc = "SELECT * FROM `flights` WHERE FLIGHT_ID = ?";
            try (PreparedStatement fst = con.prepareStatement(fc)) {
                fst.setInt(1, fId);
                try (ResultSet fcr = fst.executeQuery()) {

                    if (fcr.next()) {
                        boolean autoCommitState = con.getAutoCommit();
                        try {
                            con.setAutoCommit(false);

                            try (PreparedStatement cancelBookings = con.prepareStatement(
                                    "UPDATE flight_booking SET status = 'CANCELED', flight_id = NULL WHERE flight_id = ?")) {
                                cancelBookings.setInt(1, fId);
                                int affectedUsers = cancelBookings.executeUpdate();

                                try (PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE F_ID = ?")) {
                                    delCabs.setInt(1, fId);
                                    delCabs.executeUpdate();
                                }

                                try (PreparedStatement delPkgTrans = con.prepareStatement("DELETE FROM packages_transports WHERE flight_id = ?")) {
                                    delPkgTrans.setInt(1, fId);
                                    delPkgTrans.executeUpdate();
                                }

                                try (PreparedStatement delFlight = con.prepareStatement("DELETE FROM flights WHERE FLIGHT_ID = ?")) {
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
                                        if (!con.getAutoCommit()) con.rollback();
                                        System.out.println("Failed to Delete Flight.");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (con != null && !con.getAutoCommit()) {
                                con.rollback();
                            }
                            System.out.println("Error deleting flight: " + e.getMessage());
                        } finally {
                            if (con != null) {
                                con.setAutoCommit(autoCommitState);
                            }
                        }
                        break;
                    } else {
                        System.out.println("Invalid Flight Id");
                    }
                }
            }
        }
    }

    String[] fGen(int id, int max) throws Exception {
        String[] tickets = new String[max];
        st = new Stack(max);

        String sql = "SELECT `Flight_id`, `F_From`, `F_To`, `Boarding_Time`, `Available_Tickets` FROM `flights` WHERE Flight_id = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
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

    // ================= BATCH FILE UPLOAD =================
    public void uploadFlightFile() {
        System.out.print("Enter full path of the CSV file (e.g., C:/data/flights.csv): ");
        String filePath = sc.nextLine().trim();

        String insertSql = "INSERT INTO flights (`F_From`, `F_To`, `F_Type`, `price`, " +
                "`Boarding_Time`, `Journey_Time(in hours)`, `Departure_Time`, `Available_Tickets`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int successCount = 0;
        int failCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pst = con.prepareStatement(insertSql)) {

            String line;
            boolean isHeader = true;

            boolean autoCommitState = con.getAutoCommit();
            con.setAutoCommit(false);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 8) {
                    System.out.println("Skipping malformed row: " + line);
                    failCount++;
                    continue;
                }

                try {
                    String from = data[0].trim();
                    String to = data[1].trim();
                    String type = data[2].trim();
                    double price = Double.parseDouble(data[3].trim());

                    LocalDateTime boarding = LocalDateTime.parse(data[4].trim(), formatter);
                    float journeyTime = Float.parseFloat(data[5].trim());
                    LocalDateTime departure = LocalDateTime.parse(data[6].trim(), formatter);
                    int tickets = Integer.parseInt(data[7].trim());

                    pst.setString(1, from);
                    pst.setString(2, to);
                    pst.setString(3, type);
                    pst.setDouble(4, price);
                    pst.setObject(5, boarding);
                    pst.setFloat(6, journeyTime);
                    pst.setObject(7, departure);
                    pst.setInt(8, tickets);

                    pst.addBatch();
                    successCount++;

                } catch (Exception e) {
                    System.out.println("Error parsing row [" + line + "]: " + e.getMessage());
                    failCount++;
                }
            }

            pst.executeBatch();
            con.commit();
            con.setAutoCommit(autoCommitState);

            System.out.println("\n==========================================");
            System.out.println(" BATCH UPLOAD COMPLETE!");
            System.out.println(" Flights Added Successfully : " + successCount);
            System.out.println(" Failed / Skipped Rows     : " + failCount);
            System.out.println("==========================================\n");

        } catch (Exception e) {
            System.out.println("File upload failed: " + e.getMessage());
        }
    }
}