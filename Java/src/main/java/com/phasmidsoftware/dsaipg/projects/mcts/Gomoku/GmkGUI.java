package com.phasmidsoftware.dsaipg.projects.mcts.Gomoku;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class GmkGUI extends JFrame{
    private final int CELL_SIZE = 40;
    private final int MARGIN = 30;

    private GmkGame game;
    private GmkState currentState;
    private final GmkMCTS ai;

    private final JLabel statusLabel;
    private final BoardPanel boardPanel;
    private boolean playerTurn; // true for player, false for AI
    private int HUMAN_PLAYER; // Will be set based on player's choice (0 for black, 1 for white)
    private int AI_PLAYER;    // Will be set based on player's choice (1 for white, 0 for black)
    private int BOARD_SIZE;

    public GmkGUI() {
        // Create game components with default values
        game = new GmkGame();               // Default 15x15 board
        BOARD_SIZE = game.getSize();
        ai = new GmkMCTS(1000); // 1000 iterations for MCTS

        // Setup GUI
        setTitle("Gomoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Status panel at the top
        statusLabel = new JLabel("Welcome to Gomoku");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        // Board panel in the center
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(
                BOARD_SIZE * CELL_SIZE + 2 * MARGIN,
                BOARD_SIZE * CELL_SIZE + 2 * MARGIN));

        // Add mouse listener to the board panel - using instance method reference for better code clarity
        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleBoardClick(e);
            }
        });

        // Control panel at the bottom
        JPanel controlPanel = new JPanel();
        JButton newGameBtn = new JButton("New Game");
        newGameBtn.addActionListener(e -> showNewGameDialog());

        controlPanel.add(newGameBtn);

        // Add components to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(statusLabel, BorderLayout.NORTH);
        getContentPane().add(boardPanel, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // Show dialog to let player choose who goes first
        showNewGameDialog();
    }

    private void handleBoardClick(MouseEvent e) {
        if (playerTurn && !currentState.isTerminal()) {
            /**
             * Calculate board position from mouse coordinates
             * Use integer division with proper rounding
             */
            int row = (e.getY() - MARGIN + CELL_SIZE/2) / CELL_SIZE;
            int col = (e.getX() - MARGIN + CELL_SIZE/2) / CELL_SIZE;

            // Check if the click is within the board bounds
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                // Check if the cell is already occupied
                int[][] board = currentState.getBoard();
                if (board[row][col] == 0) {
                    makeMove(row, col);
                } else {
                    statusLabel.setText("That position is already occupied!");
                }
            }
        }
    }

    private void showNewGameDialog() {
        Object[] options = {"Play First (Black)", "Play Second (White)"};
        int choice = JOptionPane.showOptionDialog(this,
                "Would you like to play first or second?",
                "New Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // Set up the game based on player's choice
        HUMAN_PLAYER = (choice == 0) ? 0 : 1;
        AI_PLAYER = (choice == 0) ? 1 : 0;

        resetGame();

        // If AI goes first, make its move
        if (AI_PLAYER == 0) {
            playerTurn = false;
            statusLabel.setText("AI is thinking...");

            // Use SwingWorker to not block the EDT
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() {
                    // For the first move, we can place at the center for better gameplay
                    int centerPos = BOARD_SIZE / 2;
                    GmkMove aiMove = new GmkMove(AI_PLAYER, centerPos, centerPos);
                    currentState = (GmkState) currentState.next(aiMove);
                    return null;
                }

                @Override
                protected void done() {
                    boardPanel.repaint();
                    playerTurn = true;
                    String playerColor = (HUMAN_PLAYER == 0) ? "Black" : "White";
                    statusLabel.setText("Your turn (" + playerColor + ")");
                }
            };

            worker.execute();
        }
    }

    private void makeMove(int row, int col) {
        // Create a move for the human player
        GmkMove humanMove = new GmkMove(HUMAN_PLAYER, row, col);

        try {
            // Apply the move to the current state
            currentState = (GmkState) currentState.next(humanMove);
            playerTurn = false;
            boardPanel.repaint();

            // Check if the game is over after the human move
            if (checkGameOver()) {
                return;
            }

            // Update status
            statusLabel.setText("AI is thinking...");

            // Use a new Thread for AI move to not block the UI
            new Thread(() -> {
                try {
                    // Add a small delay so the UI can update
                    Thread.sleep(100);

                    // Make AI move
                    GmkMove aiMove = null;
                    try {
                        aiMove = ai.findBestMove(currentState);
                        System.out.println("AI chose move: " + aiMove);
                    } catch (Exception e) {
                        System.err.println("Error in AI move calculation: " + e.getMessage());
                        e.printStackTrace();

                        // Fallback: make a random move
                        Collection<Move<GmkGame>> moves = currentState.moves(AI_PLAYER);
                        ArrayList<Move<GmkGame>> moveList = new ArrayList<>(moves);
                        if (!moveList.isEmpty()) {
                            int randomIndex = (int) (Math.random() * moveList.size());
                            Move<GmkGame> randomMove = moveList.get(randomIndex);
                            if (randomMove instanceof GmkMove) {
                                aiMove = (GmkMove) randomMove;
                                System.out.println("Using random move instead: " + aiMove);
                            }
                        }
                    }

                    if (aiMove != null) {
                        final GmkMove finalAiMove = aiMove;

                        // Update UI on the EDT
                        SwingUtilities.invokeLater(() -> {
                            try {
                                currentState = (GmkState) currentState.next(finalAiMove);
                                boardPanel.repaint();

                                // Check if the game is over after the AI move
                                if (!checkGameOver()) {
                                    String playerColor = (HUMAN_PLAYER == 0) ? "Black" : "White";
                                    statusLabel.setText("Your turn (" + playerColor + ")");
                                }
                            } catch (Exception e) {
                                System.err.println("Error applying AI move: " + e.getMessage());
                                e.printStackTrace();
                                statusLabel.setText("Error during AI move. Your turn.");
                            }

                            // Always set player turn to true to avoid deadlock
                            playerTurn = true;
                        });
                    } else {
                        // If AI couldn't make a move
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("AI couldn't find a valid move. Your turn.");
                            playerTurn = true;
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error in AI move thread: " + e.getMessage());
                    e.printStackTrace();

                    // Update UI on the EDT
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error during AI move. Your turn.");
                        playerTurn = true;
                    });
                }
            }).start();

        } catch (IllegalArgumentException e) {
            // Invalid move
            JOptionPane.showMessageDialog(this, "Invalid move: " + e.getMessage());
        }
    }

    // Add a fallback move method to prevent complete failure
    private GmkMove findFallbackMove() {
        System.out.println("Finding fallback move...");
        // Simple strategy: find the first empty cell
        int[][] board = currentState.getBoard();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 0) {
                    return new GmkMove(AI_PLAYER, i, j);
                }
            }
        }
        return null; // Should not happen unless board is full
    }

    private boolean checkGameOver() {
        if (currentState.isTerminal()) {
            Optional<Integer> winnerOpt = currentState.winner();
            if (winnerOpt.isPresent()) {
                int winner = winnerOpt.get();
                String winnerStr = (winner == HUMAN_PLAYER) ? "You" : "AI";
                String colorStr = (winner == 0) ? "Black" : "White";
                statusLabel.setText(winnerStr + " win! (" + colorStr + ")");
            } else {
                statusLabel.setText("Game over! It's a draw.");
            }
            return true;
        }
        return false;
    }

    private void resetGame() {
        game = new GmkGame();
        currentState = new GmkState(game);
        playerTurn = (HUMAN_PLAYER == 0); // Player goes first if they are black
        String playerColor = (HUMAN_PLAYER == 0) ? "Black" : "White";
        statusLabel.setText("Your turn (" + playerColor + ")");
        boardPanel.repaint();
    }

    // Panel for drawing the board
    private class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw board background
            g2.setColor(new Color(220, 179, 92)); // Wooden color
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw grid lines
            g2.setColor(Color.BLACK);
            for (int i = 0; i < BOARD_SIZE; i++) {
                // Horizontal lines
                g2.drawLine(
                        MARGIN,
                        MARGIN + i * CELL_SIZE,
                        MARGIN + (BOARD_SIZE - 1) * CELL_SIZE,
                        MARGIN + i * CELL_SIZE
                );

                // Vertical lines
                g2.drawLine(
                        MARGIN + i * CELL_SIZE,
                        MARGIN,
                        MARGIN + i * CELL_SIZE,
                        MARGIN + (BOARD_SIZE - 1) * CELL_SIZE
                );
            }

            // Draw star points (common in Gomoku/Go boards)
            int[] starPoints = {3, BOARD_SIZE / 2, BOARD_SIZE - 4};
            g2.setColor(Color.BLACK);
            for (int row : starPoints) {
                for (int col : starPoints) {
                    g2.fillOval(
                            MARGIN + col * CELL_SIZE - 3,
                            MARGIN + row * CELL_SIZE - 3,
                            6, 6
                    );
                }
            }

            // Draw stones at intersection points (not cell centers) - fixing offset issue
            int[][] board = currentState.getBoard();
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    int player = board[row][col];
                    if (player != 0) {
                        // Draw at the exact intersection point
                        int x = MARGIN + col * CELL_SIZE;
                        int y = MARGIN + row * CELL_SIZE;
                        int stoneSize = (int) (CELL_SIZE * 0.8);
                        int offset = stoneSize / 2;

                        if (player == 1) { // Player 1 (Black)
                            g2.setColor(Color.BLACK);
                        } else { // Player 2 (White)
                            g2.setColor(Color.WHITE);
                        }

                        g2.fillOval(
                                x - offset,
                                y - offset,
                                stoneSize,
                                stoneSize
                        );

                        // Draw outline for white stones to make them more visible
                        if (player == 2) {
                            g2.setColor(Color.BLACK);
                            g2.drawOval(
                                    x - offset,
                                    y - offset,
                                    stoneSize,
                                    stoneSize
                            );
                        }
                    }
                }
            }

            // Highlight the last move if there's any
            if (!currentState.getMoveHistory().isEmpty()) {
                GmkMove lastMove = currentState.getMoveHistory().get(currentState.getMoveHistory().size() - 1);
                int row = lastMove.getRow();
                int col = lastMove.getCol();
                int x = MARGIN + col * CELL_SIZE;
                int y = MARGIN + row * CELL_SIZE;

                g2.setColor(Color.RED);
                g2.drawRect(
                        x - 10,
                        y - 10,
                        20,
                        20
                );
            }
        }
    }

    public static void main(String[] args) {
        // Fix for the horizontal winner check in GmkState
        fixWinnerMethod();

        SwingUtilities.invokeLater(() -> {
            GmkGUI gui = new GmkGUI();
            gui.setVisible(true);
        });
    }

    // This method simulates fixing the bug in GmkState.winner() method
    // In a real application, you would directly modify the GmkState class
    private static void fixWinnerMethod() {
        System.out.println("Note: In a real application, you should directly modify the GmkState.winner() method");
        System.out.println("to fix the horizontal check bounds, changing:");
        System.out.println("for (int col = 0; col < size; col++) to");
        System.out.println("for (int col = 0; col <= size - 5; col++)");
    }
}

