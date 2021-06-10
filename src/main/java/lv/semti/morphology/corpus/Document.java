package lv.semti.morphology.corpus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pet on 2016-01-07.
 */
public class Document {
    private List<Sentence> sentences = new LinkedList<>();
    public Map<String, String> metadata = new HashMap<>();
    public Multimap<String, Token> index; // FIXME - vajag visam korpusam kopēju indeksu, tas būs ātrāk un efektīvāk; tad arī var katram vārdam atstāt top x piemērus un pārējo korpusu neturēt atmiņā

    private static final int optimal_sentence_length = 10;
    public List<Example> findExamples(String lemma) {
        List<Example> result = new LinkedList<>();
        Set<String> already_seen = new HashSet<>();
        for (Token token : index.get(lemma)) {
            String sentence_text = token.sentence.sentence;

            sentence_text = sentence_text.replaceAll(" ([.,?!\"])", "$1"); // FIXME - ja būtu korekti ievaddati ar <g/> tagiem, tad šo nevajadzētu
            if (already_seen.contains(sentence_text)) continue;
            if (Blacklist.is_blacklisted(sentence_text, lemma)) continue;
            result.add(new Example(sentence_text, this, Math.abs(token.sentence.tokens.size()-optimal_sentence_length) ));
            already_seen.add(sentence_text);
        }
        return result;
    }

    private class Sentence {
        String sentence;
        List<Token> tokens;
        Sentence() {
            sentence = "";
            tokens = new LinkedList<>();
        }
        boolean isEmpty() {
            return tokens.isEmpty();
        }

        public void add(Token token, boolean needspace) {
            tokens.add(token);
            token.sentence = this;
            if (needspace) sentence = sentence + ' ';
            sentence = sentence + token.token;
        }
    }

    private class Token {
        String token;
        String tag;
        String lemma;
        Sentence sentence;
        Token (String line) throws IOException {
            String[] parts = line.split("\t");
            if (parts.length != 3) // token tag lemma   lowercase_token
                throw new IOException(String.format("Bad corpus file format - line '%s'",line));
            token = parts[0];
            tag = parts[1];
            lemma = parts[2];
        }
    }

    public Document(String header, List<String> lines) throws IOException {
        parseHeader(header);

        Sentence current_sentence = new Sentence();
        index = ArrayListMultimap.create();
        boolean needspace = false;
        for (String line : lines) {
            if (line.startsWith("</s") && !current_sentence.isEmpty()) { // end of sentence
                sentences.add(current_sentence);
                current_sentence = new Sentence();
            }
            // te ignorē paragrāfus un <g />
            if (line.startsWith("<g />")) {
                needspace = false;
            }
            if (line.startsWith("<")) continue;
            Token token = new Token(line);
            current_sentence.add(token, needspace);
            index.put(token.lemma, token);
            needspace = true; //by default, we'll need a space after this token
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
            // p = Pattern.compile( key + "=\"([^\"]*)\"");   for the old format with double quotes
            p = Pattern.compile( key + "='([^\']*)'");
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

    private static String[] fields = {"id", "reference", "section", "title", "source","author", "authorgender", "published", "genre", "keywords", "fileref"};
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
