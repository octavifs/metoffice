package com.github.octavifs;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by octavi on 26/08/15.
 */
public enum DBConnection{
    INSTANCE;

    private Connection conn;
    private static final String STATIONS_TABLE = "CREATE TABLE IF NOT EXISTS stations (" +
            "	\"name\" varchar(200) PRIMARY KEY," +
            "	\"latitude\" real," +
            "	\"longitude\" real" +
            ")";
    private static final String MEASURES_TABLE = "CREATE TABLE IF NOT EXISTS measures (" +
            "	\"pk\" SERIAL PRIMARY KEY," +
            "	\"station\" varchar(200) REFERENCES stations ON DELETE CASCADE," +
            "	\"timestamp\" date NOT NULL," +
            "	\"tempMin\" real DEFAULT NULL," +
            "	\"tempMax\" real DEFAULT NULL," +
            "	\"airfrostDays\" integer DEFAULT NULL," +
            "	\"rain\" real DEFAULT NULL," +
            "	\"sunHours\" real DEFAULT NULL" +
            ")";
    {
        // Load DB driver for PostgreSQL
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Install postgreSQL driver");
            System.exit(1);
        }
        // Open DB connection
        try {
            conn = DriverManager.getConnection("jdbc:postgresql:octavi");
            System.out.println("Connected to DB successfully");
        } catch (SQLException e) {
            System.out.println("Could not connect to the DB. Aborting...");
            System.exit(1);
        }

    }
    public static Connection getConnection() {
        return INSTANCE.conn;
    }

    public static void createSchema() throws  SQLException {
        Statement st = INSTANCE.conn.createStatement();
        st.execute(INSTANCE.STATIONS_TABLE);
        st.execute(INSTANCE.MEASURES_TABLE);
        st.close();
    }

    public static void dropSchema() throws SQLException {
        Statement st = INSTANCE.conn.createStatement();
        st.execute("DROP TABLE IF EXISTS measures");
        st.execute("DROP TABLE IF EXISTS stations");
        st.close();
    }
}