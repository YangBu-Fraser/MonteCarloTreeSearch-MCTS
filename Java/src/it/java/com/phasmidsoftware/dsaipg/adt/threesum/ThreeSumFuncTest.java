package com.phasmidsoftware.dsaipg.adt.threesum;

import org.junit.Ignore;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class ThreeSumFuncTest {

    @Ignore // Slow
    public void testGetTriples3() {
        Supplier<int[]> intsSupplier = new Source(1000, 1000).intsSupplier(10);
        int[] ints = intsSupplier.get();
        ThreeSum target = new ThreeSumQuadratic(ints);
        Triple[] triplesQuadratic = target.getTriples();
        Triple[] triplesCubic = new ThreeSumCubic(ints).getTriples();
        int expected1 = triplesCubic.length;
        assertEquals(expected1, triplesQuadratic.length);
    }

    @Ignore // Slow
    public void testGetTriples4() {
        Supplier<int[]> intsSupplier = new Source(1500, 1000).intsSupplier(10);
        int[] ints = intsSupplier.get();
        ThreeSum target = new ThreeSumQuadratic(ints);
        Triple[] triplesQuadratic = target.getTriples();
        Triple[] triplesCubic = new ThreeSumCubic(ints).getTriples();
        int expected1 = triplesCubic.length;
        assertEquals(expected1, triplesQuadratic.length);
    }
}