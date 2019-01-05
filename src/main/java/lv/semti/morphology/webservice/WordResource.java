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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeValues;

public class WordResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("word");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String language = (String) getRequest().getAttributes().get("language");

		Word w = MorphoServer.analyzer.analyze(query);
		return toJSON(w.wordforms, language);
	}
	
	private String toJSON(ArrayList<Wordform> wordforms, String language) {
		Iterator<Wordform> i = wordforms.iterator();
		String out = "[";
		while (i.hasNext()) {
			AttributeValues av = i.next();
			if ("EN".equalsIgnoreCase(language))
				av = MorphoServer.tagset.toEnglish(av);
			out += av.toJSON();
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}

	//@Get("xml")
	public String retrieveXML() {  
		String query = (String) getRequest().getAttributes().get("word");
		Wordform w = MorphoServer.analyzer.analyze(query).wordforms.get(0);
		
		StringWriter s = new StringWriter();
		
		try {
			w.toXML(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	}
}