package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.util.Scanner;

public class HolidayPackage extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    String packageName;
    double price;
    int packID, hotelID, roomID, choice;

    @Override
    public void view() throws Exception {
        String sql = "SELECT hp.pack_id, hp.Package_Name, h.hotel_name, r.room_type, hp.package_price " +
                "FROM holiday_packages hp " +
                "LEFT JOIN hotels h ON hp.hotel_id = h.hotel_id " +
                "LEFT JOIN rooms r ON hp.room_id = r.room_id " +
                "ORDER BY hp.pack_id";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.println("Package ID\tPackage Name\t\tHotel Name\t\tRoom Type\t\tPrice");
        System.out.println("--------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("pack_id") + "\t\t" +
                            rs.getString("Package_Name") + "\t\t" +
                            (rs.getString("hotel_name") != null ? rs.getString("hotel_name") : "N/A") + "\t\t" +
                            (rs.getString("room_type") != null ? rs.getString("room_type") : "N/A") + "\t\t$" +
                            rs.getDouble("package_price")
            );
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        System.out.print("Enter Package Name: ");
        packageName = sc.nextLine().trim();

        // 1. Hotel & Room Selection
        new Hotel().view();
        hotelID = new Methods().readValidInt("Enter Hotel ID for Package: ");

        String roomSql = "SELECT room_id, room_type, price_per_night FROM rooms WHERE hotel_id = ?";
        PreparedStatement roomSt = con.prepareStatement(roomSql);
        roomSt.setInt(1, hotelID);
        ResultSet roomRs = roomSt.executeQuery();

        System.out.println("\nAvailable Rooms for Hotel ID " + hotelID + ":");
        while (roomRs.next()) {
            System.out.println("Room ID: " + roomRs.getInt("room_id") + " | Type: " + roomRs.getString("room_type") + " | Price: $" + roomRs.getBigDecimal("price_per_night"));
        }
        roomID = new Methods().readValidInt("Select Room ID: ");

        while (true) {
            System.out.print("Enter Package Price: ");
            try {
                price = Double.parseDouble(sc.nextLine().trim());
                if (price > 0) break;
                System.out.println("Price must be greater than 0.");
            } catch (Exception e) {
                System.out.println("Invalid Price!");
            }
        }

        boolean autoCommitState = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            // 2. Insert Primary Package
            String sqlPkg = "INSERT INTO holiday_packages (Package_Name, hotel_id, room_id, package_price) VALUES (?, ?, ?, ?)";
            PreparedStatement pstPkg = con.prepareStatement(sqlPkg, Statement.RETURN_GENERATED_KEYS);
            pstPkg.setString(1, packageName);
            pstPkg.setInt(2, hotelID);
            pstPkg.setInt(3, roomID);
            pstPkg.setDouble(4, price);
            pstPkg.executeUpdate();

            ResultSet genKeys = pstPkg.getGeneratedKeys();
            int newPackId = 0;
            if (genKeys.next()) {
                newPackId = genKeys.getInt(1);
            }

            // 3. Option 1: Dynamic Transport Loop (Handles multiple flights/trains/buses)
            boolean addingTransports = true;
            while (addingTransports) {
                System.out.println("\n--- ADD TRANSPORT LEG TO PACKAGE ---");
                System.out.println("1. Add Flight Leg");
                System.out.println("2. Add Train Leg");
                System.out.println("3. Add Bus Leg");
                System.out.println("4. Finish Transport Selection");

                int tChoice = new Methods().readValidInt("Choice: ");
                String tType = "";
                int tId = 0;
                Integer cabId = null;

                switch (tChoice) {
                    case 1 -> {
                        tType = "FLIGHT";
                        new Flight().view();
                        tId = new Methods().readValidInt("Enter Flight ID: ");
                        cabId = checkCabForTransport("F_id", tId);
                    }
                    case 2 -> {
                        tType = "TRAIN";
                        new Train().view();
                        tId = new Methods().readValidInt("Enter Train ID: ");
                        cabId = checkCabForTransport("T_id", tId);
                    }
                    case 3 -> {
                        tType = "BUS";
                        new Bus().view();
                        tId = new Methods().readValidInt("Enter Bus ID: ");
                        cabId = checkCabForTransport("B_id", tId);
                    }
                    case 4 -> {
                        addingTransports = false;
                        continue;
                    }
                    default -> {
                        System.out.println("Invalid Option");
                        continue;
                    }
                }

                // Insert Transport Record into normalized table
                String sqlTrans = "INSERT INTO packages_transports (PACK_ID, transport_type, transport_id, cab_id) VALUES (?, ?, ?, ?)";
                PreparedStatement pstTrans = con.prepareStatement(sqlTrans);
                pstTrans.setInt(1, newPackId);
                pstTrans.setString(2, tType);
                pstTrans.setInt(3, tId);
                setNullableInt(pstTrans, 4, cabId);
                pstTrans.executeUpdate();

                System.out.println("-> Added " + tType + " Leg (ID: " + tId + ") to Package.");
            }

            con.commit();
            System.out.println("\nHoliday Package and All Transport Mappings Added Successfully!");
        } catch (Exception e) {
            con.rollback();
            System.out.println("Failed to Add Package: " + e.getMessage());
        } finally {
            con.setAutoCommit(autoCommitState);
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            packID = new Methods().readValidInt("Enter Package ID to Edit (or 0 to cancel): ");
            if (packID == 0) return;

            String sql = "SELECT * FROM holiday_packages WHERE pack_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, packID);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\nCurrent Package Details:");
                System.out.println("1. Package Name: " + rs.getString("Package_Name"));
                System.out.println("2. Hotel ID: " + rs.getInt("hotel_id"));
                System.out.println("3. Room ID: " + rs.getInt("room_id"));
                System.out.println("4. Package Price: $" + rs.getDouble("package_price"));

                choice = new Methods().readValidInt("Select Field Number to Edit: ");

                switch (choice) {
                    case 1 -> {
                        System.out.print("New Package Name: ");
                        String newName = sc.nextLine().trim();
                        PreparedStatement upst = con.prepareStatement("UPDATE holiday_packages SET Package_Name = ? WHERE pack_id = ?");
                        upst.setString(1, newName);
                        upst.setInt(2, packID);
                        upst.executeUpdate();
                    }
                    case 2 -> {
                        new Hotel().view();
                        int newHId = new Methods().readValidInt("New Hotel ID: ");
                        PreparedStatement upst = con.prepareStatement("UPDATE holiday_packages SET hotel_id = ? WHERE pack_id = ?");
                        upst.setInt(1, newHId);
                        upst.setInt(2, packID);
                        upst.executeUpdate();
                    }
                    case 3 -> {
                        int newRId = new Methods().readValidInt("New Room ID: ");
                        PreparedStatement upst = con.prepareStatement("UPDATE holiday_packages SET room_id = ? WHERE pack_id = ?");
                        upst.setInt(1, newRId);
                        upst.setInt(2, packID);
                        upst.executeUpdate();
                    }
                    case 4 -> {
                        System.out.print("New Package Price: ");
                        double newPrice = Double.parseDouble(sc.nextLine().trim());
                        PreparedStatement upst = con.prepareStatement("UPDATE holiday_packages SET package_price = ? WHERE pack_id = ?");
                        upst.setDouble(1, newPrice);
                        upst.setInt(2, packID);
                        upst.executeUpdate();
                    }
                    default -> {
                        System.out.println("Invalid Choice.");
                        continue;
                    }
                }
                System.out.println("Package Details Updated Successfully!");
                break;
            } else {
                System.out.println("Invalid Package ID!");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            packID = new Methods().readValidInt("Enter Package ID to Delete (or 0 to cancel): ");
            if (packID == 0) return;

            PreparedStatement checkSt = con.prepareStatement("SELECT Package_Name FROM holiday_packages WHERE pack_id = ?");
            checkSt.setInt(1, packID);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Mark customer package bookings as CANCELED
                    PreparedStatement cancelBookings = con.prepareStatement("UPDATE package_booking SET status = 'CANCELED' WHERE Package_id = ?");
                    cancelBookings.setInt(1, packID);
                    cancelBookings.executeUpdate();

                    // 2. Delete linked entries from packages_transports
                    PreparedStatement delTransports = con.prepareStatement("DELETE FROM packages_transports WHERE PACK_ID = ?");
                    delTransports.setInt(1, packID);
                    delTransports.executeUpdate();

                    // 3. Delete from primary holiday_packages table
                    PreparedStatement delPkg = con.prepareStatement("DELETE FROM holiday_packages WHERE pack_id = ?");
                    delPkg.setInt(1, packID);
                    int r = delPkg.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Holiday Package Deleted Successfully!");
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Holiday Package.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting package: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Package ID.");
            }
        }
    }

    // Helper: Finds associated Cab ID for selected transport type
    private Integer checkCabForTransport(String fkColumn, int transportId) throws SQLException {
        String sql = "SELECT Cab_id FROM cabs WHERE " + fkColumn + " = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, transportId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt("Cab_id");
            }
        }
        return null;
    }

    // Helper: Safely sets NULL values for SQL PreparedStatement integers
    private void setNullableInt(PreparedStatement pst, int parameterIndex, Integer value) throws SQLException {
        if (value != null) {
            pst.setInt(parameterIndex, value);
        } else {
            pst.setNull(parameterIndex, Types.INTEGER);
        }
    }
}