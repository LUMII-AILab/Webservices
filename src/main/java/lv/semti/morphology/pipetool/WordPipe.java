package lv.semti.morphology.pipetool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.corpus.Statistics;

public class WordPipe {
	public static void main(String[] args) throws Exception {
		Analyzer analyzer = new Analyzer("dist/Lexicon.xml"); 	
		analyzer.enableVocative = true;
		analyzer.enableDiminutive = true;
		analyzer.enablePrefixes = true;
		analyzer.enableGuessing = true;
		analyzer.enableAllGuesses = true;
		analyzer.meklētsalikteņus = true; 
		
		Statistics statistics = new Statistics("dist/Statistics.xml");
		
		boolean full_output = false;
		if (args.length > 0) {
		    full_output = args[0].equalsIgnoreCase("-full");
		}
				
		PrintStream out = new PrintStream(System.out, true, "UTF8");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in, "UTF8"));
	    String s;
	    while ((s = in.readLine()) != null && s.length() != 0) {
	    	out.println( analyze( analyzer, statistics, s, full_output));
	    	out.flush();
	    }
	}	
	
	private static String analyze(Analyzer analyzer, Statistics statistics, String query, boolean all_options) {
		List<Word> tokens = Splitting.tokenize(analyzer, query);
		LinkedList<String> tokenJSON = new LinkedList<String>();
		
		for (Word word : tokens) {
			if (all_options) tokenJSON.add(word.toJSON());
			else tokenJSON.add(word.toJSONsingle(statistics));
		}
		
		String s = formatJSON(tokenJSON);
		tokens = null;
		tokenJSON = null;
		
		return s;
	}
	
	private static String formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += i.next();
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
}	