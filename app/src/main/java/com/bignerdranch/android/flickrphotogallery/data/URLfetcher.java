package com.bignerdranch.android.flickrphotogallery.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLfetcher {
    private URLfetcher() {
    }

    public static byte[] getURLBytes(String URLspec) throws IOException {
        URL url = new URL(URLspec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             InputStream in = connection.getInputStream()) {
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage()
                        + ": with " + URLspec);
            }



            int bytesRead;


            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);

            }

            return out.toByteArray();

        } finally {
            connection.disconnect();
        }


    }

    public static String getURLString(String URLspec) throws IOException {
        return new String(getURLBytes(URLspec));
    }
}