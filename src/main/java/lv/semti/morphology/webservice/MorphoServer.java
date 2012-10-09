package lv.semti.morphology.webservice;

import org.restlet.*;
import org.restlet.data.*;

import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.corpus.Statistics;
import lv.ailab.lnb.fraktur.Transliterator;

public class MorphoServer {
	static Analyzer analyzer;
	static Statistics statistics;
	static Transliterator translit;

	public static void main(String[] args) throws Exception {
		analyzer = new Analyzer("dist/Lexicon.xml", false); 
		analyzer.setCacheSize(1000);
		statistics = new Statistics("dist/Statistics.xml");
		
		Transliterator.PATH_FILE = "dist/path.conf";
		translit = Transliterator.getTransliterator();
		
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
	    
	    // Now, let's start the component! 
	    // Note that the HTTP server connector is also automatically started. 
	    component.start();  
	}

	/*
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext()); 
		
		router.attach("/analyze/{word}", WordResource.class);
		
		return router;
	}*/

}
