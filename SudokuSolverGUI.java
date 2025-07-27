import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Random;

public class SudokuSolverGUI extends JFrame {

    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    private HashSet<Integer>[] rows = new HashSet[SIZE];
    private HashSet<Integer>[] cols = new HashSet[SIZE];
    private HashSet<Integer>[] subgrids = new HashSet[SIZE];
    private JTextField speedInput;
    private final int[][][] puzzles = {
        {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        },
        {
            {0, 0, 0, 6, 0, 0, 4, 0, 0},
            {7, 0, 0, 0, 0, 3, 6, 0, 0},
            {0, 0, 0, 0, 9, 1, 0, 8, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 5, 0, 1, 8, 0, 0, 0, 3},
            {0, 0, 0, 3, 0, 6, 0, 4, 5},
            {0, 4, 0, 2, 0, 0, 0, 6, 0},
            {9, 0, 3, 0, 0, 0, 0, 0, 0},
            {0, 2, 0, 0, 0, 0, 1, 0, 0}
        }
    };

    public SudokuSolverGUI() {
        setTitle("Sudoku Solver Visualizer");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        Font cellFont = new Font("Arial", Font.BOLD, 20);

        for (int row = 0; row < SIZE; row++) {
            rows[row] = new HashSet<>();
            cols[row] = new HashSet<>();
            subgrids[row] = new HashSet<>();
            for (int col = 0; col < SIZE; col++) {
                JTextField field = new JTextField();
                field.setHorizontalAlignment(JTextField.CENTER);
                field.setFont(cellFont);
                cells[row][col] = field;
                gridPanel.add(field);
            }
        }

        JPanel controlPanel = new JPanel(new FlowLayout());

        JButton loadButton = new JButton("Load Random Puzzle");
        loadButton.addActionListener(e -> loadRandomPuzzle());

        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(e -> new Thread(this::solvePuzzle).start());

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearBoard());

        speedInput = new JTextField("50", 5);

        controlPanel.add(loadButton);
        controlPanel.add(solveButton);
        controlPanel.add(clearButton);
        controlPanel.add(new JLabel("Speed (ms):"));
        controlPanel.add(speedInput);

        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void loadRandomPuzzle() {
        clearBoard(); // Reset before loading
        int[][] puzzle = puzzles[new Random().nextInt(puzzles.length)];

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                int val = puzzle[row][col];
                board[row][col] = val;
                if (val != 0) {
                    cells[row][col].setText(String.valueOf(val));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(Color.DARK_GRAY);
                    cells[row][col].setForeground(Color.WHITE);
                    rows[row].add(val);
                    cols[col].add(val);
                    subgrids[(row / 3) * 3 + col / 3].add(val);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                    cells[row][col].setBackground(Color.WHITE);
                    cells[row][col].setForeground(Color.BLACK);
                }
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < SIZE; i++) {
            rows[i].clear();
            cols[i].clear();
            subgrids[i].clear();
        }

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = 0;
                cells[row][col].setText("");
                cells[row][col].setEditable(true);
                cells[row][col].setBackground(Color.WHITE);
                cells[row][col].setForeground(Color.BLACK);
            }
        }
    }

    private void solvePuzzle() {
        if (solve()) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Sudoku Solved!", "Success", JOptionPane.INFORMATION_MESSAGE)
            );
        } else {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "No solution exists!", "Error", JOptionPane.ERROR_MESSAGE)
            );
        }
    }

    private boolean solve() {
        int[] empty = findEmptyCell();
        if (empty == null) return true;

        int row = empty[0], col = empty[1];

        for (int num = 1; num <= SIZE; num++) {
            if (isValid(row, col, num)) {
                placeNumber(row, col, num);
                delay(getSpeed());

                if (solve()) return true;

                removeNumber(row, col, num);
                delay(getSpeed());
            }
        }
        return false;
    }

    private void placeNumber(int row, int col, int num) {
        board[row][col] = num;
        rows[row].add(num);
        cols[col].add(num);
        subgrids[(row / 3) * 3 + col / 3].add(num);
        updateCell(row, col, num);
    }

    private void removeNumber(int row, int col, int num) {
        board[row][col] = 0;
        rows[row].remove(num);
        cols[col].remove(num);
        subgrids[(row / 3) * 3 + col / 3].remove(num);
        updateCell(row, col, 0);
    }

    private int[] findEmptyCell() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == 0)
                    return new int[]{r, c};
        return null;
    }

    private boolean isValid(int r, int c, int num) {
        return !rows[r].contains(num) &&
               !cols[c].contains(num) &&
               !subgrids[(r / 3) * 3 + c / 3].contains(num);
    }

    private void updateCell(int row, int col, int num) {
        SwingUtilities.invokeLater(() ->
            cells[row][col].setText(num == 0 ? "" : String.valueOf(num))
        );
    }

    private void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int getSpeed() {
        try {
            int speed = Integer.parseInt(speedInput.getText());
            return Math.max(0, speed); // avoid negative delays
        } catch (NumberFormatException e) {
            return 50; // fallback default
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuSolverGUI gui = new SudokuSolverGUI();
            gui.setVisible(true);
        });
    }
}
