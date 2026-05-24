package model;

import boardifier.model.GameStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardTest {

    @Mock
    GameStageModel gameStageModel;

    private Board board;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        board = new Board(0, 0, gameStageModel);
    }

    // INITIALIZATION
    @Test
    void boardIsNotNull() {
        assertNotNull(board);
    }

    @Test
    void cornerCellsAreNotAccessible() {
        assertFalse(board.getCell(0, 0).isAccessible());
        assertFalse(board.getCell(1, 0).isAccessible());
        assertFalse(board.getCell(6, 0).isAccessible());
        assertFalse(board.getCell(0, 1).isAccessible());
        assertFalse(board.getCell(6, 6).isAccessible());
        assertFalse(board.getCell(5, 6).isAccessible());
    }

    @Test
    void centralCrossIsAccessible() {
        assertTrue(board.getCell(3, 0).isAccessible());
        assertTrue(board.getCell(3, 3).isAccessible());
        assertTrue(board.getCell(3, 6).isAccessible());
        assertTrue(board.getCell(0, 3).isAccessible());
        assertTrue(board.getCell(6, 3).isAccessible());
    }

    @Test
    void middleAreaCellsAreAccessible() {
        for (int y = 2; y <= 4; y++) {
            for (int x = 2; x <= 4; x++) {
                assertTrue(board.getCell(x, y).isAccessible(),
                        "Cell (" + x + "," + y + ") should be accessible");
            }
        }
    }

    @Test
    void getCellReturnsCorrectCoordinates() {
        Cell cell = board.getCell(3, 3);
        assertNotNull(cell);
        assertEquals(3, cell.getX());
        assertEquals(3, cell.getY());
    }

    @Test
    void allCellsHaveCorrectCoordinates() {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                Cell c = board.getCell(x, y);
                assertNotNull(c);
                assertEquals(x, c.getX());
                assertEquals(y, c.getY());
            }
        }
    }

    // NEIGHBORS

    @Test
    void inaccessibleCellsHaveNoNeighbors() {
        Cell corner = board.getCell(0, 0);
        assertFalse(corner.isAccessible());
        assertTrue(corner.getNeighbors().isEmpty());
    }

    @Test
    void centerCellHasAtLeastFourNeighbors() {
        Cell center = board.getCell(3, 3);
        assertFalse(center.getNeighbors().isEmpty());
        assertTrue(center.getNeighbors().size() >= 4);
    }

    @Test
    void evenCoordinateCellHasDiagonalNeighbors() {
        // (2,2): x+y=4 (even) → gets diagonal neighbors
        Cell evenCell = board.getCell(2, 2);
        assertTrue(evenCell.isAccessible());
        assertTrue(evenCell.getNeighbors().size() > 4);
    }

    @Test
    void oddCoordinateCellHasOnlyOrthogonalNeighbors() {
        // (3,2): x+y=5 (odd) → only orthogonal neighbors
        Cell oddCell = board.getCell(3, 2);
        assertTrue(oddCell.isAccessible());
        assertTrue(oddCell.getNeighbors().size() <= 4);
    }

    @Test
    void edgeCellNeighborsAreAllAccessible() {
        Cell edgeCell = board.getCell(3, 0);
        assertTrue(edgeCell.isAccessible());
        for (Cell neighbor : edgeCell.getNeighbors()) {
            assertTrue(neighbor.isAccessible(),
                    "Neighbor (" + neighbor.getX() + "," + neighbor.getY() + ") should be accessible");
        }
    }

    @Test
    void allNeighborsAreAccessible() {
        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                for (Cell neighbor : board.getCell(x, y).getNeighbors()) {
                    assertTrue(neighbor.isAccessible(),
                            "Non-accessible cell should not be neighbor of (" + x + "," + y + ")");
                }
            }
        }
    }


    // VALID CELLS / REACHABLE CELLS


    @Test
    void clearValidCellsResetsAllToFalse() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        board.setValidCells(fox, 3, 3);
        board.clearValidCells();

        boolean[][] reachable = board.getReachableCells();
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                assertFalse(reachable[r][c], "Cell [" + r + "][" + c + "] should be false after clear");
    }

    @Test
    void canReachCellFalseForEveryCellAfterClear() {
        board.clearValidCells();
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                assertFalse(board.canReachCell(r, c),
                        "canReachCell(" + r + "," + c + ") should be false after clearValidCells");
    }

    @Test
    void foxSetValidCellsReturnsPositiveCountAtCenter() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        int moves = board.setValidCells(fox, 3, 3);
        assertTrue(moves > 0, "Fox at center should have at least one valid move");
    }

    @Test
    void foxSetValidCellsMarksAtLeastOneCell() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        board.setValidCells(fox, 3, 3);
        boolean[][] reachable = board.getReachableCells();
        boolean anyReachable = false;
        outer:
        for (int r = 0; r < 7; r++)
            for (int c = 0; c < 7; c++)
                if (reachable[r][c]) { anyReachable = true; break outer; }
        assertTrue(anyReachable);
    }

    @Test
    void gooseSetValidCellsReturnsZero() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);
        when(goose.isGoose()).thenReturn(true);

        int result = board.setValidCells(goose, 3, 3);
        assertEquals(0, result);
    }

    @Test
    void gooseCannotMoveDown() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);
        when(goose.isGoose()).thenReturn(true);

        board.setValidCells(goose, 3, 3);
        boolean[][] reachable = board.getReachableCells();

        for (int c = 0; c < 7; c++)
            assertFalse(reachable[4][c], "Goose should not move down to row 4");
    }

    @Test
    void gooseCanMoveHorizontally() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);
        when(goose.isGoose()).thenReturn(true);

        board.setValidCells(goose, 3, 3);
        boolean[][] reachable = board.getReachableCells();

        assertTrue(reachable[3][2] || reachable[3][4],
                "Goose should be able to move left or right");
    }

    @Test
    void gooseCanMoveUp() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);
        when(goose.isGoose()).thenReturn(true);

        board.setValidCells(goose, 3, 3);
        boolean[][] reachable = board.getReachableCells();

        assertTrue(reachable[2][3], "Goose should be able to move straight up");
    }

    @Test
    void setValidCellsClearsPreviousState() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        board.setValidCells(fox, 3, 3);
        board.setValidCells(fox, 3, 0);

        boolean[][] reachable = board.getReachableCells();
        assertFalse(reachable[4][3], "Previous reachable cells should be cleared");
    }


    // FOX CAN CAPTURE

    @Test
    void foxCanCaptureReturnsFalseWithNoGeese() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        assertFalse(board.foxCanCapture(fox, 3, 3));
    }

    @Test
    void foxCanCaptureDoesNotThrowAtBoundaries() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        assertDoesNotThrow(() -> board.foxCanCapture(fox, 3, 0));
        assertDoesNotThrow(() -> board.foxCanCapture(fox, 3, 6));
        assertDoesNotThrow(() -> board.foxCanCapture(fox, 0, 3));
    }

    // EDGE CASES


    @Test
    void multipleClearCallsDoNotThrow() {
        assertDoesNotThrow(() -> {
            board.clearValidCells();
            board.clearValidCells();
            board.clearValidCells();
        });
    }

    @Test
    void foxAtTopCenterHasValidMoves() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);
        when(fox.isGoose()).thenReturn(false);

        int moves = board.setValidCells(fox, 3, 0);
        assertTrue(moves > 0, "Fox at top-center should have valid moves");
    }

    @Test
    void gooseAtTopRowCannotMoveUpFurther() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);
        when(goose.isGoose()).thenReturn(true);

        board.setValidCells(goose, 3, 0);
        boolean[][] reachable = board.getReachableCells();

        for (int c = 0; c < 7; c++)
            assertFalse(reachable[0][c], "Goose at row 0 cannot stay in row 0");
    }
}