package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;


public class FlightPlanner {

    private final static String LANDMARKS_FILE_LOCATION = "buildings/landmarks.geojson";
    private final static String NO_FLY_ZONES_FILE_LOCATION = "buildings/no-fly-zones.geojson";

    private FeatureCollection landmarks;
    private FeatureCollection noFlyZones;

    public FlightPlanner(String machineName, String port) {
        landmarks = getGeoJsonData(machineName, port, LANDMARKS_FILE_LOCATION);
        noFlyZones = getGeoJsonData(machineName, port, NO_FLY_ZONES_FILE_LOCATION);
    }


    private FeatureCollection getGeoJsonData(String machineName, String port, String fileLocation) {
        String urlString = String.format("http://%s:%s/%s", machineName, port, fileLocation);
        String responseBody = WebServerClient.request(urlString);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseBody);

        return featureCollection;
    }
}
