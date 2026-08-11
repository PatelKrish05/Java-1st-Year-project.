package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Cab extends connection implements Manageable {
    Timestamp departure = null;
    int with_id = 0, choice = 0;
    String with = "";

    @Override
    public void view() throws Exception {
        // Fetch standard columns directly
        String sql = "SELECT Cab_id, Departure_Time, Max_Passenger, Availability, Price, F_id, T_id, B_id FROM cabs";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.println("Cab ID\tWith\t\tDeparture Time\t\tMax Pass.\tAvailability\tPrice");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            int cabId = rs.getInt("Cab_id");
            Timestamp depTime = rs.getTimestamp("Departure_Time");
            int maxPass = rs.getInt("Max_Passenger");
            int avail = rs.getInt("Availability");
            double price = rs.getDouble("Price");

            // Simple Java if-else to determine the "With" text
            String with = "None";
            if (rs.getObject("F_id") != null) {
                with = "Flight (ID: " + rs.getInt("F_id") + ")";
            } else if (rs.getObject("T_id") != null) {
                with = "Train (ID: " + rs.getInt("T_id") + ")";
            } else if (rs.getObject("B_id") != null) {
                with = "Bus (ID: " + rs.getInt("B_id") + ")";
            }

            System.out.println(
                    cabId + "\t" +
                            with + (with.length() < 12 ? "\t\t" : "\t") +
                            depTime + "\t" +
                            maxPass + "\t\t" +
                            avail + "\t\t$" +
                            price
            );
        }

        System.out.println("--------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        System.out.println("\n--- ADD CAB ---");
        System.out.println("1. Single Manual Entry");
        System.out.println("2. Bulk Upload via CSV File");
        System.out.println("3. Back");

        int mode = new Methods().readValidInt("Choice: ");
        if (mode == 2) {
            uploadCabFile();
            return;
        } else if (mode == 3 || mode == 0) {
            return;
        } else if (mode != 1) {
            System.out.println("Invalid Choice.");
            return;
        }

        // Manual Entry
        while (true) {
            System.out.println("\nCab For :-");
            System.out.println("1. Flight");
            System.out.println("2. Train");
            System.out.println("3. Buses");
            System.out.println("4. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 4) {
                break;
            } else {
                System.out.println("Invalid Choice");
            }
        }

        if (choice == 4) return;

        // 1. Flight Cab Link
        if (choice == 1) {
            with = "F_id";
            new Flight().view();
            while (true) {
                int id = new Methods().readValidInt("Enter Flight ID (or 0 to cancel): ");
                if (id == 0) return;

                String sql = "SELECT Flight_id, Boarding_Time FROM flights WHERE Flight_id = ?";
                PreparedStatement st = con.prepareStatement(sql);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                if (!rs.next()) {
                    System.out.println("Enter valid Flight ID.");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(2);
                    break;
                }
            }
        }

        // 2. Train Cab Link
        else if (choice == 2) {
            with = "T_id";
            new Train().view();
            while (true) {
                int id = new Methods().readValidInt("Enter Train ID (or 0 to cancel): ");
                if (id == 0) return;

                String sql = "SELECT train_id, Departure_Time FROM trains WHERE train_id = ?";
                PreparedStatement st = con.prepareStatement(sql);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                if (!rs.next()) {
                    System.out.println("Enter valid Train ID.");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(2);
                    break;
                }
            }
        }

        // 3. Bus Cab Link
        else if (choice == 3) {
            with = "B_id";
            new Bus().view();
            while (true) {
                int id = new Methods().readValidInt("Enter Bus ID (or 0 to cancel): ");
                if (id == 0) return;

                String sql = "SELECT bus_id, Departure_Time FROM buses WHERE bus_id = ?";
                PreparedStatement st = con.prepareStatement(sql);
                st.setInt(1, id);
                ResultSet rs = st.executeQuery();

                if (!rs.next()) {
                    System.out.println("Enter valid Bus ID.");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(2);
                    break;
                }
            }
        }

        int mP = new Methods().readValidInt("Max Passengers : ");
        int ava = new Methods().readValidInt("Availability : ");
        int price = new Methods().readValidInt("Price : ");

        String sql = "INSERT INTO `cabs`(`Departure_Time`, `Max_Passenger`, `Availability`, `Price`, `" + with + "`) " +
                "VALUES (?,?,?,?,?)";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setTimestamp(1, departure);
        pt.setInt(2, mP);
        pt.setInt(3, ava);
        pt.setInt(4, price);
        pt.setInt(5, with_id);

        int r = pt.executeUpdate();
        System.out.println(r != 0 ? "Cab Added Successfully!" : "Failed to Add Cab.");
    }

    // ================= BATCH FILE UPLOAD =================
    public void uploadCabFile() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter full path of the CSV file (e.g., C:/data/cabs.csv): ");
        String filePath = scanner.nextLine().trim();

        int successCount = 0;
        int failCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            boolean autoCommitState = con.getAutoCommit();
            con.setAutoCommit(false); // Batch transaction safety

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");

                if (data.length < 6) {
                    System.out.println("Skipping malformed row: " + line);
                    failCount++;
                    continue;
                }

                try {
                    LocalDateTime depDateTime = LocalDateTime.parse(data[0].trim(), formatter);
                    Timestamp depTimestamp = Timestamp.valueOf(depDateTime);
                    int maxPass = Integer.parseInt(data[1].trim());
                    int avail = Integer.parseInt(data[2].trim());
                    int priceVal = Integer.parseInt(data[3].trim());
                    String targetType = data[4].trim().toUpperCase();
                    int targetId = Integer.parseInt(data[5].trim());

                    String fkCol = switch (targetType) {
                        case "FLIGHT", "F" -> "F_id";
                        case "TRAIN", "T" -> "T_id";
                        case "BUS", "B" -> "B_id";
                        default -> null;
                    };

                    if (fkCol == null) {
                        System.out.println("Skipping row with invalid vehicle type [" + targetType + "]: " + line);
                        failCount++;
                        continue;
                    }

                    String insertSql = "INSERT INTO `cabs`(`Departure_Time`, `Max_Passenger`, `Availability`, `Price`, `" + fkCol + "`) " +
                            "VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement pst = con.prepareStatement(insertSql)) {
                        pst.setTimestamp(1, depTimestamp);
                        pst.setInt(2, maxPass);
                        pst.setInt(3, avail);
                        pst.setInt(4, priceVal);
                        pst.setInt(5, targetId);

                        int inserted = pst.executeUpdate();
                        if (inserted > 0) {
                            successCount++;
                        } else {
                            failCount++;
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error parsing row [" + line + "]: " + e.getMessage());
                    failCount++;
                }
            }

            con.commit();
            con.setAutoCommit(autoCommitState);

            System.out.println("\n==========================================");
            System.out.println(" BATCH UPLOAD COMPLETE!");
            System.out.println(" Cabs Added Successfully : " + successCount);
            System.out.println(" Failed / Skipped Rows   : " + failCount);
            System.out.println("==========================================\n");

        } catch (Exception e) {
            System.out.println("File upload failed: " + e.getMessage());
        }
    }

    @Override
    public void edit() throws Exception {
        int cID, choice;

        view();
        while (true) {
            cID = new Methods().readValidInt("Enter Cab ID to Edit (or 0 to cancel): ");
            if (cID == 0) return;

            String sql = "SELECT `Cab_id`, `Max_Passenger`, `Availability`, `Price` FROM cabs WHERE Cab_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, cID);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();

            if (rs.next()) {
                System.out.println("\nID : " + rs.getInt(1));
                System.out.println("1. " + rsm.getColumnName(2) + " = " + rs.getInt(2));
                System.out.println("2. " + rsm.getColumnName(3) + " = " + rs.getInt(3));
                System.out.println("3. " + rsm.getColumnName(4) + " = " + rs.getInt(4));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    String n = "";
                    int col = 0;

                    switch (choice) {
                        case 1 -> {
                            int mp = new Methods().readValidInt("New Max Passengers : ");
                            n = "" + mp;
                            col = 2;
                        }
                        case 2 -> {
                            int ava = new Methods().readValidInt("New Availability : ");
                            n = "" + ava;
                            col = 3;
                        }
                        case 3 -> {
                            int p = new Methods().readValidInt("New Price : ");
                            n = "" + p;
                            col = 4;
                        }
                        default -> {
                            System.out.println("Invalid Choice");
                            continue;
                        }
                    }

                    String fSql = "UPDATE `cabs` SET `" + rsm.getColumnName(col) + "` = ? WHERE Cab_id = ?";
                    PreparedStatement uSt = con.prepareStatement(fSql);
                    uSt.setString(1, n);
                    uSt.setInt(2, cID);

                    try {
                        uSt.executeUpdate();
                        System.out.println("Cab Details Updated Successfully!");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            } else {
                System.out.println("Invalid Cab ID.");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int cId = new Methods().readValidInt("Enter Cab Id to Delete (or 0 to cancel): ");
            if (cId == 0) return;

            String checkSql = "SELECT * FROM `cabs` WHERE Cab_id = ?";
            PreparedStatement checkSt = con.prepareStatement(checkSql);
            checkSt.setInt(1, cId);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Unlink cab references from user booking records
                    PreparedStatement uFb = con.prepareStatement("UPDATE flight_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = ?");
                    uFb.setInt(1, cId); uFb.executeUpdate();

                    PreparedStatement uTb = con.prepareStatement("UPDATE train_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = ?");
                    uTb.setInt(1, cId); uTb.executeUpdate();

                    PreparedStatement uBb = con.prepareStatement("UPDATE bus_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = ?");
                    uBb.setInt(1, cId); uBb.executeUpdate();

                    // 2. Delete the cab record
                    PreparedStatement delCab = con.prepareStatement("DELETE FROM `cabs` WHERE Cab_id = ?");
                    delCab.setInt(1, cId);
                    int r = delCab.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Cab Deleted Successfully!");
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Cab.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting cab: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Cab ID.");
            }
        }
    }
}