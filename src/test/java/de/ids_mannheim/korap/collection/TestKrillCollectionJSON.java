package de.ids_mannheim.korap.collection;

import java.util.*;
import java.io.*;

import de.ids_mannheim.korap.KrillCollection;
import de.ids_mannheim.korap.Krill;
import de.ids_mannheim.korap.KrillIndex;
import de.ids_mannheim.korap.response.Result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import static de.ids_mannheim.korap.TestSimple.*;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestKrillCollectionJSON {

    final String path = "/queries/collections/";


    @Test
    public void collection1 () {
        String metaQuery = _getJSONString("collection_1.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertEquals(kc.toString(), "pubDate:[20000101 TO 20000101]");
    };


    @Test
    public void collection2 () {
        String metaQuery = _getJSONString("collection_2.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertEquals(kc.toString(),
                "AndGroup(pubDate:[19900000 TO 99999999] pubDate:[0 TO 20061099])");
    };


    @Test
    public void collection3 () {
        String metaQuery = _getJSONString("collection_3.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertEquals(kc.toString(), "");
    };


    @Test
    public void collection5 () {
        String metaQuery = _getJSONString("collection_5.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertEquals(kc.toString(),
                "OrGroup(pubDate:[19900000 TO 99999999] title:Mannheim)");
    };


    @Test
    public void collectionWithRegex () {
        String query = _getJSONString("collection_7.jsonld");
        Krill ks = new Krill(query);
        assertFalse(ks.hasErrors());
        assertFalse(ks.hasWarnings());
        assertFalse(ks.hasMessages());
        assertEquals("QueryWrapperFilter(author:/Goethe/)", ks.getCollection()
                .toString());
    };


    @Test
    public void collectionWithNegativeRegex () {
        String query = _getJSONString("collection_negregex.jsonld");
        Krill ks = new Krill(query);
        assertFalse(ks.hasErrors());
        assertFalse(ks.hasWarnings());
        assertFalse(ks.hasMessages());
        assertEquals("-QueryWrapperFilter(author:/Goethe/)", ks.getCollection()
                .toString());
    };


    @Test
    public void collectionWithNegativeString () {
        String query = _getJSONString("collection_ne.jsonld");
        Krill ks = new Krill(query);
        assertFalse(ks.hasErrors());
        assertFalse(ks.hasWarnings());
        assertFalse(ks.hasMessages());
        assertEquals("-author:Goethe", ks.getCollection().toString());
    };


    @Test
    public void collectionWithNegativeContainment () {
        String query = _getJSONString("collection_containsnot.jsonld");
        Krill ks = new Krill(query);
        assertFalse(ks.hasErrors());
        assertFalse(ks.hasWarnings());
        assertFalse(ks.hasMessages());
        assertEquals("-author:goethe", ks.getCollection().toString());
    };


    @Test
    public void nocollectiontypegiven () {
        String metaQuery = _getJSONString("multiterm_rewrite_collection.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertEquals(701, kc.getError(0).getCode());
    };


    @Test
    public void noCollection () {
        String metaQuery = _getJSONString("no_collection.jsonld");
        KrillCollection kc = new KrillCollection(metaQuery);
        assertTrue(kc.hasErrors());
        assertEquals("", kc.toString());
    };


    @Test
    public void collectionMirror () throws Exception {
        // Construct index
        KrillIndex ki = new KrillIndex();
        // Indexing test files
        for (String i : new String[] { "00001", "00002", "00003", "00004",
                "00005", "00006", "02439" }) {
            ki.addDoc(
                    getClass().getResourceAsStream("/wiki/" + i + ".json.gz"),
                    true);
        };
        ki.commit();

        Krill krill = new Krill(_getJSONString("collection_6.jsonld"));
        Result kr = krill.apply(ki);
        assertEquals(kr.getTotalResults(), 86);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode res = mapper.readTree(kr.toJsonString());
        assertTrue(res.at("/collection").isMissingNode());

        krill = new Krill(_getJSONString("collection_6_withCollection.jsonld"));

        kr = krill.apply(ki);
        assertEquals(kr.getTotalResults(), 57);

        res = mapper.readTree(kr.toJsonString());
        assertFalse(res.at("/collection").isMissingNode());
        assertEquals("koral:doc", res.at("/collection/@type").asText());
        assertEquals("textClass", res.at("/collection/key").asText());
        assertEquals("reisen", res.at("/collection/value").asText());
        assertEquals("match:eq", res.at("/collection/match").asText());
    };



    // Legacy collections reflect old tests, that were adopted to the new scheme
    @Test
    public void metaQuery1Legacy () {
        String metaQuery = getString(getClass().getResource(
                "/queries/metaquery.jsonld").getFile());
        KrillCollection kc = new KrillCollection(metaQuery);

        /*
        assertEquals("filter with QueryWrapperFilter(+textClass:wissenschaft)",
                kc.getFilter(0).toString());
        assertEquals(
                "filter with QueryWrapperFilter(+(+pubPlace:Erfurt +author:Hesse))",
                kc.getFilter(1).toString());
        assertEquals(
                "extend with QueryWrapperFilter(+(+pubDate:[20110429 TO 20131231] +textClass:freizeit))",
                kc.getFilter(2).toString());
        assertEquals(3, kc.getCount());
        */

        // This will and should fail on optimization
        assertEquals(
                "OrGroup(AndGroup(textClass:wissenschaft AndGroup(pubPlace:Erfurt author:Hesse)) AndGroup(AndGroup(pubDate:[20110429 TO 99999999] pubDate:[0 TO 20131231]) textClass:freizeit))",
                kc.toString());
    };


    @Test
    public void metaQuery2Legacy () {
        String metaQuery = getString(getClass().getResource(
                "/queries/metaquery2.jsonld").getFile());
        KrillCollection kc = new KrillCollection(metaQuery);
        /*
        assertEquals(1, kc.getCount());
        assertEquals(
                "filter with QueryWrapperFilter(+(+author:Hesse +pubDate:[0 TO 20131205]))",
                kc.getFilter(0).toString());
        */
        assertEquals("AndGroup(author:Hesse pubDate:[0 TO 20131205])",
                kc.toString());
    };


    @Test
    public void metaQuery3Legacy () {
        String metaQuery = getString(getClass().getResource(
                "/queries/metaquery4.jsonld").getFile());
        KrillCollection kc = new KrillCollection(metaQuery);
        /*
        assertEquals(1, kc.getCount());
        assertEquals(
                     // "filter with QueryWrapperFilter(+pubDate:[20000101 TO 20131231])"
                     "filter with QueryWrapperFilter(+(+pubDate:[20000101 TO 99999999] +pubDate:[0 TO 20131231]))",
                kc.getFilter(0).toString());
        */
        assertEquals(
                "AndGroup(pubDate:[20000101 TO 99999999] pubDate:[0 TO 20131231])",
                kc.toString());
    };


    @Test
    public void metaQuery7Legacy () {
        String metaQuery = getString(getClass().getResource(
                "/queries/metaquery7.jsonld").getFile());
        KrillCollection kc = new KrillCollection(metaQuery);
        /*
        assertEquals(2, kc.getCount());
        assertEquals(
                "filter with QueryWrapperFilter(+(corpusID:c-1 corpusID:c-2))",
                kc.getFilter(0).toString());
        assertEquals(
                "filter with QueryWrapperFilter(+(+corpusID:d-1 +corpusID:d-2))",
                kc.getFilter(1).toString());
        */
        // TODO: This is subject to optimization and may change in further versions
        assertEquals(
                "AndGroup(OrGroup(corpusID:c-1 corpusID:c-2) AndGroup(corpusID:d-1 corpusID:d-2))",
                kc.toString());
    };


    @Test
    public void metaQuery9 () {
        String metaQuery = getString(getClass().getResource(
                "/queries/metaquery9.jsonld").getFile());
        KrillCollection kc = new KrillCollection(metaQuery);
        /*
        assertEquals(1, kc.getCount());
        assertEquals("filter with QueryWrapperFilter(+corpusID:WPD)", kc
                .getFilter(0).toString());
        */
        assertEquals("corpusID:WPD", kc.toString());
    };


    private String _getJSONString (String file) {
        return getString(getClass().getResource(path + file).getFile());
    };
};
