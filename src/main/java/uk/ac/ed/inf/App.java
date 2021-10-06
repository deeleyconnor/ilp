package uk.ac.ed.inf;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args )
    {
        System.out.println( "System Started!" );
    }

    public static String websiteRequest(String urlString) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();

        try {
            HttpResponse <String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            }
            else {
                System.out.println("Menu Request Failed");
                System.out.println(response.statusCode());
                System.exit(1);
            }

        }
        catch (Exception e) {
            System.out.println("Menus Request Failed");
            System.out.println(e);
            System.exit(1);
        }

        return "";
    }
}