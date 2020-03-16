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


    private void runPrompt(Scanner scanner) {
        System.out.println("Select an option\n");
        System.out.println("Rooms and Rates (1)");
        System.out.println("Reservations (2)");
        System.out.println("Reservation Change (3)");
        System.out.println("Reservation Cancellation (4)");
        System.out.println("Detailed Reservation Information (5)");
        System.out.println("Exit (0)\n");

        String input = "garbage";

        while(!input.equals('0')){
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

    private void RoomsAndRates(){
                
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
                    System.out.println("Room Code: "+rs.getString("RoomCode"));
                    System.out.println("Room Name: "+rs.getString("RoomName"));
                    System.out.println("Beds: "+rs.getString("Beds"));
                    System.out.println("Bed Type: "+rs.getString("bedType"));
                    System.out.println("Max Occ.: "+rs.getInt("maxOcc"));
                    System.out.println("Base Price: "+rs.getString("basePrice"));
                    System.out.println("Decor: "+rs.getString("decor"));
                    System.out.println("Popularity: "+rs.getString("popularity"));
                    System.out.println("Next Available: "+rs.getString("next available"));
                    System.out.println("Length of Last Stay: "+rs.getInt("length of last stay"));
                    System.out.println("Last Checkout: "+rs.getString("last checkout"));
                    System.out.println("\n");
                }
                
                
            } catch (SQLException e){
                System.out.println("prepare statement didnt work yoikes");
                
                conn.rollback();
            }

        } catch (SQLException e){
            System.out.println("Connection couldn't be made with database");
        }

    }

    private void Reservations(Scanner scanner){
        String fName, lName, roomCode, bedType;
        Date beginDate, endDate;
        int numChildren, numAdults;

        System.out.println("Please enter the following information:\n");
        System.out.print("First name: ");
        fName = scanner.nextLine();
        System.out.print("Last name: ");
        lName = scanner.nextLine();
        System.out.print("Room code ('Any' if no preference): ");
        roomCode = scanner.nextLine();
        System.out.print("Bed type ('Any' if no preference): ");
        bedType = scanner.nextLine();
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

        private void ChangeReservation(Scanner scanner) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            // Step 1: Establish connection to RDBMS
            try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                    System.getenv("HP_JDBC_USER"),
                    System.getenv("HP_JDBC_PW"))) {

                // Step 2: Construct sql statement
                String resCode, fname, lname;
                Date beginDate, endDate;
                System.out.print("Enter a reservation code to change: ");
                resCode = scanner.nextLine();

                System.out.print("Enter updated first name (or 'no change'): ");
                fname = scanner.nextLine();

                System.out.print("Enter updated last name: (or 'no change')");
                lname = scanner.nextLine();

                System.out.println("Enter updated begin date (format: yyyy-MM-dd): (or 'no change')");
                String dateStr = scanner.nextLine();
                if(!dateStr.equals("no change")){
                    try {
                        Date dateBegin = sdf.parse(dateStr);
                    } catch (ParseException e) {
                        System.out.println("Date entered incorrectly");
                    }
                }

                System.out.println("Enter updated end date (format: yyyy-MM-dd): (or 'no change')");
                String dateEndStr = scanner.nextLine();
                if(!dateStr.equals("no change")) {
                    try {
                        Date dateEnd = sdf.parse(dateStr);
                    } catch (ParseException e) {
                        System.out.println("Date entered incorrectly");
                    }
                }

                int numChildren, numAdults;

                System.out.print("Enter updated number of children: ");
                try {
                    numChildren = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("A number must be entered for children");
                }

                System.out.print("Enter updated number of adults: ");
                try {
                    numAdults = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("A number must be entered for children");
                }


                String sqlStatement = "UPDATE lab7_reservations set where code = ?";

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
}
