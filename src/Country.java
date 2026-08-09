import JDBC.connection;

import java.sql.*;
import java.util.Scanner;

public class Country extends connection implements Manageable {
    String cName , cCode, nc;
    int phone;
    int choice, cID;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM countries ORDER BY COUNTRY_NAME");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tCode\tCountry Name\t\tPhone");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t" +
                            rs.getString(3) + "\t\t\t\t\t" +
                            rs.getString(4)
            );
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

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
                phone = new Methods().readValidInt("Country Calling Code : ");
                cCode = "NX";
                sql = "INSERT INTO `countries`(`country_code`, `country_name`, `phone`) VALUES (?,?,?)";
                pst = con.prepareStatement(sql);
                pst.setString(1, cCode);
                pst.setString(2, cName);
                pst.setInt(3, phone);
                int rs1 = pst.executeUpdate();
                System.out.println(rs1 != 0 ? "Country Added" : "Failed");
                break;
            } else {
                System.out.println("Country Already Added");
            }
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            cID = new Methods().readValidInt("Enter Country ID (or 0 to cancel): ");
            if (cID == 0) return;

            String sql = "SELECT `COUNTRY_ID`, `COUNTRY_NAME` FROM COUNTRIES WHERE COUNTRY_ID = " + cID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Current Name : " + rs.getString(2));
                System.out.print("New Name : ");
                nc = sc.nextLine().trim();
                break;
            } else {
                System.out.println("Invalid Country Id");
            }
        }

        String sql = "UPDATE `COUNTRIES` SET `COUNTRY_NAME` = ? WHERE COUNTRY_ID = ?";
        PreparedStatement st = con.prepareStatement(sql);
        st.setString(1, nc);
        st.setInt(2, cID);
        int rs = st.executeUpdate();
        System.out.println(rs > 0 ? "Country Updated Successfully" : "Failed");
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            cID = new Methods().readValidInt("Enter Country ID (or 0 to cancel): ");
            if (cID == 0) return;

            String sql = "SELECT `COUNTRY_ID`, `COUNTRY_NAME` FROM COUNTRIES WHERE COUNTRY_ID = " + cID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("Current Name : " + rs.getString(2));
                System.out.println("All States and Cities Associated with the Country will be Deleted.");
                System.out.println("1. Continue");
                System.out.println("2. Back");

                choice = new Methods().readValidInt("Choice: ");

                switch (choice) {
                    case 1 -> {
                        boolean autoCommitState = con.getAutoCommit();
                        try {
                            con.setAutoCommit(false);

                            String city = "DELETE FROM `CITIES` WHERE STATE_ID IN (SELECT STATE_ID FROM `STATES` WHERE COUNTRY_ID = ?)";
                            String state = "DELETE FROM `STATES` WHERE COUNTRY_ID = ?";
                            String country = "DELETE FROM `COUNTRIES` WHERE COUNTRY_ID = ?";

                            PreparedStatement ct = con.prepareStatement(city);
                            ct.setInt(1, cID);
                            ct.executeUpdate();

                            PreparedStatement st = con.prepareStatement(state);
                            st.setInt(1, cID);
                            st.executeUpdate();

                            PreparedStatement cy = con.prepareStatement(country);
                            cy.setInt(1, cID);
                            int cyr = cy.executeUpdate();

                            if (cyr > 0) {
                                con.commit();
                                System.out.println("Country and associated States/Cities Deleted Successfully.");
                            } else {
                                con.rollback();
                                System.out.println("Failed to Delete Country.");
                            }
                        } catch (Exception e) {
                            con.rollback();
                            System.out.println("Error deleting country: " + e.getMessage());
                        } finally {
                            con.setAutoCommit(autoCommitState);
                        }
                    }
                    case 2 -> { return; }
                    default -> System.out.println("Invalid Choice.");
                }
                break;
            } else {
                System.out.println("Invalid Country Id");
            }
        }
    }
}