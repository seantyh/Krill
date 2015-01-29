package de.ids_mannheim.korap.analysis;

import java.util.*;
import de.ids_mannheim.korap.analysis.MultiTermToken;
import java.io.IOException;
import org.apache.lucene.util.BytesRef;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class TestMultiTermToken {

    @Test
    public void multiTermTokenSimple () {
        MultiTermToken mtt = new MultiTermToken("t:test", "a:abbruch");
        assertEquals("[t:test|a:abbruch]", mtt.toString());
        mtt.add("b:banane");
        assertEquals("[t:test|a:abbruch|b:banane]", mtt.toString());
        mtt.add("c:chaos#21-26");
        assertEquals("[(21-26)t:test|a:abbruch|b:banane|c:chaos#21-26]",
                     mtt.toString());
        mtt.add("d:dadaismus#21-28$vergleich");
        assertEquals(
            "[(21-28)t:test|a:abbruch|b:banane|c:chaos#21-26|" +
            "d:dadaismus#21-28$vergleich]",
            mtt.toString()
        );
    };

    @Test
    public void multiTermTokenOffsets () {
        MultiTermToken mtt = new MultiTermToken("t:test#23-27");
        assertEquals("[(23-27)t:test#23-27]", mtt.toString());
        mtt.add("b:baum#34-45");
        assertEquals("[(23-45)t:test#23-27|b:baum#34-45]", mtt.toString());
        mtt.add("c:cannonball#34-45$tatsache");
        assertEquals("[(23-45)t:test#23-27|b:baum#34-45|" +
                     "c:cannonball#34-45$tatsache]", mtt.toString());
        assertEquals(23, mtt.start);
        assertEquals(45, mtt.end);
    };
};
