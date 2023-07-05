/*******************************************************************************
 * Copyright 2012, 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.webservice;

import lv.lumii.ner.NerPipe;
import lv.semti.morphology.corpus.TaggedCorpus;
import org.restlet.*;
import org.restlet.data.*;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;
import lv.lumii.expressions.Expression;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.TagSet;
import lv.ailab.domainnames.AlternativeBuilder;
import lv.ailab.lnb.fraktur.Transliterator;
import org.restlet.service.CorsService;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

public class MorphoServer {
	static Analyzer analyzer;
    static Analyzer latgalian_analyzer;
	static Transliterator translit;
	static TagSet tagset;
//	static AbstractSequenceClassifier<CoreLabel> NERclassifier;
    static NerPipe NERclassifier;
	static AbstractSequenceClassifier<CoreLabel> morphoClassifier;
	static public AlternativeBuilder alternatives = null;
	static public TaggedCorpus corpus;
	static public boolean enableTransliterator = false;
    static public boolean enableDomeniims = false;
    static public boolean enableTezaurs = false;
    static public boolean enableCorpus = false;
    static public boolean enableTagger = true;
    static public boolean enableNERTagger = false;
    static public boolean enableTranscription = false;
    static public boolean enableLatgalian = true;
	static private int port = 8182;

	public static void main(String[] args) throws Exception {
		for (int i=0; i<args.length; i++) {
			if (args[i].equalsIgnoreCase("-transliterator")) { 
				enableTransliterator = true;
				System.out.println("Transliteration services enabled");
			}
            if (args[i].equalsIgnoreCase("-notransliterator")) {
                enableTransliterator = false;
                System.out.println("Transliteration services disabled");
            }
            if (args[i].equalsIgnoreCase("-domeniims")) {
                enableDomeniims = true;
                System.out.println("Domain name alternative generator enabled");
            }
            if (args[i].equalsIgnoreCase("-nodomeniims")) {
                enableDomeniims = true;
                System.out.println("Domain name alternative generator disabled");
            }
            if (args[i].equalsIgnoreCase("-tezaurs")) {
                enableTezaurs = true;
                System.out.println("Tezaurs.lv data enabled");
            }
            if (args[i].equalsIgnoreCase("-notezaurs")) {
                enableTezaurs = true;
                System.out.println("Tezaurs.lv data disabled");
            }
            if (args[i].equalsIgnoreCase("-tagger")) {
                enableTagger = false;
                System.out.println("Tagger functionality enabled");
            }
			if (args[i].equalsIgnoreCase("-notagger")) {
				enableTagger = false;
				System.out.println("Tagger functionality disabled");
			}
            if (args[i].equalsIgnoreCase("-corpus")) {
                enableTagger = false;
                System.out.println("Corpus example data enabled");
            }
            if (args[i].equalsIgnoreCase("-nocorpus")) {
                enableTagger = false;
                System.out.println("Corpus example data disabled");
            }
            if (args[i].equalsIgnoreCase("-nertagger")) {
                enableNERTagger = true;
                System.out.println("NER tagger enabled");
            }
            if (args[i].equalsIgnoreCase("-nonertagger")) {
                enableNERTagger = true;
                System.out.println("NER tagger disabled");
            }
            if (args[i].equalsIgnoreCase("-transcription")) {
                enableNERTagger = true;
                System.out.println("Transcription service enabled");
            }
            if (args[i].equalsIgnoreCase("-notranscription")) {
                enableNERTagger = true;
                System.out.println("Transcription service disabled");
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
				System.out.println("\t-transliterator & -notransliterator : enable/disable webservice for historical text transliteration (NB! the extra dictionary files and language models need to be included)");
                System.out.println("\t-domeniims & -nodomeniims : enable/disable webservice for domain name alternative generation service (NB! the extra word2vec model files need to be included)");
                System.out.println("\t-tezaurs & -notezaurs : enable/disable webservice for supplementary tezaurs.lv data (NB! the extra json files need to be included)");
                System.out.println("\t-nertagger & -nonertagger : enable/disable NER tagger webservice");
                System.out.println("\t-tagger &-notagger : enable/disable morphosyntactic tagger functionality to reduce memory usage");
                System.out.println("\t-corpus & -nocorpus : enable/disable corpus example functionality to reduce memory usage");
                System.out.println("\t-transcription & -notranscription : enable/disable phonetic transcription webservice");
                System.out.println("\t-port 1234 : sets the web server port to some other number than the default 8182");
				System.out.println("\nWebservice access:");
				System.out.println("http://localhost:8182/analyze/[word] : morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
				System.out.println("http://localhost:8182/analyze/en/[word] : morphological analysis of the word, but with the attributes described in english terms");
                System.out.println("http://localhost:8182/analyzesentence/[query] : JSON format of analysis for each token in a sentence for tagger needs");
				System.out.println("http://localhost:8182/tokenize/[query] or POST to http://localhost:8182/tokenize : tokenization of sentences");
				System.out.println("http://localhost:8182/verbs/[query] and http://localhost:8182/neverbs/[query] : Support webservice for 'verbs' valency annotation tool - possible inflections of wordform");
				System.out.println("http://localhost:8182/normalize/[ruleset]/[word] and http://localhost:8182/explain/[query] : (if enabled) historical word transliteration and dictionary explanations");
				System.out.println("http://localhost:8182/inflect/json/[query] : generate all inflectional forms of a lemma");
				System.out.println("http://localhost:8182/inflect_people/json/[query]?gender=[m/f] : generate all inflectional forms of words, assuming that they are person names");
				System.out.println("http://localhost:8182/inflect_phrase/[phrase]?category=[person/org/loc] : try to inflect a multiword expression / named entity, given its category");
                System.out.println("http://localhost:8182/suitable_paradigm/[lemma] : provides a sorted lists of paradigms that may form the provided lemma");
				System.out.println("http://localhost:8182/morphotagger/[query] : do statistical morphological disambiguation of a sentence");
                System.out.println("http://localhost:8182/domenims/[query] and http://localhost:8182/segment/[query] : (if enabled) domain name word2vec alternative genarator and segmentation");
                System.out.println("http://localhost:8182/corpusexample/[query] : provides a list of corpus mentions of the queried word");
                System.out.println("http://localhost:8182/words/[query] : (if enabled) provides a json representation of the queried tezaurs.lv entry");
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
        // Create a new Restlet component and add a HTTP server connector to it
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, port);

        // Then attach it to the local host
        component.getDefaultHost().attach("/", RootResource.class);

        component.getDefaultHost().attach("/version", VersionResource.class);
        component.getDefaultHost().attach("/analyze/{word}", WordResource.class);
        component.getDefaultHost().attach("/analyze/{language}/{word}", WordResource.class);
        component.getDefaultHost().attach("/tokenize/{query}", TokenResource.class);
        component.getDefaultHost().attach("/tokenize", TokenResource.class);
        component.getDefaultHost().attach("/verbs/{query}", VerbResource.class);
        component.getDefaultHost().attach("/neverbs/{query}", NonVerbResource.class);
        component.getDefaultHost().attach("/suitable_paradigm/{lemma}", SuitableParadigmResource.class);
        component.getDefaultHost().attach("/analyzesentence/{query}", MorphoAnalysisResource.class);
        if (enableTransliterator) {
            Transliterator.PATH_FILE = "path.conf";
            translit = Transliterator.getTransliterator(analyzer);
            component.getDefaultHost().attach("/explain/{word}", DictionaryResource.class);
            component.getDefaultHost().attach("/normalize/{ruleset}/{word}", TransliterationResource.class);
        }
        component.getDefaultHost().attach("/inflect/{format}/{query}", InflectResource.class);
        component.getDefaultHost().attach("/v1/inflections/{query}", InflectResource.class);
        component.getDefaultHost().attach("/inflect/{format}/{language}/{query}", InflectResource.class);

        if (enableTagger) {
            component.getDefaultHost().attach("/morphotagger/{query}", MorphoTaggerResource.class);
            component.getDefaultHost().attach("/morphotagger/{format}/{query}", MorphoTaggerResource.class);

            component.getDefaultHost().attach("/inflect_people/{format}/{query}", InflectPeopleResource.class);
            component.getDefaultHost().attach("/inflect_phrase/{phrase}", InflectPhraseResource.class);
            component.getDefaultHost().attach("/normalize_phrase/{phrase}", NormalizePhraseResource.class);
        }

        if (enableNERTagger) {
            component.getDefaultHost().attach("/nertagger/{query}", NERTaggerResource.class);
            component.getDefaultHost().attach("/nerpeople/{query}", NERPeopleResource.class);
        }

        if (enableTranscription) {
            component.getDefaultHost().attach("/phonetic_transcriber/{phrase}", PhoneticTranscriberResource.class);
            component.getDefaultHost().attach("/v1/transcriptions/{phrase}", PhoneticTranscriberResource.class);
        }

        if (enableDomeniims) {
            if (enableTagger) {
                component.getDefaultHost().attach("/domenims/{domainname}", DomainNameResource.class);
                component.getDefaultHost().attach("/segment/{domainname}", SegmentResource.class);
            } else {
                System.err.println("Domain name alternative service will not work without tagger functionality");
            }
        }

        if (enableCorpus) {
            component.getDefaultHost().attach("/corpusexample/{query}", CorpusResource.class);
            component.getDefaultHost().attach("/v1/examples/{query}", CorpusResource.class);
        }

        component.getDefaultHost().attach("/v1/pronunciation/{query}", PronunciationResource.class);
        component.getDefaultHost().attach("/v1/pronunciations/{query}", PronunciationResource.class);

        component.getDefaultHost().attach("/v1/embeddings", EmbeddingsResource.class);
        component.getDefaultHost().attach("/v1/embeddings/{query}", EmbeddingsResource.class);

        if (enableTezaurs) {
            System.err.println("Šis endpoint lietoja vecā tēzaura datus, lai dotu tos kā JSON; tagad kad viss ir normālā datubāzē citā struktūrā, tad tas būtu jātaisa savādāk");
                    // FIXME ^^^ - bet šis API pat varbūt nebūtu jāliek šeit, tas būtu no Mikus koda
            TezaursWordResource.getEntries();
            component.getDefaultHost().attach("/v1/words", TezaursWordResource.class);
            component.getDefaultHost().attach("/v1/words/{query}", TezaursWordResource.class);
        }

        // Set up CORS
        CorsService corsService = new CorsService();
        corsService.setAllowingAllRequestedHeaders(true);
        corsService.setAllowedOrigins(new HashSet(Arrays.asList("*")));
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

        if (enableNERTagger) {
            Properties props = new Properties();
            InputStream stream = MorphoServer.class.getClassLoader().getResourceAsStream("lv-ner-tagger.prop");
            props.load(stream);
            NERclassifier = new NerPipe(props);
//            NERclassifier = CRFClassifier.getClassifierNoExceptions("models/lv-ner-model.ser.gz");
//            NERclassifier.flags.props.setProperty("gazette", "./Gazetteer/LV_LOC_GAZETTEER.txt,./Gazetteer/LV_PERS_GAZETTEER.txt,./Gazetteer/PP_Onomastica_surnames.txt,./Gazetteer/PP_Onomastica_geonames.txt,./Gazetteer/PP_valstis.txt,./Gazetteer/PP_orgnames.txt,./Gazetteer/PP_org_elements.txt");
//            NERclassifier.featureFactory.init(NERclassifier.flags);
        }

        if (enableTagger) {
            LVMorphologyReaderAndWriter.setPreloadedAnalyzer(analyzer); // Lai nelādētu vēlreiz lieki
            String morphoClassifierLocation = "models/lv-morpho-model.ser.gz";
            morphoClassifier = CMMClassifier.getClassifier(morphoClassifierLocation);
            Expression.setClassifier(morphoClassifier);
        }

        if (enableDomeniims) {
            // Word embeddings and segmentation data
            String WORDLIST_FILE_LV = "wordlist-filtered-lv.txt";
            String WORDLIST_FILE_EN = "wordsEn-sil-filtered.txt";
            //String EMBEDDINGS_LV_FILENAME = "lv_visaslemmas.out";
            String EMBEDDINGS_LV_FILENAME = "polyglot_lv.out";
            String EMBEDDINGS_EN_FILENAME = "polyglot_en.out";
            String SYNONYMS_FILENAME = "sinonimi.txt";
            String BLACKLIST_FILENAME = "blacklist.txt";
            String[][] lexiconFiles = {{WORDLIST_FILE_LV, "lv"}, {WORDLIST_FILE_EN, "en"}};
            alternatives = new AlternativeBuilder(lexiconFiles, true, true, EMBEDDINGS_LV_FILENAME, EMBEDDINGS_EN_FILENAME, SYNONYMS_FILENAME, BLACKLIST_FILENAME);
        }

        if (enableCorpus) {
            // Corpus to find usage examples
            corpus = new TaggedCorpus("corpora/lvk_201809042224.vert");
        }
    }

}
