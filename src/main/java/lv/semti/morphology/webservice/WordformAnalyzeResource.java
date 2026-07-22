package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.webservice.utils.JsonOutput;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Service providing wordform analysis.
 * As of 2026-07-21, analyzer is not correctly reacting guessing variables for,
 * analysis side, thus, guesswork currently cannot be properly turned off for
 * this service. But guessed forms can be filtered off by "Minēšana"/"Guesswork"
 * property in JSON result.
 */
public class WordformAnalyzeResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		if (CentralServer.debug)
			System.out.println(getRequest().getMethod().getName() + " call handled by service" + this.getClass().getName());
		getResponse().setAccessControlAllowOrigin("*");
		boolean latgalian = "ltg".equalsIgnoreCase((String) getRequest().getAttributes().get("type"));
		String language = (String) getRequest().getAttributes().get("language");
		String query = (String) getRequest().getAttributes().get("word");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		boolean guess = "true".equalsIgnoreCase(getQuery().getValues("guess"));
		if (CentralServer.debug)
			System.out.println("Latgalian: " + latgalian + ", guessing: " + guess + ", English: " + "EN".equalsIgnoreCase(language));

		Analyzer analyzer = latgalian
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();

		analyzer.enableGuessing = guess;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = guess;
		analyzer.guessParticiples = guess;
		analyzer.guessAdjectives = guess;
		analyzer.guessInflexibleNouns = guess;
		analyzer.enableAllGuesses = guess;

		Word w = analyzer.analyze(query);
		//System.err.println("W ir:" + w);
		//w.describe(System.err);
		String result = JsonOutput.toJson(w, language, true);
		CentralServer.defaultAnalyzersSettings();
		return result;
	}
}