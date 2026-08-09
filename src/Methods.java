import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

class node {
    Object data;
    node next;
    node(Object data){
        this.data=data;
    }

}

public class Methods extends Flight {
    boolean isValidPassword(String password) {
        char[] pas = password.toCharArray();
        int ac=0,aC=0,nC=0,Oc=0;
        if(pas.length>=8){
            for (char pa : pas) {
                if ('a' <= pa && pa <= 'z') {
                    ac++;
                } else if ('A' <= pa && pa <= 'Z') {
                    aC++;
                } else if ('0' <= pa && pa <= '9') {
                    nC++;
                } else {
                    Oc++;
                }
            }
            if(ac==0){
                System.out.println("Use at least 1 lowercase must me used");
                return false;
            }
            else if(aC==0){
                System.out.println("Use at least 1 Uppercase must me used");
                return false;
            }
            else if(nC==0){
                System.out.println("Use at least 1 Number must me used");
                return false;
            }
            else if(Oc==0){
                System.out.println("Use at least 1 Special Character must me used");
                return false;
            }
            else {
                return true;
            }
        }

        else {
            return false;
        }
    }

    String cValidation() {
        String realThing;
        while (true) {
            boolean vCheck = true;
            String thing = sc.nextLine();
            if(thing.toLowerCase().equals("back")){
                realThing=null;
                break;
            }
            char[] thi = thing.toCharArray();
            if(thi.length>0) {
                for (char c : thi) {
                    if (!Character.isAlphabetic(c) && c == '\n') {
                        vCheck = false;
                    }
                }
                if (vCheck) {
                    realThing = thing;
                    break;
                }
                else {
                    System.out.println("Invalid Input.");
                    System.out.println("Try again : ");
                }
            }
            else {
                System.out.println("Invalid Input.");
                System.out.print("Try again : ");
            }
        }
        return realThing;
    }

    String mobileValidation() {
        String mNumber;
        while (true) {
            boolean vCheck = true;
            String thing = sc.nextLine();
            char[] thi = thing.toCharArray();
            if(thi.length==10) {
                for (char c : thi) {
                    if (!Character.isDigit(c)) {
                        vCheck = false;
                    }
                }
                if (vCheck) {
                    if (thi[0]=='6'||thi[0]=='7'||thi[0]=='8'||thi[0]=='9') {
                        try {

                            String sql = "SELECT * FROM USER WHERE MO = ?";
                            PreparedStatement ps = con.prepareStatement(sql);
                            ps.setString(1, thing);

                            ResultSet rs = ps.executeQuery();
                            if(!rs.next()) {
                                mNumber = thing;
                                break;
                            }
                            else {
                                System.out.println("Number Already Registered");
                                System.out.println("Try again : ");
                            }
                        }
                        catch (SQLException e) {
                            System.out.println(e);
                        }
                    }
                    else {
                        System.out.println("Invalid Input, Indian Number Format.");
                        System.out.println("Try again : ");
                    }
                }
                else {
                    System.out.println("Invalid Input, Only Digits Allowed.");
                    System.out.println("Try again : ");
                }
            }
            else {
                System.out.println("Invalid Input, Must contain 10 Digits Only.");
                System.out.println("Try again : ");
            }
        }



        return mNumber;
    }

    boolean emailValidation(String email) throws Exception{

        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');

        if (at <= 0) {
            System.out.println("Invalid Email!!!");
            return false;   // '@' not found or is the first character
        }

        if (dot <= at + 1) {
            System.out.println("Invalid Email!!!");
            return false;   // '.' must come after '@'
        }

        if (dot == email.length() - 1) {
            System.out.println("Invalid Email!!!");
            return false;   // '.' cannot be the last character
        }
        String sql = "SELECT 1 FROM USER WHERE EMAIL = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();
        if(!rs.next()) {
            return true;
        }
        else {
            System.out.println("Email Already Registered");

            return false;
        }
    }

    void tGen(int id) throws Exception {
        String sql = "SELECT `train_id`, `T_From`, `T_To`,`Departure_Time`, `Station_Name`, `Platform_Number`, `Available_Tickets`" +
                " FROM `trains` WHERE TRAIN_id = " + id;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int maxT = rs.getInt(7);
            Timestamp ts = rs.getTimestamp(4);
            int date = ts.toLocalDateTime().getDayOfMonth();
            String tic = "" + rs.getString(2).toUpperCase().charAt(0) + rs.getString(3).toUpperCase().charAt(0) +
                    rs.getString(5).toUpperCase().charAt(0) + rs.getInt(6) + id + date;
            for (int i = 1; i <= maxT; i++) {
                System.out.println(tic +"/"+ i);
            }
        }
    }


    void bGen(int id) throws Exception {
        String sql = "SELECT `bus_id`, `B_From`, `B_To`, `Departure_Time`, `Station_Name`, `Bus_Number`, `Available_Tickets` " +
                "FROM `buses` WHERE BUS_id = " + id;
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int maxT = rs.getInt(7);
            Timestamp ts = rs.getTimestamp(4);
            int date = ts.toLocalDateTime().getDayOfMonth();
            String tic = "" + rs.getString(2).toUpperCase().charAt(0) + rs.getString(3).toUpperCase().charAt(0) +
                    rs.getString(5).toUpperCase().charAt(0) + rs.getInt(6) + id + date;
            for (int i = 1; i <= maxT; i++) {
                System.out.println(tic +"/"+ i);
            }
        }
    }

    public int readValidInt(String message) {
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
}

/*
class CircularLL{
    public static void main(String[] args) {
        Methods m = new Methods();
        m.insertFirst(2);
        m.insertFirst(1);
        m.insertAfter(2 ,3);
        m.insertLast(4);
        m.insertLast(5);
        m.insertLast(6);
        m.insertLast(7);
        m.insertLast(9);
        m.insertBefore(9 ,8);
        m.display();
*/
/* m.deleteFirst();
        m.display();
        m.deleteLast();
        m.display();
        m.deleteParticularValue(1);
        m.display();
        m.DeleteOddValues();
        m.display();*//*


    }
}
*/
