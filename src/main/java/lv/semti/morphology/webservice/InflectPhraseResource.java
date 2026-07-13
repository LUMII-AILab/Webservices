package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lv.semti.morphology.analyzer.Analyzer;
import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.lumii.expressions.Expression;
import lv.lumii.expressions.Expression.Category;
import lv.lumii.expressions.Expression.Gender;
import lv.semti.morphology.attributes.AttributeNames;

public class InflectPhraseResource extends ServerResource {
	@Get
	public synchronized String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("phrase");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String category = getQuery().getValues("category");

		Analyzer analyzer = MorphoServer.getAnalyzer();
		analyzer.enableGuessing = true;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = true;
		analyzer.guessInflexibleNouns = true;
		analyzer.enableAllGuesses = true;

		JSONObject oInflections = new JSONObject();
    	Expression e = new Expression(query, category, true); // Pieņemam, ka klients padod pamatformu
    	//e.describe(new PrintWriter(System.err)); // ko tad tageris šim ir sadomājis
    	Map<String,String> inflections= e.getInflections();
    	for (String i_case : inflections.keySet()) {
    		oInflections.put(i_case, inflections.get(i_case).replace("'", "''"));
    	}
    	if (e.category == Category.hum) {
    		if (e.gender == Gender.masculine)
    			oInflections.put(AttributeNames.i_Gender, AttributeNames.v_Masculine);
    		if (e.gender == Gender.feminine)
    			oInflections.put(AttributeNames.i_Gender, AttributeNames.v_Feminine);
    	}
    				
    	analyzer.defaultSettings();
		return oInflections.toJSONString();		
	}
}