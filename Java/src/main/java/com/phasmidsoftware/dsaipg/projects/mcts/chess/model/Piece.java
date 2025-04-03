package com.phasmidsoftware.dsaipg.projects.mcts.chess.model;

import javax.swing.plaf.PanelUI;

public abstract class Piece {
    public enum Color { WHITE, BLACK; }

    public enum Type {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN;
    }

    private final Color color;
    private final Type type;
    private boolean hasMoved;

    /**
     * Initialization a new piece
     *
     * @param color
     * @param type
     */
    public Piece(Color color, Type type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public Color getColor() { return this.color; }
    private Type getType() { return this.type; }
    public boolean hasMoved() { return this.hasMoved; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    public char getSymbol() {
        char symbol;
        switch (type) {
            case KING: symbol = 'K';
            case QUEEN: symbol = 'Q';
            case ROOK: symbol = 'R';
            case BISHOP: symbol = 'B';
            case KNIGHT: symbol = 'N';
            case PAWN: symbol = 'P';
            default: symbol = ' ';
        }
        return color == Color.WHITE ? Character.toLowerCase(symbol) : symbol;
    }

    public int getValue() {
        return switch (type) {
            case KING -> 0;
            case QUEEN -> 9;
            case ROOK -> 5;
            case BISHOP -> 3;
            case KNIGHT -> 3;
            case PAWN -> 1;
        };
    }

    public abstract Piece copy();

    /**
     * Legal move for curren piece
     *
     * @param board
     * @param position
     * @return list of legal moves.
     */
    public abstract List<Move> getLegalMoves(ChessBoard board, Position position);

    public String toString() {
        return String.valueOf(getSymbol());
    }
}
