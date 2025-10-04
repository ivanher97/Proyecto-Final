package com.routeiq.connection;

import java.sql.Connection;
import java.sql.DriverManager;

/*
Clase para conectar con la BBDD, exactamente con la que almacena las dimensiones.
 */
public class DatabaseConnectionCONS {
    public static Connection getConnection() throws Exception {
        String url = p.getProperty("db.url_CONS");
        String user = p.getProperty("db.user");
        String password = p.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}