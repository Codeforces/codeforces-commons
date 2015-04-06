package com.codeforces.commons.collection;

import junit.framework.TestCase;

import java.util.*;

/**
 * @author Vitaliy Kudasov (kuviman@gmail.com)
 */
public class CollectionUtilTest extends TestCase {
    public void testCollectionComparator() {
        CollectionUtil.CollectionComparator<Integer> comparator = new CollectionUtil.CollectionComparator<Integer>();
        {
            List<Integer> a = Arrays.asList(7, 5, 11);
            List<Integer> b = Arrays.asList(7, 5, 8, 2);
            List<Integer> c = Arrays.asList(2, 5, 8, 9);
            List<Integer> d = Arrays.asList(1, 0, -100);
            List<Integer> e = Arrays.asList();
            List<Integer> f = Arrays.asList(1, 0);

            assertEquals(1, comparator.compare(a, b));
            assertEquals(0, comparator.compare(a, a));
            assertEquals(1, comparator.compare(b, c));
            assertEquals(1, comparator.compare(c, d));
            assertEquals(1, comparator.compare(d, e));

            assertEquals(-1, comparator.compare(e, a));
            assertEquals(-1, comparator.compare(e, b));
            assertEquals(-1, comparator.compare(e, c));
            assertEquals(-1, comparator.compare(e, d));

            assertEquals(0, comparator.compare(e, e));
            assertEquals(-1, comparator.compare(b, a));
            assertEquals(-1, comparator.compare(d, b));

            assertEquals(-1, comparator.compare(f, d));
            assertEquals(1, comparator.compare(d, f));

            Set<Integer> sa = new HashSet<>(a);
            Set<Integer> sb = new HashSet<>(b);
            assertTrue(comparator.compare(sa, sb) != 0);
        }
    }
}
