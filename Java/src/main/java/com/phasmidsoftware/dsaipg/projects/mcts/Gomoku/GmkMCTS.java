package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;


import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import java.util.*;

public class GmkMCTS {
    private static final double EXPLORATION_PARAMETER = Math.sqrt(2);
    private static final int DEFAULT_ITERATIONS = 1000;

    private final int iterations;

    public GmkMCTS() {
        this(DEFAULT_ITERATIONS);
    }

    public GmkMCTS(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Add all possible moves in to MCTS.
     *
     * @param node
     */
    private void expand(GmkNode node) {
        if (node == null || node.state() == null || node.state().isTerminal()) return;

        GmkState state = (GmkState) node.state();
        int player = state.player();

        Collection<Move<GmkGame>> moves = state.moves(player);
        if (moves == null || moves.isEmpty()) return;

        for (Move<GmkGame> move : moves) {
            if (move == null) continue;
            try {
                State<GmkGame> nextState = state.next(move);
                if (nextState != null) {
                    node.addChild(nextState);
                }
            } catch (Exception e) {
                System.err.println("Error creating next state for move: " + move);
                e.printStackTrace();
            }
        }
    }

    private GmkNode select(GmkNode node) {
        if (node == null) return null;

        if (node.isLeaf()) {
            if (node.state() == null || node.state().isTerminal()) {
                return node;
            }
            expand(node);

            List<Node<GmkGame>> children = new ArrayList<>(node.children());
            if (!children.isEmpty()) {
                int randomIndex = (int) (Math.random() * children.size());
                Node<GmkGame> childNode = children.get(randomIndex);
                if (childNode instanceof GmkNode) {
                    return (GmkNode) childNode;
                }
            }
            return node;
        }

        GmkNode bestNode = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        List<Node<GmkGame>> children = node.children();
        if (children == null || children.isEmpty()) {
            return node;
        }

        for (Node<GmkGame> child : children) {
            if (!(child instanceof GmkNode)) continue;

            GmkNode gmkNode = (GmkNode) child;
            double wins = gmkNode.wins();
            double playouts = gmkNode.playouts();
            double parentPlayouts = node.playouts();

            if (playouts == 0) {
                return gmkNode;
            }

            double uct = (wins / playouts) + EXPLORATION_PARAMETER * Math.sqrt(Math.log(parentPlayouts) / playouts);
            if (uct > bestValue) {
                bestValue = uct;
                bestNode = gmkNode;
            }
        }

        if (bestNode == null) {
            return node; // Return the current node if no best child found
        }

        GmkNode selectedNode = select(bestNode);
        return selectedNode != null ? selectedNode : bestNode;
    }

    private boolean simulate(GmkNode node) {
        if (node == null || node.state() == null) return false;

        GmkState currentState = (GmkState) node.state();

        if (currentState.isTerminal()) {
            Optional<Integer> winner = currentState.winner();
            return winner.isPresent() &&
                    winner.get() == (currentState.player() == 0 ? 1 : 0);
        }

        // Run a simulation from this state with improved strategy
        try {
            GmkState simulationState = currentState; // Start from the current state

            int maxMoves = 100; // Prevent infinite loops
            int moveCount = 0;

            while (!simulationState.isTerminal() && moveCount < maxMoves) {
                int player = simulationState.player();

                // Get all possible moves
                Collection<Move<GmkGame>> possibleMoves = simulationState.moves(player);
                List<Move<GmkGame>> movesList = new ArrayList<>(possibleMoves);

                if (movesList.isEmpty()) {
                    break;
                }

                // Improved move selection for simulation
                Move<GmkGame> selectedMove;

                // With 80% probability, choose a "smart" move
                if (Math.random() < 0.8) {
                    selectedMove = selectSmartMove(simulationState, movesList, player);
                } else {
                    // With 20% probability, choose a completely random move for exploration
                    int randomIndex = (int)(Math.random() * movesList.size());
                    selectedMove = movesList.get(randomIndex);
                }

                if (selectedMove == null) continue;

                State<GmkGame> nextState = simulationState.next(selectedMove);
                if (!(nextState instanceof GmkState)) break;

                simulationState = (GmkState) nextState;
                moveCount++;
            }

            Optional<Integer> winner = simulationState.winner();
            if (winner.isPresent()) {
                int initPlayer = node.white() ? 0 : 1;
                return winner.get() == initPlayer;
            }
        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // New method: Select a smart move for simulation
    private Move<GmkGame> selectSmartMove(GmkState state, List<Move<GmkGame>> movesList, int player) {
        int[][] board = state.getBoard();
        int size = state.game().getSize();

        // Create a score for each possible move
        List<ScoredMove> scoredMoves = new ArrayList<>();

        for (Move<GmkGame> move : movesList) {
            if (!(move instanceof GmkMove)) continue;

            GmkMove gmkMove = (GmkMove) move;
            int row = gmkMove.getRow();
            int col = gmkMove.getCol();

            // Skip if position is already occupied
            if (board[row][col] != 0) continue;

            // Calculate score for this move
            int score = evaluateMove(board, row, col, player, size);
            scoredMoves.add(new ScoredMove(gmkMove, score));
        }

        if (scoredMoves.isEmpty()) {
            // If no scored moves, return a random move
            int randomIndex = (int)(Math.random() * movesList.size());
            return movesList.get(randomIndex);
        }

        // Sort moves by score in descending order
        scoredMoves.sort((a, b) -> Integer.compare(b.score, a.score));

        // Select from top 3 moves with probability proportional to score
        int selectionRange = Math.min(3, scoredMoves.size());
        double totalScore = 0;
        for (int i = 0; i < selectionRange; i++) {
            totalScore += scoredMoves.get(i).score;
        }

        // If all scores are 0, select randomly from top 3
        if (totalScore == 0) {
            int randomIndex = (int)(Math.random() * selectionRange);
            return scoredMoves.get(randomIndex).move;
        }

        // Otherwise, select based on score probability
        double random = Math.random() * totalScore;
        double cumulativeScore = 0;
        for (int i = 0; i < selectionRange; i++) {
            cumulativeScore += scoredMoves.get(i).score;
            if (random < cumulativeScore) {
                return scoredMoves.get(i).move;
            }
        }

        // Fallback to the top-scored move
        return scoredMoves.get(0).move;
    }

    // Helper class for scored moves
    private static class ScoredMove {
        final GmkMove move;
        final int score;

        ScoredMove(GmkMove move, int score) {
            this.move = move;
            this.score = score;
        }
    }

    // Evaluate a potential move
    private int evaluateMove(int[][] board, int row, int col, int player, int size) {
        // We'll evaluate this position by temporarily placing a piece and counting patterns
        int playerValue = player + 1; // Board uses 1-indexed values

        // Create a copy of the board with the move applied
        int[][] tempBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, tempBoard[i], 0, size);
        }
        tempBoard[row][col] = playerValue;

        int score = 0;

        // Check patterns in all 8 directions
        int[][] directions = {
                {0, 1}, {1, 0}, {1, 1}, {1, -1},  // horizontal, vertical, diagonal
                {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}  // reverse directions
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            // Count consecutive pieces and open ends
            int count = countConsecutive(tempBoard, row, col, dx, dy, playerValue, size);

            // Award points based on pattern
            score += scorePattern(count);

            // Check if this move blocks opponent's patterns
            int opponentValue = (player == 0) ? 2 : 1;
            tempBoard[row][col] = opponentValue;
            int blockingCount = countConsecutive(tempBoard, row, col, dx, dy, opponentValue, size);
            score += scoreBlockingPattern(blockingCount);

            // Reset for next direction
            tempBoard[row][col] = playerValue;
        }

        // Give bonus for center proximity (better board control)
        int center = size / 2;
        int distanceToCenter = Math.abs(row - center) + Math.abs(col - center);

        // Penalize corner and edge positions more heavily
        if (row == 0 || row == size-1 || col == 0 || col == size-1) {
            // Corner positions are worst
            if ((row == 0 && col == 0) || (row == 0 && col == size-1) ||
                    (row == size-1 && col == 0) || (row == size-1 && col == size-1)) {
                score -= 20; // Strong penalty for corners
            } else {
                score -= 10; // Penalty for edges
            }
        }

        // Greater bonus for center proximity
        score += Math.max(0, 15 - (distanceToCenter * 2));

        return score;
    }

    private int countConsecutive(int[][] board, int row, int col, int dx, int dy, int playerValue, int size) {
        int count = 1; // Start with 1 for the current position

        // Count in positive direction
        int r = row + dx;
        int c = col + dy;
        while (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == playerValue) {
            count++;
            r += dx;
            c += dy;
        }

        // Count in negative direction
        r = row - dx;
        c = col - dy;
        while (r >= 0 && r < size && c >= 0 && c < size && board[r][c] == playerValue) {
            count++;
            r -= dx;
            c -= dy;
        }

        return count;
    }

    // Score patterns based on how many consecutive pieces
    private int scorePattern(int count) {
        return switch (count) {
            case 5 -> 1000; // Win
            case 4 -> 100;  // One move from win
            case 3 -> 10;   // Two moves from win
            case 2 -> 3;    // Early pattern
            default -> 1;   // Single piece
        };
    }

    // Score for blocking opponent's patterns
    private int scoreBlockingPattern(int count) {
        return switch (count) {
            case 5 -> 900;  // Block win
            case 4 -> 90;   // Block one from win
            case 3 -> 9;    // Block two from win
            default -> 0;   // Not significant
        };
    }


    private void backPropagation(GmkNode node, boolean res) {
        if (node == null) return;
        node.update(res);
    }

    // Check for blocking moves
    private GmkMove findBlockingMove(GmkState state, int playerToBlock) {
        int[][] board = state.getBoard();
        int size = state.game().getSize();
        int aiPlayer = state.player();

        // Check for three in a row that can be blocked

        // Horizontal check
        for (int row = 0; row < size; row++) {
            for (int col = 0; col <= size - 4; col++) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    if (board[row][col + i] == playerToBlock + 1) {
                        count++;
                    }
                }

                // If three in a row and one empty space
                if (count >= 3) {
                    for (int i = 0; i < 4; i++) {
                        if (board[row][col + i] == 0) {
                            return new GmkMove(aiPlayer, row, col + i);
                        }
                    }
                }
            }
        }

        // Vertical check
        for (int row = 0; row <= size - 4; row++) {
            for (int col = 0; col < size; col++) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    if (board[row + i][col] == playerToBlock + 1) {
                        count++;
                    }
                }

                // If three in a row and one empty space
                if (count >= 3) {
                    for (int i = 0; i < 4; i++) {
                        if (board[row + i][col] == 0) {
                            return new GmkMove(aiPlayer, row + i, col);
                        }
                    }
                }
            }
        }

        // Diagonal check (top-left to bottom-right)
        for (int row = 0; row <= size - 4; row++) {
            for (int col = 0; col <= size - 4; col++) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    if (board[row + i][col + i] == playerToBlock + 1) {
                        count++;
                    }
                }

                // If three in a row and one empty space
                if (count >= 3) {
                    for (int i = 0; i < 4; i++) {
                        if (board[row + i][col + i] == 0) {
                            return new GmkMove(aiPlayer, row + i, col + i);
                        }
                    }
                }
            }
        }

        // Diagonal check (top-right to bottom-left)
        for (int row = 0; row <= size - 4; row++) {
            for (int col = 3; col < size; col++) {
                int count = 0;
                for (int i = 0; i < 4; i++) {
                    if (board[row + i][col - i] == playerToBlock + 1) {
                        count++;
                    }
                }

                // If three in a row and one empty space
                if (count >= 3) {
                    for (int i = 0; i < 4; i++) {
                        if (board[row + i][col - i] == 0) {
                            return new GmkMove(aiPlayer, row + i, col - i);
                        }
                    }
                }
            }
        }

        return null;
    }

    public GmkMove findBestMove(GmkState state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }

        int aiPlayer = state.player();
        int humanPlayer = (aiPlayer == 0) ? 1 : 0;

        // Check if this is the first AI move
        int[][] board = state.getBoard();
        int totalPieces = 0;
        for (int[] row : board) {
            for (int cell : row) {
                if (cell != 0) {
                    totalPieces++;
                }
            }
        }

        if (totalPieces <= 1) {
            // AI first move
            int center = board.length / 2;

            // Check if center is occupied
            if (board[center][center] == 0) {
                // If center is free, then take it
                return new GmkMove(aiPlayer, center, center);
            } else {
                // Center is occupied, choose a random adjacent position
                List<int[]> adjacentPositions = new ArrayList<>();
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i == 0 && j == 0) continue; // Skip the center

                        int newRow = center + i;
                        int newCol = center + j;

                        if (newRow >= 0 && newRow < board.length &&
                                newCol >= 0 && newCol < board.length &&
                                board[newRow][newCol] == 0) {
                            adjacentPositions.add(new int[]{newRow, newCol});
                        }
                    }
                }

                if (!adjacentPositions.isEmpty()) {
                    // Randomly select an adjacent position
                    int randomIndex = (int)(Math.random() * adjacentPositions.size());
                    int[] selected = adjacentPositions.get(randomIndex);
                    return new GmkMove(aiPlayer, selected[0], selected[1]);
                }
            }
        }

        // Check if we need to block player's three in a row
        GmkMove blockingMove = findBlockingMove(state, humanPlayer);
        if (blockingMove != null) {
            System.out.println("AI is making a blocking move: " + blockingMove);
            return blockingMove;
        }

        // Otherwise, use MCTS to find the best move
        // Create the root node
        GmkNode rootNode = new GmkNode(state);

        // Expand the root node first
        expand(rootNode);

        if (rootNode.children().isEmpty()) {
            System.out.println("No valid moves available from root node");
            return findRandomMove(state);
        }

        // Run the MCTS algorithm for the specified number of iterations
        for (int i = 0; i < iterations; i++) {
            try {
                // Selection and expansion
                GmkNode selectedNode = select(rootNode);

                if (selectedNode == null) {
                    System.err.println("Selected node is null at iteration " + i);
                    continue;
                }

                // Simulation
                boolean result = simulate(selectedNode);

                // Backpropagation
                backPropagation(selectedNode, result);

            } catch (Exception e) {
                System.err.println("Error during MCTS iteration " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Choose the best move based on the most visited child
        GmkMove selectedMove;
        try {
            selectedMove = findBestMoveFromChildren(rootNode);

            // Debug why position (0,0) might be selected
            if (selectedMove.getRow() == 0 && selectedMove.getCol() == 0) {
                System.out.println("WARNING: AI selected (0,0) position");

                // Debug the top 5 moves considered by MCTS
                System.out.println("Top 5 moves by playout count:");
                List<GmkNode> sortedChildren = new ArrayList<>();
                for (Node<GmkGame> child : rootNode.children()) {
                    if (child instanceof GmkNode) {
                        sortedChildren.add((GmkNode) child);
                    }
                }

                // Sort by playouts in descending order
                sortedChildren.sort((a, b) -> Integer.compare(b.playouts(), a.playouts()));

                // Print top 5 or fewer
                int count = Math.min(5, sortedChildren.size());
                for (int i = 0; i < count; i++) {
                    GmkNode child = sortedChildren.get(i);
                    GmkState childState = (GmkState) child.state();
                    List<GmkMove> childMoves = childState.getMoveHistory();
                    GmkState rootState = (GmkState) rootNode.state();
                    List<GmkMove> rootMoves = rootState.getMoveHistory();

                    if (childMoves.size() > rootMoves.size()) {
                        GmkMove move = childMoves.get(rootMoves.size());
                        System.out.println("Move " + (i+1) + ": (" + move.getRow() + "," + move.getCol() +
                                ") - Playouts: " + child.playouts() + ", Wins: " + child.wins());
                    }
                }

                // Check random positions for comparison
                System.out.println("\nEvaluating sample positions:");
                int[][] positions = {{7, 7}, {0, 0}, {0, 7}, {7, 0}, {3, 3}, {3, 7}, {7, 3}};
                for (int[] pos : positions) {
                    int score = evaluateMove(state.getBoard(), pos[0], pos[1], state.player(), state.game().getSize());
                    System.out.println("Position (" + pos[0] + "," + pos[1] + ") score: " + score);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding best move, using random move instead: " + e.getMessage());
            e.printStackTrace();
            selectedMove = findRandomMove(state);
        }

        return selectedMove;
    }

    private GmkMove findRandomMove(GmkState state) {
        Collection<Move<GmkGame>> moves = state.moves(state.player());
        List<Move<GmkGame>> movesList = new ArrayList<>(moves);

        if (movesList.isEmpty()) {
            throw new IllegalStateException("No valid moves available");
        }

        int randomIndex = (int) (Math.random() * movesList.size());
        Move<GmkGame> randomMove = movesList.get(randomIndex);

        if (randomMove instanceof GmkMove) {
            return (GmkMove) randomMove;
        } else {
            throw new IllegalStateException("Move is not a GmkMove");
        }
    }

    private GmkMove findBestMoveFromChildren(GmkNode rootNode) {
        if (rootNode.children().isEmpty()) {
            System.out.println("Warning: Root has no children");
            return findRandomMove((GmkState) rootNode.state());
        }

        List<ScoredMove> scoredMoves = new ArrayList<>();
        GmkState rootState = (GmkState) rootNode.state();
        int size = rootState.game().getSize();
        int player = rootState.player();

        // Consider both playouts and position evaluation
        for (Node<GmkGame> child : rootNode.children()) {
            if (!(child instanceof GmkNode gomokuChild)) continue;

            int playouts = gomokuChild.playouts();
            double winRate = playouts > 0 ? (double)gomokuChild.wins() / playouts : 0;

            GmkState childState = (GmkState) gomokuChild.state();
            List<GmkMove> childMoves = childState.getMoveHistory();
            List<GmkMove> rootMoves = rootState.getMoveHistory();

            if (childMoves.size() <= rootMoves.size()) continue;

            GmkMove move = childMoves.get(rootMoves.size());

            // Calculate position score
            int positionScore = evaluateMove(rootState.getBoard(), move.getRow(), move.getCol(), player, size);

            // Combined score based on MCTS results and position evaluation
            double combinedScore = (winRate * playouts) + (positionScore * 0.1);

            // Heavily penalize (0,0) and other corners
            if ((move.getRow() == 0 && move.getCol() == 0) ||
                    (move.getRow() == 0 && move.getCol() == size-1) ||
                    (move.getRow() == size-1 && move.getCol() == 0) ||
                    (move.getRow() == size-1 && move.getCol() == size-1)) {
                combinedScore *= 0.5; // 50% penalty for corners
            }

            scoredMoves.add(new ScoredMove(move, (int)combinedScore));
        }

        if (scoredMoves.isEmpty()) {
            System.out.println("Warning: No valid scored moves");
            return findRandomMove(rootState);
        }

        // Sort by combined score
        scoredMoves.sort((a, b) -> Integer.compare(b.score, a.score));

        // Return the move with the highest combined score
        return scoredMoves.get(0).move;
    }

}
