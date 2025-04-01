# **Tic-Tac-Toe with Monte Carlo Tree Search (MCTS)**

**INFO 6205 Spring 2025 Team Project**

## **Project Overview**

This project implements a **Monte Carlo Tree Search (MCTS)**-based AI for playing **Tic-Tac-Toe**. The goal is to develop an intelligent agent that can efficiently explore possible moves and make optimal decisions using MCTS, a powerful algorithm for decision-making in games.

### **Key Features**

✅ **MCTS Implementation** – A simulation-based search algorithm for optimal move selection

✅ **Generic Game Framework** – Built using Java generics for extensibility

✅ **Unit Testing** – Ensures correctness and robustness

✅ **Performance Analysis** – Measures MCTS efficiency in decision-making

## **Project Goals**

1. **Understand MCTS** – Learn how Monte Carlo Tree Search works and apply it to Tic-Tac-Toe.
2. **Implement & Optimize** – Develop a functional AI player and explore optimizations.
3. **Extend to Other Games** – The framework allows adaptation to other board games.

## **Technical Details**

### **Project Structure**

The code is organized into two main packages:

1. **`core`** – Contains generic MCTS logic (`Game`, `Move`, `Node`, `State`).
2. **`tictactoe`** – Implements Tic-Tac-Toe-specific rules and AI.

### **How MCTS Works in This Project**

1. **Selection** – Traverse the game tree using a selection policy (e.g., UCB1).
2. **Expansion** – Add new nodes for unexplored moves.
3. **Simulation** – Play random games (rollouts) to estimate move strength.
4. **Backpropagation** – Update node statistics based on simulation results.

## **How to Run**

### **Prerequisites**

- Java 18+
- Maven

### **Steps**

1. **Clone the repository:**

   bash

   Copy

   ```
   git clone <repository-url>
   ```

2. **Build the project:**

   bash

   Copy

   ```
   mvn clean install
   ```

3. **Run the Tic-Tac-Toe game:**

   bash

   Copy

   ```
   mvn exec:java -Dexec.mainClass="com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToeGame"
   ```

4. **Run unit tests:**

   bash

   Copy

   ```
   mvn test
   ```

## Performance & Optimization

- **Default MCTS** works well for Tic-Tac-Toe due to its small state space.
- **Optimizations** (if implemented) could include:
  - Heuristic-based rollouts (instead of random playouts)
  - Parallel simulations
  - Early termination conditions

## **Report & Evaluation**

The final submission includes:

✅  **Code implementation** (MCTS + Tic-Tac-Toe logic)

✅  **Unit tests** (high coverage required)

✅  **Performance analysis** (timings, optimizations)

✅  **Documentation** (README, rules, execution steps)

✅  **Optional** – A demo video showing gameplay

## **References**

- [Monte Carlo Tree Search (Wikipedia)](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search)
- [MCTS Review Paper (arXiv)](https://arxiv.org/abs/2103.04931)
- [Beginners Guide to MCTS](https://int8.io/monte-carlo-tree-search-beginners-guide/)

## **Team & Submission**

- **Team Members**: Yang Bu, Yuxi Liu, Daoxiumei Chen
- **Project completion date**: April 20, 2025 

------

**Happy Coding! 🎮**
