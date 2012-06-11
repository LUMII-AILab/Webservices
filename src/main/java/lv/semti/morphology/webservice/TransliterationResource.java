package lv.semti.morphology.webservice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lv.ailab.lnb.fraktur.translit.ResultData;

public class TransliterationResource extends ServerResource {
	@Get("xml")
	public String retrieve() {  
		String ruleset = (String) getRequest().getAttributes().get("ruleset");
		String word = (String) getRequest().getAttributes().get("word");
		
		try {
			word = URLDecoder.decode(word, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String XML = "";

		try {
			if (!MorphoServer.translit.isValidRuleSet(ruleset)) {
				// Fallback
				ruleset = "core";
			}
			
			XML += "<normalization rule_set=\"" + ruleset + "\" apply_opt_rules=\"true\">";
			
			ResultData rd = MorphoServer.translit.processWord(word, ruleset, true);
			if (rd != null) {
				XML += rd.toXML(MorphoServer.translit.getDictIdKey(), 
								MorphoServer.translit.getEntryUrlKey(), 
								MorphoServer.translit.comparator);
			}
			
			XML += "</normalization>";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return XML;
	}
}
