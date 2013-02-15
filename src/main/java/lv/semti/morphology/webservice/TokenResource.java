package lv.semti.morphology.webservice;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

public class TokenResource extends ServerResource {
	@Get("json")
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query,"UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return analyze(query);
	}
	
	@Post("json")
	public String postquery(JsonRepresentation entity) throws JSONException, IOException {
		//System.out.println(entity.getText());
		
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
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		LinkedList<String> tokenJSON = new LinkedList<String>();
		
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