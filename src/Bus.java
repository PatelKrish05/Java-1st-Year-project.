import JDBC.connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Bus extends connection implements Manageable {
    String from="",to="",b_Type="";
    int choice, bID;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM buses");

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\tTo\tType\tPrice\tDeparture\t\tJourney(Hrs)\tStation\t\tBus No.\tTickets");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t" +
                            rs.getString(3) + "\t" +
                            rs.getString(4) + "\t" +
                            rs.getInt(5) + "\t" +
                            rs.getString(6) + "\t" +
                            rs.getInt(7) + "\t\t" +
                            rs.getString(8) + "\t\t" +
                            rs.getString(9) + "\t" +
                            rs.getInt(10)
            );
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        rs.close();
        stmt.close();
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.println("Bus type :-");
            System.out.println("1. Intercity");
            System.out.println("2. Interstate");
            System.out.println("3. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 3) {
                break;
            } else {
                System.out.println("Invalid Choice....");
            }
        }
//Intercity
        switch (choice) {
            case 1 -> {
                b_Type = "Intercity";
                Statement st = con.createStatement();
                String b_From, b_To;
                while (true) {
                    System.out.print("From : ");
                    b_From = sc.nextLine();
                    String fSql = "SELECT * FROM CITIES WHERE CITY_NAME = '" + b_From + "'";
                    ResultSet Frs = st.executeQuery(fSql);
                    if (!Frs.next()) {
                        System.out.println("City Not Available");
                        System.out.println("1. Add City to Database");
                        System.out.println("2. Try Another");

                        choice = new Methods().readValidInt("Choice: ");

                        switch (choice) {
                            case 1 -> new City().add();
                            case 2 -> {
                                continue;
                            }
                            default -> System.out.println("Invalid Choice");
                        }
                    }
                    else {
                        from = b_From;
                        break;
                    }
                }
                while (true) {
                    System.out.print("To : ");
                    b_To = sc.nextLine();
                    String tSql = "SELECT * FROM CITIES WHERE CITY_NAME = '" + b_To + "'";
                    ResultSet Trs = st.executeQuery(tSql);
                    if (!Trs.next()) {
                        System.out.println("City Not Available");
                        System.out.println("1. Add City to Database");
                        System.out.println("2. Try Another");

                        choice = new Methods().readValidInt("Choice: ");

                        switch (choice) {
                            case 1 -> new City().add();
                            case 2 -> {
                                continue;
                            }
                            default -> System.out.println("Invalid Choice");
                        }
                    } else if (b_From.toLowerCase().equals(b_To.toLowerCase())) {
                        System.out.println("From and To Can't be same");
                    } else {
                        to = b_To;
                        break;
                    }
                }
            }


//Interstate
            case 2 -> {
                b_Type = "Interstate";
                String t_CFrom, t_SFrom, t_CTo = "", t_STo = "";
                Statement st = con.createStatement();

                while (true) {
                    System.out.println("From :-");
                    System.out.print("State : ");
                    t_SFrom = sc.nextLine();
                    System.out.print("City : ");
                    t_CFrom = sc.nextLine();
                    String fSql = "SELECT * FROM STATES JOIN CiTIES ON STATES.STATE_ID=CITIES.STATE_ID WHERE STATE_NAME = '" + t_SFrom + "' AND CITY_NAME='" + t_CFrom + "'";
                    ResultSet Frs = st.executeQuery(fSql);
                    if (!Frs.next()) {
                        System.out.println("Invalid City or State");
                    } else {
                        from = t_SFrom + "," + t_CFrom;
                        break;
                    }
                }

                while (true) {
                    System.out.println("To :-");
                    System.out.print("State : ");
                    t_SFrom = sc.nextLine();
                    System.out.print("City : ");
                    t_CFrom = sc.nextLine();
                    String tSql = "SELECT * FROM STATES JOIN CiTIES ON STATES.STATE_ID=CITIES.STATE_ID WHERE STATE_NAME = '" + t_SFrom + "' AND CITY_NAME='" + t_CFrom + "'";
                    ResultSet Trs = st.executeQuery(tSql);
                    if (Trs.next()) {
                        to = t_STo + "," + t_CTo;
                        if (from.equalsIgnoreCase(to)) {
                            System.out.println("From and To Can't be same");
                            continue;
                        }
                        break;
                    } else {
                        System.out.println("Invalid City or State");
                    }
                }
            }
            case 3 -> {
                return;
            }
            default -> System.out.println("Invalid Choice....");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime departure;

        while (true) {
            System.out.print("Date of Bus Boarding (dd-MM-yyyy): ");
            String date= sc.nextLine();

            System.out.print("Time of Bus Boarding (HH:mm): ");
            String time = sc.nextLine();

            String input = date + " " + time;
            try {
                departure = LocalDateTime.parse(input, formatter);
            }
            catch (Exception e){
                System.out.println("Invalid Input");
                continue;
            }

            if (departure.isAfter(LocalDateTime.now())) {
                break;
            }
            else {
                System.out.println("Boarding date and time must be after the current date and time.");
            }
        }

        int prize = new Methods().readValidInt("Prize : ");

        System.out.print("Journey_Time(in hours) : ");
        float jTime = sc.nextFloat();
        sc.nextLine();

        System.out.print("Station Name :");
        String sName = sc.nextLine();

        int bn = new Methods().readValidInt("Bus Number : ");

        int tickets = new Methods().readValidInt("Available Tickets : ");


        String sql = "INSERT INTO BUSES (`B_From`, `B_To`, `B_Type`, `Prize`, `Departure_Time`, " +
                "`Journey_Time(in hours)`, `Station_Name`, `Bus_Number`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setString(1,from);
        pt.setString(2,to);
        pt.setString(3,b_Type);
        pt.setInt(4,prize);
        pt.setObject(5,departure);
        pt.setFloat(6,jTime);
        pt.setString(7,sName);
        pt.setInt(8,bn);
        pt.setInt(9,tickets);

        int r = pt.executeUpdate();
        System.out.println(r!=0?"Bus Added":"Failed");

    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            bID = new Methods().readValidInt("Enter Bus ID : ");

            String sql = "SELECT `bus_id`, `Prize`, `Departure_Time`, `Station_Name`, `Bus_Number`, `Available_Tickets` FROM BUSES WHERE BUS_ID = " + bID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            if (rs.next()) {
                System.out.println("ID : "+rs.getInt(1));
                System.out.println("1."+rsm.getColumnName(2)+" = "+rs.getInt(2));
                System.out.println("2."+rsm.getColumnName(3)+" = "+rs.getTimestamp(3));
                System.out.println("3."+rsm.getColumnName(4)+" = "+rs.getString(4));
                System.out.println("4."+rsm.getColumnName(5)+" = "+rs.getInt(5));
                System.out.println("5."+rsm.getColumnName(6)+" = "+rs.getInt(6));

                while (true) {
                    choice = new Methods().readValidInt("Enter Column number to edit : ");

                    String n = "";
                    int col = 0;

                    switch (choice) {
                        case 1 -> {
                            int p = new Methods().readValidInt("New Prize : ");
                            n = "" + p;
                            col = 2;
                        }
                        case 2 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            String input;
                            LocalDateTime departure;

                            while (true) {
                                System.out.print("Date of Bus Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine();

                                System.out.print("Time of Bus Departure (HH:mm): ");
                                String time = sc.nextLine();

                                input = date + " " + time;
                                try {
                                    departure = LocalDateTime.parse(input, formatter);
                                }
                                catch (Exception e) {
                                    System.out.println("Invalid Input");
                                    continue;
                                }
                                break;
                            }

                            n = departure.toString();
                            col = 3;
                        }
                        case 3 -> {
                            System.out.print("New Station Name : ");
                            String nS = sc.nextLine();
                            n = nS;
                            col = 4;
                        }
                        case 4 -> {
                            int pf = new Methods().readValidInt("New Bus No. : ");
                            n = "" + pf;
                            col = 5;
                        }
                        case 5 -> {
                            int t = new Methods().readValidInt("New Ticket Availability : ");
                            n = "" + t;
                            col = 6;
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }
                    String fSql = "UPDATE `BUSES` SET `" + rsm.getColumnName(col) + "` = '" + n + "' WHERE BUS_ID = " + bID;
                    Statement st = con.createStatement();
                    try {
                        int frs = st.executeUpdate(fSql);
                        System.out.println("Bus Details Updated.");
                        break;
                    }
                    catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            }
            else {
                System.out.println("Invalid Bus Id");
            }
        }

    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int bId = new Methods().readValidInt("Enter Bus Id to Delete (or 0 to cancel): ");
            if (bId == 0) return;

            String checkSql = "SELECT * FROM `buses` WHERE bus_id = " + bId;
            Statement checkSt = con.createStatement();
            ResultSet checkRs = checkSt.executeQuery(checkSql);

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Mark user bookings as CANCELED and unlink bus_id reference
                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE bus_booking SET status = 'CANCELED', bus_id = NULL WHERE bus_id = ?"
                    );
                    cancelBookings.setInt(1, bId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    // 2. Delete associated cabs
                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE B_ID = ?");
                    delCabs.setInt(1, bId);
                    delCabs.executeUpdate();

                    // 3. Delete the bus record
                    PreparedStatement delBus = con.prepareStatement("DELETE FROM `buses` WHERE bus_id = ?");
                    delBus.setInt(1, bId);
                    int r = delBus.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Bus Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Bus.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting bus: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Bus Id");
            }
        }
    }
}