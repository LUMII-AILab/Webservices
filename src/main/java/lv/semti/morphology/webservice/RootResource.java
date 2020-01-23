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

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class RootResource extends ServerResource {
	@Get
	public Representation retrieve() {
		StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Latvian morphology web services</title></head><body><h3>Version ");
        sb.append(MorphoServer.analyzer.getRevision());
        sb.append("</h3><ul>");
        addLink(sb, "analyze/doma", "/analyze/[word] : morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
        addLink(sb, "analyze/en/doma", "/analyze/en/[word] : morphological analysis of the word with attribute names in English");
        addLink(sb, "analyzesentence/Vīrs%20ar%20cirvi.", "/analyzesentence/[query] : JSON format of analysis for each token in a sentence for tagger needs");
        addLink(sb, "tokenize/domāju%20es%20domas%20dziļas.", "/tokenize/[query] or POST to /tokenize : tokenization of sentences");
        addLink(sb, "verbs/domai", "/verbs/[query] and /neverbs/[query] : Support webservice for 'verbs' valency annotation tool - possible inflections of wordform");
        addLink(sb, "v1/inflections/rakt", "/v1/inflections/[query] : generate all inflectional forms of a lemma");
        addLink(sb, "inflect/xml/rakt", "/inflect/[format]/[query] : generate all inflectional forms of a lemma");
        addLink(sb, "inflect/json/en/rakt", "/inflect/[format]/[language]/[query] : generate all inflectional forms of a lemma");
        addLink(sb, "suitable_paradigm/pokemonizators", "/suitable_paradigm/[lemma] : provides a sorted lists of paradigms that may form the provided lemma");
        addLink(sb, "v1/transcriptions/vīrs%20ar%20cirvi?phoneme_set=IPA", "/v1/transcriptions/[phrase] : phonetic transcription of the phrase");
        if (MorphoServer.enableCorpus) {
            addLink(sb, "v1/examples/doma", "/v1/examples/[query] : provides a list of corpus mentions of the queried word");
        }

        if (MorphoServer.enableTransliterator) {
            addLink(sb, "explain/vuška", "/explain/[query] : dictionary explanations of historical words");
            addLink(sb, "normalize/core/waj", "/normalize/[ruleset]/[word] : historical word transliteration ");
        }

        if (MorphoServer.enableTagger) {
            addLink(sb, "morphotagger/vīrs%20ar%20cirvi.", "/morphotagger/[query] : do statistical morphological disambiguation of a sentence");
            addLink(sb, "inflect_people/json/Baraks%20Obama?gender=m", "/inflect_people/json/[query]?gender=[m/f] : generate all inflectional forms of words, assuming that they are person names");
            addLink(sb, "inflect_phrase/Latvijas%20Universitātes%20Matemātikas%20un%20Informātikas%20Institūts?category=org", "/inflect_phrase/[phrase]?category=[person/org/loc] : try to inflect a multiword expression / named entity, given its category");
            addLink(sb, "normalize_phrase/Latvijas%20Universitātes%20Matemātikas%20un%20Informātikas%20Institūtam?category=org", "/normalize_phrase/[phrase]?category=[person/org/loc] : try to transform a multiword expression / named entity to its base form, given its category");
        }

        if (MorphoServer.enableNERTagger) {
            addLink(sb, "/nertagger/Maestro%20Raimonds%20Pauls%20(no%20kreis%C4%81s)%20un%20kinore%C5%BEisors%20J%C4%81nis%20Strei%C4%8Ds%20ar%20sa%C5%86emto%20balvu%20par%20m%C5%AB%C5%BEa%20ieguld%C4%ABjumu%20kino%20m%C4%81ksl%C4%81%20piedal%C4%81s%20Nacion%C4%81l%C4%81s%20kino%20balvas%20/%22Lielais%20Kristaps/%22%2festiv%C4%81la%20atkl%C4%81%C5%A1an%C4%81%20kinote%C4%81tr%C4%AB%20/%22Splendid%20Palace/%22.", "/nertagger/[query] : perform NER tagging");
            addLink(sb, "/nerpeople/Maestro%20Raimonds%20Pauls%20(no%20kreis%C4%81s)%20un%20kinore%C5%BEisors%20J%C4%81nis%20Strei%C4%8Ds%20ar%20sa%C5%86emto%20balvu%20par%20m%C5%AB%C5%BEa%20ieguld%C4%ABjumu%20kino%20m%C4%81ksl%C4%81%20piedal%C4%81s%20Nacion%C4%81l%C4%81s%20kino%20balvas%20/%22Lielais%20Kristaps/%22%2festiv%C4%81la%20atkl%C4%81%C5%A1an%C4%81%20kinote%C4%81tr%C4%AB%20/%22Splendid%20Palace/%22.", "/nerpeople/[query] : extract a list of people mentioned in the text");
        }

        if (MorphoServer.enableDomeniims && MorphoServer.enableTagger) {
            addLink(sb, "domenims/krogssala", "/domenims/[domain name] : word2vec based domain name alternative generator");
            addLink(sb, "segment/krogssala", "/domenims/[domain name] : domain name segmenter");
        }

        if (MorphoServer.enableTezaurs) {
            addLink(sb, "v1/words/doma", "/v1/words/[query] : provides a json representation of the queried tezaurs.lv entry");
        }

        sb.append("</ul></body></html>");
		return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
	}

    private void addLink(StringBuilder sb, String url, String description) {
        sb.append(String.format("<li> <a href=\"/%s\">%s</a> </li>", url, description));
    }
}

