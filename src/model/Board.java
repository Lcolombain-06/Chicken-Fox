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
    public int setValidCells(Pawn pawn, int row, int col) {
        //System.out.println("setValidCells called for " + (pawn.isFox() ? "FOX" : "GOOSE") + " at [" + row + "][" + col + "]");



        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                reachableCells[r][c] = false;

        Cell current = cells[row][col];
        if (pawn.isFox()) return setFoxValidCells(current, row, col);
        else { setGeeseValidCells(current, row, col); return 0; }
    }

    // check all the possible move of the fox (simple one and capture)
    private int setFoxValidCells(Cell current, int row, int col) {
        int nbrValidCells = 0;
        for (Cell neighbor : current.getNeighbors()) {
            int neighborX = neighbor.getX();
            int neighborY = neighbor.getY();

            if (getElement(neighborY, neighborX) == null) {
                reachableCells[neighborY][neighborX] = true;
                nbrValidCells++;
            } else {
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

    // check all the possible move of a goose
    private void setGeeseValidCells(Cell current, int row, int col) {

        for (Cell neighbor : current.getNeighbors()) {

            int nx = neighbor.getX();
            int ny = neighbor.getY();
            boolean vertical = (nx == col && ny < row);   // move up vertically
            boolean horizontal = (ny == row && nx != col); // move horizontally

            if ((vertical || horizontal) && getElement(ny, nx) == null) {
                reachableCells[ny][nx] = true;
            }
        }
    }

    // call after a fox's capture, to check if a new one is possible
    public boolean foxCanCapture(Pawn fox, int row, int col) {
        setValidCells(fox, row, col);
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (reachableCells[r][c] && (Math.abs(r - row) == 2 || Math.abs(c - col) == 2)) return true;
        return false;
    }

    // made to fill the board with cells
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

    // since the board isn't a square and some cells have diagonal neighbors,
    // we initialise a list of neighbors for each cell
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

    // when call by initNeigbors, add the neigbors of each cells in there list of neighbors
    private void addNeighbors(Cell c, int x, int y, int[][] directions) {
        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx < 0 || ny < 0 || nx >= 7 || ny >= 7) continue;
            if (cells[ny][nx].isAccessible()) c.addNeighbors(cells[ny][nx]);
        }
    }

    /**
     * Computes the total number of legal moves and captures available for the fox.
     * This is used by the AI to evaluate how well the fox is trapped.
     * @return the number of possible moves/captures
     */
    public int countPossibleFoxMoves() {
        int totalMoves = 0;

        int foxX = -1;
        int foxY = -1;
        Cell cellFox = null;

        // 1. LOCATE THE FOX ON THE BOARD
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (cells[y][x].isAccessible() && getElement(y, x) != null) {
                    Pawn p = (Pawn) getElement(y, x);
                    if (p.isFox()) {
                        foxX = x;
                        foxY = y;
                        cellFox = cells[y][x];
                        break;
                    }
                }
            }
        }

        // Security check: if fox is not found, return 0
        if (cellFox == null) return 0;

        // 2. INSPECT NEIGHBORS AND COUNT MOVES
        for (Cell neighbor : cellFox.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();


            // increment totalMoves by 1.
            if (getElement(ny, nx) == null) {
                totalMoves += 1;
            }
            else {
                // 1. Calculate jumpX and jumpY (using nx, ny, foxX, and foxY, exactly like in setValidCells)
                int jumpX = nx + (nx - foxX);
                int jumpY = ny + (ny - foxY);
                if (jumpX < 7 && jumpX >= 0 && jumpY >= 0 && jumpY < 7) {
                    Cell jumpToCell = cells[jumpY][jumpX];
                    if (jumpToCell.isAccessible() && getElement(jumpY, jumpX) == null) {
                        totalMoves += 1;
                    }
                }
            }
        }

        return totalMoves;
    }

    /**
     * Computes the total number of chickens currently vulnerable to an immediate fox capture.
     * This is used by the AI to avoid moves that give away free chickens.
     * @return the number of chickens in danger
     */
    public int countChickensInDanger() {
        int chickensInDanger = 0;

        int foxX = -1;
        int foxY = -1;
        Cell cellFox = null;

        // 1. LOCATE THE FOX ON THE BOARD
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                if (cells[y][x].isAccessible() && getElement(y, x) != null) {
                    Pawn p = (Pawn) getElement(y, x);
                    if (p.isFox()) {
                        foxX = x;
                        foxY = y;
                        cellFox = cells[y][x];
                        break;
                    }
                }
            }
        }

        if (cellFox == null) return 0;

        // 2. INSPECT NEIGHBORS TO FIND CHICKENS IN DANGER
        for (Cell neighbor : cellFox.getNeighbors()) {
            int nx = neighbor.getX();
            int ny = neighbor.getY();

            if (getElement(ny, nx) != null) {


                int jumpX = nx + (nx - foxX);
                int jumpY = ny + (ny - foxY);


                if (jumpX >= 0 && jumpX < 7 && jumpY >= 0 && jumpY < 7) {
                    Cell jumpToCell = cells[jumpY][jumpX];

                    if (jumpToCell.isAccessible() && getElement(jumpY, jumpX) == null) {
                        // 4. If true, increment chickensInDanger by 1.
                        chickensInDanger += 1;
                    }
                }
            }
        }

        return chickensInDanger;
    }

    public void clearValidCells() {
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                reachableCells[r][c] = false;
    }


}