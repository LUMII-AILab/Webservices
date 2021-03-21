package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by pet on 2016-05-25.
 */
public class TezaursInflectResourceTest {
    private static InflectResource inflectResource;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception{
        MorphoServer.enableTagger = false;
        MorphoServer.enableCorpus = false;
        MorphoServer.initResources();
        inflectResource = new InflectResource();
    }

    @Test
    public void testMultipleStems() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("jaust", "15", "", "jaus", "jauš,jauž", "jaut,jaud", new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
//            wf.describe();
            assertNotEquals("jauš", wf.getToken());
        }
    }

    @Test
    public void turpms() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("turpmāks", "13", "", "turpmāk", null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        for (Wordform wf : wordforms.get(0)) {
            assertNotEquals("turpms", wf.getToken());
        }
    }

    @Test
    public void nelokāmie() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("augstpapēžu", "49", "", "", null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        assertNotEquals(0, wordforms.get(0).size());
        for (Wordform wf : wordforms.get(0)) {
            wf.describe();
        }
    }
}