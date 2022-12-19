package lv.semti.morphology.webservice;

import org.junit.Ignore;

import static org.junit.Assert.*;

public class TezaursWordResourceTest {

    @Ignore
    @org.junit.Test
    public void testGetEntries() throws Exception {
        TezaursWordResource.getEntries();
        String doma = TezaursWordResource.getEntries().get("doma");
//        System.out.println(doma);
        assertTrue(doma.contains("Jumtveidīga kristāla forma"));
        assertTrue(doma.contains("Domāšanas rezultāts"));
    }
}