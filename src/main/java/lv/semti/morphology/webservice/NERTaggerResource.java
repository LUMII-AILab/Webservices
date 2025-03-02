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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;

public class NERTaggerResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        List<CoreLabel> out = this.nertag(query);
        StringBuilder output = new StringBuilder();
        String prevtag = "";
		for (CoreLabel word : out) {
			String token = word.get(TextAnnotation.class);
			if (token.equalsIgnoreCase("<p/>")) continue;
			String tag = word.get(AnswerAnnotation.class);
			if (tag == null) System.err.println("tag ir null");
			if (tag.length()<2) tag = "";
			
			if (output.length() > 0) output.append(" ");
			
			if (!tag.equalsIgnoreCase(prevtag)) {
				if (!prevtag.equalsIgnoreCase("")) {
					output.append("</");
					output.append(prevtag);
					output.append(">");
				}
				if (!tag.equalsIgnoreCase("")) {
					output.append("<");
					output.append(tag);
					output.append(">");
				}				
			}
			output.append(token);
			prevtag = tag;
		}
		if (!prevtag.equalsIgnoreCase("")) {
			output.append("<\\");
			output.append(prevtag);
			output.append(">");
		}
				
		return output.toString();
		
	}

	static synchronized List<CoreLabel> nertag(String query) {
		Analyzer analyzer = MorphoServer.getAnalyzer();
        analyzer.enableGuessing = true;
        analyzer.enableVocative = false;
        analyzer.guessVerbs = true;
        analyzer.guessParticiples = true;
        analyzer.guessAdjectives = true;
        analyzer.guessInflexibleNouns = true;
        analyzer.enableAllGuesses = true;

        List<CoreLabel> document = new ArrayList<CoreLabel>();

        for (Word token : Splitting.tokenize(analyzer, query)) {
            CoreLabel word = new CoreLabel();
            Wordform maxwf = token.getBestWordform();

            word.set(TextAnnotation.class, token.getToken());
            word.set(LemmaAnnotation.class, maxwf.getValue(AttributeNames.i_Lemma));
            word.set(PartOfSpeechAnnotation.class, maxwf.getTag().substring(0, 1));
            document.add(word);
        }
        CoreLabel p = new CoreLabel();
        p.set(TextAnnotation.class, "<p/>");
        p.set(LemmaAnnotation.class, "<p/>");
        p.set(PartOfSpeechAnnotation.class, "-");
        document.add(p);

        analyzer.defaultSettings();
        return MorphoServer.NERclassifier.classify(document);
    }
	

}