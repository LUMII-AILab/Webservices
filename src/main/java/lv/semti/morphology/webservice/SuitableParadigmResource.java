package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.lexicon.Paradigm;
import org.json.simple.JSONValue;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

public class SuitableParadigmResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("lemma");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Analyzer analyzer = MorphoServer.getAnalyzer();
		analyzer.guessAllParadigms = true;
		analyzer.enableAllGuesses = true;
		List<Paradigm> paradigms = analyzer.suitableParadigms(query);
		analyzer.defaultSettings();
		return toJSON(paradigms);
	}
	
	private String toJSON(List<Paradigm> paradigms) {
		Iterator<Paradigm> i = paradigms.iterator();
		String out = "[";
		while (i.hasNext()) {
			Paradigm p = i.next();
			out += String.format("{\"ID\":%d, \"Description\":%s}", p.getID(), JSONValue.toJSONString(p.getName()));
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
}