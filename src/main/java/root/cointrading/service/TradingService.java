package root.cointrading.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class TradingService {

    public static void addInitialDeposit(Connection connection, int userId, double amount) {
        String sql = "INSERT INTO wallets (user_id, coin, balance) VALUES (?, 'USD', ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewWallet(Connection connection, int currentUserId) {
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

    // 다른 거래 관련 메서드들 (주문 생성, 매칭 등)을 추가합니다...
}
