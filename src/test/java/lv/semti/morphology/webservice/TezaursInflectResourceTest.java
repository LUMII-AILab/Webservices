package lv.semti.morphology.webservice;

import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import lv.semti.morphology.attributes.AttributeValues;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by pet on 2016-05-25.
 */
public class TezaursInflectResourceTest {
    private static InflectWithParadigmResource inflectParRes;
    //private static WordformAnalyzeResource wordformResource;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception{
        CentralServer.enableTagger = false;
        CentralServer.initResources();
        //inflectRes = new InflectResource();
        inflectParRes = new InflectWithParadigmResource();
        //wordformResource = new WordformAnalyzeResource();
    }

    private void assertFormExists(List<Collection<Wordform>> wordforms, String form) {
        assertEquals(1, wordforms.size());
        boolean found = false;
        for (Wordform wf : wordforms.getFirst()) {
			if (wf.getToken().equalsIgnoreCase(form)) {
				found = true;
				break;
			}
        }
        assertTrue(found);
    }

    private void assertFormExists(Collection<Wordform> wordforms, String form) {
        boolean found = false;
        for (Wordform wf : wordforms) {
            if (wf.getToken().equalsIgnoreCase(form)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    private void assertFormDoesNotExist(List<Collection<Wordform>> wordforms, String form) {
        assertEquals(1, wordforms.size());
        for (Wordform wf : wordforms.getFirst()) {
            assertNotEquals(wf.getToken(), form);
        }
    }

    private void assertFormDoesNotExist(Collection<Wordform> wordforms, String form) {
        for (Wordform wf : wordforms) {
            assertNotEquals(wf.getToken(), form);
        }
    }

    @Test
    public void testMultipleStems() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "jaust", inflectParRes.decodeParadigm("15"), true, "jaus", "jauš,jauž", "jaut,jaud", new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Mood, AttributeNames.v_Indicative);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Tense, AttributeNames.v_Present);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertNotEquals("jauš", wf.getToken());
        }
    }

    @Test
    public void testMultipleStemsWithSpace() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "jaust", inflectParRes.decodeParadigm("15"), true, "jaus", "jauš, jauž", "jaut, jaud", new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Mood, AttributeNames.v_Indicative);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Tense, AttributeNames.v_Present);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            wf.describe();
            assertNotEquals(" jaud", wf.getToken());
        }
    }

    @Test
    public void turpms() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "turpmāks", inflectParRes.decodeParadigm("13"), true, "turpmāk", null, null, new AttributeValues());
        for (Wordform wf : wordforms) {
            assertNotEquals("turpms", wf.getToken());
        }
    }

    @Test
    public void nelokāmie() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "augstpapēžu", inflectParRes.decodeParadigm("49"), true, "", null, null, new AttributeValues());
        assertNotEquals(0, wordforms.size());
    }

    @Test
    public void pirms() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "pirmāks", inflectParRes.decodeParadigm("13"), true, "", null, null, new AttributeValues());
        for (Wordform wf : wordforms) {
            assertNotEquals("pirms", wf.getToken());
        }
    }

    @Test
    public void negationGeneration() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "prātot", inflectParRes.decodeParadigm("16"), true, "", null, null, new AttributeValues());
        assertFormExists(wordforms, "neprātoju");
        for (Wordform wf : wordforms) {
            if (wf.getToken().equalsIgnoreCase("neprātoju"))
                assertEquals("Jā", wf.getValue(AttributeNames.i_Noliegums));
        }
        assertFormDoesNotExist(wordforms, "jāneprāto");
        assertFormDoesNotExist(wordforms, "nejāprāto");
    }

    @Test
    public void secondThirdConjStems() {
        // aizmirdzēt?paradigm=verb-3a&stem1=&stem2=&stem3=mirdz
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "aizmirdzēt", inflectParRes.decodeParadigm("17"), true, "", "", "mirdz", new AttributeValues());
        assertFormExists(wordforms, "aizmirdzu");
        assertFormDoesNotExist(wordforms, "nu");
        wordforms = inflectParRes.inflect(
                "aizmirdzēt", inflectParRes.decodeParadigm("17"), true, "", null, "mirdz", new AttributeValues());
        assertFormExists(wordforms, "aizmirdzu");
        assertFormDoesNotExist(wordforms, "nu");
    }

    @Test
    public void toponymNumbers() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NounType, AttributeNames.v_ProperNoun);
        av.addAttribute(AttributeNames.i_ProperNounType, AttributeNames.v_Toponym);
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "Rīga", inflectParRes.decodeParadigm("7"), false, "", null, null, av);
        assertTrue(wordforms.size() >= 10);
        // Kādreiz šeit bija filtrs, lai nebūtu daudzskaitļu, bet tagad tas ir izspīdināšnas pusē
        //for (Wordform wf : wordforms.get(0)) {
            // assertNotEquals("Rīgām", wf.getToken());
        //}
    }

    @Test
    public void pronouns()
    {
        // Leksikonā no Tēzaura DB ir jābūt nonākušām nestandarta vietniekvārdu formām.
        // Tātad visiem vietniekvārdiem ir jābūt formām.
        // Gan "viņš", ko loka pēc lietvārda parauga bez specformām:
        Collection<Wordform> viņš = inflectParRes.inflect(
                "viņš", inflectParRes.decodeParadigm("noun-1b"), false, "", null, null, new AttributeValues());
        assertTrue(viņš.size() > 1);
        // Gan "jebkas", kam būtu jābūt specformām:
        Collection<Wordform> jebkas = inflectParRes.inflect(
                "jebkas", inflectParRes.decodeParadigm("pron"), false, "", null, null, new AttributeValues());
        assertTrue(jebkas.size() > 1);
    }

    @Test
    public void pabija() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "pabūt", inflectParRes.decodeParadigm("50"), false, "pabū", "", "pabij", new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Verb);
        filtrs.addAttribute(AttributeNames.i_Mood, AttributeNames.v_Indicative);
        filtrs.addAttribute(AttributeNames.i_Person, "2");
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        filtrs.addAttribute(AttributeNames.i_Tense, AttributeNames.v_Past);
        filtrs.addAttribute(AttributeNames.i_Noliegums, null);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("pabiji", wf.getToken());
        }
    }

    @Test
    public void nebēdņot() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "nebēdņot", inflectParRes.decodeParadigm("16"), true, "", null, null, av);
        assertFormExists(wordforms, "nebēdņot");
        assertFormDoesNotExist(wordforms, "jānebēdņo");
        assertFormDoesNotExist(wordforms, "nejābēdņo");
    }

    @Test
    public void šanaDerivatives() {
        // Ticket 100
        AttributeValues av = new AttributeValues();
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "varēt", inflectParRes.decodeParadigm("17"), true, "", null, null, av);
        for (Wordform wf : wordforms) {
            if (wf.getToken().equalsIgnoreCase("varēšana")) {
                assertEquals(AttributeNames.v_Yes, wf.getValue(AttributeNames.i_Derivative));
            }
        }
    }

    @Test
    public void plurals() {
        // Ticket 101
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_SingulareTantum);
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "Rīga", inflectParRes.decodeParadigm("7"), true, "", null, null, av);
        assertFormExists(wordforms, "Rīgai");
        // assertFormDoesNotExist(wordforms, "Rīgām");
        // Tagad šis filtrs ir morfotabulu zīmēšanas JavaScript atbilstoši karodziņam par morfotabulas īpatnībām

        wordforms = inflectParRes.inflect(
                "Foboss", inflectParRes.decodeParadigm("1"), false, "", null, null, new AttributeValues());
        assertFormExists(wordforms, "Fobosiem");
        // Mēs gribam lai webserviss šo formu atgriež, bet tēzaura tabulu zīmētājs pēc tam to ignorē

        wordforms = inflectParRes.inflect(
                "miers", inflectParRes.decodeParadigm("1"), true, "", null, null, av);
        assertFormExists(wordforms, "mieram");
        assertFormDoesNotExist(wordforms, "mieriem");
    }

    @Test
    public void abēji() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_NumberSpecial, AttributeNames.v_PlurareTantum);
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "abēji", inflectParRes.decodeParadigm("13"), false, "", null, null, av);
        assertFormExists(wordforms, "abējiem");
    }

    @Test
    public void ticket_125() {
        // nez kāpēc nestrādā atpazīšana atsevišķiem vārdiem, ja tos padod ar lielo burtu
        Word w = CentralServer.getAnalyzer().analyze("krūšu");
        assertTrue(w.isRecognized());
        w = CentralServer.getAnalyzer().analyze("Krūšu");
        assertTrue(w.isRecognized());
        w.describe(System.out);
    }

    @Test
    public void paradigmNames() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "ceļš", inflectParRes.decodeParadigm( "noun-1b"), true, null, null, null, new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("ceļiem", wf.getToken());
        }
    }

    // https://github.com/PeterisP/morphology/issues/132
    @Test
    public void ticket_132() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "paģisties", inflectParRes.decodeParadigm("verb-1r"), true, "paģis", "paģied", "paģid", new AttributeValues());
        assertTrue("Jābūt vairāk kā vienai formai", wordforms.size()>1);
    }

    @Test
    public void abbrTokenization() {
        AttributeValues av = new AttributeValues();
        av.addAttribute(AttributeNames.i_Noliegums, AttributeNames.v_Yes);
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "P.S.", inflectParRes.decodeParadigm("abbr"), true, "", null, null, av);
        /*for (Collection<Wordform> wfs : wordforms) {
            for (Wordform wf : wfs)
                wf.describe();
        }*/
        assertFormDoesNotExist(wordforms, "P");
        assertFormDoesNotExist(wordforms, ".");
        assertFormExists(wordforms, "P.S.");
    }

    // 2.5.2 bija errors The connection was broken. It was probably closed by the client. Reason: Broken pipe
    @Test
    public void connectionBroken() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "paaut", inflectParRes.decodeParadigm("verb-1"), true, "paau", "paauj,paaun", "paāv", new AttributeValues());
        assertTrue("Jābūt vairāk kā vienai formai", wordforms.size()>1);
    }


    @Test
    public void latgalian() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "kuorklys", inflectParRes.decodeParadigm("noun-1b-ltg"), true, null, null, null, new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Dative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("kuorklim", wf.getToken());
        }
    }

    @Test
    public void latgalian_pasauļs() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "pasauļs", inflectParRes.decodeParadigm("noun-2a-ltg"), false, null, null, null, new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Accusative);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Singular);
        for (Wordform wf : wordforms) {
            wf.describe();
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("pasauli", wf.getToken());
        }
    }

    @Test
    public void latgalian_vargani() {
        Collection<Wordform> wordforms = inflectParRes.inflect(
                "vargani", inflectParRes.decodeParadigm("noun-1a-ltg"), false, "vargan", null, null, new AttributeValues());
        AttributeValues filtrs = new AttributeValues();
        filtrs.addAttribute(AttributeNames.i_PartOfSpeech, AttributeNames.v_Noun);
        filtrs.addAttribute(AttributeNames.i_Case, AttributeNames.v_Genitive);
        filtrs.addAttribute(AttributeNames.i_Number, AttributeNames.v_Plural);
        for (Wordform wf : wordforms) {
            if (!wf.isMatchingWeak(filtrs)) continue;
            assertEquals("varganu", wf.getToken());
        }
    }

    @Test
    public void latgalian_pronouns()
    {
        Collection<Wordform> es = inflectParRes.inflect(
                "es", inflectParRes.decodeParadigm("pron-ltg"), false, "", null, null, new AttributeValues());
        // Jābūt vismaz nominatīvam, ģenitīvam, datīvam, akuzatīvam un lokatīvam,
        // šīm formām jānāk no tēzaura.
        assertTrue(es.size() > 4);

        Collection<Wordform> šys = inflectParRes.inflect(
                "šys", inflectParRes.decodeParadigm("pron-ltg"), false, "", null, null, new AttributeValues());
        // Jābūt vismaz nominatīvam, ģenitīvam, datīvam, akuzatīvam un lokatīvam,
        // šīm formām jānāk no tēzaura.
        assertTrue(šys.size() > 4);
    }
}