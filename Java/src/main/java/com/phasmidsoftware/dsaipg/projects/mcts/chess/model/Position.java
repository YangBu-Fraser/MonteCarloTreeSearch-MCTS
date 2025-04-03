package com.phasmidsoftware.dsaipg.projects.mcts.chess.model;

public class Position {
    private final int row;
    private final int col;

    /**
     * Initialization a position
     * @param row, val 0-7
     * @param col, val 0-7
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    //Initialization algebraic position.
    public static Position fromAlgebraic(String algrabic) {
        if (algrabic.length() != 2) throw new IllegalArgumentException("Position must be two characters long");

        char filechar = algrabic.charAt(0);
        char rankchar = algrabic.charAt(1);
        int col = filechar - 'a';
        int row = rankchar - '1';

        if (col < 0 || col > 7 || row < 0 || row > 7) {
            throw new IllegalArgumentException("Position must be between 0 and 7");
        }

        return new Position(col, row);
    }

    public String toAlgebraic() {
        char a = (char)('a' + col);
        char b = (char)('8' - row);
        return "" + a + b;
    }

    // Position check
    public boolean isValid() {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    // Generation new a position with offset
    public Position offset(int rowOffset, int colOffset) {
        return new Position(row + rowOffset, col + colOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return p.row == this.row && p.col == this.col;
    }

    @Override
    public int hashCode() {
        int cal = row;
        cal = 31 * cal + col;
        return cal;
    }

    public String toString() {
        return toAlgebraic();
    }
}
