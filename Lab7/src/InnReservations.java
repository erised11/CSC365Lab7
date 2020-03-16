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
                    SearchReservation(scanner);
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

            if (firstName.equals("")) {
                sqlStatement += "firstname is not null ";
            }
            else
                sqlStatement += "firstname = ? ";

            if (lastName.equals("")) {
                sqlStatement += "and lastname is not null ";
            }
            else
                sqlStatement += "and lastname = ? ";


            if (checkIn.equals("")) {
                sqlStatement += "and checkIn is not null ";
            }
            else
                sqlStatement += "and checkIn = ? ";

            if (checkOut.equals("")) {
                sqlStatement += "and checkOut is not null ";
            }
            else
                sqlStatement += "and checkOut = ? ";

            if (roomCode.equals("")) {
                sqlStatement += "and room is not null ";
            }
            else
                sqlStatement += "and room = ? ";

            if (resCode.equals("")) {
                sqlStatement += "and code is not null ";
            }
            else
                sqlStatement += "and code = ? ";

            // Step 3: start transaction
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sqlStatement)) {

                System.out.println("***");
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

                System.out.println("***");
                ResultSet rs = pstmt.executeQuery();

                System.out.println("sql: " + sqlStatement);

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
