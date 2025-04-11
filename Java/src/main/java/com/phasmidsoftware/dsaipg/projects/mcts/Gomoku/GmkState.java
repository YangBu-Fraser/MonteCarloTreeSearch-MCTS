package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import java.util.*;

public class GmkState implements State<GmkGame> {
    private final GmkGame game;
    private final int[][] board;
    private final int lastMovePlayer;
    private final Random random;
    private final List<GmkMove> moveHistory;

    public GmkState(GmkGame game) {
        this.game = game;
        int size = game.getSize();
        this.board = new int[size][size];
        this.lastMovePlayer = 1;    // the white player is the last turn to move
        this.random = new Random();
        this.moveHistory = new ArrayList<>();
    }

    private GmkState(GmkGame game, int[][] board, int lastMovePlayer,
                     Random random, List<GmkMove> moveHistory) {
        this.game = game;
        this.board = board;
        this.lastMovePlayer = lastMovePlayer;
        this.random = random;
        this.moveHistory = moveHistory;
    }

    @Override
    public GmkGame game() {
        return game;
    }

    @Override
    public boolean isTerminal() {
        if (winner().isPresent()) return true;

        for (int i = 0; i < game.getSize(); i++) {
            for (int j = 0; j < game.getSize(); j++) {
                if (board[i][j] == 0) return false;
            }
        }
        return true;
    }

    @Override
    public int player() {
        return (lastMovePlayer + 1) % 2;
    }

    @Override
    public Optional<Integer> winner() {
        int size = game.getSize();

        // Horizontal checks
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int player = board[row][col];
                if (player != 0 &&
                player == board[row][col+1]&&
                        player == board[row][col+2]&&
                        player == board[row][col+3]&&
                        player == board[row][col+4]) {
                    return Optional.of(player-1);
                }
            }
        }

        // Vertical checks
        for (int row = 0; row <= size - 5; row++) {
            for (int col = 0; col < size; col++) {
                int player = board[row][col];
                if (player != 0 &&
                        player == board[row+1][col] &&
                        player == board[row+2][col] &&
                        player == board[row+3][col] &&
                        player == board[row+4][col]) {
                    return Optional.of(player - 1);  // Adjust for 0-indexed player
                }
            }
        }

        // Diagonal checks (top-left to bottom-right)
        for (int row = 0; row <= size - 5; row++) {
            for (int col = 0; col <= size - 5; col++) {
                int player = board[row][col];
                if (player != 0 &&
                        player == board[row+1][col+1] &&
                        player == board[row+2][col+2] &&
                        player == board[row+3][col+3] &&
                        player == board[row+4][col+4]) {
                    return Optional.of(player - 1);  // Adjust for 0-indexed player
                }
            }
        }

        // Diagonal checks (top-right to bottom-left)
        for (int row = 0; row <= size - 5; row++) {
            for (int col = 4; col < size; col++) {
                int player = board[row][col];
                if (player != 0 &&
                        player == board[row+1][col-1] &&
                        player == board[row+2][col-2] &&
                        player == board[row+3][col-3] &&
                        player == board[row+4][col-4]) {
                    return Optional.of(player - 1);  // Adjust for 0-indexed player
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public Collection<Move<GmkGame>> moves(int player) {
        if (isTerminal()) return Collections.emptyList();

        List<Move<GmkGame>> possibleMoves = new ArrayList<>();
        int size = game.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (board[row][col] == 0) {
                    possibleMoves.add(new GmkMove(player, row, col));
                }
            }
        }
        return possibleMoves;
    }

    @Override
    public State<GmkGame> next(Move<GmkGame> move) {
        if (!(move instanceof GmkMove gmkMove)) {
            throw new IllegalArgumentException("Move must be a GomokuMove");
        }

        int row = gmkMove.getRow();
        int col = gmkMove.getCol();
        int player = gmkMove.player();

        // Check if the move is valid
        if (row < 0 || row >= game.getSize() || col < 0 || col >= game.getSize()) {
            throw new IllegalArgumentException("Move is out of bounds");
        }
        if (board[row][col] != 0) {
            throw new IllegalArgumentException("Cell is already occupied");
        }
        if (player != player()) {
            throw new IllegalArgumentException("It's not this player's turn");
        }

        // Create a copy of the board
        int[][] newBoard = new int[game.getSize()][game.getSize()];
        for (int i = 0; i < game.getSize(); i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, game.getSize());
        }

        // Apply the move to the new board
        newBoard[row][col] = player + 1;  // Adjust for 1-indexed board values

        // Create a copy of the move history and add the new move
        List<GmkMove> newMoveHistory = new ArrayList<>(moveHistory);
        newMoveHistory.add(gmkMove);

        // Create a new state with the updated board
        return new GmkState(game, newBoard, player, random, newMoveHistory);
    }

    public int[][] getBoard() {
        return board;
    }

    public List<GmkMove> getMoveHistory() {
        return moveHistory;
    }
}
