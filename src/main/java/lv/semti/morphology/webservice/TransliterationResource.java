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

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.ailab.lnb.fraktur.translit.ResultData;

public class TransliterationResource extends ServerResource {
	@Get("xml")
	public String retrieve() {  
		String ruleset = (String) getRequest().getAttributes().get("ruleset");
		String word = (String) getRequest().getAttributes().get("word");
		
		try {
			word = URLDecoder.decode(word, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String XML = "";

		try {
			if (!MorphoServer.translit.isValidRuleSet(ruleset)) {
				// Fallback
				ruleset = "core";
			}
			
			XML += "<normalization rule_set=\"" + ruleset + "\" apply_opt_rules=\"true\">";
			
			ResultData rd = MorphoServer.translit.processWord(word, ruleset, true);
			if (rd != null) {
				XML += rd.toXML(MorphoServer.translit.getDictIdKey(), 
								MorphoServer.translit.getEntryUrlKey(), 
								MorphoServer.translit.comparator);
			}
			
			XML += "</normalization>";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return XML;
	}
}
