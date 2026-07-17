package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import lv.semti.morphology.analyzer.Analyzer;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.lumii.expressions.Expression;

/**
 * Generate "lemma" for nominal phrases.
 * Future work: augument for verbal phrases?
 */
public class NormalizePhraseResource extends ServerResource {
	@Get("text")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("phrase");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		String category = getQuery().getValues("category");

		Analyzer analyzer = CentralServer.getAnalyzer();
		analyzer.enableGuessing = true;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = false;
		analyzer.guessInflexibleNouns = true;
		analyzer.enableAllGuesses = true;
		
    	Expression e = new Expression(query, category, false);
    	String lemma = e.normalize();
    	analyzer.defaultSettings();
		return lemma;
	}
}