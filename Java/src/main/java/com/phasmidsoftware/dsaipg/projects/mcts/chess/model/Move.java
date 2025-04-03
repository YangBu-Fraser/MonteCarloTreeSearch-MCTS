package com.phasmidsoftware.dsaipg.projects.mcts.chess.model;

public class Move {

    private final Position to;
    private final Position from;
    private final Piece.Type promotionType;
    private final boolean isCapture;
    private final boolean isCastling;
    private final boolean isEnPassant;

    public Move(Position from, Position to){
        this(to, from, null, false, false, false);
    }

    /**
     * Creates a new move with additional flags.
     *
     * @param from the starting position
     * @param to the destination position
     * @param promotionType the promotion piece type (null if no promotion)
     * @param isCapture true if this move captures a piece
     * @param isCastling true if this move is a castling move
     * @param isEnPassant true if this move is an en passant capture
     */
    public Move(Position from, Position to, Piece.Type promotionType,
                boolean isCapture, boolean isCastling, boolean isEnPassant) {
        this.from = from;
        this.to = to;
        this.promotionType = promotionType;
        this.isCapture = isCapture;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
    }

}
