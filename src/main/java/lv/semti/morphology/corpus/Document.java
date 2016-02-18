package lv.semti.morphology.corpus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by pet on 2016-01-07.
 */
public class Document {
    private List<List<Token>> sentences = new LinkedList<>();
    public Map<String, String> metadata = new HashMap<>();
    public Multimap<String, Token> index;

    private static final int optimal_sentence_length = 10;
    public List<Example> findExamples(String lemma) {
        List<Example> result = new LinkedList<>();
        for (Token token : index.get(lemma)) {
            String sentence_text =
                    token.sentence
                        .stream()
                        .map(t -> t.token)
                        .collect(Collectors.joining(" "));


            result.add(new Example(sentence_text, this, Math.abs(token.sentence.size()-optimal_sentence_length) ));
        }
        return result;
    }

    private class Token {
        String token;
        String tag;
        String lemma;
        List<Token> sentence;
        Token (String line, List<Token> sentence) throws IOException {
            String[] parts = line.split("\t");
            if (parts.length != 3) // token tag lemma   lowercase_token
                throw new IOException(String.format("Bad corpus file format - line '%s'",line));
            token = parts[0];
            tag = parts[1];
            lemma = parts[2];
            this.sentence = sentence;
        }
    }

    public Document(String header, List<String> lines) throws IOException {
        parseHeader(header);

        List<Token> current_sentence = new LinkedList<>();
        index = ArrayListMultimap.create();
        for (String line : lines) {
            if (line.startsWith("</s") && !current_sentence.isEmpty()) { // end of sentence
                sentences.add(current_sentence);
                current_sentence = new LinkedList<>();
            }
            // te ignorē paragrāfus un <g />
            if (line.startsWith("<")) continue;
            Token token = new Token(line, current_sentence);
            current_sentence.add(token);
            index.put(token.lemma, token);
//            System.out.printf("Liekam indeksā '%s'\n", token.lemma, token.sentence.toString());
        }

    }

    // Singleton list - prepare patterns on first use, then store them
    private static Map<String, Pattern> patterns = new HashMap<>();
    private static Pattern getPattern(String key) {
        Pattern p = patterns.get(key);
        if (p != null) {
            return p;
        } else {
            p = Pattern.compile( key + "=\"([^\"]*)\"");
            patterns.put(key,p);
            return p;
        }
    }
    private static String getValue(String data, String key) {
        Matcher m = getPattern(key).matcher(data);
        if (m.find())
            return m.group(1);
        else return null;
    }

    private static String[] fields = {"title", "source","author", "authorgender", "published", "genre", "keywords", "fileref"};
    private void parseHeader(String header) {
        // Sample header:
        // <doc title="Keita atvainojas. Vai piedos?" source="Diena. Sestdiena" author="Una Meistere" authorgender="siev." published="10/1/2005" genre="Periodika" keywords="Sabiedrība, zvaigžņu kults" fileref="p0001">

        for (String key : fields) {
            String value = getValue(header, key);
            if (value != null)
                metadata.put(key, value);
        }
    }
}
