package uk.ac.ed.inf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This class is used for connecting and requesting data from the webserver.
 */
public class WebServerClient {

    private static final String DEFAULT_MACHINE_NAME = "localhost";
    private static final String DEFAULT_PORT = "80";

    public static final HttpClient httpClient = HttpClient.newHttpClient();

    private static String machineName = DEFAULT_MACHINE_NAME;
    private static String port = DEFAULT_PORT;

    /**
     * This method sets the static parameters machineName and port so that any class can access the webserver.
     *
     * @param machineName The name of the machine which the server is running on.
     * @param port The port which the server is running on.
     */
    public static void setupWebServerClient(String machineName, String port) {
        WebServerClient.machineName = machineName;
        WebServerClient.port = port;
    }

    /**
     * This method is used to make a request to our webserver and return it to a class. If a request fails then the
     * system will exit due to this being a fatal error.
     *
     * @param fileLocation The location of the target file on the server.
     * @return The body of the response to the request. Note null is the default response but will never be returned
     *         as the system will exit if the request fails.
     */
    public static String request(String fileLocation) {
        String urlString = getUrlString(fileLocation);

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
     * This method combines a machine name, port and file location into a url to be used for a request.
     *
     * @param fileLocation The location of the file on the server.
     * @return A string of a url to be used for a request.
     */
    private static String getUrlString(String fileLocation) {
        return String.format("http://%s:%s/%s", machineName, port, fileLocation);
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
}