package lv.semti.morphology.webservice;

import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TranscriptionConversionResource extends ServerResource {
	@Get
	public String retrieve() {
		if (CentralServer.debug)
			System.out.println(getRequest().getMethod().getName() + " call handled by service " + this.getClass().getName());
		getResponse().setAccessControlAllowOrigin("*");
		String type = (String) getRequest().getAttributes().get("type");
		String query = (String) getRequest().getAttributes().get("query");
		String result = null;
		if (type == null || type.isEmpty() || query == null || query.isEmpty()) {
			// 404 for unknown
			if (CentralServer.debug && (type == null || type.isEmpty()))
				System.out.println("Unkonwn result type recieved, ipa or lfa accepted.");
			if (CentralServer.debug && (query == null || query.isEmpty()))
				System.out.println("Empty query recieved.");
			doError(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		} else if (type.equalsIgnoreCase("ipa"))
			result = CentralServer.getTranscriber().lvmfaToIpa(query);
		else if (type.equalsIgnoreCase("lfa"))
			result = CentralServer.getTranscriber().lvmfaToLfa(query);

		if (result == null || result.isEmpty()) {
			// 404 for unknown
			doError(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}
		return result;
	}
}
