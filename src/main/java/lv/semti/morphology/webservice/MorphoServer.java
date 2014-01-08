package lv.semti.morphology.webservice;

import org.restlet.*;
import org.restlet.data.*;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

import lv.lumii.expressions.Expression;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.corpus.Statistics;
import lv.ailab.lnb.fraktur.Transliterator;

public class MorphoServer {
	static Analyzer analyzer;
	static Transliterator translit;
	static AbstractSequenceClassifier<CoreLabel> NERclassifier;
	static AbstractSequenceClassifier<CoreLabel> morphoClassifier;

	public static void main(String[] args) throws Exception {
		analyzer = new Analyzer("dist/Lexicon.xml", false); 
		analyzer.setCacheSize(1000);
		
		Transliterator.PATH_FILE = "dist/path.conf";
		translit = Transliterator.getTransliterator(analyzer);
		
		//NERclassifier = CRFClassifier.getClassifierNoExceptions("dist/models/lv-ner-model.ser.gz");
		//NERclassifier.flags.props.setProperty("gazette", "./Gazetteer/LV_LOC_GAZETTEER.txt,./Gazetteer/LV_PERS_GAZETTEER.txt,./Gazetteer/PP_Onomastica_surnames.txt,./Gazetteer/PP_Onomastica_geonames.txt,./Gazetteer/PP_valstis.txt,./Gazetteer/PP_orgnames.txt,./Gazetteer/PP_org_elements.txt");
		//NERclassifier.featureFactory.init(NERclassifier.flags);
		
		LVMorphologyReaderAndWriter.setPreloadedAnalyzer(analyzer); // Lai nelādētu vēlreiz lieki
		String morphoClassifierLocation = "dist/models/lv-morpho-model.ser.gz";
		morphoClassifier = CMMClassifier.getClassifier(morphoClassifierLocation);
		
		Expression.setClassifier(morphoClassifier);
		
		// Create a new Restlet component and add a HTTP server connector to it 
	    Component component = new Component();  
	    component.getServers().add(Protocol.HTTP, 8182);  
	    
	    // Then attach it to the local host 
	    component.getDefaultHost().attach("/analyze/{word}", WordResource.class);  
	    component.getDefaultHost().attach("/tokenize/{query}", TokenResource.class);
	    component.getDefaultHost().attach("/tokenize", TokenResource.class);
	    component.getDefaultHost().attach("/verbi/{query}", VerbResource.class); //obsolete, jaaiznjem
	    component.getDefaultHost().attach("/verbs/{query}", VerbResource.class);
	    component.getDefaultHost().attach("/neverbs/{query}", NonVerbResource.class);
	    component.getDefaultHost().attach("/explain/{word}", DictionaryResource.class);
	    component.getDefaultHost().attach("/normalize/{ruleset}/{word}", TransliterationResource.class);
	    component.getDefaultHost().attach("/inflect/{format}/{query}", InflectResource.class);
	    component.getDefaultHost().attach("/inflect_people/{format}/{query}", InflectPeopleResource.class);
	    component.getDefaultHost().attach("/inflect_phrase/{phrase}", InflectPhraseResource.class);
	    component.getDefaultHost().attach("/nertagger/{query}", NERTaggerResource.class);
	    component.getDefaultHost().attach("/morphotagger/{query}", MorphoTaggerResource.class);
	    
	    // Now, let's start the component! 
	    // Note that the HTTP server connector is also automatically started. 
	    component.start();  
	}

}
