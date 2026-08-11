package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Bus extends connection implements Manageable {
    String from = "", to = "", b_Type = "";
    int choice, bID;
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

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM buses");

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\t\t\tTo\t\t\tType\t\tPrice\tDeparture\t\tJourney(Hrs)\tStation\t\tBus No.\tTickets");
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
                            rs.getInt(9) + "\t" +
                            rs.getInt(10)
            );
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        System.out.println("\n--- ADD BUS ---");
        System.out.println("1. Single Manual Entry");
        System.out.println("2. Bulk Upload via CSV File");
        System.out.println("3. Back");

        int mode = new Methods().readValidInt("Choice: ");
        if (mode == 2) {
            uploadBusFile();
            return;
        } else if (mode == 3 || mode == 0) {
            return;
        } else if (mode != 1) {
            System.out.println("Invalid Choice.");
            return;
        }

        // Manual Entry
        while (true) {
            System.out.println("\nBus type :-");
            System.out.println("1. Intercity");
            System.out.println("2. Interstate");
            System.out.println("3. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 3) {
                break;
            } else {
                System.out.println("Invalid Choice....");
            }
        }

        switch (choice) {
            case 1 -> {
                b_Type = "Intercity";
                while (true) {
                    from = resolveLocation("Departure (From)");
                    if (from == null) return;

                    to = resolveLocation("Destination (To)");
                    if (to == null) return;

                    if (from.equalsIgnoreCase(to)) {
                        System.out.println("From and To locations cannot be the same!");
                        continue;
                    }
                    break;
                }
            }

            case 2 -> {
                b_Type = "Interstate";

                while (true) {
                    from = resolveLocation("Departure State/City (From)");
                    if (from == null) return;

                    to = resolveLocation("Destination State/City (To)");
                    if (to == null) return;

                    if (from.equalsIgnoreCase(to)) {
                        System.out.println("From and To locations cannot be the same!");
                        continue;
                    }
                    break;
                }
            }
            case 3 -> {
                return;
            }
            default -> System.out.println("Invalid Choice....");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        LocalDateTime departure;

        while (true) {
            System.out.print("Date of Bus Boarding (dd-MM-yyyy): ");
            String date = sc.nextLine().trim();

            System.out.print("Time of Bus Boarding (HH:mm): ");
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

        float jTime = new Methods().readValidFloat("Time of Journey (in hours): ");

        System.out.print("Station Name :");
        String sName = sc.nextLine().trim();

        int bn = new Methods().readValidInt("Bus Number : ");

        int tickets = new Methods().readValidInt("Available Tickets : ");

        String sql = "INSERT INTO BUSES (`B_From`, `B_To`, `B_Type`, `price`, `Departure_Time`, " +
                "`Journey_Time(in hours)`, `Station_Name`, `Bus_Number`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setString(1, from);
        pt.setString(2, to);
        pt.setString(3, b_Type);
        pt.setInt(4, price);
        pt.setObject(5, departure);
        pt.setFloat(6, jTime);
        pt.setString(7, sName);
        pt.setInt(8, bn);
        pt.setInt(9, tickets);

        int r = pt.executeUpdate();
        System.out.println(r != 0 ? "Bus Added Successfully!" : "Failed to Add Bus");
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            bID = new Methods().readValidInt("Enter Bus ID : ");

            String sql = "SELECT `bus_id`, `price`, `Departure_Time`, `Station_Name`, `Bus_Number`, `Available_Tickets` FROM BUSES WHERE BUS_ID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, bID);
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
                                System.out.print("Date of Bus Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine().trim();

                                System.out.print("Time of Bus Departure (HH:mm): ");
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
                            int pf = new Methods().readValidInt("New Bus No. : ");
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

                    String fSql = "UPDATE `BUSES` SET `" + rsm.getColumnName(col) + "` = ? WHERE BUS_ID = ?";
                    PreparedStatement uSt = con.prepareStatement(fSql);
                    uSt.setString(1, n);
                    uSt.setInt(2, bID);

                    try {
                        uSt.executeUpdate();
                        System.out.println("Bus Details Updated.");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            } else {
                System.out.println("Invalid Bus Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int bId = new Methods().readValidInt("Enter Bus Id to Delete (or 0 to cancel): ");
            if (bId == 0) return;

            String checkSql = "SELECT * FROM `buses` WHERE bus_id = ?";
            PreparedStatement checkSt = con.prepareStatement(checkSql);
            checkSt.setInt(1, bId);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE bus_booking SET status = 'CANCELED', bus_id = NULL WHERE bus_id = ?"
                    );
                    cancelBookings.setInt(1, bId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE B_ID = ?");
                    delCabs.setInt(1, bId);
                    delCabs.executeUpdate();

                    PreparedStatement delBus = con.prepareStatement("DELETE FROM `buses` WHERE bus_id = ?");
                    delBus.setInt(1, bId);
                    int r = delBus.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Bus Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Bus.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting bus: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Bus Id");
            }
        }
    }

    // ================= BATCH FILE UPLOAD =================
    public void uploadBusFile() {
        System.out.print("Enter full path of the CSV file (e.g., C:/data/buses.csv): ");
        String filePath = sc.nextLine().trim();

        String insertSql = "INSERT INTO BUSES (`B_From`, `B_To`, `B_Type`, `price`, `Departure_Time`, " +
                "`Journey_Time(in hours)`, `Station_Name`, `Bus_Number`, `Available_Tickets`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int successCount = 0;
        int failCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pst = con.prepareStatement(insertSql)) {

            String line;
            boolean isHeader = true;

            boolean autoCommitState = con.getAutoCommit();
            con.setAutoCommit(false); // Enable batch transaction

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 9) {
                    System.out.println("Skipping malformed row: " + line);
                    failCount++;
                    continue;
                }

                try {
                    String fromStr = data[0].trim();
                    String toStr = data[1].trim();
                    String typeStr = data[2].trim();
                    int priceVal = Integer.parseInt(data[3].trim());

                    LocalDateTime departure = LocalDateTime.parse(data[4].trim(), formatter);
                    float journeyTime = Float.parseFloat(data[5].trim());
                    String stationName = data[6].trim();
                    int busNo = Integer.parseInt(data[7].trim());
                    int tickets = Integer.parseInt(data[8].trim());

                    pst.setString(1, fromStr);
                    pst.setString(2, toStr);
                    pst.setString(3, typeStr);
                    pst.setInt(4, priceVal);
                    pst.setObject(5, departure);
                    pst.setFloat(6, journeyTime);
                    pst.setString(7, stationName);
                    pst.setInt(8, busNo);
                    pst.setInt(9, tickets);

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
            System.out.println(" Buses Added Successfully : " + successCount);
            System.out.println(" Failed / Skipped Rows    : " + failCount);
            System.out.println("==========================================\n");

        } catch (Exception e) {
            System.out.println("File upload failed: " + e.getMessage());
        }
    }
}