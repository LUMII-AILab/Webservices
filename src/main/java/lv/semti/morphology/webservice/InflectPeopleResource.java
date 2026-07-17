package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;

import lv.semti.morphology.webservice.utils.Output;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Specialized inflection service meant for inflecting human names.
 */
public class InflectPeopleResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		String language = (String) getRequest().getAttributes().get("language");

		List<List<Wordform>> processedTokens = inflect(query, getQuery().getValues("gender"));

		return Output.toJsonGeneric(processedTokens, language);

	}

	private synchronized List<List<Wordform>> inflect(String query, String gender) {
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		Analyzer analyzer = CentralServer.getAnalyzer();
		analyzer.enableGuessing = true;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessAdjectives = true;
		analyzer.guessParticiples = false;
		analyzer.guessInflexibleNouns = true;
		analyzer.enableAllGuesses = true;
		
		LinkedList<String> showAttrs = new LinkedList<>();
		showAttrs.add("Vārds");
		showAttrs.add(AttributeNames.i_Case);
		showAttrs.add(AttributeNames.i_Number);
		showAttrs.add(AttributeNames.i_Gender);
		showAttrs.add(AttributeNames.i_Declension);
		showAttrs.add(AttributeNames.i_PartOfSpeech); // Without part of speech we can't properly transform attribute names to other languages.

		AttributeValues filter = new AttributeValues();
		if (gender != null) {
			if (gender.equalsIgnoreCase("m")) filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
			if (gender.equalsIgnoreCase("f")) filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		}
		
		String words = query;
		List<Word> tokens = Splitting.tokenize(analyzer, words);
		LinkedList<List<Wordform>> processedTokens = new LinkedList<>();
		
		for (Word word : tokens) {
			List<Wordform> forms = analyzer.generateInflections(word.getToken(), true, filter);
			for (Wordform wf : forms) {
				wf.filterAttributes(showAttrs);
				String name = wf.getValue(AttributeNames.i_Word);
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
				wf.addAttribute(AttributeNames.i_Word, name);
			}
			processedTokens.add(forms);
		}
		
		analyzer.defaultSettings();
		return processedTokens;
	}
}
