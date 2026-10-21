package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Wordform;

import java.util.Collection;
import java.util.List;

/**
 * General inflection service: provide a lemma and get all forms. Faster than
 * `InflectPronounceWithHeuristicsResource`.
 */
public class InflectWithHeuristicsResource extends InflectWithHeuristicsGeneric
{
	public InflectWithHeuristicsResource()
	{
		super.doPronunciations = false;
	}

	public List<Collection<Wordform>> inflect(String query, boolean guess, boolean latgalian) {
		return inflect(query, null, guess, latgalian);
	}
}
