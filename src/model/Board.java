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

    // during the game loop, call to check all the possible move of the selected cell (fox or goose),
    // and then, call the setValidCells of the type of pawn in function of which player turn it is
    /**
     * Calculates and highlights all valid destination cells for a given pawn.

     * during the game loop, call to check all the possible move of the selected cell (fox or goose),
     * and then, call the setValidCells of the type of pawn in function of which player turn it is
     *
     * @param pawn The pawn component moving (Fox or Goose).
     * @param row  The starting row index.
     * @param col  The starting column index.
     * @return The number of legal moves found for the fox, or 0 for the geese.
     */
    public int setValidCells(Pawn pawn, int row, int col) {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                reachableCells[r][c] = false;

        Cell current = cells[row][col];
        if (pawn.isFox()) return setFoxValidCells(current, row, col);
        else { setGeeseValidCells(current, row, col); return 0; }
    }

    /**
     * Calculates all valid movements for the Fox (standard steps and jumps).
     */
    private int setFoxValidCells(Cell current, int row, int col) {
        int nbrValidCells = 0;
        for (Cell neighbor : current.getNeighbors()) {
            int neighborX = neighbor.getX();
            int neighborY = neighbor.getY();

            if (getElement(neighborY, neighborX) == null) {
                reachableCells[neighborY][neighborX] = true;
                nbrValidCells++;
            }
            // Neighbor cell contains a goose, check for a valid jump
            else {
                int jumpX = neighborX + (neighborX - col);
                int jumpY = neighborY + (neighborY - row);
                if (jumpX < 7 && jumpX >= 0 && jumpY >= 0 && jumpY < 7) {
                    Cell jumpToCell = cells[jumpY][jumpX];
                    if (jumpToCell.isAccessible() && getElement(jumpY, jumpX) == null) {
                        reachableCells[jumpY][jumpX] = true;
                        nbrValidCells++;
                    }
                }
            }
        }
        return nbrValidCells;
    }

    /**
     * Calculates all valid movements for a Goose (only left, right, or up).
     */
    private void setGeeseValidCells(Cell current, int row, int col) {

        for (Cell neighbor : current.getNeighbors()) {

            int nx = neighbor.getX();
            int ny = neighbor.getY();
            boolean vertical = (nx == col && ny < row);   // Only moving up vertically
            boolean horizontal = (ny == row && nx != col); // Moving left or right horizontally

            if ((vertical || horizontal) && getElement(ny, nx) == null) {
                reachableCells[ny][nx] = true;
            }
        }
    }


    /**
     * After a fox's capture, checks if the fox can execute another capture move from its spot.
     *
     * @param fox The fox pawn element
     * @param row Current row index
     * @param col Current column index
     * @return true if a capture jump is available, false otherwise
     */
    public boolean foxCanCapture(Pawn fox, int row, int col) {
        setValidCells(fox, row, col);
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (reachableCells[r][c] && (Math.abs(r - row) == 2 || Math.abs(c - col) == 2)) return true;
        return false;
    }

    /**
     * Fills the 7x7 internal grid matrix with initialized Cell objects.
     */
    private void initBoard() {
        cells = new Cell[7][7];
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                boolean accessible = !((x < 2 || x > 4) && (y < 2 || y > 4));
                cells[y][x] = new Cell(x, y, accessible);
            }
        }
        initNeighbors();
    }

    /**
     * Since the board isn't a square and some cells have diagonal neighbors,
     * we initialize a list of neighbors for each cell
     */
    private void initNeighbors() {
        int[][] orthogonal = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int[][] diagonal   = {{-1, -1}, {1, -1}, {-1, 1}, {1, 1}};

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Cell c = cells[y][x];
                if (!c.isAccessible()) continue;
                addNeighbors(c, x, y, orthogonal);
                if ((x + y) % 2 == 0) addNeighbors(c, x, y, diagonal);
            }
        }
    }

    /**
     * when call by initNeigbors, add the neigbors of each cells in there list of neighbors
     */
    private void addNeighbors(Cell c, int x, int y, int[][] directions) {
        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || ny < 0 || nx >= 7 || ny >= 7) continue;
            if (cells[ny][nx].isAccessible()) c.addNeighbors(cells[ny][nx]);
        }
    }

    /**
     * Resets the entire reachable cells selection matrix to false
     */
    public void clearValidCells() {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                reachableCells[r][c] = false;
    }


}



