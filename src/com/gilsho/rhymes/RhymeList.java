package com.gilsho.rhymes;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created with IntelliJ IDEA.
 * User: gilsho
 * Date: 11/28/13
 * Time: 3:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class RhymeList extends ArrayList<String> {


    public String findRhyme(List<String> list) {
        for (String s : list) {
            String r = findRhyme(s);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    public String findRhyme(String str) {

        String word = str.toLowerCase();
        ListIterator<String> it = listIterator();
        while (it.hasNext()) {
            String r = it.next().toLowerCase();
            if (r.equals(word)) {
                return r;
            }
        }
        return null;
    }


}
