package de.ids_mannheim.korap.analysis;

import java.util.*;
import de.ids_mannheim.korap.analysis.MultiTermToken;
import de.ids_mannheim.korap.analysis.MultiTermTokenStream;
import java.io.IOException;
import org.apache.lucene.util.BytesRef;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class TestMultiTermTokenStream {
    @Test
    public void multiTermTokenStreamSimple () {
        MultiTermTokenStream ts = new MultiTermTokenStream();

        MultiTermToken mtt = new MultiTermToken("a:b#0-2");
        mtt.add("b:c");
        ts.addMultiTermToken(mtt);
        mtt = new MultiTermToken('c', "d");
        mtt.add('d', "e");
        ts.addMultiTermToken(mtt);
        assertEquals("[(0-2)a:b#0-2|b:c]"+
                     "[c:d|d:e]",
                     ts.toString());

        ts = new MultiTermTokenStream();

        mtt = new MultiTermToken('s', "Er#0-2");
        mtt.add('i', "er");
        mtt.add('p', "PPER");
        mtt.add('l', "er");
        mtt.add('m', "c:nom");
        mtt.add('m', "p:3");
        mtt.add('m', "n:sg");
        mtt.add('m', "g:masc");
        ts.addMultiTermToken(mtt);

        mtt = new MultiTermToken('s', "nahm");
        mtt.add('i', "nahm");
        mtt.add('p', "VVFIN$stts");
        mtt.add('l', "nehmen");
        mtt.add('m', "p:3#3-7");
        mtt.add('m', "n:sg");
        mtt.add('m', "t:past");
        mtt.add('m', "m:ind");
        ts.addMultiTermToken(mtt);

        assertEquals("[(0-2)s:Er#0-2|i:er|p:PPER|l:er|"+
                     "m:c:nom|m:p:3|m:n:sg|m:g:masc]"+
                     "[(3-7)s:nahm|i:nahm|p:VVFIN$stts|"+
                     "l:nehmen|m:p:3#3-7|m:n:sg|m:t:past|m:m:ind]",
                     ts.toString());

        ts.addMeta("paragraphs", 4);
        ts.addMeta("sentences", "34");

        assertEquals("[(0-2)s:Er#0-2|i:er|p:PPER|l:er|m:c:nom|"+
                     "m:p:3|m:n:sg|m:g:masc|"+
                     "-:paragraphs$   |-:sentences$34]"+
                     "[(3-7)s:nahm|i:nahm|p:VVFIN$stts|l:nehmen|"+
                     "m:p:3#3-7|m:n:sg|m:t:past|m:m:ind]",
                     ts.toString());

        ts = new MultiTermTokenStream(
            "[s:den#0-3|i:den|p:DET|l:der|m:c:acc|m:n:sg|m:masc]"
        );

        assertEquals(
            "[(0-3)s:den#0-3|i:den|p:DET|l:der|m:c:acc|m:n:sg|m:masc]",
            ts.toString()
        );
    };
};
