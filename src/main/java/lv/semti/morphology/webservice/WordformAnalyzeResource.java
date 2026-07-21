package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.webservice.utils.JsonOutput;
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
		boolean latgalian = "ltg".equalsIgnoreCase((String) getRequest().getAttributes().get("type"));

		Analyzer analyzer = latgalian
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();

		Word w = analyzer.analyze(query);
		return JsonOutput.toJson(w, language, true);
	}
}