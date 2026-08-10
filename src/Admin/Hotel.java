package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Hotel extends connection implements Manageable {
    Scanner sc = new Scanner(System.in);
    int choice, hID;
    String hName, hCity, hType, address, contactNo, email, description, n = "";
    double rating;
    long cID;

    // Helper method to resolve City Name or Pincode to City ID and Name
    private long[] resolveCityToId(String prompt) throws Exception {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return null;

            // 1. Pincode Lookup
            if (input.matches("\\d+")) {
                if (!input.matches("\\d{6}")) {
                    System.out.println("Invalid pincode length! Pincodes must be 6 digits. Try again.\n");
                    continue;
                }
                String pinSql = "SELECT city_id, city_name FROM view_pincode_location WHERE pincode = ? LIMIT 1";
                try (PreparedStatement pst = con.prepareStatement(pinSql)) {
                    pst.setString(1, input);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        long cityId = rs.getLong("city_id");
                        String cityName = rs.getString("city_name");
                        System.out.println("-> Detected City: " + cityName);
                        hCity = cityName;
                        return new long[]{cityId};
                    } else {
                        System.out.println("Pincode not found in database. Please enter a valid Pincode or City Name.\n");
                        continue;
                    }
                }
            }

            // 2. City Name Check
            String citySql = "SELECT city_id, city_name FROM cities WHERE LOWER(city_name) = LOWER(?) LIMIT 1";
            try (PreparedStatement pst = con.prepareStatement(citySql)) {
                pst.setString(1, input);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    hCity = rs.getString("city_name");
                    return new long[]{rs.getLong("city_id")};
                } else {
                    String vSql = "SELECT city_id, city_name FROM view_pincode_location WHERE LOWER(city_name) = LOWER(?) LIMIT 1";
                    try (PreparedStatement vPst = con.prepareStatement(vSql)) {
                        vPst.setString(1, input);
                        ResultSet vRs = vPst.executeQuery();
                        if (vRs.next()) {
                            hCity = vRs.getString("city_name");
                            return new long[]{vRs.getLong("city_id")};
                        }
                    }

                    System.out.println("City '" + input + "' Not Available");
                    System.out.println("1. Add City to Database");
                    System.out.println("2. Try Another");

                    int opt = new Methods().readValidInt("Choice: ");
                    if (opt == 1) {
                        new City().add();
                    }
                }
            }
        }
    }

    @Override
    public void view() throws Exception {
        String sql = "SELECT h.hotel_id, h.hotel_name, c.city_name, h.hotel_type, h.rating, " +
                "h.check_in, h.check_out, h.contact_no, h.email, h.address " +
                "FROM hotels h JOIN cities c ON h.city_id = c.city_id ORDER BY h.hotel_id";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tHotel Name\t\tCity Name\tType\t\tRating\tCheck-In\tCheck-Out\tContact\t\tEmail\t\t\tAddress");
        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt("hotel_id") + "\t" +
                            rs.getString("hotel_name") + "\t\t" +
                            rs.getString("city_name") + "\t" +
                            rs.getString("hotel_type") + "\t\t" +
                            rs.getBigDecimal("rating") + "\t" +
                            rs.getTime("check_in") + "\t" +
                            rs.getTime("check_out") + "\t" +
                            rs.getString("contact_no") + "\t" +
                            rs.getString("email") + "\t" +
                            rs.getString("address")
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        long[] result = resolveCityToId("City Name / Pincode : ");
        if (result == null) return;
        cID = result[0];

        System.out.print("Hotel Name : ");
        hName = sc.nextLine().trim();

        System.out.print("Address : ");
        address = sc.nextLine().trim();

        hType = readHotelType();

        while (true) {
            System.out.print("Rating (0.0 - 5.0) : ");
            try {
                rating = Double.parseDouble(sc.nextLine().trim());
                if (rating < 0.0 || rating > 5.0) {
                    System.out.println("Rating must be between 0.0 and 5.0");
                    continue;
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid Rating");
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime checkIn;

        while (true) {
            System.out.print("Check-In Time (HH:mm): ");
            String input = sc.nextLine().trim();

            try {
                checkIn = LocalTime.parse(input, formatter);
                break;
            } catch (Exception e) {
                System.out.println("Invalid Time");
            }
        }

        LocalTime checkOut;

        while (true) {
            System.out.print("Check-Out Time (HH:mm): ");
            String input = sc.nextLine().trim();

            try {
                checkOut = LocalTime.parse(input, formatter);
                break;
            } catch (Exception e) {
                System.out.println("Invalid Time");
            }
        }

        contactNo = readContactNo();
        email = readEmail();

        System.out.print("Description : ");
        description = sc.nextLine().trim();

        String insert = "INSERT INTO `hotels`(`hotel_name`, `city_id`, `address`, `hotel_type`, `rating`, `check_in`, `check_out`, `contact_no`, `email`, `description`)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insert);
        pst.setString(1, hName);
        pst.setLong(2, cID);
        pst.setString(3, address);
        pst.setString(4, hType);
        pst.setDouble(5, rating);
        pst.setObject(6, checkIn);
        pst.setObject(7, checkOut);
        pst.setString(8, contactNo);
        pst.setString(9, email);
        pst.setString(10, description);
        int rs = pst.executeUpdate();
        System.out.println(rs > 0 ? "Hotel Added Successfully!" : "Failed to add Hotel.");
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            hID = new Methods().readValidInt("Enter Hotel ID to Edit (or 0 to cancel): ");
            if (hID == 0) return;

            String sql = "SELECT h.hotel_id, c.city_name, h.address, h.hotel_type, h.rating, h.check_in, " +
                    "h.check_out, h.contact_no, h.email, h.description " +
                    "FROM hotels h JOIN cities c ON h.city_id = c.city_id WHERE h.hotel_id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, hID);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\nID : " + rs.getInt("hotel_id"));
                System.out.println("1. City Name = " + rs.getString("city_name"));
                System.out.println("2. Address = " + rs.getString("address"));
                System.out.println("3. Hotel Type = " + rs.getString("hotel_type"));
                System.out.println("4. Rating = " + rs.getBigDecimal("rating"));
                System.out.println("5. Check-In = " + rs.getTime("check_in"));
                System.out.println("6. Check-Out = " + rs.getTime("check_out"));
                System.out.println("7. Contact No = " + rs.getString("contact_no"));
                System.out.println("8. Email = " + rs.getString("email"));
                System.out.println("9. Description = " + rs.getString("description"));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    String columnName = "";
                    boolean isCityChange = false;
                    long newCityId = 0;

                    switch (choice) {
                        case 1 -> {
                            long[] res = resolveCityToId("New City Name / Pincode : ");
                            if (res != null) {
                                newCityId = res[0];
                                isCityChange = true;
                            }
                        }
                        case 2 -> {
                            System.out.print("New Address : ");
                            n = sc.nextLine().trim();
                            columnName = "address";
                        }
                        case 3 -> {
                            n = readHotelType();
                            columnName = "hotel_type";
                        }
                        case 4 -> {
                            while (true) {
                                System.out.print("New Rating (0.0 - 5.0) : ");
                                try {
                                    double r = Double.parseDouble(sc.nextLine().trim());
                                    if (r < 0.0 || r > 5.0) {
                                        System.out.println("Rating must be between 0.0 and 5.0");
                                        continue;
                                    }
                                    n = "" + r;
                                    columnName = "rating";
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Rating");
                                }
                            }
                        }
                        case 5 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                            LocalTime checkIn;

                            while (true) {
                                System.out.print("Check-In Time (HH:mm): ");
                                String input = sc.nextLine().trim();

                                try {
                                    checkIn = LocalTime.parse(input, formatter);
                                    n = checkIn.toString();
                                    columnName = "check_in";
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Time Format");
                                }
                            }
                        }
                        case 6 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                            LocalTime checkOut;

                            while (true) {
                                System.out.print("Check-Out Time (HH:mm): ");
                                String input = sc.nextLine().trim();

                                try {
                                    checkOut = LocalTime.parse(input, formatter);
                                    n = checkOut.toString();
                                    columnName = "check_out";
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Invalid Time Format");
                                }
                            }
                        }
                        case 7 -> {
                            n = readContactNo();
                            columnName = "contact_no";
                        }
                        case 8 -> {
                            n = readEmail();
                            columnName = "email";
                        }
                        case 9 -> {
                            System.out.print("New Description : ");
                            n = sc.nextLine().trim();
                            columnName = "description";
                        }
                        default -> {
                            System.out.println("Invalid Choice");
                            continue;
                        }
                    }

                    int updatedRows = 0;
                    if (isCityChange) {
                        PreparedStatement upst = con.prepareStatement("UPDATE `hotels` SET `city_id` = ? WHERE hotel_id = ?");
                        upst.setLong(1, newCityId);
                        upst.setInt(2, hID);
                        updatedRows = upst.executeUpdate();
                    } else {
                        String fSql = "UPDATE `hotels` SET `" + columnName + "` = ? WHERE hotel_id = ?";
                        PreparedStatement upst = con.prepareStatement(fSql);
                        upst.setString(1, n);
                        upst.setInt(2, hID);
                        updatedRows = upst.executeUpdate();
                    }

                    System.out.println(updatedRows > 0 ? "Hotel Updated Successfully!" : "Failed to Update Hotel.");
                    break;
                }
                break;
            } else {
                System.out.println("Invalid Hotel Id.");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            hID = new Methods().readValidInt("Enter Hotel ID to Delete (or 0 to cancel): ");
            if (hID == 0) return;

            PreparedStatement checkSt = con.prepareStatement("SELECT hotel_name FROM hotels WHERE hotel_id = ?");
            checkSt.setInt(1, hID);
            ResultSet checkRs = checkSt.executeQuery();

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement cancelBookings = con.prepareStatement("UPDATE hotel_booking SET status = 'CANCELED' WHERE hotel_id = ?");
                    cancelBookings.setInt(1, hID);
                    int affectedUsers = cancelBookings.executeUpdate();

                    PreparedStatement delPkgHotels = con.prepareStatement("DELETE FROM packages_hotels WHERE hotel_id = ?");
                    delPkgHotels.setInt(1, hID);
                    delPkgHotels.executeUpdate();

                    PreparedStatement delRooms = con.prepareStatement("DELETE FROM rooms WHERE hotel_id = ?");
                    delRooms.setInt(1, hID);
                    delRooms.executeUpdate();

                    PreparedStatement delHotel = con.prepareStatement("DELETE FROM hotels WHERE hotel_id = ?");
                    delHotel.setInt(1, hID);
                    int r = delHotel.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Hotel Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Hotel.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting hotel: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Hotel ID.");
            }
        }
    }

    private String readHotelType() {
        while (true) {
            System.out.println("\nHotel type :-");
            System.out.println("1. Budget");
            System.out.println("2. Standard");
            System.out.println("3. Deluxe");
            System.out.println("4. Resort");
            System.out.println("5. Suite");

            choice = new Methods().readValidInt("Choice: ");

            switch (choice) {
                case 1 -> { return "Budget"; }
                case 2 -> { return "Standard"; }
                case 3 -> { return "Deluxe"; }
                case 4 -> { return "Resort"; }
                case 5 -> { return "Suite"; }
                default -> System.out.println("Invalid Choice");
            }
        }
    }

    private String readContactNo() {
        String mNumber;
        while (true) {
            System.out.print("Contact no : ");
            boolean vCheck = true;
            String thing = sc.nextLine().trim();
            char[] thi = thing.toCharArray();
            if (thi.length == 10) {
                for (char c : thi) {
                    if (!Character.isDigit(c)) {
                        vCheck = false;
                    }
                }
                if (vCheck) {
                    if (thi[0] == '6' || thi[0] == '7' || thi[0] == '8' || thi[0] == '9') {
                        mNumber = thing;
                        break;
                    } else {
                        System.out.println("Invalid Input, Indian Number Format.");
                    }
                } else {
                    System.out.println("Invalid Input, Only Digits Allowed.");
                }
            } else {
                System.out.println("Invalid Input, Must contain 10 Digits Only.");
            }
        }
        return mNumber;
    }

    private String readEmail() {
        while (true) {
            System.out.print("Email : ");
            String input = sc.nextLine().trim();

            int at = input.indexOf('@');
            int dot = input.lastIndexOf('.');

            if (at <= 0) {
                System.out.println("Invalid Email!!!");
                continue;
            }

            if (dot <= at + 1) {
                System.out.println("Invalid Email!!!");
                continue;
            }

            if (dot == input.length() - 1) {
                System.out.println("Invalid Email!!!");
                continue;
            }
            return input;
        }
    }
}