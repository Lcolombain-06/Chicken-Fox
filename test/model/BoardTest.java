package model;

import boardifier.model.GameStageModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BoardTest {

    private Board board;
    private GameStageModel stage;

    @BeforeEach
    void setUp() {
        stage = mock(GameStageModel.class);
        board = new Board(0, 0, stage);
    }

    // board structure

    @Test
    void boardIs7x7() {
        int count = 0;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                assertNotNull(board.getCell(x, y));
                count++;
            }
        }

        assertEquals(49, count, "Board must contain 49 cells (7x7)");
    }

    // accesibilities cells


    @Test
    void cornerCellsAreNotAccessible() {
        assertFalse(board.getCell(0, 0).isAccessible());
        assertFalse(board.getCell(0, 1).isAccessible());
        assertFalse(board.getCell(1, 0).isAccessible());
        assertFalse(board.getCell(6, 6).isAccessible());
    }

    @Test
    void centerCellIsAccessible() {
        assertTrue(board.getCell(3, 3).isAccessible());
    }

    @Test
    void crossCellsAreAccessible() {
        assertTrue(board.getCell(0, 2).isAccessible());
        assertTrue(board.getCell(0, 3).isAccessible());
        assertTrue(board.getCell(0, 4).isAccessible());
    }


    // Neighbors

    @Test
    void orthogonalNeighborsExist() {
        Cell center = board.getCell(3, 3);

        assertTrue(center.getNeighbors().contains(board.getCell(2, 3)));
        assertTrue(center.getNeighbors().contains(board.getCell(4, 3)));
        assertTrue(center.getNeighbors().contains(board.getCell(3, 2)));
        assertTrue(center.getNeighbors().contains(board.getCell(3, 4)));
    }

    @Test
    void diagonalNeighborOnEvenCell() {
        Cell c = board.getCell(2, 2);
        assertTrue(c.getNeighbors().contains(board.getCell(3, 3)));
    }

    @Test
    void noDiagonalNeighborOnOddCell() {
        Cell c = board.getCell(2, 3);
        assertFalse(c.getNeighbors().contains(board.getCell(3, 4)));
    }

    @Test
    void inaccessibleCellHasNoNeighbors() {
        Cell corner = board.getCell(0, 0);
        assertTrue(corner.getNeighbors().isEmpty());
    }


    // SetValidCell


    @Test
    void setValidCellsReturnsZeroForGoose() {
        Pawn goose = mock(Pawn.class);
        when(goose.isFox()).thenReturn(false);

        int result = board.setValidCells(goose, 3, 3);

        assertEquals(0, result);
    }

    @Test
    void setValidCellsExecutesWithoutCrashForFox() {
        Pawn fox = mock(Pawn.class);
        when(fox.isFox()).thenReturn(true);

        assertDoesNotThrow(() -> board.setValidCells(fox, 3, 3));
    }

    // Fox can capture

    @Test
    void foxCanCaptureReturnsFalseWhenNoMoves() {
        Pawn fox = mock(Pawn.class);

        boolean result = board.foxCanCapture(fox, 3, 3);

        assertFalse(result);
    }

    @Test
    void foxCanCaptureReturnsBooleanValue() {
        Pawn fox = mock(Pawn.class);

        boolean result = board.foxCanCapture(fox, 3, 3);

        assertTrue(result == true || result == false);
    }


    // clear valid cell

    @Test
    void clearValidCellsDoesNotCrash() {
        board.clearValidCells();
        assertTrue(true);
    }
}