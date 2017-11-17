package com.github.a28hacks.driveby;

import com.github.a28hacks.driveby.text.TextUtils;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String[] gerAbbrevs = {" ca.", " bzw.", " bspw.", " etc.", " d.h.", " evtl.",
            " geb.", " ggf.", " Chr.", " allg.", " zz.", "o.Ä.", " z.T." };

    private static final String[] engAbbrevs = {" p. ex."," p.ex.", " e.g."};

    private static final String[] test_extracts_ger = new String[]{
            "Die Universität Bremen (kurz Uni Bremen) ist mit dem Gründungsjahr 1971 eine der jüngeren staatlichen Universitäten Deutschlands und mit ca. 19.200 Studierenden und etwa 2.300 Wissenschaftlerinnen und Wissenschaftlern die größte Hochschule des Landes Bremen.\n" +
            "Die Universität zählt seit Juni 2012 zu den elf deutschen Hochschulen, die im Rahmen der Exzellenzinitiative in der höchstdotierten Förderlinie „Zukunftskonzept“ ausgezeichnet wurden.",
            "Das Bremer Institut für Kanada- und Québec-Studien (BIKQS) ist ein Institut an der Universität Bremen. Es wurde eingerichtet, um Studien zu Kanada inklusive der Provinz Québec zu fördern. Dies geschieht interdisziplinär, also unter Beteiligung von Lehre, Forschung und Studierenden der am Thema beteiligten Disziplinen. Dazu gehören die Romanistik, die Anglistik und Amerikanistik, aber auch die General Studies. Ziel ist es, ein „transversales interdisziplinäres Zertifikatstudium“ Kanada- und Québec-Studien zu entwickeln. Zugleich setzen sich die Institutsmitglieder für eine Intensivierung des wissenschaftlichen Austausches auf der Ebene der Lehrenden, Forscher bzw. Forschungsgruppen sowie der Studierenden und Promovierenden ein. Dies geschieht in der Form von öffentlichkeitswirksamen Präsentationen von Forschungsergebnissen, durch die Begegnung mit kanadischer und Québecer Kultur und die Vermittlung von Kenntnissen über Kanada und Québec in Form von Veranstaltungen und Publikationen. Darüber hinaus wurden Kooperationen mit zwei kanadischen Universitäten in Montréal und Guelph bei Toronto vereinbart; weitere Ziele sind die Einrichtung einer Gastprofessur sowie eines studentischen Austauschprogramms.",
            "Hallo (Test(test) test). Test t te tes."
    };

    private static final String[] test_extracts_eng = new String[]{
            "Lorem ipsum e.g. dolor. Lorem ipsum 19.200 dolor."
    };

    private String[] getTestDataForLocale(Locale locale) {
        switch (locale.getLanguage()) {
            case "de":
                return test_extracts_ger;
            default:
                return test_extracts_eng;
        }
    }

    public static String[] getAbbrevs(Locale locale) {
        switch (locale.getLanguage()) {
            case "de":
                return gerAbbrevs;
            default:
                return engAbbrevs;
        }
    }

    @Test
    public void test_sentence_splitting() throws Exception {
        Locale[] locales = new Locale[]{Locale.GERMAN, Locale.ENGLISH};

        //for all locales
        for(Locale locale : locales) {
            //split every extract into sentences
            for (String s : getTestDataForLocale(locale)) {
                List<String> results = TextUtils.splitSentences(s, locale);
                //check if any of them end with an abbreviation
                for (String result : results) {
                    System.out.println(result);

                    for (String abbrev : getAbbrevs(locale)) {
                        assertFalse(result.endsWith(abbrev));
                    }
                }

                //check if two following sentences end/start with numbers without spaces ("19.200")
                for(int i = 0; i < results.size() - 1; i++) {
                    assertFalse(results.get(i).matches(".*?\\d+\\.$") && results.get(i+1).matches("^\\d*"));
                }
            }
        }
    }

    @Test
    public void test_parenthesis_extraction() {
        for(String s : test_extracts_ger) {
            String result = TextUtils.beautify(s);

            assertFalse(result.contains("(") || result.contains(")") || result.contains("[") || result.contains("]"));
        }
    }
}