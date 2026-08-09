import JDBC.connection;

import java.sql.*;
import java.util.Scanner;

public class State extends connection implements Manageable {

    String sName, cName, nS;
    int choice = 0, sID = 0, cID = 0, sCode = 0;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM states ORDER BY STATE_NAME");

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("State ID\tState Code\tState Name\t\tCountry ID");
        System.out.println("--------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t\t" +
                            rs.getString(2) + "\t\t" +
                            rs.getString(3) + "\t\t" +
                            rs.getInt(4)
            );
        }

        System.out.println("--------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.print("Country name : ");
            cName = sc.nextLine().trim();
            if (cName.isBlank()) return;

            String sql = "SELECT * FROM COUNTRIES WHERE COUNTRY_NAME=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, cName);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println("Country Not Available");
                System.out.println("1. Add Country to Database");
                System.out.println("2. Try Another");

                choice = new Methods().readValidInt("Choice: ");

                switch (choice) {
                    case 1 -> new Country().add();
                    case 2 -> { }
                    default -> System.out.println("Invalid Input.");
                }
            } else {
                cID = rs.getInt(1);
                break;
            }
        }

        while (true) {
            System.out.print("State name : ");
            sName = sc.nextLine().trim();
            if (sName.isBlank()) return;

            String sql = "SELECT * FROM STATES WHERE STATE_NAME=? AND COUNTRY_ID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, sName);
            pst.setInt(2, cID);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                sql = "SELECT COALESCE(MAX(STATE_CODE), 0) + 1 FROM STATES WHERE COUNTRY_ID = ?";
                PreparedStatement spst = con.prepareStatement(sql);
                spst.setInt(1, cID);
                ResultSet srs = spst.executeQuery();
                if (srs.next()) {
                    sCode = srs.getInt(1);
                } else {
                    sCode = 1;
                }
                break;
            } else {
                System.out.println("State Already Added");
            }
        }

        String sql = "INSERT INTO `STATES`(`STATE_CODE`, `STATE_NAME`, `COUNTRY_ID`) VALUES (?,?,?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, sCode);
        pst.setString(2, sName);
        pst.setInt(3, cID);
        int rs1 = pst.executeUpdate();
        System.out.println(rs1 != 0 ? "State Added" : "Failed");
    }

    @Override
    public void edit() throws Exception {
        String nS;
        int sID;

        view();
        while (true) {
            sID = new Methods().readValidInt("Enter State ID (or 0 to cancel): ");
            if (sID == 0) return;

            String sql = "SELECT `STATE_ID`, `STATE_NAME` FROM STATES WHERE STATE_ID = " + sID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("Current Name : " + rs.getString(2));
                System.out.print("New Name : ");
                nS = sc.nextLine().trim();
                break;
            } else {
                System.out.println("Invalid State Id");
            }
        }

        String sql = "UPDATE `STATES` SET `STATE_NAME` = ? WHERE STATE_ID = ?";
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, nS);
        st.setInt(2, sID);
        int rs = st.executeUpdate();
        System.out.println(rs > 0 ? "State Updated Successfully" : "Failed");
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            sID = new Methods().readValidInt("Enter State ID (or 0 to cancel): ");
            if (sID == 0) return;

            String sql = "SELECT `STATE_ID`, `STATE_NAME` FROM STATES WHERE STATE_ID = " + sID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("Current Name : " + rs.getString(2));
                System.out.println("All Cities Associated with the State will be Deleted.");
                System.out.println("1. Continue");
                System.out.println("2. Back");

                choice = new Methods().readValidInt("Choice: ");

                switch (choice) {
                    case 1 -> {
                        boolean autoCommitState = con.getAutoCommit();
                        try {
                            con.setAutoCommit(false);

                            String city = "DELETE FROM `CITIES` WHERE STATE_ID = ?";
                            String state = "DELETE FROM `STATES` WHERE STATE_ID = ?";

                            PreparedStatement ct = con.prepareStatement(city);
                            ct.setInt(1, sID);
                            ct.executeUpdate();

                            PreparedStatement st = con.prepareStatement(state);
                            st.setInt(1, sID);
                            int sr = st.executeUpdate();

                            if (sr > 0) {
                                con.commit();
                                System.out.println("State and associated Cities Deleted Successfully.");
                            } else {
                                con.rollback();
                                System.out.println("Failed to Delete State.");
                            }
                        } catch (Exception e) {
                            con.rollback();
                            System.out.println("Error deleting state: " + e.getMessage());
                        } finally {
                            con.setAutoCommit(autoCommitState);
                        }
                    }
                    case 2 -> { return; }
                    default -> System.out.println("Invalid Choice.");
                }
                break;
            } else {
                System.out.println("Invalid State Id");
            }
        }
    }
}