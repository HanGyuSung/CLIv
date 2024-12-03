package root;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionTest {
    public static void main(String[] args) {
        // MySQL 연결 정보
        String url = "jdbc:mysql://localhost:3306/coin_trading"; // 데이터베이스 URL
        String user = "root"; // MySQL 사용자 이름
        String password = "0707"; // MySQL 비밀번호

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection successful!"); // 연결 성공 메시지
        } catch (SQLException e) {
            System.out.println("Connection failed!"); // 연결 실패 메시지
            e.printStackTrace(); // 오류 세부 정보 출력
        }
    }
}
