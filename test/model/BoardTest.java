package model;

import model.Board;
import model.Cell;
import model.Pawn;
import boardifier.model.GameStageModel;
import boardifier.model.StageElementsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class BoardTest {

    private static class StubStageModel extends GameStageModel {
        public StubStageModel() {
            super("test", null);
        }

        @Override
        public StageElementsFactory getDefaultElementFactory() {
            return null;
        }
    }

    /**
     * The board being tested, recreated before each test.
     */
    private Board board;

    /**
     * Method called before each test.
     * It creates an empty board
     */
    @BeforeEach
    void setUp() {
        board = new Board(0, 0, new StubStageModel());
    }

    // Tests on cell accessibility
    /**
     * Verifies that the four corners of the board (and surrounding cells)
     * are correctly marked as inaccessible.**/
    @Test
    void cornerCellsAreNotAccessible() {
        // Corners of the 7x7 board: (row=0,col=0), (row=0,col=1), (row=1,col=0), (row=6,col=6)
        // Note: getCell(row, col) → row is passed first, col second
        assertFalse(board.getCell(0, 0).isAccessible(), "Corner (0,0) must not be accessible");
        assertFalse(board.getCell(0, 1).isAccessible(), "Corner (0,1) must not be accessible");
        assertFalse(board.getCell(1, 0).isAccessible(), "Corner (1,0) must not be accessible");
        assertFalse(board.getCell(6, 6).isAccessible(), "Corner (6,6) must not be accessible");
    }

    /**
     * Verifies that the cell at the center of the board
     * is accessible.
     * The cell (3,3)
     */
    @Test
    void centerCellIsAccessible() {
        assertTrue(board.getCell(3, 3).isAccessible(), "The center cell (3,3) must be accessible");
    }


    // Verifies that the cells of the upper arm of the cross are accessible.
    @Test
    void crossCellsAreAccessible() {
        assertTrue(board.getCell(0, 2).isAccessible(), "Cell (0,2) of the upper arm must be accessible");
        assertTrue(board.getCell(0, 3).isAccessible(), "Cell (0,3) of the upper arm must be accessible");
        assertTrue(board.getCell(0, 4).isAccessible(), "Cell (0,4) of the upper arm must be accessible");
    }

    // Tests on neighbors
    /**
     * Verifies that the center cell knows its neighbors
     * : top, bottom, left, right.
     */
    @Test
    void orthogonalNeighborsExist() {
        Cell center = board.getCell(3, 3);
        Cell up    = board.getCell(2, 3);  // one row above
        Cell down  = board.getCell(4, 3);  // one row below
        Cell left  = board.getCell(3, 2);  // one column to the left
        Cell right = board.getCell(3, 4);  // one column to the right

        assertTrue(center.getNeighbors().contains(up),    "Missing top neighbor");
        assertTrue(center.getNeighbors().contains(down),  "Missing bottom neighbor");
        assertTrue(center.getNeighbors().contains(left),  "Missing left neighbor");
        assertTrue(center.getNeighbors().contains(right), "Missing right neighbor");
    }

    // Tests on neighbors en diagonal

    /**
     * Verifies that an "even" cell (whose row+column sum is even)
     * does have diagonal neighbors.
     *
     * Only cells where (row + col) is even
     * have diagonal connections, which allows certain moves
     */
    @Test
    void diagonalNeighborOnEvenCell() {
        Cell c    = board.getCell(2, 2);  // (2+2)%2 == 0 → even cell
        Cell diag = board.getCell(3, 3);  // diagonal neighbor bottom-right
        assertTrue(c.getNeighbors().contains(diag),
                "The even cell (2,2) must have (3,3) as a diagonal neighbor");
    }

    /**
     * Verifies that an "odd" cell (whose row+column sum is odd)
     * does NOT have diagonal neighbors.
     *
     * Odd cells are connected only orthogonally.
     */
    @Test
    void noDiagonalNeighborOnOddCell() {
        Cell c    = board.getCell(2, 3);  // (2+3)%2 == 1 → odd cell
        Cell diag = board.getCell(3, 4);  // diagonal cell bottom-right
        assertFalse(c.getNeighbors().contains(diag),
                "The odd cell (2,3) must NOT have (3,4) as a diagonal neighbor");
    }


    // Tests on inaccessible cells

    /**
     * Verifies that an inaccessible cell (a corner) has no neighbors.
     *
     * Cells outside the playing area must not be connected
     * to the rest of the board: their neighbor list must be empty.
     */
    @Test
    void inaccessibleCellHasNoNeighbors() {
        Cell corner = board.getCell(0, 0);  // top-left corner, inaccessible
        assertTrue(corner.getNeighbors().isEmpty(),
                "An inaccessible cell must have no neighbors");
    }

}