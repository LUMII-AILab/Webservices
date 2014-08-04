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
