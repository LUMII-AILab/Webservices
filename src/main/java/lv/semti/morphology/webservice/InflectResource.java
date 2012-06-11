package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class InflectResource extends ServerResource {
	@Get("json")
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessParticibles = false;
		MorphoServer.analyzer.guessAdjectives = false;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		
		LinkedList<String> showAttrs = new LinkedList<String>();
		showAttrs.add("Vārds"); showAttrs.add("Locījums"); showAttrs.add("Skaitlis"); showAttrs.add("Dzimte");    
		
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		LinkedList<String> tokenJSON = new LinkedList<String>();
		
		for (Word word : tokens) {
			LinkedList<String> wordJSON = new LinkedList<String>();
			List<Wordform> formas = MorphoServer.analyzer.generateInflections(word.getToken());
			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
				String name = wf.getValue(AttributeNames.i_Word);
				name = name.substring(0, 1).toUpperCase() + name.substring(1,name.length());
				wf.addAttribute(AttributeNames.i_Word, name);
				wordJSON.add(wf.toJSON());
			}
			// word.toJSONsingle(MorphoServer.statistics)
			tokenJSON.add(formatJSON(wordJSON));
		}
		
		MorphoServer.analyzer.defaultSettings();
		
		return formatJSON(tokenJSON);
	}
	
	private String formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += i.next();
			if (i.hasNext()) out += ",\n";
		}
		out += "]";
		return out;
	}
}
