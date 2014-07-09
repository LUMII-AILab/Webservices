package lv.semti.morphology.webservice;

import java.io.IOException;
import java.io.PrintWriter;
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
		String language = (String) getRequest().getAttributes().get("language");
		
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
				for (Wordform wf : token) {
					if ("EN".equalsIgnoreCase(language))
						wordJSON.add(MorphoServer.tagset.toEnglish(wf).toJSON());
					else
						wordJSON.add(wf.toJSON());
				}
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
		showAttrs.add(AttributeNames.i_Word); showAttrs.add(AttributeNames.i_PartOfSpeech); 
		//nouns
		showAttrs.add(AttributeNames.i_Case); showAttrs.add(AttributeNames.i_Number); showAttrs.add(AttributeNames.i_Gender); showAttrs.add(AttributeNames.i_Declension);
		//verbs/particibles
		showAttrs.add(AttributeNames.i_Person); showAttrs.add(AttributeNames.i_Izteiksme); showAttrs.add(AttributeNames.i_Laiks);
		//adjectives
		showAttrs.add(AttributeNames.i_Degree);
		
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		LinkedList<List<Wordform>> processedTokens = new LinkedList<List<Wordform>>();
		
		for (Word word : tokens) {
			List<Wordform> formas;
			if (paradigmID == null) { // no specific options passed
				// checking for special case of common-gender nouns of 4th/5th declension - 'paziņa', 'auša', 'bende'
				boolean male_4th = false;
				boolean female_4th = false;
				boolean male_5th = false;
				boolean female_5th = false;
				for (Wordform analysis_case : word.wordforms) {
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "7")) female_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "8")) male_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "9")) female_5th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "10")) male_5th = true;
				}
				//word.describe(new PrintWriter(System.out));
				//System.out.printf("Inflectresource: vārds %s.  male4:%b  female4:%b   male5:%b   female5:%b\n", word.getToken(), male_4th, female_4th, male_5th, female_5th);
				if (male_4th && female_4th) { // if so, then build both inflections and merge their forms.
					formas = MorphoServer.analyzer.generateInflections(word.getToken(), 7);
					formas.addAll(MorphoServer.analyzer.generateInflections(word.getToken(), 8));
				} else if (male_5th && female_5th) { 
					formas = MorphoServer.analyzer.generateInflections(word.getToken(), 9);
					formas.addAll(MorphoServer.analyzer.generateInflections(word.getToken(), 10));
				} else 
					formas = MorphoServer.analyzer.generateInflections(word.getToken()); // normal case of building just from the token
				
			} else formas = MorphoServer.analyzer.generateInflections(word.getToken(), paradigmID); // if a specific paradigm is passed, inflect according to that
				
			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
				/* capitalization - because this report was used for proper names at one point
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
