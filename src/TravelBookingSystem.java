import java.util.*;

class TravelBookingSystem {

    static Scanner sc = new Scanner(System.in);

    static Route[] routes = new Route[20];
    static Transport[] transports = new Transport[50];
    static User[] users = new User[20];
    static Hotel[] hotels = new Hotel[20];

    static int totalTicketBookings = 0;
    static int totalHotelBookings = 0;
    static int totalCancellations = 0;

    static int rCount = 0, tCount = 0, uCount = 0, hCount = 0;

    public static void main(String[] args) {

        // LOAD FICTIONAL DATA
        TravelBookingSystem tbs = new TravelBookingSystem();

        //tbs.loadSampleData();

        while (true) {
            System.out.println("\n===== TRAVEL BOOKING SYSTEM =====");
            System.out.println("1. User Login");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            switch (sc.nextInt()) {
                case 1 -> tbs.userAuth();
                case 2 -> tbs.adminLogin();
                case 3 -> System.exit(0);
            }
        }
    }

    // ================= SAMPLE DATA =================
    /*void loadSampleData()*/static  {

        // ROUTES
        routes[rCount++] = new Route("Ahmedabad", "Mumbai");
        routes[rCount++] = new Route("Delhi", "Jaipur");
        routes[rCount++] = new Route("Bangalore", "Chennai");
        routes[rCount++] = new Route("Mumbai", "Goa");
        routes[rCount++] = new Route("Delhi", "Bangalore");

        // BUS
        transports[tCount++] = new Transport("Ahmedabad", "Mumbai", "Bus - Non AC", 900);
        transports[tCount++] = new Transport("Ahmedabad", "Mumbai", "Bus - AC", 1400);

        // TRAIN
        transports[tCount++] = new Transport("Delhi", "Jaipur", "Train - Sleeper", 600);
        transports[tCount++] = new Transport("Delhi", "Jaipur", "Train - AC", 1200);

        transports[tCount++] = new Transport("Bangalore", "Chennai", "Train - Sleeper", 750);
        transports[tCount++] = new Transport("Bangalore", "Chennai", "Train - AC", 1500);

        // FLIGHT
        transports[tCount++] = new Transport("Mumbai", "Goa", "Flight - Economy", 4500);
        transports[tCount++] = new Transport("Mumbai", "Goa", "Flight - Business", 8200);

        transports[tCount++] = new Transport("Delhi", "Bangalore", "Flight - Economy", 6200);
        transports[tCount++] = new Transport("Delhi", "Bangalore", "Flight - Business", 11000);

        // HOTELS
        hotels[hCount++] = new Hotel("Sea View Resort", 2500, 4000);
        hotels[hCount++] = new Hotel("City Palace Hotel", 1800, 3000);
        hotels[hCount++] = new Hotel("Budget Inn", 1200, 2000);
    }

    // ================= USER AUTH =================

    void userAuth() {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Sign Up");
            System.out.println("3. Back");
            System.out.print("Choice: ");

            switch (sc.nextInt()) {
                case 1 -> userLogin();
                case 2 -> signUp();
                case 3 -> {
                    return;
                }
            }
        }
    }

    void userLogin() {
        sc.nextLine();
        System.out.print("Username: ");
        String u = sc.nextLine();
        System.out.print("Password: ");
        String p = sc.nextLine();

        for (int i = 0; i < uCount; i++) {
            if (users[i].username.equals(u) && users[i].password.equals(p)) {
                users[i].userPanel(users[i]);
                return;
            }
        }
        System.out.println("Invalid Login!");
    }

    void signUp() {
        sc.nextLine();
        String uTry;
        while(true) {
            boolean uCheck = false;
            System.out.print("Username: ");
            uTry = cValidation();
            for(int i = 0 ; i<uCount;i++){
                if(users[i].username.equals(uTry)){
                    uCheck=true;
                }
            }
            if(uCheck){
                System.out.println("Username Alredy Taken , Try another.");
            }
            else{
                break;
            }
        }
        String u = uTry;
        System.out.println(
                "Password must be at least 8 characters long and contain at least one uppercase letter, one digit, and one special character (@, $, !, %, , ?, &). ");
        /*while (true) {
            System.out.print("Password: ");
            String p = sc.nextLine();
            boolean checkPass = isValidPassword(p);
            if (checkPass) {
                users[uCount++] = new User(u, p);
                break;
            } else {
                System.out.println("Enter Valid Password According to Instructions!!!");
            }
        }*/

        System.out.println("Account Created!");
    }


    // ================= USER PANEL =================
    void userPanel(User user) {

        while (true) {
            System.out.println("\n--- USER PANEL ---");
            System.out.println("1. Book Ticket");
            System.out.println("2. Book Hotel");
            System.out.println("3. Cancel Transport Booking");
            System.out.println("4. Cancel Hotel Booking");
            System.out.println("5. View Combined Bill");
            System.out.println("6. Back");
            System.out.print("Choice: ");

            switch (sc.nextInt()) {
                case 1 -> bookTicket(user);
                case 2 -> bookHotel(user);
                case 3 -> cancelTransportBooking(user);
                case 4 -> cancelHotelBooking(user);
                case 5 -> viewCombinedBill(user);
                case 6 -> {
                    return;
                }
            }
        }
    }
    // ================= USER FEATURES =================
    void bookTicket(User user) {
        boolean check = true;
        while (check) {
            /*System.out.println("1.Want to book Tickets: ");
            System.out.println("2.Exit");
            int chint = sc.nextInt();
            switch (chint) {
                case 1 -> {*/
            for (int i = 0; i < tCount; i++) {
                System.out.print((i + 1) + ". ");
                transports[i].showTransport();
            }

            System.out.print("Select Transport: ");
            int ch = sc.nextInt();
            user.bookedTickets[user.bookedTicketCount++] = transports[ch - 1];
            for (int i = 0; i < user.noOfTickets; i++) {
                user.bookedTickets[i].booked = true;
            }
            user.total += transports[ch - 1].price;
            totalTicketBookings++;
            System.out.println("Ticket Booked!");
            user.noOfTickets++;
            //}
//                case 2 -> {
//                    return;
//                }
//                default -> System.out.println("Enter Valid Choice!!!");
//            }
            while (true){
                System.out.println("Do you want to continue with the booking : ");
                System.out.println("1.Yes\n2.No");
                int chint = sc.nextInt();
                if (chint == 1) {
                    break;
                }
                else if (chint == 2) {
                    check = false;
                    break;
                }
                else {
                    System.out.println("Enter Valid Choice!!!");
                }
            }
        }
    }

    void bookHotel(User user) {
        for (int i = 0; i < hCount; i++) {
            System.out.print((i + 1) + ". ");
            hotels[i].show();
        }

        System.out.print("Select Hotel: ");
        int h = sc.nextInt();
        sc.nextLine();

        Hotel hotel = hotels[h - 1];
        user.h = hotels[h - 1];

        System.out.print("How Many Days Do you Want Stay: ");
        int noOfDays = sc.nextInt();

        System.out.print("How Many Nights Do you Want Stay: ");
        int noOfNights = sc.nextInt();

        user.hotelBooked = true;
        user.hotelName = hotel.name;

        user.hotelAmount = noOfDays * hotel.dayPrice + noOfNights * hotel.nightPrice;
        totalHotelBookings++;
        user.total += user.hotelAmount;
        System.out.println("Hotel Booked!");
    }

    // ==========================Cancellation=================

    void cancelTransportBooking(User user) {

        if (user.bookedTicketCount == 0) {
            System.out.println("No transport booking found.");
            return;
        }
        for (int i = 0; i < user.bookedTicketCount; i++) {
            System.out.print((i + 1) + ". ");
            user.bookedTickets[i].showTransport();
        }
        System.out.println("Enter Transport Number to Cancel it: ");
        int ch = sc.nextInt();
        if (ch >= user.bookedTicketCount) {
            System.out.println("Enter Valid Transport Number!!!");
        } else {
            user.bookedTickets[ch - 1].booked = false;
            user.total -= user.bookedTickets[ch - 1].price;
            totalCancellations++;
            totalTicketBookings--;
            user.bookedTicketCount--;
            user.bookedTickets[ch - 1] = user.bookedTickets[ch];
            System.out.println("Transport booking cancelled.");
        }

    }

    void cancelHotelBooking(User user) {

        if (!user.hotelBooked) {
            System.out.println("No hotel booking found.");
            return;
        }

        user.hotelBooked = false;
        user.total -= user.hotelAmount;
        user.hotelAmount = 0;
        user.hotelName = null;
        user.stayType = null;
        user.stayCountOfDays = 0;
        user.stayCountOfNights = 0;

        totalCancellations++;

        System.out.println("Hotel booking cancelled.");
    }

    void viewCombinedBill(User user) {
        for (int x = 0; x < user.noOfTickets; x++) {
            boolean rCheck = false;
            //for (int i = 0; i < user.noOfTickets; i++) {
            for (int j = 0; j < rCount; j++) {
                if (user.bookedTickets[x].fromCity.equals(routes[j].fromCity) && user.bookedTickets[x].toCity.equals(routes[j].toCity)) {
                    rCheck = true;
                }
            }
            //}


            if (!(user.noOfTickets == 0) && rCheck) {
                System.out.print((x + 1) + ". ");
                user.bookedTickets[x].showTransport();


            }
            else if (!(user.noOfTickets == 0) && !(rCheck)) {
                System.out.println("Transport has been Removed");
                user.total -= transports[x].price;
            }
            else {
                System.out.println("No transport booked");
            }

            if (user.hotelBooked) {
                System.out.println("Hotel (" + user.hotelName + "): ₹" + user.hotelAmount);

            } else {
                System.out.println("No hotel booked");
            }
            System.out.println("----------------------");
            /*user.amount += transports[x].price;*/
        }

        System.out.println("\n===== FINAL BILL =====");
        System.out.println("Total Amount: ₹" + user.total);
        double discount = calculateDiscount(user.total);
        user.total = user.total - discount;
        System.out.println("Discount: ₹" + discount);
        System.out.println("Final Payable Amount: ₹" + user.total);
    }

    // ================= ADMIN LOGIN =================

    void adminLogin() {
        sc.nextLine();
        System.out.print("Admin ID: ");
        String id = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (id.equals("admin123") && pass.equals("admin@123")) {
            adminPanel();
        } else {
            System.out.println("Wrong Credentials!");
        }
    }

    // ================= ADMIN PANEL =================
    void adminPanel() {
        while (true) {
            System.out.println("\n--- ADMIN PANEL ---");
            System.out.println("1. Add Route");
            System.out.println("2. Edit Route");
            System.out.println("3. Delete Route");
            System.out.println("4. Add Transport");
            System.out.println("5. Edit Transport");
            System.out.println("6. Delete Transport");
            System.out.println("7. Add Hotel");
            System.out.println("8. Edit Hotel");
            System.out.println("9. Delete Hotel");
            System.out.println("10. View Status");
            System.out.println("11. View Booking Report");
            System.out.println("12. Back");
            System.out.print("Choice: ");

            switch (sc.nextInt()) {
                case 1 -> addRoute();
                case 2 -> editRoute();
                case 3 -> deleteRoute();
                case 4 -> addTransport();
                case 5 -> editTransport();
                case 6 -> deleteTransport();
                case 7 -> addHotel();
                case 8 -> editHotel();
                case 9 -> deleteHotel();
                case 10 -> viewAll();
                case 11 -> viewBookingReport();
                case 12 -> {
                    return;
                }
            }
        }
    }

    // ================= ROUTE CRUD =================
    void addRoute() {
        sc.nextLine();
        System.out.print("From City: ");
        String from = cValidation();
        System.out.print("To City: ");
        String to = cValidation();
        routes[rCount++] = new Route(from, to);
        System.out.println("Route Added!");
    }

    void editRoute() {
        if (rCount == 0) {
            System.out.println("No Routes Found!");
            return;
        }
        for (int i = 0; i < rCount; i++) {
            System.out.print((i + 1) + ". ");
            routes[i].show();
        }
        System.out.print("Select Route: ");
        int r = sc.nextInt();
        sc.nextLine();
        System.out.print("New From City: ");
        routes[r - 1].fromCity = cValidation();
        System.out.print("New To City: ");
        routes[r - 1].toCity = cValidation();
        System.out.println("Route Updated!");
    }

    void deleteRoute() {
        if (rCount == 0) {
            System.out.println("No Routes Found!");
            return;
        }
        for (int i = 0; i < rCount; i++) {
            System.out.print((i + 1) + ". ");
            routes[i].show();
        }
        System.out.print("Select Route: ");
        int r = sc.nextInt();
        for (int i = r - 1; i < rCount - 1; i++)
            routes[i] = routes[i + 1];
        rCount--;
        System.out.println("Route Deleted!");
    }
    void addTransport() {
        if (rCount == 0) {
            System.out.println("Add route first!");
            return;
        }
        for (int i = 0; i < rCount; i++) {
            System.out.print((i + 1) + ". ");
            routes[i].show();
        }
        int r = sc.nextInt();
        Route route = routes[r - 1];
        sc.nextLine();
        System.out.print("Type: ");
        String type = cValidation();
        System.out.print("Price: ");
        double price = sc.nextDouble();
        transports[tCount++] = new Transport(route.fromCity, route.toCity, type, price);
        System.out.println("Transport Added!");
    }

    void editTransport() {
        if (tCount == 0) {
            System.out.println("No Transport Found!");
            return;
        }
        for (int i = 0; i < tCount; i++) {
            System.out.print((i + 1) + ". ");
            transports[i].showTransport();
        }
        int t = sc.nextInt();
        sc.nextLine();
        System.out.print("New Type: ");
        transports[t - 1].type = cValidation();
        System.out.print("New Price: ");
        transports[t - 1].price = sc.nextDouble();
        System.out.println("Transport Updated!");
    }

    void deleteTransport() {
        if (tCount == 0) {
            System.out.println("No Transport Found!");
            return;
        }
        for (int i = 0; i < tCount; i++) {
            System.out.print((i + 1) + ". ");
            transports[i].showTransport();
        }
        int t = sc.nextInt();
        for (int i = t - 1; i < tCount - 1; i++)
            transports[i] = transports[i + 1];
        tCount--;
        System.out.println("Transport Deleted!");
    }

    // ================= HOTEL CRUD =================
    void addHotel() {
        sc.nextLine();
        System.out.print("Hotel Name: ");
        String name = cValidation();
        System.out.print("Day Price: ");
        double day = sc.nextDouble();
        System.out.print("Night Price: ");
        double night = sc.nextDouble();
        hotels[hCount++] = new Hotel(name, day, night);
        System.out.println("Hotel Added!");
    }

    void editHotel() {
        if (hCount == 0) {
            System.out.println("No Hotels Found!");
            return;
        }
        for (int i = 0; i < hCount; i++) {
            System.out.print((i + 1) + ". ");
            hotels[i].show();
        }
        int h = sc.nextInt();
        sc.nextLine();
        System.out.print("New Name: ");
        hotels[h - 1].name = cValidation();
        System.out.print("New Day Price: ");
        hotels[h - 1].dayPrice = sc.nextDouble();
        System.out.print("New Night Price: ");
        hotels[h - 1].nightPrice = sc.nextDouble();
        System.out.println("Hotel Updated!");
    }

    void deleteHotel() {
        if (hCount == 0) {
            System.out.println("No Hotels Found!");
            return;
        }
        for (int i = 0; i < hCount; i++) {
            System.out.print((i + 1) + ". ");
            hotels[i].show();
        }
        int h = sc.nextInt();
        for (int i = h - 1; i < hCount - 1; i++)
            hotels[i] = hotels[i + 1];
        hCount--;
        System.out.println("Hotel Deleted!");
    }

    // ================= ADMIN REPORT =================
    void viewBookingReport() {
        System.out.println("\n===== BOOKING REPORT =====");
        System.out.println("Total Ticket Bookings : " + totalTicketBookings);
        System.out.println("Total Hotel Bookings  : " + totalHotelBookings);
        System.out.println("Total Cancellations  : " + totalCancellations);

        while (true) {
            System.out.println("1.Ticket Booking Booking");
            System.out.println("2.Hotel Booking Booking");
            System.out.println("3.Exit");
            System.out.println("Enter Your Choice: ");
            int ch = sc.nextInt();
            switch (ch) {
                case 1 -> {
                    viewTransportBookings();
                }
                case 2 -> {
                    viewHotelBookings();
                }
                case 3 -> {
                    return;
                }
            }
        }
    }

    void viewTransportBookings() {
        System.out.println("----- Booking Status -----");

        for (int i = 0; i < uCount; i++) {
            User u = users[i];
            System.out.println("User Name: " + u.username);
            System.out.println(" Booked Total " + u.noOfTickets + " Tickets");
            for (int j = 0; j < u.noOfTickets; j++) {
                if (u.bookedTickets[j] != null) {
                    System.out.println("Transport Type: " + u.bookedTickets[j].type);
                    System.out.println("Route: " + u.bookedTickets[j].fromCity + " to " + u.bookedTickets[j].toCity);
                    System.out.println("Price: " + u.bookedTickets[j].price);
                    System.out.println("Booking Status: " + u.bookedTickets[j].booked);
                } else {
                    System.out.println("No Transport Found");
                }
                System.out.println("--------------------------");
            }
        }
    }

    void viewHotelBookings() {
        System.out.println("----- Booking Status -----");

        for (int i = 0; i < uCount; i++) {
            User u = users[i];
            if (u.h != null) {
                System.out.println("User Name: " + u.username);
                System.out.println("Hotel Name: " + u.h.name);
                System.out.println("Day Price: " + u.h.dayPrice);
                System.out.println("night Price: " + u.h.nightPrice);
                System.out.println("Total Amount: " + u.hotelAmount);
                System.out.println("Booking Status: " + u.hotelBooked);
                System.out.println("--------------------------");
            }
        }
    }

    // ================= VIEW =================
    void viewAll() {
        System.out.println("1.View Routes");
        System.out.println("2.View Transports");
        System.out.println("3.View Hotels");
        System.out.println("4.Exit");
        int ch = sc.nextInt();
        switch (ch) {
            case 1 -> {
                System.out.println("\n--- ROUTES ---");
                for (int i = 0; i < rCount; i++)
                    routes[i].show();
            }
            case 2 -> {
                System.out.println("\n--- TRANSPORTS ---");
                for (int i = 0; i < tCount; i++)
                    transports[i].showTransport();
            }
            case 3 -> {
                System.out.println("\n--- HOTELS ---");
                for (int i = 0; i < hCount; i++)
                    hotels[i].show();
            }
        }

    }


    // =======================Validations=====================
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
            char[] thi = thing.toCharArray();
            for (char c : thi) {
                if (!Character.isAlphabetic(c) && c != ' ') {
                    vCheck = false;
                }
            }
            if (vCheck) {
                realThing = thing;
                break;
            } else {
                System.out.println("Invalid Input.");
                System.out.println("Try again : ");
            }
        }
        return realThing;
    }

    // ======================Discount======================
    double calculateDiscount(double total) {
        if (total > 35999)
            return total * 0.12;
        else if (total > 19999)
            return total * 0.09;
        else if (total > 12999)
            return total * 0.06;
        else
            return 0;
    }
}




