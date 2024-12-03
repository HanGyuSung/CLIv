package root.cointrading;

import root.cointrading.service.UserService;
import root.cointrading.service.TradingService;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    private static int currentUserId = -1;

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connected to the database!");
            runCLI(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database!");
        }
    }

    private static void runCLI(Connection connection) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();
        TradingService tradingService = new TradingService();

        while (true) {
            if (currentUserId == -1) {
                System.out.println("\n=== Coin Trading Platform ===");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Select an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 1) {
                    currentUserId = userService.login(connection, scanner);
                } else if (choice == 2) {
                    userService.registerUser(connection, scanner);
                } else if (choice == 3) {
                    System.out.println("Exiting the application...");
                    break;
                } else {
                    System.out.println("Invalid option. Try again.");
                }
            } else {
                showMainMenu(connection, scanner, userService, tradingService);
            }
        }
    }

    private static void showMainMenu(Connection connection, Scanner scanner, UserService userService, TradingService tradingService) {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. View Wallet");
        System.out.println("2. View Market Data");
        System.out.println("3. Place Order");
        System.out.println("4. Cancel Order");
        System.out.println("5. View Orders by Coin");
        System.out.println("6. Logout");
        System.out.print("Select an option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1 -> tradingService.viewWallet(connection, currentUserId);
            case 2 -> tradingService.viewMarketData(connection);
            case 3 -> tradingService.placeOrder(connection, scanner, currentUserId);
            case 4 -> tradingService.cancelOrder(connection, scanner, currentUserId);
            case 5 -> tradingService.viewOrdersByCoin(connection, scanner);
            case 6 -> {
                userService.logout();
                currentUserId = -1;
            }
            default -> System.out.println("Invalid option. Try again.");
        }
    }
}
