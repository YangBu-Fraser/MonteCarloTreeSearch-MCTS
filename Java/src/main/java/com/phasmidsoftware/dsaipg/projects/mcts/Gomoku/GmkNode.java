package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import java.util.*;

public class GmkNode implements Node<GmkGame> {
    private final GmkState state;
    private final List<Node<GmkGame>> children;
    private int wins;
    private int playouts;

    public GmkNode(GmkState state) {
        this.state = state;
        this.children = new ArrayList<>();
        this.wins = 0;
        this.playouts = 0;
    }

    @Override
    public boolean isLeaf() {
        return state.isTerminal() || children.isEmpty();
    }

    @Override
    public State<GmkGame> state() { return state; }

    @Override
    public List<Node<GmkGame>> children() { return children; }

    @Override
    public boolean white() { return state.player() == state.game().opener(); }

    @Override
    public int wins() { return wins; }

    @Override
    public void backPropagate() {
        if(isLeaf()) return;
        playouts = 0;
        wins = 0;
        for (Node<GmkGame> child : children) {
            wins += child.wins();
            playouts += child.playouts();
        }
    }

    @Override
    public void addChild(State<GmkGame> state) {
        if(!(state instanceof GmkState gmkState)) {
            throw new IllegalArgumentException("GmkNode.addChild: state is not a GmkState");
        }
        GmkNode child = new GmkNode(gmkState);
        children.add(child);
    }

    @Override
    public int playouts() { return playouts; }

    public void update(boolean win) {
        if (win) wins++;
        playouts++;
    }
}
