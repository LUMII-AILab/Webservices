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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
    		oInflections.put(i_case, inflections.get(i_case).replaceAll("'", "''"));
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