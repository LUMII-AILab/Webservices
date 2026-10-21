package lv.semti.morphology.webservice;

/**
 * General inflection service with pronunciation generation: provide a lemma and
 * optionally pronunciation and get all forms and their pronunciations.
 */
public class InflectPronounceWithHeuristicsResource extends InflectWithHeuristicsGeneric
{
	public InflectPronounceWithHeuristicsResource() {
		super.doPronunciations = true;
	}
}
