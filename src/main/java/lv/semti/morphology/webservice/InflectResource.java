package lv.semti.morphology.webservice;

import java.io.IOException;
import java.io.StringWriter;
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
import lv.semti.morphology.attributes.AttributeValues;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class InflectResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");
		
		List<List<Wordform>> processedtokens = inflect(query, getQuery().getValues("paradigm"));
				
		String format = (String) getRequest().getAttributes().get("format");
		if (format.equalsIgnoreCase("xml")) {
			StringWriter s = new StringWriter();					
			try {
				s.write("<Elements>\n");
				for (List<Wordform> token : processedtokens) {
					s.write("<Locījumi>\n");
					for (Wordform wf : token) wf.toXML(s);	
					s.write("</Locījumi>\n");
				}		
				s.write("</Elements>\n");
			} catch (IOException e) { e.printStackTrace(); }
			return s.toString();
		} else {
			List<String> tokenJSON = new LinkedList<String>();
			for (List<Wordform> token : processedtokens) {
				List<String> wordJSON = new LinkedList<String>();
				for (Wordform wf : token) wordJSON.add(wf.toJSON());
				tokenJSON.add(formatJSON(wordJSON));
			}		
			return formatJSON(tokenJSON);			
		}
	}

	private List<List<Wordform>> inflect(String query, String paradigm) {
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Integer paradigmID = null;
		if (paradigm != null) paradigmID = Integer.decode(paradigm);
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessParticiples = false;
		MorphoServer.analyzer.guessAdjectives = false;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		
		LinkedList<String> showAttrs = new LinkedList<String>();
		//FIXME - this set is not appropriate for inflecting verbs and others... 
		showAttrs.add("Vārds"); showAttrs.add("Locījums"); showAttrs.add("Skaitlis"); showAttrs.add("Dzimte"); showAttrs.add("Deklinācija");
		
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		LinkedList<List<Wordform>> processedTokens = new LinkedList<List<Wordform>>();
		
		for (Word word : tokens) {
			List<Wordform> formas;
			if (paradigmID == null) // no specific options passed
				formas = MorphoServer.analyzer.generateInflections(word.getToken());
			else formas = MorphoServer.analyzer.generateInflections(word.getToken(), paradigmID); // if a specific paradigm is passed, inflect according to that
				
			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
				/* capitalization - because it was used for proper names at one point
				String name = wf.getValue(AttributeNames.i_Word);
				name = name.substring(0, 1).toUpperCase() + name.substring(1,name.length());
				wf.addAttribute(AttributeNames.i_Word, name);
				*/
			}
			processedTokens.add(formas);
		}
		
		MorphoServer.analyzer.defaultSettings();
		return processedTokens;
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
