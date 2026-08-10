package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.util.Scanner;

public class HolidayPackage extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    String packageName, description;
    double price;
    int durationDays, availableBookings, pID, choice;

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM holiday_packages ORDER BY pack_id");

        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tPackage Name\t\tPrice\t\tDuration\tAvailable\tDescription");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("pack_id") + "\t" +
                            rs.getString("Package_Name") + "\t\t$" +
                            rs.getDouble("package_price") + "\t" +
                            rs.getInt("duration_days") + " days\t" +
                            rs.getInt("available_bookings") + "\t\t" +
                            rs.getString("description")
            );
        }
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        System.out.print("Enter Package Name: ");
        packageName = sc.nextLine().trim();

        while (true) {
            System.out.print("Enter Package Price: ");
            try {
                price = Double.parseDouble(sc.nextLine());
                if (price > 0) break;
                System.out.println("Price must be greater than 0.");
            } catch (Exception e) {
                System.out.println("Invalid Price!");
            }
        }

        durationDays = new Methods().readValidInt("Enter Duration (in Days): ");
        availableBookings = new Methods().readValidInt("Enter Available Bookings Count: ");

        System.out.print("Enter Package Description: ");
        description = sc.nextLine().trim();

        String sql = "INSERT INTO holiday_packages (Package_Name, package_price, duration_days, available_bookings, description) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, packageName);
        pst.setDouble(2, price);
        pst.setInt(3, durationDays);
        pst.setInt(4, availableBookings);
        pst.setString(5, description);

        int result = pst.executeUpdate();
        System.out.println(result > 0 ? "Holiday Package Added Successfully!" : "Failed to Add Holiday Package.");
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            pID = new Methods().readValidInt("Enter Package ID to Edit (or 0 to cancel): ");
            if (pID == 0) return;

            String sql = "SELECT * FROM holiday_packages WHERE pack_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, pID);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\nCurrent Details:");
                System.out.println("1. Package Name: " + rs.getString("Package_Name"));
                System.out.println("2. Price: $" + rs.getDouble("package_price"));
                System.out.println("3. Duration: " + rs.getInt("duration_days") + " days");
                System.out.println("4. Available Bookings: " + rs.getInt("available_bookings"));
                System.out.println("5. Description: " + rs.getString("description"));

                choice = new Methods().readValidInt("Enter Field Number to Edit: ");

                String colName = "";
                String newValueStr = "";
                double newDoubleVal = 0;
                int newIntVal = 0;
                boolean isNumber = false;

                switch (choice) {
                    case 1 -> {
                        System.out.print("New Package Name: ");
                        newValueStr = sc.nextLine().trim();
                        colName = "Package_Name";
                    }
                    case 2 -> {
                        System.out.print("New Price: ");
                        newDoubleVal = Double.parseDouble(sc.nextLine());
                        colName = "package_price";
                        isNumber = true;
                    }
                    case 3 -> {
                        newIntVal = new Methods().readValidInt("New Duration (Days): ");
                        colName = "duration_days";
                        isNumber = true;
                    }
                    case 4 -> {
                        newIntVal = new Methods().readValidInt("New Available Bookings: ");
                        colName = "available_bookings";
                        isNumber = true;
                    }
                    case 5 -> {
                        System.out.print("New Description: ");
                        newValueStr = sc.nextLine().trim();
                        colName = "description";
                    }
                    default -> {
                        System.out.println("Invalid Field Choice.");
                        continue;
                    }
                }

                String updateSql = "UPDATE holiday_packages SET `" + colName + "` = ? WHERE pack_id = ?";
                PreparedStatement upst = con.prepareStatement(updateSql);

                if (isNumber) {
                    if (colName.equals("package_price")) upst.setDouble(1, newDoubleVal);
                    else upst.setInt(1, newIntVal);
                } else {
                    upst.setString(1, newValueStr);
                }
                upst.setInt(2, pID);

                int updatedRows = upst.executeUpdate();
                System.out.println(updatedRows > 0 ? "Package Updated Successfully!" : "Failed to Update Package.");
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
            pID = new Methods().readValidInt("Enter Package ID to Delete (or 0 to cancel): ");
            if (pID == 0) return;

            PreparedStatement checkSt = con.prepareStatement("SELECT Package_Name FROM holiday_packages WHERE pack_id = ?");
            checkSt.setInt(1, pID);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement cancelBookings = con.prepareStatement("UPDATE package_booking SET status = 'CANCELED' WHERE Package_id = ?");
                    cancelBookings.setInt(1, pID);
                    cancelBookings.executeUpdate();

                    PreparedStatement delPkgHotels = con.prepareStatement("DELETE FROM packages_hotels WHERE pack_id = ?");
                    delPkgHotels.setInt(1, pID);
                    delPkgHotels.executeUpdate();

                    PreparedStatement delPkg = con.prepareStatement("DELETE FROM holiday_packages WHERE pack_id = ?");
                    delPkg.setInt(1, pID);
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
}