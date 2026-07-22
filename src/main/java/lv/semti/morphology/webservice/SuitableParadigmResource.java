package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.lexicon.Paradigm;
import lv.semti.morphology.webservice.utils.JsonOutput;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Resource for finding all appropriate paradigmas for given lemma.
 */
public class SuitableParadigmResource extends ServerResource {
	@Get("json")
	public String retrieve() {
		if (CentralServer.debug)
			System.out.println(getRequest().getMethod().getName() + " call handled by service" + this.getClass().getName());
		getResponse().setAccessControlAllowOrigin("*");
		Boolean latgalian = CentralServer.isTypeLatgalian((String) getRequest().getAttributes().get("type"));
		if (latgalian == null)
		{
			doError(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		if (CentralServer.debug)
			System.out.println("Latgalian: " + latgalian);

		String query = (String) getRequest().getAttributes().get("lemma");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		Analyzer analyzer = latgalian
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();
		analyzer.guessAllParadigms = true;
		analyzer.enableAllGuesses = true;
		List<Paradigm> paradigms = analyzer.suitableParadigms(query);
		CentralServer.defaultAnalyzersSettings();
		return JsonOutput.toJson(paradigms);
	}

}