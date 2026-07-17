package lv.semti.morphology.webservice;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lv.semti.morphology.webservice.utils.Output;
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

		return analyze(query, language);
	}
	
	@Post("json")
	public String postquery(JsonRepresentation entity) throws JSONException {

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
		return analyze(query, language);
	}

	private String analyze(String query, String language) {
		List<Word> tokens = Splitting.tokenize(CentralServer.getAnalyzer(), query);
		return Output.toJson(tokens, language, true);
	}

}