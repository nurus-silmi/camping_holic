package com.mycompany.sewa_camping.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/sewa_camping",
                        "root",
                        ""
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}