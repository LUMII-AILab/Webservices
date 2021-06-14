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
        blacklist_singleton.put("aloties", new LinkedList<String>(){{add("Aloj");}} );
        blacklist_singleton.put("cērps", new LinkedList<String>(){{add("mēs cērpam katrs");}} );
        blacklist_singleton.put("dervišs", new LinkedList<String>(){{add("Eroglu");}} );
        blacklist_singleton.put("irt", new LinkedList<String>(){{add("irtā pretruna"); add("raugs irtas"); add("tie irtie");}} );
        blacklist_singleton.put("sadars", new LinkedList<String>(){{add("");}} );
        blacklist_singleton.put("trops", new LinkedList<String>(){{add("Inga Tropa");}} );
        blacklist_singleton.put("vēlēt", new LinkedList<String>(){{add("Vēl viens faktors"); add("Vēl vairāk nekā"); add("vēl vairāk nekā"); add("zeme vēl tālu");}} );
        blacklist_singleton.put("vērst", new LinkedList<String>(){{add("spītīgs vērsis");}} );
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
