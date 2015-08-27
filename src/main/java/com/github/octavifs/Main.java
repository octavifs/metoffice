package com.github.octavifs;

import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

import static spark.Spark.get;

class Main {

    public static void main(String[] args) {
        get(new Route("/stations") {
            @Override
            public Object handle(Request request, Response response) {
                return queryStations();
            }
        });
        get(new Route("/stations/:name") {
            @Override
            public Object handle(Request request, Response response) {
                String name = request.params(":name");
                try {
                    name = new URLDecoder().decode(name, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    assert true;
                }
                return queryStation(name);
            }
        });
        get(new Route("/stations/:name/measures") {
            @Override
            public Object handle(Request request, Response response) {
                String station = request.params(":name");
                try {
                    station = new URLDecoder().decode(station, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    assert true;
                }
                return queryMeasures(station);
            }
        });
    }

    public static void initDB() {
        Parser parser = new Parser();
        Connection db = DBConnection.getConnection();
        // Parse the data from all the resources available on the net
        try {
            parser.parse();
        } catch (IOException e) {
            System.err.println("Fatal error while parsing the data. Is the network up?");
            e.printStackTrace();
            System.exit(1);
        }
        // Drop the schema and add the data to the DB
        try {
            // Init
            DBConnection.dropSchema();
            DBConnection.createSchema();
            // Add stations
            Station.dbAddStations(parser.getStations());
            for (List<Measure> l : parser.getStationMeasures().values()) {
                Measure.dbAddMeasures(l);
            }
        } catch (SQLException e) {
            System.err.println("Fatal error while setting up the database.");
            e.printStackTrace();
            System.exit((1));
        }
    }

    public static JSONArray queryStations() {
        Connection conn = DBConnection.getConnection();
        JSONArray results = new JSONArray();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(
                    "SELECT name, latitude, longitude FROM stations"
            );
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("name", rs.getString("name"));
                JSONObject coordinates = new JSONObject();
                coordinates.put("latitude", rs.getDouble("latitude"));
                coordinates.put("longitude", rs.getDouble("longitude"));
                row.put("coordinates", coordinates);
                results.put(row);
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while retrieving stations");
            e.printStackTrace();
        }
        return results;
    }

    public static JSONObject queryStation(String name) {
        Connection conn = DBConnection.getConnection();
        JSONObject row = new JSONObject();
        try {
            String query = "SELECT name, latitude, longitude FROM stations WHERE name = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            System.out.println(statement.toString());
            ResultSet rs = statement.executeQuery();
            rs.next();
            row.put("name", rs.getString("name"));
            JSONObject coordinates = new JSONObject();
            coordinates.put("latitude", rs.getDouble("latitude"));
            coordinates.put("longitude", rs.getDouble("longitude"));
            row.put("coordinates", coordinates);
            return row;
        } catch (SQLException e) {
            System.err.println("Something went wrong while retrieving stations");
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray queryMeasures(String station, String from, String to) {
        Connection conn = DBConnection.getConnection();
        JSONArray results = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(
                    "SELECT \"station\", \"timestamp\", \"tempMin\", \"tempMax\", \"airfrostDays\", \"rain\", \"sunHours\"" +
                            "FROM measures WHERE \"station\" = ? ORDER BY \"timestamp\" ASC"
            );
            statement.setString(1, station);
            ResultSet rs = statement.executeQuery();
            SimpleDateFormat df = new SimpleDateFormat("MM-yyyy");
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("station", rs.getString("station"));
                row.put("timestamp", df.format(rs.getDate("timestamp")));
                row.put("tempMin", rs.getDouble("tempMin"));
                row.put("tempMax", rs.getDouble(("tempMax")));
                row.put("airfrostDays", rs.getInt("airfrostDays"));
                row.put("rain", rs.getDouble("rain"));
                row.put("sunHours", rs.getDouble("sunHours"));
                results.put(row);
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong while retrieving stations");
            e.printStackTrace();
        }
        return results;
    }

    public static JSONArray queryMeasures(String station) {
        return queryMeasures(station, null, null);
    }
}
