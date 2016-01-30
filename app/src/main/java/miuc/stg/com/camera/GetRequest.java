package miuc.stg.com.camera;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by J on 16/11/2015.
 */
@SuppressWarnings("deprecation")
public class GetRequest {

    private static int responseCode = 503;
    private static HttpClient client = new DefaultHttpClient();
    private static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";

    public String sendGet(String url) throws Exception {

        String base = "https://www.google.com/searchbyimage?"; //image_url=http://i64.tinypic.com/2remlnn.jpg&encoded_image=&image_content=&filename=&hl=en
        String image_url = url;
        String encoded_image ="";
        String image_content = "";
        String filename = "";
        String hl = "en";
        url = base + "image_url=" + image_url + "&encoded_image=" + encoded_image + "&image_content=" + image_content + "&filename=" + filename + "&hl=" + hl;

        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());
        responseCode = response.getStatusLine().getStatusCode();

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = new String();
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

    public String getPageContent(String url) throws Exception {

        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());
        responseCode = response.getStatusLine().getStatusCode();

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = new String();
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
}
