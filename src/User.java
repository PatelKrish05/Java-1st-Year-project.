import java.sql.*;
import JDBC.connection;

import java.text.SimpleDateFormat;
import java.util.Scanner;

class User extends connection {
    static Scanner sc = new Scanner(System.in);

    // ================= HELPER INPUT METHODS =================

    private int readValidInt(String message) {
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return 0;
            try {
                int val = Integer.parseInt(input);
                if (val < 0) {
                    System.out.println("Value cannot be negative.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid whole number.");
            }
        }
    }

    private Date readValidDate(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        while (true) {
            System.out.print(message);
            String input = sc.nextLine().trim();
            if (input.isBlank()) return null;

            try {
                java.util.Date parsed = sdf.parse(input);
                java.util.Date today = sdf.parse(sdf.format(new java.util.Date()));

                if (parsed.before(today)) {
                    System.out.println("Date cannot be in the past. Please enter a current or future date.");
                    continue;
                }
                return new Date(parsed.getTime());
            } catch (Exception e) {
                System.out.println("Invalid date format! Please use YYYY-MM-DD.");
            }
        }
    }

    // ================= USER PANEL MENU =================

    public void userPanel(int User_id) throws Exception {
        while (true) {
            System.out.println("\n--- USER PANEL ---");
            System.out.println("1. Book Flight");
            System.out.println("2. Book Train");
            System.out.println("3. Book Bus");
            System.out.println("4. Book Hotel");
            System.out.println("5. Holiday Package Combo");
            System.out.println("6. Cancel Bookings");
            System.out.println("7. My Trips");
            System.out.println("8. Back");

            int choice = readValidInt("Choice: ");

            switch (choice) {
                case 1 -> fBooking(User_id);
                case 2 -> tBooking(User_id);
                case 3 -> bBooking(User_id);
                case 4 -> hBooking(User_id);
                case 5 -> pBooking(User_id);
                case 6 -> cancelMenu(User_id);
                case 7 -> myTrip(User_id);
                case 8 -> { return; }
                default -> System.out.println("Invalid Choice. Try again.");
            }
        }
    }

    // ================= FLIGHT BOOKING =================

    void fBooking(int userid) throws Exception {
        System.out.println("\n=== FLIGHT BOOKING ===");
        int tickets = readValidInt("How many tickets do you need? (or 0 to cancel): ");
        if (tickets <= 0) return;

        new Flight().view();
        while (true) {
            int fId = readValidInt("Enter Flight Id (or 0 to cancel): ");
            if (fId == 0) return;

            PreparedStatement st = con.prepareStatement("SELECT Available_Tickets FROM `flights` WHERE Flight_id = ?");
            st.setInt(1, fId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int maxT = rs.getInt("Available_Tickets");
                if (tickets > maxT) {
                    System.out.println("Booking failed: Only " + maxT + " ticket(s) remaining.");
                    continue;
                }

                int cId = 0, cn = 0;
                PreparedStatement psc = con.prepareStatement("SELECT * FROM `cabs` WHERE F_id = ?");
                psc.setInt(1, fId);
                ResultSet crs = psc.executeQuery();

                if (crs.next()) {
                    cId = crs.getInt("Cab_id");
                    System.out.println("\nWant a Cab Ride To Airport?\n1. Yes\n2. No");
                    if (readValidInt("Choice: ") == 1) {
                        int ava = crs.getInt("Availability");
                        cn = readValidInt("Cabs needed: ");
                        if (cn > ava) {
                            System.out.println("Only " + ava + " cab(s) available. Proceeding without cab.");
                            cn = 0; cId = 0;
                        }
                    } else { cId = 0; }
                }

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement uFlight = con.prepareStatement("UPDATE `flights` SET `Available_Tickets` = Available_Tickets - ? WHERE Flight_id = ?");
                    uFlight.setInt(1, tickets);
                    uFlight.setInt(2, fId);
                    uFlight.executeUpdate();

                    if (cn > 0 && cId > 0) {
                        PreparedStatement uCab = con.prepareStatement("UPDATE `cabs` SET `Availability` = Availability - ? WHERE Cab_id = ?");
                        uCab.setInt(1, cn);
                        uCab.setInt(2, cId);
                        uCab.executeUpdate();
                    }

                    PreparedStatement fst = con.prepareStatement(
                            "INSERT INTO `flight_booking` (`user_id`, `flight_id`, `f_tickets`, `cab_id`, `nCab`, `status`) VALUES (?,?,?,?,?,'PENDING')");
                    fst.setInt(1, userid);
                    fst.setInt(2, fId);
                    fst.setInt(3, tickets);
                    fst.setInt(4, cId);
                    fst.setInt(5, cn);
                    fst.executeUpdate();

                    con.commit();
                    System.out.println("Flight Reserved! Go to 'My Trips' to confirm and pay.");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Booking Failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Flight ID.");
            }
        }
    }

    // ================= TRAIN BOOKING =================

    void tBooking(int userid) throws Exception {
        System.out.println("\n=== TRAIN BOOKING ===");
        int tickets = readValidInt("How many tickets do you need? (or 0 to cancel): ");
        if (tickets <= 0) return;

        new Train().view();
        while (true) {
            int tId = readValidInt("Enter Train Id (or 0 to cancel): ");
            if (tId == 0) return;

            PreparedStatement st = con.prepareStatement("SELECT Available_Tickets FROM `trains` WHERE train_id = ?");
            st.setInt(1, tId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int maxT = rs.getInt("Available_Tickets");
                if (tickets > maxT) {
                    System.out.println("Booking failed: Only " + maxT + " ticket(s) remaining.");
                    continue;
                }

                int cId = 0, cn = 0;
                PreparedStatement psc = con.prepareStatement("SELECT * FROM `cabs` WHERE T_id = ?");
                psc.setInt(1, tId);
                ResultSet crs = psc.executeQuery();

                if (crs.next()) {
                    cId = crs.getInt("Cab_id");
                    System.out.println("\nWant a Cab Ride To Station?\n1. Yes\n2. No");
                    if (readValidInt("Choice: ") == 1) {
                        int ava = crs.getInt("Availability");
                        cn = readValidInt("Cabs needed: ");
                        if (cn > ava) {
                            System.out.println("Only " + ava + " cab(s) available. Proceeding without cab.");
                            cn = 0; cId = 0;
                        }
                    } else { cId = 0; }
                }

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement uTrain = con.prepareStatement("UPDATE `trains` SET `Available_Tickets` = Available_Tickets - ? WHERE train_id = ?");
                    uTrain.setInt(1, tickets);
                    uTrain.setInt(2, tId);
                    uTrain.executeUpdate();

                    if (cn > 0 && cId > 0) {
                        PreparedStatement uCab = con.prepareStatement("UPDATE `cabs` SET `Availability` = Availability - ? WHERE Cab_id = ?");
                        uCab.setInt(1, cn);
                        uCab.setInt(2, cId);
                        uCab.executeUpdate();
                    }

                    PreparedStatement fst = con.prepareStatement("INSERT INTO `train_booking` (`user_id`, `train_id`, `t_tickets`, `cab_id`, `nCab`, `status`) VALUES (?,?,?,?,?,'PENDING')");
                    fst.setInt(1, userid);
                    fst.setInt(2, tId);
                    fst.setInt(3, tickets);
                    fst.setInt(4, cId);
                    fst.setInt(5, cn);
                    fst.executeUpdate();

                    con.commit();
                    System.out.println("Train Reserved! Go to 'My Trips' to confirm and pay.");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Booking Failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Train ID.");
            }
        }
    }

    // ================= BUS BOOKING =================

    void bBooking(int userid) throws Exception {
        System.out.println("\n=== BUS BOOKING ===");
        int tickets = readValidInt("How many tickets do you need? (or 0 to cancel): ");
        if (tickets <= 0) return;

        new Bus().view();
        while (true) {
            int bId = readValidInt("Enter Bus Id (or 0 to cancel): ");
            if (bId == 0) return;

            PreparedStatement st = con.prepareStatement("SELECT Available_Tickets FROM `buses` WHERE bus_id = ?");
            st.setInt(1, bId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int maxT = rs.getInt("Available_Tickets");
                if (tickets > maxT) {
                    System.out.println("Booking failed: Only " + maxT + " ticket(s) remaining.");
                    continue;
                }

                int cId = 0, cn = 0;
                PreparedStatement psc = con.prepareStatement("SELECT * FROM `cabs` WHERE B_id = ?");
                psc.setInt(1, bId);
                ResultSet crs = psc.executeQuery();

                if (crs.next()) {
                    cId = crs.getInt("Cab_id");
                    System.out.println("\nWant a Cab Ride To Station?\n1. Yes\n2. No");
                    if (readValidInt("Choice: ") == 1) {
                        int ava = crs.getInt("Availability");
                        cn = readValidInt("Cabs needed: ");
                        if (cn > ava) {
                            System.out.println("Only " + ava + " cab(s) available. Proceeding without cab.");
                            cn = 0; cId = 0;
                        }
                    } else { cId = 0; }
                }

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement uBus = con.prepareStatement("UPDATE `buses` SET `Available_Tickets` = Available_Tickets - ? WHERE bus_id = ?");
                    uBus.setInt(1, tickets);
                    uBus.setInt(2, bId);
                    uBus.executeUpdate();

                    if (cn > 0 && cId > 0) {
                        PreparedStatement uCab = con.prepareStatement("UPDATE `cabs` SET `Availability` = Availability - ? WHERE Cab_id = ?");
                        uCab.setInt(1, cn);
                        uCab.setInt(2, cId);
                        uCab.executeUpdate();
                    }

                    PreparedStatement fst = con.prepareStatement("INSERT INTO `bus_booking` (`user_id`, `bus_id`, `b_tickets`, `cab_id`, `nCab`, `status`) VALUES (?,?,?,?,?,'PENDING')");
                    fst.setInt(1, userid);
                    fst.setInt(2, bId);
                    fst.setInt(3, tickets);
                    fst.setInt(4, cId);
                    fst.setInt(5, cn);
                    fst.executeUpdate();

                    con.commit();
                    System.out.println("Bus Reserved! Go to 'My Trips' to confirm and pay.");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Booking Failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Bus ID.");
            }
        }
    }

    // ================= HOTEL BOOKING =================

    void hBooking(int userid) throws Exception {
        System.out.println("\n=== HOTEL BOOKING ===");
        int guests = readValidInt("Enter number of guests (or 0 to cancel): ");
        if (guests <= 0) {
            System.out.println("Booking canceled.");
            return;
        }

        new Hotel().view();
        while (true) {
            int hId = readValidInt("Enter Hotel Id (or 0 to cancel): ");
            if (hId == 0) return;

            PreparedStatement st = con.prepareStatement("SELECT hotel_name FROM `hotels` WHERE hotel_id = ?");
            st.setInt(1, hId);
            ResultSet rs = st.executeQuery();

            if (!rs.next()) {
                System.out.println("Invalid Hotel ID.");
                continue;
            }

            String hotelName = rs.getString("hotel_name");

            System.out.println("\n--- Available Rooms for " + hotelName + " (Capacity >= " + guests + " guests) ---");
            PreparedStatement pstRooms = con.prepareStatement(
                    "SELECT room_id, room_type, max_persons, price_per_night, available_rooms " +
                            "FROM `rooms` WHERE hotel_id = ? AND max_persons >= ? AND available_rooms > 0");
            pstRooms.setInt(1, hId);
            pstRooms.setInt(2, guests);
            ResultSet rsRooms = pstRooms.executeQuery();

            System.out.println("Room ID\tRoom Type\tMax Guests\tPrice/Night\tAvailable Rooms");
            System.out.println("---------------------------------------------------------------------");
            boolean hasRooms = false;
            while (rsRooms.next()) {
                hasRooms = true;
                System.out.println(
                        rsRooms.getInt("room_id") + "\t" +
                                rsRooms.getString("room_type") + "\t\t" +
                                rsRooms.getInt("max_persons") + "\t\t" +
                                rsRooms.getInt("price_per_night") + "\t\t" +
                                rsRooms.getInt("available_rooms")
                );
            }
            System.out.println("---------------------------------------------------------------------");

            if (!hasRooms) {
                System.out.println("No suitable rooms available for " + guests + " guest(s) at this hotel.");
                continue;
            }

            int roomId = readValidInt("Enter Room ID to select (or 0 to cancel): ");
            if (roomId == 0) return;

            PreparedStatement checkRoom = con.prepareStatement(
                    "SELECT room_type, price_per_night, available_rooms FROM `rooms` WHERE room_id = ? AND hotel_id = ?");
            checkRoom.setInt(1, roomId);
            checkRoom.setInt(2, hId);
            ResultSet rsCheck = checkRoom.executeQuery();

            if (!rsCheck.next()) {
                System.out.println("Selected Room ID does not belong to this Hotel.");
                continue;
            }

            int availRooms = rsCheck.getInt("available_rooms");
            long pricePerNight = rsCheck.getLong("price_per_night");

            int roomsRequested = readValidInt("No. of Rooms: ");
            if (roomsRequested <= 0 || roomsRequested > availRooms) {
                System.out.println("Booking failed: Only " + availRooms + " room(s) available.");
                continue;
            }

            Date checkIn = readValidDate("Enter Check-in Date (YYYY-MM-DD): ");
            if (checkIn == null) return;

            Date checkOut = readValidDate("Enter Check-out Date (YYYY-MM-DD): ");
            if (checkOut == null) return;

            if (!checkOut.after(checkIn)) {
                System.out.println("Check-out date must be after Check-in date.");
                continue;
            }

            long diffMillis = checkOut.getTime() - checkIn.getTime();
            int numDays = (int) (diffMillis / (1000 * 60 * 60 * 24));
            long totalCost = numDays * pricePerNight * roomsRequested;

            System.out.println("\n--- Reservation Summary ---");
            System.out.println("Hotel: " + hotelName);
            System.out.println("Room Type: " + rsCheck.getString("room_type"));
            System.out.println("Rooms Booked: " + roomsRequested);
            System.out.println("Check-in: " + checkIn + " | Check-out: " + checkOut + " (" + numDays + " night(s))");
            System.out.println("Estimated Total: $" + totalCost);

            while (true) {
                int confirm = readValidInt("Add to My Trips as Pending? (1: Yes / 2: No): ");
                if (confirm == 2) {
                    System.out.println("Reservation Canceled.");
                    return;
                } else if (confirm == 1) {
                    break;
                } else {
                    System.out.println("Invalid Input");
                }
            }

            boolean autoCommitState = con.getAutoCommit();
            try {
                con.setAutoCommit(false);

                PreparedStatement uRoom = con.prepareStatement(
                        "UPDATE `rooms` SET `available_rooms` = available_rooms - ? WHERE room_id = ?");
                uRoom.setInt(1, roomsRequested);
                uRoom.setInt(2, roomId);
                uRoom.executeUpdate();

                PreparedStatement hst = con.prepareStatement(
                        "INSERT INTO `hotel_booking` (`user_id`, `hotel_id`, `room_id`, `Rooms`, `check_in`, `check_out`, `status`) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')");
                hst.setInt(1, userid);
                hst.setInt(2, hId);
                hst.setInt(3, roomId);
                hst.setInt(4, roomsRequested);
                hst.setDate(5, checkIn);
                hst.setDate(6, checkOut);
                hst.executeUpdate();

                con.commit();
                System.out.println("Hotel Reserved! Go to 'My Trips' to confirm and pay.");
            } catch (Exception e) {
                con.rollback();
                System.out.println("Reservation Failed: " + e.getMessage());
            } finally {
                con.setAutoCommit(autoCommitState);
            }
            break;
        }
    }

    // ================= PACKAGE BOOKING =================

    void pBooking(int userid) throws Exception {
        System.out.println("\n=== PACKAGE BOOKING ===");
        int travellers = readValidInt("Enter number of travellers (or 0 to cancel): ");
        if (travellers <= 0) return;

        new Package().view();
        while (true) {
            int pId = readValidInt("Enter Package Id (or 0 to cancel): ");
            if (pId == 0) return;

            PreparedStatement st = con.prepareStatement("SELECT * FROM `view_package_details` WHERE pack_id = ?");
            st.setInt(1, pId);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                int avail = rs.getInt("available_bookings");
                if (travellers > avail) {
                    System.out.println("Booking failed: Only " + avail + " capacity remaining.");
                    continue;
                }

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement hst = con.prepareStatement("INSERT INTO `package_booking` (`user_id`, `Package_id`, `p_person`, `status`) VALUES (?,?,?,'PENDING')");
                    hst.setInt(1, userid);
                    hst.setInt(2, pId);
                    hst.setInt(3, travellers);
                    hst.executeUpdate();

                    con.commit();
                    System.out.println("Package Reserved! Go to 'My Trips' to confirm and pay.");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Booking Failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Package ID.");
            }
        }
    }

    // ================= CANCELLATION METHODS =================

    private void cancelMenu(int userId) {
        while (true) {
            System.out.println("\n=== CANCEL BOOKINGS ===");
            System.out.println("1. Cancel Flight Booking");
            System.out.println("2. Cancel Train Booking");
            System.out.println("3. Cancel Bus Booking");
            System.out.println("4. Cancel Hotel Booking");
            System.out.println("5. Cancel Package Booking");
            System.out.println("6. Back");

            int ch = readValidInt("Choice: ");
            switch (ch) {
                case 1 -> cancelFlight(userId);
                case 2 -> cancelTrain(userId);
                case 3 -> cancelBus(userId);
                case 4 -> cancelHotel(userId);
                case 5 -> cancelPackage(userId);
                case 6 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void cancelHotel(int userId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT hb.booking_id, h.hotel_name, hb.room_id, hb.Rooms FROM hotel_booking hb JOIN hotels h ON hb.hotel_id = h.hotel_id WHERE hb.user_id = ? AND hb.status != 'CANCELED'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            System.out.println("\nYour Hotel Bookings:");
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Hotel: " + rs.getString("hotel_name") + " | Rooms: " + rs.getInt("Rooms"));
            }

            if (!found) {
                System.out.println("No hotel bookings found to cancel.");
                return;
            }

            int bId = readValidInt("Enter Booking ID to Cancel (or 0 to exit): ");
            if (bId == 0) return;

            PreparedStatement getDetails = con.prepareStatement("SELECT room_id, Rooms FROM hotel_booking WHERE booking_id = ? AND user_id = ? AND status != 'CANCELED'");
            getDetails.setInt(1, bId);
            getDetails.setInt(2, userId);
            ResultSet rsD = getDetails.executeQuery();

            if (rsD.next()) {
                int roomId = rsD.getInt("room_id");
                int roomsBooked = rsD.getInt("Rooms");

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement restoreRoom = con.prepareStatement("UPDATE rooms SET available_rooms = available_rooms + ? WHERE room_id = ?");
                    restoreRoom.setInt(1, roomsBooked);
                    restoreRoom.setInt(2, roomId);
                    restoreRoom.executeUpdate();

                    PreparedStatement del = con.prepareStatement("UPDATE hotel_booking SET status = 'CANCELED' WHERE booking_id = ?");
                    del.setInt(1, bId);
                    del.executeUpdate();

                    con.commit();
                    System.out.println("Hotel Booking #" + bId + " canceled successfully and room inventory restored!");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Cancellation failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
            } else {
                System.out.println("Invalid Booking ID.");
            }
        } catch (Exception e) {
            System.out.println("Error canceling hotel booking: " + e.getMessage());
        }
    }

    private void cancelFlight(int userId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT fb.booking_id, COALESCE(f.F_From, 'N/A') AS F_From, COALESCE(f.F_To, 'N/A') AS F_To, fb.flight_id, fb.f_tickets, fb.cab_id, fb.nCab, f.Departure_Time, fb.status " +
                            "FROM flight_booking fb LEFT JOIN flights f ON fb.flight_id = f.Flight_id " +
                            "WHERE fb.user_id = ? AND fb.status != 'CANCELED'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            System.out.println("\nYour Active Flight Bookings:");
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: " + rs.getInt("booking_id") +
                        " | Flight: " + rs.getString("F_From") + " -> " + rs.getString("F_To") +
                        " | Tickets: " + rs.getInt("f_tickets") +
                        " | Departure: " + rs.getTimestamp("Departure_Time") +
                        " | Status: " + rs.getString("status"));
            }

            if (!found) {
                System.out.println("No active flight bookings found to cancel.");
                return;
            }

            int bId = readValidInt("Enter Booking ID to Cancel (or 0 to exit): ");
            if (bId == 0) return;

            PreparedStatement getDetails = con.prepareStatement(
                    "SELECT fb.flight_id, fb.f_tickets, fb.cab_id, fb.nCab, f.Departure_Time " +
                            "FROM flight_booking fb LEFT JOIN flights f ON fb.flight_id = f.Flight_id " +
                            "WHERE fb.booking_id = ? AND fb.user_id = ? AND fb.status != 'CANCELED'");
            getDetails.setInt(1, bId);
            getDetails.setInt(2, userId);
            ResultSet rsD = getDetails.executeQuery();

            if (rsD.next()) {
                Integer flightId = (Integer) rsD.getObject("flight_id");
                int tickets = rsD.getInt("f_tickets");
                int cabId = rsD.getInt("cab_id");
                int nCab = rsD.getInt("nCab");

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement markCancel = con.prepareStatement("UPDATE flight_booking SET status = 'CANCELED' WHERE booking_id = ?");
                    markCancel.setInt(1, bId);
                    markCancel.executeUpdate();

                    if (flightId != null) {
                        PreparedStatement restore = con.prepareStatement("UPDATE flights SET Available_Tickets = Available_Tickets + ? WHERE Flight_id = ?");
                        restore.setInt(1, tickets);
                        restore.setInt(2, flightId);
                        restore.executeUpdate();
                    }

                    if (cabId > 0 && nCab > 0) {
                        PreparedStatement restoreCab = con.prepareStatement("UPDATE cabs SET Availability = Availability + ? WHERE Cab_id = ?");
                        restoreCab.setInt(1, nCab);
                        restoreCab.setInt(2, cabId);
                        restoreCab.executeUpdate();
                    }

                    con.commit();
                    System.out.println("Flight Booking #" + bId + " has been CANCELED.");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Cancellation failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
            } else {
                System.out.println("Invalid Booking ID or booking is already canceled.");
            }
        } catch (Exception e) {
            System.out.println("Error canceling flight booking: " + e.getMessage());
        }
    }

    private void cancelTrain(int userId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT tb.booking_id, t.T_From, t.T_To, tb.train_id, tb.t_tickets, tb.cab_id, tb.nCab FROM train_booking tb JOIN trains t ON tb.train_id = t.train_id WHERE tb.user_id = ? AND tb.status != 'CANCELED'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            System.out.println("\nYour Train Bookings:");
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Train: " + rs.getString("T_From") + " -> " + rs.getString("T_To") + " | Tickets: " + rs.getInt("t_tickets"));
            }

            if (!found) {
                System.out.println("No train bookings found to cancel.");
                return;
            }

            int bId = readValidInt("Enter Booking ID to Cancel (or 0 to exit): ");
            if (bId == 0) return;

            PreparedStatement getDetails = con.prepareStatement("SELECT train_id, t_tickets, cab_id, nCab FROM train_booking WHERE booking_id = ? AND user_id = ? AND status != 'CANCELED'");
            getDetails.setInt(1, bId);
            getDetails.setInt(2, userId);
            ResultSet rsD = getDetails.executeQuery();

            if (rsD.next()) {
                int trainId = rsD.getInt("train_id");
                int tickets = rsD.getInt("t_tickets");
                int cabId = rsD.getInt("cab_id");
                int nCab = rsD.getInt("nCab");

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement restore = con.prepareStatement("UPDATE trains SET Available_Tickets = Available_Tickets + ? WHERE train_id = ?");
                    restore.setInt(1, tickets);
                    restore.setInt(2, trainId);
                    restore.executeUpdate();

                    if (cabId > 0 && nCab > 0) {
                        PreparedStatement restoreCab = con.prepareStatement("UPDATE cabs SET Availability = Availability + ? WHERE Cab_id = ?");
                        restoreCab.setInt(1, nCab);
                        restoreCab.setInt(2, cabId);
                        restoreCab.executeUpdate();
                    }

                    PreparedStatement del = con.prepareStatement("UPDATE train_booking SET status = 'CANCELED' WHERE booking_id = ?");
                    del.setInt(1, bId);
                    del.executeUpdate();

                    con.commit();
                    System.out.println("Train Booking #" + bId + " canceled successfully!");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Cancellation failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
            } else {
                System.out.println("Invalid Booking ID.");
            }
        } catch (Exception e) {
            System.out.println("Error canceling train booking: " + e.getMessage());
        }
    }

    private void cancelBus(int userId) {
        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT bb.booking_id, b.B_From, b.B_To, bb.bus_id, bb.b_tickets, bb.cab_id, bb.nCab FROM bus_booking bb JOIN buses b ON bb.bus_id = b.bus_id WHERE bb.user_id = ? AND bb.status != 'CANCELED'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            System.out.println("\nYour Bus Bookings:");
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Bus: " + rs.getString("B_From") + " -> " + rs.getString("B_To") + " | Tickets: " + rs.getInt("b_tickets"));
            }

            if (!found) {
                System.out.println("No bus bookings found to cancel.");
                return;
            }

            int bId = readValidInt("Enter Booking ID to Cancel (or 0 to exit): ");
            if (bId == 0) return;

            PreparedStatement getDetails = con.prepareStatement("SELECT bus_id, b_tickets, cab_id, nCab FROM bus_booking WHERE booking_id = ? AND user_id = ? AND status != 'CANCELED'");
            getDetails.setInt(1, bId);
            getDetails.setInt(2, userId);
            ResultSet rsD = getDetails.executeQuery();

            if (rsD.next()) {
                int busId = rsD.getInt("bus_id");
                int tickets = rsD.getInt("b_tickets");
                int cabId = rsD.getInt("cab_id");
                int nCab = rsD.getInt("nCab");

                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    PreparedStatement restore = con.prepareStatement("UPDATE buses SET Available_Tickets = Available_Tickets + ? WHERE bus_id = ?");
                    restore.setInt(1, tickets);
                    restore.setInt(2, busId);
                    restore.executeUpdate();

                    if (cabId > 0 && nCab > 0) {
                        PreparedStatement restoreCab = con.prepareStatement("UPDATE cabs SET Availability = Availability + ? WHERE Cab_id = ?");
                        restoreCab.setInt(1, nCab);
                        restoreCab.setInt(2, cabId);
                        restoreCab.executeUpdate();
                    }

                    PreparedStatement del = con.prepareStatement("UPDATE bus_booking SET status = 'CANCELED' WHERE booking_id = ?");
                    del.setInt(1, bId);
                    del.executeUpdate();

                    con.commit();
                    System.out.println("Bus Booking #" + bId + " canceled successfully!");
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Cancellation failed: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
            } else {
                System.out.println("Invalid Booking ID.");
            }
        } catch (Exception e) {
            System.out.println("Error canceling bus booking: " + e.getMessage());
        }
    }

    private void cancelPackage(int userId) {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT booking_id, Package_id, p_person FROM package_booking WHERE user_id = ? AND status != 'CANCELED'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;
            System.out.println("\nYour Package Bookings:");
            while (rs.next()) {
                found = true;
                System.out.println("Booking ID: " + rs.getInt("booking_id") + " | Package ID: " + rs.getInt("Package_id") + " | Persons: " + rs.getInt("p_person"));
            }

            if (!found) {
                System.out.println("No package bookings found to cancel.");
                return;
            }

            int bId = readValidInt("Enter Booking ID to Cancel (or 0 to exit): ");
            if (bId == 0) return;

            PreparedStatement del = con.prepareStatement("UPDATE package_booking SET status = 'CANCELED' WHERE booking_id = ? AND user_id = ?");
            del.setInt(1, bId);
            del.setInt(2, userId);
            int rows = del.executeUpdate();

            if (rows > 0) {
                System.out.println("Package Booking #" + bId + " canceled successfully!");
            } else {
                System.out.println("Invalid Booking ID.");
            }
        } catch (Exception e) {
            System.out.println("Error canceling package booking: " + e.getMessage());
        }
    }

    // ================= MY TRIPS & PAYMENT CONFIRMATION =================

    void myTrip(int Userid) {
        try {
            System.out.println("\n================================ MY TRIPS ================================");

            boolean hasPending = false;

            // 1. Flights (Uses LEFT JOIN to keep receipts visible if admin deletes flight)
            PreparedStatement fb = con.prepareStatement(
                    "SELECT fb.booking_id, COALESCE(f.F_From, 'N/A') AS F_From, COALESCE(f.F_To, 'N/A') AS F_To, " +
                            "fb.f_tickets, fb.nCab, fb.status, fb.alert_note " +
                            "FROM flight_booking fb LEFT JOIN flights f ON fb.flight_id = f.Flight_id WHERE fb.user_id = ? AND fb.status != 'CANCELED'");
            fb.setInt(1, Userid);
            ResultSet rsFb = fb.executeQuery();
            System.out.println("\n[FLIGHT BOOKINGS]");
            while (rsFb.next()) {
                String status = rsFb.getString("status");
                String alert = rsFb.getString("alert_note");
                if ("PENDING".equalsIgnoreCase(status)) hasPending = true;
                System.out.println("Booking ID: " + rsFb.getInt("booking_id") + " | From: " + rsFb.getString("F_From") +
                        " -> To: " + rsFb.getString("F_To") + " | Tickets: " + rsFb.getInt("f_tickets") + " | Status: " + status +
                        " | Note: [" + (alert != null ? alert : "On Time") + "]");
            }

            // 2. Trains
            PreparedStatement tb = con.prepareStatement(
                    "SELECT tb.booking_id, t.T_From, t.T_To, tb.t_tickets, tb.nCab, tb.status " +
                            "FROM train_booking tb JOIN trains t ON tb.train_id = t.train_id WHERE tb.user_id = ? AND tb.status != 'CANCELED'");
            tb.setInt(1, Userid);
            ResultSet rsTb = tb.executeQuery();
            System.out.println("\n[TRAIN BOOKINGS]");
            while (rsTb.next()) {
                String status = rsTb.getString("status");
                if ("PENDING".equalsIgnoreCase(status)) hasPending = true;
                System.out.println("Booking ID: " + rsTb.getInt("booking_id") + " | From: " + rsTb.getString("T_From") +
                        " -> To: " + rsTb.getString("T_To") + " | Tickets: " + rsTb.getInt("t_tickets") + " | Status: " + status);
            }

            // 3. Buses
            PreparedStatement bb = con.prepareStatement(
                    "SELECT bb.booking_id, b.B_From, b.B_To, bb.b_tickets, bb.nCab, bb.status " +
                            "FROM bus_booking bb JOIN buses b ON bb.bus_id = b.bus_id WHERE bb.user_id = ? AND bb.status != 'CANCELED'");
            bb.setInt(1, Userid);
            ResultSet rsBb = bb.executeQuery();
            System.out.println("\n[BUS BOOKINGS]");
            while (rsBb.next()) {
                String status = rsBb.getString("status");
                if ("PENDING".equalsIgnoreCase(status)) hasPending = true;
                System.out.println("Booking ID: " + rsBb.getInt("booking_id") + " | From: " + rsBb.getString("B_From") +
                        " -> To: " + rsBb.getString("B_To") + " | Tickets: " + rsBb.getInt("b_tickets") + " | Status: " + status);
            }

            // 4. Hotels
            PreparedStatement hb = con.prepareStatement(
                    "SELECT hb.booking_id, h.hotel_name, r.room_type, hb.Rooms, hb.check_in, hb.check_out, hb.status, " +
                            "(DATEDIFF(hb.check_out, hb.check_in) * r.price_per_night * hb.Rooms) AS total_cost " +
                            "FROM hotel_booking hb JOIN hotels h ON hb.hotel_id = h.hotel_id JOIN rooms r ON hb.room_id = r.room_id WHERE hb.user_id = ? AND hb.status != 'CANCELED'");
            hb.setInt(1, Userid);
            ResultSet rsHb = hb.executeQuery();
            System.out.println("\n[HOTEL BOOKINGS]");
            while (rsHb.next()) {
                String status = rsHb.getString("status");
                if ("PENDING".equalsIgnoreCase(status)) hasPending = true;
                System.out.println("Booking ID: " + rsHb.getInt("booking_id") + " | Hotel: " + rsHb.getString("hotel_name") +
                        " | Room: " + rsHb.getString("room_type") + " | Dates: " + rsHb.getDate("check_in") + " to " + rsHb.getDate("check_out") +
                        " | Total: $" + rsHb.getBigDecimal("total_cost") + " | Status: " + status);
            }

            // 5. Packages
            PreparedStatement pb = con.prepareStatement(
                    "SELECT pb.booking_id, pb.Package_id, pb.p_person, pb.status FROM package_booking pb WHERE pb.user_id = ? AND pb.status != 'CANCELED'");
            pb.setInt(1, Userid);
            ResultSet rsPb = pb.executeQuery();
            System.out.println("\n[PACKAGE BOOKINGS]");
            while (rsPb.next()) {
                String status = rsPb.getString("status");
                if ("PENDING".equalsIgnoreCase(status)) hasPending = true;
                System.out.println("Booking ID: " + rsPb.getInt("booking_id") + " | Package ID: " + rsPb.getInt("Package_id") +
                        " | Persons: " + rsPb.getInt("p_person") + " | Status: " + status);
            }

            System.out.println("==========================================================================");

            if (hasPending) {
                System.out.println("\nYou have pending reservations!");
                System.out.println("1. Confirm & Pay for Pending Reservations");
                System.out.println("2. Back to Menu");
                if (readValidInt("Choice: ") == 1) {
                    confirmAndPay(Userid);
                }
            }
        } catch (Exception e) {
            System.out.println("Error fetching trips: " + e.getMessage());
        }
    }

    private void confirmAndPay(int userId) throws Exception {
        System.out.println("\n=== CONFIRMATION & PAYMENT ===");

        // 1. Calculate Total Amount for PENDING Items
        double totalAmount = 0.0;

        PreparedStatement pf = con.prepareStatement(
                "SELECT SUM(fb.f_tickets * f.Prize) AS total FROM flight_booking fb JOIN flights f ON fb.flight_id = f.Flight_id WHERE fb.user_id = ? AND fb.status = 'PENDING'");
        pf.setInt(1, userId);
        ResultSet rsF = pf.executeQuery();
        if (rsF.next()) totalAmount += rsF.getDouble("total");

        PreparedStatement pt = con.prepareStatement(
                "SELECT SUM(tb.t_tickets * t.Prize) AS total FROM train_booking tb JOIN trains t ON tb.train_id = t.train_id WHERE tb.user_id = ? AND tb.status = 'PENDING'");
        pt.setInt(1, userId);
        ResultSet rsT = pt.executeQuery();
        if (rsT.next()) totalAmount += rsT.getDouble("total");

        PreparedStatement pb = con.prepareStatement(
                "SELECT SUM(bb.b_tickets * b.Prize) AS total FROM bus_booking bb JOIN buses b ON bb.bus_id = b.bus_id WHERE bb.user_id = ? AND bb.status = 'PENDING'");
        pb.setInt(1, userId);
        ResultSet rsB = pb.executeQuery();
        if (rsB.next()) totalAmount += rsB.getDouble("total");

        PreparedStatement ph = con.prepareStatement(
                "SELECT SUM(DATEDIFF(hb.check_out, hb.check_in) * r.price_per_night * hb.Rooms) AS total FROM hotel_booking hb JOIN rooms r ON hb.room_id = r.room_id WHERE hb.user_id = ? AND hb.status = 'PENDING'");
        ph.setInt(1, userId);
        ResultSet rsH = ph.executeQuery();
        if (rsH.next()) totalAmount += rsH.getDouble("total");

        PreparedStatement pp = con.prepareStatement(
                "SELECT SUM(pb.p_person * hp.package_price) AS total FROM package_booking pb JOIN holiday_packages hp ON pb.Package_id = hp.pack_id WHERE pb.user_id = ? AND pb.status = 'PENDING'");
        pp.setInt(1, userId);
        ResultSet rsP = pp.executeQuery();
        if (rsP.next()) totalAmount += rsP.getDouble("total");

        if (totalAmount <= 0) {
            System.out.println("No pending items found for payment.");
            return;
        }

        System.out.println("Total Amount Payable: $" + totalAmount);
        System.out.println("----------------------------------------");

        System.out.println("Select Payment Method:");
        System.out.println("1. Credit / Debit Card");
        System.out.println("2. UPI / Net Banking");
        System.out.println("3. Cash");

        int payChoice = readValidInt("Payment Option: ");
        String payType = switch (payChoice) {
            case 1 -> "Credit/Debit Card";
            case 2 -> "UPI/NetBanking";
            case 3 -> "Cash";
            default -> "Other";
        };

        boolean autoCommitState = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            // 2. Insert Master Record into `bookings` and capture Auto-Generated Invoice ID
            String insertMaster = "INSERT INTO `bookings` (`user_id`, `Payment Type`, `total_amount`) VALUES (?, ?, ?)";
            PreparedStatement bSt = con.prepareStatement(insertMaster, Statement.RETURN_GENERATED_KEYS);
            bSt.setInt(1, userId);
            bSt.setString(2, payType);
            bSt.setDouble(3, totalAmount);
            bSt.executeUpdate();

            ResultSet generatedKeys = bSt.getGeneratedKeys();
            int masterBookingId = 0;
            if (generatedKeys.next()) {
                masterBookingId = generatedKeys.getInt(1);
            }

            // 3. Confirm Sub-Bookings and link to Master Payment Record
            PreparedStatement u1 = con.prepareStatement("UPDATE flight_booking SET status = 'CONFIRMED', master_booking_id = ? WHERE user_id = ? AND status = 'PENDING'");
            u1.setInt(1, masterBookingId); u1.setInt(2, userId); u1.executeUpdate();

            PreparedStatement u2 = con.prepareStatement("UPDATE train_booking SET status = 'CONFIRMED', master_booking_id = ? WHERE user_id = ? AND status = 'PENDING'");
            u2.setInt(1, masterBookingId); u2.setInt(2, userId); u2.executeUpdate();

            PreparedStatement u3 = con.prepareStatement("UPDATE bus_booking SET status = 'CONFIRMED', master_booking_id = ? WHERE user_id = ? AND status = 'PENDING'");
            u3.setInt(1, masterBookingId); u3.setInt(2, userId); u3.executeUpdate();

            PreparedStatement u4 = con.prepareStatement("UPDATE hotel_booking SET status = 'CONFIRMED', master_booking_id = ? WHERE user_id = ? AND status = 'PENDING'");
            u4.setInt(1, masterBookingId); u4.setInt(2, userId); u4.executeUpdate();

            PreparedStatement u5 = con.prepareStatement("UPDATE package_booking SET status = 'CONFIRMED', master_booking_id = ? WHERE user_id = ? AND status = 'PENDING'");
            u5.setInt(1, masterBookingId); u5.setInt(2, userId); u5.executeUpdate();

            con.commit();
            System.out.println("\n=================================================");
            System.out.println(" PAYMENT SUCCESSFUL!");
            System.out.println(" Invoice Receipt ID: #" + masterBookingId);
            System.out.println(" Payment Method: " + payType);
            System.out.println(" Total Paid: $" + totalAmount);
            System.out.println("=================================================");
        } catch (Exception e) {
            con.rollback();
            System.out.println("Payment Confirmation Failed: " + e.getMessage());
        } finally {
            con.setAutoCommit(autoCommitState);
        }
    }
}