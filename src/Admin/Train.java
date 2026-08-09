package Admin;

import JDBC.connection;
import Travel_Booking_System.Methods;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Train extends connection implements Manageable {
    String from="",to="",t_Type="";
    int choice, tID;
    Scanner sc = new Scanner(System.in);

    @Override
    public void view() throws Exception {
        String sql = "SELECT * FROM trains";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\tTo\tType\tPrice\tDeparture\t\tJourney Time(in hours)\tStation\t\tPlatform\tTickets");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t" +
                            rs.getString(3) + "\t" +
                            rs.getString(4) + "\t" +
                            rs.getInt(5) + "\t" +
                            rs.getTimestamp(6) + "\t" +
                            rs.getInt(7) + "\t" +
                            rs.getString(8) + "\t\t" +
                            rs.getInt(9) + "\t\t" +
                            rs.getInt(10));
        }

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
        rs.close();
        st.close();
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.println("Train type :-");
            System.out.println("1. Intercity");
            System.out.println("2. Interstate");
            System.out.println("3. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 3) {
                break;
            } else {
                System.out.println("Invalid Choice");
            }
        }
//Intercity
        if (choice == 1) {
            t_Type="Intercity";
            Statement st = con.createStatement();
            String t_From,t_To;
            while (true) {
                System.out.print("From : ");
                t_From = sc.nextLine();
                String fSql = "SELECT * FROM CITIES WHERE CITY_NAME = '" + t_From + "'";
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
                    from=t_From;
                    break;
                }
            }
            while (true) {
                System.out.print("To : ");
                t_To = sc.nextLine();
                String tSql = "SELECT * FROM CITIES WHERE CITY_NAME = '"+t_To+"'";
                ResultSet Trs = st.executeQuery(tSql);
                if (!Trs.next()) {
                    System.out.println("City Not Available");
                    System.out.println("1. Add City to Database");
                    System.out.println("2. Try Another");

                    choice = new Methods().readValidInt("Choice: ");

                    switch (choice) {
                        case 1 -> new City().add();
                        case 2 -> {continue;}
                        default -> System.out.println("Invalid Choice");
                    }
                }
                else if (t_From.toLowerCase().equals(t_To.toLowerCase())){
                    System.out.println("From and To Can't be same");
                }
                else {
                    to=t_To;
                    break;
                }
            }
        }

//Interstate
        else if (choice == 2) {
            t_Type="Interstate";
            String t_CFrom,t_SFrom,t_CTo="",t_STo="";
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
                }
                else {
                    from=t_SFrom+","+t_CFrom;
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
                    to = t_STo+","+t_CTo;
                    if(from.equalsIgnoreCase(to)){
                        System.out.println("From and To Can't be same");
                        continue;
                    }
                    break;
                }
                else {
                    System.out.println("Invalid City or State");
                }
            }
        }
        else if (choice == 3) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime departure;

        while (true) {
            System.out.print("Date of Train Departure (dd-MM-yyyy): ");
            String date= sc.nextLine();

            System.out.print("Time of Train Departure (HH:mm): ");
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

        int pn = new Methods().readValidInt("Platform Number : ");

        int tickets = new Methods().readValidInt("Available Tickets : ");

        String sql = "INSERT INTO TRAINS (`T_From`, `T_To`, `T_Type`, `Prize`, `Departure_Time`, " +
                "`Journey_Time(in hours)`, `Station_Name`, `Platform_Number`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        PreparedStatement pt = con.prepareStatement(sql);
        pt.setString(1,from);
        pt.setString(2,to);
        pt.setString(3,t_Type);
        pt.setInt(4,prize);
        pt.setObject(5,departure);
        pt.setFloat(6,jTime);
        pt.setString(7,sName);
        pt.setInt(8,pn);
        pt.setInt(9,tickets);

        int r = pt.executeUpdate();
        System.out.println(r!=0?"Train Added":"Failed");

    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            tID = new Methods().readValidInt("Enter Train ID : ");

            String sql = "SELECT `train_id`, `Prize`, `Departure_Time`, `Station_Name`, `Platform_Number`, `Available_Tickets` FROM TRAINS WHERE TRAIN_ID = " + tID;
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
                                System.out.print("Date of Train Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine();

                                System.out.print("Time of Train Departure (HH:mm): ");
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
                            int pf = new Methods().readValidInt("New Platform No. : ");
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
                    String fSql = "UPDATE `TRAINS` SET `" + rsm.getColumnName(col) + "` = '" + n + "' WHERE TRAIN_ID = " + tID;
                    Statement st = con.createStatement();
                    try {
                        int frs = st.executeUpdate(fSql);
                        System.out.println("Train Details Updated.");
                        break;
                    }
                    catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            }
            else {
                System.out.println("Invalid Train Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int tId = new Methods().readValidInt("Enter Train Id to Delete (or 0 to cancel): ");
            if (tId == 0) return;

            String checkSql = "SELECT * FROM `trains` WHERE train_id = " + tId;
            Statement checkSt = con.createStatement();
            ResultSet checkRs = checkSt.executeQuery(checkSql);

            if (checkRs.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Mark user bookings as CANCELED and unlink train_id reference
                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE train_booking SET status = 'CANCELED', train_id = NULL WHERE train_id = ?"
                    );
                    cancelBookings.setInt(1, tId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    // 2. Delete associated cabs
                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE T_ID = ?");
                    delCabs.setInt(1, tId);
                    delCabs.executeUpdate();

                    // 3. Delete the train record
                    PreparedStatement delTrain = con.prepareStatement("DELETE FROM `trains` WHERE train_id = ?");
                    delTrain.setInt(1, tId);
                    int r = delTrain.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Train Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Train.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting train: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Train Id");
            }
        }
    }
}