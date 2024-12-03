package root.cointrading.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class TradingService {
    public void viewWallet(Connection connection, int userId) {
        String sql = "SELECT coin, balance FROM wallets WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n=== Your Wallet ===");
            while (rs.next()) {
                System.out.println("Coin: " + rs.getString("coin") + ", Balance: " + rs.getBigDecimal("balance"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewMarketData(Connection connection) {
        String sql = "SELECT coin, price, last_updated FROM market_data";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n=== Market Data ===");
            while (rs.next()) {
                System.out.println("Coin: " + rs.getString("coin") + ", Price: " + rs.getBigDecimal("price") + ", Last Updated: " + rs.getTimestamp("last_updated"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void placeOrder(Connection connection, Scanner scanner, int userId) {
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
            stmt.setInt(1, userId);
            stmt.setString(2, coin);
            stmt.setString(3, orderType);
            stmt.setString(4, side);
            stmt.setDouble(5, amount);
            stmt.setDouble(6, price);
            stmt.executeUpdate();
            System.out.println("Order placed successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelOrder(Connection connection, Scanner scanner, int userId) {
        System.out.print("Enter order ID to cancel: ");
        int orderId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM orders WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
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

    public void viewOrdersByCoin(Connection connection, Scanner scanner) {
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
}
