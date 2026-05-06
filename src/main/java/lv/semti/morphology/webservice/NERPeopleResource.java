package lv.semti.morphology.webservice;

import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import lv.lumii.expressions.Expression;
import org.json.JSONArray;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class NERPeopleResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		List<CoreLabel> out = NERTaggerResource.nertag(query);

        JSONArray people = new JSONArray();

        String accumulator = "";
		for (CoreLabel word : out) {
			String token = word.get(TextAnnotation.class);
			if (token.equalsIgnoreCase("<p/>")) continue;
			String tag = word.get(AnswerAnnotation.class);
			if (tag == null) System.err.println("tag ir null");
			if (tag.length()<2) tag = "";
            if (tag.equalsIgnoreCase("person")) {
                accumulator += " ";
                accumulator += token;
            }
            if (tag.isEmpty() && !accumulator.isEmpty()) {
                Expression name = new Expression(accumulator.trim(), "person", false);
                String pamatforma = name.normalize();
                people.put(pamatforma);
                accumulator = "";
            }
		}
        if (!accumulator.isEmpty()) {
            Expression name = new Expression(accumulator.trim(), "person", false);
            String pamatforma = name.normalize();
            people.put(pamatforma);
        }

		return people.toString();
		
	}

}