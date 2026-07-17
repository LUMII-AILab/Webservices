package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.webservice.utils.Output;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Service providing wordform analysis.
 */
public class WordformAnalyzeResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("word");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		String language = (String) getRequest().getAttributes().get("language");

		Word w = CentralServer.getAnalyzer().analyze(query);
		return Output.toJson(w, language);
	}
}