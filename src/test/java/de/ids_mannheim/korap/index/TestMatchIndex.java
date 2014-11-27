package de.ids_mannheim.korap.index;

import java.util.*;
import java.io.*;

import org.apache.lucene.util.Version;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Bits;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.ids_mannheim.korap.KorapIndex;
import de.ids_mannheim.korap.KorapQuery;
import de.ids_mannheim.korap.KorapMatch;
import de.ids_mannheim.korap.KorapResult;
import de.ids_mannheim.korap.query.SpanNextQuery;
import de.ids_mannheim.korap.query.SpanElementQuery;
import de.ids_mannheim.korap.query.SpanWithinQuery;
import de.ids_mannheim.korap.query.SpanMatchModifyClassQuery;
import de.ids_mannheim.korap.query.SpanClassQuery;
import de.ids_mannheim.korap.index.FieldDocument;
import de.ids_mannheim.korap.analysis.MultiTermTokenStream;

import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.index.Term;

// mvn -Dtest=TestWithinIndex#indexExample1 test

// match is shrink and split

@RunWith(JUnit4.class)
public class TestMatchIndex {

    @Test
    public void indexExample1 () throws IOException {
	KorapIndex ki = new KorapIndex();

	// abcabcabac
	FieldDocument fd = new FieldDocument();
	fd.addTV("base",
		 "abcabcabac",
		 "[(0-1)s:a|i:a|_0#0-1|-:t$<i>10]" +
		 "[(1-2)s:b|i:b|_1#1-2]" +
		 "[(2-3)s:c|i:c|_2#2-3]" +
		 "[(3-4)s:a|i:a|_3#3-4]" +
		 "[(4-5)s:b|i:b|_4#4-5]" +
		 "[(5-6)s:c|i:c|_5#5-6]" +
		 "[(6-7)s:a|i:a|_6#6-7]" +
		 "[(7-8)s:b|i:b|_7#7-8]" +
		 "[(8-9)s:a|i:a|_8#8-9]" +
		 "[(9-10)s:c|i:c|_9#9-10]");
	ki.addDoc(fd);

	ki.commit();

	SpanQuery sq;
	KorapResult kr;

	sq = new SpanNextQuery(
            new SpanTermQuery(new Term("base", "s:b")),
            new SpanClassQuery(
              new SpanTermQuery(new Term("base", "s:a"))
	    )
        );
	kr = ki.search(sq, (short) 10);

	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("StartPos (0)", 7, kr.getMatch(0).startPos);
	assertEquals("EndPos (0)", 9, kr.getMatch(0).endPos);
	assertEquals("SnippetBrackets (0)", "... bcabca[b{1:a}]c", kr.getMatch(0).snippetBrackets());

	assertEquals("Test no 'more' context", "<span class=\"context-left\"><span class=\"more\"></span>bcabca</span><span class=\"match\">b<em class=\"class-1 level-0\">a</em></span><span class=\"context-right\">c</span>", kr.getMatch(0).snippetHTML());
	sq = new SpanMatchModifyClassQuery(
  	     new SpanNextQuery(
                new SpanTermQuery(new Term("base", "s:b")),
                new SpanClassQuery(
                    new SpanTermQuery(new Term("base", "s:a"))
	        )
            )
	);
	kr = ki.search(sq, (short) 10);

	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("StartPos (0)", 8, kr.getMatch(0).startPos);
	assertEquals("EndPos (0)", 9, kr.getMatch(0).endPos);
	assertEquals("SnippetBrackets (0)", "... cabcab[{1:a}]c", kr.getMatch(0).snippetBrackets());
	sq = new SpanMatchModifyClassQuery(
            new SpanNextQuery(
	        new SpanClassQuery(new SpanTermQuery(new Term("base", "s:a")), (byte) 2),
                new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 3)
            ), (byte) 3
        );

	kr = ki.search(sq, (short) 10);

	assertEquals("totalResults", kr.getTotalResults(), 3);
	assertEquals("StartPos (0)", 1, kr.getMatch(0).startPos);
	assertEquals("EndPos (0)", 2, kr.getMatch(0).endPos);
	assertEquals("SnippetBrackets (0)", "a[{3:b}]cabcab ...", kr.getMatch(0).snippetBrackets());
	

	assertEquals("<span class=\"context-left\">a</span><span class=\"match\"><em class=\"class-3 level-0\">b</em></span><span class=\"context-right\">cabcab<span class=\"more\"></span></span>", kr.getMatch(0).snippetHTML());

	assertEquals("StartPos (1)", 4, kr.getMatch(1).startPos);
	assertEquals("EndPos (1)", 5, kr.getMatch(1).endPos);
	assertEquals("SnippetBrackets (1)", "abca[{3:b}]cabac", kr.getMatch(1).snippetBrackets());

	assertEquals("<span class=\"context-left\">abca</span><span class=\"match\"><em class=\"class-3 level-0\">b</em></span><span class=\"context-right\">cabac</span>", kr.getMatch(1).snippetHTML());

	assertEquals("StartPos (2)", 7, kr.getMatch(2).startPos);
	assertEquals("EndPos (2)", 8, kr.getMatch(2).endPos);
	assertEquals("SnippetBrackets (2)", "... bcabca[{3:b}]ac", kr.getMatch(2).snippetBrackets());



	// abcabcabac
	sq = new SpanMatchModifyClassQuery( 
            new SpanNextQuery(
                new SpanTermQuery(new Term("base", "s:a")),
                new SpanClassQuery(
	            new SpanNextQuery(
	   	        new SpanTermQuery(new Term("base", "s:b")),
	    	        new SpanClassQuery(new SpanTermQuery(new Term("base", "s:a")))
		    ), (byte) 2
        )), (byte) 2);

	kr = ki.search(sq, (short) 10);

	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("SnippetBrackets (0)", "... bcabca[{2:b{1:a}}]c", kr.getMatch(0).snippetBrackets());

	assertEquals("SnippetHTML (0) 1", "<span class=\"context-left\"><span class=\"more\"></span>bcabca</span><span class=\"match\"><em class=\"class-2 level-0\">b<em class=\"class-1 level-1\">a</em></em></span><span class=\"context-right\">c</span>", kr.getMatch(0).snippetHTML());

	// Offset tokens
	kr = ki.search(sq, 0, (short) 10, true, (short) 2, true, (short) 2);
	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("SnippetBrackets (0)", "... ca[{2:b{1:a}}]c", kr.getMatch(0).snippetBrackets());



	// Offset Characters
	kr = ki.search(sq, 0, (short) 10, false, (short) 1, false, (short) 0);
	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("SnippetBrackets (0)", "... a[{2:b{1:a}}] ...", kr.getMatch(0).snippetBrackets());

	assertEquals("SnippetHTML (0) 2", "<span class=\"context-left\"><span class=\"more\"></span>a</span><span class=\"match\"><em class=\"class-2 level-0\">b<em class=\"class-1 level-1\">a</em></em></span><span class=\"context-right\"><span class=\"more\"></span></span>", kr.getMatch(0).snippetHTML());

	sq = new SpanMatchModifyClassQuery(
            new SpanClassQuery(
                new SpanNextQuery(
	            new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 1),
                    new SpanClassQuery(new SpanTermQuery(new Term("base", "s:c")), (byte) 2)
		), (byte) 3
            ), (byte) 3
	);

	kr = ki.search(sq, (short) 10);
	
	assertEquals("totalResults", kr.getTotalResults(), 2);
	assertEquals("StartPos (0)", 1, kr.getMatch(0).startPos);
	assertEquals("EndPos (0)", 3, kr.getMatch(0).endPos);
	assertEquals("SnippetBrackets (0)", "a[{3:{1:b}{2:c}}]abcaba ...", kr.getMatch(0).snippetBrackets());
	assertEquals("StartPos (1)", 4, kr.getMatch(1).startPos);
	assertEquals("EndPos (1)", 6, kr.getMatch(1).endPos);
	assertEquals("SnippetBrackets (1)", "abca[{3:{1:b}{2:c}}]abac", kr.getMatch(1).snippetBrackets());

	assertEquals("Document count", 1, ki.numberOf("base", "documents"));
	assertEquals("Token count", 10, ki.numberOf("base", "t"));

	// Don't match the expected class!
	sq = new SpanMatchModifyClassQuery(
            new SpanNextQuery(
	        new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 1),
		new SpanClassQuery(new SpanTermQuery(new Term("base", "s:c")), (byte) 2)
	    ), (byte) 3
	);

	kr = ki.search(sq, (short) 10);
	
	assertEquals("totalResults", kr.getTotalResults(), 0);

	sq = new SpanMatchModifyClassQuery(
            new SpanNextQuery(
                new SpanTermQuery(new Term("base", "s:a")),
                new SpanClassQuery(
	            new SpanNextQuery(
	                new SpanTermQuery(new Term("base", "s:b")),
	                new SpanTermQuery(new Term("base", "s:c"))
  	            )
	       )
            )
        );

	kr = ki.search(sq, (short) 2);
	
	assertEquals("totalResults", kr.getTotalResults(), 2);
	assertEquals("StartPos (0)", 1, kr.getMatch(0).startPos);
	assertEquals("EndPos (0)", 3, kr.getMatch(0).endPos);
	assertEquals("SnippetBrackets (0)", "a[{1:bc}]abcaba ...", kr.getMatch(0).snippetBrackets());
	assertEquals("StartPos (1)", 4, kr.getMatch(1).startPos);
	assertEquals("EndPos (1)", 6, kr.getMatch(1).endPos);
	assertEquals("SnippetBrackets (1)", "abca[{1:bc}]abac", kr.getMatch(1).snippetBrackets());

	assertEquals(1, ki.numberOf("base", "documents"));
	assertEquals(10, ki.numberOf("base", "t"));
    };


    @Test
    public void indexExample2 () throws IOException {
	KorapIndex ki = new KorapIndex();

	// abcabcabac
	FieldDocument fd = new FieldDocument();
	fd.addTV("base",
		 "abcabcabac",
		 "[(0-1)s:a|i:a|_0#0-1|-:t$<i>10]" +
		 "[(1-2)s:b|i:b|_1#1-2]" +
		 "[(2-3)s:c|i:c|_2#2-3]" +
		 "[(3-4)s:a|i:a|_3#3-4]" +
		 "[(4-5)s:b|i:b|_4#4-5]" +
		 "[(5-6)s:c|i:c|_5#5-6]" +
		 "[(6-7)s:a|i:a|_6#6-7]" +
		 "[(7-8)s:b|i:b|_7#7-8]" +
		 "[(8-9)s:a|i:a|_8#8-9]" +
		 "[(9-10)s:c|i:c|_9#9-10]");
	ki.addDoc(fd);

	ki.commit();

	SpanQuery sq;
	KorapResult kr;

	// No contexts:
	sq = new SpanOrQuery(
            new SpanTermQuery(new Term("base", "s:a")),
	    new SpanTermQuery(new Term("base", "s:c"))
        );
	kr = ki.search(sq, (short) 20);

	assertEquals("totalResults", kr.getTotalResults(), 7);
	assertEquals("SnippetBrackets (0)", "<span class=\"context-left\"></span><span class=\"match\">a</span><span class=\"context-right\">bcabca<span class=\"more\"></span></span>", kr.getMatch(0).snippetHTML());
	assertEquals("SnippetBrackets (0)", "[a]bcabca ...", kr.getMatch(0).snippetBrackets());

	assertEquals("SnippetBrackets (1)", "ab[c]abcaba ...", kr.getMatch(1).snippetBrackets());
	assertEquals("SnippetBrackets (1)", "<span class=\"context-left\">ab</span><span class=\"match\">c</span><span class=\"context-right\">abcaba<span class=\"more\"></span></span>", kr.getMatch(1).snippetHTML());

	assertEquals("SnippetBrackets (6)", "... abcaba[c]", kr.getMatch(6).snippetBrackets());
	assertEquals("SnippetBrackets (6)", "<span class=\"context-left\"><span class=\"more\"></span>abcaba</span><span class=\"match\">c</span><span class=\"context-right\"></span>", kr.getMatch(6).snippetHTML());

	kr = ki.search(sq, 0, (short) 20, true, (short) 0, true, (short) 0);

	assertEquals("totalResults", kr.getTotalResults(), 7);
	assertEquals("SnippetBrackets (0)", "[a] ...", kr.getMatch(0).snippetBrackets());
	assertEquals("SnippetHTML (0)", "<span class=\"context-left\"></span><span class=\"match\">a</span><span class=\"context-right\"><span class=\"more\"></span></span>", kr.getMatch(0).snippetHTML());

	assertEquals("SnippetBrackets (1)", "... [c] ...", kr.getMatch(1).snippetBrackets());
	assertEquals("SnippetHTML (1)", "<span class=\"context-left\"><span class=\"more\"></span></span><span class=\"match\">c</span><span class=\"context-right\"><span class=\"more\"></span></span>", kr.getMatch(1).snippetHTML());

	assertEquals("SnippetBrackets (6)", "... [c]", kr.getMatch(6).snippetBrackets());
	assertEquals("SnippetBrackets (6)", "<span class=\"context-left\"><span class=\"more\"></span></span><span class=\"match\">c</span><span class=\"context-right\"></span>", kr.getMatch(6).snippetHTML());
    };


    @Test
    public void indexExample3 () throws Exception {
	KorapIndex ki = new KorapIndex();

	// abcabcabac
	FieldDocument fd = new FieldDocument();
	fd.addTV("base",
		 "abcabcabac",
		 "[(0-1)s:a|i:a|_0#0-1|-:t$<i>10]" +
		 "[(1-2)s:b|i:b|_1#1-2]" +
		 "[(2-3)s:c|i:c|_2#2-3]" +
		 "[(3-4)s:a|i:a|_3#3-4]" +
		 "[(4-5)s:b|i:b|_4#4-5]" +
		 "[(5-6)s:c|i:c|_5#5-6]" +
		 "[(6-7)s:a|i:a|_6#6-7]" +
		 "[(7-8)s:b|i:b|_7#7-8]" +
		 "[(8-9)s:a|i:a|_8#8-9]" +
		 "[(9-10)s:c|i:c|_9#9-10]");
	ki.addDoc(fd);

	ki.commit();

	KorapResult kr;

	KorapQuery kq = new KorapQuery("base");

	SpanQuery sq = kq._(1,kq.seq(kq.seg("s:b")).append(kq.seg("s:a")).append(kq._(2,kq.seg("s:c")))).toQuery();

	kr = ki.search(sq, 0, (short) 20, true, (short) 2, true, (short) 5);

	assertEquals("totalResults", kr.getTotalResults(), 1);
	assertEquals("SnippetBrackets (0)", "... ca[{1:ba{2:c}}]", kr.getMatch(0).snippetBrackets());
    };


    @Test
    public void indexExampleExtend () throws IOException {
	KorapIndex ki = new KorapIndex();

	// abcabcabac
	FieldDocument fd = new FieldDocument();
	fd.addTV("base",
		 "abcabcabac",
		 "[(0-1)s:a|i:a|_0#0-1|-:t$<i>10]" +
		 "[(1-2)s:b|i:b|_1#1-2]" +
		 "[(2-3)s:c|i:c|_2#2-3]" +
		 "[(3-4)s:a|i:a|_3#3-4]" +
		 "[(4-5)s:b|i:b|_4#4-5]" +
		 "[(5-6)s:c|i:c|_5#5-6]" +
		 "[(6-7)s:a|i:a|_6#6-7]" +
		 "[(7-8)s:b|i:b|_7#7-8]" +
		 "[(8-9)s:a|i:a|_8#8-9]" +
		 "[(9-10)s:c|i:c|_9#9-10]");
	ki.addDoc(fd);

	ki.commit();

	SpanQuery sq;
	KorapResult kr;

	sq = new SpanMatchModifyClassQuery(
            new SpanNextQuery(
	        new SpanClassQuery(new SpanTermQuery(new Term("base", "s:a")), (byte) 2),
                new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 3)
            ), (byte) 3
        );

	kr = ki.search(sq, (short) 10);

	assertEquals("totalResults", kr.getTotalResults(), 3);

	KorapMatch km = kr.getMatch(0);
	assertEquals("StartPos (0)", 1, km.startPos);
	assertEquals("EndPos (0)", 2, km.endPos);
	assertEquals("SnippetBrackets (0)", "a[{3:b}]cabcab ...", km.getSnippetBrackets());

	sq = new SpanMatchModifyClassQuery(
	    new SpanMatchModifyClassQuery(
                new SpanNextQuery(
	            new SpanClassQuery(new SpanTermQuery(new Term("base", "s:a")), (byte) 2),
                    new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 3)
                ), (byte) 3
	    ), (byte) 2
	);
	
	kr = ki.search(sq, (short) 10);

	km = kr.getMatch(0);
	assertEquals("StartPos (0)", 0, km.startPos);
	assertEquals("EndPos (0)", 1, km.endPos);
	assertEquals("SnippetBrackets (0)", "[{2:a}]bcabca ...", km.getSnippetBrackets());


	// TODO: Check ID
    };


    @Test
    public void indexExampleShrinkWithSpan () throws IOException {
	KorapIndex ki = new KorapIndex();

	// abcabcabac
	FieldDocument fd = new FieldDocument();
	fd.addTV("base",
		 "abcabcabac",
		 "[(0-1)s:a|i:a|_0#0-1|-:t$<i>10]" +
		 "[(1-2)s:b|i:b|_1#1-2|<>:s#1-5$<i>5]" +
		 "[(2-3)s:c|i:c|_2#2-3|<>:s#2-7$<i>7]" +
		 "[(3-4)s:a|i:a|_3#3-4]" +
		 "[(4-5)s:b|i:b|_4#4-5]" +
		 "[(5-6)s:c|i:c|_5#5-6]" +
		 "[(6-7)s:a|i:a|_6#6-7]" +
		 "[(7-8)s:b|i:b|_7#7-8]" +
		 "[(8-9)s:a|i:a|_8#8-9]" +
		 "[(9-10)s:c|i:c|_9#9-10]");
	ki.addDoc(fd);

	ki.commit();

	SpanQuery sq;
	KorapResult kr;

	sq = new SpanWithinQuery(
	    new SpanClassQuery(new SpanElementQuery("base", "s"), (byte) 2),
            new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 3)
        );

	kr = ki.search(sq, (short) 10);
	assertEquals(kr.getQuery(), "spanContain({2: <base:s />}, {3: base:s:b})");
	assertEquals(kr.getMatch(0).getSnippetBrackets(), "a[{2:{3:b}cab}]cabac");

	sq = new SpanMatchModifyClassQuery(
            new SpanWithinQuery(
	        new SpanClassQuery(new SpanElementQuery("base", "s"), (byte) 2),
                new SpanClassQuery(new SpanTermQuery(new Term("base", "s:b")), (byte) 3)
            ), (byte) 3
        );

	kr = ki.search(sq, (short) 10);
	assertEquals(kr.getQuery(), "shrink(3: spanContain({2: <base:s />}, {3: base:s:b}))");
	assertEquals(kr.getMatch(0).getSnippetBrackets(), "a[{3:b}]cabcab ...");
    };
};
