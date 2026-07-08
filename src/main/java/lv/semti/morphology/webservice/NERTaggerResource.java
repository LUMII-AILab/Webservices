package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;

public class NERTaggerResource extends ServerResource {
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		List<CoreLabel> out = nertag(query);
        StringBuilder output = new StringBuilder();
        String prevtag = "";
		for (CoreLabel word : out) {
			String token = word.get(TextAnnotation.class);
			if (token.equalsIgnoreCase("<p/>")) continue;
			String tag = word.get(AnswerAnnotation.class);
			if (tag == null) System.err.println("tag ir null");
			if (tag == null || tag.length()<2) tag = "";
			
			if (!output.isEmpty()) output.append(" ");
			
			if (!tag.equalsIgnoreCase(prevtag)) {
				if (!prevtag.equalsIgnoreCase("")) {
					output.append("</");
					output.append(prevtag);
					output.append(">");
				}
				if (!tag.equalsIgnoreCase("")) {
					output.append("<");
					output.append(tag);
					output.append(">");
				}				
			}
			output.append(token);
			prevtag = tag;
		}
		if (!prevtag.equalsIgnoreCase("")) {
			output.append("<\\");
			output.append(prevtag);
			output.append(">");
		}
				
		return output.toString();
		
	}

	static synchronized List<CoreLabel> nertag(String query) {
		Analyzer analyzer = MorphoServer.getAnalyzer();
        analyzer.enableGuessing = true;
        analyzer.enableVocative = false;
        analyzer.guessVerbs = true;
        analyzer.guessParticiples = true;
        analyzer.guessAdjectives = true;
        analyzer.guessInflexibleNouns = true;
        analyzer.enableAllGuesses = true;

        List<CoreLabel> document = new ArrayList<CoreLabel>();

        for (Word token : Splitting.tokenize(analyzer, query)) {
            CoreLabel word = new CoreLabel();
            Wordform maxwf = token.getBestWordform();

            word.set(TextAnnotation.class, token.getToken());
            word.set(LemmaAnnotation.class, maxwf.getValue(AttributeNames.i_Lemma));
            word.set(PartOfSpeechAnnotation.class, maxwf.getTag().substring(0, 1));
            document.add(word);
        }
        CoreLabel p = new CoreLabel();
        p.set(TextAnnotation.class, "<p/>");
        p.set(LemmaAnnotation.class, "<p/>");
        p.set(PartOfSpeechAnnotation.class, "-");
        document.add(p);

        analyzer.defaultSettings();
        return MorphoServer.NERclassifier.classify(document);
    }
	

}