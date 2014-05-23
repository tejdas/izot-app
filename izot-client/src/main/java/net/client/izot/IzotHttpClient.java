package net.client.izot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;

public class IzotHttpClient {
    public static void main(String[] args) throws IOException {
        String url = "http://10.0.0.28/api/devices/1";
        String userName = "pi";
        String password = "raspberry";

        HttpGet request = new HttpGet(url);
        String auth = String.format("%s:%s", userName, password);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        StringBuilder jsonMessage = new StringBuilder();
        CloseableHttpClient client = HttpClientBuilder.create().build();
        try {
            HttpResponse response = client.execute(request);

            System.out.println("Response Code : " + response.getStatusLine()
                    .getStatusCode());
            System.out.println("Content Type: " + response.getEntity()
                    .getContentType());
            System.out.println("Content Length: " + response.getEntity()
                    .getContentLength());

            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()))) {
                String line = "";
                while ((line = rd.readLine()) != null) {
                    jsonMessage.append(line);
                }
                System.out.println("Message body: " + jsonMessage.toString());
            }
        } finally {
            client.close();
        }

        PubnubPublisher publisher = new PubnubPublisher();
        publisher.initialize();
        try {
            publisher.publish("twenty", jsonMessage.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        publisher.shutdown();
    }
}
