package lv.semti.morphology.webservice;

import org.restlet.resource.Get;

public class NonVerbResource extends VerbResource {
	@Get
	public String retrieve() {  
		return parsequery(false);
	}
	
}