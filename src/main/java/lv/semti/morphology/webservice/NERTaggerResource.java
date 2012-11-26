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
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.err.println(query);
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = false;
		MorphoServer.analyzer.guessVerbs = true;
		MorphoServer.analyzer.guessParticibles = true;
		MorphoServer.analyzer.guessAdjectives = true;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;		
		
		List<CoreLabel> document = new ArrayList<CoreLabel>(); 
		
		for (Word token : Splitting.tokenize(MorphoServer.analyzer, query)) {
			CoreLabel word = new CoreLabel();
			Wordform maxwf = token.getBestWordform(MorphoServer.statistics);
			
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
		
		MorphoServer.analyzer.defaultSettings();
		
		StringBuilder output = new StringBuilder();
		
		List<CoreLabel> out = MorphoServer.NERclassifier.classify(document);
		String prevtag = "";
		for (CoreLabel word : out) {
			String token = word.get(TextAnnotation.class);
			if (token.equalsIgnoreCase("<p/>")) continue;
			String tag = word.get(AnswerAnnotation.class);
			System.out.println(token);
			if (tag == null) System.err.println("tag ir null");
			System.out.println(tag);
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
	

}