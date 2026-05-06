package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lv.semti.morphology.analyzer.Analyzer;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.lumii.expressions.Expression;

public class NormalizePhraseResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("phrase");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String category = getQuery().getValues("category");

		Analyzer analyzer = MorphoServer.getAnalyzer();
		analyzer.enableGuessing = true;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = false;
		analyzer.guessInflexibleNouns = true;
		analyzer.enableAllGuesses = true;
		
    	Expression e = new Expression(query, category, false);
    	String pamatforma = e.normalize();
    	analyzer.defaultSettings();
		return pamatforma;		
	}
}