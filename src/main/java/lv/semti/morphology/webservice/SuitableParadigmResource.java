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

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Paradigm;
import org.json.simple.JSONValue;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SuitableParadigmResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("lemma");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		MorphoServer.analyzer.guessAllParadigms = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		List<Paradigm> paradigms = MorphoServer.analyzer.suitableParadigms(query);
		MorphoServer.analyzer.defaultSettings();
		return toJSON(paradigms);
	}
	
	private String toJSON(List<Paradigm> paradigms) {
		Iterator<Paradigm> i = paradigms.iterator();
		String out = "[";
		while (i.hasNext()) {
			Paradigm p = i.next();
			out += String.format("{\"ID\":%d, \"Description\":%s}", p.getID(), JSONValue.toJSONString(p.getName()));
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
}