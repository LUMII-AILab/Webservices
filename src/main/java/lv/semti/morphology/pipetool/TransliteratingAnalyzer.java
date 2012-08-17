package lv.semti.morphology.pipetool;

import java.util.ArrayList;
import java.util.Collections;

import lv.ailab.lnb.fraktur.Transliterator;
import lv.ailab.lnb.fraktur.ngram.VariantComparator;
import lv.ailab.lnb.fraktur.translit.ResultData;
import lv.ailab.lnb.fraktur.translit.Variant;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.webservice.MorphoServer;

public class TransliteratingAnalyzer extends Analyzer {
	private Transliterator transliterator;
	
	public TransliteratingAnalyzer(String lexiconFileName) throws Exception {
		super(lexiconFileName);
		transliterator = Transliterator.getTransliterator();
	}

	public TransliteratingAnalyzer(String lexiconFileName, boolean useAuxiliaryLexicons) throws Exception {
		super(lexiconFileName, useAuxiliaryLexicons);
		transliterator = Transliterator.getTransliterator();
	}

	public Word analyze(String word) {
		ResultData rd = transliterator.processWord(word, "fraktur", true);
		ArrayList<Variant> variants = rd.getAllVariants();
		
		String transliterated = word;
		if (variants.size() > 0) {
			Collections.sort(variants, transliterator.comparator);
			transliterated = variants.get(0).token;
		}
		return super.analyze(transliterated);
	}
}
