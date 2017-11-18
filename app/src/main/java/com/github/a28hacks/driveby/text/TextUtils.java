package com.github.a28hacks.driveby.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by stefan on 15.10.16.
 */

public class TextUtils {

    private static final String TAG = "TextUtils";

    private enum BracketPair {
        Round('(',')'), Normal('[',']');

        char open;
        char close;

        BracketPair(char open, char close) {
            this.open = open;
            this.close = close;
        }
    }

    private static BreakIterator iterator;

    public static String beautify(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String s = extractParenthesis(input);

        return s;
    }

    public static List<String> splitSentences(String input, Locale locale) {

        iterator = BreakIterator.getSentenceInstance(locale);

        List<String> sentences = new ArrayList<>();
        if (input != null && !input.isEmpty()) {
            iterator.setText(input);
            int start = iterator.first();
            for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                sentences.add(input.substring(start,end));
            }
        }

        return sentences;
    }

    private static String extractParenthesis(String input) {

        String result = input;

        for(BracketPair pair : BracketPair.values()) {
            //search for last opening bracket
            int start = result.lastIndexOf(pair.open);
            int end;

            while (start != -1) {
                //remove text upto to first closing bracket
                end = result.indexOf(pair.close, start);

                //catch broken brackets
                if(end == -1) {
                    end = result.length();
                }

                result = result.replace(result.substring(start,end+1), "");
                start = result.lastIndexOf(pair.open);
            }
        }

        return result;
    }
}
