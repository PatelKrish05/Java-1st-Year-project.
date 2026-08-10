package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Scanner;

public class Room extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, rID, hID, maxP, totalRooms, availableRooms;
    String roomType, n = "";
    BigDecimal pricePerNight;

    private String readRoomType() {
        while (true) {
            System.out.println("\nRoom type :-");
            System.out.println("1. Single Room");
            System.out.println("2. Twin Room");
            System.out.println("3. Triple Room");
            System.out.println("4. Family Room");
            System.out.println("5. Suite Room");

            choice = new Methods().readValidInt("Choice: ");

            switch (choice) {
                case 1 -> { maxP = 1; return "Single Room"; }
                case 2 -> { maxP = 2; return "Twin Room"; }
                case 3 -> { maxP = 3; return "Triple Room"; }
                case 4 -> { maxP = 4; return "Family Room"; }
                case 5 -> { maxP = 5; return "Suite Room"; }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    private int readTotalRooms() {
        return readTotalRooms(1);
    }

    private int readTotalRooms(int min) {
        while (true) {
            System.out.print("Total Rooms : ");
            try {
                int t = Integer.parseInt(sc.nextLine().trim());
                if (t < min) {
                    System.out.println("Total Rooms must be at least " + min);
                    continue;
                }
                return t;
            } catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private int readAvailableRooms(int cap) {
        while (true) {
            System.out.print("Available Rooms : ");
            try {
                int a = Integer.parseInt(sc.nextLine().trim());
                if (a < 0 || a > cap) {
                    System.out.println("Available Rooms must be between 0 and " + cap);
                    continue;
                }
                return a;
            } catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private int readMaxPersons() {
        while (true) {
            System.out.print("Max Persons : ");
            try {
                int m = Integer.parseInt(sc.nextLine().trim());
                if (m <= 0) {
                    System.out.println("Max Persons must be greater than 0");
                    continue;
                }
                return m;
            } catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private BigDecimal readPrice() {
        while (true) {
            System.out.print("Price per Night : ");
            try {
                BigDecimal p = new BigDecimal(sc.nextLine().trim());
                if (p.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Price must be greater than 0");
                    continue;
                }
                return p;
            } catch (Exception e) {
                System.out.println("Invalid Price");
            }
        }
    }

    // Resolves Hotel by Hotel ID, City Name, or Pincode
    private int readHotelId() throws Exception {
        while (true) {
            System.out.print("Hotel ID / City Name / Pincode : ");
            String input = sc.nextLine().trim();
            if (input.isBlank()) return 0;

            // 1. Pincode Lookup
            if (input.matches("\\d{6}")) {
                String pinSql = "SELECT h.hotel_id, h.hotel_name, v.city_name " +
                        "FROM hotels h JOIN view_pincode_location v ON h.city_id = v.city_id " +
                        "WHERE v.pincode = ? LIMIT 1";
                try (PreparedStatement pst = con.prepareStatement(pinSql)) {
                    pst.setString(1, input);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        int hotelId = rs.getInt("hotel_id");
                        System.out.println("-> Detected Hotel: " + rs.getString("hotel_name") + " (" + rs.getString("city_name") + ")");
                        return hotelId;
                    }
                }
            }

            // 2. Direct Hotel ID
            if (input.matches("\\d+")) {
                int h = Integer.parseInt(input);
                String fSql = "SELECT hotel_name FROM hotels WHERE hotel_id = ?";
                try (PreparedStatement pst = con.prepareStatement(fSql)) {
                    pst.setInt(1, h);
                    ResultSet Frs = pst.executeQuery();
                    if (Frs.next()) {
                        System.out.println("-> Selected Hotel: " + Frs.getString("hotel_name"));
                        return h;
                    }
                }
            }

            // 3. City Name Lookup for Hotels
            String citySql = "SELECT h.hotel_id, h.hotel_name FROM hotels h " +
                    "JOIN cities c ON h.city_id = c.city_id WHERE LOWER(c.city_name) = LOWER(?) LIMIT 1";
            try (PreparedStatement pst = con.prepareStatement(citySql)) {
                pst.setString(1, input);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    int hotelId = rs.getInt("hotel_id");
                    System.out.println("-> Selected Hotel: " + rs.getString("hotel_name"));
                    return hotelId;
                }
            }

            System.out.println("Hotel / Location Not Available");
            System.out.println("1. View All Hotels");
            System.out.println("2. Try Another");
            choice = new Methods().readValidInt("Choice: ");

            switch (choice) {
                case 1 -> new Hotel().view();
                case 2 -> { }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    @Override
    public void view() throws Exception {
        new Hotel().view();
        int h = readHotelId();
        if (h == 0) return;

        String sql = "SELECT * FROM rooms WHERE hotel_id = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, h);
        ResultSet rs = stmt.executeQuery();

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("ID\tHotel ID\tRoom Type\tTotal\tAvailable\tMax Persons\tPrice/Night");
        System.out.println("------------------------------------------------------------------------------------------------");

        boolean hasRooms = false;
        while (rs.next()) {
            hasRooms = true;
            System.out.println(
                    rs.getInt("room_id") + "\t" +
                            rs.getInt("hotel_id") + "\t\t" +
                            rs.getString("room_type") + "\t\t" +
                            rs.getInt("total_rooms") + "\t" +
                            rs.getInt("available_rooms") + "\t\t" +
                            rs.getInt("max_persons") + "\t\t$" +
                            rs.getBigDecimal("price_per_night")
            );
        }

        if (!hasRooms) {
            System.out.println("No rooms configured for this hotel yet.");
        }

        System.out.println("------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        new Hotel().view();
        hID = readHotelId();
        if (hID == 0) return;

        roomType = readRoomType();
        totalRooms = readTotalRooms();
        availableRooms = readAvailableRooms(totalRooms);
        pricePerNight = readPrice();

        String insert = "INSERT INTO `rooms`(`hotel_id`, `room_type`, `total_rooms`, `available_rooms`, `max_persons`, `price_per_night`)" +
                " VALUES (?,?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insert);
        pst.setInt(1, hID);
        pst.setString(2, roomType);
        pst.setInt(3, totalRooms);
        pst.setInt(4, availableRooms);
        pst.setInt(5, maxP);
        pst.setBigDecimal(6, pricePerNight);
        int rs = pst.executeUpdate();
        System.out.println(rs > 0 ? "Room Added Successfully!" : "Failed to add Room");
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            rID = new Methods().readValidInt("Enter Room ID to Edit (or 0 to cancel): ");
            if (rID == 0) return;

            String sql = "SELECT `room_id`,`room_type`,`total_rooms`,`available_rooms`,`max_persons`,`price_per_night` FROM `rooms` WHERE room_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, rID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();

            if (rs.next()) {
                int currentTotal = rs.getInt(3);
                int currentAvailable = rs.getInt(4);
                System.out.println("\nID : " + rs.getInt(1));
                System.out.println("1. " + rsm.getColumnName(2) + " = " + rs.getObject(2));
                System.out.println("2. " + rsm.getColumnName(3) + " = " + rs.getObject(3));
                System.out.println("3. " + rsm.getColumnName(4) + " = " + rs.getObject(4));
                System.out.println("4. " + rsm.getColumnName(5) + " = " + rs.getObject(5));
                System.out.println("5. " + rsm.getColumnName(6) + " = " + rs.getObject(6));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    int col = 0;
                    switch (choice) {
                        case 1 -> {
                            n = readRoomType();
                            col = 2;
                        }
                        case 2 -> {
                            int t = readTotalRooms(currentAvailable);
                            n = "" + t;
                            col = 3;
                        }
                        case 3 -> {
                            int a = readAvailableRooms(currentTotal);
                            n = "" + a;
                            col = 4;
                        }
                        case 4 -> {
                            int m = readMaxPersons();
                            n = "" + m;
                            col = 5;
                        }
                        case 5 -> {
                            BigDecimal p = readPrice();
                            n = p.toString();
                            col = 6;
                        }
                        default -> {
                            System.out.println("Invalid Choice");
                            continue;
                        }
                    }

                    String fSql = "UPDATE `rooms` SET `" + rsm.getColumnName(col) + "` = ? WHERE room_id = ?";
                    PreparedStatement upst = con.prepareStatement(fSql);
                    upst.setString(1, n);
                    upst.setInt(2, rID);
                    int r = upst.executeUpdate();
                    System.out.println(r > 0 ? "Room Updated Successfully!" : "Failed to Update Room.");
                    break;
                }
                break;
            } else {
                System.out.println("Invalid Room Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            rID = new Methods().readValidInt("Enter Room ID to Delete (or 0 to cancel): ");
            if (rID == 0) return;

            PreparedStatement checkSt = con.prepareStatement("SELECT room_type FROM rooms WHERE room_id = ?");
            checkSt.setInt(1, rID);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                String csql = "DELETE FROM `rooms` WHERE `room_id` = ?";
                PreparedStatement pst = con.prepareStatement(csql);
                pst.setInt(1, rID);
                int r = pst.executeUpdate();
                System.out.println(r > 0 ? "Room Deleted Successfully!" : "Failed to Delete Room.");
                break;
            } else {
                System.out.println("Invalid Room ID.");
            }
        }
    }
}