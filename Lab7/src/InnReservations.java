import java.util.*;

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
        String fName, lName, rCode, bedType;
        Date beginDate, endDate;
        int numChildren, numAdults;

        System.out.println("Please enter the following information:\n");
        System.out.print("First name: ");
        fName = scanner.nextLine();
        System.out.print("Last name: ");
        lName = scanner.nextLine();
        System.out.print("Room code ('Any' if no preference): ");
        rCode = scanner.nextLine();
        System.out.print("Bed type ('Any' if no preference): ");
        bedType = scanner.nextLine();
    }

}
