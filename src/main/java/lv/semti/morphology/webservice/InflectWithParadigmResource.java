package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Paradigm;
import lv.semti.morphology.lexicon.StemType;
import lv.semti.morphology.webservice.utils.AttributeFilter;
import lv.semti.morphology.webservice.utils.JsonOutput;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tēzaurs inflection service: provide a lemma and paradigm (and optionally
 * stems) and get all forms. Returns 404 for lacking paradigm to facilitate
 * separation for general inflection service.
 */
public class InflectWithParadigmResource extends ServerResource
{
	@Get("json")
	public String retrieve() {
		getResponse().setAccessControlAllowOrigin("*");
		String query = (String) getRequest().getAttributes().get("query");
		String language = (String) getRequest().getAttributes().get("language");
		String inflmisc = getQuery().getValues("inflmisc");
		Paradigm paradigm = decodeParadigm(getQuery().getValues("paradigm"));

		if (paradigm == null)
		{
			doError(Status.CLIENT_ERROR_NOT_FOUND);
			return null;
		}

		List<Wordform> processedTokens = inflect(query, paradigm,
				getQuery().getValues("stem1"), getQuery().getValues("stem2"), getQuery().getValues("stem3"), decodeInflMisc(inflmisc));

		return JsonOutput.toJsonGeneric(processedTokens, language, false);
	}

	public List<Wordform> inflect(String query, Paradigm paradigm, String stem1, String stem2, String stem3, AttributeValues lemmaAttrs) {
		query = URLDecoder.decode(query, StandardCharsets.UTF_8);
		if (paradigm == null || query == null || query.isEmpty()) return null;
		Analyzer analyzer = paradigm.name.endsWith("-ltg")
				? CentralServer.getLatgalian_analyzer() : CentralServer.getAnalyzer();

		// 2026-07-21: I'm not completely sure, if and when guess parameter
		// influences inflection generation with given paradigm. More research
		// needed.
		analyzer.enableGuessing = true;
		analyzer.enableVocative = true;
		analyzer.guessVerbs = false;
		analyzer.guessParticiples = false;
		analyzer.guessAdjectives = false;
		analyzer.guessInflexibleNouns = true;
		analyzer.enableAllGuesses = true;

		Word word = new Word(query);
		List<Wordform> forms;

		//if ((paradigm.getStems()>1) && stem1 != null && stem2 != null && stem3 != null) {
		// For 1st conjugation verbs, if all three stems are passed, then try to use them for inflection
		if ( !paradigm.getName().matches("verb-[23].*") &&
				((stem1 != null && !stem1.equalsIgnoreCase("")) || (stem2 != null && stem2.equalsIgnoreCase("")) || (stem3 != null && stem3.equalsIgnoreCase("")))) {
			forms = multistemGenerate(analyzer, word.getToken(), paradigm.getID(), stem1, stem2, stem3);

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
			forms = analyzer.generateInflectionsFromParadigm(word.getToken(), paradigm.getID(), lemmaAttrs);
		}

		for (Wordform wf : forms) {
			wf.filterAttributes(AttributeFilter.showableAttributes);
			wf.addAttribute(StemType.FORM_STEM.descriptionLV, wf.lexeme.getStem(wf.getEnding().stemType));
			wf.addAttribute(StemType.STEM1.descriptionLV, wf.lexeme.getStem(StemType.STEM1));
			wf.lexeme = null; // so that identical forms would compare as equal
		}

		CentralServer.defaultAnalyzersSettings();
		return forms;
	}

	Paradigm decodeParadigm (String paradigmParam)
	{
		if (paradigmParam == null || paradigmParam.isEmpty())
			return null;
		Paradigm paradigm;
		Analyzer analyzer = CentralServer.getAnalyzer();
		try {
			int paradigmID = Integer.decode(paradigmParam);
			paradigm = analyzer.paradigmByID(paradigmID);
		} catch (NumberFormatException e) {
			// Using paradigm names
			if (paradigmParam.endsWith("ltg")) {
				analyzer = CentralServer.getLatgalian_analyzer();
			}
			paradigm = analyzer.paradigmByName(paradigmParam);
		}

		if (paradigm == null) {
			System.err.printf("Could not find paradigm '%s'\n", paradigmParam);
		}
		return paradigm;
	}

	static AttributeValues decodeInflMisc(String inflmisc) {
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

	/**
	 * Generates wordforms with the assumption that "stemX" may contain multiple
	 * alternate stem options separated by a comma
	 */
	private List<Wordform> multistemGenerate(Analyzer analyzer, String token, Integer paradigmID, String stem1, String stem2, String stem3) {
		if (stem2 != null && stem2.contains(",")) {
			List<Wordform> formas = new LinkedList<>();
			String[] stems2 = stem2.split(",");
			String[] stems3 = stem3.split(",");
			for (int i=0; i<stems2.length; i++) {
				String matching_stem3 = stem3;
				if (i<stems3.length)
					matching_stem3 = stems3[i];
				formas.addAll(multistemGenerate(analyzer, token, paradigmID, stem1, stems2[i].trim(), matching_stem3.trim()));
			}
			return formas;
		}
		if (stem3 != null && stem3.contains(",")) {
			List<Wordform> formas = new LinkedList<>();
			for (String stem : stem3.split(",")) {
				formas.addAll(multistemGenerate(analyzer, token, paradigmID, stem1, stem2, stem.trim()));
			}
			return formas;
		}
//        System.out.printf("%s\t%s\t%s\n", stem1, stem2, stem3);
		return analyzer.generateInflectionsFromParadigm(token, paradigmID, stem1, stem2, stem3);
	}

}
