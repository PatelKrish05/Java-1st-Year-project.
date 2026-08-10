package Admin;

import JDBC.connection;
import java.sql.*;
import java.util.Scanner;

public class ReportService extends connection {
    private final Scanner sc = new Scanner(System.in);

    public void showReportMenu() {
        while (true) {
            System.out.println("\n===================================");
            System.out.println("          REPORTS MENU             ");
            System.out.println("===================================");
            System.out.println("1. View Category Sales & Revenue Summary");
            System.out.println("2. View All Users Booking History");
            System.out.println("3. View Specific User Booking History");
            System.out.println("4. Exit / Back");
            System.out.println("===================================");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number.");
                continue;
            }

            switch (choice) {
                case 1 -> getSalesSummaryReport();
                case 2 -> getUserBookingHistoryReport(0); // 0 explicitly means "All Users"
                case 3 -> {
                    System.out.print("Enter User ID: ");
                    try {
                        int userId = Integer.parseInt(sc.nextLine().trim());
                        if (userId <= 0) {
                            System.out.println("Invalid User ID! Please enter a positive User ID (e.g., 1, 2, 3).");
                        } else {
                            getUserBookingHistoryReport(userId);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid User ID format!");
                    }
                }
                case 4 -> {
                    System.out.println("Exiting Reports Menu...");
                    return;
                }
                default -> System.out.println("Invalid choice! Please select an option between 1 and 4.");
            }
        }
    }

    public void getSalesSummaryReport() {
        String sql = "SELECT * FROM view_category_sales_summary";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            System.out.println("\n=================================== CATEGORY SALES REPORT ===================================");
            System.out.printf("%-12s | %-18s | %-16s | %-20s%n", "Category", "Total Transactions", "Total Units Sold", "Total Revenue ($)");
            System.out.println("---------------------------------------------------------------------------------------------");

            double grandTotalRevenue = 0.0;
            int grandTotalUnits = 0, grandTotalTxns = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                int txns = rs.getInt("Total_Transactions");
                int units = rs.getInt("Total_Units_Sold");
                double revenue = rs.getDouble("Total_Revenue");

                grandTotalTxns += txns;
                grandTotalUnits += units;
                grandTotalRevenue += revenue;

                System.out.printf("%-12s | %-18d | %-16d | $%-19.2f%n", rs.getString("Category"), txns, units, revenue);
            }

            if (!hasData) {
                System.out.println("No confirmed sales records found.");
            } else {
                System.out.println("---------------------------------------------------------------------------------------------");
                System.out.printf("%-12s | %-18d | %-16d | $%-19.2f%n", "TOTAL", grandTotalTxns, grandTotalUnits, grandTotalRevenue);
            }
            System.out.println("=============================================================================================\n");
        } catch (Exception e) {
            System.out.println("Error generating sales report: " + e.getMessage());
        }
    }

    public void getUserBookingHistoryReport(int targetUserId) {
        String sql = "SELECT * FROM view_user_booking_history" +
                (targetUserId > 0 ? " WHERE user_id = ?" : "") +
                " ORDER BY user_id ASC, booking_date DESC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (targetUserId > 0) {
                pst.setInt(1, targetUserId);
            }
            ResultSet rs = pst.executeQuery();

            System.out.println("\n================================================ USER BOOKING HISTORY ================================================");
            System.out.printf("%-8s | %-15s | %-10s | %-30s | %-6s | %-10s | %-15s | %-10s%n",
                    "User ID", "Customer", "Category", "Service Details", "Units", "Amount", "Payment", "Status");
            System.out.println("----------------------------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d | %-15s | %-10s | %-30s | %-6d | $%-9.2f | %-15s | %-10s%n",
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("Service_Category"),
                        rs.getString("Details"),
                        rs.getInt("Units"),
                        rs.getDouble("Item_Total"),
                        rs.getString("Payment_Method"),
                        rs.getString("Status"));
            }

            if (!found) {
                System.out.println("No booking history records found for User ID: " + (targetUserId > 0 ? targetUserId : "ALL"));
            }
            System.out.println("================================================================================----------------------\n");
        } catch (Exception e) {
            System.out.println("Error fetching user history report: " + e.getMessage());
        }
    }
}