package lv.semti.morphology.webservice;


import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class EmbeddingsResource extends ServerResource {

    @Get("json")
	public String retrieve() {
        getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
        if (query != null)
            try {
                query = URLDecoder.decode(query,"UTF8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else query = "";
        this.redirectPermanent("http://194.8.1.230:7070/#/query/" + query);
        return null;
	}

}