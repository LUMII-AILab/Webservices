package lv.semti.morphology.webservice;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * General info about all services: main page, morphology version and returning
 * 404 for unkonwn GETs.
 */
public class InfoResource extends ServerResource {
	@Get
	public Representation retrieve() {

        String tail = (String) getRequest().getAttributes().get("tail");
        if (tail != null && (tail.equals("version") || tail.equals("versions"))) {
            // Version info
            return new StringRepresentation(CentralServer.getAnalyzer().getRevision(), MediaType.TEXT_HTML);
        } else if (tail != null && !tail.isEmpty()) {
            // 404 for unknown
            doError(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        // Main page

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Latvian morphology web services</title></head><body><h3>Version ");
        sb.append(CentralServer.getAnalyzer().getRevision());
        sb.append("</h3>\n<ul>\n");
        addGetLink(sb, "analyze/doma", "/analyze/[word]", "morphological analysis of the word (guessing of out-of-vocabulary words disabled by default)");
        addGetLink(sb, "analyze/en/doma", "/analyze/en/[word]", "morphological analysis of the word with attribute names in English");
        addGetLink(sb, "v1/inflections/rakt", "/v1/inflections/[query]", "generate all inflectional forms of a lemma");
        addGetLink(sb, "v1/inflections/aita?paradigm=noun-4f", "/v1/inflections/[query]&amp;paradigm=[paradigm name]", "generate all inflectional forms of a lemma according to the given paradigm");
        addGetLink(sb, "v1/inflections/aust?paradigm=verb-1&stem1=aus&stem2=aust&stem3=aus", "/v1/inflections/[query]&amp;paradigm=[paradigm name]&amp;stem1=[infinitive stem]&amp;stem2=[present stem]&amp;stem3=[past stem]", "generate all inflectional forms of a verb from a 1st conjunction");
        addGetLink(sb, "inflect/xml/rakt", "/inflect/[format]/[query]", "generate all inflectional forms of a lemma");
        addGetLink(sb, "inflect/json/en/rakt", "/inflect/[format]/[language]/[query]", "generate all inflectional forms of a lemma");
        addGetLink(sb, "suitable_paradigm/pokemonizators", "/suitable_paradigm/[lemma]", "provides a sorted lists of paradigms that may form the provided lemma");
        addGetLink(sb, "tokenize/domāju%20es%20domas%20dziļas.", "/tokenize/[query] or POST to /tokenize", "tokenization of sentences");
        sb.append("</ul>\n");

        if (CentralServer.enableTagger) {
            sb.append("<p>Morphotagger is turned on and provides following:</p>\n<ul>\n");
            addGetLink(sb, "morphotagger/vīrs%20ar%20cirvi.", "/morphotagger/[query]", "do statistical morphological disambiguation of a sentence");
            addGetLink(sb, "inflect_people/json/Baraks%20Obama?gender=m", "/inflect_people/json/[query]?gender=[m/f]", "generate all inflectional forms of words, assuming that they are person names");
            addGetLink(sb, "inflect_phrase/Latvijas%20Universitātes%20Matemātikas%20un%20Informātikas%20Institūts?category=org", "/inflect_phrase/[phrase]?category=[person/org/loc]", "try to inflect a multiword expression / named entity, given its category");
            addGetLink(sb, "normalize_phrase/Latvijas%20Universitātes%20Matemātikas%20un%20Informātikas%20Institūtam?category=org", "/normalize_phrase/[phrase]?category=[person/org/loc]", "try to transform a multiword expression / named entity to its base form, given its category");
            sb.append("</ul>\n");
        } else sb.append("<p>Morphotagger is turned off.</p>\n");


        if (CentralServer.enableTranscription) {
            sb.append("<p>Phonetic transcriber is turned on and provides following:</p>\n<ul>\n");
            addGetLink(sb, "v1/transcriptions/vīrs%20ar%20cirvi", "/v1/transcriptions/[phrase]", "phonetic transcription of the phrase");
            addGetLink(sb, "v1/transcriptions/vīrs%20ar%20cirvi?phoneme_set=IPA", "/v1/transcriptions/[phrase]?phoneme_set=IPA", "IPA phonetic transcription of the phrase");
            sb.append("</ul>\n");
        } else sb.append("<p>Phonetic transcriber is turned off.</p>\n");

        if (CentralServer.enableLexiconReloader)
        {
            sb.append("<p>Lexicon reloader is available:</p>\n<ul>\n");
            addPostLink(sb, "reload_lexicon/latvian", "/reload_lexicon/latvian", "reload a lexicon from Tēzaurs DB  (POST)");
            addPostLink(sb, "reload_lexicon/latgalian", "/reload_lexicon/latgalian", "reload a lexicon from LTG Tēzaurs DB  (POST)");
            sb.append("</ul>\n");
        }

        sb.append("</body></html>\n");
		return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
	}

    private void addGetLink(StringBuilder sb, String url, String urlText, String description) {
        sb.append(String.format("<li> <a href=\"/%s\">%s</a>: %s</li>\n", url, urlText, description));
    }

    private void addPostLink(
            StringBuilder sb, String url, String urlText, String description)
    {
        sb.append(String.format("<li> <form method=\"post\" action=\"%s\" style=\"display:inline\">" +
                " <button type=\"submit\">%s</button>: %s\n" +
                " </form></li>", url, urlText, description));
    }

    /*private void addPostLink(
            StringBuilder sb, String url, String param, String val, String description)
    {
        sb.append(String.format("<li> <form method=\"post\" action=\"%s\">" +
                " <button type=\"submit\" name=\"%s\" value=\"%s\">%s</button>\n" +
                " </form></li>", url, param, val, description));
    }
    private void addPostLink(
            StringBuilder sb, String url, String param1, String val1,
            String param2, String val2, String description)
    {
        sb.append(String.format("<li> <form method=\"post\" action=\"%s\">" +
                " <input type=\"hidden\" name=\"%s\" value=\"%s\">" +
                " <button type=\"submit\" name=\"%s\" value=\"%s\">%s</button>\n" +
                " </form></li>", url, param1, val1, param2, val2, description));
    }//*/
}

