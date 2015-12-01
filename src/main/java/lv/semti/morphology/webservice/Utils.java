package lv.semti.morphology.webservice;

import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.Resource;
import org.restlet.util.Series;

public class Utils {
	public static void allowCORS(Resource resource) {
		@SuppressWarnings("unchecked")
		Series<Header> headers = (Series<Header>)resource.getResponseAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (headers==null) {
			headers = new Series<Header>(Header.class);
			resource.getResponseAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
		}
		headers.add(new Header("Access-Control-Allow-Origin", "*"));
	}
}
