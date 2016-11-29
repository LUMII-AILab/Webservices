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

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class InflectResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");
		String language = (String) getRequest().getAttributes().get("language");
		
		List<Collection<Wordform>> processedtokens = inflect(query, getQuery().getValues("paradigm"), getQuery().getValues("guess"),
                getQuery().getValues("stem1"), getQuery().getValues("stem2"), getQuery().getValues("stem3"));
		
		Utils.allowCORS(this);
				
		String format = (String) getRequest().getAttributes().get("format");
		if (format == null) format = "json";
		if (format.equalsIgnoreCase("xml")) {
			StringWriter s = new StringWriter();					
			try {
				s.write("<Elements>\n");
				for (Collection<Wordform> token : processedtokens) {
					s.write("<Locījumi>\n");
					for (Wordform wf : token) wf.toXML(s);	
					s.write("</Locījumi>\n");
				}		
				s.write("</Elements>\n");
			} catch (IOException e) { e.printStackTrace(); }
			return s.toString();
		} else {
			List<String> tokenJSON = new LinkedList<String>();
			for (Collection<Wordform> token : processedtokens) {
				List<String> wordJSON = new LinkedList<String>();
				for (Wordform wf : token) {
					if ("EN".equalsIgnoreCase(language))
						wordJSON.add(MorphoServer.tagset.toEnglish(wf).toJSON());
					else
						wordJSON.add(wf.toJSON());
				}
				tokenJSON.add(formatJSON(wordJSON));
			}		
			return formatJSON(tokenJSON);			
		}
	}

	private List<Collection<Wordform>> inflect(String query, String paradigm, String guess_param, String stem1, String stem2, String stem3) {
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Integer paradigmID = null;
		if (paradigm != null) paradigmID = Integer.decode(paradigm);

		boolean guess = true;
		if ("false".equalsIgnoreCase(guess_param)) {
			guess = false;
		}

		MorphoServer.analyzer.enableGuessing = guess;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessParticiples = false;
		MorphoServer.analyzer.guessAdjectives = false;
		MorphoServer.analyzer.guessInflexibleNouns = guess;
		MorphoServer.analyzer.enableAllGuesses = guess;

		LinkedList<String> showAttrs = new LinkedList<String>();
		//FIXME - this set is not appropriate for inflecting verbs and others... 
		showAttrs.add(AttributeNames.i_Word); showAttrs.add(AttributeNames.i_PartOfSpeech); 
		//nouns
		showAttrs.add(AttributeNames.i_Case); showAttrs.add(AttributeNames.i_Number); showAttrs.add(AttributeNames.i_Gender); showAttrs.add(AttributeNames.i_Declension);
		//verbs/particibles
		showAttrs.add(AttributeNames.i_Person); showAttrs.add(AttributeNames.i_Izteiksme); showAttrs.add(AttributeNames.i_Laiks); showAttrs.add(AttributeNames.i_Voice); showAttrs.add(AttributeNames.i_Konjugaacija);
		//adjectives
		showAttrs.add(AttributeNames.i_Degree); showAttrs.add(AttributeNames.i_Definiteness);
		
		List<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		LinkedList<Collection<Wordform>> processedTokens = new LinkedList<>();
		
		for (Word word : tokens) {
			List<Wordform> formas;

			if (paradigmID == null) { // no specific options passed
				// checking for special case of common-gender nouns of 4th/5th declension - 'paziņa', 'auša', 'bende'
				boolean male_4th = false;
				boolean female_4th = false;
				boolean male_5th = false;
				boolean female_5th = false;
				for (Wordform analysis_case : word.wordforms) {
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "7")) female_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "8")) male_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "9")) female_5th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "10")) male_5th = true;
				}
//				word.describe(new PrintWriter(System.out));
//				System.out.printf("Inflectresource: vārds %s.  male4:%b  female4:%b   male5:%b   female5:%b\n", word.getToken(), male_4th, female_4th, male_5th, female_5th);
				if (male_4th && female_4th) { // if so, then build both inflections and merge their forms.
					formas = MorphoServer.analyzer.generateInflections(word.getToken(), 7);
					formas.addAll(MorphoServer.analyzer.generateInflections(word.getToken(), 8));
				} else if (male_5th && female_5th) { 
					formas = MorphoServer.analyzer.generateInflections(word.getToken(), 9);
					formas.addAll(MorphoServer.analyzer.generateInflections(word.getToken(), 10));
				} else 
					formas = MorphoServer.analyzer.generateInflections(word.getToken()); // normal case of building just from the token

			} else {
                if ((paradigmID == 15 || paradigmID == 18) && stem1 != null && stem2 != null && stem3 != null) {
                    // For 1st conjugation verbs, if all three stems are passed, then try to use them for inflection
                    formas = multistem_generate(word.getToken(), paradigmID, stem1, stem2, stem3);
                } else {
                    // if a specific paradigm is passed, inflect according to that
                    formas = MorphoServer.analyzer.generateInflections(word.getToken(), paradigmID);
                }
            }

			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
                wf.lexeme = null; // so that identical forms would compare as equal
			}
			processedTokens.add(new LinkedHashSet<>(formas));
		}
		
		MorphoServer.analyzer.defaultSettings();
		return processedTokens;
	}

    private List<Wordform> multistem_generate(String token, Integer paradigmID, String stem1, String stem2, String stem3) {
        if (stem2.contains(",")) {
            List<Wordform> formas = new LinkedList<>();
            for (String stem : stem2.split(",")) {
                formas.addAll(multistem_generate(token, paradigmID, stem1, stem, stem3));
            }
            return formas;
        }
        if (stem3.contains(",")) {
            List<Wordform> formas = new LinkedList<>();
            for (String stem : stem3.split(",")) {
                formas.addAll(multistem_generate(token, paradigmID, stem1, stem2, stem));
            }
            return formas;
        }
        return MorphoServer.analyzer.generateInflections(token, paradigmID, stem1, stem2, stem3);
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
