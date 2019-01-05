package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class SegmentResource extends ServerResource{
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("domainname");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(MorphoServer.alternatives);
		System.out.println(MorphoServer.alternatives.segmenter);
		System.out.println(MorphoServer.alternatives.segmenter.segment(query));
		System.out.println(MorphoServer.alternatives.segmenter.segment(query).toJSON());
		return MorphoServer.alternatives.segmenter.segment(query).toJSON();
	}

}
