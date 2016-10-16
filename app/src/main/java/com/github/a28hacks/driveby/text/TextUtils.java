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
    private String testInput = "The City Municipality of Bremen (German: Stadtgemeinde Bremen, IPA: " +
            "[\u02c8b\u0281e\u02d0m\u0259n]) is a Hanseatic city in northwestern Germany, which " +
            "belongs to the state Free Hanseatic City of Bremen (also called just \"Bremen\" for short).\n" +
            "As a commercial and industrial city with a major port on the River Weser, Bremen is part " +
            "of the Bremen/Oldenburg Metropolitan Region, with 2.4 million people. Bremen is the second " +
            "most populous city in Northern Germany and eleventh in Germany.\nBremen is a major cultural " +
            "and economic hub in the northern regions of Germany. Bremen is home to dozens of historical " +
            "galleries and museums, ranging from historical sculptures to major art museums, such as the " +
            "\u00dcbersee-Museum Bremen. Bremen has a reputation as a working class city. Along with this, " +
            "Bremen is home to a large number of multinational companies and manufacturing centers. Companies" +
            " headquartered in Bremen include the Hachez chocolate company and Vector Foiltec. Four-time" +
            " German football champions Werder Bremen are also based in the city.\nBremen is some 60 km " +
            "(37 mi) south from the Weser mouth on the North Sea. With Bremerhaven right on the mouth the " +
            "two comprise the state of the Free Hanseatic City of Bremen (official German name: Freie " +
            "Hansestadt Bremen).";

    private static final Pattern parenthesis = Pattern.compile(" \\(([^\\)]+)\\)");

    public static String beautify(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String s = extractParenthesis(input);

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
            String[] sentences = input.split("(?<=\\. )");
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
}
