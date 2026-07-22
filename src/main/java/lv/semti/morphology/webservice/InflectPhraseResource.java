package lv.semti.morphology.webservice;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.attributes.FixedAttribute;
import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.lumii.expressions.Expression;
import lv.lumii.expressions.Expression.Category;
import lv.lumii.expressions.Expression.Gender;
import lv.semti.morphology.attributes.AttributeNames;

/**
 * Specialized inflection service for phrases: organizations, locations and
 * person phrases like "prezidents Viesturs Priedājs".
 */
public class InflectPhraseResource extends ServerResource {
	@Get("json")
	public synchronized String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("phrase");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String category = getQuery().getValues("category");
		String language = (String) getRequest().getAttributes().get("language");

		Analyzer analyzer = CentralServer.getAnalyzer();
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
			System.err.println(i_case + " " + inflections.get(i_case));
			if ("EN".equalsIgnoreCase(language)) {
				FixedAttribute caseAttr = (FixedAttribute)CentralServer.tagset.getAttribute(AttributeNames.i_Case, AttributeNames.v_Noun, "LV").getFirst();
				oInflections.put(caseAttr.toEnglish(i_case), inflections.get(i_case).replace("'", "''"));
			}
    		else oInflections.put(i_case, inflections.get(i_case).replace("'", "''"));
    	}
    	if (e.category == Category.hum && (e.gender == Gender.feminine || e.gender == Gender.masculine)) {
			String gender = AttributeNames.v_Feminine;
			if (e.gender == Gender.masculine)
				gender = AttributeNames.v_Masculine;
			if ("EN".equalsIgnoreCase(language)) {
				FixedAttribute genderAttr = (FixedAttribute) CentralServer.tagset.getAttribute(AttributeNames.i_Gender, AttributeNames.v_Noun, "LV").getFirst();
				oInflections.put(genderAttr.attributeEN, genderAttr.toEnglish(gender));
			}
			else
				oInflections.put(AttributeNames.i_Gender, gender);
		}

		CentralServer.defaultAnalyzersSettings();
		return oInflections.toJSONString();
	}
}


/*public class InflectPhraseResource extends ServerResource {
	@Get
	public synchronized String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("phrase");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String category = getQuery().getValues("category");

		Analyzer analyzer = CentralServer.getAnalyzer();
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
}*/