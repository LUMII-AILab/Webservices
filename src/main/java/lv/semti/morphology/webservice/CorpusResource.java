package lv.semti.morphology.webservice;


import lv.semti.morphology.corpus.Example;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

public class CorpusResource extends ServerResource {
    @Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query,"UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Example> examples = MorphoServer.corpus.findExamples(query);

        String json = "[" +
                examples.stream()
                    .map(Example::toString)
                    .collect(Collectors.joining(", "))
                + "]";
		
		return json;
	}
	

}