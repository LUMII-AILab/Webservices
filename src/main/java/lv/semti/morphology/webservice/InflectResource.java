package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import lv.semti.morphology.lexicon.StemType;
import lv.semti.morphology.webservice.utils.AttributeFilter;
import lv.semti.morphology.webservice.utils.JsonOutput;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * General inflection service: provide a lemma (and optionally paradigm and
 * stems) and get all forms.
 * TODO: split in two – general one providing all inflection variants and paradigm/stem-based for tezaurs.
 */
public class InflectResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		if (CentralServer.debug)
			System.out.println(getRequest().getMethod().getName() + " call handled by service " + this.getClass().getName());
        getResponse().setAccessControlAllowOrigin("*");
		Boolean latgalian = CentralServer.isTypeLatgalian((String) getRequest().getAttributes().get("type"));
		if (latgalian == null)
		{
			doError(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		String query = (String) getRequest().getAttributes().get("query");
		String language = (String) getRequest().getAttributes().get("language");
		boolean guess = "true".equalsIgnoreCase(getQuery().getValues("guess"));
		if (CentralServer.debug)
			System.out.println("Latgalian: " + latgalian + ", guessing: " + guess + ", English: " + "EN".equalsIgnoreCase(language));

		List<Collection<Wordform>> processedTokens = inflect(query, guess, latgalian);
		return JsonOutput.toJsonDoubleGeneric(processedTokens, language, false);
	}

    public List<Collection<Wordform>> inflect(String query, boolean guess, boolean latgalian) {
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		Analyzer analyzer = latgalian
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();

		analyzer.enableGuessing = guess;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = false;
		analyzer.guessInflexibleNouns = guess;
		analyzer.enableAllGuesses = guess;

		/*
		 * In 2026-03-20 a workaround is added to avoid tokenizer in case when
		 * the paradigm is known. Further investigation needed on wheather the
		 * tokenization is ever usefull at all in this place.
		 * Lauma.
		 */
		Word wholeWord = new Word(query);
		List<Word> tokens = Splitting.tokenize(analyzer, query);

		LinkedList<Collection<Wordform>> processedTokens = new LinkedList<>();
		
		for (Word word : tokens) {
			List<Wordform> formas;

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
//				word.describe(new PrintWriter(System.out));
//				System.out.printf("Inflectresource: vārds %s.  male4:%b  female4:%b   male5:%b   female5:%b\n", word.getToken(), male_4th, female_4th, male_5th, female_5th);
			if (male_4th && female_4th) { // if so, then build both inflections and merge their forms.
				formas = analyzer.generateInflectionsFromParadigm(word.getToken(), 7);
				formas.addAll(analyzer.generateInflectionsFromParadigm(word.getToken(), 8));
			} else if (male_5th && female_5th) {
				formas = analyzer.generateInflectionsFromParadigm(word.getToken(), 9);
				formas.addAll(analyzer.generateInflectionsFromParadigm(word.getToken(), 10));
			} else
				formas = analyzer.generateInflections(word.getToken()); // normal case of building just from the token

			for (Wordform wf : formas) {
				wf.filterAttributes(AttributeFilter.showableAttributes);
				wf.addAttribute(StemType.FORM_STEM.descriptionLV, wf.lexeme.getStem(wf.getEnding().stemType));
				wf.addAttribute(StemType.STEM1.descriptionLV, wf.lexeme.getStem(StemType.STEM1));
				wf.lexeme = null; // so that identical forms would compare as equal
			}

			processedTokens.add(new LinkedHashSet<>(formas));
		}

		CentralServer.defaultAnalyzersSettings();
		return processedTokens;
	}

}
