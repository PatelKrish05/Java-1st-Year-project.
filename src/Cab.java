import JDBC.connection;

import java.sql.*;
import java.util.Scanner;

public class Cab extends connection implements Manageable {
    String from = "", to = "", b_Type = "";
    int choice, bID;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM cabs");

        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.println("Cab ID\tWith\tWith ID\tDeparture\t\tMax Pass.\tAvailable\tPrice");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t" +
                            rs.getInt(3) + "\t" +
                            rs.getString(4) + "\t" +
                            rs.getInt(5) + "\t\t" +
                            rs.getString(6) + "\t\t" +
                            rs.getInt(7)
            );
        }

        System.out.println("--------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        Timestamp departure = null;
        int with_id = 0, choice = 0;
        String with = "";

        while (true) {
            System.out.println("Cab For :-");
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

        if (choice == 1) {
            with = "F_id";
            while (true) {
                new Flight().view();
                int id = new Methods().readValidInt("Flight id : ");
                String sql = "SELECT * FROM FLIGHTS WHERE FLIGHT_ID = " + id;
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql);
                if (!rs.next()) {
                    System.out.println("Enter valid Id");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(6);
                    break;
                }
            }
        } else if (choice == 2) {
            with = "T_id";
            while (true) {
                new Train().view();
                int id = new Methods().readValidInt("Train id : ");
                String sql = "SELECT * FROM TRAINS WHERE TRAIN_ID = " + id;
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql);
                if (!rs.next()) {
                    System.out.println("Enter valid Id");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(6);
                    break;
                }
            }
        } else if (choice == 3) {
            with = "B_id";
            while (true) {
                new Bus().view();
                int id = new Methods().readValidInt("Bus id : ");
                String sql = "SELECT * FROM BUSES WHERE BUS_ID = " + id;
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql);
                if (!rs.next()) {
                    System.out.println("Enter valid Id");
                } else {
                    with_id = rs.getInt(1);
                    departure = rs.getTimestamp(6);
                    break;
                }
            }
        } else if (choice == 4) {
            return;
        }

        int mP = new Methods().readValidInt("Max Passenger : ");

        int ava = new Methods().readValidInt("Availability : ");

        int p = new Methods().readValidInt("Prize : ");

        String sql = "INSERT INTO `cabs`(`Departure_Time`, `Max_Passenger`, `Availability`, `Prize`, `" + with + "`) " +
                "VALUES (?,?,?,?,?)";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setTimestamp(1, departure);
        pt.setInt(2, mP);
        pt.setInt(3, ava);
        pt.setInt(4, p);
        pt.setInt(5, with_id);

        int r = pt.executeUpdate();
        System.out.println(r != 0 ? "Cab Added" : "Failed");
    }

    @Override
    public void edit() throws Exception {
        int cID, choice;

        view();
        while (true) {
            cID = new Methods().readValidInt("Enter Cab ID : ");

            String sql = "SELECT `Cab_id`, `Max_Passenger`, `Availability`, `Prize` FROM CABS WHERE CAB_ID = " + cID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            if (rs.next()) {
                System.out.println("ID : " + rs.getInt(1));
                System.out.println("1." + rsm.getColumnName(2) + " = " + rs.getInt(2));
                System.out.println("2." + rsm.getColumnName(3) + " = " + rs.getInt(3));
                System.out.println("3." + rsm.getColumnName(4) + " = " + rs.getInt(4));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    String n = "";
                    int col = 0;

                    switch (choice) {
                        case 1 -> {
                            int mp = new Methods().readValidInt("New Max Passenger : ");
                            n = "" + mp;
                            col = 2;
                        }
                        case 2 -> {
                            int ava = new Methods().readValidInt("New Availability : ");
                            n = "" + ava;
                            col = 3;
                        }
                        case 3 -> {
                            int p = new Methods().readValidInt("New Prize : ");
                            n = "" + p;
                            col = 4;
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }

                    String fSql = "UPDATE `CABS` SET `" + rsm.getColumnName(col) + "` = '" + n + "' WHERE CAB_ID = " + cID;
                    Statement st = con.createStatement();
                    try {
                        int frs = st.executeUpdate(fSql);
                        System.out.println("Cab Details Updated.");
                        break;
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            } else {
                System.out.println("Invalid Cab Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int cId = new Methods().readValidInt("Enter Cab Id to Delete (or 0 to cancel): ");
            if (cId == 0) return;

            String checkSql = "SELECT * FROM `cabs` WHERE Cab_id = " + cId;
            Statement checkSt = con.createStatement();
            ResultSet checkRs = checkSt.executeQuery(checkSql);

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Unlink cab reference from user booking records
                    con.prepareStatement("UPDATE flight_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = " + cId).executeUpdate();
                    con.prepareStatement("UPDATE train_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = " + cId).executeUpdate();
                    con.prepareStatement("UPDATE bus_booking SET cab_id = NULL, nCab = 0 WHERE cab_id = " + cId).executeUpdate();

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
                System.out.println("Invalid Cab Id");
            }
        }
    }
}