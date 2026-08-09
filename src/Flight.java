import JDBC.connection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class Flight extends connection implements Manageable{
    static ArrayList<String[]> f = new ArrayList<>();
    static Stack st ;
    String from="",to="",f_Type="";
    int choice,fID;
    Scanner sc = new Scanner(System.in);

    public void view() throws Exception{

        String sql = "SELECT * FROM flights";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        System.out.println("ID\tFrom\tTo\tType\tPrice\tBoarding\t\tJourney Time\tDeparture\tAvailable_Tickets\n");
        System.out.println("--------------------------------------------------------------------------------------------------------------");
        while (rs.next()) {
            System.out.println(
                    rs.getInt(1) + "\t" +
                            rs.getString(2) + "\t" +
                            rs.getString(3) + "\t" +
                            rs.getString(4) + "\t" +
                            rs.getInt(5) + "\t" +
                            rs.getTimestamp(6) + "\t" +
                            rs.getInt(7) + "\t" +
                            rs.getTimestamp(8) +"\t"+
                            rs.getInt(9));
        }

        System.out.println("--------------------------------------------------------------------------------------------------------------");
    }

    @Override
    public void add() throws Exception {
        while (true) {
            System.out.println("Flight type :-");
            System.out.println("1. Domestic");
            System.out.println("2. International");
            System.out.println("3. Private");
            System.out.println("4. Back");

            choice = new Methods().readValidInt("Choice: ");

            if (choice >= 1 && choice <= 4) {
                break;
            } else {
                System.out.println("Invalid Choice");
            }
        }


//Domestic
        if (choice == 1) {
            f_Type="Domestic";
            Statement st = con.createStatement();
            String f_From,f_To;
            while (true) {
                System.out.print("From : ");
                f_From = sc.nextLine();
                String fSql = "SELECT * FROM STATES WHERE STATE_NAME = '" + f_From + "'";
                ResultSet Frs = st.executeQuery(fSql);
                if (!Frs.next()) {
                    System.out.println("State Not Available");
                    System.out.println("1. Add State to Database");
                    System.out.println("2. Try Another");

                    choice = new Methods().readValidInt("Choice: ");

                    switch (choice) {
                        case 1 -> new State().add();
                        case 2 -> {
                            continue;
                        }
                        default -> System.out.println("Invalid Choice");
                    }
                }
                else {
                    from=f_From;
                    break;
                }
            }
            while (true) {
                System.out.print("To : ");
                f_To = sc.nextLine();
                String tSql = "SELECT * FROM STATES WHERE STATE_NAME = '"+f_To+"'";
                ResultSet Trs = st.executeQuery(tSql);
                if (!Trs.next()) {
                    System.out.println("State Not Available");
                    System.out.println("1. Add State to Database");
                    System.out.println("2. Try Another");

                    choice = new Methods().readValidInt("Choice: ");

                    switch (choice) {
                        case 1 -> new State().add();
                        case 2 -> {continue;}
                        default -> System.out.println("Invalid Choice");
                    }
                }
                else if (f_From.toLowerCase().equals(f_To.toLowerCase())){
                    System.out.println("From and To Can't be same");
                }
                else {
                    to=f_To;
                    break;
                }
            }
        }

//International
        else if (choice == 2) {
            f_Type="International";
            String f_CFrom,f_SFrom,f_CTo,f_STo;
            Statement st = con.createStatement();

            while (true) {
                System.out.println("From :-");
                System.out.print("Country : ");
                f_CFrom = sc.nextLine();
                System.out.print("State : ");
                f_SFrom = sc.nextLine();
                String fSql = "SELECT * FROM STATES JOIN COUNTRIES ON STATES.COUNTRY_ID=COUNTRIES.COUNTRY_ID WHERE STATE_NAME = '" + f_SFrom + "' AND COUNTRY_NAME='" + f_CFrom + "'";
                ResultSet Frs = st.executeQuery(fSql);
                if (!Frs.next()) {
                    System.out.println("Invalid Country or State");
                }
                else {
                    from=f_CFrom+","+f_SFrom;
                    break;
                }
            }

            while (true) {
                System.out.println("To :-");
                System.out.print("Country : ");
                f_CTo = sc.nextLine();
                System.out.print("State : ");
                f_STo = sc.nextLine();
                String tSql = "SELECT * FROM STATES JOIN COUNTRIES ON STATES.COUNTRY_ID=COUNTRIES.COUNTRY_ID WHERE STATE_NAME = '"+f_STo+"' AND COUNTRY_NAME='"+f_CTo+"'";
                ResultSet Trs = st.executeQuery(tSql);
                if (Trs.next()) {
                    to = f_CTo+","+f_STo;
                    if(from.toLowerCase().equals(to.toLowerCase())){
                        System.out.println("From and To Can't be same");
                        continue;
                    }
                    break;
                }
                else {
                    System.out.println("Invalid Country or State");
                }
            }
        }

//Private
        else if (choice == 3) {
            f_Type="Private";
            String f_CFrom,f_SFrom,f_CTo,f_STo;
            Statement st = con.createStatement();

            while (true) {
                System.out.println("From :-");
                System.out.print("Country : ");
                f_CFrom = sc.nextLine();
                System.out.print("State : ");
                f_SFrom = sc.nextLine();
                String fSql = "SELECT * FROM STATES JOIN COUNTRIES ON STATES.COUNTRY_ID=COUNTRIES.COUNTRY_ID WHERE STATE_NAME = '" + f_SFrom + "' AND COUNTRY_NAME='" + f_CFrom + "'";
                ResultSet Frs = st.executeQuery(fSql);
                if (!Frs.next()) {
                    System.out.println("Invalid Country or State");
                }
                else {
                    from=f_CFrom+","+f_SFrom;
                    break;
                }
            }

            while (true) {
                System.out.println("To :-");
                System.out.print("Country : ");
                f_CTo = sc.nextLine();
                System.out.print("State : ");
                f_STo = sc.nextLine();
                String tSql = "SELECT * FROM STATES JOIN COUNTRIES ON STATES.COUNTRY_ID=COUNTRIES.COUNTRY_ID WHERE STATE_NAME = '"+f_STo+"' AND COUNTRY_NAME='"+f_CTo+"'";
                ResultSet Trs = st.executeQuery(tSql);
                if (Trs.next()) {
                    to = f_CTo+","+f_STo;
                    if(from.toLowerCase().equals(to.toLowerCase())){
                        System.out.println("From and To Can't be same");
                        continue;
                    }
                    break;
                }
                else {
                    System.out.println("Invalid Country or State");
                }
            }
        }
        else if (choice == 4) {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        LocalDateTime boarding;

        while (true) {
            System.out.print("Date of Flight Boarding (dd-MM-yyyy): ");
            String date= sc.nextLine();

            System.out.print("Time of Flight Boarding (HH:mm): ");
            String time = sc.nextLine();

            String input = date + " " + time;
            try {
                boarding = LocalDateTime.parse(input, formatter);
            }
            catch (Exception e){
                System.out.println("Invalid Input");
                continue;
            }

            if (boarding.isAfter(LocalDateTime.now())) {
                break;
            }
            else {
                System.out.println("Boarding date and time must be after the current date and time.");
            }
        }

        LocalDateTime departure;

        while (true) {
            System.out.print("Departure Date (dd-MM-yyyy): ");
            String date = sc.nextLine();

            System.out.print("Departure Time (HH:mm): ");
            String time = sc.nextLine();

            String input = date + " " + time;
            try {
                departure = LocalDateTime.parse(input, formatter);
            }
            catch (Exception e){
                System.out.println("Invalid Input");
                continue;
            }

            if (departure.isAfter(boarding.plusMinutes(30))) {
                break;
            }
            else {
                System.out.println("Departure must be at least 30 minutes after boarding.");
            }
        }


        System.out.print("Time of Journey : ");
        float jTime = sc.nextFloat();
        sc.nextLine();

        int tickets = new Methods().readValidInt("Available Tickets : ");

        int prize = new Methods().readValidInt("Prize : ");


        String sql = "INSERT INTO flights  (`F_From`, `F_To`, `F_Type`, `Prize`, " +
                "`Boarding_Time`, `Journey_Time(in hours)`, `Departure_Time`, `Available_Tickets`) " +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement pt = con.prepareStatement(sql);
        // pt.setInt(1,fCount);
        pt.setString(1,from);
        pt.setString(2,to);
        pt.setString(3,f_Type);
        pt.setInt(4,prize);
        pt.setObject(5,boarding);
        pt.setFloat(6,jTime);
        pt.setObject(7,departure);
        pt.setInt(8,tickets);

        int r = pt.executeUpdate();
        if(r>0) {
            String id = "SELECT MAX(FLIGHT_ID) FROM FLIGHTS";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(id);
            rs.next();
            f.add(fGen(rs.getInt(1),tickets));
            System.out.println("Flight Added");
        }
        else {
            System.out.println("failed to Add Flight");
        }
    }

    @Override
    public void edit() throws Exception {
        view();
        while (true) {
            fID = new Methods().readValidInt("Enter Flight ID : ");

            String sql = "SELECT `FLIGHT_ID`, `Prize`,`Boarding_Time`,`Departure_Time`,`Available_Tickets` FROM FLIGHTS WHERE FLIGHT_ID = " + fID;
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            if (rs.next()) {
                System.out.println("ID : "+rs.getInt(1));
                System.out.println("1."+rsm.getColumnName(2)+" = "+rs.getInt(2));
                System.out.println("2."+rsm.getColumnName(3)+" = "+rs.getTimestamp(3));
                System.out.println("3."+rsm.getColumnName(4)+" = "+rs.getTimestamp(4));
                System.out.println("4."+rsm.getColumnName(5)+" = "+rs.getInt(5));

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
                            LocalDateTime boarding;

                            while (true) {
                                System.out.print("Date of Flight Boarding (dd-MM-yyyy): ");
                                String date = sc.nextLine();

                                System.out.print("Time of Flight Boarding (HH:mm): ");
                                String time = sc.nextLine();

                                input = date + " " + time;
                                try {
                                    boarding = LocalDateTime.parse(input, formatter);
                                } catch (Exception e) {
                                    System.out.println("Invalid Input");
                                    continue;
                                }
                                break;
                            }
                            System.out.println(boarding.toString());
                            n = boarding.toString();
                            col = 3;
                        }
                        case 3 -> {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
                            String input;
                            LocalDateTime departure;

                            while (true) {
                                System.out.print("Date of Flight Departure (dd-MM-yyyy): ");
                                String date = sc.nextLine();

                                System.out.print("Time of Flight Departure (HH:mm): ");
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
                            col = 4;
                        }
                        case 4 -> {
                            int t = new Methods().readValidInt("New Ticket Availability : ");
                            n = "" + t;
                            col = 5;
                            String[] New = ticketEdit(f.get(fID-1),t);
                            f.set(fID - 1, New);
                        }
                        default -> {
                            System.out.println("Invalid Input");
                            continue;
                        }
                    }
                    String fSql = "UPDATE `flights` SET `" + rsm.getColumnName(col) + "` = '" + n + "' WHERE FLIGHT_ID = " + fID;
                    Statement st = con.createStatement();
                    try {
                        int frs = st.executeUpdate(fSql);
                        System.out.println("Flight Details Updated.");
                        break;
                    }
                    catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
                break;
            }
            else {
                System.out.println("Invalid Flight Id");
            }
        }
    }

    @Override
    public void delete() throws Exception {
        view();
        while (true) {
            int fId = new Methods().readValidInt("Enter Flight Id to Delete (or 0 to cancel): ");
            if (fId == 0) return;

            String fc = "SELECT * FROM `flights` WHERE FLIGHT_ID = " + fId;
            Statement fst = con.createStatement();
            ResultSet fcr = fst.executeQuery(fc);

            if (fcr.next()) {
                boolean autoCommitState = con.getAutoCommit();
                try {
                    con.setAutoCommit(false);

                    // 1. Mark user bookings as CANCELED and clear flight_id reference
                    PreparedStatement cancelBookings = con.prepareStatement(
                            "UPDATE flight_booking SET status = 'CANCELED', flight_id = NULL WHERE flight_id = ?"
                    );
                    cancelBookings.setInt(1, fId);
                    int affectedUsers = cancelBookings.executeUpdate();

                    // 2. Delete associated cabs
                    PreparedStatement delCabs = con.prepareStatement("DELETE FROM cabs WHERE F_ID = ?");
                    delCabs.setInt(1, fId);
                    delCabs.executeUpdate();

                    // 3. Delete package mappings if any
                    PreparedStatement delPkgTrans = con.prepareStatement("DELETE FROM packages_transports WHERE flight_id = ?");
                    delPkgTrans.setInt(1, fId);
                    delPkgTrans.executeUpdate();

                    // 4. Delete the flight record
                    PreparedStatement delFlight = con.prepareStatement("DELETE FROM flights WHERE FLIGHT_ID = ?");
                    delFlight.setInt(1, fId);
                    int r = delFlight.executeUpdate();

                    if (r > 0) {
                        con.commit();
                        System.out.println("Flight Deleted Successfully!");
                        if (affectedUsers > 0) {
                            System.out.println(affectedUsers + " user booking(s) automatically marked as CANCELED.");
                        }
                        if (fId - 1 < f.size()) {
                            f.remove(fId - 1);
                        }
                    } else {
                        con.rollback();
                        System.out.println("Failed to Delete Flight.");
                    }
                } catch (Exception e) {
                    con.rollback();
                    System.out.println("Error deleting flight: " + e.getMessage());
                } finally {
                    con.setAutoCommit(autoCommitState);
                }
                break;
            } else {
                System.out.println("Invalid Flight Id");
            }
        }
    }

    String[] fGen(int id , int max) throws Exception{
        String[] tickets = new String[max];
        st= new Stack(max);

        String sql = "SELECT `Flight_id`, `F_From`, `F_To`,  `Boarding_Time`, `Available_Tickets` FROM `flights` WHERE Flight_id = "+id;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            int maxT =  rs.getInt(5);
            Timestamp ts = rs.getTimestamp(4);
            int date = ts.toLocalDateTime().getDayOfMonth();
            String tic = ""+rs.getString(2).toUpperCase().charAt(0)+rs.getString(3).toUpperCase().charAt(0)+id+date;
            for(int i = 1;i<=max;i++){
                st.push(tic+"/"+i,tickets);
            }
        }
        try {
            FileOutputStream fout = new FileOutputStream("Flight Tickets.txt", true);
            for (String c : tickets) {
                fout.write((c + " ").getBytes());
                //System.out.println(Arrays.toString(tickets));
            }
            fout.write("\n".getBytes());
            fout.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }

        return tickets;
    }

    String[] ticketEdit(String[] og , int max){
        String[] temp = new String[max];
        String tic = og[0].substring(0, og[0].indexOf('/'));
        if(og.length>=max) {
            for (int i = 0; i < max; i++) {
                temp[i] = og[i];
            }
        }
        else {
            for(int i = og.length+1;i<=max;i++){
                temp[i] = (tic+"/"+i);
            }
        }
        return temp;

    }
}