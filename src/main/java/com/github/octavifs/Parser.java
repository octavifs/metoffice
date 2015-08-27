package com.github.octavifs;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.sql.Date;

/**
 * Created by octavi on 24/08/15.
 * Parses input information, retrieves it from the net and returns it as a list of objects
 */
public class Parser {
    private static final String API_URL = "http://data.gov.uk/api/2/rest/package/historic-monthly-meteorological-station-data";
    private List<Station> stations;
    private Map<String, List<Measure>> stationMeasures;
    private boolean parsed = false;

    public Parser() {
        this.stations = new ArrayList<Station>();
        this.stationMeasures = new HashMap<String, List<Measure>>();
    }

    public List<Station> getStations() {
        return stations;
    }

    public Map<String, List<Measure>> getStationMeasures() {
        return stationMeasures;
    }

    public List<Measure> getStationMeasures(String s) {
        return stationMeasures.get(s);
    }

    public boolean isParsed() {
        return parsed;
    }

    // Takes the url
    public void parse() throws MalformedURLException, IOException{
        URL apiUrl = new URL(API_URL);
        JSONObject apiData = new JSONObject(new JSONTokener(apiUrl.openStream()));
        JSONArray resources = apiData.getJSONArray("resources");
        for (int idx = 0; idx < resources.length(); ++idx) {
            JSONObject resource = resources.getJSONObject(idx);
            if (!resource.getString("mimetype").equalsIgnoreCase("text/plain"))
                continue;
            URL stationUrl = new URL(resource.getString("url"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stationUrl.openStream()));
            String stationName = this.parseStationName(reader.readLine());
            Coordinates stationCoordinates = this.parseCoordinates(reader.readLine());
            Station station = new Station(stationName, stationCoordinates);
            // Skip next lines, which will be something like
            // Estimated data is marked with a * after the value.
            // Missing data (more than 2 days missing in month) is marked by  ---.
            // Sunshine data taken from an automatic Kipp & Zonen sensor marked with a #, otherwise sunshine data taken from a Campbell Stokes recorder.
            //    yyyy  mm   tmax    tmin      af    rain     sun
            //               degC    degC    days      mm   hours
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            // Parse measures from retrieved data
            List<Measure> stationMeasures = new ArrayList<Measure>();
            for (String measureStr = reader.readLine(); measureStr != null; measureStr = reader.readLine()) {
                try {
                    stationMeasures.add(parseMeasure(station.getName(), measureStr));
                } catch (NumberFormatException e) {
                    System.err.printf("Error while parsing \"%s\" in %s. Skipping measure...\n", measureStr, station.getName());
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.printf("Error while parsing \"%s\" in %s. Skipping measure...\n", measureStr, station.getName());
                }
            }
            // Add parsed data to private members
            this.stations.add(station);
            this.stationMeasures.put(station.getName(), stationMeasures);
            System.out.printf("Successfully parsed %s.\n", station.getName());
        }
        // parsed is true now
        this.parsed = true;
    }

    // Input format
    // Heathrow (London Airport)
    private String parseStationName(String s) {
        return s.trim().toLowerCase();
    }

    // Input format
    // Location 5078E 1767N 25m amsl
    private Coordinates parseCoordinates(String s) {
        // TODO: Implement a real parser for this, that converts this british coordinate system to lat lng (hahaha)
        return new Coordinates(23.34, 32.03);
    }

    // Input format
    // 1948  11   10.8     4.6    ---     44.0    ---
    // 1948  12    8.8     3.8    ---     63.0    ---
    // 1949   1    8.5     1.8       9    23.0    ---
    // 1949   2   10.4     0.6      11    27.0    ---
    // 1957   2    9.0     2.9       5    69.8    64.9
    private Measure parseMeasure(String station, String s) {
        String[] l = s.trim().split("\\s+");
        // Clean line from estimated data marks (*)
        for (int i = 0; i < l.length; i++) {
            l[i] = l[i]
                    .replace('*', ' ')
                    .replace('#', ' ')
                    .replace('$', ' ')
                    .trim();
        }
        Integer year, month;
        Double tMax, tMin;
        Integer afDays;
        Double rain, sunHours;
        year = Integer.parseInt(l[0]);
        month = Integer.parseInt(l[1]);
        tMax = l[2].equals("---") ? null : Double.parseDouble(l[2]);
        tMin = l[3].equals("---") ? null : Double.parseDouble(l[3]);
        afDays = l[4].equals("---") ? null : Integer.parseInt(l[4]);
        rain = l[5].equals("---") ? null : Double.parseDouble(l[5]);
        sunHours = l[6].equals("---") ? null : Double.parseDouble(l[6]);

        Measure m = new Measure(
                station,
                new Date(year.intValue()-1900, month.intValue(), 1),
                tMin,
                tMax,
                afDays,
                rain,
                sunHours
        );
        return m;
    }

}
