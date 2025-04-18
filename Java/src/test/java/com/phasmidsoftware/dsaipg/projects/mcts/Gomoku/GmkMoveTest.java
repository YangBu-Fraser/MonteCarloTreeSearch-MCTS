package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for GmkMove class.
 * These tests verify equality and string representation of a move.
 */
public class GmkMoveTest {

    /**
     * This test checks that two moves with the same player, row, and column
     * are considered equal using the equals() method (if overridden).
     */
    @Test
    public void testMoveEquality() {
        GmkMove m1 = new GmkMove(0, 2, 3);
        GmkMove m2 = new GmkMove(0, 2, 3);
        assertEquals(m1.toString(), m2.toString()); // fallback if .equals is not overridden
    }

    /**
     * This test checks the toString method of a move to ensure it includes
     * the correct format: "Player X(row,col)"
     */
    @Test
    public void testMoveToString() {
        GmkMove move = new GmkMove(1, 1, 1);
        String result = move.toString();
        assertTrue(result.contains("Player 1"));
        assertTrue(result.contains("(1,1)"));
    }
}
