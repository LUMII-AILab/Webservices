package lv.semti.morphology.pipetool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lv.ailab.lnb.fraktur.Transliterator;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.corpus.Statistics;

public class WordPipe {
	public static void main(String[] args) throws Exception {
		boolean full_output = false;
		boolean tab_output = false;
		boolean transliterate = false;
		boolean useAux = true;
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-full")) full_output = true;
			if (args[i].equalsIgnoreCase("-tab")) tab_output = true;
			if (args[i].equalsIgnoreCase("-transliterate")) transliterate = true;
			if (args[i].equalsIgnoreCase("-core")) useAux = false;
		}

		Analyzer analyzer;
		if (transliterate) {
			Transliterator.PATH_FILE = "dist/path.conf";
			analyzer = new TransliteratingAnalyzer("dist/Lexicon.xml");
		} else analyzer = new Analyzer("dist/Lexicon.xml", useAux); 	
		
		analyzer.enableVocative = true;
		analyzer.enableDiminutive = true;
		analyzer.enablePrefixes = true;
		analyzer.enableGuessing = true;
		analyzer.enableAllGuesses = true;
		analyzer.meklētsalikteņus = true; 
		analyzer.setCacheSize(10000);
		
		Statistics statistics = new Statistics("dist/Statistics.xml");
					
		PrintStream out = new PrintStream(System.out, true, "UTF8");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
	    String s;
	    while ((s = in.readLine()) != null && s.length() != 0) {
	    	List<Word> tokens = Splitting.tokenize(analyzer, s, false);	    	
	    	
	    	if (!tab_output) 
	    		out.println( analyze( analyzer, statistics, tokens, full_output));
	    	else out.println( analyze_tab( analyzer, statistics, tokens, full_output));
	    	out.flush();
	    }
	}	
	
	private static String analyze(Analyzer analyzer, Statistics statistics, List<Word> tokens, boolean all_options) {		
		LinkedList<String> tokenJSON = new LinkedList<String>();
		
		for (Word word : tokens) {
			if (all_options) tokenJSON.add(word.toJSON());
			else tokenJSON.add(word.toJSONsingle(statistics));
		}
		
		String s = formatJSON(tokenJSON).toString();
		tokens = null;
		tokenJSON = null;
		
		return s;
	}
	
	private static String analyze_tab(Analyzer analyzer, Statistics statistics, List<Word> tokens, boolean all_options){
		StringBuilder s = new StringBuilder(); 
		
		for (Word word : tokens) {
			if (s.length()>0) s.append("\t");
			if (all_options)
				s.append(word.toTabSep());
			else s.append(word.toTabSepsingle(statistics));
		}
		
		tokens = null;
		return s.toString();
	}
	
	private static StringBuilder formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		StringBuilder out = new StringBuilder("[");
		while (i.hasNext()) {
			out.append(i.next());
			if (i.hasNext()) out.append(", ");
		}
		out.append("]");
		return out;
	}
}	