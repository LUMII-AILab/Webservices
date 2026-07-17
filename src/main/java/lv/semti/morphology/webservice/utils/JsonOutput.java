package lv.semti.morphology.webservice.utils;

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.lexicon.Paradigm;
import lv.semti.morphology.webservice.CentralServer;
import org.json.simple.JSONValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JsonOutput
{
	public static <T extends Iterable<? extends Iterable<Wordform>>> String toJsonDoubleGeneric(
			T wordforms, String language, boolean useDefaultAttributeFilter){
		List<String> topLevelJSON = new LinkedList<>();
		for (Iterable<Wordform> wordformSubset : wordforms) {
			List<String> subJSON = new LinkedList<>();
			for (Wordform wf : wordformSubset) {
				if (useDefaultAttributeFilter) wf.filterAttributes(AttributeFilter.showableAttributes);
				if ("EN".equalsIgnoreCase(language))
					subJSON.add(CentralServer.tagset.toEnglish(wf, true).toJSON());
				else
					subJSON.add(wf.toJSON());
			}
			topLevelJSON.add(toJson(subJSON, " "));
		}
		//return new StringRepresentation(toJSON(tokenJSON), MediaType.APPLICATION_JSON);
		return toJson(topLevelJSON, "\n");
	}

	public static <T extends Iterable<Wordform>> String toJsonGeneric(
			T wordforms, String language, boolean useDefaultAttributeFilter) {
		List<String> topLevelJSON = new LinkedList<>();
		for (Wordform wf : wordforms) {
			if (useDefaultAttributeFilter) wf.filterAttributes(AttributeFilter.showableAttributes);
			if ("EN".equalsIgnoreCase(language))
				topLevelJSON.add(CentralServer.tagset.toEnglish(wf, true).toJSON());
			else
				topLevelJSON.add(wf.toJSON());
		}
		return toJson(topLevelJSON, "\n");
	}

	public static <T extends Iterable<Word>> String toJson(
			T words, String language,boolean useDefaultAttributeFilter){
		List<String> topLevelJSON = new LinkedList<>();
		for (Word word : words) {
			topLevelJSON.add(toJson(word, language, useDefaultAttributeFilter));
		}
		return toJson(topLevelJSON, "\n");
	}

	public static String toJson(
			Word word, String language, boolean useDefaultAttributeFilter) {
		List<String> topLevelJSON = new LinkedList<>();
		for (Wordform wf : word.wordforms) {
			if (useDefaultAttributeFilter) wf.filterAttributes(AttributeFilter.showableAttributes);
			if ("EN".equalsIgnoreCase(language))
				topLevelJSON.add(CentralServer.tagset.toEnglish(wf, true).toJSON());
			else
				topLevelJSON.add(wf.toJSON());
		}
		return toJson(topLevelJSON, " ");
	}

	public static String toJson(Collection<String> tags, String afterComma) {
		Iterator<String> i = tags.iterator();
		StringBuilder out = new StringBuilder("[");
		while (i.hasNext()) {
			out.append(i.next());
			if (i.hasNext())
			{
				out.append(",");
				out.append(afterComma);
			}
		}
		out.append("]");
		return out.toString();
	}

	public static String toJson(List<Paradigm> paradigms) {
		Iterator<Paradigm> i = paradigms.iterator();
		StringBuilder out = new StringBuilder("[");
		while (i.hasNext()) {
			Paradigm p = i.next();
			//out.append(String.format("{\"ID\":%d, \"Description\":%s}", p.getID(), JSONValue.toJSONString(p.getName())));
			out.append(JSONValue.toJSONString(p.getName()));
			if (i.hasNext()) out.append(", ");
		}
		out.append("]");
		return out.toString();
	}
}
