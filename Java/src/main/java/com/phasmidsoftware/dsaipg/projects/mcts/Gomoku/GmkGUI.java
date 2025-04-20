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
import java.util.List;
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

    // MCTS Visualization components
    private JFrame mctsFrame;
    private MCTSVisualizationPanel mctsPanel;

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

        // Create MCTS visualization window
        initializeMCTSVisualization();

        pack();
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);

        // Show dialog to let player choose who goes first
        showNewGameDialog();
    }

    private void initializeMCTSVisualization() {
        // Create a new frame for MCTS visualization
        mctsFrame = new JFrame("MCTS Tree Visualization");
        mctsFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Don't close the game when this window is closed

        // Create visualization panel
        mctsPanel = new MCTSVisualizationPanel();
        mctsPanel.setPreferredSize(new Dimension(600, 500));

        // Add panel to frame
        mctsFrame.getContentPane().add(mctsPanel);
        mctsFrame.pack();

        // Position the MCTS visualization window to the right of the main game window
        mctsFrame.setLocation(this.getX() + this.getWidth() + 10, this.getY());
        mctsFrame.setVisible(true);
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
                        // Find best move using MCTS - this will build the tree
                        aiMove = ai.findBestMove(currentState);
                        System.out.println("AI chose move: " + aiMove);

                        // Get root node from AI for visualization
                        GmkNode rootNode = ai.getCurrentRootNode();

                        // Update MCTS visualization with the root node
                        updateMCTSVisualization(rootNode);

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

    private void updateMCTSVisualization(GmkNode rootNode) {
        // Update the MCTS visualization panel with the root node
        SwingUtilities.invokeLater(() -> {
            mctsPanel.setRootNode(rootNode);
            mctsPanel.repaint();
        });
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

        // Reset MCTS visualization
        if (mctsPanel != null) {
            mctsPanel.setRootNode(null);
            mctsPanel.repaint();
        }
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

            // Draw stones at intersection points (not cell centers)
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

    // Panel for visualizing the MCTS tree
    private class MCTSVisualizationPanel extends JPanel {
        private GmkNode rootNode;
        private final int NODE_SIZE = 30;
        private final int LEVEL_HEIGHT = 80;
        private final int HORIZONTAL_GAP = 50;

        public MCTSVisualizationPanel() {
            setBackground(Color.WHITE);
        }

        public void setRootNode(GmkNode node) {
            this.rootNode = node;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (rootNode == null) {
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString("No MCTS tree available yet", 20, 30);
                return;
            }

            // Draw the tree structure
            drawNode(g2, rootNode, getWidth() / 2, 50, 0);

            // Draw legend and information
            drawLegend(g2);
        }

        private void drawNode(Graphics2D g2, GmkNode node, int x, int y, int level) {
            // Only draw up to 5 levels (level 0-4)
            if (level > 4) return;

            // Node appearance based on visits and win rate
            double winRate = node.playouts() > 0 ? (double) node.wins() / node.playouts() : 0;
            Color nodeColor;

            if (level == 0) {
                // Root node
                nodeColor = Color.BLUE;
            } else {
                // Color based on win rate
                float hue = (float) (0.33 * winRate); // 0.0 (red) to 0.33 (green)
                nodeColor = Color.getHSBColor(hue, 0.8f, 0.9f);
            }

            // Draw the node
            g2.setColor(nodeColor);
            g2.fillOval(x - NODE_SIZE/2, y - NODE_SIZE/2, NODE_SIZE, NODE_SIZE);
            g2.setColor(Color.BLACK);
            g2.drawOval(x - NODE_SIZE/2, y - NODE_SIZE/2, NODE_SIZE, NODE_SIZE);

            // Draw node information
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            String nodeInfo = node.playouts() + "/" + node.wins();
            int textWidth = g2.getFontMetrics().stringWidth(nodeInfo);
            g2.drawString(nodeInfo, x - textWidth/2, y + 4);

            // Draw move information if not root
            if (level > 0) {
                GmkState state = (GmkState) node.state();
                if (!state.getMoveHistory().isEmpty()) {
                    GmkMove lastMove = state.getMoveHistory().get(state.getMoveHistory().size() - 1);
                    String moveStr = "(" + lastMove.getRow() + "," + lastMove.getCol() + ")";
                    g2.drawString(moveStr, x - textWidth/2, y - NODE_SIZE/2 - 5);
                }
            }

            // Draw children
            List<Node<GmkGame>> children = node.children();
            if (!children.isEmpty() && level < 4) {
                // Sort children by playouts
                List<GmkNode> sortedChildren = new ArrayList<>();
                for (Node<GmkGame> child : children) {
                    if (child instanceof GmkNode) {
                        sortedChildren.add((GmkNode) child);
                    }
                }

                // Sort by playouts in descending order
                sortedChildren.sort((a, b) -> Integer.compare(b.playouts(), a.playouts()));

                // Limit to max 5 children per node for clarity
                int maxChildren = Math.min(5, sortedChildren.size());
                int totalWidth = (maxChildren - 1) * HORIZONTAL_GAP;
                int startX = x - totalWidth / 2;

                for (int i = 0; i < maxChildren; i++) {
                    GmkNode child = sortedChildren.get(i);
                    int childX = startX + i * HORIZONTAL_GAP;
                    int childY = y + LEVEL_HEIGHT;

                    // Draw line to child
                    g2.setColor(Color.GRAY);
                    g2.drawLine(x, y + NODE_SIZE/2, childX, childY - NODE_SIZE/2);

                    // Draw child node
                    drawNode(g2, child, childX, childY, level + 1);
                }

                // Indicate if there are more children
                if (sortedChildren.size() > maxChildren) {
                    g2.setColor(Color.BLACK);
                    g2.drawString("+" + (sortedChildren.size() - maxChildren) + " more",
                            x + totalWidth/2 + 10, y + LEVEL_HEIGHT/2);
                }
            }
        }

        private void drawLegend(Graphics2D g2) {
            int legendX = 20;
            int legendY = getHeight() - 80;

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("MCTS Tree Visualization", legendX, legendY);

            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString("• Showing top 5 levels of the tree", legendX, legendY + 15);
            g2.drawString("• Node format: visits/wins", legendX, legendY + 30);
            g2.drawString("• Color indicates win rate (red → green = low → high)", legendX, legendY + 45);
            g2.drawString("• Max 5 best children shown per node", legendX, legendY + 60);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GmkGUI gui = new GmkGUI();
            gui.setVisible(true);
        });
    }

}