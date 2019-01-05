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

import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import lv.lumii.expressions.Expression;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import org.json.JSONArray;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NERPeopleResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		List<CoreLabel> out = NERTaggerResource.nertag(query);

        JSONArray people = new JSONArray();

        String accumulator = "";
		for (CoreLabel word : out) {
			String token = word.get(TextAnnotation.class);
			if (token.equalsIgnoreCase("<p/>")) continue;
			String tag = word.get(AnswerAnnotation.class);
			if (tag == null) System.err.println("tag ir null");
			if (tag.length()<2) tag = "";
            if (tag.equalsIgnoreCase("person")) {
                accumulator += " ";
                accumulator += token;
            }
            if (tag.isEmpty() && !accumulator.isEmpty()) {
                Expression name = new Expression(accumulator.trim(), "person", false);
                String pamatforma = name.normalize();
                people.put(pamatforma);
                accumulator = "";
            }
		}
        if (!accumulator.isEmpty()) {
            Expression name = new Expression(accumulator.trim(), "person", false);
            String pamatforma = name.normalize();
            people.put(pamatforma);
        }

		return people.toString();
		
	}

}