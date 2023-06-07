package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by pet on 2016-05-25.
 */
public class TezaursInflectResourceTest {
    private static InflectResource inflectResource;
    private static WordResource wordResource;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception{
        MorphoServer.enableTagger = false;
        MorphoServer.enableCorpus = false;
        MorphoServer.initResources();
        inflectResource = new InflectResource();
        wordResource = new WordResource();
    }

    private void assertFormExists(List<Collection<Wordform>> wordforms, String form) {
        assertEquals(1, wordforms.size());
        boolean found = false;
        for (Wordform wf : wordforms.get(0)) {
            if (wf.getToken().equalsIgnoreCase(form))
                found = true;
        }
        assertTrue(found);
    }

    private void assertFormDoesNotExist(List<Collection<Wordform>> wordforms, String form) {
        assertEquals(1, wordforms.size());
        for (Wordform wf : wordforms.get(0)) {
            assertNotEquals(wf.getToken(), form);
        }
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
    public void testMultipleStemsWithSpace() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("jaust", "15", "", "jaus", "jauš, jauž", "jaut, jaud", new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Tagadne);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            wf.describe();
            assertNotEquals(" jaud", wf.getToken());
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
//        for (Wordform wf : wordforms.get(0)) {
//            wf.describe();
//        }
    }

    @Test
    public void pirms() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("pirmāks", "13", "", "", null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        for (Wordform wf : wordforms.get(0)) {
            assertNotEquals("pirms", wf.getToken());
        }
    }

    @Test
    public void noliegumu_ģenerēšana() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("prātot", "16", "", "", null, null, new AttributeValues());
        assertFormExists(wordforms, "neprātoju");
        for (Wordform wf : wordforms.get(0)) {
            if (wf.getToken().equalsIgnoreCase("neprātoju"))
                assertEquals("Jā", wf.getValue(AttributeNames.i_Noliegums));
        }
        assertFormDoesNotExist(wordforms, "jāneprāto");
        assertFormDoesNotExist(wordforms, "nejāprāto");
    }

    @Test
    public void skaitļi() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
        av.addAttribute(AttributeNames.i_ProperNounType, AttributeNames.v_Toponym);
        List<Collection<Wordform>> wordforms = inflectResource.inflect("Rīga", "7", "", "", null, null, av);
        assertEquals(1, wordforms.size());
        for (Wordform wf : wordforms.get(0)) {
            // assertNotEquals("Rīgām", wf.getToken());
            //        Tagad šis filtrs ir morfotabulu zīmēšanas JavaScript atbilstoši karodziņam par morfotabulas īpatnībām
        }
    }

    @Test
    public void pabija() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("pabūt", "50", "", "pabū", "", "pabij", new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Izteiksme, AttributeNames.v_Iisteniibas);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Laiks, AttributeNames.v_Pagaatne);
        filtrs.addAttribute(AttributeNames.i_Noliegums, null);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("pabiji", wf.getToken());
        }
    }

    @Test
    public void nebēdņot() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        List<Collection<Wordform>> wordforms = inflectResource.inflect("nebēdņot", "16", "", "", null, null, av);
        assertFormExists(wordforms, "nebēdņot");
        assertFormDoesNotExist(wordforms, "jānebēdņo");
        assertFormDoesNotExist(wordforms, "nejābēdņo");
    }

    @Test
    public void ticket_100() {
        AttributeValues av = new AttributeValues();
        List<Collection<Wordform>> wordforms = inflectResource.inflect("varēt", "17", "", "", null, null, av);
        for (Wordform wf : wordforms.get(0)) {
            if (wf.getToken().equalsIgnoreCase("varēšana")) {
                assertEquals(AttributeNames.v_Yes, wf.getValue(AttributeNames.i_Derivative));
            }
        }
    }

    @Test
    public void ticket_101() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum);
        List<Collection<Wordform>> wordforms = inflectResource.inflect("Rīga", "7", "", "", null, null, av);
        assertFormExists(wordforms, "Rīgai");
//        assertFormDoesNotExist(wordforms, "Rīgām");
//        Tagad šis filtrs ir morfotabulu zīmēšanas JavaScript atbilstoši karodziņam par morfotabulas īpatnībām

        wordforms = inflectResource.inflect("miers", "1", "", "", null, null, av);
        assertFormExists(wordforms, "mieram");
        assertFormDoesNotExist(wordforms, "mieriem");
    }

    @Test
    public void abēji() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
        List<Collection<Wordform>> wordforms = inflectResource.inflect("abēji", "13", "", "", null, null, av);
        assertFormExists(wordforms, "abējiem");
    }

    @Test
    public void morfotabulu_īpatnības() {
        AttributeValues av = new AttributeValues();
        List<Collection<Wordform>> wordforms = inflectResource.inflect("Foboss", "1", "", "", null, null, av);
        assertFormExists(wordforms, "Fobosiem");
        // Mēs gribam lai webserviss šo formu atgriež, bet tēzaura tabulu zīmētājs pēc tam to ignorē
    }

    @Test
    public void ticket_125() throws Exception {
        // nez kāpēc nestrādā atpazīšana atsevišķiem vārdiem, ja tos padod ar lielo burtu
        Word w = MorphoServer.analyzer.analyze("krūšu");
        assertTrue(w.isRecognized());

        w = MorphoServer.analyzer.analyze("Krūšu");
        assertTrue(w.isRecognized());
        w.describe(System.out);
//        System.out.println(wordResource.toJSON(w.wordforms, null) );
    }

    @Test
    public void paradigm_names() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("ceļš", "noun-1b", "", null, null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("ceļiem", wf.getToken());
        }
    }

    @Test
    public void latgalian() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("kuorklys", "noun-1b-ltg", "", null, null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("kuorklim", wf.getToken());
        }
    }

    @Test
    public void latgalian_pasauļs() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("pasauļs", "noun-2a-ltg", "", null, null, null, new AttributeValues());
        assertEquals(1, wordforms.size());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        for (Wordform wf : wordforms.get(0)) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("pasauli", wf.getToken());
        }
    }

    // https://github.com/PeterisP/morphology/issues/132
    @Test
    public void ticket_132() {
        List<Collection<Wordform>> wordforms = inflectResource.inflect("paģisties", "verb-1r", "", "paģis", "paģied", "paģid", new AttributeValues());
        assertEquals(1, wordforms.size());
        assertTrue("Jābūt vairāk kā vienai formai", wordforms.get(0).size()>1);
    }
}