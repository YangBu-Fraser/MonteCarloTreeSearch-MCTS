# **Monte Carlo Tree Search (MCTS)**

**INFO 6205 Spring 2025 Team Project**

## **Project Overview**

This project implements a playable Gomoku game with a graphical user interface. Players can compete against an AI opponent powered by the Monte Carlo Tree Search algorithm, a powerful decision-making technique used in many game AI applications.

## What is Gomoku?

Gomoku, also known as Five in a Row, is a traditional board game played on a Go board (typically 15×15). Players take turns placing stones of their color (black or white) on intersections of the grid. The first player to form an unbroken chain of five stones horizontally, vertically, or diagonally wins the game.

### **Project Structure**

```
.
+-- Java
|   +-- src
|   |   +-- it
|   |   +-- main
|   |   |   +-- java
|   |   |   |   +-- com
|   |   |   |   |   +-- phasmidsoftware
|   |   |   |   |   |   +-- dsaipg
|   |   |   |   |   |   |   +-- adt
|   |   |   |   |   |   |   |   +-- bqs
|   |   |   |   |   |   |   |   |   +-- UnorderedIterator.java
|   |   |   |   |   +-- projects
|   |   |   |   |   |   +-- mcts
|   |   |   |   |   |   |   +-- chess
|   |   |   |   |   |   |   +-- core
|   |   |   |   |   |   |   |   +-- Game.java
|   |   |   |   |   |   |   |   +-- Move.java
|   |   |   |   |   |   |   |   +-- Node.java
|   |   |   |   |   |   |   |   +-- RandomState.java
|   |   |   |   |   |   |   |   +-- State.java
|   |   |   |   |   |   |   +-- tictactoe
|   |   |   |   |   |   |   |   +-- MCTS.java
|   |   |   |   |   |   |   |   +-- Position.java
|   |   |   |   |   |   |   |   +-- TicTacToe.java
|   |   |   |   |   |   |   |   +-- TicTacToeNode.java
|   |   |	+-- resources
|   |   |   |	+-- img
|   |   |   |	+-- 3000-common-words.txt
|   |   |   |	+-- config.ini
|   |   |   |	+-- log4j.properties
|   |   +-- test
```


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
3. **`Gomoku Game`** - Implement the game and enhance the Monte Carlo tree selection strategy.

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

   ```
git clone <repository-url>
   ```

2. **Build the project:**

   ```
mvn clean install
   ```

3. **Run the Tic-Tac-Toe game:**

   ```
mvn exec:java -Dexec.mainClass="com.phasmidsoftware.dsaipg.projects.mcts.tictactoe.TicTacToeGame"
   ```

4. **Run unit tests:**

   ```
   mvn test
   ```

5. **Run the Gomoku game:**

   Run the `GmkGUI` class, which contains the `main` method

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

## **References**

[How to Win in Gomoku](https://tictactoefree.com/tips/how-to-win-gomoku)

**Wikipedia article:**

1. [Gomoku](https://en.wikipedia.org/wiki/Gomoku) 
2. [Monte Carlo tree search](https://en.wikipedia.org/wiki/Monte_Carlo_tree_search)

**Review paper:**

1. [Go-Moku and Threat-Space Search](https://web.archive.org/web/20140411074912/http://chalmersgomoku.googlecode.com/files/allis1994.pdf) 
2. [Enhancements for Real-Time Monte-Carlo Tree Search in General Video Game Playing](https://arxiv.org/pdf/2407.03049)
3. [Monte Carlo Tree Search](https://arxiv.org/abs/2103.04931)
4. [A Survey of Monte Carlo Tree Search Methods](https://ieeexplore.ieee.org/abstract/document/6145622)
5. [Progressive strategies for Monte-Carlo tree search](https://www.worldscientific.com/doi/abs/10.1142/s1793005708001094)

## **Team & Submission**

- **Team Members**: Yang Bu, Yuxi Liu, Daoxiumei Chen
- **Project completion date**: April 20, 2025 

------

**Happy Coding! 🎮**
