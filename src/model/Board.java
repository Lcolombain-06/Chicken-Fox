package model;

import boardifier.model.GameStageModel;
import boardifier.model.ContainerElement;

/**
 * Main board represent the element where pawns are put when played
 * Thus, a simple ContainerElement with 7 rows and 7 column is needed.
 * However, not all cell are reachable, since the board have a "plus" shape.
 * Nevertheless, in order to "simplify" the work for the controller part,
 * this class also contains method to determine all the valid cells to put a
 * pawn with a given value.
 */
public class Board extends ContainerElement {
    private Cell[][] cells;

    public Board(int x, int y, GameStageModel gameStageModel) {
        super("board", x, y, 7, 7, gameStageModel);
        initBoard();
    }

    public Cell getCell(int x, int y) {
        return this.cells[y][x];
    }

    public int setValidCells(Pawn pawn, int row, int col) {
        resetReachableCells();
        Cell current = cells[row][col];
        return pawn.isFox() ? setFoxValidCells(current, row, col) : setGooseValidCells(current, row);
    }

    public boolean foxCanCapture(Pawn fox, int row, int col) {
        setValidCells(fox, row, col);
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (reachableCells[r][c] && isJump(r, c, row, col)) return true;
            }
        }
        return false;
    }

    // --- private helpers (made because of the refractor of the big main methods into smaller ones ---

    private void resetReachableCells() {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                reachableCells[r][c] = false;
    }

    private boolean isJump(int r, int c, int fromRow, int fromCol) {
        return Math.abs(r - fromRow) == 2 || Math.abs(c - fromCol) == 2;
    }

    private boolean isFree(int row, int col) {
        return getElement(row, col) == null;
    }

    private int setFoxValidCells(Cell current, int row, int col) {
        int count = 0;
        for (Cell neighbor : current.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            if (isFree(ny, nx)) {
                reachableCells[ny][nx] = true;
                count++;
            } else {
                count += checkFoxJump(nx, ny, row, col);
            }
        }
        return count;
    }

    private int checkFoxJump(int neighborX, int neighborY, int fromRow, int fromCol) {
        int jumpX = neighborX + (neighborX - fromCol);
        int jumpY = neighborY + (neighborY - fromRow);
        if (jumpX < 0 || jumpX >= 7 || jumpY < 0 || jumpY >= 7) return 0;
        if (cells[jumpY][jumpX].isAccessible() && isFree(jumpY, jumpX)) {
            reachableCells[jumpY][jumpX] = true;
            return 1;
        }
        return 0;
    }

    private int setGooseValidCells(Cell current, int row) {
        for (Cell neighbor : current.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();
            if (ny <= row && (nx == current.getX() || ny == row) && isFree(ny, nx)) {
                reachableCells[ny][nx] = true;
            }
        }
        return 0; // geese don't need a count
    }

    // main init for board's cells
    private void initBoard() {
        cells = new Cell[7][7];
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++)
                cells[y][x] = new Cell(x, y, isAccessible(x, y));
        initNeighbors();
    }

    private boolean isAccessible(int x, int y) {
        return !((x < 2 || x > 4) && (y < 2 || y > 4));
    }


    // methods to add the neighbors into the cell neighbors list
    private void initNeighbors() {
        int[][] orthogonal = {{-1,0},{1,0},{0,-1},{0,1}};
        int[][] diagonal   = {{-1,-1},{1,-1},{-1,1},{1,1}};

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Cell c = cells[y][x];
                if (!c.isAccessible()) continue;
                addNeighbors(c, x, y, orthogonal);
                if ((x + y) % 2 == 0) addNeighbors(c, x, y, diagonal);
            }
        }
    }

    private void addNeighbors(Cell c, int x, int y, int[][] directions) {
        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || ny < 0 || nx >= 7 || ny >= 7) continue;
            if (cells[ny][nx].isAccessible()) c.addNeighbors(cells[ny][nx]);
        }
    }
}
