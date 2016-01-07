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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class InflectPeopleResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");

		Utils.allowCORS(this);
		
		List<List<Wordform>> processedtokens = inflect(query, getQuery().getValues("gender"));
				
		String format = (String) getRequest().getAttributes().get("format");
		if (format.equalsIgnoreCase("xml")) {
			StringWriter s = new StringWriter();					
			try {
				s.write("<Elements>\n");
				for (List<Wordform> token : processedtokens) {
					s.write("<Locījumi>\n");
					for (Wordform wf : token) wf.toXML(s);	
					s.write("</Locījumi>\n");
				}		
				s.write("</Elements>\n");
			} catch (IOException e) { e.printStackTrace(); }
			return s.toString();
		} else {
			List<String> tokenJSON = new LinkedList<String>();
			for (List<Wordform> token : processedtokens) {
				List<String> wordJSON = new LinkedList<String>();
				for (Wordform wf : token) wordJSON.add(wf.toJSON());
				tokenJSON.add(formatJSON(wordJSON));
			}		
			return formatJSON(tokenJSON);			
		}
	}

	private List<List<Wordform>> inflect(String query, String gender) {
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessAdjectives = true;
		MorphoServer.analyzer.guessParticiples = false;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		
		LinkedList<String> showAttrs = new LinkedList<String>();
		showAttrs.add("Vārds"); showAttrs.add("Locījums"); showAttrs.add("Skaitlis"); showAttrs.add("Dzimte"); showAttrs.add("Deklinācija");
		
		AttributeValues filter = new AttributeValues();
		if (gender != null) {
			if (gender.equalsIgnoreCase("m")) filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);
			if (gender.equalsIgnoreCase("f")) filter.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);
		}
		
		String words = query;
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, words);
		LinkedList<List<Wordform>> processedTokens = new LinkedList<List<Wordform>>();
		
		for (Word word : tokens) {
			List<Wordform> formas = MorphoServer.analyzer.generateInflections(word.getToken(), true, filter);
			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
				String name = wf.getValue(AttributeNames.i_Word);
				name = name.substring(0, 1).toUpperCase() + name.substring(1,name.length());
				wf.addAttribute(AttributeNames.i_Word, name);
			}
			processedTokens.add(formas);
		}
		
		MorphoServer.analyzer.defaultSettings();
		return processedTokens;
	}
	
	private String formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += i.next();
			if (i.hasNext()) out += ",\n";
		}
		out += "]";
		return out;
	}
}
