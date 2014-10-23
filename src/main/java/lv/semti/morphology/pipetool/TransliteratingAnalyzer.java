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
