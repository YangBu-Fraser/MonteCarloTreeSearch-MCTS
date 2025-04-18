package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the GmkNode class.
 */
public class GmkNodeTest {

    @Test
    public void testInitialNodeState() {
        GmkGame game = new GmkGame();
        GmkState state = game.start();
        GmkNode node = new GmkNode(state);

        assertTrue(node.isLeaf());
        assertEquals(0, node.wins());
        assertEquals(0, node.children().size());
    }

    @Test
    public void testAddChildManually() {
        GmkGame game = new GmkGame();
        GmkState parentState = game.start();
        GmkNode parentNode = new GmkNode(parentState);

        GmkState childState = new GmkState(game); // simulate a child
        parentNode.addChild(childState);

        assertEquals(1, parentNode.children().size());
    }
}
