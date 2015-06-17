package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import lv.ailab.domainnames.AlternativeBuilder;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DomainNameResource extends ServerResource{


	@Get("json")
	public String retrieve() throws Exception {  
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		Integer limit = null;
		try {
			String limit_str= getQuery().getValues("limit");
			System.out.println("limits");
			System.out.println(limit_str);
			if (limit_str != null)
				limit = Integer.parseInt(limit_str);
		} catch (Exception e) {
			limit = null;
		}
		
		List<String> alternatives = MorphoServer.alternatives.buildAlternatives(query, limit);

		return AlternativeBuilder.resultToJson(alternatives);
	}

}
