package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;

public class GmkGame implements Game<GomokuGame>{
    private final int size;     // game board size
    public static final int DEFAUTL_SIZE = 15;

    public GmkGame(int size) {
        this.size = size;
    }

    public GmkGame() {
        this(DEFAUTL_SIZE);
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public int opener() {
        return 0;
    }

    @Override
    public GomokuState start() {
        return new GomokuState(this);
    }
}
