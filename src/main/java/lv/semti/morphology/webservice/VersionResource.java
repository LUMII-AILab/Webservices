package lv.semti.morphology.webservice;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class VersionResource extends ServerResource {
	@Get
	public Representation retrieve() {
		return new StringRepresentation(MorphoServer.getAnalyzer().getRevision(), MediaType.TEXT_HTML);
	}

}

