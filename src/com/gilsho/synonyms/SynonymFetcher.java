package com.gilsho.synonyms;


import com.gilsho.web.JSONReader;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SynonymFetcher {


    public static List<String> getSynonyms(String word) {
        List<String> synList = new ArrayList<String>();

        NounSynset nounSynset;
        NounSynset[] hyponyms;
        WordNetDatabase database = WordNetDatabase.getFileInstance();

        Synset[] synsets = database.getSynsets(word, SynsetType.NOUN);
        for (int i = 0; i < synsets.length; i++) {
            nounSynset = (NounSynset)(synsets[i]);
            for (String candidate : nounSynset.getWordForms()) {
                synList.add(candidate);
            }

            for (NounSynset n : nounSynset.getHyponyms()) {
                for (String candidate : n.getWordForms()) {
                    synList.add(candidate);
                }
            }

            for (NounSynset n : nounSynset.getHypernyms()) {
                for (String candidate : n.getWordForms()) {
                    synList.add(candidate);
                }
            }
        }

        return synList;
    }

    public static List<String> getSynonymsWeb(String word, boolean isNoun) {

        String API_KEY = "55992db54d834acfca1daca69677a69e";

        List<String> synonyms = new ArrayList<String>();
        try {
            URIBuilder uri = new URIBuilder();
            uri.setScheme("http");
            uri.setHost("words.bighugelabs.com");
            uri.setPath("/api/2/" + API_KEY + "/" + word + "/json");

            JSONReader jreader = new JSONReader();
            JSONObject jroot = new JSONObject(jreader.readJsonFromUrl(uri.build().toURL().toString()));

            String postag = isNoun ? "noun" : "verb";

            JSONObject jobj = jroot.getJSONObject(postag);
            JSONArray jarr = jobj.getJSONArray("syn");
            for (int i=0; i<jarr.length(); i++) {
                String s = jarr.getString(i);
                synonyms.add(s);
            }

        } catch(Exception e) {
            System.out.println(e);
        } finally {
            return synonyms;
        }

    }

}
