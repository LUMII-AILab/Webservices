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
import phonetic_character_converter.IPACharacterConverter;
import phonetic_character_converter.PhoneticCharacterConverter;
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
		
		String converter_type = getQuery().getValues("phoneme_set");
		String converter_type_new =  getQuery().getValues("encoding");
        if (converter_type_new != null) converter_type = converter_type_new;

		Utils.allowCORS(this);
		
		//switch can't handle null string
		if(converter_type==null)
			converter_type="";
		
		PhoneticCharacterConverter converter;
		
		switch(converter_type.toLowerCase())
		{
		case "ipa":
			converter=new IPACharacterConverter();
			break;
		default:
			converter=new AlphabeticCharacterConverter();
		}
		
		PhoneticTranscriber transcriber=new PhoneticTranscriber(" ", converter);
		
		try {
			return transcriber.transcribePhrase(phrase);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
