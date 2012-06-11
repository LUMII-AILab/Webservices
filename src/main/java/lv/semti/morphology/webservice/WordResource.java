package lv.semti.morphology.webservice;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.semti.morphology.analyzer.*;

public class WordResource extends ServerResource {
	@Get("json")
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("word");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Word w = MorphoServer.analyzer.analyze(query);
		return w.toJSON();
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