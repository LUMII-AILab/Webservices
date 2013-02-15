package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.json.simple.JSONValue;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import lv.semti.morphology.analyzer.*;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.corpus.Statistics;

public class VerbResource extends ServerResource {
	@Get
	public String retrieve() {  
		return parsequery(true);
	}
	
	public String parsequery(Boolean verb) {
		String query = (String) getRequest().getAttributes().get("query");
		try {
			query = URLDecoder.decode(query,"UTF8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MorphoServer.analyzer.defaultSettings();
		
		LinkedList<Word> tokens = Splitting.tokenize(MorphoServer.analyzer, query);
		String debug = "";
		for (Word token : tokens) {
			if (token.isRecognized()) 
				debug += token.wordforms.get(0).getDescription();
				else debug += token.getToken();
			debug += "\n";
		}
		debug += String.valueOf(tokens.size());
		
		String tag = "";
		if (tokens.size() == 1) tag = tagWord (tokens.get(0), verb);
		else tag = tagChunk(tokens);  // Heiristikas vairākvārdu situācijas risināšanai

		if (tag == "") return debug; else return tag;
	}

	private String tagWord(Word word, Boolean verb) {  // Atrodam ticamāko tagu, ja ir viens vārds analizējams		
		LinkedHashSet<String> tags = new LinkedHashSet<String>();
		
		if (word.isRecognized()) {
			Wordform maxwf = word.wordforms.get(0);
			double maxticamība = -1;
			for (Wordform wf : word.wordforms) {  // Paskatamies visus atrastos variantus un ņemam statistiski ticamāko
				//tag += String.format("%s\t%d\n", wf.getDescription(), MorphoServer.statistics.getTicamība(wf));
				double ticamība = Statistics.getStatistics().getEstimate(wf);
				if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb) == verb) ticamība += 200;
				if (ticamība > maxticamība) {
					maxticamība = ticamība;
					maxwf = wf;
				}
			}
			//System.out.printf("Ticamiiba vaardam %s ir %d", maxwf.getToken(), maxticamība);
			
			if (maxwf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb)) {  // Verbiem tagi ar verba personu
				String person = maxwf.getValue(AttributeNames.i_Person);
				if (person != null && person.length() == 1) tags.add("V" + person);
				if (maxwf.isMatchingStrong(AttributeNames.i_Izteiksme, AttributeNames.v_Nenoteiksme)) tags.add("Inf");
				
				Collections.addAll(tags, "V1","V2","V3","Inf");
			}
			if (maxwf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun) || maxwf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Pronoun) ) { // Lietvārdiem un vietniekvārdiem tagi ar locījumu
				String ncase = caseCode(maxwf.getValue(AttributeNames.i_Case));
				if (ncase != null) tags.add(ncase);
				Collections.addAll(tags, "Nom","Gen","Dat","Acc","Loc");
			}
			if (maxwf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Adverb)) { // Apstākļa vārdi
				Collections.addAll(tags, "Adv");
			}
		}
		
		if (tags.isEmpty()) Collections.addAll(tags, "Nom","Gen","Dat","Acc","Loc", "V1","V2","V3", "Inf","S","TR", "Adv"); // Ja nesapratām, dodam visus variantus
		
		return formatJSON(tags);
	}

	private String tagChunk(LinkedList<Word> tokens) {
		LinkedHashSet<String> tags = new LinkedHashSet<String>();
		// dažādi minējumi. normāli bū†u tikai ar sintakses analīzi
		//tags.add(String.valueOf(tokens.size()));
		//tags.add(tokens.get(0).getToken());
		//tags.add(tokens.get(0).getPartOfSpeech());
		if (tokens.size() > 1 && tokens.get(0).isRecognized() && tokens.get(0).hasAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Preposition)) {
			// ja frāze sākas ar prievārdu
			for (Wordform wf : tokens.get(0).wordforms) {
				//tags.add(wf.getDescription());
				if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.v_Preposition)) {
					String ncase = wf.getValue(AttributeNames.i_Rekcija);
					if (ncase != null) tags.add(wf.getToken() + caseCode(ncase));
				}
			}			
		}
		
		//ja sākas ar saikli, tad vareetu buut paliigteikums
		if (tokens.size() > 1 && tokens.get(0).isRecognized() && tokens.get(0).hasAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Conjunction)) {
			tags.add("S");
		}
		
		if (tags.isEmpty()) return tagWord(tokens.getLast(),false); // Ja nesapratām, dodam pēdējā vārda analīzi - Gunta teica, ka esot ticamāk tā
		
		return formatJSON(tags);
	}

	private String formatJSON(Collection<String> tags) {
		Iterator<String> i = tags.iterator();
		String out = "[";
		while (i.hasNext()) {
			out += "\"" + JSONValue.escape(i.next()) + "\"";
			if (i.hasNext()) out += ", ";
		}
		out += "]";
		return out;
	}
	
	private String caseCode(String caseName) {
		if (caseName == null) return null;
		String result = null;
		if (caseName.equals(AttributeNames.v_Nominative)) 	result = "Nom";
		if (caseName.equals(AttributeNames.v_Genitive)) 	result = "Gen";
		if (caseName.equals(AttributeNames.v_Dative)) 		result = "Dat";
		if (caseName.equals(AttributeNames.v_Accusative)) 	result = "Acc";
		if (caseName.equals(AttributeNames.v_Locative)) 	result = "Loc";
		return result;
	}
	
}