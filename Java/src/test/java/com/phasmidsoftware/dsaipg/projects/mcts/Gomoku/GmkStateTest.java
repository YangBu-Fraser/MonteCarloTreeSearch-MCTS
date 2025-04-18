package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the GmkState class.
 * These tests verify starting state behavior, move options, and win status.
 */
public class GmkStateTest {

    /**
     * Verifies that the game starts with no winner.
     */
    @Test
    public void testWinnerInitiallyEmpty() {
        GmkGame game = new GmkGame();
        GmkState state = game.start();
        assertFalse(state.winner().isPresent());
    }

    /**
     * Verifies that the game is not in a terminal (win/draw) state at the beginning.
     */
    @Test
    public void testNotTerminalAtStart() {
        GmkGame game = new GmkGame();
        GmkState state = game.start();
        assertFalse(state.isTerminal());
    }

    /**
     * Verifies that valid moves exist for the first player (player 0) at game start.
     */
    @Test
    public void testHasMovesForPlayerZero() {
        GmkGame game = new GmkGame();
        GmkState state = game.start();
        assertFalse(state.moves(0).isEmpty());
    }
}
