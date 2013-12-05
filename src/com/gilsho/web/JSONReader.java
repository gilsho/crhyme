package com.gilsho.web;

import org.json.JSONException;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 11/28/13
 * Time: 1:53 AM
 * To change this template use File | Settings | File Templates.
 */

public class JSONReader {

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        String jsonText = "{}";
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            jsonText = readAll(rd);
        } finally {
            is.close();
            return jsonText;
        }
    }

}
