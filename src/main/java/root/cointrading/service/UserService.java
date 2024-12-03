package root.cointrading.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserService {
    private int currentUserId = -1;

    public void login(Connection connection, Scanner scanner) {
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

    public void registerUser(Connection connection, Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter initial deposit amount: ");
        double initialDeposit = Double.parseDouble(scanner.nextLine());

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                TradingService.addInitialDeposit(connection, userId, initialDeposit);
                System.out.println("Registration successful! You can now log in.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        currentUserId = -1;
        System.out.println("Logged out successfully.");
    }

    public int getCurrentUserId() {
        return currentUserId;
    }
}
