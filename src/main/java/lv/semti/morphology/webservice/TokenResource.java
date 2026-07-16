package lv.semti.morphology.webservice;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

		return analyze(query);
	}
	
	@Post("json")
	public String postquery(JsonRepresentation entity) throws JSONException {

		JSONObject json;
		String query = null;
		try {
			json = entity.getJsonObject();
			query = json.getString("query");
		} catch (JSONException e) {
			
			e.printStackTrace();
		}

		System.out.println(query);
		return analyze(query);
	}

	private String analyze(String query) {
		List<Word> tokens = Splitting.tokenize(CentralServer.getAnalyzer(), query);
		LinkedList<String> tokenJSON = new LinkedList<>();
		
		for (Word word : tokens) {
			tokenJSON.add(word.toJSONsingle());
		}
		
		return formatJSON(tokenJSON);
	}
	
	private String formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += i.next();
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
}