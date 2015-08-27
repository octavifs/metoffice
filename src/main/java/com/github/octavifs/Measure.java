package com.github.octavifs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.util.List;

/**
 * Created by octavi on 24/08/15.
 */
public class Measure {
    private final String station;
    private final Date timestamp;
    private final Double tempMin;
    private final Double tempMax;
    private final Integer airfrostDays;
    private final Double rain;
    private final Double sunHours;

    public Measure(
            String station,
            Date timestamp,
            Double tempMin,
            Double tempMax,
            Integer airfrostDays,
            Double rain,
            Double sunHours
    ) {
        this.station = station;
        this.timestamp =  timestamp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.airfrostDays = airfrostDays;
        this.rain = rain;
        this.sunHours = sunHours;
    }

    // Copy constructor
    public Measure(Measure m) {
        this.station = m.getStation();
        this.timestamp =  m.getTimestamp();
        this.tempMin = m.getTempMin();
        this.tempMax = m.getTempMax();
        this.airfrostDays = m.getAirfrostDays();
        this.rain = m.getRain();
        this.sunHours = m.getSunHours();
    }


    public String getStation() {
        return station;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public Double getTempMin() {
        return tempMin;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public Integer getAirfrostDays() {
        return airfrostDays;
    }

    public Double getRain() {
        return rain;
    }

    public Double getSunHours() {
        return sunHours;
    }

    public static void dbAddMeasure(Measure m, boolean commit) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(commit);
        String insertStatement =
                "INSERT INTO measures (\"station\", \"timestamp\", \"tempMin\", \"tempMax\", \"airfrostDays\", \"rain\", \"sunHours\") " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insert = conn.prepareStatement(insertStatement);
        insert.setString(1, m.getStation());
        insert.setDate(2, m.getTimestamp());
        if (m.getTempMin() != null) {
            insert.setDouble(3, m.getTempMin());
        } else {
            insert.setNull(3, Types.REAL);
        }
        if (m.getTempMax() != null) {
            insert.setDouble(4, m.getTempMax());
        } else {
            insert.setNull(4, Types.REAL);
        }
        if (m.getAirfrostDays() != null) {
            insert.setInt(5, m.getAirfrostDays());
        } else {
            insert.setNull(5, Types.INTEGER);
        }
        if (m.getRain() != null) {
            insert.setDouble(6, m.getRain());
        } else {
            insert.setNull(6, Types.REAL);
        }
        if (m.getSunHours() != null) {
            insert.setDouble(7, m.getSunHours());
        } else {
            insert.setNull(7, Types.REAL);
        }
        insert.executeUpdate();
    }

    public static void dbAddMeasure(Measure m) throws SQLException{
        dbAddMeasure(m, true);
    }

    public static void dbAddMeasures(List<Measure> measures) throws SQLException {
        Connection conn = DBConnection.getConnection();
        for (Measure m : measures) {
            dbAddMeasure(m, false);
        }
        conn.commit();
        conn.setAutoCommit(true);
    }

    @Override
    public String toString() {
        return "Measure{" +
                "station='" + station + '\'' +
                ", timestamp=" + timestamp +
                ", tempMin=" + tempMin +
                ", tempMax=" + tempMax +
                ", airfrostDays=" + airfrostDays +
                ", rain=" + rain +
                ", sunHours=" + sunHours +
                '}';
    }
}
