import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

public class SudokuSolverGUI extends JFrame {

    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    @SuppressWarnings("unchecked")
    private HashSet<Integer>[] rows = new HashSet[SIZE];
    @SuppressWarnings("unchecked")
    private HashSet<Integer>[] cols = new HashSet[SIZE];
    @SuppressWarnings("unchecked")
    private HashSet<Integer>[] subgrids = new HashSet[SIZE];
    private JTextField speedInput;

    public SudokuSolverGUI() {
        setTitle("Sudoku Solver");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        for (int row = 0; row < SIZE; row++) {
            rows[row] = new HashSet<>();
            cols[row] = new HashSet<>();
            subgrids[row] = new HashSet<>();
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                gridPanel.add(cells[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));

        JButton loadButton = new JButton("Load Puzzle");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPuzzle();
            }
        });
        buttonPanel.add(loadButton);

        JButton solveButton = new JButton("Solve");
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        solvePuzzle();
                    }
                }).start();
            }
        });
        buttonPanel.add(solveButton);

        speedInput = new JTextField("10"); // default speed value
        buttonPanel.add(new JLabel("Speed (ms):"));
        buttonPanel.add(speedInput);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPuzzle() {
        int[][] puzzle = {
            {5, 3, 0, 0, 7, 0, 0, 0, 0},
            {6, 0, 0, 1, 9, 5, 0, 0, 0},
            {0, 9, 8, 0, 0, 0, 0, 6, 0},
            {8, 0, 0, 0, 6, 0, 0, 0, 3},
            {4, 0, 0, 8, 0, 3, 0, 0, 1},
            {7, 0, 0, 0, 2, 0, 0, 0, 6},
            {0, 6, 0, 0, 0, 0, 2, 8, 0},
            {0, 0, 0, 4, 1, 9, 0, 0, 5},
            {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = puzzle[row][col];
                if (puzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(puzzle[row][col]));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(Color.GRAY);
                    cells[row][col].setForeground(Color.WHITE);
                    rows[row].add(puzzle[row][col]);
                    cols[col].add(puzzle[row][col]);
                    subgrids[(row / 3) * 3 + col / 3].add(puzzle[row][col]);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                }
            }
        }
    }

    private void solvePuzzle() {
        if (solve()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SudokuSolverGUI.this, "Sudoku Solved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(SudokuSolverGUI.this, "No solution exists for the given Sudoku board.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private boolean isValid(int row, int col, int num) {
        if (rows[row].contains(num) || cols[col].contains(num) || subgrids[(row / 3) * 3 + col / 3].contains(num)) {
            return false;
        }
        return true;
    }

    private boolean solve() {
        int[] empty = findEmptyCell();
        if (empty == null) {
            return true;
        }
        int row = empty[0];
        int col = empty[1];

        for (int num = 1; num <= SIZE; num++) {
            if (isValid(row, col, num)) {
                board[row][col] = num;
                rows[row].add(num);
                cols[col].add(num);
                subgrids[(row / 3) * 3 + col / 3].add(num);
                updateGUI(row, col, num);
                delay(getSpeed()); // Use the speed input value
                if (solve()) {
                    return true;
                }
                board[row][col] = 0;
                rows[row].remove(num);
                cols[col].remove(num);
                subgrids[(row / 3) * 3 + col / 3].remove(num);
                updateGUI(row, col, 0);
                delay(getSpeed()); // Use the speed input value
            }
        }
        return false;
    }

    private int[] findEmptyCell() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private void updateGUI(int row, int col, int num) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                cells[row][col].setText(num == 0 ? "" : String.valueOf(num));
            }
        });
    }

    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int getSpeed() {
        try {
            return Integer.parseInt(speedInput.getText());
        } catch (NumberFormatException e) {
            return 50; // Default speed
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SudokuSolverGUI solver = new SudokuSolverGUI();
                solver.setVisible(true);
            }
        });
    }
}