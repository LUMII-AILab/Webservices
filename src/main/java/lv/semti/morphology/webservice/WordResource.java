package lv.semti.morphology.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeValues;

public class WordResource extends ServerResource {
	@Get("json")
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("word");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String language = (String) getRequest().getAttributes().get("language");
		
		Word w = MorphoServer.analyzer.analyze(query);
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
		Wordform w = MorphoServer.analyzer.analyze(query).wordforms.get(0);
		
		StringWriter s = new StringWriter();
		
		try {
			w.toXML(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return s.toString();
	}
}