package uk.ac.ed.inf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This class is used for connecting and requesting data from the webserver.
 */
public class WebServerClient {

    public static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * This method is used to make a request to our webserver and return it to a class. If a request fails then the
     * system will exit due to this being a fatal error.
     *
     * @param urlString The target url for the request.
     * @return The body of the response to the request. Note null is the default response but will never be returned
     *         as the system will exit if the request fails.
     */
    public static String request(String urlString) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
            else {
                failedHttpRequestExit("Response Status Code " + response.statusCode());
            }

        }
        catch (Exception e) {
            failedHttpRequestExit(e);
        }

        return null;
    }

    /**
     * If a Http request fails it is a fatal error and thus we cannot recover from so the system will exit.
     *
     * @param message The reason why the Http request failed.
     * @param <T> Generic parameter due to some message being strings and others being exceptions.
     */
    private static <T> void failedHttpRequestExit(T message) {
        System.out.println("Http Request Failed");
        System.out.println(message);
        System.exit(1);
    }

    public static String getUrlString(String machineName, String port, String fileLocation) {
        return String.format("http://%s:%s/%s", machineName, port, fileLocation);
    }
}
