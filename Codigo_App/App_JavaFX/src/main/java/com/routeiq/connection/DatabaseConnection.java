package com.routeiq.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
Clase para conectar con la BBDD, exactamente con la que almacena los planos y los estudios.
 */
public class DatabaseConnection {
    private static final String URL = p.getProperty("db.url");
    private static final String USER = p.getProperty("db.user");
    private static final String PASSWORD = p.getProperty("db.password");

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}