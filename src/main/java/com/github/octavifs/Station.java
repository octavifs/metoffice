package com.github.octavifs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by octavi on 25/08/15.
 */
public class Station {
    private final String name;
    private final Coordinates coordinates;

    public Station(String name, Coordinates coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public Station(String name, double latitude, double longitude) {
        this.name = name;
        this.coordinates = new Coordinates(latitude, longitude);
    }

    public Station(Station s) {
        this.name = s.getName();
        this.coordinates = s.getCoordinates();
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {return new Coordinates(coordinates);}

    public double getLatitude() {
        return coordinates.getLatitude();
    }

    public double getLongitude() {
        return coordinates.getLongitude();
    }

    public static void dbAddStation(Station s, boolean commit) throws SQLException{
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        String insertStatement =
                "INSERT INTO stations VALUES " +
                "(?, ?, ?)";
        PreparedStatement insert = conn.prepareStatement(insertStatement);
        insert.setString(1, s.getName());
        insert.setDouble(2, s.getLatitude());
        insert.setDouble(3, s.getLongitude());
        insert.executeUpdate();
        if (commit)
            conn.commit();
        conn.setAutoCommit(true);
    }

    public static void dbAddStation(Station s) throws SQLException{
        dbAddStation(s, true);
    }

    public static void dbAddStations(List<Station> stations) throws SQLException {
        Connection conn = DBConnection.getConnection();
        for (Station s : stations) {
            dbAddStation(s, false);
        }
        conn.setAutoCommit(false);
        conn.commit();
        conn.setAutoCommit(true);
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + name + '\'' +
                ", coordinates=" + coordinates.toString();
    }
}