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

import phonetic_character_converter.AlphabeticCharacterConverter;
import phonetic_transcriber.PhoneticTranscriber;

public class PhoneticTranscriberResource extends ServerResource {
	@Get
	public String retrieve() {  
		String phrase = (String) getRequest().getAttributes().get("phrase");
		try {
			phrase = URLDecoder.decode(phrase, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		PhoneticTranscriber transcriber=new PhoneticTranscriber(" ", new AlphabeticCharacterConverter());
		
		try {
			return transcriber.transcribePhrase(phrase);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
