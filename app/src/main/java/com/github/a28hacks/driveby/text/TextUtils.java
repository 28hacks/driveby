package com.github.a28hacks.driveby.text;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stefan on 15.10.16.
 */

public class TextUtils {

    private static final String TAG = "TextUtils";

    private static final Pattern parenthesis = Pattern.compile(" \\(([^\\)]+)\\)");
    private static final Pattern brokenSentence = Pattern.compile("((?<=\\. )|(?<=\\.((\\r\\n)|(\\n))))[^\\(]*\\)\\.*");
    private static final String sentenceEnd = "(?<=\\. )|(?<=\\.((\\r\\n)|(\\n)))";

    public static String beautify(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String s = extractParenthesis(input);
               s = killBrokenSentences(s);

        return s;
    }

    public static String cutOutFirstSentence(String input) {
        if (input != null && !input.isEmpty()) {
            return input.substring(input.indexOf(". ") + 2);
        }
        return null;
    }

    public static List<String> splitSentences(String input) {
        Log.e(TAG, "splitSentences: " + input);
        if (input != null && !input.isEmpty()) {
            //match for ". ", ".\n", ".\r\n"
            String[] sentences = input.split(sentenceEnd);
            for(String s : sentences) {
                Log.e(TAG, "splitSentences: " + s);
            }
            if(sentences.length == 0) {
                return Collections.singletonList(input);
            } else {
                return Arrays.asList(sentences);
            }
        }
        return null;
    }

    private static String extractParenthesis(String input) {
        Matcher m = parenthesis.matcher(input);
        return m.replaceAll("");
    }

    private static String killBrokenSentences(String input) {
        Matcher m = brokenSentence.matcher(input);
        return m.replaceAll("");
    }
}
