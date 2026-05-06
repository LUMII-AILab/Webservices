package lv.semti.morphology.webservice;

import lv.lumii.morphotagger.MorphoConverter;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import org.json.JSONArray;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;

public class MorphoAnalysisResource extends ServerResource {
	@Get
	public String retrieve() {  
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        getResponse().setAccessControlAllowOrigin("*");

        JSONArray analysis = new JSONArray();
        LinkedList<Word> tokens = Splitting.tokenize(MorphoServer.getAnalyzer(), query);
		for (Word token : tokens) {
            analysis.put(MorphoConverter.formatToken(token, null, null));
        }

		return analysis.toString();
	}

}