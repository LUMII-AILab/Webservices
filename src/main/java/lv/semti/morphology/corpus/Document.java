package lv.semti.morphology.corpus;

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
    private List<Token> tokens; // TODO - vajag dalīt teikumos
    public Map<String, String> metadata = new HashMap<>();

    public List<Example> findExamples(String lemma) {
        List<Example> result = new LinkedList<>();
        int i = 0;
        for (Token token : tokens) {
            if (token.lemma.equals(lemma)) {
                String surroundings =
                        tokens.subList(Math.max(i-5,0), Math.min(i+5, tokens.size()))
                            .stream()
                            .map(t -> t.token)
                            .collect(Collectors.joining(" "));

                result.add(new Example(surroundings, this));
            }
            i++;
        }
        return result;
    }

    private class Token {
        String token;
        String tag;
        String lemma;
        Token (String line) throws IOException {
            String[] parts = line.split("\t");
            if (parts.length != 4) // token tag lemma   lowercase_token
                throw new IOException(String.format("Bad corpus file format - line '%s'",line));
            token = parts[0];
            lemma = parts[1];
            tag = parts[2];
        }
    }

    public Document(String header, List<String> lines) throws IOException {
        parseHeader(header);

        tokens = new LinkedList<>();
        for (String line : lines) {
            // TODO - te ignorē paragrāfus un <g />
            if (line.startsWith("<")) continue;
            tokens.add(new Token(line));
        }
        tokens = new ArrayList(tokens);
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
