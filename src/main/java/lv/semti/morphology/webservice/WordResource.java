package lv.semti.morphology.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeValues;

public class WordResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("word");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String language = (String) getRequest().getAttributes().get("language");

		Word w = MorphoServer.getAnalyzer().analyze(query);
		return toJSON(w.wordforms, language);
	}
	
	private String toJSON(ArrayList<Wordform> wordforms, String language) {
		Iterator<Wordform> i = wordforms.iterator();
		String out = "[";
		while (i.hasNext()) {
			AttributeValues av = i.next();
			if ("EN".equalsIgnoreCase(language))
				av = MorphoServer.tagset.toEnglish(av);
			out += av.toJSON();
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}

	//@Get("xml")
	public String retrieveXML() {  
		String query = (String) getRequest().getAttributes().get("word");
		Wordform w = MorphoServer.getAnalyzer().analyze(query).wordforms.getFirst();
		
		StringWriter s = new StringWriter();
		
		try {
			w.toXML(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	}
}