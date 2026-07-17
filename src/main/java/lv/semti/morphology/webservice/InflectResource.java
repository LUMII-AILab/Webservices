package lv.semti.morphology.webservice;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;

import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Paradigm;
import lv.semti.morphology.lexicon.StemType;
import lv.semti.morphology.webservice.utils.Output;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * General inflection service: provide a lemma (and optionally paradigm and
 * stems) and get all forms.
 * TODO: split in two – general one providing all inflection variants and paradigm/stem-based for tezaurs.
 */
public class InflectResource extends ServerResource {
	@Get("json")
	public String retrieve() {
        getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		String language = (String) getRequest().getAttributes().get("language");

        String inflmisc = getQuery().getValues("inflmisc");
		List<Collection<Wordform>> processedTokens = inflect(query, getQuery().getValues("paradigm"), getQuery().getValues("guess"),
                getQuery().getValues("stem1"), getQuery().getValues("stem2"), getQuery().getValues("stem3"), decodeInflMisc(inflmisc));

		return Output.toJsonGeneric(processedTokens, language);
	}

    private static AttributeValues decodeInflMisc(String inflmisc) {
        AttributeValues lemmaAttrs = new AttributeValues();
        if (inflmisc != null) {
            for (String attr : inflmisc.split(",")) {
                if (attr.equalsIgnoreCase("Vīriešu_dzimte"))
                    lemmaAttrs.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Masculine);

                if (attr.equalsIgnoreCase("Sieviešu_dzimte"))
                    lemmaAttrs.addAttribute(AttributeNames.i_Gender, AttributeNames.v_Feminine);

                if (attr.equalsIgnoreCase("Daudzskaitlis"))
                    lemmaAttrs.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);

				if (attr.equalsIgnoreCase("Vienskaitlis"))
					lemmaAttrs.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum);

				if (attr.equalsIgnoreCase("Noliegums"))
					lemmaAttrs.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
            }
        }
        return lemmaAttrs;
    }

    public List<Collection<Wordform>> inflect(String query, String paradigm_param, String guess_param, String stem1, String stem2, String stem3, AttributeValues lemmaAttrs) {
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);

		Analyzer analyzer = CentralServer.getAnalyzer();
		Paradigm paradigm = null;
		if (paradigm_param != null) {
			try {
				int paradigmID = Integer.decode(paradigm_param);
				paradigm = analyzer.paradigmByID(paradigmID);
			} catch (NumberFormatException e) {
				// Using paradigm names
				if (paradigm_param.endsWith("ltg")) {
					analyzer = CentralServer.getLatgalian_analyzer();
				}
				paradigm = analyzer.paradigmByName(paradigm_param);
			}

			if (paradigm == null) {
				System.err.printf("Could not find paradigm '%s'\n", paradigm_param);
			}
		}

		boolean guess = !"false".equalsIgnoreCase(guess_param);

		analyzer.enableGuessing = guess;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = false;
		analyzer.guessInflexibleNouns = guess;
		analyzer.enableAllGuesses = guess;

		LinkedList<String> showAttrs = new LinkedList<>();
		//FIXME - this set is not appropriate for inflecting verbs and others... 
		showAttrs.add(AttributeNames.i_Word); showAttrs.add(AttributeNames.i_PartOfSpeech); showAttrs.add(AttributeNames.i_Derivative);
		//nouns
		showAttrs.add(AttributeNames.i_Case); showAttrs.add(AttributeNames.i_Number); showAttrs.add(AttributeNames.i_Gender); showAttrs.add(AttributeNames.i_Declension);
		//verbs/participles
		showAttrs.add(AttributeNames.i_Person); showAttrs.add(AttributeNames.i_Mood); showAttrs.add(AttributeNames.i_Tense); showAttrs.add(AttributeNames.i_Voice); showAttrs.add(AttributeNames.i_Konjugaacija); showAttrs.add(AttributeNames.i_Noliegums);
		//adjectives
		showAttrs.add(AttributeNames.i_Degree); showAttrs.add(AttributeNames.i_Definiteness);
		// usage restrictions are necessary for distinguishing which forms to use/show
		showAttrs.add(AttributeNames.i_Frequency); showAttrs.add(AttributeNames.i_Usage); showAttrs.add(AttributeNames.i_Normative);

		/*
		 * In 2026-03-20 a workaround is added to avoid tokenizer in case when
		 * the paradigm is known. Further investigation needed on wheather the
		 * tokenization is ever usefull at all in this place.
		 * Lauma.
		 */
		Word wholeWord = new Word(query);
		List<Word> tokens = paradigm == null
				? Splitting.tokenize(analyzer, query)
				: new ArrayList<>() {{add(wholeWord);}};

		LinkedList<Collection<Wordform>> processedTokens = new LinkedList<>();
		
		for (Word word : tokens) {
			List<Wordform> formas;

			if (paradigm == null) { // no specific options passed or not found
				// checking for special case of common-gender nouns of 4th/5th declension - 'paziņa', 'auša', 'bende'
				boolean male_4th = false;
				boolean female_4th = false;
				boolean male_5th = false;
				boolean female_5th = false;
				for (Wordform analysis_case : word.wordforms) {
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "7")) female_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "8")) male_4th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "9")) female_5th = true;
					if (analysis_case.isMatchingStrong(AttributeNames.i_ParadigmID, "10")) male_5th = true;
				}
//				word.describe(new PrintWriter(System.out));
//				System.out.printf("Inflectresource: vārds %s.  male4:%b  female4:%b   male5:%b   female5:%b\n", word.getToken(), male_4th, female_4th, male_5th, female_5th);
				if (male_4th && female_4th) { // if so, then build both inflections and merge their forms.
					formas = analyzer.generateInflectionsFromParadigm(word.getToken(), 7, lemmaAttrs);
					formas.addAll(analyzer.generateInflectionsFromParadigm(word.getToken(), 8, lemmaAttrs));
				} else if (male_5th && female_5th) { 
					formas = analyzer.generateInflectionsFromParadigm(word.getToken(), 9, lemmaAttrs);
					formas.addAll(analyzer.generateInflectionsFromParadigm(word.getToken(), 10, lemmaAttrs));
				} else 
					formas = analyzer.generateInflections(word.getToken()); // normal case of building just from the token

			} else {
//				if ((paradigm.getStems()>1) && stem1 != null && stem2 != null && stem3 != null) {
//					// For 1st conjugation verbs, if all three stems are passed, then try to use them for inflection
				if ( !paradigm.getName().matches("verb-[23].*") &&
						((stem1 != null && !stem1.equalsIgnoreCase("")) || (stem2 != null && stem2.equalsIgnoreCase("")) || (stem3 != null && stem3.equalsIgnoreCase("")))) {
					formas = multistem_generate(analyzer, word.getToken(), paradigm.getID(), stem1, stem2, stem3);

					/* TODO FIXME.
					Te ir vairākas lietas vienā:
					(1) 3. konjugācijai vajag celmus,
					(2) laikam arī latgaliešu daudskaitliniekiem vajag celmus,
					(3) mēs esam kādreiz domājuši, ka 3. konjugācijas verbiem,
					    kam LLVV ir norādīta 1. konj. paralēlforma (aizmirdzēt ->
					    -> aizmirdzēja + aizmirdza) varētu caur celmu nodošanu
					    varbūt šīs formās ģenerēt, bet 2025-09-04 viena papildu
					    celma padošana izraisa tēzaurā to, ka daļu formu uzģenerē
					    vispār bez celma. Tāpēc šobrīd 2./3. konjugācijai šis ir
					    aizpačots. + jādomā, ko ar šīm formām darīt - likt
					    izņēmumformas būtu daudz, jo Baiba apstiprināja, ka šie
					    celmi rada arī attiecīgos divdabjus. No pagātnes celma
					    taisa pagātnes divdabjus, no tagadnes celma - tagadnes un
					    nelokāmos.
					 */
				} else {
					// if a specific paradigm is passed, inflect according to that
					formas = analyzer.generateInflectionsFromParadigm(word.getToken(), paradigm.getID(), lemmaAttrs);
				}
			}

			for (Wordform wf : formas) {
				wf.filterAttributes(showAttrs);
				wf.addAttribute("Formas celms", wf.lexeme.getStem(wf.getEnding().stemType));
				wf.addAttribute("Nenoteiksmes celms", wf.lexeme.getStem(StemType.STEM1));
				wf.lexeme = null; // so that identical forms would compare as equal
			}

			processedTokens.add(new LinkedHashSet<>(formas));
		}
		
		analyzer.defaultSettings();
		return processedTokens;
	}


	/**
	 * Generates wordforms with the assumption that "stemX" may contain multiple
	 * alternate stem options separated by a comma
	 */
    private List<Wordform> multistem_generate(Analyzer analyzer, String token, Integer paradigmID, String stem1, String stem2, String stem3) {
        if (stem2 != null && stem2.contains(",")) {
            List<Wordform> formas = new LinkedList<>();
            String[] stems2 = stem2.split(",");
            String[] stems3 = stem3.split(",");
            for (int i=0; i<stems2.length; i++) {
                String matching_stem3 = stem3;
                if (i<stems3.length)
                    matching_stem3 = stems3[i];
                formas.addAll(multistem_generate(analyzer, token, paradigmID, stem1, stems2[i].trim(), matching_stem3.trim()));
            }
            return formas;
        }
        if (stem3 != null && stem3.contains(",")) {
            List<Wordform> formas = new LinkedList<>();
            for (String stem : stem3.split(",")) {
                formas.addAll(multistem_generate(analyzer, token, paradigmID, stem1, stem2, stem.trim()));
            }
            return formas;
        }
//        System.out.printf("%s\t%s\t%s\n", stem1, stem2, stem3);
        return analyzer.generateInflectionsFromParadigm(token, paradigmID, stem1, stem2, stem3);
    }


}
