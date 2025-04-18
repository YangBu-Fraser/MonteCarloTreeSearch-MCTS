package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the GmkGame class.
 * These tests validate basic game setup and move generation for Gomoku.
 */
public class GmkGameTest {

    @Test
    public void testStartStateNotNull() {
        GmkGame game = new GmkGame();
        GmkState initialState = game.start();
        assertNotNull(initialState);
    }

    @Test
    public void testMovesNotEmpty() {
        GmkGame game = new GmkGame();
        GmkState state = game.start();
        assertFalse(state.moves(0).isEmpty());  // 0 = player one
    }

}
