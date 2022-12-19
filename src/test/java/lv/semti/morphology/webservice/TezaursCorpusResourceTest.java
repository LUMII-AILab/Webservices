package lv.semti.morphology.webservice;

import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;
import lv.semti.morphology.corpus.Example;
import org.junit.BeforeClass;
import org.junit.Ignore;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TezaursCorpusResourceTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        MorphoServer.enableCorpus = true;
        MorphoServer.initResources();
        LVMorphologyReaderAndWriter.setAnalyzerDefaults();
    }

    @org.junit.Test
    public void dervišs() throws Exception {
        List<Example> examples = MorphoServer.corpus.findExamples("dervišs");
        String example_string = examples.stream()
                .map(Example::toString)
                .collect(Collectors.joining(", "));
        assertFalse(example_string.contains("Eroglu"));
    }
}