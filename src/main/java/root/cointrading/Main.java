package root.cointrading;

import root.cointrading.service.TradingService;
import root.cointrading.service.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database!");
            UserService userService = new UserService();
            TradingService tradingService = new TradingService();

            runCLI(connection, userService, tradingService);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database!");
        }
    }

    private static void runCLI(Connection connection, UserService userService, TradingService tradingService) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (userService.getCurrentUserId() == -1) {
                System.out.println("\n=== Coin Trading Platform ===");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Select an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 1) {
                    userService.login(connection, scanner);
                } else if (choice == 2) {
                    userService.registerUser(connection, scanner);
                } else if (choice == 3) {
                    System.out.println("Exiting the application...");
                    break;
                } else {
                    System.out.println("Invalid option. Try again.");
                }
            } else {
                // 메인 메뉴에서의 추가적인 작업을 정의합니다.
                System.out.println("\n=== Main Menu ===");
                System.out.println("1. View Wallet");
                System.out.println("2. Logout");
                System.out.print("Select an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> tradingService.viewWallet(connection, userService.getCurrentUserId());
                    case 2 -> userService.logout();
                    default -> System.out.println("Invalid option. Try again.");
                }
            }
        }
    }
}
