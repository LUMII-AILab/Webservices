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

import org.json.simple.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.lumii.expressions.Expression;

public class NormalizePhraseResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("phrase");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String category = getQuery().getValues("category");
		
		MorphoServer.analyzer.enableGuessing = true;
		MorphoServer.analyzer.enableVocative = true;
		MorphoServer.analyzer.guessVerbs = false;
		MorphoServer.analyzer.guessParticiples = false;
		MorphoServer.analyzer.guessAdjectives = false;
		MorphoServer.analyzer.guessInflexibleNouns = true;
		MorphoServer.analyzer.enableAllGuesses = true;
		
    	Expression e = new Expression(query, category, false);
    	//e.describe(new PrintWriter(System.err)); // ko tad tageris šim ir sadomājis
    	String pamatforma = e.normalize();		
    	MorphoServer.analyzer.defaultSettings();
		return pamatforma;		
	}
}