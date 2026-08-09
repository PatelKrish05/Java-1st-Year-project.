import JDBC.connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Package1 extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, pID, hID, roomID;
    long packagePrice;

    private static class TransportDetail {
        int transportTableId; // The primary key 'id' of the packages_transports table
        String type;
        int transportId;
        Integer cabId;
        long price;
    }

    private String show(Object o) {
        return o == null ? "-" : o.toString();
    }

    private int readMenuChoice() {
        try {
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid Choice");
            return -1;
        }
    }

    private int readHotelId() throws Exception {
        new Hotel().view();
        while (true) {
            System.out.print("Hotel ID : ");
            int h;
            try {
                h = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid Hotel ID");
                continue;
            }
            PreparedStatement pst = con.prepareStatement("SELECT * FROM hotels WHERE hotel_id = ?");
            pst.setInt(1, h);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return h;
            }

            System.out.println("Hotel Not Available");
            System.out.println("1. View Hotels\n2. Try Another");
            choice = readMenuChoice();
            if (choice == 1) {
                new Hotel().view();
            }
        }
    }

    private int readRoomId(int hotelId) throws Exception {
        new Room().view();
        while (true) {
            System.out.print("Room ID : ");
            int r;
            try {
                r = Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid Room ID");
                continue;
            }
            PreparedStatement pst = con.prepareStatement("SELECT * FROM rooms WHERE room_id = ?");
            pst.setInt(1, r);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                if (rs.getInt("hotel_id") != hotelId) {
                    System.out.println("This Room does not belong to the selected Hotel");
                    continue;
                }
                return r;
            }

            System.out.println("Room Not Available");
            System.out.println("1. View Rooms\n2. Try Another");
            choice = readMenuChoice();
            if (choice == 1) new Room().view();
        }
    }

    private TransportDetail TransportCreation() throws Exception {
        while (true) {
            System.out.println("Select Transport Type:");
            System.out.println("1. Flight");
            System.out.println("2. Train");
            System.out.println("3. Bus");
            choice = readMenuChoice();

            String type = "";
            String table = "";
            String idCol = "";
            String ticketCol = "";
            String cabCol = "";

            switch (choice) {
                case 1 -> { type = "FLIGHT"; table = "flights"; idCol = "Flight_id"; ticketCol = "Available_Tickets"; cabCol = "F_id"; }
                case 2 -> { type = "TRAIN"; table = "trains"; idCol = "train_id"; ticketCol = "Available_Tickets"; cabCol = "T_id"; }
                case 3 -> { type = "BUS"; table = "buses"; idCol = "bus_id"; ticketCol = "Available_Tickets"; cabCol = "B_id"; }
                default -> {
                    System.out.println("Invalid Choice");
                    continue;
                }
            }

            Integer id = readTransportId(table, idCol, ticketCol, type);
            if (id == null) return null;

            TransportDetail td = new TransportDetail();
            td.type = type;
            td.transportId = id;
            td.price = getPrize(table, idCol, id);

            Integer cab = offerCab(cabCol, id, type);
            td.cabId = cab;
            if (cab != null) {
                td.price += getPrize("cabs", "Cab_id", cab);
            }
            return td;
        }
    }

    private Integer readTransportId(String table, String idCol, String ticketCol, String label) throws Exception {
        while (true) {
            System.out.print(label + " ID (blank to cancel) : ");
            String input = sc.nextLine();
            if (input.isBlank()) return null;

            int id;
            try {
                id = Integer.parseInt(input);
            } catch (Exception e) {
                System.out.println("Invalid ID");
                continue;
            }

            PreparedStatement pst = con.prepareStatement("SELECT * FROM " + table + " WHERE " + idCol + " = ?");
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println(label + " Not Found");
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

        System.out.println("Cab(s) available for this " + label + " :-");
        for (String row : rows) System.out.println(row);
        System.out.println("Add a Cab? 1. Yes  2. No");
        int c = readMenuChoice();
        if (c != 1) return null;

        if (ids.size() == 1) return ids.get(0);

        while (true) {
            System.out.print("Enter Cab ID to add : ");
            try {
                int cid = Integer.parseInt(sc.nextLine());
                if (ids.contains(cid)) return cid;
                System.out.println("Invalid Cab ID");
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

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM holiday_packages");

        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("Package ID\tHotel ID\tRoom ID\tPrice\tAttached Transports (Mapping ID | Type:TransportID)");
        System.out.println("----------------------------------------------------------------------------------");

        while (rs.next()) {
            int packId = rs.getInt("pack_id");
            PreparedStatement ptStmt = con.prepareStatement(
                    "SELECT id, transport_type, transport_id, cab_id FROM packages_transports WHERE pack_id = ?");
            ptStmt.setInt(1, packId);
            ResultSet ptRs = ptStmt.executeQuery();

            StringBuilder transportsList = new StringBuilder();
            while (ptRs.next()) {
                if (transportsList.length() > 0) transportsList.append(", ");
                transportsList.append("[MapID:")
                        .append(ptRs.getInt("id"))
                        .append("] ")
                        .append(ptRs.getString("transport_type"))
                        .append(":")
                        .append(ptRs.getInt("transport_id"));
                Object cab = ptRs.getObject("cab_id");
                if (cab != null) transportsList.append(" (Cab:").append(cab).append(")");
            }
            if (transportsList.length() == 0) transportsList.append("None");

            System.out.println(packId + "\t\t" +
                    rs.getInt("hotel_id") + "\t\t" +
                    rs.getInt("room_id") + "\t" +
                    rs.getBigDecimal("package_price") + "\t" +
                    transportsList.toString());
        }
        System.out.println("----------------------------------------------------------------------------------");
        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        hID = readHotelId();
        roomID = readRoomId(hID);

        List<TransportDetail> transports = new ArrayList<>();
        while (true) {
            System.out.println("1. Add Transport item\n2. Finish and Save Package");
            int c = readMenuChoice();
            if (c == 1) {
                TransportDetail td = TransportCreation();
                if (td != null) transports.add(td);
            }
            else if (c == 2) {
                break;
            }
        }

        long transportTotal = 0;
        for (TransportDetail t : transports) transportTotal += t.price;

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

            PreparedStatement pstTrans = con.prepareStatement(
                    "INSERT INTO packages_transports (pack_id, transport_type, transport_id, cab_id) VALUES (?, ?, ?, ?)");
            for (TransportDetail st : transports) {
                pstTrans.setInt(1, newPackId);
                pstTrans.setString(2, st.type);
                pstTrans.setInt(3, st.transportId);
                if (st.cabId == null) pstTrans.setNull(4, Types.INTEGER);
                else pstTrans.setInt(4, st.cabId);
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
            System.out.println("3. Manage Transports (Add, Edit individual, or Delete individual transport)");
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
                    // Custom transport sub-menu
                    while (true) {
                        System.out.println("\n--- Transport Management for Package " + pID + " ---");
                        PreparedStatement ptStmt = con.prepareStatement(
                                "SELECT id, transport_type, transport_id, cab_id FROM packages_transports WHERE pack_id = ?");
                        ptStmt.setInt(1, pID);
                        ResultSet ptRs = ptStmt.executeQuery();

                        System.out.println("MapID\tType\tTransportID\tCabID");
                        while (ptRs.next()) {
                            System.out.println(ptRs.getInt("id") + "\t" +
                                    ptRs.getString("transport_type") + "\t" +
                                    ptRs.getInt("transport_id") + "\t\t" +
                                    show(ptRs.getObject("cab_id")));
                        }

                        System.out.println("\n1. Add a new transport to this package");
                        System.out.println("2. Edit a specific transport by MapID");
                        System.out.println("3. Delete a specific transport by MapID");
                        System.out.println("4. Back / Exit transport management");
                        int subChoice = readMenuChoice();

                        if (subChoice == 1) {
                            TransportDetail td = TransportCreation();
                            if (td != null) {
                                PreparedStatement ins = con.prepareStatement(
                                        "INSERT INTO packages_transports (pack_id, transport_type, transport_id, cab_id) VALUES (?, ?, ?, ?)");
                                ins.setInt(1, pID);
                                ins.setString(2, td.type);
                                ins.setInt(3, td.transportId);
                                if (td.cabId == null) ins.setNull(4, Types.INTEGER);
                                else ins.setInt(4, td.cabId);
                                ins.executeUpdate();
                                System.out.println("Transport added successfully.");
                            }
                        } else if (subChoice == 2) {
                            System.out.print("Enter MapID of the transport to edit: ");
                            int mapId = readMenuChoice();
                            TransportDetail td = TransportCreation();
                            if (td != null) {
                                PreparedStatement upd = con.prepareStatement(
                                        "UPDATE packages_transports SET transport_type = ?, transport_id = ?, cab_id = ? WHERE id = ? AND pack_id = ?");
                                upd.setString(1, td.type);
                                upd.setInt(2, td.transportId);
                                if (td.cabId == null) upd.setNull(3, Types.INTEGER);
                                else upd.setInt(3, td.cabId);
                                upd.setInt(4, mapId);
                                upd.setInt(5, pID);
                                int rows = upd.executeUpdate();
                                System.out.println(rows > 0 ? "Transport updated successfully." : "Invalid MapID.");
                            }
                        } else if (subChoice == 3) {
                            System.out.print("Enter MapID of the transport to delete: ");
                            int mapId = readMenuChoice();
                            PreparedStatement del = con.prepareStatement(
                                    "DELETE FROM packages_transports WHERE id = ? AND pack_id = ?");
                            del.setInt(1, mapId);
                            del.setInt(2, pID);
                            int rows = del.executeUpdate();
                            System.out.println(rows > 0 ? "Transport deleted successfully." : "Invalid MapID.");
                        } else if (subChoice == 4) {
                            break;
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
        } else {
            System.out.println("Invalid Package ID");
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        System.out.print("Enter Package ID to Delete : ");
        int pID = readMenuChoice();
        if (pID == -1) return;

        boolean autoCommitState = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            PreparedStatement delTrans = con.prepareStatement("DELETE FROM packages_transports WHERE pack_id = ?");
            delTrans.setInt(1, pID);
            delTrans.executeUpdate();

            PreparedStatement delPkg = con.prepareStatement("DELETE FROM holiday_packages WHERE pack_id = ?");
            delPkg.setInt(1, pID);
            int r = delPkg.executeUpdate();

            con.commit();
            System.out.println(r > 0 ? "Package Deleted Successfully" : "Package ID Not Found");
        } catch (Exception e) {
            con.rollback();
            System.out.println("Failed to Delete Package: " + e.getMessage());
        } finally {
            con.setAutoCommit(autoCommitState);
        }
    }
}