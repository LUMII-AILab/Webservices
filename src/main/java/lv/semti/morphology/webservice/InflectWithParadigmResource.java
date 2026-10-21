package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeValues;
import lv.semti.morphology.lexicon.Paradigm;

import java.util.List;

/**
 * Tēzaurs service for getting inflections:
 * provide a lemma and paradigm and optionally stems and get all forms.
 * Faster than `InflectPronounceWithParadigmResource`. Returns 404 when no
 * paradigm is given or paradigm is not recognized.
 */
public class InflectWithParadigmResource extends InflectWithParadigmGeneric
{
	public InflectWithParadigmResource()
	{
		super.doPronunciations = false;
	}

	public List<Wordform> inflect(String query, Paradigm paradigm, String stem1, String stem2, String stem3, AttributeValues lemmaAttrs)
	{
		return inflect(query, null, paradigm, stem1, stem2, stem3, lemmaAttrs);
	}

}
