package lv.semti.morphology.webservice;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TaggerTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        MorphoServer.initResources();
        LVMorphologyReaderAndWriter.setAnalyzerDefaults();
    }

    private static List<CoreLabel> tag (String sentence) {
        return MorphoServer.morphoClassifier.classify( LVMorphologyReaderAndWriter.analyzeSentence(sentence.trim() ));
    }

    private void assertTag(List<CoreLabel> sentence, int word, String tag) {
        String token = sentence.get(word).getString(CoreAnnotations.TextAnnotation.class);
        assertFalse(token.contains("<s>"));
        Word analysis = sentence.get(word).get(CoreAnnotations.LVMorphologyAnalysis.class);
        Wordform maxwf = analysis.getMatchingWordform(sentence.get(word).getString(CoreAnnotations.AnswerAnnotation.class), true);
        assertEquals(tag, maxwf.getTag());
    }

    @Test
    public void mistika() {
        // atšķīrās rezultāti, laižot it kā to pašu versiju caur morphotagger.sh un webservices
        List<CoreLabel> sentence = tag("Vārds Ford ir slikts.");
        assertTag(sentence, 2, "xf");

        sentence = tag("Vārds fuck ir slikts.");
        assertTag(sentence, 2, "xf");

        sentence = tag("Vārds DJ ir slikts.");
        assertTag(sentence, 2, "y");

        sentence = tag("Vārds Fords ir slikts.");
        assertTag(sentence, 2, "ncmsn1");
    }
}