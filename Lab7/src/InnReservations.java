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

    private void CancelReservation(Scanner scanner) throws SQLException {

        // Step 1: Establish connection to RDBMS
        try (Connection conn = DriverManager.getConnection(System.getenv("HP_JDBC_URL"),
                System.getenv("HP_JDBC_USER"),
                System.getenv("HP_JDBC_PW"))) {

            // Step 2: Construct sql statement
            String resCode;
            System.out.print("Enter a reservation code to cancel");
            resCode = scanner.nextLine();

            String sqlStatement = "DELETE from lab7_reservations where code = ?";

            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, resCode);
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                System.out.format("Deleted %d reservation with code %s", rowCount, resCode);


            } catch (SQLException e){
                conn.rollback();
            }

        }

    }

}
