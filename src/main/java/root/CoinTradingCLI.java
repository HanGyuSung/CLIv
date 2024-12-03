import java.sql.*;
import java.util.Scanner;

public class CoinTradingCLI {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/coin_trading";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "0707";

    private static int currentUserId = -1;

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to the database!");
            runCLI(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database!");
        }
    }

    private static void runCLI(Connection connection) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (currentUserId == -1) {
                System.out.println("\n=== Coin Trading Platform ===");
                System.out.println("1. Login");
                System.out.println("2. Exit");
                System.out.print("Select an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 1) {
                    login(connection, scanner);
                } else if (choice == 2) {
                    System.out.println("Exiting the application...");
                    break;
                } else {
                    System.out.println("Invalid option. Try again.");
                }
            } else {
                showMainMenu(connection, scanner);
            }
        }
    }

    private static void login(Connection connection, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                System.out.println("Login successful!");
            } else {
                System.out.println("Invalid credentials. Try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showMainMenu(Connection connection, Scanner scanner) {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. View Wallet");
        System.out.println("2. View Market Data");
        System.out.println("3. Place Order");
        System.out.println("4. Logout");
        System.out.print("Select an option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1 -> viewWallet(connection);
            case 2 -> viewMarketData(connection);
            case 3 -> placeOrder(connection, scanner);
            case 4 -> logout();
            default -> System.out.println("Invalid option. Try again.");
        }
    }

    private static void viewWallet(Connection connection) {
        String sql = "SELECT coin, balance FROM wallets WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n=== Your Wallet ===");
            while (rs.next()) {
                System.out.println("Coin: " + rs.getString("coin") + ", Balance: " + rs.getBigDecimal("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewMarketData(Connection connection) {
        String sql = "SELECT coin, price, last_updated FROM market_data";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n=== Market Data ===");
            while (rs.next()) {
                System.out.println("Coin: " + rs.getString("coin") + ", Price: " + rs.getBigDecimal("price") +
                        ", Last Updated: " + rs.getTimestamp("last_updated"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void placeOrder(Connection connection, Scanner scanner) {
        System.out.print("Enter coin: ");
        String coin = scanner.nextLine();
        System.out.print("Enter order type (market, limit, stop): ");
        String orderType = scanner.nextLine();
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter price (for limit or stop orders): ");
        double price = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO orders (user_id, coin, order_type, amount, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, coin);
            stmt.setString(3, orderType);
            stmt.setDouble(4, amount);
            stmt.setDouble(5, price);
            stmt.executeUpdate();
            System.out.println("Order placed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void logout() {
        currentUserId = -1;
        System.out.println("Logged out successfully.");
    }
}
