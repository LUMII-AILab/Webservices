package lv.semti.morphology.webservice;

import org.restlet.*;
import org.restlet.data.*;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

import lv.lumii.expressions.Expression;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.TagSet;
import lv.ailab.lnb.fraktur.Transliterator;

public class MorphoServer {
	static Analyzer analyzer;
	static Transliterator translit;
	static TagSet tagset;
	static AbstractSequenceClassifier<CoreLabel> NERclassifier;
	static AbstractSequenceClassifier<CoreLabel> morphoClassifier;
	static private boolean enableTransliterator = false;
	static private int port = 8182;

	public static void main(String[] args) throws Exception {
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-transliterator")) { 
				enableTransliterator = true;
				System.out.println("Transliteration services enabled");
			}
			if (args[i].equalsIgnoreCase("-port")) {
				if (i+1 < args.length && !args[i+1].startsWith("-")) {
					try {
						port = Integer.parseInt(args[i+1]);
						i++;
					} catch (Exception e) {
						System.err.printf("Error when parsing command line parameter '%s %s'\n",args[i], args[i+1]);
						System.err.println(e.getMessage());
						System.exit(64); //EX_USAGE flag according to sysexit.h 'standard'
					}
				}
			}
			
			if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-?")) {
				System.out.println("Webservice for LV morphological analysis&inflection, and morphological tagger");
				System.out.println("\nCommand line options:");
				System.out.println("\t-transliterator : enable webservice for historical text transliteration (NB! the extra dictionary files and language models need to be included)");
				System.out.println("\nWebservice access:");
				System.out.println("http://localhost:8182/analyze/[word] : morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
				System.out.println("http://localhost:8182/tokenize/[query] or POST to http://localhost:8182/tokenize : tokenization of sentences");
				System.out.println("http://localhost:8182/verbs/[query] and http://localhost:8182/neverbs/[query] : Support webservice for 'verbs' valency annotation tool - possible inflections of wordform");
				System.out.println("http://localhost:8182/normalize/[ruleset]/[word] and http://localhost:8182/explain/[query] : (if enabled) historical word transliteration and dictionary explanations");
				System.out.println("http://localhost:8182/inflect/json/[query] : generate all inflectional forms of a lemma");
				System.out.println("http://localhost:8182/inflect_people/json/[query]?gender=[m/f] : generate all inflectional forms of words, assuming that they are person names");
				System.out.println("http://localhost:8182/inflect_phrase/[phrase]?category=[person/org/loc] : try to inflect a multiword expression / named entity, given its category");
				System.out.println("http://localhost:8182/morphotagger/[query] : do statistical morphological disambiguation of a sentence");
				System.out.flush();
				System.exit(0);
			}
		}
		
		
		analyzer = new Analyzer("dist/Lexicon.xml", false); 
		analyzer.setCacheSize(1000);
		
		tagset = TagSet.getTagSet();
		
		//NERclassifier = CRFClassifier.getClassifierNoExceptions("dist/models/lv-ner-model.ser.gz");
		//NERclassifier.flags.props.setProperty("gazette", "./Gazetteer/LV_LOC_GAZETTEER.txt,./Gazetteer/LV_PERS_GAZETTEER.txt,./Gazetteer/PP_Onomastica_surnames.txt,./Gazetteer/PP_Onomastica_geonames.txt,./Gazetteer/PP_valstis.txt,./Gazetteer/PP_orgnames.txt,./Gazetteer/PP_org_elements.txt");
		//NERclassifier.featureFactory.init(NERclassifier.flags);
		
		LVMorphologyReaderAndWriter.setPreloadedAnalyzer(analyzer); // Lai nelādētu vēlreiz lieki
		String morphoClassifierLocation = "dist/models/lv-morpho-model.ser.gz";
		morphoClassifier = CMMClassifier.getClassifier(morphoClassifierLocation);
		
		Expression.setClassifier(morphoClassifier);
		
		// Create a new Restlet component and add a HTTP server connector to it 
	    Component component = new Component();  
	    component.getServers().add(Protocol.HTTP, port);  
	    
	    // Then attach it to the local host 
	    component.getDefaultHost().attach("/analyze/{word}", WordResource.class);
	    component.getDefaultHost().attach("/analyze/{language}/{word}", WordResource.class);
	    component.getDefaultHost().attach("/tokenize/{query}", TokenResource.class);
	    component.getDefaultHost().attach("/tokenize", TokenResource.class);
	    component.getDefaultHost().attach("/verbi/{query}", VerbResource.class); //obsolete, jaaiznjem
	    component.getDefaultHost().attach("/verbs/{query}", VerbResource.class);
	    component.getDefaultHost().attach("/neverbs/{query}", NonVerbResource.class);
	    if (enableTransliterator) {
			Transliterator.PATH_FILE = "dist/path.conf";
			translit = Transliterator.getTransliterator(analyzer);			
		    component.getDefaultHost().attach("/explain/{word}", DictionaryResource.class);
		    component.getDefaultHost().attach("/normalize/{ruleset}/{word}", TransliterationResource.class);
	    }
	    component.getDefaultHost().attach("/inflect/{format}/{query}", InflectResource.class);
	    component.getDefaultHost().attach("/inflect/{format}/{language}/{query}", InflectResource.class);
	    component.getDefaultHost().attach("/inflect_people/{format}/{query}", InflectPeopleResource.class);
	    component.getDefaultHost().attach("/inflect_phrase/{phrase}", InflectPhraseResource.class);
	    component.getDefaultHost().attach("/normalize_phrase/{phrase}", NormalizePhraseResource.class);
	    component.getDefaultHost().attach("/nertagger/{query}", NERTaggerResource.class);
	    component.getDefaultHost().attach("/morphotagger/{query}", MorphoTaggerResource.class);
	    
	    component.getDefaultHost().attach("/phonetic_transcriber/{phrase}", PhoneticTranscriberResource.class);
	    
	    // Now, let's start the component! 
	    // Note that the HTTP server connector is also automatically started. 
	    component.start();  
		System.out.println("Usage sample for entity inflection:\nhttp://localhost:8182/inflect_phrase/Vaira Vīķe-Freiberga?category=person");
	}

}
