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
import org.restlet.service.CorsService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

public class MorphoServer {
	static private Analyzer analyzer;
    static synchronized Analyzer getAnalyzer() { return MorphoServer.analyzer; }
    static synchronized void setAnalyzer(Analyzer analyzer) { MorphoServer.analyzer = analyzer; }
    static private Analyzer latgalian_analyzer;
    static synchronized Analyzer getLatgalian_analyzer() { return MorphoServer.latgalian_analyzer; }
    static synchronized void setLatgalian_analyzer(Analyzer analyzer) { MorphoServer.latgalian_analyzer = analyzer; }
	static TagSet tagset;
	static AbstractSequenceClassifier<CoreLabel> morphoClassifier;
    static public boolean enableTagger = true;
	/**
	 * Transcription service is very old.
	 * Planned future work: integrate the new, python based transcription service.
	 */
    static public boolean enableTranscription = false;
    static public boolean enableLatgalian = true;
	static public boolean enableLexiconReloader = false;
	static public Path MORPHO_DUMPER_PATH = Paths.get("../TezaursMorphoDump/");
	static private int port = 8182;

	public static void main(String[] args) throws Exception {
		for (int i=0; i<args.length; i++) {
            if (args[i].equalsIgnoreCase("-tagger")) {
                enableTagger = true;
                System.out.println("Tagger functionality enabled");
            }
			if (args[i].equalsIgnoreCase("-notagger")) {
				enableTagger = false;
				System.out.println("Tagger functionality disabled");
			}
            if (args[i].equalsIgnoreCase("-transcription")) {
                enableTranscription = true;
                System.out.println("Transcription service enabled");
            }
            if (args[i].equalsIgnoreCase("-notranscription")) {
                enableTranscription = false;
                System.out.println("Transcription service disabled");
            }
			if (args[i].equalsIgnoreCase("-lexreloader")) {
				if (Files.exists(MORPHO_DUMPER_PATH)) {
					enableLexiconReloader = true;
					System.out.println("Lexicon reloading service enabled with path " + MORPHO_DUMPER_PATH);
				}
				else {
					System.err.println("Folder '" + MORPHO_DUMPER_PATH + "' not found!");
					enableLexiconReloader = false;
					System.out.println("Lexicon reloading service is turned of because of lacking dumper script.\n");
				}
			}
			if (args[i].equalsIgnoreCase("-nolexreloader")) {
				enableLexiconReloader = false;
				System.out.println("Lexicon reloading service disabled");
			}
			if (args[i].length() >= "-lexreloader=".length()
					&& args[i].substring(0, "-lexreloader=".length()).equalsIgnoreCase("-lexreloader="))
			{
				MORPHO_DUMPER_PATH = Paths.get(args[i].substring("-lexreloader=".length()));
				if (Files.exists(MORPHO_DUMPER_PATH)) {
					enableLexiconReloader = true;
					System.out.println("Lexicon reloading service enabled with path " + MORPHO_DUMPER_PATH);
				}
				else {
					System.err.println("Folder '" + MORPHO_DUMPER_PATH + "' not found!");
					enableLexiconReloader = false;
					System.out.println("Lexicon reloading service is turned of because of lacking dumper script.\n");
				}
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
                System.out.println("\t-tagger & -notagger : enable/disable morphosyntactic tagger functionality to reduce memory usage");
                //System.out.println("\t-transcription & -notranscription : enable/disable phonetic transcription webservice");
                System.out.println("\t-lexreloader[=/path/to/TezaursMorphoDump] & -nolexreloader : enable/disable morphological lexicon reloading helper service (NB! python3, the extra script and DB connections config needs to be provided");
                System.out.println("\t-port 1234 : sets the web server port to some other number than the default 8182");
				System.out.println("\nWebservice access:");
				System.out.println("http://localhost:8182/analyze/[word] : morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
				System.out.println("http://localhost:8182/analyze/en/[word] : morphological analysis of the word, but with the attributes described in english terms");
                System.out.println("http://localhost:8182/analyzesentence/[query] : JSON format of analysis for each token in a sentence for tagger needs");
				System.out.println("http://localhost:8182/tokenize/[query] or POST to http://localhost:8182/tokenize : tokenization of sentences");
				System.out.println("http://localhost:8182/inflect/json/[query] : generate all inflectional forms of a lemma");
				System.out.println("http://localhost:8182/inflect_people/json/[query]?gender=[m/f] : generate all inflectional forms of words, assuming that they are person names");
				System.out.println("http://localhost:8182/inflect_phrase/[phrase]?category=[person/org/loc] : try to inflect a multiword expression / named entity, given its category");
                System.out.println("http://localhost:8182/suitable_paradigm/[lemma] : provides a sorted lists of paradigms that may form the provided lemma");
				System.out.println("http://localhost:8182/morphotagger/[query] : do statistical morphological disambiguation of a sentence");
				System.out.flush();
				System.exit(0);
			}
		}

        initResources();
        initComponents();

        System.out.println("Ready!");
//		System.out.println("Usage sample for entity inflection:\nhttp://localhost:8182/inflect_phrase/Vaira Vīķe-Freiberga?category=person");
	}

    private static void initComponents() throws Exception {
        // Create a new Restlet component and add an HTTP server connector to it
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, port);

        // Then attach it to the local host
        component.getDefaultHost().attach("/", RootResource.class);
        component.getDefaultHost().attach("/{tail}", RootResource.class);

        component.getDefaultHost().attach("/version", VersionResource.class);
		if (enableLexiconReloader) {
			Reloader.TEZAURS_DUMP_PATH = MORPHO_DUMPER_PATH;
			component.getDefaultHost().attach("/reload_lexicon/{lexicon}", ReloadLexiconResource.class);
			component.getDefaultHost().attach("/reload_lexicon/{lexicon}/{wait}", ReloadLexiconResource.class);
		}
        component.getDefaultHost().attach("/analyze/{word}", WordResource.class);
        component.getDefaultHost().attach("/analyze/{language}/{word}", WordResource.class);
        component.getDefaultHost().attach("/tokenize/{query}", TokenResource.class);
        component.getDefaultHost().attach("/tokenize", TokenResource.class);
        component.getDefaultHost().attach("/suitable_paradigm/{lemma}", SuitableParadigmResource.class);
        component.getDefaultHost().attach("/inflect/{format}/{query}", InflectResource.class);
        component.getDefaultHost().attach("/v1/inflections/{query}", InflectResource.class); // pārtaisīt homonīmiem
        component.getDefaultHost().attach("/inflect/{format}/{language}/{query}", InflectResource.class);

        if (enableTagger) {
            component.getDefaultHost().attach("/morphotagger/{query}", MorphoTaggerResource.class);
            component.getDefaultHost().attach("/morphotagger/{format}/{query}", MorphoTaggerResource.class);

            component.getDefaultHost().attach("/inflect_people/{format}/{query}", InflectPeopleResource.class);
            component.getDefaultHost().attach("/inflect_phrase/{phrase}", InflectPhraseResource.class);
            component.getDefaultHost().attach("/normalize_phrase/{phrase}", NormalizePhraseResource.class);
        }
        if (enableTranscription) { // aizstāt ar viestura risinājumu
            component.getDefaultHost().attach("/phonetic_transcriber/{phrase}", PhoneticTranscriberResource.class);
            component.getDefaultHost().attach("/v1/transcriptions/{phrase}", PhoneticTranscriberResource.class);
        }

        // Set up CORS
        CorsService corsService = new CorsService();
        corsService.setAllowingAllRequestedHeaders(true);
        corsService.setAllowedOrigins(new HashSet<>(List.of("*")));
        corsService.setAllowedCredentials(true);
        Application application = new Application();
        application.getServices().add(corsService);
        component.getDefaultHost().attachDefault(application);
        component.start();
    }

    public static void initResources() throws Exception {
        analyzer = new Analyzer(false);
        analyzer.setCacheSize(1000);

        if (enableLatgalian) {
            latgalian_analyzer = new Analyzer("Latgalian.xml", false);
            latgalian_analyzer.setCacheSize(100);
        }

        tagset = TagSet.getTagSet();

        if (enableTagger) {
            LVMorphologyReaderAndWriter.setPreloadedAnalyzer(analyzer); // Lai nelādētu vēlreiz lieki
            String morphoClassifierLocation = "models/lv-morpho-model.ser.gz";
            morphoClassifier = CMMClassifier.getClassifier(morphoClassifierLocation);
            Expression.setClassifier(morphoClassifier);
        }
    }

}
