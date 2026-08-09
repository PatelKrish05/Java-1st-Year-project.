import JDBC.connection;

import java.sql.*;
import java.util.Scanner;

public class City extends connection implements Manageable {

    String sName, cName, tName;
    int choice = 0, cId = 0, sId = 0, sCode = 0;
    long tId = 0;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM cities ORDER BY CITY_NAME");

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("City ID\t\tCity Name\t\tState ID\tCountry ID");
        System.out.println("--------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t\t" +
                            rs.getString(2) + "\t\t" +
                            rs.getInt(3) + "\t\t" +
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
                cId = rs.getInt(1);
                break;
            }
        }

        while (true) {
            System.out.print("State name : ");
            sName = sc.nextLine().trim();
            if (sName.isBlank()) return;

            String sql = "SELECT * FROM STATES WHERE STATE_NAME=? AND COUNTRY_ID=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, sName);
            pst.setInt(2, cId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                System.out.println("State Not Available");
                System.out.println("1. Add State to Database");
                System.out.println("2. Try Another");

                choice = new Methods().readValidInt("Choice: ");

                switch (choice) {
                    case 1 -> new State().add();
                    case 2 -> { }
                    default -> System.out.println("Invalid Input.");
                }
            } else {
                sId = rs.getInt(1);
                break;
            }
        }

        while (true) {
            System.out.print("City name : ");
            tName = sc.nextLine().trim();
            if (tName.isBlank()) return;

            String sql = "SELECT * FROM CITIES WHERE CITY_NAME=? AND STATE_ID = ? AND COUNTRY_ID = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tName);
            pst.setInt(2, sId);
            pst.setInt(3, cId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                String cSql = "SELECT COALESCE(MAX(CITY_ID), 0) + 1 FROM CITIES";
                Statement spst = con.createStatement();
                ResultSet srs = spst.executeQuery(cSql);
                if (srs.next()) {
                    tId = srs.getLong(1);
                } else {
                    tId = 1;
                }
                break;
            } else {
                System.out.println("City Already Added");
            }
        }

        String sql = "INSERT INTO `CITIES`(`CITY_ID`, `CITY_NAME`, `STATE_ID`, `COUNTRY_ID`) VALUES (?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setLong(1, tId);
        pst.setString(2, tName);
        pst.setInt(3, sId);
        pst.setInt(4, cId);
        int rs1 = pst.executeUpdate();
        System.out.println(rs1 != 0 ? "City Added" : "Failed");
    }

    @Override
    public void edit() throws Exception {
        String nT;
        int tID;

        view();
        while (true) {
            tID = new Methods().readValidInt("Enter City ID (or 0 to cancel): ");
            if (tID == 0) return;

            String sql = "SELECT `CITY_ID`, `CITY_NAME` FROM CITIES WHERE CITY_ID = " + tID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Current Name : " + rs.getString(2));
                System.out.print("New Name : ");
                nT = sc.nextLine().trim();
                break;
            } else {
                System.out.println("Invalid City Id");
            }
        }

        String sql = "UPDATE `CITIES` SET `CITY_NAME` = ? WHERE CITY_ID = ?";
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, nT);
        st.setInt(2, tID);
        int rs = st.executeUpdate();
        System.out.println(rs > 0 ? "City Updated Successfully" : "Failed");
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            tId = new Methods().readValidInt("Enter City ID (or 0 to cancel): ");
            if (tId == 0) return;

            String sql = "SELECT `CITY_ID`, `CITY_NAME` FROM CITIES WHERE CITY_ID = " + tId;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("City Name : " + rs.getString(2));

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    String delSql = "DELETE FROM `CITIES` WHERE CITY_ID = ?";
                    PreparedStatement pst1 = con.prepareStatement(delSql);
                    pst1.setLong(1, tId);
                    int r = pst1.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("City Deleted Successfully");
                    } else {
                        con.rollback();
                        System.out.println("Deletion Failed");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting city: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid City Id");
            }
        }
    }
}