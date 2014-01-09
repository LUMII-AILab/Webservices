package lv.semti.morphology.webservice;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LVMorphologyAnalysis;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

import lv.lumii.expressions.Expression;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;

public class InflectPhraseResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("phrase");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String category = getQuery().getValues("category");
		System.err.println(category);
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessParticiples = false;
		MorphoServer.analyzer.guessAdjectives = false;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		
		MorphoServer.analyzer.describe(new PrintWriter(System.err));
		
		JSONObject oInflections = new JSONObject();
    	Expression e = new Expression(query, category, true); // Pieņemam, ka klients padod pamatformu
    	e.describe(new PrintWriter(System.err)); // ko tad tageris šim ir sadomājis
    	Map<String,String> inflections= e.getInflections();
    	for (String i_case : inflections.keySet()) {
    		oInflections.put(i_case, inflections.get(i_case).replaceAll("'", "''"));
    	}
		
    	MorphoServer.analyzer.defaultSettings();
		return oInflections.toJSONString();		
	}
}