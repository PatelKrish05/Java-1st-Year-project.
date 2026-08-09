package Admin;

import Travel_Booking_System.Methods;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Admin{
    int Adminid;
    Scanner sc = new Scanner(System.in);
    public Admin(int Adminid){
        this.Adminid = Adminid;
    }

    public void adminPanel() throws Exception{
        while (true) {
            System.out.println("\n--- ADMIN PANEL ---");
            System.out.println("1. Manage Flight");
            System.out.println("2. Manage Train");
            System.out.println("3. Manage Bus");
            System.out.println("4. Manage Cab");
            System.out.println("5. Manage Hotel");
            System.out.println("6. Manage Hotel Rooms");
            System.out.println("7. Manage Holiday Package Combo");
            System.out.println("8. Manage Country");
            System.out.println("9. Manage State");
            System.out.println("10. Manage City");
            System.out.println("11. Back");
            System.out.print("Choice: ");

            int choice = new Methods().readValidInt("Enter Choice : ");
            switch (choice) {
                case 1 -> manager(new Flight());//flight
                case 2 -> manager(new Train());//train
                case 3 -> manager(new Bus());//bus
                case 4 -> manager(new Cab());//cab
                case 5 -> manager(new Hotel());// hotel
                case 6 -> manager(new Room());// hotel room
                case 7 -> manager(new Packages());//Package
                case 8 -> manager(new Country());//Country
                case 9 -> manager(new State());//States
                case 10 -> manager(new City());//Citties
                case 11 -> {return;}
                default -> System.out.println("Invalid Choice.");
            }
        }
    }

    void manager(Manageable of) throws Exception{
        while (true) {
            System.out.println("1. View");
            System.out.println("2. Add");
            System.out.println("3. Edit");
            System.out.println("4. Delete");
            System.out.println("5. Back");
            try {
                int choice = new Methods().readValidInt("Enter Choice : ");
                switch (choice) {
                    case 1 -> of.view();
                    case 2 -> of.add();
                    case 3 -> of.edit();
                    case 4 -> of.delete();
                    case 5 -> {
                        return;
                    }
                    default -> System.out.println("Invalid Choice");
                }
            }
            finally {
                try {
                    FileOutputStream fout = new FileOutputStream("Flight Tickets.txt");
                    for (String[] tickets : Flight.f) {
                        for (String c : tickets) {
                            fout.write((c + " ").getBytes());
                            //System.out.println(Arrays.toString(tickets));
                        }
                        fout.write("\n".getBytes());
                    }
                    fout.close();
                }
                catch (IOException e) {
                    System.out.println(e);
                }
            }

        }
    }
}