package lv.semti.morphology.webservice;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lv.semti.morphology.webservice.utils.JsonOutput;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import lv.semti.morphology.analyzer.*;

/**
 * Service providing morphological analyzer based tokenization.
 */
public class TokenResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String language = (String) getRequest().getAttributes().get("language");
		boolean latgalian = "ltg".equalsIgnoreCase((String) getRequest().getAttributes().get("type"));
		boolean guess = "true".equalsIgnoreCase(getQuery().getValues("guess"));

		return analyze(query, language, latgalian, guess);
	}
	
	@Post("json")
	public String postquery(JsonRepresentation entity) throws JSONException {
		boolean latgalian = "ltg".equalsIgnoreCase((String) getRequest().getAttributes().get("type"));
		boolean guess = "true".equalsIgnoreCase(getQuery().getValues("guess"));

		JSONObject json;
		String query = null;
		String language = null;
		try {
			json = entity.getJsonObject();
			query = json.getString("query");
			language = json.has("language") ? json.getString("language") : "lv";
		} catch (JSONException e) {
			
			e.printStackTrace();
		}

		System.out.println(query);
		return analyze(query, language, latgalian, guess);
	}

	private String analyze(String query, String language, boolean latgalian, boolean guess) {
		Analyzer analyzer = latgalian
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();

		analyzer.enableGuessing = guess;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = guess;
		analyzer.guessParticiples = guess;
		analyzer.guessAdjectives = guess;
		analyzer.guessInflexibleNouns = guess;
		analyzer.enableAllGuesses = guess;

		List<Word> tokens = Splitting.tokenize(analyzer, query);
		CentralServer.defaultAnalyzersSettings();
		return JsonOutput.toJson(tokens, language, true);
	}

}