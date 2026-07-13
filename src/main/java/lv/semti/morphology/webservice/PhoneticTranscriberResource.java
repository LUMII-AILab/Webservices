package lv.semti.morphology.webservice;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import phonetic_character_converter.AlphabeticCharacterConverter;
import phonetic_character_converter.IPACharacterConverter;
import phonetic_character_converter.PhoneticCharacterConverter;
import phonetic_transcriber.PhoneticTranscriber;

public class PhoneticTranscriberResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String phrase = (String) getRequest().getAttributes().get("phrase");
		phrase = URLDecoder.decode(phrase, StandardCharsets.UTF_8);

		String converter_type = getQuery().getValues("phoneme_set");
		String converter_type_new =  getQuery().getValues("encoding");
        if (converter_type_new != null) converter_type = converter_type_new;

		//switch can't handle null string
		if(converter_type==null)
			converter_type="";
		
		PhoneticCharacterConverter converter;

		if (converter_type.equalsIgnoreCase("ipa")) {
			converter = new IPACharacterConverter();
		} else {
			converter = new AlphabeticCharacterConverter();
		}
		
		PhoneticTranscriber transcriber=new PhoneticTranscriber(" ", converter);
		
		try {
			return transcriber.transcribePhrase(phrase);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
