/*******************************************************************************
 * Copyright 2012, 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
		getResponse().setAccessControlAllowOrigin("*");
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
		List<Word> tokens = Splitting.tokenize(MorphoServer.getAnalyzer(), query);
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