package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LVMorphologyAnalysis;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;

public class MorphoTaggerResource extends ServerResource {
	// Kopija no LVTaggera morphopipe koda - FIXME, DRY
	private enum outputTypes {JSON, TAB, VERT, MOSES, CONLL_X, XML, VISL_CG, lemmatizedText}
	
	@Get
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");

		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		LVMorphologyReaderAndWriter.setAnalyzerDefaults();
		List<CoreLabel> sentence = LVMorphologyReaderAndWriter.analyzeSentence(query);
		sentence = MorphoServer.morphoClassifier.classify(sentence); // runs the actual morphotagging system

		String format = (String) getRequest().getAttributes().get("format");	
		outputTypes outputType = outputTypes.VERT;
		if ("json".equalsIgnoreCase(format))
			outputType = outputTypes.JSON;

		String out = output(sentence, outputType);
		MorphoServer.getAnalyzer().defaultSettings(); // FIXME - nesapratu, kāpēc un vai tas ir vajadzīgs
		return out;
	}
	
	private static String output(List<CoreLabel> tokens, outputTypes outputType){
		// konfigurācija tam kodam
		String token_separator = System.lineSeparator();
		String field_separator = "\t";
		boolean mini_tag = false;
		
		StringBuilder s = new StringBuilder();
		
		for (CoreLabel word : tokens) {
			String token = word.getString(TextAnnotation.class);
			if (token.contains("<s>")) continue;
			if (!s.isEmpty()) s.append(token_separator);
			if (outputType == outputTypes.MOSES) token = token.replace(' ', '_');
			s.append(token);
			s.append(field_separator);
			Word analysis = word.get(LVMorphologyAnalysis.class);
			Wordform mainwf = analysis.getMatchingWordform(word.getString(AnswerAnnotation.class), false); 
			if (mainwf != null) {
				if (mini_tag) mainwf.removeNonlexicalAttributes();
				s.append(mainwf.getTag());
				s.append(field_separator);
				String lemma = mainwf.getValue(AttributeNames.i_Lemma);
				if (outputType == outputTypes.MOSES) lemma = lemma.replace(' ', '_');
				s.append(lemma);
			} else s.append(field_separator); 
		}
		
		return s.toString();
	}
}