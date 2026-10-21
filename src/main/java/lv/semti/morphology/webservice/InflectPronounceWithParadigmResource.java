package lv.semti.morphology.webservice;

/**
 * Tēzaurs service for getting inflections with pronunciations:
 * provide a lemma, paradigm, lemma pronunciation and optionally stems and get
 * all forms with their pronunciations. Returns 404 when paradigm is not given
 * or when pronunciation service doesn't support that paradigm.
 */
public class InflectPronounceWithParadigmResource extends InflectWithParadigmGeneric
{
	public InflectPronounceWithParadigmResource()
	{
		super.doPronunciations = true;
	}
}
