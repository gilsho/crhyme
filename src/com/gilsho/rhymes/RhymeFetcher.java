package com.gilsho.rhymes;

import com.gilsho.web.JSONReader;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 11/28/13
 * Time: 2:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class RhymeFetcher {

   private static Runtime runtime = Runtime.getRuntime();

   private static String lastToken(String word) {
       return word;
   }

    private static String stripNonAlpha(String token) {
        String str = "";
        int i=0;
        while (i<token.length()) {
            if (Character.isLetter(token.charAt(i)))
                break;
            i++;
        }

        while(i<token.length()) {
            if (Character.isLetter(token.charAt(i)) || token.charAt(i) == '-')
                str = str + token.charAt(i);
            else
                break;
            i++;
        }

        /* Make sure token has only one consecutive stream of letters */
        while (i<token.length()) {
            if (Character.isLetter(i)) {
                return null;
            }
            i++;
        }

        return str;
    }

    /* either token starts with a number, or it might have a comma at the end */
//    private static String stripComma(String str) {
//        if (!Character.isLetter(str.charAt(0)))
//            return null;
//        if (str.charAt(str.length()-2) == ',')
//            return str.substring(0,str.length()-1);
//        return str;
//    }

    private static List<String> extractAllWords(String str)  {
        List<String> list = new ArrayList<String>();
        StringTokenizer strtok = new StringTokenizer(str);
        while (strtok.hasMoreTokens()) {
            String tok = stripNonAlpha(strtok.nextToken().toLowerCase());
            if (tok != null && !tok.equals("")) {
                list.add(tok);
            }
        }
        return list;
    }

    public static RhymeList getRhymes(String word) {
        RhymeList rhymeList = new RhymeList();
        try {
            Process p = runtime.exec("rhyme " + lastToken(word));
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                            p.getInputStream()));
            String line = reader.readLine();
            assert(line.equals("Finding perfect rhymes for " + word + "..."));
            line = reader.readLine();
            while (line != null) {
                for (String candidate : extractAllWords(line))
                    if (!candidate.equals(word))
                        rhymeList.add(candidate);
                line = reader.readLine();
            }


        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException e) {
            System.err.println(e);
        } finally {
            return rhymeList;
        }

    }

    public static RhymeList getRhymeWeb(String word) {
        RhymeList rhymeList = new RhymeList();
        try {

            URIBuilder uri = new URIBuilder();
            uri.setScheme("http");
            uri.setHost("rhymebrain.com");
            uri.setPath("/talk");
            uri.setParameter("function","getRhymes");
            uri.setParameter("word", word);

            JSONReader jreader = new JSONReader();
            JSONArray jarr = new JSONArray(jreader.readJsonFromUrl(uri.build().toURL().toString()));

            for (int i=0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                String r = jobj.getString("word");
                rhymeList.add(r);
            }

        } catch(Exception e) {
            System.out.println(e);
        } finally {
            return rhymeList;
        }

    }

}
