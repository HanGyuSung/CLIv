package root.cointrading.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserService {
    public int login(Connection connection, Scanner scanner) {
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
                System.out.println("Login successful!");
                return rs.getInt("id");
            } else {
                System.out.println("Invalid credentials. Try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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
                addInitialDeposit(connection, userId, initialDeposit);
                System.out.println("Registration successful! You can now log in.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addInitialDeposit(Connection connection, int userId, double amount) {
        String sql = "INSERT INTO wallets (user_id, coin, balance) VALUES (?, 'USD', ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        System.out.println("Logged out successfully.");
    }
}
