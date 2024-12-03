import java.sql.*;
import java.util.Scanner;

public class CoinTradingCLI2_origin {
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
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Select an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 1) {
                    login(connection, scanner);
                } else if (choice == 2) {
                    registerUser(connection, scanner);
                } else if (choice == 3) {
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

    private static void registerUser(Connection connection, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter initial deposit amount: ");
        double initialDeposit = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                addInitialDeposit(connection, userId, initialDeposit);
                System.out.println("Registration successful! You can now log in.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addInitialDeposit(Connection connection, int userId, double amount) {
        String sql = "INSERT INTO wallets (user_id, coin, balance) VALUES (?, 'USD', ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showMainMenu(Connection connection, Scanner scanner) {
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
            case 1 -> viewWallet(connection);
            case 2 -> viewMarketData(connection);
            case 3 -> placeOrder(connection, scanner);
            case 4 -> cancelOrder(connection, scanner);
            case 5 -> viewOrdersByCoin(connection, scanner);
            case 6 -> logout();
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
        System.out.print("Enter side (buy, sell): ");
        String side = scanner.nextLine();
        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter price (for limit or stop orders): ");
        double price = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO orders (user_id, coin, order_type, side, amount, price) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, coin);
            stmt.setString(3, orderType);
            stmt.setString(4, side);
            stmt.setDouble(5, amount);
            stmt.setDouble(6, price);
            stmt.executeUpdate();

            // Check for matching orders
            if (side.equalsIgnoreCase("buy")) {
                matchOrder(connection, coin, "sell", amount, price);
            } else if (side.equalsIgnoreCase("sell")) {
                matchOrder(connection, coin, "buy", amount, price);
            }

            System.out.println("Order placed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void matchOrder(Connection connection, String coin, String oppositeSide, double amount, double price) {
        String sql = "SELECT id, user_id, amount, price FROM orders WHERE coin = ? AND side = ? AND price = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, coin);
            stmt.setString(2, oppositeSide);
            stmt.setDouble(3, price);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int matchingOrderId = rs.getInt("id");
                int matchingUserId = rs.getInt("user_id");
                double matchingAmount = rs.getDouble("amount");

                double tradeAmount = Math.min(amount, matchingAmount);

                // Insert into trades table
                String tradeSql = "INSERT INTO trades (buy_order_id, sell_order_id, coin, amount, price) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement tradeStmt = connection.prepareStatement(tradeSql)) {
                    if (oppositeSide.equalsIgnoreCase("sell")) {
                        tradeStmt.setInt(1, matchingOrderId);
                        tradeStmt.setInt(2, currentUserId);
                    } else {
                        tradeStmt.setInt(1, currentUserId);
                        tradeStmt.setInt(2, matchingOrderId);
                    }
                    tradeStmt.setString(3, coin);
                    tradeStmt.setDouble(4, tradeAmount);
                    tradeStmt.setDouble(5, price);
                    tradeStmt.executeUpdate();
                }

                // Update or delete matching order
                if (amount == matchingAmount) {
                    deleteOrder(connection, matchingOrderId);
                    deleteOrder(connection, currentUserId); // Delete the user's order as it is fully matched
                } else if (amount < matchingAmount) {
                    updateOrderAmount(connection, matchingOrderId, matchingAmount - amount);
                } else {
                    deleteOrder(connection, matchingOrderId);
                    updateOrderAmount(connection, currentUserId, amount - matchingAmount); // Update user's order with remaining amount
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteOrder(Connection connection, int orderId) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateOrderAmount(Connection connection, int orderId, double newAmount) {
        String sql = "UPDATE orders SET amount = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, newAmount);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewOrdersByCoin(Connection connection, Scanner scanner) {
        String sqlCoins = "SELECT DISTINCT coin FROM orders";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sqlCoins)) {
            System.out.println("\n=== Available Coins for Orders ===");
            while (rs.next()) {
                System.out.println("Coin: " + rs.getString("coin"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        System.out.print("Enter coin to view orders: ");
        String coin = scanner.nextLine();

        String sql = "SELECT id, user_id, order_type, side, amount, price, timestamp FROM orders WHERE coin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, coin);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n=== Orders for Coin: " + coin + " ===");
            while (rs.next()) {
                System.out.println("Order ID: " + rs.getInt("id") + ", User ID: " + rs.getInt("user_id") + ", Type: " + rs.getString("order_type") + ", Side: " + rs.getString("side") + ", Amount: " + rs.getBigDecimal("amount") + ", Price: " + rs.getBigDecimal("price") + ", Timestamp: " + rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cancelOrder(Connection connection, Scanner scanner) {
        System.out.print("Enter order ID to cancel: ");
        int orderId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM orders WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, currentUserId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Order cancelled successfully!");
            } else {
                System.out.println("Order not found or could not be cancelled.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void logout() {
        currentUserId = -1;
        System.out.println("Logged out successfully.");
    }
}
