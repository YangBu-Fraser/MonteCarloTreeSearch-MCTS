package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;

public class GmkMove implements Move<GmkGame> {
    private final int player;
    private final int row;
    private final int col;

    /**
     * @param player , 0 - black and 1 - white
     * @param row
     * @param col
     */
    public GmkMove(int player, int row, int col) {
        this.player = player;
        this.row = row;
        this.col = col;
    }

    @Override
    public int player() {
        return player;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public String toString() {
        return "Player " + player + "(" + row + "," + col + ")";
    }

}
