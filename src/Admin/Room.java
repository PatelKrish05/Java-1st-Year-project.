package Admin;

import JDBC.connection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.Scanner;

class Room extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, rID, hID, maxP, totalRooms, availableRooms;
    String roomType, n = "";
    BigDecimal pricePerNight;

    private String readRoomType() {
        while (true) {
            System.out.println("Room type :-");
            System.out.println("1. Single Room");
            System.out.println("2. Twin Room");
            System.out.println("3. Triple Room");
            System.out.println("4. Family Room");
            System.out.println("5. Suite Room");
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
                case 1 -> { maxP = 1; return "Single Room"; }
                case 2 -> { maxP = 2; return "Twin Room"; }
                case 3 -> { maxP = 3; return "Triple Room"; }
                case 4 -> { maxP = 4; return "Family Room"; }
                case 5 -> { maxP = 5; return "Suite Room"; }
                default -> System.out.println("invalid Choice");
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
                int t = Integer.parseInt(sc.nextLine());
                if (t < min) {
                    System.out.println("Total Rooms must be at least " + min);
                    continue;
                }
                return t;
            }
            catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private int readAvailableRooms(int cap) {
        while (true) {
            System.out.print("Available Rooms : ");
            try {
                int a = Integer.parseInt(sc.nextLine());
                if (a < 0 || a > cap) {
                    System.out.println("Available Rooms must be between 0 and " + cap);
                    continue;
                }
                return a;
            }
            catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private int readMaxPersons() {
        while (true) {
            System.out.print("Max Persons : ");
            try {
                int m = Integer.parseInt(sc.nextLine());
                if (m <= 0) {
                    System.out.println("Max Persons must be greater than 0");
                    continue;
                }
                return m;
            }
            catch (Exception e) {
                System.out.println("Invalid Number");
            }
        }
    }

    private BigDecimal readPrice() {
        while (true) {
            System.out.print("Price per Night : ");
            try {
                BigDecimal p = new BigDecimal(sc.nextLine());
                if (p.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Price must be greater than 0");
                    continue;
                }
                return p;
            }
            catch (Exception e) {
                System.out.println("Invalid Price");
            }
        }
    }

    private int readHotelId() throws Exception {
        while (true) {
            System.out.print("Hotel ID : ");
            int h;
            try {
                h = Integer.parseInt(sc.nextLine());
            }
            catch (Exception e) {
                System.out.println("Invalid Hotel ID");
                continue;
            }
            String fSql = "SELECT * FROM hotels WHERE hotel_id = ?";
            PreparedStatement pst = con.prepareStatement(fSql);
            pst.setInt(1, h);
            ResultSet Frs = pst.executeQuery();
            if (Frs.next()) {
                return h;
            }
            else {
                System.out.println("Hotel Not Available");
                System.out.println("1. View Hotels");
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
                    case 1 -> new Hotel().view();
                    case 2 -> { }
                    default -> System.out.println("Invalid Choice");
                }
            }
        }
    }

    @Override
    public void view() throws Exception {
        new Hotel().view();
        System.out.print("Hotel Id : ");
        int h = readHotelId();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM rooms WHERE HOTEL_ID = "+h);

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("ID\tHotel ID\tRoom Type\tTotal\tAvailable\tMax Persons\tPrice/Night");
        System.out.println("------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("room_id") + "\t" +
                            rs.getInt("hotel_id") + "\t\t" +
                            rs.getString("room_type") + "\t\t" +
                            rs.getInt("total_rooms") + "\t" +
                            rs.getInt("available_rooms") + "\t\t" +
                            rs.getInt("max_persons") + "\t\t" +
                            rs.getBigDecimal("price_per_night")
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        new Hotel().view();
        hID = readHotelId();

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
        System.out.println(rs>0?"Room Added":"Failed to add Room");
    }

    @Override
    public void edit() throws Exception {

        view();
        while (true) {
            System.out.println("Enter Room ID : ");
            rID = sc.nextInt();
            sc.nextLine();
            String sql = "SELECT `room_id`,`room_type`,`total_rooms`,`available_rooms`,`max_persons`,`price_per_night` FROM `rooms` WHERE room_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, rID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            if (rs.next()) {
                int currentTotal = rs.getInt(3);
                int currentAvailable = rs.getInt(4);
                System.out.println("ID : "+rs.getInt(1));
                System.out.println("1."+rsm.getColumnName(2)+" = "+rs.getObject(2));
                System.out.println("2."+rsm.getColumnName(3)+" = "+rs.getObject(3));
                System.out.println("3."+rsm.getColumnName(4)+" = "+rs.getObject(4));
                System.out.println("4."+rsm.getColumnName(5)+" = "+rs.getObject(5));
                System.out.println("5."+rsm.getColumnName(6)+" = "+rs.getObject(6));
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
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }
                    String fSql = "UPDATE `rooms` SET `" + rsm.getColumnName(col) + "` = ? WHERE room_id = ?";
                    PreparedStatement upst = con.prepareStatement(fSql);
                    upst.setString(1, n);
                    upst.setInt(2, rID);
                    int r = upst.executeUpdate();
                    System.out.println(r>0?"Room Updated Successfully":"Failed to Update Room");
                    break;
                }
                break;
            }
            else {
                System.out.println("Invalid Room Id");
            }
        }

    }

    @Override
    public void delete() throws Exception {
        view();
        System.out.println("Enter Room Id : ");
        int rID = sc.nextInt();
        String csql = "DELETE FROM `rooms` WHERE `room_id` = ?";
        PreparedStatement pst = con.prepareStatement(csql);
        pst.setInt(1, rID);
        int r = pst.executeUpdate();
        System.out.println(r>0?"Room Deleted Successfully":"Failed to Delete Room");
    }
}