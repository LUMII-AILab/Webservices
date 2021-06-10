package lv.semti.morphology.corpus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Blacklist {
    private static HashMap<String, List<String>> blacklist_singleton = null;
    private static HashMap<String, List<String>> getBlacklist() {
        if (blacklist_singleton == null) createBlacklist();
        return blacklist_singleton;
    }

    private static void createBlacklist() {
        blacklist_singleton = new HashMap<>();
        blacklist_singleton.put("dervišs", new LinkedList<String>(){{add("Eroglu");}} );
    }

    public static boolean is_blacklisted(String sentence, String lemma) {
        List<String> bad_sentences = getBlacklist().get(lemma);
        if (bad_sentences != null) {
            for (String bad_sentence : bad_sentences) {
                if (sentence.contains(bad_sentence)) return true;
            }
        }
        return false;
    }

}
