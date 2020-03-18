import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.text.ParseException;

public class InnReservations {
    public static void main(String args[]){


        InnReservations ir = new InnReservations();

        Scanner scanner =  new Scanner(System.in);
        ir.runPrompt(scanner);
    }

    private void runPromptStrings(){
        System.out.println("\nSelect an option\n");
        System.out.println("(1) Rooms and Rates");
        System.out.println("(2) Reservations");
        System.out.println("(3) Reservation Change");
        System.out.println("(4) Reservation Cancellation");
        System.out.println("(5) Detailed Reservation Information");
        System.out.println("(6) Revenue");
        System.out.println("(0) Exit\n");
    }

    private void runPrompt(Scanner scanner) {

        String input = "garbage";

        while(!input.equals('0')){
            runPromptStrings();
            input = scanner.nextLine();
            switch(input.replaceAll(" ", "")) {
                case "1":
                    System.out.println("Rooms and Rates:\n");
                    RoomsAndRates();
                    break;
                case "2":
                    System.out.println("Reservations:\n");
                    Reservations(scanner);
                    break;
                case "3":
                    System.out.println("Reservation Change:\n");
                    ChangeReservation(scanner);
                    break;
                case "4":
                    System.out.println("Reservation Cancellation:\n");
                    CancelReservation(scanner);
                    break;
                case "5":
                    System.out.println("Detailed Reservation Information:\n");
                    SearchReservation(scanner);
                    break;
                case "6":
                     System.out.println("Revenue");
                     Revenue();
                     break;
                case "0":
                    return;
                default:
                    System.out.println("Not a valid option\n");
                    break;
            }
        }
    }

    private void RoomsAndRates() {

        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))) {

            // Step 2: Construct sql statement

            String sqlStatement = "with popularities as (" +
                    "select room, " +
                    "round(sum(datediff(checkout, checkin))/180, 2) pop " +
                    "from lab7_reservations " +
                    "group by room), " +
                    "nextavailable as ( " +
                    "select room, " +
                    "min(checkout) next " +
                    "from lab7_reservations " +
                    "where checkout >= curdate() " +
                    "group by room), " +
                    "staylengths as (" +
                    "select room, " +
                    "code, " +
                    "checkin, checkout, " +
                    "datediff(checkout, checkin) length, " +
                    "max(checkout) over (partition by room) latest " +
                    "from lab7_reservations " +
                    "where checkout < curdate()), " +
                    "mostrecent as ( " +
                    "select room, " +
                    "length, " +
                    "checkout " +
                    "from staylengths " +
                    "where checkout = latest) " +
                    "select r.*, " +
                    "p.pop popularity, " +
                    "na.next 'next available', " +
                    "mr.length 'length of last stay', " +
                    "mr.checkout 'last checkout' " +
                    "from lab7_rooms r " +
                    "join popularities p on r.roomcode = p.room " +
                    "join nextavailable na on r.roomcode = na.room " +
                    "join mostrecent mr on r.roomcode = mr.room " +
                    "order by p.pop desc;";

            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {
                ResultSet rs = pstmt.executeQuery();


                // print out results
                while (rs.next()) {
                    System.out.println("Room Code: " + rs.getString("RoomCode"));
                    System.out.println("Room Name: " + rs.getString("RoomName"));
                    System.out.println("Beds: " + rs.getString("Beds"));
                    System.out.println("Bed Type: " + rs.getString("bedType"));
                    System.out.println("Max Occ.: " + rs.getInt("maxOcc"));
                    System.out.println("Base Price: " + rs.getString("basePrice"));
                    System.out.println("Decor: " + rs.getString("decor"));
                    System.out.println("Popularity: " + rs.getString("popularity"));
                    System.out.println("Next Available: " + rs.getString("next available"));
                    System.out.println("Length of Last Stay: " + rs.getInt("length of last stay"));
                    System.out.println("Last Checkout: " + rs.getString("last checkout"));
                    System.out.println("\n");
                }


            } catch (SQLException e) {
                System.out.println("prepare statement didnt work yoikes");

                conn.rollback();
            }

        } catch (SQLException e) {
            System.out.println("Connection couldn't be made with database");
        }
    }

    private void Reservations(Scanner scanner){
         // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            // Step 2: Construct sql statement
            String firstName;
            String lastName;
            String checkIn;
            String checkOut;
            String roomCode;
            String bedType;
            String numChildren;
            String numAdults;
            
            
            System.out.print("First name: ");
            while ((firstName = scanner.nextLine()) == "")
                System.out.print("First name cannot be empty: ");
            
            System.out.print("Last name: ");
            while ((lastName = scanner.nextLine()) == "")
                System.out.print("Last name cannot be empty: ");
            
            System.out.print("Room Code: ");
            roomCode = scanner.nextLine();
            
            System.out.print("Bed Type: ");
            bedType = scanner.nextLine();

            System.out.print("Check in date: ");
            while ((checkIn = scanner.nextLine()) == "")
                System.out.print("Check in date cannot be empty: ");

            System.out.print("Check out date: ");
            while ((checkOut = scanner.nextLine()) == "")
                System.out.print("Check out date cannot be empty: ");
            
            System.out.print("Number of Children: ");
            while ((numChildren = scanner.nextLine()) == "")
                System.out.print("Number of children cannot be empty: ");

            System.out.print("Number of Adults: ");
            while ((numAdults = scanner.nextLine()) == "")
                System.out.print("Number of adults cannot be empty: ");
            
            if (!CheckValidRoom(Integer.parseInt(numChildren), Integer.parseInt(numAdults), roomCode, bedType)) {
                System.out.println("No rooms fit");
                return;
            }

            String sqlStatement = "SELECT * from lab7_rooms where ";

            if (roomCode.equals("Any")) {
                sqlStatement += "roomcode is not null ";
            }
            else
                sqlStatement += "roomcode = ? ";
            
            if (bedType.equals("Any")) {
                sqlStatement += "and bedType is not null ";
            }
            else
                sqlStatement += "and bedType = ? ";
            
            sqlStatement += "and maxocc >= ? ";
            
            sqlStatement += "and roomcode not in (select distinct room from lab7_reservations where ";
            
            if (roomCode.equals("Any"))
                sqlStatement += "room is not null ";
            else
                sqlStatement += "room = ? ";
            
            sqlStatement += "and (not (? <= checkIn or ? >= checkout)))";
            
            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {
                
                // Step 4: Send SQL statement to DBMS
                int numParams = 0;
                
                if (!roomCode.equals("Any")) {
                    numParams++;
                    pstmt.setString(numParams, roomCode);
                }

                if (!bedType.equals("Any")) {
                    numParams++;
                    pstmt.setString(numParams, bedType);
                }
                
                numParams++;
                pstmt.setString(numParams, numChildren + numAdults);
                
                if (!roomCode.equals("Any")) {
                    numParams++;
                    pstmt.setString(numParams, roomCode);
                }
                
                numParams++;
                pstmt.setString(numParams, checkOut);
                
                numParams++;
                pstmt.setString(numParams, checkIn);
                
                System.out.println("sql: " + sqlStatement);
                
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    System.out.println("Here are room options that fit your requests:\n");
                    
                    System.out.println("(" + rs.getRow() + ") " + "roomcode: " + rs.getString("roomcode") + "  roomname: " + rs.getString("roomname"));
                    
                    while (rs.next()) {
                        System.out.println("(" + rs.getRow() + ") " + "roomcode: " + rs.getString("roomcode") + "  roomname: " + rs.getString("roomname"));
                    }
                    System.out.println();
                    
                    System.out.println("Please select your room choice: ");
                    int roomChoiceIndex = Integer.parseInt(scanner.nextLine());
                    
                    // move rs to point at correct row
                    rs.absolute(roomChoiceIndex);
                    
                    System.out.println("\nHere is the info for your selected room");
                    System.out.println("Name attached to reservation: " + firstName + " " + lastName);
                    System.out.println("Room code: " + rs.getString("roomcode") + "  Room name: " + rs.getString("roomname") + "  Bed type: " + rs.getString("bedType"));
                    System.out.println("Check in day: " + checkIn + "  Check out day: " + checkOut);
                    System.out.println("Number of adults: " + numAdults + "  Number of children: " + numChildren);
                    
                    
                    double cost = 0;
                    int numDays = 0;
                    Calendar startCal = Calendar.getInstance(), endCal = Calendar.getInstance();
                    try {
                        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(checkIn), endDate = new SimpleDateFormat("yyyy-MM-dd").parse(checkOut);
                        startCal.setTime(startDate);
                        endCal.setTime(endDate);
                        while(startCal.getTimeInMillis() < endCal.getTimeInMillis()) {
                            numDays++;
                            if(startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                                cost += Double.parseDouble(rs.getString("basePrice")) * 1.10;
                            } else {
                                cost += Double.parseDouble(rs.getString("basePrice"));
                            }
                            startCal.add(Calendar.DAY_OF_MONTH, 1);
                        }
                        cost *= 1.18;
                        System.out.println("Total cost of stay: " + cost);
                        
                    } catch (ParseException e) {
                        System.out.println("Exception: " + e);
                    }
                    
                    System.out.println("\nWould you like to book this room? (y/n) ");
                    String bookReservation = scanner.nextLine();
                    
                    if (bookReservation.equals("y")) {
                        String ins = "INSERT INTO lab7_reservations (Code, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        String str = "SELECT MAX(CODE) Code from lab7_reservations";
                        try (PreparedStatement psmt = conn.prepareStatement(str)) {
                            ResultSet maxCode = psmt.executeQuery();
                            maxCode.next();
                            
                            try (PreparedStatement psmt2 = conn.prepareStatement(ins)) {
                                
                                psmt2.setString(1, Integer.toString(Integer.parseInt(maxCode.getString("Code")) + 1));
                                psmt2.setString(2, rs.getString("roomcode"));
                                psmt2.setString(3, checkIn);
                                psmt2.setString(4, checkOut);
                                psmt2.setString(5, Double.toString(cost/numDays));
                                psmt2.setString(6, lastName);
                                psmt2.setString(7, firstName);
                                psmt2.setString(8, numAdults);
                                psmt2.setString(9, numChildren);
                                
                                psmt2.executeUpdate();
                                
                            } catch (SQLException e) {
                                System.out.println("Rollback" + e);
                                conn.rollback();
                            }
                            
                        } catch (SQLException e){
                            System.out.println("Rollback" + e);
                            conn.rollback();
                        }
                    } else {
                        System.out.println("\nSorry none of those reservations worked for you.");
                    }
                    
                    
                }
                else
                    System.out.println("no reservations fit -> print out list of 5 recommendations");
                    
                
                // Step 5: Handle results

                conn.commit();

                } catch (SQLException e){
                    System.out.println("Rollback" + e);
                    conn.rollback();
                }

            } catch (SQLException e){
               System.out.println("Connection couldn't be made with database");
            }

    }
    
    private boolean CheckValidRoom(int c, int a, String roomCode, String bedType) {
        
        boolean valid = false;
        
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                                                           System.getenv("HP_JDBC_USER"),
                                                           System.getenv("HP_JDBC_PW"))) {
            String sqlStatement = "select * from lab7_rooms";
            
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    if (rs.getInt("maxOcc") >= (c + a) && roomCode.equals("Any"))
                        valid = true;
                    else if (roomCode.equals(rs.getString("roomcode"))) {
                        if (rs.getInt("maxOcc") >= (c + a) && (bedType.equals(rs.getString("bedtype")) || bedType.equals("Any")))
                            valid = true;
                    }
                }
                
            } catch (SQLException e) {
                System.out.println("Rollback " + e);
                conn.rollback();
            }
            
            return valid;
        } catch (SQLException e) {
            System.out.println("Connection couldn't be made with database");
            return false;
        }
    }
    
    private void CancelReservation(Scanner scanner) {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            // Step 2: Construct sql statement
            String resCode;
            System.out.print("Enter a reservation code to cancel: ");
            resCode = scanner.nextLine();

            String sqlStatement = "DELETE from lab7_reservations where code = ?";

            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, resCode);
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                if(rowCount == 0) {
                    System.out.format("No reservation found with code %s", resCode);
                }
                else{
                    System.out.format("Deleted %d reservation with code %s", rowCount, resCode);
                }

                conn.commit();

                } catch (SQLException e){
                    conn.rollback();
                }

            } catch (SQLException e){
                System.out.println("Connection couldn't be made with database");
            }

    }

    private boolean checkValidDates(String code, String newIn, String newOut) {
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            String stmt = "with res as (select code, room, checkin, checkout from lab7_reservations where code = ?) " +
                    "select * from res r join reservations rs on r.room and r.code != rs.code where not ";

            int numParams = 1;
            boolean out = false;
            boolean in = false;

            if(newOut != "no change"){
                stmt += "(? <= rs.checkin or ";
                out = true;
            } else
                stmt += "(r.checkout <= rs.checkin or ";
            if(newIn != "no change"){
                stmt += "? >= rs.checkout)";
                in = true;
            }
            else{
                stmt += "r.checkin >= rs.checkout)";
            }

            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(stmt)) {

                int place = 1;
                pstmt.setString(place, code);
                place++;

                if(out && !in){
                    pstmt.setString(place, newOut);
                    place++;
                }
                else if(in && !out){
                    pstmt.setString(place, newIn);
                    place++;
                }
                else if(in && out){
                    pstmt.setString(place, newOut);
                    place++;
                    pstmt.setString(place, newIn);
                }

                ResultSet rs = pstmt.executeQuery();
                conn.commit();

                return !rs.next();

            } catch (SQLException e) {
                System.out.println("Connection couldn't be made with database");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Connection couldn't be made with database");
            return false;
        }



    }

    private void ChangeReservation(Scanner scanner) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            boolean firstThing = true;


            String sqlStatement = "UPDATE lab7_reservations set ";

            // Step 2: Construct sql statement
            String resCode, fname, lname;
            Date beginDate, endDate;

            System.out.print("Enter a reservation code to change: ");
            resCode = scanner.nextLine();

            System.out.print("Enter updated first name (or 'no change'): ");
            fname = scanner.nextLine();

            System.out.print("Enter updated last name (or 'no change'): ");
            lname = scanner.nextLine();

            boolean beginChanged = false;
            System.out.print("Enter updated begin date (format: yyyy-MM-dd) (or 'no change'):  ");
            String dateStr = scanner.nextLine();
            if(!dateStr.equals("no change")){
                try {
                    beginDate = sdf.parse(dateStr);
                    beginChanged = true;
                } catch (ParseException e) {
                    System.out.println("Date entered incorrectly");
                }
            }

            boolean endChanged = false;
            System.out.print("Enter updated end date (format: yyyy-MM-dd) (or 'no change'):  ");
            String dateEndStr = scanner.nextLine();
            if(!dateStr.equals("no change")) {
                try {
                    endDate = sdf.parse(dateStr);
                    endChanged = true;
                } catch (ParseException e) {
                    System.out.println("Date entered incorrectly");
                }
            }


            System.out.print("Enter updated number of children (or 'no change'):  ");
            String numChildrenStr = scanner.nextLine();
            if(!numChildrenStr.equals("no change")){
                try {
                    int numChildren = Integer.parseInt(numChildrenStr);
                } catch (NumberFormatException e) {
                    System.out.println("A number must be entered for children");
                }
            }

            System.out.print("Enter updated number of adults (or 'no change'):  ");
            String numAdultsStr = scanner.nextLine();
            if(!numAdultsStr.equals("no change")){
                try {
                    int numAdults = Integer.parseInt(numAdultsStr);
                } catch (NumberFormatException e) {
                    System.out.println("A number must be entered for children");
                }
            }

            int numParams = 0;

            if(!fname.equals("no change")){
                sqlStatement += "firstname = ?";
                firstThing = false;
                numParams++;
            }

            if(!lname.equals("no change")){
                if (firstThing) {
                    sqlStatement += "lastname = ?";
                    firstThing = false;
                }
                else{
                    sqlStatement += ", lastname = ?";
                }
                numParams++;
            }

            if(!numChildrenStr.equals("no change")){
                if (firstThing) {
                    sqlStatement += "kids = ?";
                    firstThing = false;
                }
                else{
                    sqlStatement += ", kids = ?";
                }
                numParams++;
            }

            if(!numAdultsStr.equals("no change")){
                if (firstThing) {
                    sqlStatement += "adults = ?";
                    firstThing = false;
                }
                else{
                    sqlStatement += ", adults = ?";
                }
                numParams++;
            }

            if(!dateEndStr.equals("no change")){
                if (firstThing) {
                    sqlStatement += "checkout = ?";
                    firstThing = false;
                }
                else{
                    sqlStatement += ", checkout = ?";
                }
                numParams++;
            }

            if(!dateStr.equals("no change")){
                if (firstThing) {
                    sqlStatement += "checkin = ?";
                }
                else{
                    sqlStatement += ", checkin = ?";
                }
                numParams++;
            }

            sqlStatement += "where code = ?";

            if(numParams == 0){
                System.out.println("Didn't update anything");
                return;
            }

            if(!checkValidDates(resCode, dateStr, dateEndStr)){
                System.out.println("Dates overlap with existing reservation, cannot add reservation");
                return;
            }


            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {

                // Step 4: Send SQL statement to DBMS


                int numStatements = 1;

                if(!fname.equals("no change")){
                    pstmt.setString(numStatements, fname);
                    numStatements += 1;
                }

                if(!lname.equals("no change")){
                    pstmt.setString(numStatements, lname);
                    numStatements += 1;
                }

                if(!numChildrenStr.equals("no change")){
                    pstmt.setString(numStatements, numChildrenStr);
                    numStatements += 1;
                }

                if(!numAdultsStr.equals("no change")){
                    pstmt.setString(numStatements, numAdultsStr);
                    numStatements += 1;
                }

                if(!dateEndStr.equals("no change")){
                    pstmt.setString(numStatements, dateEndStr);
                    numStatements += 1;
                }

                if(!dateStr.equals("no change")){
                    pstmt.setString(numStatements, dateStr);
                    numStatements += 1;
                }

                pstmt.setString(numStatements, resCode);

                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                if(rowCount == 0) {
                    System.out.format("No reservation found with code %s", resCode);
                }
                else{
                    System.out.format("\nUpdated %d reservation with code %s\n", rowCount, resCode);
                }

                conn.commit();

            } catch (SQLException e){
                conn.rollback();
            }

        } catch (SQLException e){
            System.out.println("Connection couldn't be made with database");
            System.out.println(e.getMessage());
        }

    }


    private void Revenue() {
         try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"), System.getenv("HP_JDBC_USER"), System.getenv("HP_JDBC_PW"))) {
            String sql = "with BeforeMonth as " +
                        "(select monthname(CheckIn) month, Room, sum(datediff(least(Checkout,last_day(CheckIn)), CheckIn) * Rate) Total " +
                        "from Reservations " +
                        "group by monthname(CheckIn), Room " +
                        "order by Room), " +
                        "AfterMonth as " +
                        "(select monthname(CheckOut) month, Room, sum((datediff(CheckOut, last_day(CheckIn)) - 1) * Rate) Total " +
                        "from Reservations " +
                        "where month(CheckIn) <> month(CheckOut) " +
                        "group by monthname(CheckOut), Room " +
                        "order by Room), " +
                        "TotalMonth as " +
                        "(select BeforeMonth.month month, BeforeMonth.Room Room, case " +
                            "when AfterMonth.Total is null then BeforeMonth.Total " +
                            "else (BeforeMonth.Total + AfterMonth.Total) " +
                        "end Total " +
                        "from BeforeMonth " +
                            "left join AfterMonth on BeforeMonth.month = AfterMonth.month " +
                            "and BeforeMonth.Room = AfterMonth.Room), " +
                        "Jan as " +
                        "(select Room,Total " +
                        "from TotalMonth " +
                        "where month = 'January'), " +
                        "Feb as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'February'), " +
                        "Mar as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'March'), " +
                        "Apr as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'April'), " +
                        "May as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'May'), " +
                        "Jun as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'June'), " +
                        "Jul as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'July'), " +
                        "Aug as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'August'), " +
                        "Sep as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'September'), " +
                        "Oct as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'October'), " +
                        "Nov as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'November'), " +
                        "Decb as " +
                        "(select Room, Total " +
                        "from TotalMonth " +
                        "where month = 'December'), " +
                        "Totals as " +
                        "(select RoomId, case when Jan.Total is null then 0 else Jan.Total end January, " +
                            "case when Feb.Total is null then 0 else Feb.Total end February, " +
                            "case when Mar.Total is null then 0 else Mar.Total end March, " +
                            "case when Apr.Total is null then 0 else Apr.Total end April, " +
                            "case when May.Total is null then 0 else May.Total end May, " +
                            "case when Jun.Total is null then 0 else Jun.Total end June, " +
                            "case when Jul.Total is null then 0 else Jul.Total end July, " +
                            "case when Aug.Total is null then 0 else Aug.Total end August, " +
                            "case when Sep.Total is null then 0 else Sep.Total end September, " +
                            "case when Oct.Total is null then 0 else Oct.Total end October, " +
                            "case when Nov.Total is null then 0 else Nov.Total end November, " +
                            "case when Decb.Total is null then 0 else Decb.Total end December " +
                        "from Rooms " +
                            "left join Jan on RoomId = Jan.Room " +
                            "left join Feb on RoomId = Feb.Room " +
                            "left join Mar on RoomId = Mar.Room " +
                            "left join Apr on RoomId = Apr.Room " +
                            "left join May on RoomId = May.Room " +
                            "left join Jun on RoomId = Jun.Room " +
                            "left join Jul on RoomId = Jul.Room " +
                            "left join Aug on RoomId = Aug.Room " +
                            "left join Sep on RoomId = Sep.Room " +
                            "left join Oct on RoomId = Oct.Room " +
                            "left join Nov on RoomId = Nov.Room " +
                            "left join Decb on RoomId = Decb.Room) " +
                        "select RoomId, January, February, March, April, May, June, " +
                        "July, August, September, October, November, December, " +
                        "(January+ February+ March+ April+ May+ June+ " +
                        "July+ August+ September+ October+ November+ December) Total " +
                        "from Totals";

            conn.setAutoCommit(false);
            try(Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                System.out.println("RoomId Jan  Feb  Mar  Apr  May  Jun  " +
                        "Jul  Aug  Sept  Oct  Nov  Dec Total");
                while (rs.next()) {
                    String room = rs.getString("RoomId");
                    String jan = rs.getString("January");
                    String feb = rs.getString("February");
                    String mar = rs.getString("March");
                    String apr = rs.getString("April");
                    String may = rs.getString("May");
                    String jun = rs.getString("June");
                    String jul = rs.getString("July");
                    String aug = rs.getString("August");
                    String sep = rs.getString("September");
                    String oct = rs.getString("October");
                    String nov = rs.getString("November");
                    String dec = rs.getString("December");
                    String total = rs.getString("Total");
                    System.out.format("%6s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %4s %s\n", room, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, total);
                }
            } catch (SQLException e){
                System.out.println("catch" + e.getMessage());
                conn.rollback();
            }

        } catch (SQLException e){
            System.out.println("Connection couldn't be made with database" + e.getMessage());
        }

    }

    private void SearchReservation(Scanner scanner) {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            // Step 2: Construct sql statement
            String firstName;
            String lastName;
            String checkIn;
            String checkOut;
            String roomCode;
            String resCode;

            System.out.println("Search with: ");
            System.out.print("First name: ");
            firstName = scanner.nextLine();

            System.out.print("Last name: ");
            lastName = scanner.nextLine();

            System.out.print("Check in date: ");
            checkIn = scanner.nextLine();

            System.out.print("Check out date: ");
            checkOut = scanner.nextLine();

            System.out.print("Room Code: ");
            roomCode = scanner.nextLine();

            System.out.print("Reservation Code: ");
            resCode = scanner.nextLine();

            String sqlStatement = "SELECT * from lab7_reservations where ";

            if (firstName.equals(""))
                sqlStatement += "firstname is not null ";
            else
                sqlStatement += "firstname LIKE ? ";

            if (!lastName.equals(""))
                sqlStatement += "and lastname LIKE ? ";

            if (!checkIn.equals(""))
                sqlStatement += "and checkIn LIKE ? ";

            if (!checkOut.equals(""))
                sqlStatement += "and checkOut LIKE ? ";

            if (!roomCode.equals(""))
                sqlStatement += "and room LIKE ? ";

            if (!resCode.equals(""))
                sqlStatement += "and code LIKE ? ";

            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {

                // Step 4: Send SQL statement to DBMS
                int numParams = 0;
                if (!firstName.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, firstName);
                }

                if (!lastName.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, lastName);
                }

                if (!checkIn.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, checkIn);
                }

                if (!checkOut.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, checkOut);
                }

                if (!roomCode.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, roomCode);
                }

                if (!resCode.equals("")) {
                    numParams++;
                    pstmt.setString(numParams, resCode);
                }

                ResultSet rs = pstmt.executeQuery();

                while(rs.next()) {
                    System.out.print("Reservation Code: " + rs.getInt("code") + ", ");
                    System.out.print("Room Code: " + rs.getString("room") + ", ");
                    System.out.print("Nightly Rate: " + rs.getString("rate") + ", ");
                    System.out.print("Last Name: " + rs.getString("lastname") + ", ");
                    System.out.print("First Name: " + rs.getString("firstname") + ", ");
                    System.out.print("Adults: " + rs.getInt("adults") + ", ");
                    System.out.print("Kids: " + rs.getInt("kids") + ", ");
                    System.out.print("Check in Date: " + rs.getString("checkIn") + ", ");
                    System.out.println("Check out Date: " + rs.getString("checkOut"));
                }


                // Step 5: Handle results

                conn.commit();

            } catch (SQLException e){
                System.out.println("Rollback" + e);
                conn.rollback();
            }

        } catch (SQLException e){
            System.out.println("Connection couldn't be made with database");
        }

    }

}
