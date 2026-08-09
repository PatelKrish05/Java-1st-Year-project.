package Admin;

import JDBC.connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Packages extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, pID, hID, roomID;
    long packagePrice;

    private static class SingleTransportInput {
        String type; // "FLIGHT", "TRAIN", or "BUS"
        Integer flightId, trainId, busId;
        Integer flightCabId, trainCabId, busCabId;
        long price;
    }

    private String show(Object o) {
        return o == null ? "-" : o.toString();
    }

    private int readMenuChoice() {
        while (true) {
            try {
                int x = Integer.parseInt(sc.nextLine());
                return x;

            }
            catch (Exception e) {
                System.out.println("Invalid Choice");
                System.out.print("Try Again : ");
            }
        }
    }

    private void setNullableInt(PreparedStatement pst, int idx, Integer val) throws Exception {
        if (val == null) pst.setNull(idx, Types.INTEGER);
        else pst.setInt(idx, val);
    }

    // ---------- Displays ----------

    private void showAvailableHotels() throws Exception {
        System.out.println("\n--- Available Hotels ---");
        new Hotel().view();
        System.out.println("------------------------");
    }

    private void showAvailableRooms(int hotelId) throws Exception {
        System.out.println("\n--- Available Rooms for Hotel ID: " + hotelId + " ---");
        PreparedStatement pst = con.prepareStatement("SELECT * FROM rooms WHERE hotel_id = ?");
        pst.setInt(1, hotelId);
        ResultSet rs = pst.executeQuery();

        System.out.println("Room_ID\tRoom_Type\tPrice_Per_Night");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println(rs.getInt("room_id") + "\t" +
                    rs.getString("room_type") + "\t\t" +
                    rs.getBigDecimal("price_per_night"));
        }
        if (!found) System.out.println("No rooms available for this hotel.");
        System.out.println("------------------------------------------");
    }

    // ---------- Hotel / Room Selection ----------

    private int readHotelId() throws Exception {
        showAvailableHotels();
        while (true) {
            System.out.print("Hotel ID : ");
            int h;
            try {
                h = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid Hotel ID format");
                continue;
            }
            PreparedStatement pst = con.prepareStatement("SELECT * FROM hotels WHERE hotel_id = ?");
            pst.setInt(1, h);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return h;

            System.out.println("Hotel ID Not Found. Please try again.");
            showAvailableHotels();
        }
    }

    private int readRoomId(int hotelId) throws Exception {
        showAvailableRooms(hotelId);
        while (true) {
            System.out.print("Room ID : ");
            int r;
            try {
                r = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid Room ID format");
                continue;
            }
            PreparedStatement pst = con.prepareStatement("SELECT * FROM rooms WHERE room_id = ?");
            pst.setInt(1, r);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getInt("hotel_id") != hotelId) {
                    System.out.println("This Room does not belong to Hotel ID " + hotelId);
                    continue;
                }
                return r;
            }

            System.out.println("Room ID Not Found. Please try again.");
            showAvailableRooms(hotelId);
        }
    }

    // ---------- Transport Selection ----------

    private SingleTransportInput TransportCreation() throws Exception {
        while (true) {
            System.out.println("\nSelect Transport Type:");
            System.out.println("1. Flight");
            System.out.println("2. Train");
            System.out.println("3. Bus");
            System.out.println("4. Cancel");
            choice = readMenuChoice();

            if (choice == 4) return null;

            SingleTransportInput st = new SingleTransportInput();

            switch (choice) {
                case 1 -> {
                    new Flight().view();
                    Integer id = readTransportId("flights", "Flight_id", "Available_Tickets", "Flight");
                    if (id == null) return null;
                    st.type = "FLIGHT";
                    st.flightId = id;
                    st.price = getPrize("flights", "Flight_id", id);
                    Integer cab = offerCab("F_id", id, "Flight");
                    st.flightCabId = cab;
                    if (cab != null) st.price += getPrize("cabs", "Cab_id", cab);
                    return st;
                }
                case 2 -> {
                    new Train().view();
                    Integer id = readTransportId("trains", "train_id", "Available_Tickets", "Train");
                    if (id == null) return null;
                    st.type = "TRAIN";
                    st.trainId = id;
                    st.price = getPrize("trains", "train_id", id);
                    Integer cab = offerCab("T_id", id, "Train");
                    st.trainCabId = cab;
                    if (cab != null) st.price += getPrize("cabs", "Cab_id", cab);
                    return st;
                }
                case 3 -> {
                    new Bus().view();
                    Integer id = readTransportId("buses", "bus_id", "Available_Tickets", "Bus");
                    if (id == null) return null;
                    st.type = "BUS";
                    st.busId = id;
                    st.price = getPrize("buses", "bus_id", id);
                    Integer cab = offerCab("B_id", id, "Bus");
                    st.busCabId = cab;
                    if (cab != null) st.price += getPrize("cabs", "Cab_id", cab);
                    return st;
                }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    private Integer readTransportId(String table, String idCol, String ticketCol, String label) throws Exception {
        while (true) {
            System.out.print("Enter " + label + " ID (blank to cancel) : ");
            String input = sc.nextLine();
            if (input.isBlank()) return null;

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (Exception e) {
                System.out.println("Invalid ID format");
                continue;
            }

            PreparedStatement pst = con.prepareStatement("SELECT * FROM " + table + " WHERE " + idCol + " = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println(label + " ID Not Found");
                continue;
            }

            if (rs.getInt(ticketCol) <= 0) {
                System.out.println("No Tickets Available on this " + label);
                continue;
            }
            return id;
        }
    }

    private Integer offerCab(String cabColumn, int refId, String label) throws Exception {
        PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM cabs WHERE " + cabColumn + " = ? AND Availability > 0");
        pst.setInt(1, refId);
        ResultSet rs = pst.executeQuery();

        List<Integer> ids = new ArrayList<>();
        List<String> rows = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt("Cab_id"));
            rows.add(rs.getInt("Cab_id") + "\tMax Passengers: " + rs.getInt("Max_Passenger")
                    + "\tPrize: " + rs.getInt("Prize") + "\tDeparture: " + rs.getTimestamp("Departure_Time"));
        }

        if (ids.isEmpty()) return null;

        System.out.println("\nCab(s) available for this " + label + " :-");
        for (String row : rows) System.out.println(row);
        System.out.println("Add a Cab? 1. Yes  2. No");
        int c = readMenuChoice();
        if (c != 1) {
            return null;
        }

        if (ids.size() == 1) {
            return ids.get(0);
        }

        while (true) {
            System.out.print("Enter Cab ID to add : ");
            try {
                int cid = Integer.parseInt(sc.nextLine());
                if (ids.contains(cid)) return cid;
                System.out.println("Invalid Cab ID from list above");
            } catch (Exception e) {
                System.out.println("Invalid Input");
            }
        }
    }

    private long getPrize(String table, String idCol, int id) throws Exception {
        PreparedStatement pst = con.prepareStatement("SELECT Prize FROM " + table + " WHERE " + idCol + " = ?");
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return rs.getInt("Prize");
        return 0;
    }

    private long getRoomPrice(int roomId) throws Exception {
        PreparedStatement pst = con.prepareStatement("SELECT price_per_night FROM rooms WHERE room_id = ?");
        pst.setInt(1, roomId);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return rs.getLong("price_per_night");
        return 0;
    }

    private long readPackagePrice(long suggested) {
        while (true) {
            System.out.print("Package Price [suggested: " + suggested + ", press Enter to accept] : ");
            String input = sc.nextLine();
            if (input.isBlank()) return suggested;
            try {
                long p = Long.parseLong(input);
                if (p <= 0) {
                    System.out.println("Price must be greater than 0");
                    continue;
                }
                return p;
            } catch (Exception e) {
                System.out.println("Invalid Price");
            }
        }
    }

    // ---------- CRUD Operations ----------

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM holiday_packages");

        System.out.println("------------------------------------------------------------------------------------------------------------------");
        System.out.println("Package ID\tHotel ID\tRoom ID\tPrice\tAttached Transports (Flight, Train, Bus)");
        System.out.println("------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            int packId = rs.getInt("pack_id");
            PreparedStatement ptStmt = con.prepareStatement(
                    "SELECT flight_id, train_id, bus_id, flight_cab_id, train_cab_id, bus_cab_id FROM packages_transports WHERE PACK_ID = ?");
            ptStmt.setInt(1, packId);
            ResultSet ptRs = ptStmt.executeQuery();

            StringBuilder transportsList = new StringBuilder();
            while (ptRs.next()) {
                if (transportsList.length() > 0) transportsList.append(" | ");

                if (ptRs.getObject("flight_id") != null) {
                    transportsList.append("Flight:").append(ptRs.getInt("flight_id"));
                    if (ptRs.getObject("flight_cab_id") != null) transportsList.append(" (Cab:").append(ptRs.getInt("flight_cab_id")).append(")");
                } else if (ptRs.getObject("train_id") != null) {
                    transportsList.append("Train:").append(ptRs.getInt("train_id"));
                    if (ptRs.getObject("train_cab_id") != null) transportsList.append(" (Cab:").append(ptRs.getInt("train_cab_id")).append(")");
                } else if (ptRs.getObject("bus_id") != null) {
                    transportsList.append("Bus:").append(ptRs.getInt("bus_id"));
                    if (ptRs.getObject("bus_cab_id") != null) transportsList.append(" (Cab:").append(ptRs.getInt("bus_cab_id")).append(")");
                }
            }
            if (transportsList.length() == 0) transportsList.append("None");

            System.out.println(packId + "\t\t" +
                    rs.getInt("hotel_id") + "\t\t" +
                    rs.getInt("room_id") + "\t" +
                    rs.getBigDecimal("package_price") + "\t" +
                    transportsList.toString());
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------");
        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        hID = readHotelId();
        roomID = readRoomId(hID);

        List<SingleTransportInput> transports = new ArrayList<>();
        while (true) {
            System.out.println("\n1. Add Transport item\n2. Finish and Save Package");
            int c = readMenuChoice();
            if (c == 1) {
                SingleTransportInput st = TransportCreation();
                if (st != null) transports.add(st);
            } else if (c == 2) {
                break;
            }
        }

        long transportTotal = 0;
        for (SingleTransportInput t : transports) transportTotal += t.price;

        long suggested = getRoomPrice(roomID) + transportTotal;
        packagePrice = readPackagePrice(suggested);

        boolean autoCommitState = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            PreparedStatement pstPkg = con.prepareStatement(
                    "INSERT INTO holiday_packages (hotel_id, room_id, package_price) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pstPkg.setInt(1, hID);
            pstPkg.setInt(2, roomID);
            pstPkg.setLong(3, packagePrice);
            pstPkg.executeUpdate();

            ResultSet keys = pstPkg.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("Failed to get package ID");
            int newPackId = keys.getInt(1);

            String insertTransportSql = "INSERT INTO packages_transports " +
                    "(PACK_ID, flight_id, train_id, bus_id, flight_cab_id, train_cab_id, bus_cab_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstTrans = con.prepareStatement(insertTransportSql);

            for (SingleTransportInput st : transports) {
                pstTrans.setInt(1, newPackId);
                setNullableInt(pstTrans, 2, st.flightId);
                setNullableInt(pstTrans, 3, st.trainId);
                setNullableInt(pstTrans, 4, st.busId);
                setNullableInt(pstTrans, 5, st.flightCabId);
                setNullableInt(pstTrans, 6, st.trainCabId);
                setNullableInt(pstTrans, 7, st.busCabId);
                pstTrans.addBatch();
            }
            pstTrans.executeBatch();

            con.commit();
            System.out.println("Package Added Successfully");
        } catch (Exception e) {
            con.rollback();
            System.out.println("Error adding package: " + e.getMessage());
        } finally {
            con.setAutoCommit(autoCommitState);
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            System.out.print("Enter Package ID to Edit : ");
            pID = readMenuChoice();
            if (pID == -1) return;

            PreparedStatement pst = con.prepareStatement("SELECT * FROM holiday_packages WHERE pack_id = ?");
            pst.setInt(1, pID);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int curHotel = rs.getInt("hotel_id");
                System.out.println("1. Edit Hotel ID");
                System.out.println("2. Edit Room ID");
                System.out.println("3. Manage Transports (Add or Delete transports)");
                System.out.println("4. Edit Package Price");
                System.out.print("Choose option: ");
                choice = readMenuChoice();

                switch (choice) {
                    case 1 -> {
                        int newHotel = readHotelId();
                        PreparedStatement u = con.prepareStatement("UPDATE holiday_packages SET hotel_id = ? WHERE pack_id = ?");
                        u.setInt(1, newHotel);
                        u.setInt(2, pID);
                        u.executeUpdate();
                        System.out.println("Hotel Updated Successfully");
                    }
                    case 2 -> {
                        int newRoom = readRoomId(curHotel);
                        PreparedStatement u = con.prepareStatement("UPDATE holiday_packages SET room_id = ? WHERE pack_id = ?");
                        u.setInt(1, newRoom);
                        u.setInt(2, pID);
                        u.executeUpdate();
                        System.out.println("Room Updated Successfully");
                    }
                    case 3 -> {
                        while (true) {
                            System.out.println("\n--- Transport Management for Package " + pID + " ---");
                            PreparedStatement ptStmt = con.prepareStatement(
                                    "SELECT flight_id, train_id, bus_id, flight_cab_id, train_cab_id, bus_cab_id FROM packages_transports WHERE PACK_ID = ?");
                            ptStmt.setInt(1, pID);
                            ResultSet ptRs = ptStmt.executeQuery();

                            int index = 1;
                            while (ptRs.next()) {
                                String itemStr = "";
                                if (ptRs.getObject("flight_id") != null) {
                                    itemStr = "Flight ID: " + ptRs.getInt("flight_id") + " (Cab: " + show(ptRs.getObject("flight_cab_id")) + ")";
                                } else if (ptRs.getObject("train_id") != null) {
                                    itemStr = "Train ID: " + ptRs.getInt("train_id") + " (Cab: " + show(ptRs.getObject("train_cab_id")) + ")";
                                } else if (ptRs.getObject("bus_id") != null) {
                                    itemStr = "Bus ID: " + ptRs.getInt("bus_id") + " (Cab: " + show(ptRs.getObject("bus_cab_id")) + ")";
                                }
                                System.out.println(index + ". " + itemStr);
                                index++;
                            }

                            System.out.println("\n1. Add a transport to this package");
                            System.out.println("2. Clear all transports and re-enter");
                            System.out.println("3. Back / Exit");
                            int subChoice = readMenuChoice();

                            if (subChoice == 1) {
                                SingleTransportInput st = TransportCreation();
                                if (st != null) {
                                    String sql = "INSERT INTO packages_transports (PACK_ID, flight_id, train_id, bus_id, flight_cab_id, train_cab_id, bus_cab_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                                    PreparedStatement ins = con.prepareStatement(sql);
                                    ins.setInt(1, pID);
                                    setNullableInt(ins, 2, st.flightId);
                                    setNullableInt(ins, 3, st.trainId);
                                    setNullableInt(ins, 4, st.busId);
                                    setNullableInt(ins, 5, st.flightCabId);
                                    setNullableInt(ins, 6, st.trainCabId);
                                    setNullableInt(ins, 7, st.busCabId);
                                    ins.executeUpdate();
                                    System.out.println("Transport added successfully.");
                                }
                            }
                            else if (subChoice == 2) {
                                PreparedStatement del = con.prepareStatement("DELETE FROM packages_transports WHERE PACK_ID = ?");
                                del.setInt(1, pID);
                                del.executeUpdate();
                                System.out.println("All transports cleared for Package " + pID);
                            }
                            else if (subChoice == 3) {
                                break;
                            }
                            else {
                                System.out.println("Invalid Choice");
                            }
                        }
                    }
                    case 4 -> {
                        long newPrice = readPackagePrice(rs.getLong("package_price"));
                        PreparedStatement u = con.prepareStatement("UPDATE holiday_packages SET package_price = ? WHERE pack_id = ?");
                        u.setLong(1, newPrice);
                        u.setInt(2, pID);
                        u.executeUpdate();
                        System.out.println("Price Updated Successfully");
                    }
                    default -> System.out.println("Invalid Choice");
                }
                break;

            }
            else {
                System.out.println("Invalid Package ID");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            System.out.print("Enter Package ID to Delete : ");
            int pID = readMenuChoice();
            if (pID == -1) return;

            boolean autoCommitState = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                PreparedStatement delTrans = con.prepareStatement("DELETE FROM packages_transports WHERE PACK_ID = ?");
                delTrans.setInt(1, pID);
                delTrans.executeUpdate();

                PreparedStatement delPkg = con.prepareStatement("DELETE FROM holiday_packages WHERE pack_id = ?");
                delPkg.setInt(1, pID);
                int r = delPkg.executeUpdate();

                con.commit();
                if(r > 0) {
                    System.out.println("Package Deleted Successfully" );
                    break;
                }
                else {
                    System.out.println("Package ID Not Found");
                }
            }
            catch (Exception e) {
                con.rollback();
                System.out.println("Failed to Delete Package: " + e.getMessage());
            }
            finally {
                con.setAutoCommit(autoCommitState);
            }
        }
    }
}