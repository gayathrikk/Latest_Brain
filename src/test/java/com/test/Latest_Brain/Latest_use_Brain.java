package com.test.Latest_Brain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Latest_use_Brain {

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    // Database credentials
    private static final String URL = "jdbc:mysql://apollo2.humanbrain.in:3306/HBA_V2";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Health#123";

    private static final String SQL_QUERY = "SELECT \r\n"
            + "    activity.id,\r\n"
            + "    activity.timestamp,\r\n"
            + "    activity.action,\r\n"
            + "    activity.info,\r\n"
            + "    CC_User.user_name,\r\n"
            + "    seriesset.name AS seriesset_name\r\n"
            + "FROM activity\r\n"
            + "JOIN CC_User ON CC_User.id = activity.user\r\n"
            + "JOIN seriesset ON seriesset.id = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(activity.info, ':', 1), '-', -1) AS UNSIGNED)\r\n"
            + "WHERE DATE(activity.timestamp) = CURDATE() - INTERVAL 1 DAY\r\n"
            + "  AND activity.action != 'Login'\r\n"
            + "  AND activity.info LIKE 'SS-%'\r\n"
            + "ORDER BY seriesset.name, activity.info ASC;";

    @BeforeClass
    public void setup() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection failed.");
        }
    }

    @Test
    public void verifyDatabaseRecords() {
        try {
            stmt = conn.prepareStatement(SQL_QUERY);
            rs = stmt.executeQuery();

            // Table format
            String format = "%-15s %-25s %-25s %-30s %-25s %-30s%n";

            // Print headers
            System.out.printf(format, "id", "timestamp", "action", "info", "user_name", "name");
            System.out.println("-".repeat(150));

            String lastSeriesName = null;

            while (rs.next()) {
                String currentSeriesName = rs.getString("seriesset_name");

                if (lastSeriesName != null && !lastSeriesName.equals(currentSeriesName)) {
                    System.out.println(); // Space between seriesset.name groups
                }
                lastSeriesName = currentSeriesName;

                System.out.printf(format,
                        rs.getString("id"),
                        rs.getString("timestamp"),
                        rs.getString("action"),
                        rs.getString("info"),
                        rs.getString("user_name"),
                        currentSeriesName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error executing query.");
        }
    }

    @AfterClass
    public void tearDown() {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
