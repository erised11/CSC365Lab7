import java.util.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

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
        //TODO: FR-1
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

    private void Revenue() {
         try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {
            String sql = "with BeforeMonth as" +
                        "(select monthname(CheckIn) month, Room, sum(datediff(least(Checkout,last_day(CheckIn)), CheckIn) * Rate) Total" +
                        "from Reservations" +
                        "group by monthname(CheckIn), Room" +
                        "order by Room)," +
                        "AfterMonth as " +
                        "(select monthname(CheckOut) month, Room, sum((datediff(CheckOut, last_day(CheckIn)) - 1) * Rate) Total" +
                        "from Reservations" +
                        "where month(CheckIn) <> month(CheckOut)" +
                        "group by monthname(CheckOut), Room" +
                        "order by Room)," +
                        "TotalMonth as " +
                        "(select BeforeMonth.month month, BeforeMonth.Room Room, case" +
                            "when AfterMonth.Total is null then BeforeMonth.Total" +
                            "else (BeforeMonth.Total + AfterMonth.Total) " +
                        "end Total" +
                        "from BeforeMonth " +
                            "left join AfterMonth on BeforeMonth.month = AfterMonth.month" +
                            "and BeforeMonth.Room = AfterMonth.Room)," +
                        "Jan as " +
                        "(select Room,Total" +
                        "from TotalMonth" +
                        "where month = 'January')," +
                        "Feb as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'February')," +
                        "Mar as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'March')," +
                        "Apr as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'April')," +
                        "May as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'May')," +
                        "Jun as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'June')," +
                        "Jul as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'July')," +
                        "Aug as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'August')," +
                        "Sep as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'September')," +
                        "Oct as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'October')," +
                        "Nov as " +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'November')," +
                        "Decb as" +
                        "(select Room, Total" +
                        "from TotalMonth" +
                        "where month = 'December')," +
                        "Totals as " +
                        "(select RoomId, case when Jan.Total is null then 0 else Jan.Total end January," +
                            "case when Feb.Total is null then 0 else Feb.Total end February, " +
                            "case when Mar.Total is null then 0 else Mar.Total end March," +
                            "case when Apr.Total is null then 0 else Apr.Total end April," +
                            "case when May.Total is null then 0 else May.Total end May," +
                            "case when Jun.Total is null then 0 else Jun.Total end June," +
                            "case when Jul.Total is null then 0 else Jul.Total end July," +
                            "case when Aug.Total is null then 0 else Aug.Total end August," +
                            "case when Sep.Total is null then 0 else Sep.Total end September," +
                            "case when Oct.Total is null then 0 else Oct.Total end October," +
                            "case when Nov.Total is null then 0 else Nov.Total end November," +
                            "case when Decb.Total is null then 0 else Decb.Total end December" +
                        "from Rooms" +
                            "left join Jan on RoomId = Jan.Room" +
                            "left join Feb on RoomId = Feb.Room" +
                            "left join Mar on RoomId = Mar.Room" +
                            "left join Apr on RoomId = Apr.Room" +
                            "left join May on RoomId = May.Room" +
                            "left join Jun on RoomId = Jun.Room" +
                            "left join Jul on RoomId = Jul.Room" +
                            "left join Aug on RoomId = Aug.Room" +
                            "left join Sep on RoomId = Sep.Room" +
                            "left join Oct on RoomId = Oct.Room" +
                            "left join Nov on RoomId = Nov.Room" +
                            "left join Decb on RoomId = Decb.Room)" +
                        "select RoomId, January, February, March, April, May, June," +
                        "July, August, September, October, November, December," +
                        "(January+ February+ March+ April+ May+ June+" +
                        "July+ August+ September+ October+ November+ December) Total" +
                        "from Totals";
            try(Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
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
                    System.out.format("%s %s %s %s %s %s %s %s %s %s %s %s %s", room, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, total);
                }
            } catch (SQLException e){
                conn.rollback();
            }

        } catch (SQLException e){
            System.out.println("Connection couldn't be made with database");
        }

    }

}
