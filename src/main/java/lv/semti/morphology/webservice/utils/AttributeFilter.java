package lv.semti.morphology.webservice.utils;

import lv.semti.morphology.attributes.AttributeNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AttributeFilter
{
	public static final Set<String> showableAttributesAlways = initAlwaysFilter();
	public static final Set<String> showableAttributesWithPron = initFilterWithPron();

	static Set<String> initAlwaysFilter() {
		return Set.of(
				// General
				AttributeNames.i_Word,
				AttributeNames.i_PartOfSpeech,
				AttributeNames.i_Derivative,
				AttributeNames.i_Guess,
				// Nouns
				AttributeNames.i_Case,
				AttributeNames.i_Number,
				AttributeNames.i_Gender,
				AttributeNames.i_Declension,
				// Verbs/participles
				AttributeNames.i_Person,
				AttributeNames.i_Mood,
				AttributeNames.i_Tense,
				AttributeNames.i_Voice,
				AttributeNames.i_Konjugaacija,
				AttributeNames.i_Noliegums,
				// Aadjectives
				AttributeNames.i_Degree,
				AttributeNames.i_Definiteness,
				// Usage restrictions are necessary for distinguishing which forms to use/show
				AttributeNames.i_Frequency,
				AttributeNames.i_Usage,
				AttributeNames.i_Normative,
				// Tokenization results
				AttributeNames.i_PositionInParagraph,
				AttributeNames.i_SpaceBefore);
	}

	static Set<String> initFilterWithPron() {
		List<String> result = new ArrayList<>(showableAttributesAlways);
		result.add(AttributeNames.i_Pronunciation);
		return Set.copyOf(result);
	}
}
